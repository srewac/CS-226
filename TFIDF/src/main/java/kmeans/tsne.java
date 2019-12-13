package kmeans;

import java.io.*;

import com.jujutsu.tsne.TSneConfiguration;
import com.jujutsu.tsne.barneshut.BHTSne;
import com.jujutsu.tsne.barneshut.BarnesHutTSne;
import com.jujutsu.tsne.barneshut.ParallelBHTsne;
import com.jujutsu.utils.MatrixOps;
import com.jujutsu.utils.MatrixUtils;
import com.jujutsu.utils.TSneUtils;

public class tsne{
    public static void main(String [] args) throws IOException {
        String input = args[0], output = args[3];
        tsne t = new tsne();
        t.testTSNE(input, output);

        // Plot Y or save Y to file and plot with some other tool such as for instance R
    }

    public void testTSNE(String input, String output) throws IOException {
        int initial_dims = 10;
        double perplexity = 20.0;
        double [][] X = MatrixUtils.simpleRead2DMatrix(new File(input), "   ");
        BarnesHutTSne tsne;
        boolean parallel = true;
        if(parallel) {
            tsne = new ParallelBHTsne();
        } else {
            tsne = new BHTSne();
        }
        TSneConfiguration config = TSneUtils.buildConfig(X, 3, initial_dims, perplexity, 1000);
        double [][] Y = tsne.tsne(config);

        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < Y[0].length; i++)//for each row
        {
            for(int j = 0; j < Y.length; j++)//for each column
            {
                builder.append(Y[i][j]+"");//append to the output string
                if(j < Y.length - 1)//if this is not the last row element
                    builder.append(",");//then add comma (if you don't like commas you can use spaces)
            }
            builder.append("\n");//append new line at the end of the row
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(output));
        writer.write(builder.toString());//save the string representation of the board
        writer.close();
    }
}