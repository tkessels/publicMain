package org.publicmain.sql;

import java.security.KeyStore.Entry;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.publicmain.common.MSG;
import org.publicmain.common.Node;

public class DatabaseEngine {
	private LocalDBConnection local;
	private BackupDBConnection backup;
	
	private BlockingQueue<MSG> msg2Store;
	private BlockingQueue<Node> node2Store;
	private BlockingQueue<Map.Entry<Long, Long>> routes2Store;
	
	private HashSet<MSG> failed_msgs;
	private HashSet<Node> stored_nodes;
//	private HashSet<Node> stored_routes;
	
	
	Thread msgPutter = new Thread(new msgsToDB());
	Thread nodesPutter = new Thread(new nodesToDB());
	Thread routesPutter = new Thread(new routesToDB());
	
	
	public DatabaseEngine() {
		local = LocalDBConnection.getDBConnection(this);
		backup = BackupDBConnection.getBackupDBConnection();
		msg2Store = new LinkedBlockingQueue<MSG>();
		node2Store = new LinkedBlockingQueue<Node>();
		routes2Store = new LinkedBlockingQueue<Map.Entry<Long,Long>>();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				//
				msgPutter.start();
				nodesPutter.start();
				routesPutter.start();				
			}
		}).start();
		
		
		
	
		
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
		
	}
	
	public void pull(){
		//TODO: HILFE
	}
	
	private final class routesToDB implements Runnable {
		@Override
		public void run() {
			
			while(true){
				Map.Entry<Long, Long> tmp;
				try {
					tmp = routes2Store.take();
					local.writeRoutingTable_to_t_nodes(tmp.getKey(), tmp.getValue());
				} catch (InterruptedException e) {
				}
			}
			// TODO Timeing und prüfung
			
		}
	}

	private final class nodesToDB implements Runnable {
		@Override
		public void run() {
			while(true){
				Node tmp;
				try {
					tmp = node2Store.take();
//					if (!local.writeAllUsersToLocDB(Arrays.asList(tmp))){
				} catch (InterruptedException e) {
				}
			}
			
			// TODO prüfen und timeout
			
		}
	}

	private final class msgsToDB implements Runnable {
		@Override
		public void run() {
			synchronized (this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
				}
			}
			while(true){
				try {
					MSG tmp=msg2Store.take();
					if(!local.writeMsgToDB(tmp)){
						if(local.getStatus()){
							failed_msgs.add(tmp);
						}else {
							msg2Store.add(tmp);
							synchronized (this) {
								this.wait();
							}
						}
					}
				} catch (InterruptedException e) {
				}
			}
			
			// TODO timeintervall  und prüfung
			
		}

		
	}
	

	public void go() {

		synchronized (msgPutter) {
			msgPutter.notify();
		}
		synchronized (nodesPutter) {
			nodesPutter.notify();
		}
		synchronized (routesPutter) {
			routesPutter.notify();
		}


	}

	
	
	

}
