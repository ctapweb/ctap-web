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
import com.ctapweb.web.shared.AnalysisEngine;

/**
 * Analysis engine utility class.
 * @author xiaobin
 *
 */
public class AEUtils {

	private static Connection dbConnection  = DBConnectionManager.getDbConnection();
	private static Logger logger = LogManager.getLogger();

	//gets a list of all AEs from the DB
	public static List<AnalysisEngine> getAEList(int offset, int limit) throws SQLException {
		ArrayList<AnalysisEngine> aeList = new ArrayList<>(limit);

		//get data from database;
		String queryStr = ""
				+ "SELECT id, name, type, version, vendor, description, descriptor_file_name, "
				+ "descriptor_file_content, create_timestamp " 
				+ "FROM analysis_engine "
				+ "ORDER BY id DESC "
				+ "LIMIT ? OFFSET ?";
		PreparedStatement ps;
		ps = dbConnection.prepareStatement(queryStr);
		ps.setInt(1, limit);
		ps.setInt(2, offset);

		ResultSet rs = ps.executeQuery();

		// get infomation of all AE
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
			ae.setAeDependency(getAEDependency(ae.getId()));
			aeList.add(ae);
		}

		return aeList;
	}

	public static List<AnalysisEngine> getAEDependency(long aeID) throws SQLException {
		List<AnalysisEngine> aeDependency = new ArrayList<>();

		String queryStr = "SELECT ae.id, ae.name, ae.type, ae.version, ae.vendor, ae.description, "
				+ "ae.descriptor_file_name, descriptor_file_content, create_timestamp " 
				+ "FROM ae_dependency AS dep, analysis_engine AS ae " 
				+ "WHERE dep.dep_ae_id = ae.id AND dep.ae_id=? "
				+ "ORDER BY dep.id";
		PreparedStatement ps = dbConnection.prepareStatement(queryStr);
		ps.setLong(1, aeID);
		ResultSet rs = ps.executeQuery();
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

			aeDependency.add(ae);
		}

		return aeDependency;
	}

	

}
