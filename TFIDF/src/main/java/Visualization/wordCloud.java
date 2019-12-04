package Visualization;

//import com.kennycason.kumo.*;
//import com.kennycason.kumo.bg.CircleBackground;
//import com.kennycason.kumo.bg.PixelBoundryBackground;
//import com.kennycason.kumo.font.scale.LinearFontScalar;
//import com.kennycason.kumo.font.scale.SqrtFontScalar;
//import com.kennycason.kumo.nlp.FrequencyAnalyzer;
//import com.kennycason.kumo.palette.ColorPalette;
import main.all;
import main.single;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;

import javax.xml.crypto.Data;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class wordCloud {
    public static void main (String[] args) throws IOException {
        String input = args[0], output = args[1], tw = args[2], output1 = args[3];
        SparkSession spark = single.initSpark();
        single s = new single();
        all a = new all();
        // similarDataset(getValue(input, tw));
        String[] dir = s.findDir(input);

        Dataset<Row> df = spark.read()
                .json(input+"/"+"T"+"/part-00000");
        df.toJSON().toJavaRDD().repartition(1).saveAsTextFile(output+"/"+"T");
        Dataset<Row> t = df.groupBy(functions.col("word")).count();
        Dataset<Row> df1;
        int i = 0;
        for (String d : dir) {
            if (d.equals("movie") || d.equals("T")) continue;
            df1 = spark.read()
                    .json(input+"/"+d+"/part-00000")
                    .groupBy(functions.col("word")).count();
            df1.toJSON().toJavaRDD().repartition(1).saveAsTextFile(output+"/"+d);
        }
    }
//    public void bi() throws IOException {
//        final FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
//        frequencyAnalyzer.setWordFrequenciesToReturn(750);
//        frequencyAnalyzer.setMinWordLength(4);
//        // frequencyAnalyzer.setStopWords(loadStopWords());
//
//        final List<WordFrequency> wordFrequencies = frequencyAnalyzer.load("text/new_york_positive.txt");
//        final List<WordFrequency> wordFrequencies2 = frequencyAnalyzer.load("text/new_york_negative.txt");
//        final Dimension dimension = new Dimension(600, 600);
//        final PolarWordCloud wordCloud = new PolarWordCloud(dimension, CollisionMode.PIXEL_PERFECT, PolarBlendMode.BLUR);
//        wordCloud.setPadding(2);
//        wordCloud.setBackground(new CircleBackground(300));
//        wordCloud.setFontScalar(new SqrtFontScalar(10, 40));
//        wordCloud.build(wordFrequencies, wordFrequencies2);
//        wordCloud.writeToFile("~/output/polar_newyork_circle_blur_sqrt_font.png");
//    }
//    public void mon() throws IOException {
//        final FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
//        frequencyAnalyzer.setWordFrequenciesToReturn(300);
//        frequencyAnalyzer.setMinWordLength(4);
//
//        final List<WordFrequency> wordFrequencies = frequencyAnalyzer.load("text/datarank.txt");
//        final Dimension dimension = new Dimension(500, 312);
//        final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
//        wordCloud.setPadding(2);
//        wordCloud.setBackground(new PixelBoundryBackground("backgrounds/whale_small.png"));
//        wordCloud.setColorPalette(new ColorPalette(new Color(0x4055F1), new Color(0x408DF1), new Color(0x40AAF1), new Color(0x40C5F1), new Color(0x40D3F1), new Color(0xFFFFFF)));
//        wordCloud.setFontScalar(new LinearFontScalar(10, 40));
//        wordCloud.build(wordFrequencies);
//        wordCloud.writeToFile("~/output/whale_wordcloud_small.png");
//    }
}
