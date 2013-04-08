package org.publicmain.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JTextField;

import org.publicmain.common.LogEngine;

import com.mysql.jdbc.PreparedStatement;

public class BackupDBConnection {

	private static BackupDBConnection me;
	
	private Statement 	stmt;
	private String 		backupServerUrl;
	private String 		dbName;
	private String 		backupRootUser;
	private String 		backupUser;
	private String 		rootPasswd;
	
//	//Tabellen der Loc DB
//	private String chatLogTbl;
//	private String msgTbl;
//	private String usrTbl;
//	private String nodeTbl;
//	private String groupTbl;
//	private String configTbl;
//	private String eventTypeTbl;
//	private String routingOverviewTbl;
//	private Calendar cal;
//	private SimpleDateFormat splDateFormt;
	
	
	// verbindungssachen
	private Connection 	rootCon;
	private Connection 	userCon;
	
	private BackupDBConnection() {
		
		this.backupRootUser 	= "backuproot";
		this.rootPasswd			= "0123456789";
		this.dbName 			= "db_backUpServer_publicMain";
	}
	public static BackupDBConnection getBackupDBConnection() {
		if (me == null) {
			me = new BackupDBConnection();
		}
		return me;
	}
	
	public void createNewUser(JTextField statusTextField, JTextField serverIPTextField, JTextField userNameTextField, JTextField passWordTextField){
		
		this.backupServerUrl	= "jdbc:mysql://" + serverIPTextField + ":3306/";
		try {
			this.rootCon 	= DriverManager.getConnection(backupServerUrl, backupRootUser, rootPasswd);
			this.stmt 		= rootCon.createStatement();
			stmt.addBatch("create database if not exists " + dbName);
			stmt.addBatch("use " + dbName);
			stmt.addBatch("CREATE USER" + userNameTextField.getText() + " IDENTIFIED BY PASSWORD " + passWordTextField.getText());
			
		} catch (SQLException e) {
			//TODO:
			statusTextField.setText(e.getMessage());
			LogEngine.log(this, "DB-Verbindung zu Backupserver fehlgeschlagen: " + e.getMessage(), LogEngine.ERROR);
		}
	}
}
