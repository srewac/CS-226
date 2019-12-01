package main;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;

import java.io.*;

public class score {
    public String getValue(String path, String src) {
        String col = "score";
        SparkSession spark = SparkSession
                .builder()
                .appName("TF.IDF")
                // .config("")
                .getOrCreate();
        Dataset<Row> df = spark.read().json(path+"/"+src+"/COMMENTS_"+src+".json").select(col);
        df.describe().show();

        Dataset<Row> des = df
                .select(functions.mean(col).alias("mean"), functions.min(col).alias("min"),
                        functions.max(col).alias("max"), functions.stddev(col).alias("stddev"));
        des.show();
        Dataset<Row> df1 = spark.read().json(path+"/"+src+"/SUBMISSION_"+src+".json").select(col, "upvote_ratio");
        //    .withColumn("name", functions.lit(src))
        //    .withColumn("glo_score", functions.lit(df1.select(col).head().getInt(0)))
        //    .withColumn("upvote_ratio", functions.lit(df1.select("upvote_ratio").head().getInt(0)));
        des.show();
        Dataset<Row> attr = des
                .withColumn("label", functions.lit(src))
                .withColumn("glo_score", functions.lit(df1.select(col).head().getLong(0)))
                .withColumn("upvote_ratio", functions.lit(df1.select("upvote_ratio").head().getDouble(0)));
        attr.show();
        String res = attr.toJSON().toString();
        spark.close();
        return res;
    }
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        String input = args[0], output = args[1];
        single s = new single();
        score s1 = new score();
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
            if (d.equals("movie")) continue;
            if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
            fos = new FileOutputStream(file, append);
            osw = new OutputStreamWriter(fos, charset);
            bw = new BufferedWriter(osw);
            pw = new PrintWriter(bw, autoFlush);
            tmp = s1.getValue(input, d);
            pw.write(tmp);
        }
    }

}
