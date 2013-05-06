package org.publicmain.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.table.DefaultTableModel;

import org.publicmain.common.Config;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.Node;
import org.publicmain.nodeengine.NodeEngine;

/**
 * 
 * Die Klasse DatabaseEngine Verwaltet die Kommunikation mit der lokalen und der Backupdatenbank.
 * Sie ruft deren Methoden auf um dem Anwenderwunsch gerecht zu werden
 * 
 * @author ATRM
 */
public class DatabaseEngine {

	private LocalDBConnection localDB;
	private BackupDBConnection backupDB;

	private BlockingQueue<MSG> msg2Store;
	private BlockingQueue<Node> node2Store;
	private BlockingQueue<Map.Entry<Long, Long>> routes2Store;
	private BlockingQueue<String> groups2Store; 

	private HashSet<MSG> failed_msgs;
	private List<Node> failed_node;
	private BlockingQueue<Map.Entry<Long, Long>> failed_routes;

	private static DatabaseEngine me;
	Thread transporter = new Thread(new DPTransportBot());

	private DatabaseEngine() {
		me=this;
		localDB = LocalDBConnection.getDBConnection(this);
		backupDB = BackupDBConnection.getBackupDBConnection();

		msg2Store = new LinkedBlockingQueue<MSG>();
		node2Store = new LinkedBlockingQueue<Node>();
		routes2Store = new LinkedBlockingQueue<Map.Entry<Long,Long>>();
		groups2Store = new LinkedBlockingQueue<String>();
		failed_msgs = new HashSet<MSG>();
		failed_node = new ArrayList<Node>();
		failed_routes = new LinkedBlockingQueue<Map.Entry<Long,Long>>();

		Config.registerDatabaseEngine(this);
		transporter.start();
	}

	/**
	 * Factory-Methode der DatabaseEngine
	 * @return DIE DatabaseEngine-Instanz
	 */
	public synchronized static DatabaseEngine getDatabaseEngine() {
		if(me==null) {
			new DatabaseEngine();
		}
		return me;
	}

	
	/**
	 * Diese Methode weist die BackupDatenbankConnectionKlasse an eine bestimmte Nutzername-Passwort-Kombination auf gültigkeit zu überprüfen.
	 * @param username: Nutzername des bestimmten Benutzers
	 * @param password:	Passwort des bestimmten Benutzers
	 * @return
	 */
	public boolean isValid(String username, String password) {
		return (backupDB.getIDfor(username, password)!=-1);
	}
	
	/**
	 * Diese Methode holt sich eine Connection von der BackupDBConnection Klasse,
	 * indem sie ihr die gewünschten parameter übergibt und prüft damit / anschließend damit die 
	 * richtigkeit der übergebenen Daten.
	 * @param username:		zu überprüfender Benutzername
	 * @param password:		zu überprüfendes Passowort
	 * @param ip:			IP-Adresse des Datenbankservers zu welchem die Verbindung hergestellt werden soll
	 * @param dbPort:		Port über welchen die DB-Verbidung hergestellt werden soll
	 * @param dbusername:	Username mit welchem die DB-Verbidung hergestellt werden soll
	 * @param dbpassword:	Passwort mit welchem die DB-Verbidung hergestellt werden soll
	 * @return true:		übergebene Daten Korrekt
	 * @return false:		übergebene Daten fehlerhaft
	 */
	public boolean isValid(String username, String password,String ip, String dbPort, String dbusername, String dbpassword) {
		long tmpID=-1;
		PreparedStatement prp = null;
		Connection x = null;
		ResultSet myid = null;
		try {
			x = BackupDBConnection.getBackupDBConnection().getCon(ip, dbPort, Config.getConfig().getBackupDBDatabasename(), dbusername, dbpassword, 100);
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
		}finally {
				   try{myid.close();  }catch(Exception ignored){}
				   try{prp.close();}catch(Exception ignored){}
				   try{x.close();}catch(Exception ignored){}
		}
		return (tmpID!=-1);
	}

