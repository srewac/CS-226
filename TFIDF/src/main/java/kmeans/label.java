package kmeans;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

import static kmeans.kmeans.initContext;
import static kmeans.kmeans.initSC;

public class label {
    public void removeLabel(String input, String output) {
        SparkSession spark = initSC();
        JavaSparkContext jsc = initContext();
        // Load and parse data
        JavaRDD<String> data = jsc.textFile(input);
        JavaRDD<String> res = data.map(s -> {
            if (s.charAt(0)=='T')
                return s.substring(9, s.length());
            else
                return s.substring(8, s.length());
        }).coalesce(1);

        res.saveAsTextFile(output);
    }

    public void setValue(String input, String output) {
        SparkSession spark = initSC();
        JavaSparkContext jsc = initContext();
        // Load and parse data
        JavaRDD<String> data = jsc.textFile(input);
        JavaRDD<String> res = data.map(s -> {
            if (s.charAt(0) == 'T')
                return s.substring(9, s.length())+",Twitter";
            else
                return s.substring(8, s.length())+",Reddit";
        }).coalesce(1);

        res.saveAsTextFile(output);
    }
    public static void main(String[] args) {
        String input = args[0], o = args[1];
        label l = new label();
        l.setValue(input, o);
    }
}
