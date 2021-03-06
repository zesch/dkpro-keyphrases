/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.keyphrases.example.core.frequency.tfidf;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.keyphrases.example.core.frequency.tfidf.model.DfStore;
import org.dkpro.keyphrases.example.core.frequency.tfidf.util.TermIterator;
import org.dkpro.keyphrases.example.core.frequency.tfidf.util.TfidfUtils;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.Parameters;

/**
 * This consumer builds a {@link DfModel}. It collects the df (document frequency) counts for the
 * processed collection. The counts are serialized as a {@link DfModel}-object.
 */
@ResourceMetaData(name = "TF/IDF Model Writer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@Parameters(
        exclude = { 
                TfIdfWriter.PARAM_TARGET_LOCATION  })
public class TfIdfWriter
    extends JCasAnnotator_ImplBase
{
    @Deprecated
    public static final String PARAM_OUTPUT_PATH = ComponentParameters.PARAM_TARGET_LOCATION;
    /**
     * Specifies the path and filename where the model file is written.
     */
    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private String outputPath;

    /**
     * If set to true, the whole text is handled in lower case.
     */
    public static final String PARAM_LOWERCASE = "lowercase";
    @ConfigurationParameter(name = PARAM_LOWERCASE, mandatory = true, defaultValue = "false")
    private boolean lowercase;

    /**
     * This annotator is type agnostic, so it is mandatory to specify the type of the working
     * annotation and how to obtain the string representation with the feature path.
     */
    public static final String PARAM_FEATURE_PATH = "featurePath";
    @ConfigurationParameter(name = PARAM_FEATURE_PATH, mandatory = true)
    private String featurePath;

    /**
     * Any terms which have lower frequency than minimalFrequency in the corpus are discarded
     * before the model is written.
     */
    public static final String PARAM_MIN_FREQ = "minimalFrequency";
    @ConfigurationParameter(name = PARAM_MIN_FREQ, mandatory = true, defaultValue = "1")
    private int minimalFrequency;

    private DfStore dfStore;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        dfStore = new DfStore(featurePath, lowercase);
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        dfStore.registerNewDocument();

        for (String term : TermIterator.create(jcas, featurePath, lowercase)) {
            dfStore.countTerm(term);
        }

        dfStore.closeCurrentDocument();
    }

    /**
     * When this method is called by the framework, the dfModel is serialized.
     */
    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        try {
            TfidfUtils.writeDfModel(dfStore, outputPath, minimalFrequency);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