	/**
	 * Diese Methode speichert einen übergebene Nachricht(x) in eine lokale NachrichtenQueue 
	 * @param x: Die zu speichernde Nachricht
	 */
	public void put(MSG x){
		msg2Store.offer(x);
	}

	/**
	 * Diese Methode speichert einen übergebenen Node(x) in eine lokale NodeQueue 
	 * @param x: Der zu speichernde Node
	 */
	public void put(Node x){
		node2Store.offer(x);
	}

	/**
	 * Diese Methode speichert eine übergebene Nodecollection(x) nacheinander in eine lokale NodeQueue 
	 * @param x: Die zu speichernde Nodeliste
	 */
	public void put(Collection<Node> x){
		for (Node node : x) {
			node2Store.offer(node);
		}
	}

	/**
	 * Diese Methode speichert einen übergebenen Gruppe(group) in eine lokale GruppenQueue 
	 * @param x: Der zu speichernde Node
	 */
	public void put(String group){
		groups2Store.add(group);
	}
	
	/**
	 * Diese Methode speichert einen übergebenen Node(x) in eine lokale NodeQueue 
	 * @param x: Der zu speichernde Node
	 */
	/**
	 * Diese Methode speichert einen übergebenen Ziel-Gateway-Routenkombination in eine lokale RoutenQueue
	 * @param target:	NodeID des Ziels
	 * @param gateway:	NodeID des Gateways
	 */
	public void put(long target, long gateway){
		routes2Store.offer(new AbstractMap.SimpleEntry(target, gateway));
	}

	/**
	 * Diese Methode weist die LocalDBConnection an alle Nachichten aus der Datenbank zu entfernen
	 */
	public void deleteLocalHistory() {
		localDB.deleteAllMsgs();
	}

	/**
	 * Diese Methode führt nach Statusprüfung die Methode  deleteAllMessages() auf der backupDB aus. 
	 */
	public void deleteBackupMessages() {
		if(backupDB.getStatus()==2){
			backupDB.deleteAllMessages();
		}
	}

	/**
	 * Diese Methode weist die BackupDBConnection an die gegebene BenutzerName-Passwort-Kombination zu löschen
	 * @param username:	Benutzername des zu löschenden Nutzers
	 * @param password:	Passwort des zu löschenden Nutzers
	 * @return
	 */
	public int deleteBackupUserAccount(String username, String password) {
		if(backupDB.getStatus()>=1){
			if (backupDB.deleteUser(username,password))
				return 2;
			return 1;
		}
		return 0;
	}

	/**
	 * Diese Methode holt sich nach Statusüprüfung alle User,Nachichten und Settings von der LocalDBConnection-Klasse
	 * und übergibt diese zur Speicherung an die BackupDBConnection-Klasse
	 */
	public void push(){
		if(localDB.getStatus()&&(backupDB.getStatus()==2)){
			long id = backupDB.getMyID();
			backupDB.push_users(localDB.pull_users(),id);
			backupDB.push_msgs(localDB.pull_msgs(),id);
			backupDB.push_settings(localDB.pull_settings(),id);
		}
	}

	/**
	 * Diese Methode holt sich nach Statusüprüfung alle User,Nachichten und Settings von der BackupDBConnection-Klasse
	 * und übergibt diese zur Speicherung an die LocalDBConnection-Klasse
	 */
	public void pull(){
		if(localDB.getStatus()&&(backupDB.getStatus()==2)){
			long id = backupDB.getMyID();
			localDB.push_users(backupDB.pull_users(id));
			localDB.push_msgs(backupDB.pull_msgs(id));
			localDB.push_settings(backupDB.pull_settings(id));
		}
	}

