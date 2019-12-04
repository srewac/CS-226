package Visualization;

import com.github.abel533.echarts.axis.CategoryAxis;
import com.github.abel533.echarts.axis.ValueAxis;
import com.github.abel533.echarts.code.Magic;
import com.github.abel533.echarts.code.Tool;
import com.github.abel533.echarts.code.Trigger;
import com.github.abel533.echarts.feature.MagicType;
import com.github.abel533.echarts.json.GsonOption;
import com.github.abel533.echarts.series.Bar;
import com.github.abel533.echarts.series.Line;

import main.single;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.HashMap;
import java.util.Map;


public class Stat {
    public static void main(String[] args) {
        String m = args[0];
        single s = new single();
        SparkSession spark = single.initSpark();
        String[] dir = s.findDir(m);
        Dataset<Row> df = spark.read().json(m+"/"+dir[0]+"/part-00000");
        Dataset<Row> tmp;
        int flag = 0;
        for (String d: dir) {
            if (d.equals("movie")) continue;
            if (flag == 0) {
                flag += 1;
                continue;
            }
            tmp = spark.read().json(m+"/"+d+"/part-00000");
            df = df.union(tmp);
        }

    }
    public String line (boolean isH, int[][] datas, String[] cat) {
        String[] types = { "AVG", "MIN", "MAX" };
        // int[][] datas = { { 120, 132, 101, 134, 90, 230, 210 }, { 220, 182, 191, 234, 290, 330, 310 }, { 150, 232, 201, 154, 190, 330, 410 } };
        String title = "Subreddit Score";

        GsonOption option = new GsonOption();

        option.title().text(title).subtext("Statistics").x("left");// 大标题、小标题、位置

        // 提示工具
        option.tooltip().trigger(Trigger.axis);// 在轴上触发提示数据
        // 工具栏
        option.toolbox().show(true).feature(Tool.saveAsImage,Tool.magicType,Tool.dataZoom);// 显示保存为图片

        option.legend(types);// 图例
        CategoryAxis category = new CategoryAxis();// 轴分类
        category.data(cat);
        category.boundaryGap(false);// 起始和结束两端空白策略
        // 循环数据
        for (int i = 0; i < types.length; i++) {

                String type = types[i];
                Bar bar = new Bar();// 三条线，三个对象

                bar.name(type).stack("Capacity");
                for (int j = 0; j < datas[i].length; j++){
                    Map<String,Object> map = new HashMap<>();
                    map.put("value",datas[i][j]);
                    map.put("name",cat[i]);
                    bar.data(map);
                }

                option.series(bar);
        }

        if (isH) {// 横轴为类别、纵轴为值
             option.xAxis(category);// x轴
             option.yAxis(new ValueAxis());// y轴
        } else {// 横轴为值、纵轴为类别
             option.xAxis(new ValueAxis());// x轴
             option.yAxis(category);// y轴
        }
        return option.toString();
    }
}
