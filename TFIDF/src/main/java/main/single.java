package main;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.feature.*;
import org.apache.spark.ml.linalg.BLAS;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.*;
import scala.Tuple2;

import java.io.*;
import java.util.Arrays;

import static org.apache.spark.sql.functions.*;


public class single {
    private static SparkSession spark = null;
    private static JavaSparkContext sc = null;
    public static JavaSparkContext initContext(){
        if (sc == null)
            sc = new JavaSparkContext(initSpark().sparkContext());
        return sc;
    }

    public static SparkSession initSpark() {
        if (spark == null) {
            spark = SparkSession
                    .builder()
                    .appName("TF.IDF")
                    .getOrCreate();
        }
        return spark;
    }

    public Dataset<Row> initReddit(String path, String src) {
        SparkSession spark = initSpark();

        Dataset<Row> df = spark.read()
                .json(path+"/"+src+"/COMMENTS_"+src+".json")
                // .filter("score > 10")
                .select("body", "score")
                .orderBy(col("score").desc()).limit(25);
        Dataset<Row> new_df = df
                .withColumn("body", functions.regexp_replace(df.col("body"),"[^a-zA-Z.'?!]+"," "));
        Dataset<Row> new_df1 = new_df
                .withColumn("body", functions.trim(new_df.col("body")))
                .filter("body != '\\s+deleted'")
                .filter("body != ''");

        // new_df2.show(5);

        Tokenizer tokenizer = new Tokenizer()
                .setInputCol("body")
                .setOutputCol("token");
        Dataset<Row> wordsData = tokenizer.transform(new_df1);

        StopWordsRemover remover = new StopWordsRemover()
                .setInputCol("token")
                .setOutputCol("filtered");
        Dataset<Row> wordFiltered = remover
                .transform(wordsData)
                // .filter("filtered != null")
                .withColumn("label", functions.lit("Reddit"));
        // wordFiltered.show(5);
        return wordFiltered.select("label", "filtered");
    }

/*    public Tuple2<String, String[]> tw(String line) {
        String[] str = line
*//*                .replace(" ;['",";")
                .replace("']","")
                .replace("', '", ",")*//*
                .split(";");
        Tuple2<String, String[]> res = new Tuple2<String, String[]>(
                str[0].toString(), str[1].split(",")
        );
        return res;
    }*/

    public static class TW implements Serializable {
        private String label;
        private String body;

        public String getBody() {
            return body;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }

    public Dataset<Row> initTwitter(String path) {
        SparkSession spark = initSpark();

        JavaSparkContext sc = initContext();
        JavaRDD<String> in = sc.textFile(path);

        JavaRDD<TW> table = in
                .map(line -> {
                    String[] parts = line.split(";");
                    TW tw = new TW();
                    tw.setLabel(parts[0].trim());
                    tw.setBody(parts[1]
                            .replaceAll("\\['", "")
                            .replaceAll("', '", " ")
                            .replaceAll("']",""));
                    return tw;
                });

        Dataset<Row> twDF = spark.createDataFrame(table, TW.class);
        // twDF.show(5);

        Tokenizer tokenizer = new Tokenizer()
                .setInputCol("body")
                .setOutputCol("token");
        Dataset<Row> wordsData = tokenizer.transform(twDF);

        StopWordsRemover remover = new StopWordsRemover()
                .setInputCol("token")
                .setOutputCol("filtered");
        Dataset<Row> wordFiltered = remover
                .transform(wordsData);
                // .filter("filtered != null");
        // wordFiltered.show(5);
        return wordFiltered.select("label", "filtered");
    }
    public Dataset<Row> getValue(String path, String tw, String d) {

        SparkSession spark = initSpark();
/*        StructType schema = DataTypes.createStructType(new StructField[] {
            new StructField("label", DataTypes.StringType, true, Metadata.empty()),
                new StructField("filtered", new ArrayType(DataTypes.StringType, true), true, Metadata.empty())
        });*/


        single s = new single();
//        String[] dir = s.findDir(path);
//
//        Dataset<Row> tmp = s.initReddit(path, dir[0])
//                .limit(1).withColumn("label", when(col("label").equalTo("Reddit"), "X"));
//        Dataset<Row> reddit1;


        Dataset<Row> reddit = s.initReddit(path, d);

        Dataset<Row> twitter = s.initTwitter(tw);
        Dataset<Row> df = twitter.union(reddit);

/*        CountVectorizerModel cv = new CountVectorizer()
                .setInputCol("filtered")
                .setOutputCol("rawFeatures")
                .setVocabSize(50000)
                .setMinDF(2)
                .fit(wordFiltered);
        Dataset<Row> featurizedData = cv.transform(wordFiltered);
        featurizedData.show(5);*/

        HashingTF hashingTF = new HashingTF()
                .setInputCol("filtered")
                .setOutputCol("rawFeatures");

        Dataset<Row> featurizedData = hashingTF.transform(df);
        // featurizedData.show(5);

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
        return rescaledData.select("label", "features");
    }

