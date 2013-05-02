package org.publicmain.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.publicmain.common.Config;
import org.publicmain.common.LogEngine;


public class BackupDBConnection {

	private static BackupDBConnection me;
	
	private Statement 	stmt;
	private int 		backUpDBStatus;
	private long 		warteZeitInSec;
	
	private BackupDBConnection() {
		
		this.backUpDBStatus		= 0;									// Status: 11 als user Verbunden, 13 use DB als backupPublicMain
		this.warteZeitInSec 	= 10;
		
		connectToBackupDBServer();
		
	}
	// Verbindungsdaten
	private Connection 	con;
	
	public static BackupDBConnection getBackupDBConnection() {
		if (me == null) {
			me = new BackupDBConnection();
		}
		return me;
	}
	
	private long getMyID() {
		String username = Config.getConfig().getBackupDBChoosenUsername();
		String password = Config.getConfig().getBackupDBChoosenUserPassWord();
		long tmpID=-1;
		synchronized (stmt) {
			try {
				ResultSet myid = stmt.executeQuery("Select backupUserID from t_backupUser where username like " + username + " and password like " + password);
				if (myid.first()) tmpID=myid.getLong(1);
			} catch (SQLException e) {
				LogEngine.log(this, e);
				return -1;
			}
			return tmpID;
		}
	
	}

	
	
	
	
	// push & pull für ResultSets
	
	private synchronized boolean connectToBackupDBServer(){
		try {
			System.out.println(Config.getConfig().getBackupDBUser());
			System.out.println(Config.getConfig().getBackupDBPw());
			con = DriverManager.getConnection("jdbc:mysql://"+Config.getConfig().getBackupDBIP()+":"+ Config.getConfig().getBackupDBPort(), Config.getConfig().getBackupDBUser(), Config.getConfig().getBackupDBPw());
			stmt = con.createStatement();
			LogEngine.log(this, "DB-ServerVerbindung als " + Config.getConfig().getBackupDBUser() + " hergestellt ", LogEngine.INFO);
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
	
	
	
	public synchronized boolean push_msgs(ResultSet tmp_messages) {
		long myID = getMyID();
		if(myID !=-1){
			try {
				PreparedStatement prp = con.prepareStatement("Insert ignore into t_messages(t_backupUser_backupUserID,msgID,timestmp,fk_t_users_userID_sender, displayName, groupName, fk_t_users_userID_empfaenger, txt,  fk_t_msgType_ID) values(?,?,?,?,?,?,?,?,?)");
				while (tmp_messages.next()){
					prp.setLong(1, myID);
					prp.setInt(2,tmp_messages.getInt(1));
					prp.setLong(3, tmp_messages.getLong(2));
					prp.setLong(4,tmp_messages.getLong(3));
					prp.setString(5, tmp_messages.getString(4));
					prp.setString(6, tmp_messages.getString(7));
					prp.setLong(7, tmp_messages.getLong(6));
					prp.setString(8,tmp_messages.getString(5));
					prp.setInt(9,tmp_messages.getInt(8));
					prp.addBatch();
				}
				prp.executeBatch();
				prp.close();
				return true;

			} catch (SQLException e) {
				LogEngine.log(this, e);
				return false;

			}
			
		}
		else return false;
	}
	
	
	
	
	public synchronized boolean push_users(ResultSet tmp_users) {
		long myID = getMyID();
		if(myID !=-1){
			try {
				PreparedStatement prp = con.prepareStatement("Insert ignore into t_users(userID, displayName, userName) values(?,?,?)");
				while (tmp_users.next()){
					prp.setLong(1, tmp_users.getLong(1));
					prp.setString(2, tmp_users.getString(2));
					prp.setString(3, tmp_users.getString(3));
					prp.addBatch();
				}
				prp.executeBatch();
				prp.close();

			} catch (SQLException e) {
				LogEngine.log(this, e);

			}

		}
		else return false;
		return true;	
	}

	public synchronized boolean push_settings(ResultSet tmp_settings) {
		long myID = getMyID();
		if(myID !=-1){
			try {
				PreparedStatement prp = con.prepareStatement("Insert ignore into t_settings(settingsKey, settingsValue, fk_t_backupUser_username) values(?,?,?)");
				while (tmp_settings.next()){
					prp.setString(1, tmp_settings.getString(1));
					prp.setLong(2, tmp_settings.getLong(2));
					prp.setString(3, tmp_settings.getString(3));
					prp.addBatch();
				}
				prp.executeBatch();
				prp.close();

			} catch (SQLException e) {
				LogEngine.log(this, e);

			}

		}
		else return false;
		return true;
	}

	
	
	
	
	public boolean getStatus() {
		//returns true if Backupserver is connected and User in config is present
		//Displays a Dialog for Userdata if Connection is present but Userdata not
		// TODO Auto-generated method stub
		if (backUpDBStatus >= 3){
			return true;
		}
		return false;
	}
	
	public synchronized boolean createUser (String usrName, String passwd){
		try {
			PreparedStatement prp = con.prepareStatement("Instert into t_backupUser(username,password) values(?,?)");
			prp.setString(1, usrName);
			prp.setString(2, passwd);
			prp.execute();
			prp.close();
			return true;
		} catch (SQLException e) {
			LogEngine.log(this, e);
		}
		return false;
		
	}
}