	/**
	 * Queries the local Database for Messages of a selected user (<code>uid</code>) which have been send after the begin date but before the end date. 
	 * Is either of the given dates a negative long the respective field will be ignored. Additionialy the Querry can be further narrowed by giving a search text.
	 * Only messages where the written text contains the text (case insensitiv) will be returned. If the searchtext is an empty String it will be ignored.  
	 * @param uid UserId of User whose messages should be retrieved
	 * @param begin only messages after the given date (in long) will be considered. begin date will be ignored if negative 
	 * @param end only messages before the given date (in long) will be considered. end date will be ignored if negative
	 * @param text only messages containing this text will be shown. text will be ignored if empty string
	 * @return a JTable listing all the messages fitting the given attributes (Maybe the Database Engine will update the Datamodel of the HistoryWindow later)
	 */
	public DatabaseDaten selectMSGsByUser(long uid,GregorianCalendar begin, GregorianCalendar end,String text) {

		ResultSet tmpRS;

		String para_uid	=(uid>=0)?String.valueOf(uid):"%";
		String para_alias	=null;
		String para_group	=null;
		String para_text	=(text.trim().length()==0)?"%":"%"+text.trim()+"%";
		long para_begin 	= (begin!=null)?begin.getTimeInMillis():0;
		long para_end 	= (end!=null)?end.getTimeInMillis():Long.MAX_VALUE;

		//		System.out.println(para_uid+para_alias+para_group+"<"+para_begin+":"+para_end+">"+para_text);

		if (para_begin<para_end) {
			tmpRS =localDB.searchInHistory(para_uid,para_alias,para_group,para_begin,para_end,para_text);
		} else {
			tmpRS =localDB.searchInHistory(para_uid,para_alias,para_group,para_end,para_begin,para_text);
		}
		if(tmpRS!=null){
			try {
				return getResultData(tmpRS);
			} catch (SQLException e) {
				LogEngine.log(this, "Couldn't querry any messages",LogEngine.ERROR);
			}
		}
		return null;
	}

	/**
	 * Queries the local Database for Messages which have been send with the given <code>alias</code> if send after the begin date but before the end date. 
	 * Is either of the given dates a negative long the respective field will be ignored. Additionialy the Querry can be further narrowed by providing a search <code>text</code>.
	 * Only messages where the written text contains the text (case insensitiv) will be returned. If the searchtext is an empty String it will be ignored.  
	 * @param alias used in message
	 * @param begin only messages after the given date (in long) will be considered. begin date will be ignored if negative 
	 * @param end only messages before the given date (in long) will be considered. end date will be ignored if negative
	 * @param text only messages containing this text will be shown. text will be ignored if empty string
	 * @return a JTable listing all the messages fitting the given attributes
	 */	
	public DatabaseDaten selectMSGsByAlias(String alias,GregorianCalendar begin, GregorianCalendar end,String text) {
		ResultSet tmpRS;
		String para_uid		= null;
		String para_alias	= (alias.trim().length()==0)?"%":"%"+alias.trim()+"%";
		String para_group	= null;
		String para_text	= (text.trim().length()==0)?"%":"%"+text.trim()+"%";
		long para_begin 	= (begin!=null)?begin.getTimeInMillis():0;
		long para_end 		= (end!=null)?end.getTimeInMillis():Long.MAX_VALUE;

		System.out.println(para_uid+para_alias+para_group+"<"+para_begin+":"+para_end+">"+para_text);

		if (para_begin<para_end) {
			tmpRS =localDB.searchInHistory(para_uid,para_alias,para_group,para_begin,para_end,para_text);
		} else {
			tmpRS =localDB.searchInHistory(para_uid,para_alias,para_group,para_end,para_begin,para_text);
		}

		if(tmpRS!=null){
			try {
				return getResultData(tmpRS); //124
			} catch (SQLException e) {
				LogEngine.log(this, "Couldn't querry any messages",LogEngine.ERROR);
			}
		}
		return null;

	}

