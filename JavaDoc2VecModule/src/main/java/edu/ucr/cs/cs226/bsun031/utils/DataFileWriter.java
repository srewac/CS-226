package edu.ucr.cs.cs226.bsun031.utils;

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
    public void writeTextData(ArrayList<String> inList, String outputPath) throws IOException {
        File writeFile = new File(outputPath);
        writeFile.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(writeFile));
        for(int i=0;i<inList.size();i++)
        {
            //the write line must have the same order in the arraylist, to make sure the tag is matched
            out.write(inList.get(i)+"\r\n");
        }
        out.flush();
        out.close();
    }
    public void writeResultVectors(ParagraphVectors vectors, String outputPath, ArrayList<String> tagList) throws IOException {
        VocabCache<VocabWord> vocabCache = vectors.getVocab();
        File writeFile = new File(outputPath);
        writeFile.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(writeFile));
        for(VocabWord word : vocabCache.vocabWords())
        {
            StringBuilder builder = new StringBuilder();
            INDArray vector = vectors.getWordVectorMatrix(word.getLabel());
            for(int j=0;j<vector.length();j++)
            {
                builder.append(tagList.get(j)).append(";");
                builder.append(vector.getDouble(j));
                if(j<vector.length()-1)
                {
                    builder.append(" ");
                }
            }

            out.write(builder.append("\n").toString());
        }
        out.flush();
        out.close();
    }
}
