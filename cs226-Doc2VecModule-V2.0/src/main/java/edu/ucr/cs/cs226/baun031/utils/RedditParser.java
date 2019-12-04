package edu.ucr.cs.cs226.baun031.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;

public class RedditParser {
    public ArrayList parse(String file, String outputPath) throws IOException {
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
                if(text.length() > 2 && !text.equals("") && !text.equals(null))
                    out1.println("reddit;" + text + "");
                //System.out.print(text);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        out1.flush();
        out1.close();
        return l;
    }
    public ArrayList parseFolder(String path, String outputPath) throws FileNotFoundException, IOException {
        File file = new File(path);
        ArrayList<String> res = new ArrayList<String>();
        if (file.exists()) {
            File[] files = file.listFiles();
            if (null == files || files.length == 0) {
                System.out.println("Empty folder!");
                return res;
            } else {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        String folderName = file2.getName();
                        String fileName = file2.getAbsolutePath() + "/COMMENTS_"+folderName + ".json";
                        ArrayList<String> curr = parse(fileName, outputPath);
                        res.addAll(curr);
                    }
                }
            }
        } else {
            System.out.println("Folder does not exists!");
        }
        return res;
    }
}
