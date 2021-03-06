package org.publicmain.sql;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.sql.rowset.CachedRowSet;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.Config;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.NachrichtenTyp;
import org.publicmain.common.Node;
import org.resources.Help;

import com.sun.rowset.CachedRowSetImpl;

/**
 * Die Klasse DBConnection stellt die Verbindung zu dem Lokalen DB-Server her.
 * Sie legt weiterhin alle zwingend notwendigen Datenbanken(1) und Tabellen an.
 * Zudem stellt sie Methoden zum Abfragen und Speichern von Daten zur Verf�gung.
 *
 * @author ATRM
 *
 */
public class LocalDBConnection {

	private Connection con;
	private Statement stmt;
	private String url;
	private String dbName;
	private String rootUser;
	private String rootPasswd;

	//Tabellen der Loc DB
	private String usrTbl;
	private String nodeTbl;
	private String msgTbl;
	private Set<Node> allnodes = Collections.synchronizedSet(new HashSet<Node>());

	//--------neue Sachen------------
	private int dbStatus;					// 0=nicht bereit	1=con besteht	2=DB und Tabellen angelegt 3=mit db verbunden
	private int maxVersuche;
	private long warteZeitInSec;
	private Thread connectToDBThread;
	private final static int LOCAL_DATABASE_VERSION = 22;
	private DatabaseEngine databaseEngine;
	private boolean ceReadyForWritingSettings;
	//--------neue Sachen ende------------

	// verbindungssachen
	private static LocalDBConnection me;

	private void addnodez(Collection<Node> node){
		synchronized (allnodes) {
			allnodes.removeAll(node);
			allnodes.addAll(node);
		}
	}
	private Connection getCon() throws SQLException {
		
		return DriverManager.getConnection("jdbc:mysql://localhost:"+Config.getConfig().getLocalDBPort()+"/"+Config.getConfig().getLocalDBDatabasename()+"?connectTimeout=1000",Config.getConfig().getLocalDBUser(), Config.getConfig().getLocalDBPw());
	}


	/**
	 * Konstruktor der LocalDBConnection
	 * 
	 * @param databaseEngine
	 */
	private LocalDBConnection(DatabaseEngine databaseEngine) {
		this.url 					= "jdbc:mysql://localhost:"+Config.getConfig().getLocalDBPort()+"/";
		this.dbName 				= Config.getConfig().getLocalDBDatabasename();
		this.rootUser				= "root";
		this.rootPasswd				= "";
		this.usrTbl					= "t_user";
		this.nodeTbl				= "t_nodes";
		this.msgTbl					= "t_messages";
		this.databaseEngine			= databaseEngine;
		//--------neue Sachen------------
		this.dbStatus	= 0;				// 0=nicht bereit	1=con besteht	2=Tables wurden angelegt 3=use db_publicmain erfolgreich / als user publicMain angemeldet / hardWorkingThread l�uft
		this.maxVersuche = 5;
		this.warteZeitInSec = 10;
		//		this.writtenStandardUser = false;
		this.connectToDBThread = new Thread(new firstConnectToLocDBServerBot());
		this.ceReadyForWritingSettings = false;
		//--------neue Sachen ende------------
		connectToDBThread.start();
	}



	/**
	 * Factorymethode f�r die LocDBConnection.
	 *  
	 * @param databaseEngine
	 * @return instanz der LocDBConnection
	 */
	public static LocalDBConnection getDBConnection(DatabaseEngine databaseEngine) {
		if (me == null) {
			me = new LocalDBConnection(databaseEngine);
		}
		return me;
	}



	/**
	 * Ist f�r den ersten Verbindungsaufbau verantwortlich. Pr�ft den Status der
	 * DB durch Abfragen. L�sst DB und notwendige Benutzer anlegen.
	 * 
	 * Braucht eine komplette �berarbeitung da durch Verschachtelungen und
	 * Erweiterungen mit der Zeit un�bersichtlich. Statuspr�fmethode ->
	 * SWITCH-CASE zur abarbeitung der aufgaben. NEED MORE TIME!
	 */
	private class firstConnectToLocDBServerBot implements Runnable { // wird nur vom Construktor aufgerufen

