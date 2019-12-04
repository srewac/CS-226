package main;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import org.apache.commons.lang.ArrayUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.IDFModel;
import org.apache.spark.ml.linalg.Vector;

import org.apache.spark.sql.*;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.api.java.UDF2;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.types.DataTypes;
import scala.collection.Seq;

import static main.single.*;
import static org.apache.spark.sql.functions.*;


public class multi {
    public static class KeyWords implements Serializable {
        private String label;
        private String key;
        private double value;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

    public static Dataset<Row> getValue(String path, String tw, String d) {

        SparkSession spark = initSpark();

        single s = new single();
        Dataset<Row> reddit = s.initReddit(path, d);
        Dataset<Row> twitter = s.initTwitter(tw);
        Dataset<Row> df = twitter.union(reddit);


        HashingTF hashingTF = new HashingTF()
                .setInputCol("filtered")
                .setOutputCol("rawFeatures");

        Dataset<Row> featurizedData = hashingTF.transform(df);
        featurizedData.show(5);

        // IDF is an Estimator which is fit on a dataset and produces an IDFModel
        IDF idf = new IDF()
                .setInputCol("rawFeatures")
                .setOutputCol("features");
        IDFModel idfModel = idf.fit(featurizedData);

        // The IDFModel takes feature vectors (generally created from HashingTF or CountVectorizer) and scales each column
        Dataset<Row> rescaledData = idfModel.transform(featurizedData);
        rescaledData.show(5);

        // Get Top N data and filter deleted row

        // SparseVector s = new SparseVector();
        return rescaledData.select("label", "filtered", "features");
    }

    public static Dataset<Row> slice(String path, String in, String d, String fil){
        SparkSession spark = initSpark();
        Dataset<Row> df = getValue(path, in, d);
        int num;
        if (fil.equals("'Twitter'"))
            num = 2000;
        else
            num = 20;

        JavaRDD<KeyWords> key1 = df
                .select("label", "filtered", "features")
                .javaRDD()
                .map(r -> {
                        String label = r.getString(0);
                        List<Object> fuckU = r.getList(1);
                        Object[] ff = fuckU.toArray();

                        Vector tmp = r.getAs(2);
                        double[] value = tmp.toSparse().values();
                        List a = Arrays.asList(ArrayUtils.toObject(value));
                        double max = (double) Collections.max(a);

                        int idx = a.indexOf(max);
                        String key = ff[idx].toString();

                        KeyWords keyWords = new KeyWords();
                        keyWords.setLabel(label);
                        keyWords.setKey(key);
                        keyWords.setValue(max);
                        return keyWords;
                });
        Dataset<Row> keyword = spark.createDataFrame(
                key1,
                KeyWords.class
        ); // .withColumn("id", monotonically_increasing_id());

/*        UDF2 concatItems = new UDF2<Seq<Object>, Seq<Double>, Seq<String>>() {
            public Seq<String> call(final Seq<Object> col1, final Seq<Double> col2) throws Exception {
                ArrayList zipped = new ArrayList();

                for (int i = 0, listSize = col1.size(); i < listSize; i++) {
                    String subRow = col1.apply(i).toString() + ";" + col2.apply(i).toString();
                    zipped.add(subRow);
                }

                return scala.collection.JavaConversions.asScalaBuffer(zipped);
            }
        };*/

        Dataset<Row> mean = keyword
                .groupBy(col("key")).agg(first("label").as("label"), sum("value").as("value"));
        Dataset<Row> rank = mean
                .withColumn("rank", rank().over(Window.partitionBy("label").orderBy(col("value").desc())))
                .filter("rank <= "+num).filter("label ="+fil)
                .drop("rank");
        return rank.select("label", "key", "value");
    }

    public static void main(String[] args) throws IOException {
        String input = args[0], output = args[1], tw = args[2], output1 = args[3];

        single s = new single();
        String[] dir = s.findDir(input);

        for (String d : dir) {
            if (d.equals("movie")) break;
            JavaRDD<String> df = slice(input, tw,d, "'Reddit'").toJSON().toJavaRDD();

            df.saveAsTextFile(output+"/"+d);
        }
        JavaRDD<String> df = slice(input, tw, dir[-1], "'Twitter'").toJSON().toJavaRDD();

        df.saveAsTextFile(output+"/T");

    }

}
