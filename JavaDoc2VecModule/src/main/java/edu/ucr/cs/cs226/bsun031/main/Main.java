package edu.ucr.cs.cs226.bsun031.main;

import edu.ucr.cs.cs226.bsun031.utils.DataFileReader;
import edu.ucr.cs.cs226.bsun031.utils.DataFileWriter;
import edu.ucr.cs.cs226.bsun031.utils.Doc2Vec;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws Exception {
        String datasetPath = "/Users/ericsun/Downloads/CS-226-master2/data";
        String stopwordsFilePath = "/Users/ericsun/Downloads/CS-226-master2/stopwords.txt";
        String sentenceOutputPath = "4Doc2Vec.txt";
        String vectorOutputPath = "Vectors.txt";
        DataFileReader reader = new DataFileReader();
        DataFileWriter sentenceWriter = new DataFileWriter();
        ArrayList<String> list = reader.readFromTokenizedData(datasetPath, stopwordsFilePath);
        if(list.size() < 1)
        {
            System.exit(128);
        }
        sentenceWriter.writeTextData(list,sentenceOutputPath);
        Doc2Vec doc2Vec = new Doc2Vec();
        boolean status = doc2Vec.turnDoc2Vec(sentenceOutputPath,vectorOutputPath,list);
        if(status == true)
           System.out.println("Doc2Vec Successful!");
    }
}
