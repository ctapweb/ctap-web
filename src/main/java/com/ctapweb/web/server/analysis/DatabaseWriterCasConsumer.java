/**
 * 
 */
package com.ctapweb.web.server.analysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;

import com.ctapweb.feature.type.ComplexityFeatureBase;
import com.ctapweb.web.server.DBConnectionManager;
import com.ctapweb.web.server.analysis.type.CorpusTextInfo;
import com.ctapweb.web.shared.AnalysisEngine;


/**
 * Writes the analysis results to DB.
 * Each document is represented as a CAS in the framework. Up to this point, the CAS
 * has been populated with the text content and feature values. This consumer retrieves 
 * the data from the the CAS and writes them into the DB.
 * @author xiaobin
 *
 */
public class DatabaseWriterCasConsumer extends CasConsumer_ImplBase {

	//the analysis id from the 'analysis' table of the database
	public static final String PARAM_ANALYSISID = "analysisID";

	private long analysisID;

	Connection dbConnection = DBConnectionManager.getDbConnection();
	Logger logger = LogManager.getLogger();

	@Override
	public void initialize() throws ResourceInitializationException {
		super.initialize();
		analysisID = (int) getConfigParameterValue(PARAM_ANALYSISID);
	}

	/* (non-Javadoc)
	 * @see org.apache.uima.collection.base_cpm.CasObjectProcessor#processCas(org.apache.uima.cas.CAS)
	 */
	@Override
	public void processCas(CAS aCAS) throws ResourceProcessException {
		try {
			JCas jcas = aCAS.getJCas();

			//get the text id from the document info type, which was added to the CAS by the 
			// collection reader.
			long textID = 0;
			Iterator<CorpusTextInfo> corpusTextInfoIterator = jcas.getAllIndexedFS(CorpusTextInfo.class);
			if(corpusTextInfoIterator.hasNext()) {
				CorpusTextInfo corpusTextInfo = corpusTextInfoIterator.next();
				textID = corpusTextInfo.getId();
			}

			//NEW-IMPLEMENTATION
			//gets features the analysis includes 
			List<AnalysisEngine> analysisFeatures = AnalysisUtils.getAnalysisFeatures(analysisID);
			List<Long> analysisFeatureIDs = new ArrayList<>();
			for(AnalysisEngine featureAE: analysisFeatures) {
				analysisFeatureIDs.add(featureAE.getId());
			}

			//iterate through all ComplexityFeature types in the CAS, get their ae id and values
			Iterator<ComplexityFeatureBase> it = jcas.getAllIndexedFS(ComplexityFeatureBase.class);
			while(it.hasNext()) {
				ComplexityFeatureBase cf = it.next();
				long featureID = cf.getId();
				double featureValue = cf.getValue();
				
				//only write results that the feature set specifies
				if(!analysisFeatureIDs.contains(featureID)) {
					continue;
				}

				//writes results to database
				String insertStr = ""
						+ "INSERT INTO result (analysis_id, text_id, feature_id, value) "
						+ "VALUES (?, ?, ?, ?)";
				PreparedStatement ps = dbConnection.prepareStatement(insertStr);
				ps.setLong(1, analysisID);
				ps.setLong(2, textID);
				ps.setLong(3, featureID);
				ps.setDouble(4, featureValue);

				ps.executeUpdate();
			}
		} catch (CASException | SQLException e) {
			throw logger.throwing(new ResourceProcessException(e));
		}



		//OLD-IMPLEMENTATION
		//get the max AE id for each complexity feature, which is the AE 
		// that calculates the complexity feature value.
		// only results of these AEs are recorded in the database
		//		String queryStr=""
		//				+ "SELECT cf_ae.cf_id, MAX(cf_ae.ae_id) AS ae_id "
		//				+ "FROM analysis, fs_cf, cf_ae "
		//				+ "WHERE analysis.featureset_id=fs_cf.fs_id "
		//				+ "     AND fs_cf.cf_id=cf_ae.cf_id "
		//				+ "     AND analysis.id=? "
		//				+ "GROUP BY cf_ae.cf_id"
		//				+ "";
		//		PreparedStatement ps;
		//		try {
		//			ps = dbConnection.prepareStatement(queryStr);
		//			ps.setLong(1, analysisID);
		//			ResultSet rs = ps.executeQuery();
		//			while(rs.next()) {
		//				long cfID = rs.getLong("cf_id"); //the complexity feature id
		//				long aeID = rs.getLong("ae_id"); //the ae that calculates the CF
		//
		//				//iterate through all ComplexityFeature types in the CAS, get their ae id and values
		//				Iterator it = jcas.getAllIndexedFS(ComplexityFeatureBase.class);
		//				while(it.hasNext()) {
		//					ComplexityFeatureBase cf = (ComplexityFeatureBase) it.next();
		//					long casAEID = cf.getId();
		//					double featureValue = cf.getValue();
		//
		//					if(casAEID == aeID) {
		//						//write them to the database
		//						String insertStr = ""
		//								+ "INSERT INTO result (analysis_id, text_id, feature_id, value) "
		//								+ "VALUES (?, ?, ?, ?)";
		//						ps = dbConnection.prepareStatement(insertStr);
		//						ps.setLong(1, analysisID);
		//						ps.setLong(2, textID);
		//						ps.setLong(3, cfID);
		//						ps.setDouble(4, featureValue);
		//
		//						ps.executeUpdate();
		//					}
		//				}
		//			}
		//		}catch (SQLException e) {
		//			e.printStackTrace();
		//		}
	}
}

