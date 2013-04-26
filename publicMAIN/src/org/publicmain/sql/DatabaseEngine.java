package org.publicmain.sql;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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


	public void go() {
		
		synchronized (transporter) {
			transporter.notify();
		}

		


	}

	
	
	

}
