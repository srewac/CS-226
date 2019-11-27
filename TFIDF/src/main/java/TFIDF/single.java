package TFIDF;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.IDFModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;


public class single {

    public String getValue(String path, String src) {
//        SparkConf conf = new SparkConf();
//        conf.setAppName("WordCounter").setMaster("local");
//        JavaSparkContext sc = new JavaSparkContext(conf);
        SparkSession spark = SparkSession
                .builder()
                .appName("TF.IDF")
                // .config("")
                .getOrCreate();

        // final HashingTF hTF = new HashingTF();

		/*
		 	hello mllib
			spark
			goodBye spark
			hello spark
			goodBye spark
		 */
		Dataset<Row> df = spark.read().json(path+"/"+src+"/COMMENTS_"+src+".json").select("body");
        df.show(5);
        Tokenizer tokenizer = new Tokenizer().setInputCol("body").setOutputCol("words");
        Dataset<Row> wordsData = tokenizer.transform(df);

        HashingTF hashingTF = new HashingTF()
                .setInputCol("words")
                .setOutputCol("rawFeatures");

        Dataset<Row> featurizedData = hashingTF.transform(wordsData);
        featurizedData.show(5); 

        // IDF is an Estimator which is fit on a dataset and produces an IDFModel
        IDF idf = new IDF()
                .setInputCol("rawFeatures").setOutputCol("features");
        IDFModel idfModel = idf.fit(featurizedData);
    
        // The IDFModel takes feature vectors (generally created from HashingTF or CountVectorizer) and scales each column
        Dataset<Row> rescaledData = idfModel.transform(featurizedData);

		// JavaRDD<String> text = df.toJavaRDD().map(s -> s.getAs("body").toString());

        // JavaRDD<List<String>> wordFlow = text
		// 		.map(new Function<String, List<String>>() {
        //             @Override
        //             public List<String> call(String line) throws Exception {
		// 				String[] words = line.replaceAll("[^a-zA-Z\\s]", "").split(" ");
		// 				return Arrays.asList(words);
		// 			}

        //         }).cache();
                
        // JavaRDD<Vector> tf = hTF.transform(wordFlow).cache();
        // IDFModel idf = new IDF().fit(tf);

        // JavaRDD<Vector> tfIdf = idf.transform(tf);
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
