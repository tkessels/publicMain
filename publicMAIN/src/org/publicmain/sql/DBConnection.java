package org.publicmain.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JDialog;

import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.NachrichtenTyp;
import org.publicmain.gui.GUI;

/**
 * Die Klasse DBConnection stellt die Verbindung zu dem Lokalen DB-Server her.
 * Sie legt weiterhin alle zwingend notwendigen Datenbanken(1) und Tabellen an.
 */
public class DBConnection {

	private Connection con;;
	private Statement stmt;
	//private ResultSet rs;
	private String url;
	private String dbName;
	private String user;
	private String passwd;
	
	//Tabellen der Loc DB
	private String chatLogTbl;
	private String msgTbl;
	private String usrTbl;
	private String nodeTbl;
	private String groupTbl;
	private String configTbl;
	private String eventTypeTbl;
	private String routingOverviewTbl;
	
	
	// verbindungssachen
	private boolean isDBConnected;		// noch richtig implementieren oder weg lassen weil ehh jedesmal neu prüfen?
	private static DBConnection me;
	
	private DBConnection() {
		this.url 				= "jdbc:mysql://localhost:3306/";
		this.user 				= "root";
		this.passwd 			= "";
		this.dbName 			= "db_publicMain";
		this.chatLogTbl			= "t_chatLog";
		this.msgTbl				= "t_msg";
		this.usrTbl				= "t_usr";
		this.nodeTbl			= "t_node";
		this.groupTbl			= "t_group";
		this.configTbl			= "t_config";
		this.eventTypeTbl		= "t_eventType";
		this.routingOverviewTbl	= "t_routingOverView";
		this.isDBConnected 		= false;

		if(connectToLocDBServer()){
			isDBConnected = true;
			createDbAndTables();
		}
	}
	public static DBConnection getDBConnection() {
		if (me == null) {
			me = new DBConnection();
		}
		return me;
	}
	private boolean connectToLocDBServer(){	// wird nur vom Construktor aufgerufen
		try {
			this.con = DriverManager.getConnection(url, user, passwd);
			this.stmt = con.createStatement();
			LogEngine.log(this, "DB-ServerVerbindung hergestellt", LogEngine.INFO);
			return true;
		} catch (SQLException e) {
			LogEngine.log(this, "DB-Verbindung fehlgeschlagen: " + e.getMessage(), LogEngine.ERROR);
			return false;
		}
	}
	private void createDbAndTables (){	// wird nur vom Construktor aufgerufen
		try {
			this.stmt = con.createStatement();
			stmt.addBatch("create database if not exists " + dbName);
			stmt.addBatch("use " + dbName);
			// erstellen der Message Tabelle
			stmt.addBatch("create table if not exists "+ chatLogTbl + "(id INTEGER NOT NULL AUTO_INCREMENT," +		// hier autoincrement nutzen - kann ja mehrere mit der selben geben
																	"msgID INTEGER NOT NULL," +
																	"sender BIGINT NOT NULL," +
																	"timestamp BIGINT NOT NULL," +
																	"empfaenger BIGINT NOT NULL," +
																	"grp varchar(20) NOT NULL," +
																	"data varchar(200) NOT NULL," +
																	"primary key(id))" +
																	"engine = INNODB");
			// TODO Datentypen anpassen! 
			// erstellen der user-Tabelle
			stmt.addBatch("create table if not exists "+ usrTbl + "(id BIGINT NOT NULL," +		// hier die USER-ID
					"alias VARCHAR(20) NOT NULL," +														// hier auf 20 Zeichen begrenzt
					"primary key(id))" +
					"engine = INNODB");
			// erstellen der message-Tabelle was soll die machen? Verstehe ich nicht!??
//						stmt.addBatch("create table if not exists "+ msgTbl + "(id BIGINT NOT NULL," +		// hier die USER-ID
//								"alias VARCHAR(20)," +														// hier auf 20 Zeichen begrenzt
//								"primary key(id))" +
//								"engine = INNODB");
			// erstellen der node-Tabelle für ALLE nodes?
			stmt.addBatch("create table if not exists "+ nodeTbl + "(ip VARCHAR(15) NOT NULL," +// Als sting abspeichern? 192.168.100.200 -> 15  
					"hostname VARCHAR(20) NOT NULL," +											//TODO: wie lang max? 
					"nodeID BIGINT NOT NULL," +
					"primary key(nodeID))" +
					"engine = INNODB");	
			// erstellen der Gruppen-Tabelle
			stmt.addBatch("create table if not exists "+ groupTbl + "(groupID BIGINT NOT NULL," +			
					"name VARCHAR(20) NOT NULL," +													//TODO: wie lang max? 
					"password VARCHAR(20) NOT NULL," +												//TODO: wie lang max?
					"groupOwner BIGINT NOT NULL," +
					"primary key(groupID))" +
					"engine = INNODB");	
			// erstellen der Config-Tabelle
			stmt.addBatch("create table if not exists "+ configTbl + "(nodeID BIGINT NOT NULL," +			
					"userID BIGINT NOT NULL," +														//TODO: wie lang max? 
					"layout VARCHAR(20) NOT NULL," +												//TODO: wie lang max?
					"remotDBSvrIP VARCHAR(15) NOT NULL," +											//TODO: Als sting abspeichern? 192.168.100.200 -> 15  
					"Alias VARCHAR(20) NOT NULL," +
					"primary key(nodeID,userID))" +
					"engine = INNODB");	
			// erstellen der EventTyp-Tabelle														//TODO: was macht das wo kommen daten her? Was wird genau gespeichert?!?
			stmt.addBatch("create table if not exists "+ eventTypeTbl + "(id BIGINT NOT NULL," +			
					"description VARCHAR(200) NOT NULL," +														//TODO: wie lang max? 
					"primary key(ID))" +
					"engine = INNODB");	
			// erstellen die Routing-Tabelle
			stmt.addBatch("create table if not exists "+ routingOverviewTbl + "(id BIGINT NOT NULL," +	//TODO: hier nochmal extrem nachdenken ;-)
					"primary key(id))" +
					"engine = INNODB");	
			stmt.executeBatch();
			LogEngine.log(this, "createDbAndTables erstellt", LogEngine.INFO);
		} catch (SQLException e) {
			LogEngine.log(this, "createDbAndTables fehlgeschlagen: "+ e.getMessage(), LogEngine.ERROR);
		}
	}
	
