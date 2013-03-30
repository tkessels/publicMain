package org.publicmain.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
	private static DBConnection me;
	
	private DBConnection() {
		this.url = "jdbc:mysql://localhost:3306/";
		this.user = "root";
		this.passwd = "";
		this.dbName = "db_javatest";
		this.msgHistTbl= "t_msgHistory";
		if(connectToLocDBServer()){
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
			stmt.execute("create database if not exists " + dbName);
			stmt.execute("use " + dbName);
			// TODO Datentypen anpassen!
			stmt.execute("create table if not exists "+ msgHistTbl + "(id int(200) NOT NULL," +
																	"sender int(200) NOT NULL," +
																	"timestamp int(200) NOT NULL," +
																	"empfänger int(200) NOT NULL," +
																	"grp varchar(20)," +
																	"data varchar(20) NOT NULL," +
																	"primary key(id))" +
																	"engine = INNODB");
			LogEngine.log(this, "createDbAndTables erstellt", LogEngine.INFO);
		} catch (SQLException e) {
			LogEngine.log(this, "createDbAndTables fehlgeschlagen: "+ e.getMessage(), LogEngine.ERROR);
		}
	}
	
	// TODO in seperate Klasse auslagern! 
	public void saveMsg (MSG m){
		if(m.getTyp() == NachrichtenTyp.GROUP || m.getTyp() == NachrichtenTyp.PRIVATE){
			String saveStmt = ("insert into " + msgHistTbl + " id, sender, timestamp, empfänger,grp,data values (" +
					m.getId() + "," +
					m.getSender() + "," +
					m.getTimestamp() + "," +
					m.getEmpfänger() + "," +
					m.getGroup() + ","+
					m.getData() + ")");
			try {
				stmt.execute(saveStmt);
				LogEngine.log(this, "Nachicht in DB-Tabelle " + msgHistTbl + " eingetragen.", LogEngine.INFO);
			} catch (Exception e) {
				LogEngine.log(this, "Fehler beim eintragen in : "+ msgHistTbl + " " + e.getMessage(), LogEngine.ERROR);
			}
		}
		
	}

}

