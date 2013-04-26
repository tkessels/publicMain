package org.publicmain.sql;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JComboBox;
import javax.swing.JTable;

import org.publicmain.common.MSG;
import org.publicmain.common.Node;

public class DatabaseEngine {
	
	
	public synchronized static DatabaseEngine createDatabaseEngine() {
		if(me==null) new DatabaseEngine();
		return me;
	}


	private LocalDBConnection local;
	private BackupDBConnection backup;
	
	private BlockingQueue<MSG> msg2Store;
	private BlockingQueue<Node> node2Store;
	private BlockingQueue<Map.Entry<Long, Long>> routes2Store;
	private BlockingQueue<String> groups2Store; 
	
	private HashSet<MSG> failed_msgs;
	private List<Node> failed_node;
//	private HashSet<Node> stored_nodes;
//	private HashSet<Node> stored_routes;
 
	private static DatabaseEngine me;
	Thread transporter = new Thread(new DPTransportBot());
	
	
	
	private DatabaseEngine() {
		me=this;
		local = LocalDBConnection.getDBConnection(this);
		backup = BackupDBConnection.getBackupDBConnection();
		
		msg2Store = new LinkedBlockingQueue<MSG>();
		node2Store = new LinkedBlockingQueue<Node>();
		routes2Store = new LinkedBlockingQueue<Map.Entry<Long,Long>>();
		groups2Store = new LinkedBlockingQueue<String>();
		
		

		transporter.start();
		
	
		
	}
	
	public void put(MSG x){
		msg2Store.offer(x);
	}
	
	public void put(Node x){
		node2Store.offer(x);
	}

	public void put(Collection<Node> x){
		for (Node node : x) {
			node2Store.offer(node);
		}
	}
	

	public void put(long target, long gateway){
		routes2Store.offer(new AbstractMap.SimpleEntry(target, gateway));
	}
	
	
	public void push(){
		//TODO:
	}
	
	public void pull(){
		//TODO: HILFE
	}
	
	private final class DPTransportBot implements Runnable {
		@Override
		public void run() {
			while (true) {
				if (!local.getStatus()) {
					synchronized (this) {
						try {
							this.wait();
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

					// schreibe nodes
					if(!local.writeAllUsersToDB(tmp_nodes)){
						failed_node.addAll(tmp_nodes);
						if(!local.getStatus()) break;
					}
					// schreibe groups
					local.writeAllGroupsToDB(tmp_groups);
					// schreibe msgs
					boolean all_msgs_written=true;
					for (MSG current : tmp_msg) {
						if(!local.writeMsgToDB(current))
						all_msgs_written=false;
						failed_msgs.add(current);
					}
					if(all_msgs_written&&!local.getStatus())break;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}
	
	/**Queries the local Database for Messages of a selected user (<code>uid</code>) which have been send after the begin date but before the end date. 
	 * Is either of the given dates a negative long the respective field will be ignored. Additionialy the Querry can be further narrowed by giving a search text.
	 * Only messages where the written text contains the text (case insensitiv) will be returned. If the searchtext is an empty String it will be ignored.  
	 * @param uid UserId of User whose messages should be retrieved
	 * @param begin only messages after the given date (in long) will be considered. begin date will be ignored if negative 
	 * @param end only messages before the given date (in long) will be considered. end date will be ignored if negative
	 * @param text only messages containing this text will be shown. text will be ignored if empty string
	 * @return a JTable listing all the messages fitting the given attributes (Maybe the Database Engine will update the Datamodel of the HistoryWindow later)
	 */
	public JTable selectMSGsByUser(long uid,long begin, long end,String text) {
		return null;
	}
	
	/**Queries the local Database for Messages which have been send with the given <code>alias</code> if send after the begin date but before the end date. 
	 * Is either of the given dates a negative long the respective field will be ignored. Additionialy the Querry can be further narrowed by providing a search <code>text</code>.
	 * Only messages where the written text contains the text (case insensitiv) will be returned. If the searchtext is an empty String it will be ignored.  
	 * @param alias used in message
	 * @param begin only messages after the given date (in long) will be considered. begin date will be ignored if negative 
	 * @param end only messages before the given date (in long) will be considered. end date will be ignored if negative
	 * @param text only messages containing this text will be shown. text will be ignored if empty string
	 * @return a JTable listing all the messages fitting the given attributes
	 */	
	public JTable selectMSGsByAlias(String alias,long begin, long end,String text) {
		return null;
	}

	/**Queries the local Database for Messages which have been send within the given <code>group</code> if send after the begin date but before the end date. 
	 * Is either of the given dates a negative long the respective field will be ignored. Additionialy the Querry can be further narrowed by providing a search <code>text</code>.
	 * Only messages where the written text contains the text (case insensitiv) will be returned. If the searchtext is an empty String it will be ignored.  
	 * @param group the message was send to
	 * @param begin only messages after the given date (in long) will be considered. begin date will be ignored if negative 
	 * @param end only messages before the given date (in long) will be considered. end date will be ignored if negative
	 * @param text only messages containing this text will be shown. text will be ignored if empty string
	 * @return a JTable listing all the messages fitting the given attributes
	 */	
	public JTable selectMSGsByGroup(String group,long begin, long end,String text) {
		return null;
	}
	
	public JComboBox getUsers(){
		return new JComboBox<UserIdentifier>();
	}

	public void go() {
		
		synchronized (transporter) {
			transporter.notify();
		}

		


	}
	
	class UserIdentifier{
		public String alias;
		public long uid;
		public String username;
	}

	
	
	

}
