package edu.ucr.cs.cs226.baun031.utils;

import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.sentenceiterator.LineSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Doc2Vec {
    //private static Logger logger = LoggerFactory.getLogger(Doc2Vec.class);
    public ParagraphVectors turnDoc2Vec(String inputPath,  String outputPath, ArrayList<String> list) throws IOException {
        File inputTxt = new File(inputPath);
        if(!inputTxt.exists())
            return null;
        //logger.info("Loading sentence data: "+inputTxt.getName());
        System.out.println("Loading sentence data: "+inputTxt.getName());
        SentenceIterator iter = new LineSentenceIterator(inputTxt);
        //Tokenizeer
        TokenizerFactory token = new DefaultTokenizerFactory();
        //Remove special symbol
        token.setTokenPreProcessor(new CommonPreprocessor());
        AbstractCache<VocabWord> cache=new AbstractCache<VocabWord>();
        //add the tag
        ArrayList<String> tagList = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {
            String currStr = list.get(i);
            String tag = currStr.split(";")[0];
            tagList.add(tag);
        }
        LabelsSource source = new LabelsSource(tagList);
        System.out.println("Training Model ...");
        ParagraphVectors vec = new ParagraphVectors.Builder()
                .minWordFrequency(0)
                .iterations(5)
                .epochs(1)
                .layerSize(50)
                .learningRate(0.025)
                .labelsSource(source)
                .windowSize(5)
                .iterate(iter)
                .trainWordVectors(false)
                .vocabCache(cache)
                .tokenizerFactory(token)
                .sampling(0)
                .build();

        vec.fit();
        //logger.info("Similar Sentence:");
        //Collection<String> lst = vec.wordsNearest("doc1", 10);
        //System.out.println(lst);
        System.out.println("Writing vector to document");
        //writeDocVectors(vec,outputPath);
        //DataFileWriter writer = new DataFileWriter();
        //writer.writeResultVectors(vec,outputPath,tagList);
        //logger.info("Writing result to "+outputPath+" successful!");
        System.out.println("Writing result to "+outputPath+" successful!");
        //logger.info("Obtain Vector:");
        //double[] docVector = vec.getWordVector("not");
        //System.out.println(Arrays.toString(docVector));
        //INDArray idn = vec.inferVector("Hi there you reall right?");
        //System.out.println(idn.length());
        //INDArray idn2 = vec.inferVector("Panic! Discos version Unknown amazing.... ");
        //System.out.println(idn2.length());
        return vec;

    }
    public boolean test(ParagraphVectors vec, String testDataPath, String vecOutputPath) throws Exception {
        File dataFile = new File(testDataPath);
        if(!dataFile.exists())
            return false;
        Scanner scanner = new Scanner(dataFile);
        File writeFile = new File(vecOutputPath);
        if(writeFile.exists())
            writeFile.delete();
        writeFile.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(writeFile));
        //TODO read the file
        while(scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            String[] contents = line.split(";");
            if(contents.length == 2 && line.length()>15)
            {
                StringBuilder builder = new StringBuilder();
                //System.out.println("Content is:"+contents[1]);
                INDArray vector = vec.inferVector(contents[1].toString());
                //System.out.println(" and dimension is: "+vector.length());
                builder.append(contents[0].replace(" ","")).append(",");
                for(int j=0;j<vector.length();j++)
                {
                    builder.append(vector.getDouble(j));
                    if(j<vector.length()-1)
                    {
                        builder.append(",");
                    }
                }
                out.write(builder.append("\n").toString());
            }

        }
        out.flush();
        out.close();
        scanner.close();

        return true;
    }
}
