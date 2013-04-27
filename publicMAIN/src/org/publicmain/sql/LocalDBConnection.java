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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JTextPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.Config;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.NachrichtenTyp;
import org.publicmain.common.Node;
import org.publicmain.gui.GUI;
import org.publicmain.nodeengine.NodeEngine;

import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.log.Log;

/**
 * Die Klasse DBConnection stellt die Verbindung zu dem Lokalen DB-Server her.
 * Sie legt weiterhin alle zwingend notwendigen Datenbanken(1) und Tabellen an.
 */
public class LocalDBConnection {

	private Connection con;
	private Statement stmt;
	private PreparedStatement checkoutHistoryPrpStmt;
	//private ResultSet rs;
	private String url;
	private String dbName;
	private String user;
	private String passwd;
	
	//Tabellen der Loc DB
	private String usrTbl;
	private String nodeTbl;
	private Calendar cal;
	private SimpleDateFormat splDateFormt;
	
	
	//--------neue Sachen------------
	private BlockingQueue<MSG> locDBInbox;
	private int dbStatus;					// 0=nicht bereit	1=con besteht	2=DB und Tabellen angelegt 3=mit db verbunden
	private int maxVersuche;
	private long warteZeitInSec;
	private Thread connectToDBThread;
	private int dbVersion;
	private DatabaseEngine databaseEngine;
	//--------neue Sachen ende------------
	
	// verbindungssachen
	private static LocalDBConnection me;
	
	private LocalDBConnection(DatabaseEngine databaseEngine) {
		this.url 					= "jdbc:mysql://localhost:"+Config.getConfig().getLocalDBPort()+"/";
		this.user 					= Config.getConfig().getLocalDBUser();
		this.passwd 				= Config.getConfig().getLocalDBPw();
		this.dbName 				= Config.getConfig().getLocalDBDatabasename();
		this.usrTbl					= "t_user";
		this.nodeTbl				= "t_nodes";
		this.cal					= Calendar.getInstance();
		this.splDateFormt			= new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		this.databaseEngine			= databaseEngine;

		//--------neue Sachen------------
		this.locDBInbox = new LinkedBlockingQueue<MSG>();;
		this.dbStatus	= 0;				// 0=nicht bereit	1=con besteht	2=Tables wurden angelegt 3=use db_publicmain erfolgreich / hardWorkingThread läuft
		this.maxVersuche = 5;
		this.warteZeitInSec = 10;
//		this.writtenStandardUser = false;
		this.connectToDBThread = new Thread(new firstConnectToLocDBServerBot());
		this.dbVersion = 3;
		//--------neue Sachen ende------------
		connectToDBThread.start();
	}
	public static LocalDBConnection getDBConnection(DatabaseEngine databaseEngine) {
		if (me == null) {
			me = new LocalDBConnection(databaseEngine);
		}
		return me;
	}

	private class firstConnectToLocDBServerBot implements Runnable { // wird nur vom Construktor aufgerufen

		public void run() {
			int versuche = 0;
			while ((versuche < maxVersuche) && (dbStatus == 0)) { // hatte da erst con==null drin aber das ist ein problem beim reconnecten unten.
				if(connectToLocDBServer()){
					versuche = 0;
				} else {
					versuche ++;
				}
			}
			if(dbStatus >= 1){
				try {
					stmt.executeQuery("use " + dbName);
					//TODO: Tabelle mit version hinzufügen und prüfen
					if(Config.getConfig().getLocalDBVersion() != dbVersion){
						createDbAndTables();
						run();
					} else {
						dbStatus = 3;
						databaseEngine.go();
						LogEngine.log(this, "DB-Status: " + dbStatus, LogEngine.INFO);
					}
				} catch (SQLException e1) {
					createDbAndTables();
					run();
				}
			} else {
				LogEngine.log(this,	versuche +". zum DB-Verbindungsaufbau fehlgeschlagen -> für zugriff auf locDB sorgen und neu starten! ", LogEngine.ERROR);
			}
		}
	}
	
