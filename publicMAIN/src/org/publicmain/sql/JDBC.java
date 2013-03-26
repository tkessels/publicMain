package org.publicmain.sql;

import java.beans.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * @author ATRM
 * 
 */

public class JDBC {

	/**
	 * @param args
	 */

	public JDBC(){

		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		// Verbindung zum lokalen Datenbankserver herstellen
		String url = "jdbc:mysql://localhost:3306";

		// Datenbankname
		String dbName = "publicMain";
		
		// Login für Datenbank hinterlegen
		String user = "root";
		String passwd = "";

		// String für SQL-Statement anlegen
		String sqlStmt = null;
		
		try {
			con = DriverManager.getConnection(url + dbName, user, passwd);
			stmt = con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void insertIntoUser(long userID, String name){
		sqlStmt = " ";
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