	// TODO in seperate Klasse auslagern! 
	public void saveMsg (final MSG m){
		Runnable tmp = new Runnable() {
			public void run() {
				if (isDBConnected) {
					if (m.getTyp() == NachrichtenTyp.GROUP
							|| m.getTyp() == NachrichtenTyp.PRIVATE) {
						String saveStmt = ("insert into " + chatLogTbl + " (msgID,sender,timestamp,empfaenger,grp,data)"
								+ " VALUES (" + m.getId() + "," + m.getSender()
								+ "," + m.getTimestamp() + ","
								+ m.getEmpfänger() + "," + "'" + m.getGroup()
								+ "'" + "," + "'" + m.getData() + "'" + ")");
						try {
							//System.out.println(saveStmt);
							stmt.execute(saveStmt);
							LogEngine.log(DBConnection.this,
									"Nachicht in DB-Tabelle " + chatLogTbl
											+ " eingetragen.", LogEngine.INFO);
						} catch (Exception e) {
							LogEngine.log(DBConnection.this,
									"Fehler beim eintragen in : " + chatLogTbl
											+ " " + e.getMessage(),
									LogEngine.ERROR);
						}
					}
				} else {
					//System.out.println("es besteht keine DB-Verbindung!");
					if (connectToLocDBServer()) {
						isDBConnected = true;
						createDbAndTables();
						saveMsg(m);

					} else {
						//System.out.println("Erneuter versuch der Verbindungsherstellung erfolglos!");
					}
				}
			}
		};
		(new Thread(tmp)).start();
		
		
	}

}

