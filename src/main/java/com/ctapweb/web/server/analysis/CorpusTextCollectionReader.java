/**
 * 
 */
package com.ctapweb.web.server.analysis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import com.ctapweb.web.server.DBConnectionManager;
import com.ctapweb.web.server.analysis.type.CorpusTextInfo;
import com.ctapweb.web.server.logging.LogMarker;
import com.ctapweb.web.shared.CorpusText;

/**
 * A collection reader that reads texts from database corpus table.
 * 
 * When initialized, the UIMA framework passes in the 'corpusID' as a parameter.
 * The reader query the database for all the texts in the corpus whose id equals 'corpusID'.
 * The framework then iterates through all the texts the reader obtained by calling the 'hasNext()' 
 * and 'getNext()' functions.
 * 
 * A collection reader is like any other AEs. It works on the CAS. As a result, this collection reader
 * outputs the CorpusTextInfo type, which has a feature of 'id' designating the id of the text in the 
 * 'text' table of the system database. This id will be used 
 *  
 * @author xiaobin
 *
 */
public class CorpusTextCollectionReader extends CollectionReader_ImplBase {
	//a connection to the database
	Connection dbConnection = DBConnectionManager.getDbConnection();
	Logger logger = LogManager.getLogger();

	//name of the parameter from the collection reader descriptor.
	public static final String PARAM_ANALYSISID = "analysisID";

	//the list of corpus texts
	private List<CorpusText> texts = new ArrayList<>();
	private int currentIndex;

	@Override
	public void initialize() throws ResourceInitializationException {
		logger.info(LogMarker.CTAP_SERVER_MARKER, "Initiating Corpus Text Collection Reader...");
		super.initialize();
		currentIndex = 0;

		//get the analysisID parameter
		// This needs to be set in the descriptor or when initiating the cpe.
		long analysisID = (int) getConfigParameterValue(PARAM_ANALYSISID);
		
		//get analysis texts from DB
		try {
			logger.info(LogMarker.CTAP_SERVER_MARKER, "Getting analysis texts from DB...");
			texts = AnalysisUtils.getAnalysisTexts(analysisID);
			logger.info(LogMarker.CTAP_SERVER_MARKER, "Obtained {} texts from DB.", texts.size());
		} catch (SQLException e) {
			throw logger.throwing(new ResourceInitializationException(e));
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.uima.collection.CollectionReader#getNext(org.apache.uima.cas.CAS)
	 */
	@Override
	public void getNext(CAS aCAS) throws IOException, CollectionException {
		//create a jcas
		JCas jcas;
		try {
			jcas = aCAS.getJCas();
		} catch (CASException e) {
			throw logger.throwing(new CollectionException(e));
		}

		//put text in the cas
		CorpusText currentText = texts.get(currentIndex++);
		jcas.setDocumentText(currentText.getContent());

		//store text info in the cas, the cas consumer will need this information
		CorpusTextInfo corpusTextInfo = new CorpusTextInfo(jcas);
		corpusTextInfo.setId(currentText.getId());
		corpusTextInfo.addToIndexes();
	}

	/* (non-Javadoc)
	 * @see org.apache.uima.collection.base_cpm.BaseCollectionReader#close()
	 */
	@Override
	public void close() throws IOException {

	}

	/* (non-Javadoc)
	 * @see org.apache.uima.collection.base_cpm.BaseCollectionReader#getProgress()
	 */
	@Override
	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(currentIndex, texts.size(), Progress.ENTITIES) };
	}

	/* (non-Javadoc)
	 * @see org.apache.uima.collection.base_cpm.BaseCollectionReader#hasNext()
	 */
	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return currentIndex < texts.size();
	}

}