    public static class SimilarText implements Serializable {
        private long label1;
        private long label2;
        private double similarity;

        public long getLabel1() {
            return label1;
        }

        public long getLabel2() {
            return label2;
        }

        public double getSimilarity() {
            return similarity;
        }

        public void setLabel1(long label1) {
            this.label1 = label1;
        }

        public void setLabel2(long label2) {
            this.label2 = label2;
        }

        public void setSimilarity(double similarity) {
            this.similarity = similarity;
        }
    }
    public static Dataset<Row> similarDataset (Dataset<Row> res) {
        SparkSession spark = initSpark();
        spark.conf().set("spark.sql.crossJoin.enabled", "true");
        Dataset<Row> reddit = res.filter("label = 'Reddit'").withColumn("id", monotonically_increasing_id());
        Dataset<Row> twitter = res.filter("label = 'Twitter'").withColumn("id", monotonically_increasing_id());

//        UDF1 dot = new UDF1<Row[], double[]>() {
//            @Override
//            public double[] call(Row[] row) throws Exception {
//                Row r = row[0], t = row[1];
//                String
//                return new double[0];
//            }
//        };
        Dataset<Row> jj = twitter.select("id", "features").join(reddit.select("id", "features"));
        // jj.show(10);

        JavaRDD<SimilarText> similarTextDataset = jj.toJavaRDD()
                .map(r -> {
                        long label1 = r.getLong(0);
                        long label2 = r.getLong(2);

                        Vector fTwitter = r.getAs(1);
                        Vector fR = r.getAs(3);

                        double ddot = BLAS.dot(fTwitter.toSparse(), fR.toSparse());
                        double v1 = Vectors.norm(fTwitter.toSparse(), 2.0);
                        double v2 = Vectors.norm(fR.toSparse(), 2.0);
                        double sim = ddot / (v1 * v2);

                        SimilarText similarText = new SimilarText();
                        similarText.setLabel1(label1);
                        similarText.setLabel2(label2);
                        similarText.setSimilarity(sim);
                        return similarText;
                });

        Dataset<Row> sim = spark.createDataFrame(
                similarTextDataset,
                SimilarText.class
        );
/*        Dataset<Row> remain = sim.filter("similarity > 0");
        System.out.println("cosine");*/
        // remain.show(10);
        // sim.select("label1", "label2", "similarity").createOrReplaceTempView("tmp");
/*        .withColumn("rank",
                functions.rank().over(Window.partitionBy("label1").orderBy("similarity")))*/
/*        Dataset<Row> ds1 = sim
                .groupBy(col("label1").alias("label"))
                .agg(max("similarity").alias("max")
                        , min("similarity").alias("min")
                        , avg("similarity").alias("avg")
                        , stddev("similarity").alias("dev"));
        Dataset<Row> ds2 = sim
                .groupBy(col("label1").alias("label"))
                .agg(max("similarity").alias("max")
                        , min("similarity").alias("min")
                        , avg("similarity").alias("avg")
                        , stddev("similarity").alias("dev"));

        Dataset<Row> ds = ds1.select("label", "max", "min", "avg", "dev")
                .union(ds2.select("label", "max", "min", "avg", "dev"));
        ds.show(5);*/
        return sim.filter("similarity > 0.0");
    }

    public String[] findDir (String path) {
        File file = new File(path);
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        System.out.println(Arrays.toString(directories));
        return directories;
    }

    public static void main(String[] args) throws IOException {
        String input = args[0], output = args[1], tw = args[2], output1 = args[3];
        single s = new single();
        // similarDataset(getValue(input, tw));


/*        JavaRDD<String> res = init.toJSON().toJavaRDD();

        // System.out.println(res.toString());
        res.saveAsTextFile(output);*/
        String[] dir = s.findDir(input);

        for (String d : dir) {
            if (d.equals("movie")) break;
            JavaRDD<String> res1 = similarDataset(s.getValue(input, tw, d)).toJSON().toJavaRDD();
            res1.saveAsTextFile(output1+"/"+d);
        }


    }
}