		public void run() {
			int versuche = 0;
			if(connectToLocDBServerAspublicMain() && correctLocDBVersion()){
				databaseEngine.go();
				LogEngine.log(this, "DB-Status: " + dbStatus, LogEngine.INFO);
			} else {
				while ((versuche < maxVersuche) && (dbStatus == 0)) { // hatte da erst con==null drin aber das ist ein problem beim reconnecten unten.
					if(connectToLocDBServerAsRoot()){
						versuche = 0;
					} else {
						versuche ++;
					}
				}
				if(dbStatus >= 1){
					try {
						synchronized (stmt) {
							stmt.executeQuery("use " + dbName);
						}
						if(!correctLocDBVersion()){
							createDbAndTables();
							run();
						} else {
							if (connectToLocDBServerAspublicMain()) {
								dbStatus = 3;
								databaseEngine.go();
								LogEngine.log(this, "DB-Status: " + dbStatus, LogEngine.INFO);
							}
							else {
								dbStatus = 0;
								connectToLocDBServerAsRoot();
								createDbAndTables();
								run();
							}
						}
					} catch (SQLException e1) {
						createDbAndTables();
						run();
					}
				} else {
					LogEngine.log(this,	versuche +". Versuch zum DB-Verbindungsaufbau fehlgeschlagen -> f�r Zugriff auf locDB sorgen und neu starten! ", LogEngine.ERROR);
				}
			}
		}
	}

	/**
	 * Die Methode pr�ft ob die in der Config geschriebene DB-Version mit der in
	 * dieser Klasse befindlichen DB-Version �berein stimmt.
	 * 
	 * @return true DB-Version korrekt.
	 * @return false DB-Version !korrekt.
	 */
	private boolean correctLocDBVersion (){
		if(Config.getConfig().getLocalDBVersion() == LOCAL_DATABASE_VERSION)
			return true;
		return false;
	}



	/**
	 * Diese Methode stellt eine Verbindung als root zum DB-Server her.
	 * 
	 * @return true = erfolgreich.
	 * @return false = fehlgeschlagen.
	 */
	private synchronized boolean connectToLocDBServerAsRoot(){
		try {
			if(stmt!= null) {
				stmt.close();
			}
			if(con!= null) {
				con.close();
			}
			con = DriverManager.getConnection(url, rootUser, rootPasswd);
			stmt = con.createStatement();
			LogEngine.log(this, "DB-ServerVerbindung als " + rootUser + " hergestellt", LogEngine.INFO);
			dbStatus = 1;
			return true;
		} catch (SQLException e) {
			try {
				Thread.sleep(warteZeitInSec * 1000);
			} catch (InterruptedException e1) {
				LogEngine.log(this,"Fehler beim Warten: " + e1.getMessage(),LogEngine.ERROR);
			}
			dbStatus = 0;
			return false;
		}
	}


	/**
	 * Diese Methode stellt eine Verbindung mit in der Config angegebenen
	 * Benutzer zum DB-Server her.
	 * 
	 * @return true = erfolgreich.
	 * @return false = fehlgeschlagen.
	 */
	private synchronized boolean connectToLocDBServerAspublicMain() {
		try {
			if (stmt != null) {
				stmt.close();
			}
			if (con != null) {
				con.close();
			}
			con = DriverManager.getConnection(url, Config.getConfig().getLocalDBUser(), Config.getConfig().getLocalDBPw());
			stmt = con.createStatement();
			synchronized (stmt) {
				stmt.executeQuery("use " + dbName);
				dbStatus = 3;
			}
			LogEngine.log(this, "DB-ServerVerbindung als "+ Config.getConfig().getLocalDBUser() + " hergestellt",LogEngine.INFO);
			return true;
		} catch (SQLException e) {
			LogEngine.log(this,"Couldn�t connect with user 'publicMain'",LogEngine.INFO);
			dbStatus = 0;
			return false;
		}
	}
	public int getStatus2() {
		try {
			getCon();
			return 1;
		} catch (Exception e) {
			return 0;
		}
		
	}

	/**
	 * Diese �ffentliche Methode repr�sentiert den Status der DB nach 'Aussen'.
	 * 
	 * @return true = DB berreit zum schreiben.
	 * @return false = DB !berreit zum schreiben.
	 */
	public boolean getStatus(){
//		return (getStatus2()==1);
		if (dbStatus==3)
			return true;
		else
			return false;
	}

	/**
	 * Wird ein Verbindungsabbruch zur DB festgestellt wird diese Methode zum
	 * reconnect genutzt. Methode wartet festgelegte Zeit zwischen den
	 * Versuchen, welche durch einen maximalwert begrenzt sind.
	 * 
	 */
	public void reconnectToLocDBServer(){
		new Thread(new Runnable() {
			public void run() {
				int reconnectVersuche = 1;
				while ((dbStatus < 3) && (maxVersuche >= reconnectVersuche)){
					LogEngine.log(this, "Lost connetion to locDBSvr. Try to reconnect in 10 seconds " + reconnectVersuche + "/" + maxVersuche +".", LogEngine.ERROR);
					try {
						Thread.sleep(warteZeitInSec * 1000);
					} catch (InterruptedException e1) {
					}
					if (connectToLocDBServerAspublicMain()){
						dbStatus = 3;
						databaseEngine.go();
						reconnectVersuche = 0;
					}
					reconnectVersuche++;
					if (reconnectVersuche == maxVersuche) {
						LogEngine.log(this, "Lost connection to locDBSvr conclusively - will not try again.",LogEngine.ERROR);
					}
				}
			}
		}).start();

	}


