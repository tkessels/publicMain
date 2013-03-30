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
	private String msgHistTbl;
	private boolean isDBConnected;
	private static DBConnection me;
	
	private DBConnection() {
		this.url = "jdbc:mysql://localhost:3306/";
		this.user = "root";
		this.passwd = "";
		this.dbName = "db_javatest";
		this.msgHistTbl= "t_msgHistory";
		this.isDBConnected = false;

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
			LogEngine.log("DB-ServerVerbindung hergestellt", this, LogEngine.INFO);
			return true;
		} catch (SQLException e) {
			LogEngine.log("DB-Verbindung fehlgeschlagen: " + e.getMessage(), this, LogEngine.ERROR);
			return false;
		}
	}
	private void createDbAndTables (){	// wird nur vom Construktor aufgerufen
		try {
			this.stmt = con.createStatement();
			stmt.execute("create database if not exists " + dbName);
			stmt.execute("use " + dbName);
			// TODO Datentypen anpassen!
			stmt.execute("create table if not exists "+ msgHistTbl + "(id int(200) NOT NULL," +
																	"sender BIGINT NOT NULL," +
																	"timestamp DOUBLE PRECISION NOT NULL," +
																	"empfaenger int(200) NOT NULL," +
																	"grp varchar(20) NOT NULL," +
																	"data varchar(20) NOT NULL," +
																	"primary key(id))" +
																	"engine = INNODB");
			LogEngine.log("createDbAndTables erstellt", this, LogEngine.INFO);
		} catch (SQLException e) {
			LogEngine.log("createDbAndTables fehlgeschlagen: "+ e.getMessage(), this, LogEngine.ERROR);
		}
	}
	
	// TODO in seperate Klasse auslagern! 
	public void saveMsg (MSG m){
		if (isDBConnected){
			if(m.getTyp() == NachrichtenTyp.GROUP || m.getTyp() == NachrichtenTyp.PRIVATE){
				String saveStmt = ("insert into " + msgHistTbl + " VALUES (" +
						m.getId() + "," +
						m.getSender() + "," +
						m.getTimestamp() + "," +
						m.getEmpfänger() + "," +
						"'" + m.getGroup() + "'" + ","+
						"'" + m.getData() + "'" + ")");
				try {
					System.out.println(saveStmt);
					stmt.execute(saveStmt);
					LogEngine.log("Nachicht in DB-Tabelle " + msgHistTbl + " eingetragen.", this, LogEngine.INFO);
				} catch (Exception e) {
					LogEngine.log("Fehler beim eintragen in : "+ msgHistTbl + " " + e.getMessage(), this, LogEngine.ERROR);
				}
			}
		} else {
			System.out.println("es besteht keine DB-Verbindung!");
			if(connectToLocDBServer()){
				isDBConnected = true;
				createDbAndTables();
				saveMsg(m);
				
			}else {
				System.out.println("Erneuter versuch der Verbindungsherstellung erfolglos!");
			}
		}
		
		
		
	}

}

