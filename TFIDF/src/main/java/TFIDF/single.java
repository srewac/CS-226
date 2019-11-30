package TFIDF;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.feature.*;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;


public class single {

    public String getValue(String path, String src) {


        SparkSession spark = SparkSession
                .builder()
                .appName("TF.IDF")
                // .config("")
                .getOrCreate();

        // final HashingTF hTF = new HashingTF();

		/*
		 	mllib
			spark
		 */

		Dataset<Row> df = spark.read().json(path+"/"+src+"/COMMENTS_"+src+".json").select("body");
        df.show(5);
        df.withColumn("body", functions.regexp_replace(df.col("body"), "[^a-zA-Z.',?!]", " "))
                .filter("body != ' '")
                .filter("body != ''"); // remove strange symbols include 0-9,.?!
        df.show(5);

        Tokenizer tokenizer = new Tokenizer()
                .setInputCol("body")
                .setOutputCol("token");
        Dataset<Row> wordsData = tokenizer.transform(df);

        StopWordsRemover remover = new StopWordsRemover()
                .setInputCol("token")
                .setOutputCol("filtered");
        Dataset<Row> wordFiltered = remover.transform(wordsData);
        wordFiltered.show(5); 

        HashingTF hashingTF = new HashingTF()
                .setInputCol("filtered")
                .setOutputCol("rawFeatures");
                // .setNumFeatures(262144);

        Dataset<Row> featurizedData = hashingTF.transform(wordFiltered);
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
        // rescaledData.select(" where ");


        List<String> list = new ArrayList();
        for(Row row:rescaledData.collectAsList()){
            list.add(row.toString());
        }
        spark.close();

        return list.toString();
    }

    public String[] findDir(String path) {
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
        String input = args[0], output = args[1];
        single s = new single();
        String[] dir = s.findDir(input);

        boolean append = true;
        boolean autoFlush = true;
        String charset = "UTF-8";
        String filePath = output;
        String tmp;

        File file = new File(filePath);
        FileOutputStream fos;
        OutputStreamWriter osw;
        BufferedWriter bw;
        PrintWriter pw;


        for (String d : dir) {
            if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
            fos = new FileOutputStream(file, append);
            osw = new OutputStreamWriter(fos, charset);
            bw = new BufferedWriter(osw);
            pw = new PrintWriter(bw, autoFlush);
            pw.write(d);
            tmp = s.getValue(input,d);
            pw.write(tmp);
        }
    }
}
