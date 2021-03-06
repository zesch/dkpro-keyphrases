package org.dkpro.keyphrases.example.pipeline;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import org.dkpro.keyphrases.example.core.filter.NGramCharFilter;
import org.dkpro.keyphrases.example.core.filter.NGramLengthFilter;
import org.dkpro.keyphrases.example.core.frequency.tfidf.TfIdfWriter;
import org.dkpro.keyphrases.example.core.frequency.tfidf.model.DfModel;
import org.dkpro.keyphrases.example.core.frequency.tfidf.util.TfidfUtils;
import org.dkpro.keyphrases.example.io.TestXmlReader;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.NGram;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.ngrams.NGramAnnotator;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;


public class TFIdfWriterExample {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		//String input_file = "";
		runPipeline(
                createReaderDescription(TestXmlReader.class,
                        TestXmlReader.PARAM_SOURCE_LOCATION, args[0],
                        TestXmlReader.PARAM_PATTERNS, args[1],
                        TestXmlReader.PARAM_XPATH, args[2],
                        TestXmlReader.PARAM_LANGUAGE, "de",
                        TestXmlReader.PARAM_LOG_FREQ, 1000),
		        createEngineDescription(BreakIteratorSegmenter.class),
		        createEngineDescription(NGramAnnotator.class,
		        		NGramAnnotator.PARAM_N, 2),
                createEngineDescription(NGramLengthFilter.class,
                        NGramLengthFilter.PARAM_MIN_LENGTH, 3,
                        NGramLengthFilter.PARAM_MAX_LENGTH, 100),
                createEngineDescription(NGramCharFilter.class,
                        NGramCharFilter.PARAM_CHAR_SET, "[A-ZÄÖÜa-zäöüß ]+"),
		        createEngineDescription(
		        		TfIdfWriter.class,
                        TfIdfWriter.PARAM_TARGET_LOCATION, args[3],
                        TfIdfWriter.PARAM_FEATURE_PATH, NGram.class.getName(),
                        TfIdfWriter.PARAM_LOWERCASE, false,
                        TfIdfWriter.PARAM_MIN_FREQ, 2)
		        		
		        
		    );

        DfModel dfModel = TfidfUtils.getDfModel(args[3]);

        System.out.println(dfModel.getDf("John Lennon"));
        System.out.println(dfModel.getDf("Angela Merkel"));
        System.out.println(dfModel.getDf("Angela"));
        System.out.println(dfModel.getDf("Gauck"));
        System.out.println(dfModel.getDf("zu"));
        System.out.println(dfModel.getLowercase());
        System.out.println(dfModel.getDocumentCount());
	}

}
