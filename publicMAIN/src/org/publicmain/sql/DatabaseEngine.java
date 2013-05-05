package org.publicmain.sql;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
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
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.publicmain.common.Config;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.Node;
import org.publicmain.nodeengine.NodeEngine;

import com.mysql.jdbc.CommunicationsException;
import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;

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
	//	private HashSet<Node> stored_nodes;
	//	private HashSet<Node> stored_routes;

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

	public void writeConfig(){
		if(localDB.getStatus()) {
			localDB.writeAllSettingsToDB(Config.getNonDefault());
		}
	}


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

	public synchronized static DatabaseEngine getDatabaseEngine() {
		if(me==null) {
			new DatabaseEngine();
		}
		return me;
	}

	public void put(MSG x){
		msg2Store.offer(x);
	}

	public void put(Node x){
		node2Store.offer(x);
	}
	
	public boolean isValid(String username, String password) {
		return (backupDB.getIDfor(username, password)!=-1);
	}
	
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

	public void put(Collection<Node> x){
		for (Node node : x) {
			node2Store.offer(node);
		}
	}

	public void put(String group){
		groups2Store.add(group);
	}


	public void put(long target, long gateway){
		routes2Store.offer(new AbstractMap.SimpleEntry(target, gateway));
	}

	public void deleteLocalHistory() {
		localDB.deleteAllMsgs();
	}



	/**
	 * Diese Methode f�hrt nach Statuspr�fung die Methode  deleteAllMessages() auf der backupDB aus. 
	 */
	public void deleteBackupMessages() {
		if(backupDB.getStatus()==2){
			backupDB.deleteAllMessages();
		}
	}

	public int deleteBackupUserAccount(String username, String password) {
		if(backupDB.getStatus()>=1){
			if (backupDB.deleteUser(username,password))
				return 2;
			return 1;
		}
		return 0;
	}

	public void push(){
		if(localDB.getStatus()&&(backupDB.getStatus()==2)){
			long id = backupDB.getMyID();
			backupDB.push_users(localDB.pull_users(),id);
			backupDB.push_msgs(localDB.pull_msgs(),id);
			backupDB.push_settings(localDB.pull_settings(),id);
		}
	}

	public void pull(){
		if(localDB.getStatus()&&(backupDB.getStatus()==2)){
			long id = backupDB.getMyID();
			localDB.push_users(backupDB.pull_users(id));
			localDB.push_msgs(backupDB.pull_msgs(id));
			localDB.push_settings(backupDB.pull_settings(id));
		}
	}

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
					List<Map.Entry<Long, Long>> tmp_routes = new ArrayList<Map.Entry<Long, Long>>(routes2Store);
					routes2Store.removeAll(tmp_routes);

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
					for (Entry<Long, Long> tmp_route : tmp_routes){
						if(!localDB.writeRoutingTableToDB(tmp_route.getKey(),NodeEngine.getNE().getNode(tmp_route.getKey()).getHostname(), NodeEngine.getNE().getUIDforNID(tmp_route.getKey()), tmp_route.getValue())){
							failed_routes.add(tmp_route);
							break;
						}
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


	public int getStatusBackup() {
		return backupDB.getStatus();
	}


	private static DatabaseDaten getResultData(ResultSet rs) throws SQLException{

		ResultSetMetaData metaData = rs.getMetaData();

		// names of columns
		int columnCount = metaData.getColumnCount();
		String[] spa�b= new String[columnCount];
		for (int column = 1; column <= columnCount; column++) {
			spa�b[column-1]= metaData.getColumnName(column);
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

		return new DatabaseDaten(spa�b, stringdata);
	}

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
	
	/**Pr�ft ob eine Datenbankverbindung mit den angegebenen Parametern m�glich ist und wirft entsprechende Fehlercodes
	 * @param ip	Ip oder Hostname des Datenbankserver 
	 * @param port Port des Datenbankserver
	 * @param databasename Zu verbindende Datenbank
	 * @param user Anmeldename f�r den Datenbankserver
	 * @param password Passwort f�r den Datenbankserver
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


	public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
		DatabaseDaten tmp = getResultData(rs);

		return new DefaultTableModel(tmp.getZelleninhalt(127),tmp.getSpalten�berschriften(127));

	}
	public static DefaultTableModel buildTableModel(DatabaseDaten dbd) throws SQLException {
		return new DefaultTableModel(dbd.getZelleninhalt(127),dbd.getSpalten�berschriften(127));
		
	}

	public synchronized void go() {
		synchronized (transporter) {
			transporter.notify();
		}
	}

	public int createUser(String username, String password) {
		if(!username.matches(Config.getConfig().getNamePattern()) || !password.matches(Config.getConfig().getNamePattern())) return 2;
		if(backupDB.getStatus()>=1){
			if(backupDB.createUser(username, password))
				return 3;
			return 1;
		}
		return 0;

	}

	public boolean getStatusLocal() {
		return localDB.getStatus();
	}


}
