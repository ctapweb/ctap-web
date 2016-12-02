package com.ctapweb.web.server.analysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ctapweb.web.server.DBConnectionManager;
import com.ctapweb.web.server.user.FeatureSetUtils;
import com.ctapweb.web.shared.Analysis;
import com.ctapweb.web.shared.AnalysisEngine;
import com.ctapweb.web.shared.AnalysisStatus;
import com.ctapweb.web.shared.CorpusText;

/**
 * Analysis utility class.
 * @author xiaobin
 *
 */
public class AnalysisUtils {
	private static Logger logger = LogManager.getLogger();
	private static Connection dbConnection = DBConnectionManager.getDbConnection();

	/**
	 * Check if use is owner of an analysis.
	 * @param userID
	 * @param analysisID
	 * @return
	 * @throws SQLException
	 */
	public static boolean isUserAnalysisOwner(long userID, long analysisID) throws SQLException {
		boolean isUserOwner = false;

		//check if the logged in user is the same as the feature set owner
		String queryStr =  "SELECT id FROM analysis "
				+ "WHERE id=? AND owner_id=? ";
		PreparedStatement ps;
		ps = dbConnection.prepareStatement(queryStr);
		ps.setLong(1, analysisID);
		ps.setLong(2, userID);
		ResultSet rs = ps.executeQuery();
		if(rs.isBeforeFirst()) {
			isUserOwner = true;
		}

		return isUserOwner;
	}

	/**
	 * gets an analysis by ID. If it doesn't exist, returns null.
	 * @param analysisID
	 * @return
	 * @throws SQLException
	 */
	public static Analysis getAnalysisDetails(long analysisID) throws SQLException {
		Analysis analysis = new Analysis();

		//get data from database
		String queryStr = ""
				+ "SELECT analysis.id, analysis.owner_id, analysis.name, "
				+ "analysis.description, "
				+ "analysis.corpus_id, corpus.name AS corpus_name, "
				+ "analysis.tag_filter_logic, analysis.tag_filter_keyword, "
				+ "analysis.featureset_id, feature_set.name AS featureset_name, "
				+ "analysis.create_timestamp "
				+ "FROM analysis "
				+ "JOIN corpus ON (analysis.corpus_id=corpus.id) "
				+ "JOIN feature_set ON (analysis.featureset_id=feature_set.id) "
				+ "WHERE analysis.id=?";
		PreparedStatement ps;
		ps = dbConnection.prepareStatement(queryStr);
		ps.setLong(1, analysisID);

		ResultSet rs = ps.executeQuery();

		// get infomation of analysis 
		if(rs.next()) {
			analysis.setId(rs.getLong("id"));
			analysis.setOwner_id(rs.getLong("owner_id"));
			analysis.setName(rs.getString("name")); 
			analysis.setDescription(rs.getString("description")); 
			analysis.setCorpusID(rs.getLong("corpus_id"));
			analysis.setCorpusName(rs.getString("corpus_name"));
			analysis.setTagFilterLogic(rs.getString("tag_filter_logic"));
			analysis.setTagKeyword(rs.getString("tag_filter_keyword"));
			analysis.setCreateDate(rs.getDate("create_timestamp"));
			analysis.setFeatureSetID(rs.getLong("featureset_id"));
			analysis.setFeatureSetName(rs.getString("featureset_name"));
		}

		return analysis;
	}

