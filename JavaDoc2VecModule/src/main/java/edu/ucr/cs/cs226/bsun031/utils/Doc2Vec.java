package edu.ucr.cs.cs226.bsun031.utils;


import java.io.*;
import java.util.*;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.sentenceiterator.LineSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;


public class Doc2Vec {
    //private static Logger logger = LoggerFactory.getLogger(Doc2Vec.class);
    public boolean turnDoc2Vec(String inputPath, String outputPath, ArrayList<String> list) throws IOException {
        File inputTxt = new File(inputPath);
        if(!inputTxt.exists())
            return false;
        //logger.info("Loading sentence data: "+inputTxt.getName());
        System.out.println("Loading sentence data: "+inputTxt.getName());
        SentenceIterator iter = new LineSentenceIterator(inputTxt);
        //Tokenizing
        TokenizerFactory token = new DefaultTokenizerFactory();
        //remove special symbols
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
                .minWordFrequency(1)
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
        //logger.info("Similiar Sentence:");
        //Collection<String> lst = vec.wordsNearest("doc1", 10);
        //System.out.println(lst);
        System.out.println("Writing vector to document");
        //writeDocVectors(vec,outputPath);
        DataFileWriter writer = new DataFileWriter();
        writer.writeResultVectors(vec,outputPath,tagList);
        //logger.info("Writing result to "+outputPath+" successful!");
        System.out.println("Writing result to "+outputPath+" successful!");
        //logger.info("Getting Vector for word:");
        //double[] docVector = vec.getWordVector("frozen");
        //System.out.println(Arrays.toString(docVector));
        return true;

    }
}
