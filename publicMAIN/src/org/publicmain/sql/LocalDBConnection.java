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
	private boolean writtenStandardGroups;
	private boolean writtenStandardUser;
	private Thread connectToDBThread;
	private Thread hardWorkingThread;
	private int dbVersion;
	//--------neue Sachen ende------------
	
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

		//--------neue Sachen------------
		this.locDBInbox = new LinkedBlockingQueue<MSG>();;
		this.dbStatus	= 0;				// 0=nicht bereit	1=con besteht	2=Tables wurden angelegt 3=???
		this.maxVersuche = 5;
		this.warteZeitInSec = 10;
		this.writtenStandardGroups = false;
		this.writtenStandardUser = false;
		this.connectToDBThread = new Thread(new connectToLocDBServer());
		this.hardWorkingThread = new Thread(new writeEverythingToLocDB());
		this.dbVersion = 2;
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
					LogEngine.log(
							this,
							"DB-Verbindungsaufbau fehlgeschlagen: "+ e.getMessage(), LogEngine.ERROR);
					try {
						Thread.sleep(warteZeitInSec * 1000);
					} catch (InterruptedException e1) {
						LogEngine.log(this,"Fehler beim Warten: " + e1.getMessage(),LogEngine.ERROR);
					}
					versuche++;
					dbStatus = 0;
				}
			}
			try {
				stmt.executeQuery("use " + dbName);
				if(Config.getConfig().getLocalDBVersion() < dbVersion){
					createDbAndTables();
					run();
				} else {
					dbStatus = 3;
					LogEngine.log(this, "DB-Status: " + dbStatus, LogEngine.INFO);
					hardWorkingThread.start(); 
				}
			} catch (SQLException e1) {
				createDbAndTables();
				run();
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
			stmt.execute("CREATE PROCEDURE `db_publicmain`.`p_t_groups_saveGroups` (IN newname VARCHAR(20),IN newt_user_userID BIGINT(20)) BEGIN insert into t_groups (name, t_user_userID) values (newname,newt_user_userID) ON DUPLICATE KEY UPDATE t_user_userID=VALUES(t_user_userID); END");
			stmt.execute("CREATE PROCEDURE `db_publicmain`.`p_t_messages_saveMessage` (IN newmsgID INT(11), IN newtimestmp BIGINT(20), IN newt_user_userID_sender BIGINT(20), IN newt_user_userID_empfaenger BIGINT(20), IN newt_msgType_name VARCHAR(20), IN newt_groups_name VARCHAR(20), IN newtxt VARCHAR(200)) BEGIN	INSERT INTO t_messages (msgID,timestmp,t_user_userID_sender,t_user_userID_empfaenger,t_msgType_name,t_groups_name,txt) VALUES (newmsgID, newtimestmp, newt_user_userID_sender, newt_user_userID_empfaenger, newt_msgType_name, newt_groups_name, newtxt); END;");
			stmt.execute("CREATE PROCEDURE `db_publicmain`.`p_t_user_saveUsers` (IN newuserID BIGINT(20),IN newalias VARCHAR(45),IN newusername VARCHAR(45)) BEGIN insert into t_users (userID, alias, username)	values (newuserID,newalias,newusername) ON DUPLICATE KEY UPDATE alias=VALUES(alias),username=VALUES(username); END;");
			dbStatus = 2;
			Config.getConfig().setLocalDBVersion(dbVersion);
			Config.write();
			LogEngine.log(this, "DB-Status: " + dbStatus, LogEngine.INFO);
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
		allNodes = newAllNodes;
	}
	
	private void executeWritingUsers(){
		if(!writtenStandardUser){
			try {
				stmt.execute("CALL p_t_user_saveUsers(" + ChatEngine.getCE().getUserID() + ",'" + ChatEngine.getCE().getAlias() + "','" + NodeEngine.getNE().getNode(NodeEngine.getNE().getNodeID()).getUsername() + "')");
				stmt.execute("CALL p_t_user_saveUsers(-1, 'standardPublicUser', 'standardPublicUser')");
				} catch (SQLException e) {
				LogEngine.log(this, "Fehler beim Schreiben der StandardUser in 'executeWritingUsers" + e.getMessage(), LogEngine.ERROR);
			}
			writtenStandardUser = true;
		}
		
		
		if (allNodes != null){
			System.out.println("drin");
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
					//hier falls während der schreibvorgänge die verbind verloren geht.
				}
			}
			allNodes = null;
		} else {
		}
	}
	
	public synchronized void writeAllGroupsToLocDB(HashSet<String> newGroupsSet){
		groupsSet = newGroupsSet;
	}
	
	private void executeWriteAllGroupsToLocDB(){
		if(!writtenStandardGroups){
			try {
				stmt.execute("call p_t_groups_saveGroups('SYSTEM'," + ChatEngine.getCE().getUserID() +")");
				stmt.execute("call p_t_groups_saveGroups('PRIVATE',"+ ChatEngine.getCE().getUserID() +")");
				stmt.execute("call p_t_groups_saveGroups('public',"+ ChatEngine.getCE().getUserID() +")");
			} catch (SQLException e) {
				LogEngine.log(this, "Fehler beim Schreiben der StandardGruppen in 'executeWriteAllGroupsToLocDB " + e.getMessage(), LogEngine.ERROR);
			}
			writtenStandardGroups = true;
		}
		
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
		System.out.println("in der write");
	}
	
	private void executeWriteMsgToDB() {
		BlockingQueue<MSG> tmpLocDBInbox = locDBInbox;
		locDBInbox.clear();
		while (!tmpLocDBInbox.isEmpty() && dbStatus >= 3) {
			System.out.println("in der while!");
			StringBuffer saveMsgStmt = new StringBuffer();
			StringBuffer saveGrpStmt = new StringBuffer();
			MSG m = tmpLocDBInbox.poll();
			System.out.println(m.toString());
			long uid_empfänger = NodeEngine.getNE().getUIDforNID(m.getEmpfänger());
			long uid_sender = NodeEngine.getNE().getUIDforNID(m.getSender());
			if (m.getTyp() == NachrichtenTyp.GROUP) {
				// fügt Gruppe hinzu
//				saveGrpStmt.append("CALL p_t_groups_saveGroups(");
//				saveGrpStmt.append("'" + m.getGroup() + "',");
//				saveGrpStmt.append(ChatEngine.getCE().getUserID() + ")");
				// fügt Nachicht hinzu
				saveMsgStmt.append("CALL p_t_messages_saveMessage(");
				saveMsgStmt.append(m.getId() + ",");
				saveMsgStmt.append(m.getTimestamp() + ",");
				saveMsgStmt.append(uid_sender + ",");
				saveMsgStmt.append(uid_empfänger + ",");
				saveMsgStmt.append("'" + m.getTyp() + "',");
				saveMsgStmt.append("'" + m.getGroup() + "',");
				saveMsgStmt.append("'" + m.getData() + "')");
				System.out.println("in Group");
			}
			if (m.getTyp() == NachrichtenTyp.PRIVATE) {
				// fügt Gruppe PRIVATE
//				saveGrpStmt.append("CALL p_t_groups_saveGroups(");
//				saveGrpStmt.append("'PRIVATE',");
//				saveGrpStmt.append(ChatEngine.getCE().getUserID() + ")");
				// fügt Nachicht hinzu
				saveMsgStmt.append("CALL p_t_messages_saveMessage(");
				saveMsgStmt.append(m.getId() + ",");
				saveMsgStmt.append(m.getTimestamp() + ",");
				saveMsgStmt.append(uid_sender + ",");
				saveMsgStmt.append(uid_empfänger + ",");
				saveMsgStmt.append("'" + m.getTyp() + "',");
				saveMsgStmt.append("'PRIVATE',");
				saveMsgStmt.append("'" + m.getData() + "')");
			}
			if (m.getTyp() == NachrichtenTyp.SYSTEM) {
				// fügt Gruppe PRIVATE
//				saveGrpStmt.append("CALL p_t_groups_saveGroups(");
//				saveGrpStmt.append("'SYSTEM',");
//				saveGrpStmt.append(ChatEngine.getCE().getUserID() + ")");
				// fügt Nachicht hinzu
				saveMsgStmt.append("CALL p_t_messages_saveMessage(");
				saveMsgStmt.append(m.getId() + ",");
				saveMsgStmt.append(m.getTimestamp() + ",");
				saveMsgStmt.append(uid_sender + ",");
				saveMsgStmt.append(uid_empfänger + ",");
				saveMsgStmt.append("'" + m.getCode() + "',");
				saveMsgStmt.append("'SYSTEM',");
				saveMsgStmt.append("'" + m.getData() + "')");
			}
			if (saveMsgStmt.length()> 0){ //&& saveGrpStmt.length() > 0){
				try {
//					stmt.execute(saveGrpStmt.toString());
//					LogEngine.log(LocalDBConnection.this, "Nachicht " + m.getId() + " mit p_t_groups_saveGroups aufgerufen", LogEngine.INFO);
					stmt.execute(saveMsgStmt.toString());
//					LogEngine.log(LocalDBConnection.this, "Nachicht " + m.getId() + " mit p_t_messages_saveMessage aufgerufen", LogEngine.INFO);
				} catch (Exception e) {
					LogEngine.log(LocalDBConnection.this,"Fehler beim aufruf von  : p_t_groups_saveGroups oder p_t_messages_saveMessage "+ e.getMessage(),LogEngine.ERROR);
					locDBInbox.add(m);	//TODO: Schreibt nachicht, die rausgenommen wurde, bei der es einen Fehler beim abspeichern gab wieder zurück in Queue! Wie schafft man das, dass es an die richtige stelle geschrieben wird
					dbStatus = 0;
					//hier falls während der schreibvorgänge die verbind verloren geht.
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
			System.out.println("draußen");
		}
		
	}
}
