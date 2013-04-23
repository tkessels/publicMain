package org.publicmain.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
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
	private String msgLogTbl;
	private String msgTbl;
	private String usrTbl;
	private String nodeTbl;
	private String groupTbl;
	private String configTbl;
	private String eventTypeTbl;
	private String routingOverviewTbl;
	private Calendar cal;
	private SimpleDateFormat splDateFormt;
	
	
	//--------neue Sachen------------
	private BlockingQueue<MSG> locDBInbox;
	private int dbStatus;					// 0=nicht bereit	1=con besteht	2=DB und Tabellen angelegt 3=mit db verbunden
	private int maxVersuche;
	private long warteZeitInSec;
	private Collection<Node> allNodes;
	private HashSet<String> groupsSet;
	private boolean writtenStandardUser;
	private Thread connectToDBThread;
	private Thread hardWorkingThread;
	private int dbVersion;
	//--------neue Sachen ende------------
	private int allNodes_hash ;
	
	// verbindungssachen
	private static LocalDBConnection me;
	
	private LocalDBConnection() {
		this.url 					= "jdbc:mysql://localhost:"+Config.getConfig().getLocalDBPort()+"/";
		this.user 					= Config.getConfig().getLocalDBUser();
		this.passwd 				= Config.getConfig().getLocalDBPw();
		this.dbName 				= Config.getConfig().getLocalDBDatabasename();
		this.msgLogTbl				= "t_messages";
		this.msgTbl					= "t_msg";
		this.usrTbl					= "t_user";
		this.nodeTbl				= "t_nodes";
		this.groupTbl				= "t_group";
		this.configTbl				= "t_config";
		this.eventTypeTbl			= "t_eventType";
		this.routingOverviewTbl		= "t_routingOverView";
		this.cal					= Calendar.getInstance();
		this.splDateFormt			= new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		this.allNodes				= Collections.synchronizedSet(new HashSet<Node>());
		this.allNodes_hash 			= allNodes.hashCode();

		//--------neue Sachen------------
		this.locDBInbox = new LinkedBlockingQueue<MSG>();;
		this.dbStatus	= 0;				// 0=nicht bereit	1=con besteht	2=Tables wurden angelegt 3=use db_publicmain erfolgreich / hardWorkingThread l�uft
		this.maxVersuche = 5;
		this.warteZeitInSec = 10;
		this.writtenStandardUser = false;
		this.connectToDBThread = new Thread(new connectToLocDBServer());
		this.hardWorkingThread = new Thread(new writeEverythingToLocDB());
		this.dbVersion = 89;
		//--------neue Sachen ende------------
		connectToDBThread.start();
	}
	public static LocalDBConnection getDBConnection() {
		if (me == null) {
			me = new LocalDBConnection();
		}
		return me;
	}

	private class connectToLocDBServer implements Runnable { // wird nur vom Construktor aufgerufen

		public void run() {
			int versuche = 0;
			while ((versuche < maxVersuche) && (dbStatus == 0)) { // hatte da erst con==null drin aber das ist ein problem beim reconnecten unten.
				try {
					con = DriverManager.getConnection(url, user, passwd);
					stmt = con.createStatement();
					LogEngine.log(this, "DB-ServerVerbindung hergestellt", LogEngine.INFO);
					dbStatus = 1;
					versuche = 0;
				} catch (SQLException e) {
					LogEngine.log(this,	"DB-Verbindungsaufbau fehlgeschlagen: "+ e.getMessage(), LogEngine.ERROR);
					try {
						Thread.sleep(warteZeitInSec * 1000);
					} catch (InterruptedException e1) {
						LogEngine.log(this,"Fehler beim Warten: " + e1.getMessage(),LogEngine.ERROR);
					}
					versuche++;
					dbStatus = 0;
				}
			}
			if(dbStatus >= 1){
				try {
					stmt.executeQuery("use " + dbName);
					//TODO: Tabelle mit version hinzuf�gen und pr�fen
					if(Config.getConfig().getLocalDBVersion() != dbVersion){
						createDbAndTables();
						run();
					} else {
						dbStatus = 3;
						hardWorkingThread.start();
						LogEngine.log(this, "DB-Status: " + dbStatus, LogEngine.INFO);
					}
				} catch (SQLException e1) {
					createDbAndTables();
					run();
				}
			}
		}
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
			//TODO: durch �nderungen in der db muss hier noch angepasst werden.
			stmt.addBatch("DROP procedure IF EXISTS `db_publicmain`.`p_t_messages_savePrivateMessage`;");
			stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_messages_savePrivateMessage` (IN newMsgID INT(11), IN newTimestmp BIGINT(20), IN newFk_t_users_userID_sender BIGINT(20), IN newFk_t_users_userID_empfaenger BIGINT(20), IN newTxt VARCHAR(200)) BEGIN INSERT IGNORE INTO t_messages (msgID,timestmp,fk_t_users_userID_sender,fk_t_users_userID_empfaenger,txt) VALUES (newMsgID, newTimestmp, newFk_t_users_userID_sender, newFk_t_users_userID_empfaenger, newTxt); END;");
			stmt.addBatch("DROP procedure IF EXISTS `db_publicmain`.`p_t_messages_saveGroupMessage`;");
			stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_messages_saveGroupMessage` (IN newMsgID INT(11), IN newTimestmp BIGINT(20), IN newFk_t_users_userID_sender BIGINT(20),IN newFk_t_groups_groupName VARCHAR(20), IN newTxt VARCHAR(200)) BEGIN INSERT IGNORE INTO t_messages (msgID,timestmp,fk_t_users_userID_sender,fk_t_groups_groupName,txt) VALUES (newMsgID, newTimestmp, newFk_t_users_userID_sender, newFk_t_groups_groupName, newTxt); END;");
			stmt.addBatch("DROP procedure IF EXISTS `db_publicmain`.`p_t_messages_saveSystemMessage`;");
			stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_messages_saveSystemMessage` (IN newMsgID INT(11), IN newTimestmp BIGINT(20), IN newFk_t_users_userID_sender BIGINT(20), IN newFk_t_users_userID_empfaenger BIGINT(20),IN newFk_t_msgType_ID INT, IN newTxt VARCHAR(200)) BEGIN INSERT IGNORE INTO t_messages (msgID,timestmp,fk_t_users_userID_sender,fk_t_users_userID_empfaenger,fk_t_msgType_ID,txt) VALUES (newMsgID, newTimestmp, newFk_t_users_userID_sender, newFk_t_users_userID_empfaenger, newFk_t_msgType_ID, newTxt); END;");
			stmt.addBatch("DROP procedure IF EXISTS `db_publicmain`.`p_t_groups_saveGroups`;");
			stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_groups_saveGroups` (IN newGroupName VARCHAR(20),IN newFk_t_users_userID BIGINT(20)) BEGIN insert into t_groups (groupName, fk_t_users_userID) values (newGroupName,newFk_t_users_userID) ON DUPLICATE KEY UPDATE fk_t_users_userID=VALUES(fk_t_users_userID); END;");
			
			stmt.addBatch("DROP procedure IF EXISTS `db_publicmain`.`p_t_user_saveUsers`;");
			stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_user_saveUsers` (IN newUserID BIGINT(20),IN newDisplayName VARCHAR(45),IN newUserName VARCHAR(45)) BEGIN insert into t_users (userID, displayName, userName) values (newUserID,newDisplayName,newUserName) ON DUPLICATE KEY UPDATE displayName=VALUES(displayName),userName=VALUES(userName); END;");
			
			stmt.addBatch("DROP procedure IF EXISTS `db_publicmain`.`p_t_msgType`;");
			stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_msgType`(IN newMsgTypeID INT,IN newName VARCHAR(45),IN newDescription VARCHAR(45)) BEGIN INSERT INTO t_msgType (msgTypeID, name, description) VALUES (newMsgTypeID,newName,newDescription) ON DUPLICATE KEY UPDATE name=VALUES(name),description=VALUES(description); END;");
			
			//Hier wird die Tabelle t_msgType automatisch bef�llt!
			
			
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
	
	public synchronized void writeAllUsersToLocDB(Collection<Node> newAllNodes){
		allNodes.addAll(newAllNodes);
	}
	
	private void executeWritingUsers(){
		if(!writtenStandardUser){
			try {
				stmt.execute("CALL p_t_user_saveUsers(" + ChatEngine.getCE().getUserID() + ",'" + ChatEngine.getCE().getAlias() + "','" + NodeEngine.getNE().getNode(NodeEngine.getNE().getNodeID()).getUsername() + "')");
				} catch (SQLException e) {
				LogEngine.log(this, "Fehler beim Schreiben der StandardUser in 'executeWritingUsers" + e.getMessage(), LogEngine.ERROR);
			}
			writtenStandardUser = true;
		}
		
		
		if (allNodes_hash != allNodes.hashCode()){
			Collection<Node> tmpAllNodes = allNodes;
			StringBuffer saveUserStmt; 
			Iterator<Node> it = tmpAllNodes.iterator();
			while (it.hasNext()){
				Node tmpNode = (Node) it.next();
				saveUserStmt = new StringBuffer();
				saveUserStmt.append("CALL p_t_user_saveUsers(");
				saveUserStmt.append(tmpNode.getUserID() + ",");
				saveUserStmt.append("'" + tmpNode.getAlias() + "',");
				saveUserStmt.append("'" + tmpNode.getUsername() + "')");
				try {
					stmt.execute(saveUserStmt.toString());
					LogEngine.log(LocalDBConnection.this, "user" + tmpNode.getUserID() + " in DB-Tabelle " + usrTbl + " eingetragen.", LogEngine.INFO);
				} catch (SQLException e) {
					LogEngine.log(LocalDBConnection.this,"Fehler beim eintragen in : "+ usrTbl + " or " + nodeTbl	 + " "+ e.getMessage(),LogEngine.ERROR);
					//hier falls w�hrend der schreibvorg�nge die verbind verloren geht.
				}
			}
			allNodes_hash = allNodes.hashCode();
		} else {
		}
	}
	
	public synchronized void writeAllGroupsToLocDB(HashSet<String> newGroupsSet){
		groupsSet = newGroupsSet;
	}
	
	private void executeWriteAllGroupsToLocDB(){
		if (groupsSet != null){
			HashSet<String> tmpGroupsSet = groupsSet;
			groupsSet.clear();
			StringBuffer saveGroupsStmt; 
			Iterator<String> it = tmpGroupsSet.iterator();
			while(it.hasNext()){
				String tmpGrpStr = it.next();
				saveGroupsStmt = new StringBuffer();
				saveGroupsStmt.append("p_t_groups_saveGroups(");
				saveGroupsStmt.append("'" + tmpGrpStr + "',");
				saveGroupsStmt.append(ChatEngine.getCE().getUserID()+ ")");
				try {
					
					stmt.execute(saveGroupsStmt.toString());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public synchronized void writeMsgToLocDB (MSG m){
		//Hier wird message in ne Blocking queue geschrieben
		locDBInbox.add(m);
	}
	
	private void executeWriteMsgToDB() {
		while (!locDBInbox.isEmpty() && dbStatus >= 3) {
			StringBuffer saveMsgStmt = new StringBuffer();
			StringBuffer saveGrpStmt = null;
			MSG m = locDBInbox.poll();
			long uid_empf�nger = NodeEngine.getNE().getUIDforNID(m.getEmpf�nger());
			long uid_sender = NodeEngine.getNE().getUIDforNID(m.getSender());
			if (m.getTyp() == NachrichtenTyp.GROUP) {
				// f�gt Gruppe hinzu
				saveGrpStmt = new StringBuffer();
				saveGrpStmt.append("CALL p_t_groups_saveGroups(");
				saveGrpStmt.append("'" + m.getGroup() + "',");
				saveGrpStmt.append(ChatEngine.getCE().getUserID() + ")");
				// f�gt Nachicht hinzu
				saveMsgStmt.append("CALL p_t_messages_saveGroupMessage(");
				saveMsgStmt.append(m.getId() + ",");
				saveMsgStmt.append(m.getTimestamp() + ",");
				saveMsgStmt.append(uid_sender + ",");
				saveMsgStmt.append("'" + m.getGroup() + "',");
				saveMsgStmt.append("'" + m.getData() + "')");
			}
			if (m.getTyp() == NachrichtenTyp.PRIVATE) {
				// f�gt Nachicht hinzu
				saveMsgStmt.append("CALL p_t_messages_savePrivateMessage(");
				saveMsgStmt.append(m.getId() + ",");
				saveMsgStmt.append(m.getTimestamp() + ",");
				saveMsgStmt.append(uid_sender + ",");
				saveMsgStmt.append(uid_empf�nger+ ",");
				saveMsgStmt.append("'" + m.getData() + "')");
			}
			if (m.getTyp() == NachrichtenTyp.SYSTEM) {
				String toWriteUID_empf�nger = String.valueOf(uid_empf�nger);
				if(uid_empf�nger == -1){
					toWriteUID_empf�nger = "null";
				}
				// f�gt Nachicht hinzu
				saveMsgStmt.append("CALL p_t_messages_saveSystemMessage(");
				saveMsgStmt.append(m.getId() + ",");
				saveMsgStmt.append(m.getTimestamp() + ",");
				saveMsgStmt.append(uid_sender + ",");
				saveMsgStmt.append(toWriteUID_empf�nger + ",");
				saveMsgStmt.append("'" + m.getCode().ordinal() + "',");
				saveMsgStmt.append("'" + m.getData() + "')");
			}
			if (saveMsgStmt.length()> 0){ //&& saveGrpStmt.length() > 0){
				try {
					if (saveGrpStmt != null){
						stmt.execute(saveGrpStmt.toString());
						LogEngine.log(LocalDBConnection.this, "Nachicht " + m.getId() + " mit p_t_groups_saveGroups aufgerufen", LogEngine.INFO);
					}
					System.out.println(saveMsgStmt.toString());
					stmt.execute(saveMsgStmt.toString());
//					LogEngine.log(LocalDBConnection.this, "Nachicht " + m.getId() + " mit p_t_messages_saveMessage aufgerufen", LogEngine.INFO);
					locDBInbox.clear();
				} catch (Exception e) {
					LogEngine.log(LocalDBConnection.this,"Fehler beim aufruf von  : p_t_groups_saveGroups oder p_t_messages_saveMessage "+ e.getMessage(),LogEngine.ERROR);
					locDBInbox.add(m);	//TODO: Schreibt nachicht, die rausgenommen wurde, bei der es einen Fehler beim abspeichern gab wieder zur�ck in Queue! Wie schafft man das, dass es an die richtige stelle geschrieben wird
					dbStatus = 0;
					//hier falls w�hrend der schreibvorg�nge die verbind verloren geht.
				}
			}
		}
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
//					//TODO: hier die �bersetung von nr in string einbinden
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
//						+ "<th><b>Sender</th>" + "<th><b>Empf�nger</th>" + "<th><b>Typ</th>"
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
//	                "F�r diese Operation ist eine Verbindung zum lokalen DB-Server zwingend notwendig",
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
	
	private final class writeEverythingToLocDB implements Runnable{

		public void run() {
			while (dbStatus >= 3){
				executeWritingUsers();
				executeWriteAllGroupsToLocDB();
				executeWriteMsgToDB();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
}
