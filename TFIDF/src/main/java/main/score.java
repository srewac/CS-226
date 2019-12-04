package main;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.*;

import java.io.*;

public class score {
    private static SparkSession spark = null;

    public static SparkSession initSpark() {
        if (spark == null) {
            spark = SparkSession
                    .builder()
                    .appName("TF.IDF")
                    .getOrCreate();
        }
        return spark;
    }
    public static Dataset<Row> getValue(String path, String src) {
        String col = "score";
        SparkSession spark = initSpark();
        Dataset<Row> df = spark.read().json(path+"/"+src+"/COMMENTS_"+src+".json").select(col);
        df.describe().show();

        Dataset<Row> des = df
                .select(functions.mean(col).alias("mean"), functions.min(col).alias("min"),
                        functions.max(col).alias("max"), functions.stddev(col).alias("stddev"));
        des.show();
        // Dataset<Row> df1 = spark.read().json(path+"/"+src+"/SUBMISSION_"+src+".json").select(col, "upvote_ratio");
        return des.select( "mean", "stddev", "min", "max");
    }
    public static void main(String[] args) throws IOException {
        String input = args[0], output = args[1];
        single s = new single();

        String[] dir = s.findDir(input);
        JavaRDD<String> df;
        for (String d : dir) {
            df = getValue(input, d).toJSON().toJavaRDD();
            df.saveAsTextFile(output +"/"+ d);
        }

/*        File file = new File(output);
        if (!file.exists()) {
            file.getParentFile().mkdir();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream fos = new FileOutputStream(file, true);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
        PrintWriter pw = new PrintWriter(writer, true);
        String res;
        for (String d: dir) {
            if (d.equals("movie")) break;
            res = getValue(input, d);
            System.out.println(res);
            pw.write(res);
        }
        pw.close();
        writer.close();
        fos.close();*/

    }

}
