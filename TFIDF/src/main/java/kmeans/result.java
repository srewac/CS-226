package kmeans;
import java.io.*;

public class result {
    public static void main(String[] args) {
        String input = args[0], cluster = args[1], output = args[2], output1 = args[3];
        int c = Integer.parseInt(cluster), best_i = 0;
        double res, best = Double.POSITIVE_INFINITY;
        kmeans k = new kmeans();
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {

            String data = " This is new content";

            File file = new File(output);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            // true = append file
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);

            for (int i = 4; i < c+1; i++) {
                res = k.run_cost(input, i, 50);
                if (res < best) {
                    best = res;
                    best_i = i;
                }
                bw.write(String.valueOf(i)+","+res+"\n");

            }

            System.out.println("Done==================================");

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {

                if (bw != null)
                    bw.close();

                if (fw != null)
                    fw.close();

            } catch (IOException ex) {

                ex.printStackTrace();

            }
        }
//        String ColumnNamesList = "cluster number,iterate number,WSSSE";
//// No need give the headers Like: id, Name on builder.append
//        builder.append(ColumnNamesList + "\n");

        k.run_kmeans(input, 4, 50, output1);
        System.out.println("done!================================================");

    }
}

