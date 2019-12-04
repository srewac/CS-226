package edu.ucr.cs.cs226.baun031.main;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import edu.ucr.cs.cs226.baun031.utils.DataFileReader;
import edu.ucr.cs.cs226.baun031.utils.DataFileWriter;
import edu.ucr.cs.cs226.baun031.utils.Doc2Vec;
import edu.ucr.cs.cs226.baun031.utils.RedditParser;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length != 2)
            System.exit(128);
        //String datasetPath = "/Users/ericsun/Downloads/CS-226-master2/data";
        String datasetPath = args[0];
        //String stopwordsFilePath = "/Users/ericsun/Downloads/CS-226-master2/stopwords.txt";
        String stopwordsFilePath = args[1];
        //String sentenceOutputPath = "/home/ericsun/IdeaProjects/cs226-Doc2VecModuleV2.0/output/4Doc2Vec.txt";
        String trainDataPath = "/home/ericsun/IdeaProjects/cs226-Doc2VecModuleV2.0/output/trainData.txt";
        String testDataPath = "/home/ericsun/IdeaProjects/cs226-Doc2VecModuleV2.0/output/testData.txt";
        String vectorOutputPath = "/home/ericsun/IdeaProjects/cs226-Doc2VecModuleV2.0/output/Vectors.txt";
        String vectorOutputPath1 = "/home/ericsun/IdeaProjects/cs226-Doc2VecModuleV2.0/output/Vectorsr.txt";
        String datasetRedditPath ="/home/ericsun/IdeaProjects/cs226-Doc2VecModuleV2.0/input/frozen";
        String rsentenceOutputPath = "/home/ericsun/IdeaProjects/cs226-Doc2VecModuleV2.0/output/reddit.txt";
        DataFileReader reader = new DataFileReader();
        DataFileWriter sentenceWriter = new DataFileWriter();
        //generate reddit.txt
        RedditParser redditParser = new RedditParser();
        redditParser.parseFolder(datasetRedditPath,rsentenceOutputPath);
        System.out.println("Reading twitter data: ");
        ArrayList<String> list = reader.readFromTokenizedData(datasetPath, stopwordsFilePath);
        //ArrayList<String> listR = reader.readFromTokenizedData(rsentenceOutputPath, stopwordsFilePath);
        System.out.println("Reading Reddit data: ");
        ArrayList<String> listR = reader.readRedditData(rsentenceOutputPath, stopwordsFilePath);
        long startTime = System.
        double rate = 1.0;

        //TODO parse reddit.txt
        if(list.size() < 1 )
        {
            System.exit(128);
        }
        ArrayList<String> total = new ArrayList<String>();
        int size = 0;
        if(list.size()>listR.size())
        {
            size = listR.size();
        } else{
            size = list.size();
        }
        for(int in=0;in<size;in++)
        {
            total.add(list.get(in));
            total.add(listR.get(in));
        }
        int trainSize = (int) Math.floor(total.size() * rate);
        System.out.println("Writing train and test data: ");
        sentenceWriter.writeTextData(total,trainDataPath,testDataPath,trainSize);


        ArrayList<String> getTag = new ArrayList<String>();
        for(int i=0;i<trainSize;i++)
        {
            getTag.add(total.get(i));
        }
        Doc2Vec doc2Vec = new Doc2Vec();
        System.out.println("Training model: ");
        ParagraphVectors model = doc2Vec.turnDoc2Vec(trainDataPath,vectorOutputPath,getTag);
        System.out.println("Writing vectors: ");
        boolean status = doc2Vec.test(model,testDataPath,vectorOutputPath);
        //boolean status_reddit = true;
        System.out.println("Doc2Vec Successful!");
    }
}
