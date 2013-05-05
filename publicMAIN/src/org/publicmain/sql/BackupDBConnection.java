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


/**
 * @author ATRM
 * 	Über diese Klasse kann mit der BackupDatenbank kommuniziert werden.
 *  Sie beinhaltet Methoden zum Abfragen und Speichern von Daten und stellt eine Verbindunge zum BackupServer her. 
 *
 */
public class BackupDBConnection {

	private static BackupDBConnection me;

	public static BackupDBConnection getBackupDBConnection() {
		if (me == null) {
			me = new BackupDBConnection();
		}
		return me;
	}

	/**Versucht mit den in den Einstellungen hinterlegten Zugangsdaten die BackupserverID abzufragen
	 * @return BackupUserID des Konfigurierten Push/Pull - Users oder <code>-1</code> wenn der User nicht existiert
	 */
	public long getMyID() {
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

	/**
	 * Über diese Methode kann der Status der Backupdatenbankverbindung abgefragt werden
	 * @return:	0 - keine Verbindung zum Backupserver
	 * @return:	1 - Verbindung besteht, aber kein User eingerichtet
	 * @return:	2 -	Verbindung besteht, angegebene Nutzerdaten korrekt.
	 */
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

	/**
	 * Diese Methode fragt alle settings eines bestimmten Nutzers in der Datenabank ab.
	 * @param id: ID des bestimmten Benutzers 
	 * @return ResultSet: Ergebnis der Abfrage
	 */
	public ResultSet pull_settings(long id){
		Connection con=null;
		Statement stmt=null;
		ResultSet rs = null;
		try {
			CachedRowSet tmp = new CachedRowSetImpl();
			con = getCon();
			stmt=con.createStatement();
			rs=stmt.executeQuery("SELECT * FROM t_settings WHERE fk_t_backupUser_backupUserID_2 LIKE '" + id + "'");
			tmp.populate(rs);
			return tmp;
		} catch (SQLException e) {
			LogEngine.log(this, "Error while pulling settings from backupDB " + e.getMessage(), LogEngine.ERROR );
			return null;
		}finally {
			   try{rs.close();  }catch(Exception ignored){}
			   try{stmt.close();}catch(Exception ignored){}
			   try{con.close();}catch(Exception ignored){}
		}
	}

	/**
	 * Diese Methode fragt die UserTabelle eines bestimmten Nutzers in der BackupDatenbank ab.
	 * @param id: ID des bestimmten Benutzers 
	 * @return ResultSet: Ergebnis der Abfrage
	 */
	public ResultSet pull_users(long id){
		Connection con=null;
		Statement stmt=null;
		ResultSet rs = null;
		try {
			CachedRowSet tmp = new CachedRowSetImpl();
			con = getCon();
			stmt=con.createStatement();
			rs=stmt.executeQuery("SELECT userID, t_users.displayName, userName FROM t_users, t_messages WHERE userID = fk_t_users_userID_sender AND fk_t_backupUser_backupUserID LIKE '" + id + "'");
			tmp.populate(rs);
			return tmp;
			
		} catch (SQLException e) {
			LogEngine.log(this, "Error while pulling users from backupDB " + e.getMessage(), LogEngine.ERROR );
			return null;
		}finally {
			   try{rs.close();  }catch(Exception ignored){}
			   try{stmt.close();}catch(Exception ignored){}
			   try{con.close();}catch(Exception ignored){}
		}
	}
	
	/**
	 * Diese Methode fragt alle Nachrichten eines bestimmten Nutzers in der BackupDatenbank ab.
	 * @param id: ID des bestimmten Benutzers 
	 * @return ResultSet: Ergebnis der Abfrage
	 */
	public ResultSet pull_msgs(long id){
		Connection con=null;
		Statement stmt=null;
		ResultSet rs = null;
		try {
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

	/**
	 * Diese Methode speichert alle Nachichten eines bestimmten Nutzers in der BackupDatenbank ab.
	 * @param tmp_messages: Übergebenes ResultSet welches die zu speichernden Nachichten beinhaltet.
	 * @param id: ID des Nutzers, dessen Nachichten gespeichert werden sollen
	 * @return 	true: 	Speichern erfolgreich
	 * 			false:	Speichern fehlgeschlagen
	 */
	public synchronized boolean push_msgs(ResultSet tmp_messages, long id) {
		if(id !=-1){
			Connection con=null;
			PreparedStatement  prp=null;
			try {
				con = getCon();
				prp = con.prepareStatement("Insert ignore into t_messages(fk_t_backupUser_backupUserID, msgID, timestmp, fk_t_users_userID_sender, displayName, groupName, fk_t_users_userID_empfaenger, txt,  fk_t_msgType_ID) values(?,?,?,?,?,?,?,?,?)");
				while (tmp_messages.next()){
					prp.setLong(1, id);
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
			}finally {
				try{prp.close();}catch(Exception ignored){}
				try{con.close();}catch(Exception ignored){}
			}
		}
		else {
			LogEngine.log(this, "BackUpUser nicht vorhanden, ANLEGEN!", LogEngine.INFO);
			return false;
		}
	}

	/**
	 * Diese Methode speichert die Nutzertabelle eines bestimmten Nutzers in der BackupDatenbank ab.
	 * @param tmp_users: Übergebenes ResultSet welches die zu speichernden User(Nutzer) beinhaltet.
	 * @param id: ID des Nutzers, dessen Nutzertabelle gespeichert werden sollen
	 * @return 	true: 	Speichern erfolgreich
	 * 			false:	Speichern fehlgeschlagen
	 */
	public synchronized boolean push_users(ResultSet tmp_users,long id) {
		if(id !=-1){
			Connection con=null;
			PreparedStatement prp = null;
			try {
				con=getCon();
				prp = con.prepareStatement("Insert ignore into t_users(userID, displayName, userName) values(?,?,?)");
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
			}finally {
				try{prp.close();}catch(Exception ignored){}
				try{con.close();}catch(Exception ignored){}
			}

		}
		else return false;
		return true;	
	}

	/**
	 * Diese Methode speichert alle Einstellungen (Settings) eines bestimmten Nutzers in der BackupDatenbank ab.
	 * @param tmp_settings: Übergebenes ResultSet welches die zu speichernden Einstellungen beinhaltet.
	 * @param id: ID des Nutzers, dessen Einstellungen gespeichert werden sollen
	 * @return 	true: 	Speichern erfolgreich
	 * 			false:	Speichern fehlgeschlagen
	 */
	public synchronized boolean push_settings(ResultSet tmp_settings,long id) {
		if(id !=-1){
			Connection con = null;
			PreparedStatement prp=null;
			try {
				con = getCon();
				prp = con.prepareStatement("Insert into t_settings(settingsKey, settingsValue, fk_t_backupUser_backupUserID_2) values(?,?,?) ON DUPLICATE KEY UPDATE settingsKey=VALUES(settingsKey), settingsValue=VALUES(settingsValue), fk_t_backupUser_backupUserID_2=VALUES(fk_t_backupUser_backupUserID_2)");
				while (tmp_settings.next()){
					prp.setString(1, tmp_settings.getString(1));
					prp.setString(2, tmp_settings.getString(3));
					prp.setLong(3, id);
					prp.addBatch();
				}
				prp.executeBatch();
				prp.close();

			} catch (SQLException e) {
				LogEngine.log(this, e);
			}finally {
				try{prp.close();}catch(Exception ignored){}
				try{con.close();}catch(Exception ignored){}
			}
		}
		else return false;
		return true;
	}
	
	/**
	 * Diese Methode überprüft ob ein bestimmter Nutzer in der BackupDatenbank angelegt ist
	 * @param userName: Username des gesuchten Nutzers
	 * @return	true:	gesuchter Nutzer vorhanden
	 * @return	false:	gesuchter Nutzer nicht vorhanden
	 * @throws SQLException
	 */
	private synchronized boolean userexists(String userName) throws SQLException{
		PreparedStatement prp = getCon().prepareStatement("Select backupUserID from t_backupUser where username like ?");
		prp.setString(1, userName);
		ResultSet tmp_rs= prp.executeQuery();
		boolean tmp = (tmp_rs.next());
		prp.close();
		return tmp;
	}

	/**
	 * Diese Methode dient zum Anlegen eines neuen Benutzers.
	 * Sollte dieser schon vorhanden sein: return false.
	 * @param usrName:	gewünschter Username
	 * @param passwd:	gewpnschtes Passwort
	 * @return	true:	Nutzer angelegt
	 * @return	false:	Nutzer nicht angelegt (schon vorhanden / SQL Exception)
	 */
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

	/**
	 * Diese Methode löscht alle Nachrichten des aktuellen Benutzers
	 * @return	true:	Löschen aller Nachirchten erfolgreich
	 * @return	false:	Löschen aller Nachirchten nicht erfolgreich
	 */
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

	/**
	 * Diese Methode löscht den aktuell genutzten User von der BackupDatenbank
	 * @return	true:	Löschen erfolgreich
	 * @return	false:	Löschen nicht erfolgreich
	 */
	public synchronized boolean deleteUser(){
		String usrName = Config.getConfig().getBackupDBChoosenUsername();
		String passwd = Config.getConfig().getBackupDBChoosenUserPassWord();
		return deleteUser(usrName, passwd);
	}

	/**
	 * Diese Methode löscht einen bestimmten Benutzer (Benutzernamen-Passwort-kombination) von der BackupDatenbank
	 * @param usrName:	UserName des zu löschenden Nutzers
	 * @param passwd:	Passwort des zu löschenden Nutzers
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

	/**
	 * Diese Methode wandelt ein ResultSet von Settings in ein Config-Objekt um (Properties)
	 * @param settingsRS:	ResultSet der umzuwandelnden settings
	 * @return	Properties-Objekt welches Settings aus dem ResultSet zum Inhalt hat
	 */
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

	/**
	 * Diese Methode stellt eine Verbinung mit in der Config gespeicherten Daten her
	 * @return Connection: mit ConfigDaten hergestellte Verbindung
	 * @throws SQLException
	 */
	public Connection getCon() throws SQLException {
		return getCon(Config.getConfig().getBackupDBIP(), Config.getConfig().getBackupDBPort(),Config.getConfig().getBackupDBDatabasename(),Config.getConfig().getBackupDBUser(), Config.getConfig().getBackupDBPw(),1000);
	}
	
	/**
	 * Diese Methode stellt eine Verbinung mit gegenen Parametern her
	 * @param ip:			IP-Adresse des Zielservers	
	 * @param port:			Port des Zielservers
	 * @param databasename:	Anzusprechender Datenbankname auf dem Zielserver
	 * @param user:			BenutzerName welcher zum Verbindungsaufbau genutzt werden soll
	 * @param password:		Passwort welches zum Verbindungsaufbau genutzt werden soll
	 * @param timeout:		gewünschtes VerbindungsTimeOut
	 * @return Connection: 	mit gegebenen Parametern hergestellte Verbindung
	 * @throws SQLException
	 */
	public Connection getCon(String ip, String port,String databasename,String user,String password, long timeout) throws SQLException{
		return DriverManager.getConnection("jdbc:mysql://"+ip+":"+ port+"/"+databasename+"?connectTimeout="+timeout, user, password);
	}

	/**
	 * Diese Methode liefert die Settings eines bestimmten Benutzers aus der BackupDatenbank 
	 * @param user:		UserName des bestimmten Nutzers 
	 * @param password:	Passwort des bestimmten Nutzers
	 * @return	Properties-Objekt welche die gewünschten Settings enthält.
	 * @throws IllegalArgumentException
	 */
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
