package com.ctapweb.web.server.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.ctapweb.web.server.DBConnectionManager;
import com.ctapweb.web.shared.Tag;

public class CorpusManagerServiceUtils {

	private static Connection dbConnection = DBConnectionManager.getDbConnection();

	
	/**
	 * Gets all the tags assigned to the text.
	 * @param textID
	 * @return
	 * @throws SQLException
	 */
	public static Set<Tag> getTextTags(long textID) throws SQLException {
		Set<Tag> tags = new HashSet<>();

		//get tags for the text
		String queryStr = "SELECT tag.id, tag.name "
				+ "FROM tag, ta_te "
				+ "WHERE ta_te.ta_id = tag.id"
				+ "     AND ta_te.te_id =?";
		PreparedStatement ps = dbConnection.prepareStatement(queryStr);
		ps.setLong(1, textID);
		ResultSet tagRS = ps.executeQuery();
		while(tagRS.next()) {
			Tag tag = new Tag();
			tag.setId(tagRS.getLong("id"));
			tag.setName(tagRS.getString("name"));
			tags.add(tag);
		}

		return tags;
	}
	
}
