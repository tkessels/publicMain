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
	private String 		dbName;
	private String 		backupRootUser;
	private String 		backupUser;
	private String 		rootPasswd;
	private int 		dbVersion;
	
	// Verbindungsdaten
	private Connection 	rootCon;
	private Connection 	userCon;
	
	private BackupDBConnection() {
		
		this.backupRootUser 	= "root";
		this.rootPasswd			= "";
		this.dbName 			= "db_backUpServer_publicMain";
	}
	public static BackupDBConnection getBackupDBConnection() {
		if (me == null) {
			me = new BackupDBConnection();
		}
		return me;
	}
	
	
	
	// push & pull für ResultSets
	
	
	
	
	
	public boolean create() {
		String read=null;
		try (BufferedReader in = new BufferedReader(new FileReader(new File(getClass().getResource("create_backup_db.sql").toURI())))){
			while((read = in.readLine()) != null) {
				while (!read.endsWith(";") && !read.endsWith("--")){
					read = read + in.readLine();
				}
				stmt.execute(read);
			}
			for (MSGCode c : MSGCode.values()){
				stmt.addBatch("CALL p_t_msgType(" + c.ordinal() + ",'" + c.name() + "','" +  c.getDescription() + "')");
			}
			
			stmt.executeBatch();
			Config.getConfig().setLocalDBVersion(dbVersion);
			Config.write();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e2) {
			e2.printStackTrace();
		}
		return false;
	}
	
	public void createNewUser(final JTextField statusTextField, final JTextField serverIPTextField, final JTextField userNameTextField, final JTextField passWordTextField){
		Runnable tmp = new Runnable() {
			private Connection rootCon;
			private Statement stmt;

			public void run() {
				backupServerUrl	= "jdbc:mysql://" + serverIPTextField.getText() + ":3306/";
				try {
					statusTextField.setText("Versuche verbindung herzustellen!");
					this.rootCon 	=  DriverManager.getConnection(backupServerUrl, backupRootUser, rootPasswd);
					this.stmt 		= rootCon.createStatement();
					
					stmt.addBatch("create database if not exists " + dbName);
					stmt.addBatch("use " + dbName);
					statusTextField.setText("Lege User an!");
					stmt.addBatch("CREATE USER" + userNameTextField.getText() + " IDENTIFIED BY PASSWORD " + passWordTextField.getText());
					statusTextField.setText("Fertig!");
				} catch (SQLException e) {
					//TODO:
					statusTextField.setText(e.getMessage());
					LogEngine.log(this, "DB-Verbindung zu Backupserver fehlgeschlagen: " + e.getMessage(), LogEngine.ERROR);
				}
			}
			
		};
		(new Thread(tmp)).start();
		
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
