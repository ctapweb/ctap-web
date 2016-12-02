/**
 * 
 */
package com.ctapweb.web.server.admin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.metadata.AnalysisEngineMetaData;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;

import com.ctapweb.web.server.DBConnectionManager;
import com.ctapweb.web.server.ServerProperties;
import com.ctapweb.web.server.logging.LogMarker;
import com.ctapweb.web.shared.AnalysisEngine;
import com.ctapweb.web.shared.exception.AEDependencyException;
import com.ctapweb.web.shared.exception.UIMAException;

/**
 * Imports analysis engines into the database.
 * @author xiaobin
 *
 */
public class ImportAE {
	Logger logger = LogManager.getLogger();
	private Connection dbConnection  = DBConnectionManager.getDbConnection();

	private static final String [] DESCRIPTOR_SUFFIXES = 
			new String [] {"Annotator.xml", "Feature.xml"};

	//automatically import all the descriptor files from the ctap-feature project. 
	//requires the class path to be set to point to ctap-feature's resources folder as well.
	public void importFeatureUIMADescriptors() 
			throws IOException, UIMAException, SQLException, AEDependencyException {
		logger.info(LogMarker.CTAP_SERVER_MARKER, 
				"Reading analysis engine descriptors from the feature_uima project...");
		//reads descriptors from descriptor/annotator and descriptor/featureAE and descriptor/type_system
		//look for files from classpath
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		File annotatorFolder = FileUtils.getFile(
				classLoader.getResource(ServerProperties.ANNOTATOR_FOLDER).getFile());
		File featureAEFolder = FileUtils.getFile(
				classLoader.getResource(ServerProperties.FEATUREAE_FOLDER).getFile());
		
		logger.info(LogMarker.CTAP_SERVER_MARKER, 
				"Importing annotator AE descriptors from annotator folder...");
		importAEDescriptorFolder(annotatorFolder, AnalysisEngine.AEType.ANNOTATOR);

		logger.info(LogMarker.CTAP_SERVER_MARKER, 
				"Importing AE descriptors from featureAE folder...");
		importAEDescriptorFolder(featureAEFolder, AnalysisEngine.AEType.FEATURE_EXTRACTOR);

		//for feature extractors, update dependency table
		logger.info(LogMarker.CTAP_SERVER_MARKER, 
				"Updating feature AE dependencies...");
		updateAEDependency();

	}

	private void updateAEDependency() 
			throws SQLException, AEDependencyException {
		//get all feature extractor AEs
		String queryStr = "SELECT id, name, description FROM analysis_engine";
		PreparedStatement queryPS = dbConnection.prepareStatement(queryStr);
		ResultSet queryRS = queryPS.executeQuery();

		//for each feature extractor
		while(queryRS.next()) {
			long aeID = queryRS.getLong("id");
			String name = queryRS.getString("name");
			String description = queryRS.getString("description");

			logger.trace(LogMarker.CTAP_SERVER_MARKER, 
					"Updating dependencies for " + name + "...");
			//clear original dependencies
			clearDependency(aeID);

			//add new dependencies
			addDependency(aeID, description);
		}


	}

	private void addDependency(long aeID, String description) 
			throws SQLException, AEDependencyException {
		//get dependency from description
		List<Long> dependencyList = getDependencyListFromDescription(description);
		
		String insertStr = "INSERT INTO ae_dependency (ae_id, dep_ae_id) "
				+ "VALUES (?, ?)";
		PreparedStatement ps = dbConnection.prepareStatement(insertStr);
		for(Long dependentAEid : dependencyList) {
			ps.setLong(1, aeID);
			ps.setLong(2, dependentAEid);
			ps.executeUpdate();
		}

	}

	private List<Long> getDependencyListFromDescription(String description) 
			throws SQLException, AEDependencyException {
		List<Long> dependencyList = new ArrayList<>();

		//find the dependency lines, which ends with either 'Annotator.xml' or 'Feature.xml'
		for(String line: description.split("\\n")) {
			String trimmedLine = line.trim();
			if(trimmedLine.endsWith("Annotator.xml") || trimmedLine.endsWith("Feature.xml")) {
				//is dependency line, get dependency AE id
				Long dependentAEid = getAEidByDescriptorName(trimmedLine);

				if(dependencyList.contains(dependentAEid)) {
					//dependency already exists
					throw logger.throwing(
							new AEDependencyException("Duplicated dependency item " + trimmedLine));
				} else {
					dependencyList.add(dependentAEid);
				}
			} 
		}

		return dependencyList;
	}