	/**
	 * Queries the local Database for Messages which have been send within the given <code>group</code> if send after the begin date but before the end date. 
	 * Is either of the given dates a negative long the respective field will be ignored. Additionialy the Querry can be further narrowed by providing a search <code>text</code>.
	 * Only messages where the written text contains the text (case insensitiv) will be returned. If the searchtext is an empty String it will be ignored.  
	 * @param group the message was send to
	 * @param begin only messages after the given date (in long) will be considered. begin date will be ignored if negative 
	 * @param end only messages before the given date (in long) will be considered. end date will be ignored if negative
	 * @param text only messages containing this text will be shown. text will be ignored if empty string
	 * @return a JTable listing all the messages fitting the given attributes
	 */	
	public DatabaseDaten selectMSGsByGroup(String group,GregorianCalendar begin, GregorianCalendar end,String text) {
		ResultSet tmp;
	
		String para_uid	=null;
		String para_alias	=null;
		String para_group	=(group.trim().length()==0)?"%":"%"+group.trim()+"%";
		String para_text	=(text.trim().length()==0)?"%":"%"+text.trim()+"%";
		long para_begin 	= (begin!=null)?begin.getTimeInMillis():0;
		long para_end 	= (end!=null)?end.getTimeInMillis():Long.MAX_VALUE;
	
		//		System.out.println(para_uid+para_alias+para_group+"<"+para_begin+":"+para_end+">"+para_text);
	
		if (para_begin<para_end) {
			tmp =localDB.searchInHistory(para_uid,para_alias,para_group,para_begin,para_end,para_text);
		} else {
			tmp =localDB.searchInHistory(para_uid,para_alias,para_group,para_end,para_begin,para_text);
		}
	
		if(tmp!=null){
			try {
				return getResultData(tmp); //108
			} catch (SQLException e) {
				LogEngine.log(this, "Couldn't querry any messages",LogEngine.ERROR);
			}
		}
		return null;
	
	}

	
	/**
	 * Diese Methode gibt den aktuellen Status der BackupDB zurück indem
	 * sie die BackupDBConnection-Klasse danach fragt
	 * @return	true 	bereit
	 * @return 	false	nicht bereit
	 */
	public int getStatusBackup() {
		return backupDB.getStatus();
	}


	/**
	 * Diese Methode gibt den aktuellen Status der LocalDB zurück indem
	 * sie die LocalDBConnection-Klasse danach fragt
	 * @return	true 	bereit
	 * @return 	false	nicht bereit
	 */
	public boolean getStatusLocal() {
		return localDB.getStatus();
	}

	/**
	 * Diese Methode wandelt ein gegebenes ResultSet in ein DatabaseDaten-Objekt um und gibt dies zurück
	 * @param rs:	umzuwandelndes ResultSet
	 * @return		Gibt ein DatabaseDaten-Objekt zurück
	 * @throws SQLException
	 */
	private static DatabaseDaten getResultData(ResultSet rs) throws SQLException{

		ResultSetMetaData metaData = rs.getMetaData();

		// names of columns
		int columnCount = metaData.getColumnCount();
		String[] spaüb= new String[columnCount];
		for (int column = 1; column <= columnCount; column++) {
			spaüb[column-1]= metaData.getColumnName(column);
		}

		// data of the table
		rs.last();
		int rows = rs.getRow();
		rs.beforeFirst();

		String[][] stringdata = new String[rows][columnCount];
		while (rs.next()) {
			for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
				Object zelle = rs.getObject(columnIndex);
				stringdata[rs.getRow()-1][columnIndex-1]=(zelle!=null)?zelle.toString():"";
			}
		}

