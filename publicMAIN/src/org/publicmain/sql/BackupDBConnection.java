package org.publicmain.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JTextField;

import org.publicmain.common.Config;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSGCode;

import com.mysql.jdbc.PreparedStatement;

public class BackupDBConnection {

	private static BackupDBConnection me;
	
	private Statement 	stmt;
	private String 		backupServerUrl;
	private String 		backupRootUser;
	private String 		backupUser;
	private int 		dbVersion;
	private int 		backUpDBStatus;
	private long 		warteZeitInSec;
	
	// Verbindungsdaten
	private Connection 	rootCon;
	private Connection 	userCon;
	
	private BackupDBConnection() {
		
		this.backUpDBStatus		= 0;									// Status: 11 als user Verbunden, 13 use DB als backupPublicMain
		this.warteZeitInSec 	= 10;

		
	}
	
	public static BackupDBConnection getBackupDBConnection() {
		if (me == null) {
			me = new BackupDBConnection();
		}
		return me;
	}
	
	
	
	// push & pull f�r ResultSets
	
	private synchronized boolean connectToBackupDBServer(){
		try {
			rootCon = DriverManager.getConnection("jdbc:mysql://"+Config.getConfig().getBackupDBIP()+":"+ Config.getConfig().getBackupDBPort(), Config.getConfig().getBackupDBChoosenUsername(), Config.getConfig().getBackupDBChoosenUserPassWord());
			stmt = rootCon.createStatement();
			LogEngine.log(this, "DB-ServerVerbindung als " + Config.getConfig().getBackupDBChoosenUsername() + " hergestellt ", LogEngine.INFO);
			backUpDBStatus = 91;
			return true;
		} catch (SQLException e) {
			try {
				Thread.sleep(warteZeitInSec * 1000);
			} catch (InterruptedException e1) {
				LogEngine.log(this,"Fehler beim Warten: " + e1.getMessage(),LogEngine.ERROR);
			}
			backUpDBStatus = 0;
			LogEngine.log(this, "Error while connecting to BackupDB " + e.getMessage(), LogEngine.ERROR);
		}
		
		return false;
	}
	
	public ResultSet pull_settings(){
		
		if(backUpDBStatus >= 3 && Config.getConfig().getBackupDBChoosenUsername()!= null) {
			try {
				return stmt.executeQuery("SELECT * FROM v_searchInHistory WHERE fk_t_backupUser_username LIKE '" + Config.getConfig().getBackupDBChoosenUsername() + "'");
			} catch (SQLException e) {
				LogEngine.log(this, "Error while pulling settings from backupDB " + e.getMessage(), LogEngine.ERROR );
			}
		}
		return null;
	}
	public void push_msgs(ResultSet tmp_messages) {
		// TODO Auto-generated method stub
		
	}
	public void push_users(ResultSet tmp_users) {
		// TODO Auto-generated method stub
		
	}
	public void push_settings(ResultSet tmp_settings) {
		// TODO Auto-generated method stub
		
	}
	public boolean getStatus() {
		//returns true if Backupserver is connected and User in config is present
		//Displays a Dialog for Userdata if Connection is present but Userdata not
		// TODO Auto-generated method stub
		return false;
	}
}