	private synchronized boolean connectToLocDBServer(){
		try {
			con = DriverManager.getConnection(url, user, passwd);
			stmt = con.createStatement();
			LogEngine.log(this, "DB-ServerVerbindung hergestellt", LogEngine.INFO);
			dbStatus = 1;
			return true;
		} catch (SQLException e) {
			try {
				Thread.sleep(warteZeitInSec * 1000);
			} catch (InterruptedException e1) {
				LogEngine.log(this,"Fehler beim Warten: " + e1.getMessage(),LogEngine.ERROR);
			}
			dbStatus = 0;
		}
		
		return false;
	}
	
	public boolean getStatus(){
		if (dbStatus==3){
			return true;
		} else {
			return false;
		}
	}
	
	public void reconnectToLocDBServer(){
		new Thread(new Runnable() {
			public void run() {
				int reconnectVersuche = 1;
				while (dbStatus < 3 && maxVersuche >= reconnectVersuche){
					LogEngine.log(this, "Lost connetion to locDBSvr. Try to reconnect in 10 seconds " + reconnectVersuche + "/" + maxVersuche +".", LogEngine.ERROR);
					try {
						Thread.sleep(warteZeitInSec * 1000);
					} catch (InterruptedException e1) {
					}
					if (connectToLocDBServer()){
						try {
							stmt.executeQuery("use " + dbName);
							dbStatus = 3;
							databaseEngine.go();
						} catch (SQLException e) {
						}
					}
					reconnectVersuche++;
				}
				LogEngine.log(this, "Lost connection to locDBSvr conclusively - will not try again.",LogEngine.ERROR);
			}
		}).start();
		
	}
	