	/**
	 * Diese Methode erstellt die Datenbank aus einem create-script, legt alle
	 * Procedures und Trigger an, legt alle notwenigen Benutzer an.
	 */
	// Wird nur vom Konstruktor aufgerufen.
	private void createDbAndTables (){
		String read=null;
		synchronized (stmt) {
			try (BufferedReader in = new BufferedReader(new FileReader(Help.getFile("create_LocalDB.sql")))){
				while((read = in.readLine()) != null) {
					while (!read.endsWith(";") && !read.endsWith("--")){
						read = read + in.readLine();
					}
					stmt.addBatch(read);
				}
				// INSERT PROCEDURES

				stmt.addBatch("DROP PROCEDURE IF EXISTS `db_publicmain`.`p_t_messages_savePrivateMessage`;");
				stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_messages_savePrivateMessage` (IN newMsgID INT(11), IN newTimestmp BIGINT(20), IN newFk_t_users_userID_sender BIGINT(20), IN newTxt VARCHAR(200), IN newFk_t_users_userID_empfaenger BIGINT(20)) BEGIN INSERT IGNORE INTO t_messages (msgID,timestmp,fk_t_users_userID_sender, txt,fk_t_users_userID_empfaenger) VALUES (newMsgID, newTimestmp, newFk_t_users_userID_sender, newTxt, newFk_t_users_userID_empfaenger); END;");
				stmt.addBatch("DROP PROCEDURE IF EXISTS `db_publicmain`.`p_t_messages_saveGroupMessage`;");
				stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_messages_saveGroupMessage` (IN newMsgID INT(11), IN newTimestmp BIGINT(20), IN newFk_t_users_userID_sender BIGINT(20), IN newTxt VARCHAR(200), IN newFk_t_groups_groupName VARCHAR(20)) BEGIN INSERT IGNORE INTO t_messages (msgID,timestmp, fk_t_users_userID_sender, txt, fk_t_groups_groupName) VALUES (newMsgID, newTimestmp, newFk_t_users_userID_sender, newTxt, newFk_t_groups_groupName); END;");
				stmt.addBatch("DROP PROCEDURE IF EXISTS `db_publicmain`.`p_t_messages_saveSystemMessage`;");
				stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_messages_saveSystemMessage` (IN newMsgID INT(11), IN newTimestmp BIGINT(20), IN newFk_t_users_userID_sender BIGINT(20), IN newFk_t_users_userID_empfaenger BIGINT(20),IN newFk_t_msgType_ID INT) BEGIN INSERT IGNORE INTO t_messages (msgID,timestmp,fk_t_users_userID_sender, fk_t_users_userID_empfaenger,fk_t_msgType_ID) VALUES (newMsgID, newTimestmp, newFk_t_users_userID_sender, newFk_t_users_userID_empfaenger, newFk_t_msgType_ID); END;");

				stmt.addBatch("DROP PROCEDURE IF EXISTS `db_publicmain`.`p_t_groups_saveGroups`;");
				stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_groups_saveGroups` (IN newGroupName VARCHAR(20),IN newFk_t_users_userID BIGINT(20)) BEGIN insert into t_groups (groupName, fk_t_users_userID) values (newGroupName,newFk_t_users_userID) ON DUPLICATE KEY UPDATE fk_t_users_userID=VALUES(fk_t_users_userID); END;");

				stmt.addBatch("DROP PROCEDURE IF EXISTS `db_publicmain`.`p_t_user_saveUsersAndNodes`;");
				stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_user_saveUsersAndNodes` (IN newUserID BIGINT(20),IN newDisplayName VARCHAR(45),IN newUserName VARCHAR(45), IN newNodeID BIGINT(20),  IN newComputerName VARCHAR(45)) BEGIN	DELETE FROM t_nodes WHERE fk_t_users_userID_2 = newUserID; INSERT INTO t_users (userID, displayName, userName) VALUES (newUserID,newDisplayName,newUserName) ON DUPLICATE KEY UPDATE displayName=VALUES(displayName), userName=VALUES(userName); INSERT INTO t_nodes(nodeID, computerName, fk_t_users_userID_2) VALUES (newNodeID, newComputerName, newUserID) ON DUPLICATE KEY UPDATE computerName=VALUES(computerName), fk_t_users_userID_2=VALUES(fk_t_users_userID_2); END;");

				stmt.addBatch("DROP PROCEDURE IF EXISTS `db_publicmain`.`p_t_msgType`;");
				stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_msgType`(IN newMsgTypeID INT,IN newName VARCHAR(45),IN newDescription VARCHAR(45)) BEGIN INSERT INTO t_msgType (msgTypeID, name, description) VALUES (newMsgTypeID,newName,newDescription) ON DUPLICATE KEY UPDATE name=VALUES(name),description=VALUES(description); END;");

				stmt.addBatch("DROP PROCEDURE IF EXISTS `db_publicmain`.`p_t_nodes_saveNodes`;");
				stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_nodes_saveNodes` (IN newNodeID BIGINT(20), IN newComputerName VARCHAR(45), IN newFk_t_users_userID_2 BIGINT(20), IN newFk_t_nodes_nodeID BIGINT(20)) BEGIN INSERT INTO t_nodes (nodeID, computerName, fk_t_users_userID_2, fk_t_nodes_nodeID) VALUES (newNodeID, newComputerName, newFk_t_users_userID_2, newFk_t_nodes_nodeID) ON DUPLICATE KEY UPDATE computerName=VALUES(computerName), fk_t_users_userID_2=VALUES(fk_t_users_userID_2), fk_t_nodes_nodeID=VALUES(fk_t_nodes_nodeID); END;");

				stmt.addBatch("DROP PROCEDURE IF EXISTS `db_publicmain`.`p_t_settings_saveSettings`;");
				stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_settings_saveSettings` (IN newSettingsKey VARCHAR(45),IN newFk_t_users_userID_3 BIGINT(20), IN newSettingsValue VARCHAR(100)) BEGIN INSERT INTO t_settings (settingsKey, fk_t_users_userID_3, settingsValue) VALUES (newsettingsKey, newFk_t_users_userID_3, newSettingsValue) ON DUPLICATE KEY UPDATE fk_t_users_userID_3=VALUES(fk_t_users_userID_3), settingsValue=VALUES(settingsValue); END;");
				// PUSH PROCEDURES
				stmt.addBatch("DROP PROCEDURE IF EXISTS `db_publicmain`.`p_t_messages_pushMessages`;");
				stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_messages_pushMessages` (IN newMsgID INT(11), IN newTimestmp BIGINT(20), IN newFk_t_users_userID_sender BIGINT(20),IN newDisplayName VARCHAR(200), IN newTxt VARCHAR(200),  IN newFk_t_users_userID_empfaenger BIGINT(20), IN newFk_t_groups_groupName VARCHAR(20), IN newFk_t_msgType_ID INT) BEGIN INSERT IGNORE INTO t_messages (msgID,timestmp, fk_t_users_userID_sender, displayName, txt, fk_t_users_userID_empfaenger, fk_t_groups_groupName, fk_t_msgType_ID) VALUES (newMsgID, newTimestmp, newFk_t_users_userID_sender, newDisplayName, newTxt, newFk_t_users_userID_empfaenger, newFk_t_groups_groupName, newFk_t_msgType_ID);END;");

				stmt.addBatch("DROP PROCEDURE IF EXISTS `db_publicmain`.`p_t_user_pushUsers`;");
				stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_user_pushUsers` (IN newUserID BIGINT(20),IN newDisplayName VARCHAR(45),IN newUserName VARCHAR(45)) BEGIN INSERT IGNORE INTO t_users (userID, displayName, userName) VALUES (newUserID,newDisplayName,newUserName); END;");

				// TRIGGER
				stmt.addBatch("DROP TRIGGER IF EXISTS `db_publicmain`.`tr_t_messages`;");
				stmt.addBatch("CREATE TRIGGER `db_publicmain`.`tr_t_messages` BEFORE INSERT ON t_messages FOR EACH ROW SET new.displayName = (SELECT displayName FROM t_users WHERE userID = new.fk_t_users_userID_sender);");

				// Hier wird die Tabelle t_msgType automatisch bef�llt!
				for (MSGCode c : MSGCode.values()){
					stmt.addBatch("CALL p_t_msgType(" + c.ordinal() + ",'" + c.name() + "','" +  c.getDescription() + "')");
				}
				stmt.executeBatch();
				dbStatus = 2;
				Config.getConfig().setLocalDBVersion(LOCAL_DATABASE_VERSION);
				Config.write();
				LogEngine.log(this, "DB-Status: " + dbStatus, LogEngine.INFO);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (SQLException e) {
				dbStatus = 0;
				LogEngine.log(this, e.getMessage(), LogEngine.ERROR);
				e.printStackTrace();
			}
			try {
				stmt.addBatch("DELETE FROM mysql.user WHERE user =''");
				stmt.addBatch("CREATE USER '" + Config.getConfig().getLocalDBUser() + "' IDENTIFIED BY '" + Config.getConfig().getLocalDBPw() + "'");
				stmt.addBatch("GRANT ALL PRIVILEGES ON * . * TO  '" + Config.getConfig().getLocalDBUser() + "'@'%' WITH GRANT OPTION MAX_QUERIES_PER_HOUR 0 MAX_CONNECTIONS_PER_HOUR 0 MAX_UPDATES_PER_HOUR 0 MAX_USER_CONNECTIONS 0");
				stmt.executeBatch();
			} catch (SQLException e) {
			}
		}

	}


	/**
	 * Fragt die View 'v_pullAll_t_messages' ab und gibt das Ergebniss als
	 * ResultSet zur�ck.
	 * 
	 * @return ResultSet
	 */
	public ResultSet pull_msgs(){
		if (dbStatus >= 3){
			synchronized (stmt) {
				try {
					return stmt.executeQuery("SELECT * FROM v_pullAll_t_messages");
				} catch (SQLException e) {
					dbStatus = 0;
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * Fragt die View 'v_pullAll_t_users' ab und gibt das Ergebniss als
	 * ResultSet zur�ck.
	 * 
	 * @return ResultSet
	 */
	public ResultSet pull_users() {
		if (dbStatus >= 3) {
			synchronized (stmt) {
				try {
					return stmt.executeQuery("SELECT * FROM v_pullAll_t_users");
				} catch (SQLException e) {
					LogEngine.log(this, "Error while pulling all users: " + e.getMessage(), LogEngine.ERROR);
					dbStatus = 0;
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * Fragt die View 'v_pullALL_t_settings' ab und gibt das Ergebniss als
	 * ResultSet zur�ck.
	 * 
	 * @return ResultSet
	 */
	public ResultSet pull_settings() {
		if (dbStatus >= 3) {
			synchronized (stmt) {
				try {
					return stmt.executeQuery("SELECT * FROM v_pullALL_t_settings");
				} catch (SQLException e) {
					dbStatus = 0;
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * Diese Methode speichert alle Nachrichten in der LocDB indem es eine
	 * Prozedur aufruft.
	 * 
	 * @param msgRS
	 *            zu speicherndes ResultSet
	 * @return true Speichern hat geklappt
	 * @return false Speichern hat !geklappt
	 */
	public boolean push_msgs(ResultSet msgRS){
		if ((dbStatus >=3) &&  (msgRS != null)){
			try {
				synchronized (stmt) {
					PreparedStatement prp = con.prepareStatement("CALL p_t_messages_pushMessages(?,?,?,?,?,?,?,?)");
					while (msgRS.next()){
						prp.setInt(1, msgRS.getInt(1));
						prp.setLong(2, msgRS.getLong(2));
						prp.setLong(3, msgRS.getLong(3));
						prp.setString(4, msgRS.getString(6));
						prp.setString(5, msgRS.getString(5));
						long tmp = msgRS.getLong(7);
						if(msgRS.wasNull()){
							prp.setNull(6, java.sql.Types.BIGINT);
						}else{
							prp.setLong(6, tmp);
						}
						prp.setString(7, msgRS.getString(4));
						int tmpMsgID = msgRS.getInt(8);
						if (msgRS.wasNull()){
							prp.setNull(8, java.sql.Types.INTEGER);
						} else {
							prp.setInt(8, tmpMsgID);
						}
						prp.addBatch();
					}
					prp.executeBatch();
					prp.close();
					return true;
				}
			} catch (SQLException e) {
				LogEngine.log(this, "Error while executing 'push_msgs': " + e.getMessage(), LogEngine.ERROR);
				return false;
			}
		}
		return false;
	}

	/**
	 * Diese Methode speichert alle Einstellungen in der LocDB indem es eine
	 * Prozedur aufruft.
	 * 
	 * @param tmp_settings
	 *            zu speicherndes ResultSet
	 * @return true Speichern hat geklappt
	 * @return false Speichern hat !geklappt
	 */
	public boolean push_settings(ResultSet tmp_settings){
		if ((dbStatus >=3) &&  (tmp_settings != null)){
			try {
				PreparedStatement prp = con.prepareStatement("CALL p_t_settings_saveSettings(?,?,?)");
				while (tmp_settings.next()){
					prp.setString(1, tmp_settings.getString(1));
					prp.setLong(2, ChatEngine.getCE().getUserID());
					prp.setString(3, tmp_settings.getString(3));
					prp.addBatch();
				}
				prp.executeBatch();
				prp.close();
			} catch (SQLException e) {
				LogEngine.log(this, e);
			}
		}
		return false;
	}
	
	/**
	 * Diese Methode speichert alle User in der LocDB indem es eine Prozedur
	 * aufruft.
	 * 
	 * @param usrRS
	 *            zu speicherndes ResultSet
	 * @return true Speichern hat geklappt
	 * @return false Speichern hat !geklappt
	 */
	public boolean push_users(ResultSet usrRS){
		if ((dbStatus >=3) && (usrRS != null)){
			try {
				synchronized (stmt) {
					while (usrRS.next()){
						StringBuffer pushMsgStmt = new StringBuffer();
						pushMsgStmt.append("CALL p_t_user_pushUsers(");
						pushMsgStmt.append(usrRS.getLong("userID") + ",");
						pushMsgStmt.append("'" + usrRS.getString("displayName") + "',");
						pushMsgStmt.append("'" + usrRS.getString("userName") + "')");

						stmt.addBatch(pushMsgStmt.toString());
					}
					stmt.executeBatch();
					return true;
				}
			} catch (SQLException e) {
				LogEngine.log(this, "Error while executing 'push_users': " + e.getMessage(), LogEngine.ERROR);
				return false;
			}
		}
		return false;
	}

	/**
	 * Diese Methode schreibt eine Collection von Nodes unter aufruf einer
	 * Prozedur in die Datenbank.
	 * 
	 * @param allNodesFormDBE
	 *            Collection aller �bergebnener Nodes
	 * @return true Speichern hat geklappt
	 * @return false Speichern hat !geklappt
	 */
	public synchronized boolean writeAllUsersToDB(Collection<Node> allNodesFormDBE){
		addnodez(allNodesFormDBE);
		if (dbStatus >= 3){
			if (allNodesFormDBE != null){
				StringBuffer saveUserStmt; 
				Iterator<Node> it = allNodesFormDBE.iterator();
				boolean alldone=true;
				while (it.hasNext()){
					Node tmpNode = it.next();
					saveUserStmt = new StringBuffer();
					saveUserStmt.append("CALL p_t_user_saveUsersAndNodes(");
					saveUserStmt.append(tmpNode.getUserID() + ",");
					saveUserStmt.append("'" + tmpNode.getAlias() + "',");
					saveUserStmt.append("'" + tmpNode.getUsername() + "',");
					saveUserStmt.append(tmpNode.getNodeID()+ ",");
					saveUserStmt.append("'" + tmpNode.getHostname() + "')");

					try {
						synchronized (stmt) {
							stmt.execute(saveUserStmt.toString());
							if (!ceReadyForWritingSettings){
								ceReadyForWritingSettings = true;
								Config.write();				
							}
						}
					} catch (SQLException e) {
						alldone=false;
						dbStatus = 0;
						LogEngine.log(LocalDBConnection.this,"Fehler beim eintragen in : "+ usrTbl + " or " + nodeTbl	 + " "+ e.getMessage(),LogEngine.ERROR);
						//hier falls w�hrend der schreibvorg�nge die verbind verloren geht.
					}
				}
				return alldone;
			}
			return true;
		}
		return false;
	}

	/**
	 * Diese Methode schreibt eine Collection von Groups unter Aufruf einer
	 * Prozedur in die DB.
	 * 
	 * @param groupsSet
	 *            Collection aller �bergebnener Groups
	 * @return true Speichern hat geklappt
	 * @return false Speichern hat !geklappt
	 */
	public synchronized boolean writeAllGroupsToDB(Collection<String> groupsSet){
		if (dbStatus >= 3) {
			if (groupsSet != null) {
				StringBuffer saveGroupsStmt;
				Iterator<String> it = groupsSet.iterator();
				while (it.hasNext()) {
					String tmpGrpStr = it.next();

					saveGroupsStmt = new StringBuffer();
					saveGroupsStmt.append("CALL p_t_groups_saveGroups(");
					saveGroupsStmt.append("'" + tmpGrpStr + "',");
					saveGroupsStmt.append(ChatEngine.getCE().getUserID() + ")");
					try {

						synchronized (stmt) {
							stmt.execute(saveGroupsStmt.toString());
						}
					} catch (SQLException e) {
						LogEngine.log(this, "dumping groups failed: " + e.getMessage(),LogEngine.ERROR);
						dbStatus = 0;
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Verarbeitet einzelne Nachrichten um sie zum schrieben in die DB
	 * vorzubereiten. Ruft 'writeMSG' mit aufgearbeiteten Daten auf.
	 * 
	 * @param m
	 *            �bernimmt eine einzelne Nachricht
	 * @return true Nachricht entgegen genommen und verarbeitet
	 * @return false Fehler bei der Verarbeitung
	 */
	public synchronized boolean writeMsgToDB(MSG m) {
		if (dbStatus >= 3) {
			long uid_empf�nger = -1;
			if (m.getEmpf�nger() != -1){

				uid_empf�nger = getUIDforNID(m.getEmpf�nger());
			}
			long uid_sender = getUIDforNID(m.getSender());
			Object data = m.getData();
			MSGCode code = m.getCode();
			long timestamp = m.getTimestamp();
			int id = m.getId();
			String group = m.getGroup();
			NachrichtenTyp typ = m.getTyp();
			return writeMSG(uid_empf�nger, uid_sender, data, code, timestamp, id, group, typ);
		}
		return false;
	}

	/**
	 * Sucht node aus lokalem Nodes-Set.
	 * 
	 * @param nid
	 *            ID des gesuchten Nodes
	 * @return gibt gefundenen Node zur�ck / null wenn nicht gefunden
	 */
	private Node getNode(long nid){
		synchronized (allnodes) {
			for (Node node : allnodes) {
				if (node.getNodeID()==nid) return node;
			}
		}
		return null;
	}

	/**
	 * Wandelt NodeID in entsprechende UserID um.
	 * 
	 * @param nid
	 *            gesuchte NodeID
	 * @return gefundene UserID oder '-1' wenn nicht gefunden
	 */
	private long getUIDforNID(long nid) {
		Node node = getNode(nid);
		if(node!=null)return node.getUserID();
		else return -1;
	}


	/**
	 * F�hrt Schreibvorgang f�r alle Nachrichten auf die LocDB unter Benutzung
	 * verschiedener Prozeduren durch.
	 * 
	 * @param uid_empf�nger
	 *            Zu schreibende uid des Empf�ngers der Nachricht
	 * @param uid_sender
	 *            Zu schreibende uid des senders der Nachricht
	 * @param data
	 *            Zu schreibender NachrichtenText der Nachricht
	 * @param code
	 *            Zu schreibende MsgCode der Nachricht
	 * @param timestamp
	 *            Zu schreibende timestamp der Nachricht
	 * @param id
	 *            Zu schreibende ID der Nachricht
	 * @param group
	 *            Zu schreibende Gruppenzugeh�rigkeit der Nachricht
	 * @param typ
	 *            Zu schreibender Typ der Nachricht
	 * @return true Speichern hat geklappt
	 * @return false Speichern hat !geklappt
	 */
	private synchronized boolean writeMSG(long uid_empf�nger, long uid_sender, Object data, MSGCode code, long timestamp, int id, String group, NachrichtenTyp typ) {
		PreparedStatement prpstmt=null;
		try {
			if (typ == NachrichtenTyp.GROUP) {
				// f�gt Nachricht hinzu
				prpstmt = con.prepareStatement("CALL p_t_messages_saveGroupMessage(?,?,?,?,?)");
				prpstmt.setLong(1, id);
				prpstmt.setLong(2, timestamp);
				prpstmt.setLong(3, uid_sender);
				prpstmt.setString(4, data.toString());
				prpstmt.setString(5, group);
			}
			else if (typ == NachrichtenTyp.PRIVATE) {
				//			// f�gt Nachricht hinzu
				prpstmt = con.prepareStatement("CALL p_t_messages_savePrivateMessage(?,?,?,?,?)");
				prpstmt.setLong(1, id);
				prpstmt.setLong(2, timestamp);
				prpstmt.setLong(3, uid_sender);
				prpstmt.setString(4, data.toString());
				prpstmt.setLong(5, uid_empf�nger);
			} else if (typ == NachrichtenTyp.SYSTEM) {
				prpstmt = con.prepareStatement("CALL p_t_messages_saveSystemMessage(?,?,?,?,?)");
				prpstmt.setLong(1, id);
				prpstmt.setLong(2, timestamp);
				prpstmt.setLong(3, uid_sender);
				if(uid_empf�nger>=0) {
					prpstmt.setLong(4,uid_empf�nger);
				} else {
					prpstmt.setNull(4, java.sql.Types.BIGINT);
				}
				prpstmt.setInt(5, code.ordinal());

			} else	return true;

			if (prpstmt!=null) {
				synchronized (stmt) {
					prpstmt.execute();

				}
			}
		} catch (Exception e) {
			dbStatus = 0;
			LogEngine.log(this, "Fehler beim schreiben von: " + ((prpstmt!=null)?prpstmt.toString():"") + " " + e.getMessage(), LogEngine.ERROR);
			return false;
			// hier falls w�hrend der schreibvorg�nge die verbindung verloren geht.
		}finally {
			try{prpstmt.close();} catch (Exception ignored) {}
		}
		return true;
	}

	/**
	 * Diese Methode schreibt die Routinginformationen in die Datenbank.
	 * 
	 * @param nIDZiel NodeID des Ziels
	 * @param nIDGateWay nodeID des zu nutzenden Gateways
	 * @return true Speichern hat geklappt
	 * @return false Speichern hat !geklappt
	 */
	public synchronized boolean writeRoutingTableToDB(Long nIDZiel, Long nIDGateWay) {
		PreparedStatement prp =null;
		try {
			if (dbStatus >= 3){
				prp = con.prepareStatement("update t_nodes set fk_t_nodes_nodeID=? where nodeID = ?");
				prp.setLong(1, nIDGateWay);
				prp.setLong(2, nIDZiel);
				prp.execute();
				return true;
			}
		} catch (SQLException e) {
			return false;
		}finally {
			try {prp.close();}catch(Exception ignored) {}
		}
		return true;
	}

	/**
	 * Diese Methode schreibt alle �bergebenen Einstellungen in die DB.
	 * 
	 * @param settings
	 *            �bergebene Einstellung vom Typ ConfigData
	 */
	public void writeAllSettingsToDB(final Properties settings) {
		if (ceReadyForWritingSettings){
			new Thread(new Runnable() {
				public void run() {
					if (dbStatus >= 3) {
						synchronized (stmt) {
							for (String key : settings.stringPropertyNames()) {
								StringBuffer saveSettingsStmt = new StringBuffer();
								if (!settings.getProperty(key).equals("")){
									saveSettingsStmt.append("CALL p_t_settings_saveSettings(");
									saveSettingsStmt.append("'" + key + "',");
									saveSettingsStmt.append(ChatEngine.getCE().getUserID() + ",");
									saveSettingsStmt.append("'"+ settings.getProperty(key) + "');");
									try {
										stmt.addBatch(saveSettingsStmt.toString());
									} catch (SQLException e) {
										LogEngine.log(this, "Error by 'addBatch: " + e.getMessage(), LogEngine.ERROR);
									}
								}
							}
							try {
								stmt.executeBatch();
							} catch (SQLException e) {
								LogEngine.log(LocalDBConnection.this,"Communication with LocDB failed while 'writeAllSettingsToDB': " + e.getMessage(), LogEngine.ERROR);
								dbStatus = 0;
							}
						}
					}
				}
			}).start();
		}
	}

	/**
	 * L�scht alle Nachrichten aus der LocDB.
	 * 
	 * @return true L�schen hat geklappt
	 * @return false L�schen hat fehler verursacht
	 */
	public boolean deleteAllMsgs () {
		try {
			synchronized (stmt) {
				return stmt.execute("DELETE FROM " + msgTbl);
			}
		} catch (SQLException e) {
			LogEngine.log(this, "Error while delMsgs: " + e.getMessage(), LogEngine.ERROR);
			dbStatus = 0;
			return false;
		}
	}

	/**
	 * Diese Methode erm�glicht die Suche in der History auf der locDB.
	 * 
	 * @param userID
	 *            gesuchte UserID
	 * @param alias
	 *            gesuchter Alias
	 * @param groupName
	 *            gesuchter Gruppenname
	 * @param begin
	 *            gibt den TimeStamp an ab dem alle Nachrichten gesucht werden
	 *            sollen
	 * @param end
	 *            gibt den TimeStamp an bis zu dem alle Nachrichten gesucht
	 *            werden sollen
	 * @param msgTxt
	 *            gibt den gesuchten Nachrichtentext an
	 * @return Resultset mit allen Treffern der suche oder 'null' wenn keine
	 *          treffer oder Fehler
	 */
	public ResultSet searchInHistory (String userID, String alias, String groupName, long begin, long end, String msgTxt){
		if (dbStatus >= 3){
				StringBuilder prepState = new StringBuilder();

				prepState.append						("SELECT * from v_searchInHistory WHERE ");
				if(userID!=null) prepState.append		("(userID_Sender LIKE ? OR userID_Recipient LIKE ?) AND ");
				if(alias != null) prepState.append		("(sender LIKE ? OR recipient LIKE ?) AND");
				if(groupName!=null) prepState.append	("`group` LIKE ? AND ");
				prepState.append						("(time BETWEEN ? AND ?) AND message LIKE ? AND length(message)>0 ORDER BY time");


				PreparedStatement prp=null;
				ResultSet rs = null;
				try {
					CachedRowSet tmp = new CachedRowSetImpl();
					prp = con.prepareStatement(prepState.toString());
					int part=1;
					if(userID!=null) {
						prp.setString(part++, userID);
						prp.setString(part++, userID);
					}
					if(alias != null){
						prp.setString(part++, alias);
						prp.setString(part++, alias);
					}
					if(groupName!=null) {
						prp.setString(part++, groupName);
					}
					prp.setLong(part++, begin);
					prp.setLong(part++, end);
					prp.setString(part++, msgTxt);
					rs=prp.executeQuery();
					tmp.populate(rs);
					return tmp;
				} catch (SQLException e) {
					LogEngine.log(this, "Error while executing 'searchInHistStmt' PreparedStatment: " + e.getMessage(), LogEngine.ERROR);
					dbStatus = 0;
					return null;
				}
				finally {
					try{rs.close();  }catch(Exception ignored){}
					try{prp.close();}catch(Exception ignored){}
				}
			}
		return null;
	}

	/**
	 * Diese Methode schlie�t alle Verbindungen. 
	 */
	public void shutdownLocDB() {
		//alle verbindungen trennen
		try {
			if(stmt!=null){
				stmt.close();
				LogEngine.log(LocalDBConnection.this,"stmt of LocDBServers closed",LogEngine.INFO);
			}
		} catch (SQLException e) {
			LogEngine.log(this, e);
		}
		try {
			if(con!=null){
				con.close();
				LogEngine.log(LocalDBConnection.this,"Connection to LocDBServer closed",LogEngine.INFO);
			}


		} catch (SQLException e) {
			LogEngine.log(this, e);
		}
	}
}
