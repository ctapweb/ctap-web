package com.ctapweb.web.server.user;

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
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.ctapweb.web.server.DBConnectionManager;
import com.ctapweb.web.shared.CorpusText;
import com.ctapweb.web.shared.exception.DatabaseException;

/**
 * For importing large number of texts locally.
 * Usage: java ImportCorpusLocal corpusId corpusFolder
 * @author xiaobin
 *
 */
public class ImportCorpusLocal {


	private static int corpusId;
	private static File corpusFolder; 
	private static Connection dbConnection = 
			DBConnectionManager.getDbConnection();


	public static void main(String[] args) throws IOException, DatabaseException, SQLException {
		corpusId = Integer.parseInt(args[0]);
		corpusFolder = new File(args[1]); 

		//iterate the folder to be analyzed
		Iterator<File> it = FileUtils.iterateFiles(corpusFolder, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		while(it.hasNext()) {
			File file = it.next();
			String fileName = file.getName();
			System.out.println("Importing file " + fileName + "...");

			//get text from file:
			String content = FileUtils.readFileToString(file, "UTF8");

			CorpusText corpusText = new CorpusText();
			corpusText.setCorpusID(corpusId);
			corpusText.setTitle(fileName);
			corpusText.setContent(content);

			//write to database
			writeToDatabase(corpusText);

		}
		
		System.out.println("All texts in " + args[1] + " imported successfully.");


	}

	private static void writeToDatabase(CorpusText corpusText) throws DatabaseException, SQLException {
		String insertStr = ""
				+ "INSERT INTO text (corpus_id, title, content, create_timestamp) "
				+ "VALUES (?, ?, ?, CURRENT_TIMESTAMP) RETURNING id";

		PreparedStatement ps = dbConnection.prepareStatement(insertStr);
		ps.setLong(1, corpusText.getCorpusID());
		ps.setString(2, corpusText.getTitle());
		ps.setString(3, corpusText.getContent());
		ResultSet rs = ps.executeQuery();

	}
}
