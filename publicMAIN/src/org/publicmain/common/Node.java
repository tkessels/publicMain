package org.publicmain.common;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

public class Node implements Serializable {
	private static Node me;

	private long nodeID;
	private long userID;
	private String alias;
	private InetAddress[] sockets;
	private String hostname;
	private boolean isRoot;
	
	private Node() {
		Random myrnd = new Random();
		nodeID = myrnd.nextLong();
		userID = myrnd.nextLong(); //noch zufällig später aus config
		alias = System.getProperties().getProperty("user.name");
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public static Node getMe(){
		if(me==null)me=new Node();
		return me;
	}

	public long getNodeID() {
		return nodeID;
	}

	public void setNodeID(long nodeID) {
		this.nodeID = nodeID;
	}

	public long getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public InetAddress[] getSockets() {
		return sockets;
	}

	public void setSockets(InetAddress[] sockets) {
		this.sockets = sockets;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public boolean isRoot() {
		return isRoot;
	}

	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

}
