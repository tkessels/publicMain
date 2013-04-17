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
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JTextPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.publicmain.common.Config;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.NachrichtenTyp;

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
	private int dbStatus;					// 0=nicht bereit	1=con besteht	2=???
	private int maxVersuche;
	private long warteZeitInSec;
	//--------neue Sachen ende------------
	
	// verbindungssachen
	private static LocalDBConnection me;
	
	private LocalDBConnection() {
		this.url 					= "jdbc:mysql://localhost:"+Config.getConfig().getLocalDBPort()+"/";
		this.user 					= Config.getConfig().getLocalDBUser();
		this.passwd 				= Config.getConfig().getLocalDBPw();
		this.dbName 			= Config.getConfig().getLocalDBDatabasename();
		this.msgLogTbl				= "t_messages";
		this.msgTbl					= "t_msg";
		this.usrTbl					= "t_usr";
		this.nodeTbl				= "t_node";
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
		//--------neue Sachen ende------------
		
		
		
		Runnable tmp = new Runnable(){
			public void run() {
				connectToLocDBServer(); //TODO: Warum läuft das prog nicht direkt weiter wenn der DB-Server aus ist? Selbst wenn dieser Aufruf hier in nem extra Threat ist geht´s nicht.
			}
		};
//		tmp.run();
		new Thread(tmp).start();
		
	}
	public static LocalDBConnection getDBConnection() {
		if (me == null) {
			me = new LocalDBConnection();
		}
		return me;
	}
	private void connectToLocDBServer(){	// wird nur vom Construktor aufgerufen
		Runnable tmp = new Runnable(){
		int versuche = 0;

			public void run() {
				while ((versuche < maxVersuche) && (dbStatus == 0)){ //hatte da erst con==null drin aber das ist ein problem beim reconnecten unten.
					try {
								con = DriverManager.getConnection(url, user, passwd);
								LogEngine.log(this, "DB-ServerVerbindung hergestellt", LogEngine.INFO);
								dbStatus = 1;
								versuche = 0;
					} catch (SQLException e) {
						LogEngine.log(this, "DB-Verbindungsaufbau fehlgeschlagen: " + e.getMessage(), LogEngine.ERROR);
						try {
							Thread.sleep(warteZeitInSec * 1000);
						} catch (InterruptedException e1) {
							LogEngine.log(this, "Fehler beim Warten: " + e1.getMessage(), LogEngine.ERROR);
						}
						versuche ++;
						dbStatus = 0;
					}
				}
			}
		};
		new Thread(tmp).start();			
		if (dbStatus == 1 ){
			createDbAndTables();
		}
	}
	
	private void createDbAndTables (){	// wird nur vom Construktor aufgerufen
		String read=null;
		try (BufferedReader in = new BufferedReader(new FileReader(new File(getClass().getResource("create_db.sql").toURI())))){
			this.stmt = con.createStatement();
			while((read = in.readLine()) != null) {
				stmt.execute(read);
			}
			dbStatus = 2;
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
		
//		try {
//			this.stmt = con.createStatement();
//			stmt.addBatch("create database if not exists " + dbName);
//			stmt.addBatch("use " + dbName);
//			// erstellen der Message Tabelle
//			stmt.addBatch("create table if not exists "+ chatLogTbl + "(id INTEGER NOT NULL AUTO_INCREMENT," +		// hier autoincrement nutzen - kann ja mehrere mit der selben geben
//																	"msgID INTEGER NOT NULL," +
//																	"timestamp BIGINT NOT NULL," +
//																	"sender BIGINT NOT NULL," +
//																	"empfaenger BIGINT NOT NULL," +
//																	"typ varchar(30) NOT NULL," +
//																	"grp varchar(20) NOT NULL," +
//																	"data varchar(200) NOT NULL," +
//																	"primary key(id))" +
//																	"engine = INNODB");
//			// TODO Datentypen anpassen! 
//			// erstellen der user-Tabelle
//			stmt.addBatch("create table if not exists "+ usrTbl + "(id BIGINT NOT NULL," +		// hier die USER-ID
//					"alias VARCHAR(20) NOT NULL," +														// hier auf 20 Zeichen begrenzt
//					"primary key(id))" +
//					"engine = INNODB");
//			// erstellen der message-Tabelle was soll die machen? Verstehe ich nicht!??
////						stmt.addBatch("create table if not exists "+ msgTbl + "(id BIGINT NOT NULL," +		// hier die USER-ID
////								"alias VARCHAR(20)," +														// hier auf 20 Zeichen begrenzt
////								"primary key(id))" +
////								"engine = INNODB");
//			// erstellen der node-Tabelle für ALLE nodes?
//			stmt.addBatch("create table if not exists "+ nodeTbl + "(ip VARCHAR(15) NOT NULL," +// Als sting abspeichern? 192.168.100.200 -> 15  
//					"hostname VARCHAR(20) NOT NULL," +											//TODO: wie lang max? 
//					"nodeID BIGINT NOT NULL," +
//					"primary key(nodeID))" +
//					"engine = INNODB");	
//			// erstellen der Gruppen-Tabelle
//			stmt.addBatch("create table if not exists "+ groupTbl + "(groupID BIGINT NOT NULL," +			
//					"name VARCHAR(20) NOT NULL," +													//TODO: wie lang max? 
//					"password VARCHAR(20) NOT NULL," +												//TODO: wie lang max?
//					"groupOwner BIGINT NOT NULL," +
//					"primary key(groupID))" +
//					"engine = INNODB");	
//			// erstellen der Config-Tabelle
//			stmt.addBatch("create table if not exists "+ configTbl + "(nodeID BIGINT NOT NULL," +			
//					"userID BIGINT NOT NULL," +														//TODO: wie lang max? 
//					"layout VARCHAR(20) NOT NULL," +												//TODO: wie lang max?
//					"remotDBSvrIP VARCHAR(15) NOT NULL," +											//TODO: Als sting abspeichern? 192.168.100.200 -> 15  
//					"Alias VARCHAR(20) NOT NULL," +
//					"primary key(nodeID,userID))" +
//					"engine = INNODB");	
//			// erstellen der EventTyp-Tabelle														//TODO: was macht das wo kommen daten her? Was wird genau gespeichert?!?
//			stmt.addBatch("create table if not exists "+ eventTypeTbl + "(id BIGINT NOT NULL," +			
//					"description VARCHAR(200) NOT NULL," +														//TODO: wie lang max? 
//					"primary key(ID))" +
//					"engine = INNODB");	
//			// erstellen die Routing-Tabelle
//			stmt.addBatch("create table if not exists "+ routingOverviewTbl + "(id BIGINT NOT NULL," +	//TODO: hier nochmal extrem nachdenken ;-)
//					"primary key(id))" +
//					"engine = INNODB");	
//			stmt.executeBatch();
//			dbStatus = 2;
//			LogEngine.log(this, "createDbAndTables erstellt", LogEngine.INFO);
//		} catch (SQLException e) {
//			LogEngine.log(this, "createDbAndTables fehlgeschlagen: "+ e.getMessage(), LogEngine.ERROR);
//		}
	}
	
	public void saveMsg (MSG m){
		//Hier wird message in ne Blocking queue geschrieben
		locDBInbox.add(m);
		//TODO: Einbinden!
//		if (locDBInbox.size() >= 10 && dbStatus >= 2){	//Sobald 10 nachichten drin sind wird in DB geschrieben!!!
//			writeMsgToDB(); //was ist wenn die methode schon aufgerufen ist und der Threat dadrin schon läuf???
////			 hier muss man doch was mit Syncornized machen wenn nachichten hier rein geschriebn und unten raus geholt werden...
//		}
	}
	
	public void writeMsgToDB() {
		Runnable tmp = new Runnable() {
			public void run() {
				if (dbStatus >= 2) {
					while (!locDBInbox.isEmpty() && dbStatus >=2) {
							MSG m = locDBInbox.poll();
							if (m.getTyp() == NachrichtenTyp.GROUP || m.getTyp() == NachrichtenTyp.PRIVATE) {
								String saveStmt = ("insert into "
										+ msgLogTbl
										+ " (msgID,timestamp,sender,empfaenger,typ,grp,data,t_user_userID)"
										+ " VALUES (" + m.getId() + ","
										+ m.getTimestamp() + "," + m.getSender()
										+ "," + m.getEmpfänger() + "," + "'"
										+ m.getTyp() + "'" + "," + "'"
										+ m.getGroup() + "'" + "," + "'"
										+ m.getData() + "'" + ")");
								try {
									stmt.execute(saveStmt);
									LogEngine.log(LocalDBConnection.this, "Nachicht " + m.getId() + " in DB-Tabelle " + msgLogTbl + " eingetragen.", LogEngine.INFO);
								} catch (Exception e) {
									LogEngine.log(LocalDBConnection.this,"Fehler beim eintragen in : "+ msgLogTbl + " "+ e.getMessage(),LogEngine.ERROR);
									locDBInbox.add(m);	//TODO: Schreibt nachicht, die rausgenommen wurde, bei der es einen Fehler beim abspeichern gab wieder zurück in Queue! Wie schafft man das, dass es an die richtige stelle geschrieben wird
									dbStatus = 0;
									connectToLocDBServer();	//DB-Connection wird also nur versucht wieder aufzubauen wenn sie kurz nach programmstart schonmal bestand - sonnst kommt man hier garnicht rein.
									//hier falls während der schreibvorgänge die verbind verloren geht.
								}
							}
					}
				} else {
					LogEngine.log(LocalDBConnection.this,"DB nicht bereit zu schreiben"+ msgLogTbl, LogEngine.ERROR);
				}
				
			}
		};
		(new Thread(tmp)).start();
	}
	
	public void deleteAllMsgs () {
//		if (isDBConnected) {
//			int eingabe = JOptionPane.showConfirmDialog(null,
//					"Yout´re about to delete your ChatLog!\n"
//							+ "Are you really shore that you want to do it?",
//					"Delete confirmation", JOptionPane.YES_NO_OPTION);
//			if (eingabe == 1) {
//				// no, he´s not shore!
//
//			} else if (eingabe == 0) {
//				askForConnectionRetry = true;
//				connectToLocDBServer();
//
//				try {
//					this.stmt = con.createStatement();
//					stmt.addBatch("DROP TABLE IF EXISTS " + chatLogTbl);
//					stmt.executeBatch();
//					createDbAndTables();
//
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}else {
//			askForConnectionRetry = true;
//			if (connectToLocDBServer()) {
//				isDBConnected = true;
//				deleteAllMsgs();
//			} else {
//				//System.out.println("Erneuter versuch der Verbindungsherstellung erfolglos!");
//			}
//		}
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

}