	private void createDbAndTables (){	// wird nur vom Construktor aufgerufen
		String read=null;
		try (BufferedReader in = new BufferedReader(new FileReader(new File(getClass().getResource("create_db.sql").toURI())))){
			while((read = in.readLine()) != null) {
				while (!read.endsWith(";") && !read.endsWith("--")){
					read = read + in.readLine();
				}
				stmt.execute(read);
			}
			//TODO: durch änderungen in der db muss hier noch angepasst werden.
			stmt.addBatch("DROP procedure IF EXISTS `db_publicmain`.`p_t_messages_savePrivateMessage`;");
			stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_messages_savePrivateMessage` (IN newMsgID INT(11), IN newTimestmp BIGINT(20), IN newFk_t_users_userID_sender BIGINT(20), IN newDisplayName VARCHAR(45), IN newTxt VARCHAR(200), IN newFk_t_users_userID_empfaenger BIGINT(20)) BEGIN INSERT IGNORE INTO t_messages (msgID,timestmp,fk_t_users_userID_sender, displayName,txt , fk_t_users_userID_empfaenger) VALUES (newMsgID, newTimestmp, newFk_t_users_userID_sender, newDisplayName,  newTxt, newFk_t_users_userID_empfaenger); END;");
			stmt.addBatch("DROP procedure IF EXISTS `db_publicmain`.`p_t_messages_saveGroupMessage`;");
			stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_messages_saveGroupMessage` (IN newMsgID INT(11), IN newTimestmp BIGINT(20), IN newFk_t_users_userID_sender BIGINT(20),IN newDisplayName VARCHAR(45), IN newTxt VARCHAR(200), IN newFk_t_groups_groupName VARCHAR(20)) BEGIN INSERT IGNORE INTO t_messages (msgID,timestmp, fk_t_users_userID_sender, displayName, txt, fk_t_groups_groupName) VALUES (newMsgID, newTimestmp, newFk_t_users_userID_sender, newDisplayName, newTxt, newFk_t_groups_groupName); END;");
			stmt.addBatch("DROP procedure IF EXISTS `db_publicmain`.`p_t_messages_saveSystemMessage`;");
			stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_messages_saveSystemMessage` (IN newMsgID INT(11), IN newTimestmp BIGINT(20), IN newFk_t_users_userID_sender BIGINT(20), IN newDisplayName VARCHAR(45), IN newFk_t_users_userID_empfaenger BIGINT(20),IN newFk_t_msgType_ID INT) BEGIN INSERT IGNORE INTO t_messages (msgID,timestmp,fk_t_users_userID_sender, displayName, fk_t_users_userID_empfaenger,fk_t_msgType_ID) VALUES (newMsgID, newTimestmp, newFk_t_users_userID_sender, newDisplayName, newFk_t_users_userID_empfaenger, newFk_t_msgType_ID); END;");
			
			stmt.addBatch("DROP procedure IF EXISTS `db_publicmain`.`p_t_groups_saveGroups`;");
			stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_groups_saveGroups` (IN newGroupName VARCHAR(20),IN newFk_t_users_userID BIGINT(20)) BEGIN insert into t_groups (groupName, fk_t_users_userID) values (newGroupName,newFk_t_users_userID) ON DUPLICATE KEY UPDATE fk_t_users_userID=VALUES(fk_t_users_userID); END;");
			
			stmt.addBatch("DROP procedure IF EXISTS `db_publicmain`.`p_t_user_saveUsers`;");
			stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_user_saveUsers` (IN newUserID BIGINT(20),IN newDisplayName VARCHAR(45),IN newUserName VARCHAR(45)) BEGIN insert into t_users (userID, displayName, userName) values (newUserID,newDisplayName,newUserName) ON DUPLICATE KEY UPDATE displayName=VALUES(displayName),userName=VALUES(userName); END;");
			
			stmt.addBatch("DROP procedure IF EXISTS `db_publicmain`.`p_t_msgType`;");
			stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_msgType`(IN newMsgTypeID INT,IN newName VARCHAR(45),IN newDescription VARCHAR(45)) BEGIN INSERT INTO t_msgType (msgTypeID, name, description) VALUES (newMsgTypeID,newName,newDescription) ON DUPLICATE KEY UPDATE name=VALUES(name),description=VALUES(description); END;");
			
			//Hier wird die Tabelle t_msgType automatisch befüllt!
			
			
			for (MSGCode c : MSGCode.values()){
				stmt.addBatch("CALL p_t_msgType(" + c.ordinal() + ",'" + c.name() + "','" +  c.getDescription() + "')");
			}
			
			stmt.executeBatch();
			dbStatus = 2;
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
	}
	
		
	public synchronized boolean writeAllUsersToDB(Collection<Node> allnodes){
		if (dbStatus >= 3){
			if (allnodes != null){
				StringBuffer saveUserStmt; 
				Iterator<Node> it = allnodes.iterator();
				boolean alldone=true;
				while (it.hasNext()){
					Node tmpNode = (Node) it.next();
					saveUserStmt = new StringBuffer();
					saveUserStmt.append("CALL p_t_user_saveUsers(");
					saveUserStmt.append(tmpNode.getUserID() + ",");
					saveUserStmt.append("'" + tmpNode.getAlias() + "',");
					saveUserStmt.append("'" + tmpNode.getUsername() + "')");
					try {
						synchronized (stmt) {
							stmt.execute(saveUserStmt.toString());
						}
					} catch (SQLException e) {
						alldone=false;
						dbStatus = 0;
						LogEngine.log(LocalDBConnection.this,"Fehler beim eintragen in : "+ usrTbl + " or " + nodeTbl	 + " "+ e.getMessage(),LogEngine.ERROR);
						//hier falls während der schreibvorgänge die verbind verloren geht.
					}
				}
				return alldone;
			}
			return true;
		}
		return false;
	}
	
	public ResultSet pull_msgs(){
		return null;
	}
	
	public ResultSet pull_users(){
		return null;
	}
	
	public ResultSet pull_settings(){
		return null;
	}
	
	public boolean push_msgs(ResultSet backup){
		return false;
	}
	
	public boolean push_users(ResultSet backup){
		return false;
	}
	//Settings werden ins beziehungsweise aus dem ConfigObjekt geladen nicht hier	
	
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
	
	
	public synchronized boolean writeMsgToDB(MSG m) {
		if (dbStatus >= 3) {
//			MSG m = locDBInbox.poll();
			long uid_empfänger = NodeEngine.getNE().getUIDforNID(m.getEmpfänger());
			long uid_sender = NodeEngine.getNE().getUIDforNID(m.getSender());
			Object data = m.getData();
			MSGCode code = m.getCode();
			long timestamp = m.getTimestamp();
			int id = m.getId();
			String group = m.getGroup();
			NachrichtenTyp typ = m.getTyp();
			String senderAlias = "to implement";
			return writeMSG(uid_empfänger, uid_sender, data, code, timestamp, id, group, typ, senderAlias);
		}
		return false;
	}
	
	
	private boolean writeMSG(long uid_empfänger, long uid_sender, Object data, MSGCode code, long timestamp, int id, String group, NachrichtenTyp typ, String senderAlias) {
		StringBuffer saveMsgStmt = new StringBuffer();
		if (typ == NachrichtenTyp.GROUP) {
			// fügt Nachicht hinzu
			saveMsgStmt.append("CALL p_t_messages_saveGroupMessage(");
			saveMsgStmt.append(id + ",");
			saveMsgStmt.append(timestamp + ",");
			saveMsgStmt.append(uid_sender + ",");
			saveMsgStmt.append("'" + senderAlias + "',");
			saveMsgStmt.append("'" + data + "',");
			saveMsgStmt.append("'" + group + "')");
		}
		else if (typ == NachrichtenTyp.PRIVATE) {
			// fügt Nachicht hinzu
			saveMsgStmt.append("CALL p_t_messages_savePrivateMessage(");
			saveMsgStmt.append(id + ",");
			saveMsgStmt.append(timestamp + ",");
			saveMsgStmt.append(uid_sender + ",");
			saveMsgStmt.append("'" + senderAlias + "',");
			saveMsgStmt.append("'" + data + "',");
			saveMsgStmt.append(uid_empfänger + ")");
		} else if (typ == NachrichtenTyp.SYSTEM) {
			String toWriteUID_empfänger = String.valueOf(uid_empfänger);
			if (uid_empfänger == -1) {
				toWriteUID_empfänger = "null";
			}
			// fügt Nachicht hinzu
			saveMsgStmt.append("CALL p_t_messages_saveSystemMessage(");
			saveMsgStmt.append(id + ",");
			saveMsgStmt.append(timestamp + ",");
			saveMsgStmt.append(uid_sender + ",");
			saveMsgStmt.append("'" + senderAlias + "',");
			saveMsgStmt.append(toWriteUID_empfänger + ",");
			saveMsgStmt.append(code.ordinal() + ")");
		} else
			return true;

		if (saveMsgStmt.length() > 0) { // && saveGrpStmt.length() > 0){
			synchronized (stmt) {
				try {
					LogEngine.log(LocalDBConnection.this, "Nachicht " + id + " mit p_t_groups_saveGroups aufgerufen", LogEngine.INFO);
					stmt.execute(saveMsgStmt.toString());
					return true;
				} catch (Exception e) {
					dbStatus = 0;
					LogEngine.log(this, "Fehler beim schreiben von: " + saveMsgStmt.toString() + " " + e.getMessage(), LogEngine.ERROR);
					return false;
					// hier falls während der schreibvorgänge die verbind
					// verloren geht.
				}
			}
		}
		return true;
	}

	public synchronized void writeRoutingTable_to_t_nodes(Map routingTable){
	}
	
	public void deleteAllMsgs () {
	}
	
	public void searchInHistory (JTextPane historyContentTxt, String chosenNTyp, String chosenAliasOrGrpName, Date fromDateTime, Date toDateTime, HTMLEditorKit htmlKit, HTMLDocument htmlDoc){
//		if (isDBConnected){
//			try {
//				String tmpStmtStr = (
//						"SELECT * " +
//						"FROM " + chatLogTbl + " " +
//						"WHERE timestamp BETWEEN '" + fromDateTime.getTime() + "' AND '" + toDateTime.getTime() + "'");
//				if (!chosenNTyp.equals("ALL")){
//					tmpStmtStr = tmpStmtStr + " AND typ = '" + chosenNTyp + "'";
//				}
//				if (!chosenAliasOrGrpName.equals("...type in here.")){
//					//TODO: hier die übersetung von nr in string einbinden
////					tmpStmtStr = tmpStmtStr + " AND ";
//				}
//				Statement searchStmt = con.createStatement();
//				ResultSet rs = searchStmt.executeQuery(tmpStmtStr);
//				
//				
//				
//				//TODO bei mehrfachen aufruf muss das HTML-Doc wieder geleert werden!
//				historyContentTxt.setText("");
//				htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(), "<table>" + "<th><b>ID</b></th>"
//						+ "<th><b>msgID</th>" + "<th><b>Timestamp</th>"
//						+ "<th><b>Sender</th>" + "<th><b>Empfänger</th>" + "<th><b>Typ</th>"
//						+ "<th><b>Gruppe</th>" + "<th><b>Daten</th>", 0, 0, null);
//				while (rs.next()) {
//					
//					cal.setTimeInMillis(rs.getLong("timestamp"));
//					htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(),
//							"<tr>" + "<td>" + rs.getInt("id") + "<td>"
//									+ rs.getInt("msgID") + "<td>"
//									+ splDateFormt.format(cal.getTime())+ "<td>"
//									+ rs.getLong("sender") + "<td>"
//									+ rs.getLong("empfaenger") + "<td>"
//									+ rs.getString("typ") + "<td>"
//									+ rs.getString("grp") + "<td>"
//									+ rs.getString("data"), 0, 0, null);
//					htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(), "</tr>", 0, 0,
//							null);
//				}
//				htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(), "</table>", 0, 0,
//						null);
//			} catch (BadLocationException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			} catch (SQLException e) {
//				e.printStackTrace();
//			} 
//		}else {
//			JOptionPane.showMessageDialog(null,
//	                "Für diese Operation ist eine Verbindung zum lokalen DB-Server zwingend notwendig",
//	                "Hinweis",
//	                JOptionPane.INFORMATION_MESSAGE);
//			askForConnectionRetry = true;
//			if (connectToLocDBServer()) {
//				isDBConnected = true;
//				searchInHistory(historyContentTxt, chosenNTyp, chosenAliasOrGrpName, fromDateTime, toDateTime,  htmlKit, htmlDoc);
//				
//			} else {
//				//System.out.println("Erneuter versuch der Verbindungsherstellung erfolglos!");
//			}
//		}
	}
	
	public void shutdownLocDB() {
		// TODO alle verbindungen trennen
		try {
			if(stmt!=null){
				stmt.close();
				LogEngine.log(LocalDBConnection.this,"stmt des LocDBServers closed",LogEngine.INFO);
			}
		} catch (SQLException e) {
			LogEngine.log(this, e);
		}
		try {
			if(con!=null){
				con.close();
				LogEngine.log(LocalDBConnection.this,"Connection to LocDBServer closed",LogEngine.INFO);
			}
			
			// TODO What else?
		} catch (SQLException e) {
			LogEngine.log(this, e);
		}
	}

	public void writeRoutingTable_to_t_nodes(Long key, Long value) {
		// TODO Auto-generated method stub
		
	}
}