	/**
	 * Gets the texts specified by the analysis.
	 * 
	 *  An analysis specifies a corpus to be analyzed. The analysis also specifies the text filtering conditions and keywords 
	 *  through its tag filter logic and tag filter keyword fields.
	 * @param analysisID
	 * @return
	 * @throws SQLException
	 */
	public static List<CorpusText> getAnalysisTexts(long analysisID) throws SQLException {
		List<CorpusText> texts = new ArrayList<>();
		Analysis analysis = getAnalysisDetails(analysisID);

		if(analysis == null) {
			return null;
		}

		long corpusID = analysis.getCorpusID();
		String tagFilterLogic = analysis.getTagFilterLogic();
		String tagFilterKeyword = analysis.getTagKeyword();
		String likeStr = getTagFilterString(analysis);

		String queryStr;
		PreparedStatement ps;
		if(tagFilterKeyword == null || tagFilterKeyword.isEmpty() 
				|| Analysis.TagFilterLogic.NOFILTER.equals(tagFilterLogic) || tagFilterLogic == null) {
			//no filtering
			queryStr = "SELECT id, corpus_id, title, content, create_timestamp "
					+ "FROM text "
					+ "WHERE corpus_id = ?";
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, corpusID);
		} else {
			queryStr = "SELECT text.id, text.corpus_id, text.title, text.content, text.create_timestamp "
					+ "FROM text, ta_te, tag "
					+ "WHERE tag.id=ta_te.ta_id "
					+ "     AND ta_te.te_id=text.id "
					+ "     AND text.corpus_id=? "
					+ "     AND tag.name ILIKE ?";
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, corpusID);
			ps.setString(2, likeStr);
		}

		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			CorpusText text = new CorpusText();
			text.setId(rs.getLong("id"));
			text.setCorpusID(rs.getLong("corpus_id"));
			text.setTitle(rs.getString("title"));
			text.setContent(rs.getString("content"));
			text.setCreateDate(rs.getDate("create_timestamp"));

			texts.add(text);
		}

		return texts;
	}

	/**Gets the tag filter string to be used in the LIKE clause of database query.
	 * 
	 * @param analysis
	 * @return
	 */
	public static String getTagFilterString(Analysis analysis) {
		String tagFilterStr = "%";
		String tagFilterLogic = analysis.getTagFilterLogic();
		String tagFilterKeyword = analysis.getTagKeyword();
		if(Analysis.TagFilterLogic.STARTSWITH.equals(tagFilterLogic)) {
			tagFilterStr = tagFilterKeyword + "%";
		} else if(Analysis.TagFilterLogic.CONTAINS.equals(tagFilterLogic)) {
			tagFilterStr = "%" + tagFilterKeyword + "%";
		} else if(Analysis.TagFilterLogic.ENDSWITH.equals(tagFilterLogic)) {
			tagFilterStr = "%" + tagFilterKeyword;
		} else if(Analysis.TagFilterLogic.EQUALS.equals(tagFilterLogic)) {
			tagFilterStr = tagFilterKeyword;
		}
		return tagFilterStr;
	}
	
	public static String getTagFilterString(long analysisID) throws SQLException {
		return getTagFilterString(getAnalysisDetails(analysisID));
	}

	public static void updateAnalysisStatus(AnalysisStatus analysisStatus) throws SQLException {
		long analysisID = analysisStatus.getAnalysisID();
		double progress = analysisStatus.getProgress();
		String status = analysisStatus.getStatus();

		logger.trace("Analysis Status info: analysisID({}), progress({}), status({}). ", 
				analysisID, progress, status);

		//check if status record already exists in DB
		String queryStr = ""
				+ "SELECT id "
				+ "FROM analysis_status "
				+ "WHERE analysis_id=?";
		PreparedStatement queryPS = dbConnection.prepareStatement(queryStr);
		queryPS.setLong(1, analysisID);
		ResultSet rs = queryPS.executeQuery();

		String updateStr;
		PreparedStatement updatePS;
		if(rs.isBeforeFirst()) {
			//status record exists
			updateStr = ""
					+ "UPDATE analysis_status "
					+ "SET progress=?, status=?, last_update=CURRENT_TIMESTAMP "
					+ "WHERE analysis_id=?";
			updatePS = dbConnection.prepareStatement(updateStr);
			updatePS.setDouble(1, progress);
			updatePS.setString(2, status);
			updatePS.setLong(3, analysisID);
		} else {
			updateStr = "INSERT INTO analysis_status (analysis_id, progress, status, last_update) "
					+ "VALUES (?, ?, ?, CURRENT_TIMESTAMP) ";
			updatePS = dbConnection.prepareStatement(updateStr);
			updatePS.setLong(1, analysisID);
			updatePS.setDouble(2, progress);
			updatePS.setString(3, status); 
		}

		updatePS.executeUpdate();

	}

	public static void updateAnalysisProgress(AnalysisStatus analysisStatus) throws SQLException {
		long analysisID = analysisStatus.getAnalysisID();
		double progress = analysisStatus.getProgress();

		//check if status record already exists in DB
		String queryStr = ""
				+ "SELECT id "
				+ "FROM analysis_status "
				+ "WHERE analysis_id=?";
		PreparedStatement queryPS = dbConnection.prepareStatement(queryStr);
		queryPS.setLong(1, analysisID);
		ResultSet rs = queryPS.executeQuery();

		String updateStr;
		PreparedStatement updatePS;
		if(rs.isBeforeFirst()) {
			//status record exists
			updateStr = ""
					+ "UPDATE analysis_status "
					+ "SET progress=?, last_update=CURRENT_TIMESTAMP "
					+ "WHERE analysis_id=?";
			updatePS = dbConnection.prepareStatement(updateStr);
			updatePS.setDouble(1, progress);
			updatePS.setLong(2, analysisID);
		} else {
			updateStr = "INSERT INTO analysis_status (analysis_id, progress, last_update) "
					+ "VALUES (?, ?, CURRENT_TIMESTAMP) ";
			updatePS = dbConnection.prepareStatement(updateStr);
			updatePS.setLong(1, analysisID);
			updatePS.setDouble(2, progress);
		}

		updatePS.executeUpdate();

	}

	public static List<AnalysisEngine> getAnalysisFeatures(long analysisID) throws SQLException {
		return FeatureSetUtils.getFeatureList(getAnalysisDetails(analysisID).getFeatureSetID());
	}

	//construct an AE list to be added as cas processors
	//Logic: each feature is obtained by running a series of AEs. Multiple features may use the same AEs.
	//As a result, we need to make sure that the same AE is added to the CPE only once. 
	//We also need to garantee the order in which the AEs are run for each feature. 
	//As a result, the AE list is constructed by inserting subsequent AE dependencies into the first AE dependency.
	//Then repeated AEs are deleted. 
	public static List<AnalysisEngine> getCleanAEs(long analysisID) throws SQLException {
		List<AnalysisEngine> cleanAEs = new ArrayList<>();

		//construct a list an AEs containing repetitions
		for(AnalysisEngine ae: getRawAEs(analysisID)) {
			List<AnalysisEngine> dependentAEs = ae.getAeDependency();

			if(cleanAEs.contains(dependentAEs)) {
				cleanAEs.addAll(cleanAEs.indexOf(dependentAEs), dependentAEs);
			} else {
				cleanAEs.addAll(0, dependentAEs);
			}
		}

		//clean up the AEs
		for(int i =0; i < cleanAEs.size(); i++) {
			for(int j=i + 1; j < cleanAEs.size(); j++) {
				if(cleanAEs.get(i).equals(cleanAEs.get(j))) {
					cleanAEs.remove(j);
					j = i;
				}
			}
		}

		return cleanAEs;
	}

	//gets a list of all AEs in a feature set from the DB
	private static List<AnalysisEngine> getRawAEs(long analysisID) throws SQLException {
		logger.entry(analysisID);
		ArrayList<AnalysisEngine> aeList = new ArrayList<>();

		//get data from database;
		String queryStr = "SELECT ae.id, ae.name, ae.type, ae.version,"
				+ "ae.vendor, ae.description, ae.descriptor_file_name, "
				+ "ae.descriptor_file_content, ae.create_timestamp "
				+ "FROM analysis, fs_cf, analysis_engine as ae "
				+ "WHERE analysis.featureset_id=fs_cf.fs_id "
				+ "     AND fs_cf.cf_id=ae.id "
				+ "     AND analysis.id = ? ";

		PreparedStatement ps;
		ps = dbConnection.prepareStatement(queryStr);
		ps.setLong(1, analysisID);

		ResultSet rs = ps.executeQuery();

		// get infomation of all AEs 
		while(rs.next()) {
			AnalysisEngine ae = new AnalysisEngine();
			ae.setId(rs.getLong("id"));
			ae.setName(rs.getString("name"));
			ae.setType(rs.getString("type"));
			ae.setVersion(rs.getString("version"));
			ae.setVendor(rs.getString("vendor"));
			ae.setDescription(rs.getString("description"));
			ae.setDescriptorFileName(rs.getString("descriptor_file_name"));
			ae.setDescriptorFileContent(rs.getString("descriptor_file_content"));
			ae.setCreateDate(rs.getDate("create_timestamp"));

			//get AE dependency
			List<AnalysisEngine> dependencyList = AEUtils.getAEDependency(ae.getId());
			dependencyList.add(ae); //adding the current AE, which is reuqired to get the analysis result
			ae.setAeDependency(dependencyList);

			aeList.add(ae);
		}

		return aeList;
	}

	public static void clearAnalysisResults(long analysisID) throws SQLException {
		//clear previous results for this analysis
		String deleteStr = ""
				+ "DELETE FROM result WHERE analysis_id=?";
		PreparedStatement ps = dbConnection.prepareStatement(deleteStr);
		ps.setLong(1, analysisID);
		ps.executeUpdate();
	}

	public static AnalysisStatus getAnalysisStatus(long analysisID) throws SQLException {
		AnalysisStatus analysisStatus = new AnalysisStatus();
		String queryStr = ""
				+ "SELECT analysis_id, progress, status, last_update "
				+ "FROM analysis_status "
				+ "WHERE analysis_id=?";

		PreparedStatement ps = dbConnection.prepareStatement(queryStr);
		ps.setLong(1, analysisID);
		ResultSet rs = ps.executeQuery();

		if(rs.next()) {
			analysisStatus.setAnalysisID(rs.getLong("analysis_id"));
			analysisStatus.setProgress(rs.getDouble("progress"));
			analysisStatus.setStatus(rs.getString("status"));
			analysisStatus.setLastUpdate(rs.getDate("last_update"));
		}

		return analysisStatus;
	}
}
