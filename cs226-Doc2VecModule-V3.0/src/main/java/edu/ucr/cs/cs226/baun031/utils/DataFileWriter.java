package edu.ucr.cs.cs226.baun031.utils;

import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class DataFileWriter {
    public void writeTextData(ArrayList<String> inList, String trainPath, String testPath,int trainSize) throws IOException {
        File trainFile = new File(trainPath);
        File testFile = new File(testPath);
        if(trainFile.exists())
            testFile.delete();
        if(testFile.exists())
            testFile.delete();
        trainFile.createNewFile();
        testFile.createNewFile();
        BufferedWriter outTrain = new BufferedWriter(new FileWriter(trainFile));

        for(int i=0;i<inList.size();i++)
        {
            //the write line must have the same order in the arraylist, to make sure the tag is matched
            //put >15 to remove tag;: |
            if(inList.get(i).length() > 15)
                outTrain.write(inList.get(i)+"\r\n");
        }
        outTrain.flush();
        outTrain.close();
        BufferedWriter outTest = new BufferedWriter(new FileWriter(testFile));
        for(int i= 0;i<inList.size();i++)
        {
            if(inList.get(i).length() > 15)
                outTest.write(inList.get(i)+"\r\n");
        }
        outTest.flush();
        outTest.close();
    }
    public void writeResultVectors(ParagraphVectors vectors, String outputPath, ArrayList<String> tagList) throws IOException {
        VocabCache<VocabWord> vocabCache = vectors.getVocab();
        File writeFile = new File(outputPath);
        writeFile.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(writeFile));
        int k=0;
        for(VocabWord word : vocabCache.vocabWords())
        {
            StringBuilder builder = new StringBuilder();
            INDArray vector = vectors.getWordVectorMatrix(word.getLabel());
            if(k<tagList.size())
                builder.append(tagList.get(k).replace(" ","")).append(",");
            for(int j=0;j<vector.length();j++)
            {
                builder.append(vector.getDouble(j));
                if(j<vector.length()-1)
                {
                    builder.append(",");
                }
            }
            k++;
            out.write(builder.append("\n").toString());
        }
        out.flush();
        out.close();
    }
}
