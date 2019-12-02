package edu.ucr.cs.cs226.baun031.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class DataFileReader {
    public ArrayList readFromTokenizedData(String datasetPath, String stopwordsPath) throws Exception {
        //HashMap<String, ArrayList<String>> dataMap = new HashMap<String, ArrayList<String>>();
        ArrayList<String> list = new ArrayList<String>();
        File dataFile = new File(datasetPath);
        if(!dataFile.exists())
            return list;
        Scanner scanner = new Scanner(dataFile);
        //TODO read the file
        while(scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            if(line.split(";").length == 2)
            {
                String[] textWithTag = process(line, stopwordsPath);
                list.add(textWithTag[0]+";"+textWithTag[1]);
            }

        }
        scanner.close();
        return list;
    }
    //TODO String processor
    public String[] process(String line, String stopwordsFilePath) throws Exception {
        String[] result = new String[2];
        String replacedLine = line.replace("[","").replace("]","").replace("'","");
        String[] spiltTag = replacedLine.split(";");
        if(spiltTag.length != 2)
            return null;
        result[0] = spiltTag[0];
        String[] textArr = spiltTag[1].replace("\"","").split(",");
        String resultText;

        //TODO get stopwords from stopwords.txt
        File stopwordsFile = new File(stopwordsFilePath);
        if(!stopwordsFile.exists())
            return null;
        List<String> stopwordsArr = new ArrayList<String>();
        Scanner scannerStopwords = new Scanner(stopwordsFile);
        while(scannerStopwords.hasNextLine())
        {
            String currentLine = scannerStopwords.nextLine();
            if(!currentLine.contains("#"))
            {
                stopwordsArr.add(currentLine);
            }
        }

        scannerStopwords.close();
        //TODO remove @words, #words and stop words
        String resStr = "";
        for(String str : textArr)
        {
            str = str.replace(" ","");
            int status = 0;
            if(!str.contains("@") && !str.contains("#") && !str.contains("https://"))
            {
                status = 1;
            }
            //look up in the stop word dictionary
            for(String stopwords : stopwordsArr)
            {
                if(stopwords.contains(str.toLowerCase()))
                {
                    status = 0;
                }
            }
            if(status == 1)
            {
                resStr += str+" ";
            }
        }
        result[1] = resStr;
        return result;
    }
    //TODO write joinHashMap method for multiple file reading purpose
    public HashMap<String, ArrayList<String>> joinHashMap(HashMap<String, ArrayList<String>> hashMap1, HashMap<String, ArrayList<String>> hashMap2){

        return hashMap1;
    }


}
