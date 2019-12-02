
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;

public class RedditParser {
    public static ArrayList<String> parse(String file) throws IOException {
        FileWriter f1 = new FileWriter("reddit.txt", true);
        PrintWriter out1 = new PrintWriter(f1);

        ArrayList<String> l = new ArrayList<String>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        try {
            String line = bufferedReader.readLine();
            JSONArray comments = new JSONArray(line);
            for (Object object: comments) {
                JSONObject comment = (JSONObject) object;
                String text = comment.getString("body");
                l.add(text);
                while(text.indexOf("\n") > 0){
                    text = text.substring(0, text.indexOf("\n")) + text.substring(text.indexOf("\n") + 1);
                }
                while(text.indexOf("\r") > 0){
                    text = text.substring(0, text.indexOf("\r")) + text.substring(text.indexOf("\r") + 1);
                }
                out1.println("reddit;[" + text + "]");

                //System.out.print(text);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        out1.close();
        return l;
    }
    public static void parseFolder(String path) throws FileNotFoundException, IOException {
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (null == files || files.length == 0) {
                System.out.println("Empty folder!");
                return;
            } else {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        String folderName = file2.getName();
                        String fileName = file2.getAbsolutePath() + "/COMMENTS_"+folderName + ".json";
                        parse(fileName);
                    }
                }
            }
        } else {
            System.out.println("Folder does not exists!");
        }
    }
    public static void main(String args[]) throws FileNotFoundException, IOException {
        parseFolder("/Users/yfkang/Desktop/frozen");
    }
}