		return new DatabaseDaten(spaüb, stringdata);
	}

	/**
	 * Diese Methode weist die LocalDBConnection-Klasse an alle gespeicherten
	 * User zu übergeben, wandelt diese um und gibt sie als JComboBox zurück
	 * @return
	 */
	public JComboBox<Node> getUsers(){
		try {

			ResultSet tmp = localDB.pull_users();
			if(tmp!=null){
				Vector<Node> nodes = new Vector<Node>();
				Set <Node> testdata = new HashSet<Node>();

				while (tmp.next()) {
					Node tmp_node = new Node(tmp.getLong(4),tmp.getLong(1),tmp.getString(3),tmp.getString(2),tmp.getString(5));
					nodes.add(tmp_node);
					testdata.add(tmp_node);
				}
				DefaultComboBoxModel<Node> tobi = new DefaultComboBoxModel<Node>(nodes);
				JComboBox<Node> tmp_combo = new JComboBox<Node>(tobi);
				tmp_combo.insertItemAt(null, 0);
				tmp_combo.setSelectedItem(null);
				return tmp_combo;


			}else{
				//				System.out.println("tmp war null" );
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage() );
		}
		return new JComboBox<Node>();

	}
	
	/**
	 * Diese Methode weist die Klasse BackupDBConnection an die Configdaten einers bestimmten
	 * Benutzers aus der BackupDatenbank zu lesen und schreibt das ergebnis in die ProgrammConfig
	 * @param user:		Username des besitmmten Benutzers
	 * @param password:	Passwort des besitmmten Benutzers
	 * @return 0:		Probleme mit dem SQL-Server
	 * @return 1:		geladene Config leer
	 * @return 2:		erfolgreich durchgeführt
	 */
	public int getConfig(String user, String password) {
		//load config
		try {
			Properties tmp = backupDB.getConfig(user, password);
			if (tmp!=null) { //load worked
				Config.importConfig(tmp);
				return 2;
			} else
				return 1; //load did return nothing
		} catch (IllegalArgumentException e) {
			return 0;
		}
	}

	/**
	 * Diese Methode weist die Klasse LocalDBConnection.java an
	 * alle Nachichten aus der Config in die Datenbank zu schreiben
	 */
	public void writeConfig(){
		if(localDB.getStatus()) {
			localDB.writeAllSettingsToDB(Config.getNonDefault());
		}
	}

	/**Prüft ob eine Datenbankverbindung mit den angegebenen Parametern möglich ist und wirft entsprechende Fehlercodes
	 * @param ip	Ip oder Hostname des Datenbankserver 
	 * @param port Port des Datenbankserver
	 * @param databasename Zu verbindende Datenbank
	 * @param user Anmeldename für den Datenbankserver
	 * @param password Passwort für den Datenbankserver
	 * @param timeout TODO
	 * @return <table><tr><th>Wert</th><th align="left">Bedeutung</th></tr>
	 * 				 <tr><td align="center">0</td><td>Verbindungsdaten sind korrekt Datenbank hat geantwortet</td></tr>
	 * 				 <tr><td align="center">1</td><td>Server Antwortet nicht </td></tr>
	 * 				 <tr><td align="center">2</td><td>Server Antwortet, Zugangsdaten sind jedoch falsch</td></tr>
	 * 				 <tr><td align="center">3</td><td>Verbindungsdaten sind korrekt, aber angegebener Datenbankname nicht gefunden </td></tr>
	 */
	public int checkCon(String ip, String port,String databasename,String user,String password, long timeout){
		Connection con=null;
		try {
			con = DriverManager.getConnection("jdbc:mysql://"+ip+":"+ port+"/"+databasename+"?connectTimeout="+timeout, user, password);
			return 0;
		} catch (SQLException e) {
			switch(e.getErrorCode()) {
			case 0:
				//host unreachable
				return 1;
			case 1045:
				//access denied
				return 2;
			case 1049:
				//unknown database
				return 3;
			default:
				return 4;
			}
		}finally {
			try {con.close();}catch(Exception ignored) {}
		}
	}


	/**
	 * Diese Methode wandelt ein ResultSet in ein DefaultTableModel um und gibt dieses zurück
	 * @param rs:	umzuwandelndes ResultSet
	 * @return		DefaultTableModel
	 * @throws SQLException
	 */
	public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
		DatabaseDaten tmp = getResultData(rs);

		return new DefaultTableModel(tmp.getData(127),tmp.getHeader(127));

	}
	
	/**
	 * Diese Methode wandelt ein übergebenes DatabaseDaten-Objekt in dein DefaultTableModel um
	 * @param 	dbd
	 * @return	DefaultTableModel
	 * @throws SQLException
	 */
	public static DefaultTableModel buildTableModel(DatabaseDaten dbd) throws SQLException {
		return new DefaultTableModel(dbd.getData(127),dbd.getHeader(127));
		
	}

	/**
	 * Diese Methode weist die BackupDBConnection an, einen nutzer mit bestimmten Username und Passwort zu erstellen
	 * @param username:	Benutzername des zu erstellenden Benutzers
	 * @param password:	Passwort des zu erstellenden Benutzers
	 * @return	0:	BackupDBConnection-Klasse nicht bereit
	 * @return	1:	Anlegen des Benutzers nicht erfolgreich
	 * @return	2:	Benutzername oder Passwort enthält verbotene Zeichen.
	 * @return	3:	Benutzer wurde angelegt	
	 */
	public int createUser(String username, String password) {
		if(!username.matches(Config.getConfig().getNamePattern()) || !password.matches(Config.getConfig().getNamePattern())) return 2;
		if(backupDB.getStatus()>=1){
			if(backupDB.createUser(username, password))
				return 3;
			return 1;
		}
		return 0;
	
	}

	/**
	 * 
	 * @author ATRM
	 * Diese Klasse (bzw.die Run-Methode) wertet den Status der LocalDBConnection aus und lässt diese gegebenenfalls einen 
	 * reconnect auf die lokale Datenbank ausführen.
	 * Zeigt der Status eine erfolgreiche Verbindung an übergibt sie die Daten aus der Nachichten-Queue, der Gruppen-Queue und der Nodes-Queue
	 * um sie in umgekehrter Reihenfolge zum schreiben an die Local-DB-Connection-Klasse zu übergeben.   
	 *
	 */
	private final class DPTransportBot implements Runnable {
		@Override
		public void run() {
			boolean locDBWasConnectetBefore = false;
			while (true) {
				if (!localDB.getStatus()) {
					synchronized (transporter) {
						try {
							if (locDBWasConnectetBefore) {
								localDB.reconnectToLocDBServer();
							}
							transporter.wait();
							locDBWasConnectetBefore = true;
						} catch (InterruptedException e) {
						}
					}
				}
				while (true) {
					// kopiere msgs
					List<MSG> tmp_msg = new ArrayList<MSG>(msg2Store);
					msg2Store.removeAll(tmp_msg);
	
					// kopiere groups
					List<String> tmp_groups = new ArrayList<String>(groups2Store);
					groups2Store.removeAll(tmp_groups);
	
					// kopiere nodes
					List<Node> tmp_nodes = new ArrayList<Node>(node2Store);
					node2Store.removeAll(tmp_nodes);
					
	
					// kopiere routen
					Map<Long, Long> tmp_routes = (NodeEngine.getNE().getRoutes());
//					routes2Store.removeAll(tmp_routes);
	
					// schreibe nodes
					if(!localDB.writeAllUsersToDB(tmp_nodes)){
						failed_node.addAll(tmp_nodes);
						if(!localDB.getStatus()) {
							break;
						}
					}
	
					// schreibe groups
					localDB.writeAllGroupsToDB(tmp_groups);
	
					// schreibe msgs
					boolean all_msgs_written=true;
					for (MSG current : tmp_msg) {
						if(!localDB.writeMsgToDB(current)){
							all_msgs_written=false;
							failed_msgs.add(current);
						}
					}
					if(!all_msgs_written){
						if(!localDB.getStatus()){
							break;
						}
					}
	
					//schreibe alle routen
					for (Long ziel :tmp_routes.keySet()) {
						Long gw = tmp_routes.get(ziel);
						localDB.writeRoutingTableToDB(ziel, gw);
					}
					
	
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	/**
	 * Diese öffentliche Methode 'weckt' den transporterBot der DatabaseEngine
	 */
	public synchronized void go() {
		synchronized (transporter) {
			transporter.notify();
		}
	}


}