	private long getAEidByDescriptorName(String descriptorName) 
			throws SQLException, AEDependencyException {
		long aeID = 0 ;
		String queryStr = "SELECT id FROM analysis_engine "
				+ "WHERE descriptor_file_name = ?";
		PreparedStatement ps = dbConnection.prepareStatement(queryStr);
		ps.setString(1, descriptorName);
		ResultSet rs = ps.executeQuery();
		if(rs.isBeforeFirst()) {
			rs.next();
			aeID = rs.getLong("id");
		} else {
			throw logger.throwing(new AEDependencyException("Dependency descriptor " 
					+ descriptorName + " can't be found in the analysis_engine table." ));
		}

		return aeID;
	}
	private void clearDependency(long aeID) throws SQLException {
		String deleteStr = "DELETE FROM ae_dependency "
				+ "WHERE ae_id=?";
		PreparedStatement ps = dbConnection.prepareStatement(deleteStr);
		ps.setLong(1, aeID);
		ps.executeUpdate();
	}

	private void importAEDescriptorFolder(File descriptorFolder, String aeType)
			throws IOException, UIMAException, SQLException {
		//import only files with certain suffixes, ignore sub folders
		Iterator<File> descriptorFileIter = FileUtils.iterateFiles(descriptorFolder, 
				new SuffixFileFilter(DESCRIPTOR_SUFFIXES), null);

		while(descriptorFileIter.hasNext()) {
			//for each file in folder, get AE metadata and populate DB
			File descriptorFile = descriptorFileIter.next();
			logger.info(LogMarker.CTAP_SERVER_MARKER, 
					"Found descriptor file: " + descriptorFile.getName() + ", saving info into DB...");

			//save AE details into DB
			populateDB(descriptorFile, aeType);
		}
	}


	public void importDescriptorFile(File descriptorFile, String aeType)
			throws IOException, UIMAException, SQLException, AEDependencyException {
			populateDB(descriptorFile, aeType);

			updateAEDependency();
	}


	private void populateDB(File descriptorFile, String aeType) 
			throws IOException, UIMAException, SQLException {
		AnalysisEngineDescription aeDescription = getAEDescription(descriptorFile);

		AnalysisEngineMetaData aeMetaData = aeDescription.getAnalysisEngineMetaData();
		String aeName = aeMetaData.getName();
		String version = aeMetaData.getVersion();
		String vendor = aeMetaData.getVendor();
		String description = aeMetaData.getDescription();
		String descriptorFileContent = FileUtils.readFileToString(descriptorFile);

		//check if AE already in database
		if(isAEExits(descriptorFile)) {
			logger.info(LogMarker.CTAP_SERVER_MARKER,
					"Descriptor file exists in DB, updating AE info...");
			//update
			String updateStr = "UPDATE analysis_engine "
					+ "SET name = ?, type = ?, version = ?, "
					+ "vendor = ?, description = ?,  "
					+ "descriptor_file_content = ?, create_timestamp = CURRENT_TIMESTAMP "
					+ "WHERE descriptor_file_name = ?";
			PreparedStatement ps = dbConnection.prepareStatement(updateStr);
			ps.setString(1, aeName); 
			ps.setString(2, aeType);
			ps.setString(3, version);
			ps.setString(4, vendor);
			ps.setString(5, description);
			ps.setString(6, descriptorFileContent);
			ps.setString(7, descriptorFile.getName());

			ps.executeUpdate();
		} else {
			logger.info(LogMarker.CTAP_SERVER_MARKER,
					"Descriptor file does not exists in DB, add AE info...");
			//insert new
			String insertStr = "INSERT INTO analysis_engine (name, type, version, vendor, description, "
					+ "descriptor_file_name, descriptor_file_content, create_timestamp) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
			PreparedStatement ps = dbConnection.prepareStatement(insertStr);
			ps.setString(1, aeName);
			ps.setString(2, aeType);
			ps.setString(3, version);
			ps.setString(4, vendor);
			ps.setString(5, description);
			ps.setString(6, descriptorFile.getName());
			ps.setString(7, descriptorFileContent);

			ps.executeUpdate();
		}
	}

	private AnalysisEngineDescription getAEDescription(File descriptorFile) 
			throws IOException, UIMAException {
		AnalysisEngineDescription aeDescription = null;
		try {
			XMLInputSource xmlInput = new XMLInputSource(descriptorFile);
			aeDescription = UIMAFramework.getXMLParser().parseAnalysisEngineDescription(xmlInput);
		} catch (InvalidXMLException e) {
			throw logger.throwing(new UIMAException(e.getMessage()));
		}

		return aeDescription;
	}

	private boolean isAEExits(File descriptorFile) throws SQLException {
		boolean isExist = false;
		String queryStr = "SELECT id FROM analysis_engine "
				+ "WHERE descriptor_file_name=?";
		PreparedStatement ps = dbConnection.prepareStatement(queryStr);
		ps.setString(1, descriptorFile.getName());
		ResultSet rs = ps.executeQuery();
		if(rs.isBeforeFirst()) {
			isExist = true;
		}

		return isExist;
	}

}
