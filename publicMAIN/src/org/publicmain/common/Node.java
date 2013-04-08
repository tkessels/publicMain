package org.publicmain.common;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.nodeengine.NodeEngine;

/**
 * @author ATRM
 * 
 */

public class Node implements Serializable {

	private static Node me;
	private long nodeID;
	private long userID;
	private String alias;
	private List<InetAddress> sockets;
	private String hostname;
	private int server_port;
	
	public Node() {
		nodeID = NodeEngine.getNE().getNodeID();
		userID = ChatEngine.getCE().getUserID();
		this.alias = ChatEngine.getCE().getAlias();
		
		sockets=getMyIPs();
		server_port = NodeEngine.getNE().getServer_port();
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
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

	public List<InetAddress> getSockets() {
		return sockets;
	}


	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
/*
	public boolean isRoot() {
		return isRoot;
	}

	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}
	*/
	/**Erzeugt eine Liste aller lokal vergebenen IP-Adressen mit ausnahme von Loopbacks und IPV6 Adressen
	 * @return Liste aller lokalen IPs
	 */
	public static List<InetAddress> getMyIPs() {
		List<InetAddress> addrList = new ArrayList<InetAddress>();
		try {
			for (InetAddress inetAddress : InetAddress.getAllByName(InetAddress.getLocalHost().getHostName())) { //Finde alle IPs die mit meinem hostname assoziert sind und 
			if (inetAddress.getAddress().length==4)addrList.add(inetAddress);									 //füge die meiner liste hinzu die IPV4 sind also 4Byte lang
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return addrList;
	}

	public int getServer_port() {
		return server_port;
	}
	
	@Override
	public String toString() {
		// 
		return alias+"@"+hostname;
	}


	/* Liefert  Hashcode des Knoten über die beiden eindeutigen IDs 
	 * 
	 * Wird für die Haltung der Nodes in einem Hashset benötigt.
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (nodeID ^ (nodeID >>> 32));	//da die NodeID 64Bit LONG ist und der Hash nur 32 bit INTus hat (omg ;) 
		result = prime * result + (int) (userID ^ (userID >>> 32)); 		//werden hier die beiden 32 bit Hälften der IDs mit OR übereinander gelegt und zusammen gerechnet.
		return result;																					//die Primzahl spreizt das ergebnis ausserdem sind Primzahlen total toll und sollten überall drin sein.
	}

	/* 
	 * Liefert true wenn zwei Knoten die sowohl die Gleiche UserID als auch NodeID haben.
	 * Allerdings nur wenn beide Nodes auch Nodes sind. 
	 * Ist das Vergleichsobjekt kein Node gehen wir davon aus, dass es eine andere NodeID hätte und der User gerade in Vermont zum shoppen ist. (=Ungleicheit) 
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Node other = (Node) obj;
		if (nodeID != other.nodeID) return false;
		if (userID != other.userID) return false;
		return true;
	}
	
	

	

}
