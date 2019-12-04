package main;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;

import java.util.Arrays;

import static main.single.initContext;
import static main.single.initSpark;


public class all {
    public static void main(String[] args) {
        String input = args[0], output = args[1], tw = args[2], output1 = args[3];

        single s = new single();
        all a = new all();
        // similarDataset(getValue(input, tw));
        String[] dir = s.findDir(input);
        Dataset<Row> ini = a.initTwitter(tw);
        JavaRDD<String> re = ini.toJSON().toJavaRDD();
        re.saveAsTextFile(output+"/"+"T");

        for (String d : dir) {
            if (d.equals("movie")) break;
            Dataset<Row> init = a.initRed(input, d);
            JavaRDD<String> res = init.toJSON().toJavaRDD();
            res.saveAsTextFile(output+"/"+d);
        }
    }
    public  Dataset<Row> initRed(String path, String src) {
        SparkSession spark = initSpark();

        Dataset<Row> df = spark.read()
                .json(path+"/"+src+"/COMMENTS_"+src+".json")
                // .filter("score > 10")
                .select("body");
        Dataset<Row> new_df = df
                .withColumn("body", functions.regexp_replace(df.col("body"),"[^a-zA-Z.'?!]+"," "));
        Dataset<Row> new_df1 = new_df
                .withColumn("body", functions.trim(new_df.col("body")))
                .filter("body != '\\s+deleted'")
                .filter("body != ''");
        Tokenizer tokenizer = new Tokenizer()
                .setInputCol("body")
                .setOutputCol("token");
        Dataset<Row> wordsData = tokenizer.transform(new_df1);

        StopWordsRemover remover = new StopWordsRemover()
                .setInputCol("token")
                .setOutputCol("filtered");
        Dataset<Row> wordFiltered = remover
                .transform(wordsData);
        return wordFiltered.select("filtered").withColumn("word", functions.explode(functions.col("filtered"))).drop("filtered");
    }
    public Dataset<Row> initTwitter(String path) {
        SparkSession spark = initSpark();

        JavaSparkContext sc = initContext();
        JavaRDD<String> in = sc.textFile(path);

        JavaRDD<single.TW> table = in
                .map(line -> {
                    String[] parts = line.split(";");
                    single.TW tw = new single.TW();
                    tw.setLabel(parts[0].trim());
                    tw.setBody(parts[1]
                            .replaceAll("\\['", "")
                            .replaceAll("', '", " ")
                            .replaceAll("']",""));
                    return tw;
                });

        Dataset<Row> twDF = spark.createDataFrame(table, single.TW.class).limit(1000);
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
        return wordFiltered.select("filtered").withColumn("word", functions.explode(functions.col("filtered"))).drop("filtered");

    }
}
