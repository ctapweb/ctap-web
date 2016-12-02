/**
 * 
 */
package com.ctapweb.web.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author xiaobin
 * Manages database connections.
 * 
 * Before being able to connect to the database, create the user and grant it the rights to use the database.
 *    create user 'mysqluser'@'localhost' identified by 'MySQLuser_pw123';
 *    create database complexity;
 *    grant all privileges on complexity.* to 'mysqluser'@'localhost';
 */
public class DBConnectionManager {

	private static final String dbHost = ServerProperties.DBHOST; 
	private static final String dbName = ServerProperties.DBNAME; 
	private static final String dbUser = ServerProperties.DBUSER; 
	private static final String dbPasswd = ServerProperties.DBPASSWD;

	private static Connection dbConnection= null;

	private DBConnectionManager() {
	}

	public static Connection getDbConnection() {
		if(dbConnection == null) {
			try {
				Class.forName("org.postgresql.Driver");
				String url = "jdbc:postgresql://" + dbHost + "/" + dbName;
				dbConnection =DriverManager.getConnection(url, dbUser, dbPasswd); 
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return dbConnection;
	}

}
