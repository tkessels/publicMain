package org.publicmain.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.rowset.CachedRowSet;

import org.publicmain.common.Config;
import org.publicmain.common.LogEngine;

import com.sun.rowset.CachedRowSetImpl;


public class BackupDBConnection {

	private static BackupDBConnection me;

public BackupDBConnection() {
//	DriverManager.setLogWriter(LogEngine.getLogWriter("Backupserver"));
}


	public static BackupDBConnection getBackupDBConnection() {
		if (me == null) {
			me = new BackupDBConnection();
		}
		return me;
	}

	/**Versucht mit den in den Einstellungen hinterlegten Zugangsdaten die BackupserverID abzufragen
	 * @return BackupUserID des Konfigurierten Push/Pull - Users oder <code>-1</code> wenn der User nicht existiert
	 */
	private long getMyID() {
		String username = Config.getConfig().getBackupDBChoosenUsername();
		String password = Config.getConfig().getBackupDBChoosenUserPassWord();
		return getIDfor(username, password);
	}

	/**Versucht mit den angegebenen Zugangsdaten die BackupserverID abzufragen
	 * @param username accountname auf dem Backupserver
	 * @param password accountpasswort auf dem Backupserver
	 * @return BackupUserID des Konfigurierten Push/Pull - Users oder <code>-1</code> wenn der User nicht existiert
	 */
	public long getIDfor(String username, String password) {
		long tmpID=-1;
		PreparedStatement prp = null;
		Connection x = null;
		ResultSet myid = null;
		try {
			x = getCon();
			prp = x.prepareStatement("Select backupUserID from t_backupUser where username like ? and password like ?");
			prp.setString(1, username);
			prp.setString(2, password);
			myid = prp.executeQuery();
			if (myid.first()) {
				tmpID=myid.getLong(1);
			}
			myid.close();
			prp.close();
		} catch (SQLException e) {
			LogEngine.log(this, e);
			return -1;
		}finally {
				   try{myid.close();  }catch(Exception ignored){}
				   try{prp.close();}catch(Exception ignored){}
				   try{x.close();}catch(Exception ignored){}
		}

		return tmpID;
	}





	public ResultSet pull_settings(){
		try {
			return getCon().createStatement().executeQuery("SELECT * FROM t_settings WHERE fk_t_backupUser_backupUserID_2 LIKE '" + getMyID() + "'");
		} catch (SQLException e) {
			LogEngine.log(this, "Error while pulling settings from backupDB " + e.getMessage(), LogEngine.ERROR );
			return null;
		}
	}


	public ResultSet pull_users(){
		try {
			return getCon().createStatement().executeQuery("SELECT userID, t_users.displayName, userName FROM t_users, t_messages WHERE userID = fk_t_users_userID_sender AND fk_t_backupUser_backupUserID LIKE '" + getMyID() + "'");
		} catch (SQLException e) {
			LogEngine.log(this, "Error while pulling users from backupDB " + e.getMessage(), LogEngine.ERROR );
			return null;
		}
	}

	public CachedRowSet pull_msgs(){
		Connection con=null;
		Statement stmt=null;
		ResultSet rs = null;
		try {
			long id = getMyID();
			CachedRowSet tmp = new CachedRowSetImpl();
			con = getCon();
			stmt=con.createStatement();
			rs=stmt.executeQuery("SELECT * FROM t_messages WHERE fk_t_backupUser_backupUserID LIKE '" + id + "'");
			tmp.populate(rs);
			return tmp;
		} catch (SQLException e) {
			LogEngine.log(this, "Error while pulling messages from backupDB " + e.getMessage(), LogEngine.ERROR );
			return null;
		}finally {
			   try{rs.close();  }catch(Exception ignored){}
			   try{stmt.close();}catch(Exception ignored){}
			   try{con.close();}catch(Exception ignored){}
	}
	}


