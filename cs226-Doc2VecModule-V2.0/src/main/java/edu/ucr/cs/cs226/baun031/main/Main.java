package edu.ucr.cs.cs226.baun031.main;

import edu.ucr.cs.cs226.baun031.utils.DataFileReader;
import edu.ucr.cs.cs226.baun031.utils.DataFileWriter;
import edu.ucr.cs.cs226.baun031.utils.Doc2Vec;
import edu.ucr.cs.cs226.baun031.utils.RedditParser;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length != 2)
            System.exit(128);
        //String datasetPath = "/Users/ericsun/Downloads/CS-226-master2/data";
        String datasetPath = args[0];
        //String stopwordsFilePath = "/Users/ericsun/Downloads/CS-226-master2/stopwords.txt";
        String stopwordsFilePath = args[1];
        String sentenceOutputPath = "/home/ericsun/IdeaProjects/cs226-Doc2VecModuleV2.0/output/4Doc2Vec.txt";
        String vectorOutputPath = "/home/ericsun/IdeaProjects/cs226-Doc2VecModuleV2.0/output/Vectors.txt";
        String vectorOutputPath1 = "/home/ericsun/IdeaProjects/cs226-Doc2VecModuleV2.0/output/Vectorsr.txt";
        String datasetRedditPath ="/home/ericsun/IdeaProjects/cs226-Doc2VecModuleV2.0/input/frozen";
        String rsentenceOutputPath = "/home/ericsun/IdeaProjects/cs226-Doc2VecModuleV2.0/output/reddit.txt";
        DataFileReader reader = new DataFileReader();
        DataFileWriter sentenceWriter = new DataFileWriter();
        ArrayList<String> list = reader.readFromTokenizedData(datasetPath, stopwordsFilePath);
        //generate reddit.txt
        RedditParser redditParser = new RedditParser();
        ArrayList<String> arrayList = redditParser.parseFolder(datasetRedditPath, rsentenceOutputPath);
        for(int i=0;i<arrayList.size();i++)
        {
            if(!arrayList.get(i).contains("\n"))
                System.out.println(arrayList.get(i));
        }
        //TODO parse reddit.txt
        //reader.readFromTokenizedData(rsentenceOutputPath,stopwordsFilePath);
        if(list.size() < 1 )
        {
            System.exit(128);
        }
        sentenceWriter.writeTextData(list,sentenceOutputPath);

        Doc2Vec doc2Vec = new Doc2Vec();
        boolean status_twitter = doc2Vec.turnDoc2Vec(sentenceOutputPath,vectorOutputPath,list);
        //boolean status_reddit = true;
        if(status_twitter)
            System.out.println("Doc2Vec Successful!");
    }
}
