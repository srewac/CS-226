package main;

import java.io.*;
import java.util.Arrays;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.feature.*;
import org.apache.spark.sql.*;
import scala.Tuple2;


public class single {
    private static SparkSession spark = null;
/*    private static JavaSparkContext sc = null;
    public static JavaSparkContext initContext(){
        if (sc == null)
            sc = new JavaSparkContext(new SparkConf());
        return sc;
    }*/

    public static SparkSession initSpark() {
        if (spark == null) {
            spark = SparkSession
                    .builder()
                    .appName("TF.IDF")
                    .getOrCreate();
        }
        return spark;
    }

    public static Dataset<Row> initReddit(String path, String src) {
        SparkSession spark = initSpark();

        Dataset<Row> df = spark.read()
                .json(path+"/"+src+"/COMMENTS_"+src+".json")
                .select("body");
        Dataset<Row> new_df = df
                .withColumn("body", functions.regexp_replace(df.col("body"),"[^a-zA-Z.'?!]+"," "));
        Dataset<Row> new_df1 = new_df
                .withColumn("body", functions.trim(new_df.col("body")));

        Dataset<Row> new_df2 = new_df1
                .withColumn("body", functions.regexp_replace(new_df1.col("body"), "\\s+", " "))
                .filter("body != '\\s+deleted'")
                .filter("body != 'deleted'")
                .filter("body != ''");
        new_df2.collect();
        new_df2.show(5);

        Tokenizer tokenizer = new Tokenizer()
                .setInputCol("body")
                .setOutputCol("token");
        Dataset<Row> wordsData = tokenizer.transform(new_df2);

        StopWordsRemover remover = new StopWordsRemover()
                .setInputCol("token")
                .setOutputCol("filtered");
        Dataset<Row> wordFiltered = remover
                .transform(wordsData)
                // .filter("filtered != null")
                .withColumn("label", functions.lit(src));
        wordFiltered.show(5);
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

    public static Dataset<Row> initTwitter(String path) {
        SparkSession spark = initSpark();

        JavaSparkContext sc = new JavaSparkContext(spark.sparkContext());
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
        twDF.show(5);

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
        wordFiltered.show(5);
        return wordFiltered.select("label", "filtered");
    }
    public Dataset<Row> getValue(String path, String tw) {

        SparkSession spark = initSpark();

        // final HashingTF hTF = new HashingTF();
		/*
		 	mllib
			spark
		 */
        single s = new single();
        Dataset<Row> twitter = initTwitter(tw);
        Dataset<Row> reddit;

        String[] dir = s.findDir(path);
        for (String d : dir) {
            if (d.equals("movie")) continue;
            reddit = initReddit(path, d);
            twitter = twitter.union(reddit);
        }
        Dataset<Row> df = twitter;

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

    public static void similarDataset () {

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

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        String input = args[0], output = args[1], tw = args[2];
        single s = new single();
        String[] dir = s.findDir(input);
        s.getValue(input, tw);

/*        boolean append = true;
        boolean autoFlush = true;
        String charset = "UTF-8";
        String filePath = output;
        String tmp;*/

/*        File file = new File(filePath);
        FileOutputStream fos;
        OutputStreamWriter osw;
        BufferedWriter bw;
        PrintWriter pw;


        for (String d : dir) {
            if (d.equals("movie")) continue;
            if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
            fos = new FileOutputStream(file, append);
            osw = new OutputStreamWriter(fos, charset);
            bw = new BufferedWriter(osw);
            pw = new PrintWriter(bw, autoFlush);
            pw.write(d);
            break;
        }*/
    }
}
