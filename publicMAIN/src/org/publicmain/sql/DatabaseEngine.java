package org.publicmain.sql;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.publicmain.common.MSG;
import org.publicmain.common.Node;

public class DatabaseEngine {
	private final class nodesToDB implements Runnable {
		@Override
		public void run() {
			while(true){
				Node tmp;
				try {
					tmp = node2Store.take();
					local.writeAllUsersToLocDB(Arrays.asList(tmp));
				} catch (InterruptedException e) {
				}
			}
			
			// TODO prüfen und timeout
			
		}
	}

	private final class msgsToDB implements Runnable {
		@Override
		public void run() {
			
			while(true){
				try {
					local.writeMsgToLocDB(msg2Store.take());
				} catch (InterruptedException e) {
				}
			}
			
			// TODO timeintervall  und prüfung
			
		}
	}

	private LocalDBConnection local;
	private BackupDBConnection backup;
	private BlockingQueue<MSG> msg2Store;
	private BlockingQueue<Node> node2Store;
	
	private HashSet<MSG> stored_msgs;
	private HashSet<Node> stored_nodes;
	
	Thread msgPutter = new Thread(new msgsToDB());
	Thread nodesPutter = new Thread(new nodesToDB());
	
	
	public DatabaseEngine() {
		local = LocalDBConnection.getDBConnection();
		backup = BackupDBConnection.getBackupDBConnection();
		msg2Store = new LinkedBlockingQueue<MSG>();
		node2Store = new LinkedBlockingQueue<Node>();
		
		
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

	
	
	public void push(){
		
	}
	
	public void pull(){
		//TODO: HILFE
	}
	
	
	
	

}
