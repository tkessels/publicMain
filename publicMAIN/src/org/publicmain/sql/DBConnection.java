package org.publicmain.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.NachrichtenTyp;


public class DBConnection {
// TODO Kommentieren
	private Connection con;;
	private Statement stmt;
	private ResultSet rs;
	private String url;
	private String dbName;
	private String user;
	private String passwd;
	private String msgHistTbl;
	private LogEngine le;
	
	public DBConnection(LogEngine le) {
		this.url = "jdbc:mysql://localhost:3306/";
		this.user = "root";
		this.passwd = "";
		this.dbName = "db_javatest";
		this.msgHistTbl= "t_msgHistory";
		this.le = le;
		if(dbVerbindHerstellen()){
			createDbAndTables();
		}
	}
	private boolean dbVerbindHerstellen(){
		try {
			this.con = DriverManager.getConnection(url, user, passwd);
			this.stmt = con.createStatement();
			le.log("DB-ServerVerbindung hergestellt", this, LogEngine.INFO);
			return true;
		} catch (SQLException e) {
			le.log("DB-Verbindung fehlgeschlagen", this, LogEngine.ERROR);
			return false;
		}
	}
	private void createDbAndTables (){
		try {
			this.stmt = con.createStatement();
			stmt.execute("create database if not exists " + dbName);
			stmt.execute("use " + dbName);
			stmt.execute("create table if not exists "+ msgHistTbl + "(id int(200) NOT NULL," +
																	"sender int(200) NOT NULL," +
																	"timestamp int(200) NOT NULL," +
																	"empfänger int(200) NOT NULL," +
																	"grp varchar(20)," +
																	"data varchar(20) NOT NULL," +
																	"primary key(id))" +
																	"engine = INNODB");
			le.log("createDbAndTables erstellt", this, LogEngine.INFO);
		} catch (SQLException e) {
			le.log("createDbAndTables fehlgeschlagen: "+ e.getMessage(), this, LogEngine.ERROR);
		}
	}
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
				le.log("Nachicht in DB-Tabelle " + msgHistTbl + " eingetragen.", this, LogEngine.INFO);
			} catch (Exception e) {
				le.log("Fehler beim eintragen in : "+ msgHistTbl + " " + e.getMessage(), this, LogEngine.ERROR);
			}
		}
		
	}

}