	public synchronized boolean push_msgs(ResultSet tmp_messages) {
		long myID = getMyID();
		if(myID !=-1){
			try {
				PreparedStatement prp = getCon().prepareStatement("Insert ignore into t_messages(fk_t_backupUser_backupUserID, msgID, timestmp, fk_t_users_userID_sender, displayName, groupName, fk_t_users_userID_empfaenger, txt,  fk_t_msgType_ID) values(?,?,?,?,?,?,?,?,?)");
				while (tmp_messages.next()){
					prp.setLong(1, myID);
					prp.setInt(2,tmp_messages.getInt(1));
					prp.setLong(3, tmp_messages.getLong(2));
					prp.setLong(4,tmp_messages.getLong(3));
					prp.setString(5, tmp_messages.getString(4));
					prp.setString(6, tmp_messages.getString(7));
					long tmpUIDReciever = tmp_messages.getLong(6);
					if(tmp_messages.wasNull()){
						prp.setNull(7, java.sql.Types.BIGINT);
					}else{
						prp.setLong(7, tmpUIDReciever);
					}
					prp.setString(8,tmp_messages.getString(5));
					int tmp_msgID =tmp_messages.getInt(8);
					if(tmp_messages.wasNull()){
						prp.setNull(9, java.sql.Types.INTEGER);
					} else {
						prp.setInt(9,tmp_msgID);
					}
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
		else {
			LogEngine.log(this, "BackUpUser nicht vorhanden, ANLEGEN!", LogEngine.INFO);
			return false;
		}
	}




	public synchronized boolean push_users(ResultSet tmp_users) {
		long myID = getMyID();
		if(myID !=-1){
			try {
				PreparedStatement prp = getCon().prepareStatement("Insert ignore into t_users(userID, displayName, userName) values(?,?,?)");
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
				PreparedStatement prp = getCon().prepareStatement("Insert into t_settings(settingsKey, settingsValue, fk_t_backupUser_backupUserID_2) values(?,?,?) ON DUPLICATE KEY UPDATE settingsKey=VALUES(settingsKey), settingsValue=VALUES(settingsValue), fk_t_backupUser_backupUserID_2=VALUES(fk_t_backupUser_backupUserID_2)");
				while (tmp_settings.next()){
					prp.setString(1, tmp_settings.getString(1));
					prp.setString(2, tmp_settings.getString(3));
					prp.setLong(3, myID);
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

	public int getStatus() {
		try {
			Connection x = getCon();
			x.close();
			//level 1
			long id = getMyID();
			if (id!=-1) return 2;
			return 1;

		} catch (SQLException e) {
			return 0;
		}
	}

	private synchronized boolean userexists(String userName) throws SQLException{
		PreparedStatement prp = getCon().prepareStatement("Select backupUserID from t_backupUser where username like ?");
		prp.setString(1, userName);
		ResultSet tmp_rs= prp.executeQuery();
		boolean tmp = (tmp_rs.next());
		prp.close();
		return tmp;
	}

	public synchronized boolean createUser (String usrName, String passwd){
		try {
			if(!userexists(usrName)){
				PreparedStatement prp = getCon().prepareStatement("Insert into t_backupUser(username,password) values(?,?)");
				prp.setString(1, usrName);
				prp.setString(2, passwd);
				prp.execute();
				prp.close();
				return true;
			}
			else{
				LogEngine.log(this, "BackupUser already exists",LogEngine.ERROR);
			}
		} catch (SQLException e) {
			LogEngine.log(this, e);
		}
		return false;
	}

	public synchronized boolean deleteAllMessages(){
		try {
			PreparedStatement prp = getCon().prepareStatement("delete from t_messages where fk_t_backupUser_backupUserID = ?");
			prp.setLong(1, getMyID());
			prp.execute();
			prp.close();
		} catch (SQLException e) {
			LogEngine.log(this, e);
		}
		return false;
	}

	private Properties convertResultToConfig (ResultSet settingsRS){
		Properties tmp = new Properties();
		try {
			settingsRS.beforeFirst();
			while (settingsRS.next()){
				tmp.put(settingsRS.getString(1), settingsRS.getString(3));
			}
		} catch (SQLException e) {
			LogEngine.log(this,e);
		}
		return tmp;
	}


	public synchronized boolean deleteUser(){
		String usrName = Config.getConfig().getBackupDBChoosenUsername();
		String passwd = Config.getConfig().getBackupDBChoosenUserPassWord();
		return deleteUser(usrName, passwd);
	}


	/**
	 * @param usrName
	 * @param passwd
	 * @throws SQLException
	 */
	public boolean deleteUser(String usrName, String passwd){
		long tmp = getIDfor(usrName, passwd);
		try {
			if(tmp!=-1){
				PreparedStatement prp = getCon().prepareStatement("delete from t_backupUser where username = ?  and password = ?");
				prp.setString(1, usrName);
				prp.setString(2, passwd);
				prp.execute();
				prp.close();
				return true;
			}
			else{
				LogEngine.log(this, "No such user or wrong password!",LogEngine.ERROR);
			}
		} catch (SQLException e) {
			LogEngine.log(this, e);

		}
		return false;
	}


	public Connection getCon() throws SQLException {
		DriverManager.setLoginTimeout(0);
		return DriverManager.getConnection("jdbc:mysql://"+Config.getConfig().getBackupDBIP()+":"+ Config.getConfig().getBackupDBPort()+"/"+Config.getConfig().getBackupDBDatabasename()+"?connectTimeout=1000", Config.getConfig().getBackupDBUser(), Config.getConfig().getBackupDBPw());
	}

	public Properties getConfig(String user, String password) throws IllegalArgumentException {
		try {
			long tmp_ID = getIDfor(user, password);
			if (tmp_ID != -1) {
				PreparedStatement prp = getCon().prepareStatement("SELECT * FROM t_settings WHERE fk_t_backupUser_backupUserID_2 LIKE ?");
				prp.setLong(1, tmp_ID);
				ResultSet config_data = prp.executeQuery();
				Properties tmp = convertResultToConfig(config_data);
				prp.close();
				return tmp;
			} else
				throw new IllegalArgumentException("Given BackupUserAccount not valid");
		} catch (SQLException e) {
			LogEngine.log(this, "Error while pulling settings from backupDB " + e.getMessage(), LogEngine.ERROR);
			return null;
		}
	}

}
