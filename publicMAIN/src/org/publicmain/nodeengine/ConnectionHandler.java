package org.publicmain.nodeengine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.publicmain.common.Config;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.NachrichtenTyp;
import org.publicmain.common.Node;

/**
 * Hüll-Klasse für einen TCP-Socket zur Übertragung von Message-Objekten.
 *  
 * @author ATRM
 * 
 */

public class ConnectionHandler {
	private Set<Node>			children;
	private Set<String>			groups;
	public Node					host_node;
	private NodeEngine			ne;
	private Socket				line;
	private ObjectOutputStream	line_out;
	private ObjectInputStream	line_in;
	private volatile ConnectionHandler	me;
	private String				hostname;
	private long					latency				= Integer.MAX_VALUE;

	private Thread				pakets_rein_hol_bot	= new Thread(new Reciever());
	private Thread				pingpongBot			= new Thread(new Pinger());

	/**
	 * TODO: Konstruktor-Kommentar, was wird hier grob gemacht?
	 * 
	 * @param underlying
	 * @throws IOException
	 */
	public ConnectionHandler(Socket underlying) throws IOException {

		ne = NodeEngine.getNE();
		children = new HashSet<Node>();
		groups = new HashSet<String>();
		line = underlying;
		line.setTcpNoDelay(true);
		line.setKeepAlive(true);
		line.setSoTimeout(0);
		line_out = new ObjectOutputStream(new BufferedOutputStream(line.getOutputStream()));
		line_out.flush();
		line_in = new ObjectInputStream(new BufferedInputStream(line.getInputStream()));
		hostname = line.getInetAddress().getHostAddress();
		ping();
		if (Config.getConfig().getPingEnabled()) {
			pingpongBot.start();
		}
		LogEngine.log(this, "Verbunden");
		me = this;
		pakets_rein_hol_bot.start();
	}

	/**
	 * Verschickt ein MSG-Objekt über den Socket.
	 * 
	 * @param paket, das zu versendende Paket
	 * @throws IOException, wenn es zu einem Fehler beim Senden auf dem TCP-Socket kommt
	 */
	public synchronized void send(final MSG paket) {
		if (isConnected()) {
			try {
				LogEngine.log(ConnectionHandler.this, "sending", paket);
				line_out.writeUnshared(paket);
				line_out.flush();
			} catch (IOException e) {
				LogEngine.log(ConnectionHandler.this, "failure", paket);
				System.out.println(e.getMessage());
			}
		} else
			LogEngine.log(ConnectionHandler.this, "dropped", paket);
	}

	/**
	 * Prüft ob der eigene Node noch verbunden ist.
	 * 
	 * @return <code>true</code> wenn die Verbindung noch besteht <code>false</code> wenn nicht
	 */
	public boolean isConnected() {
		return (line != null && line.isConnected() && !line.isClosed());
	}

	/**
	 * Sendet eine Information über das unmittelbar bevorstehende herunterfahren des Nodes an
	 * andere Nodes.
	 */
	public void disconnect() {
		send(new MSG(ne.getMe(), MSGCode.NODE_SHUTDOWN));
		close();
	}
	
	/**
	 * Schliesst die ein- und ausgehenden Verbindungen. Drei try/catch-Blöcke um zu verhindern,
	 * dass nicht bei der ausführung der ersten Anweisung eine Exception ausgelöst wird und
	 * die weiteren Anweisungen nicht mehr ausgeführt werden.
	 */
	public void close() {

		try {
			line_out.close();
		} catch (IOException e) {
		}
		try {
			line_in.close();
		} catch (IOException e) {
		}
		try {
			line.close();
		} catch (IOException e) {
		}
		LogEngine.log(this.toString(), "closed", LogEngine.INFO);
		me = null;
		// TODO: Entscheidung - Tobi
		// pakets_rein_hol_bot.stop();
		pakets_rein_hol_bot = null;
		ne.remove(this);
	}
	
	/**
	 * Eigene toString-Methode.
	 */
	public String toString() {
		return "ConnectionHandler [" + hostname + "]"
				+ ((latency < 10000) ? "[" + latency + "]" : "");
	}
	
	/**
	 * Methode zum anpingen anderer Nodes, durch den MSGCode sendet der andere Node
	 * eine Anwort.
	 */
	private void ping() {
		send(new MSG(null, MSGCode.ECHO_REQUEST));
	}
	
	/**
	 * TODO: Überprüfen!
	 * 
	 * Methode für das routen von Gruppen-Nachrichten. Diese Methode speichert welche
	 * Gruppen auf welcher Verbindung existieren.
	 * 
	 * @param gruppe
	 * @return
	 */
	public boolean add(Collection<String> gruppe) {
		synchronized (groups) {
			return groups.addAll(gruppe);
		}
	}
	
	/**
	 * TODO: Kommentar!
	 * 
	 * @param gruppe
	 * @return
	 */
	public boolean remove(Collection<String> gruppe) {
		synchronized (groups) {
			return groups.removeAll(gruppe);
		}
	}
	
	/**
	 * Prüft ob der eigene Nodes weitere Child-Nodes hat und liefert entsprechend
	 * <code>true</code> oder <code>false</code> zurück.
	 * 
	 * @param nid
	 * @return
	 */
	public boolean hasChild(final long nid) {
		synchronized (children) {
			for (Node x : children)
				if (x.getNodeID() == nid)
					return true;
			return false;
		}

	}			
		
	/**
	 * Getter für die Gruppenliste, hier als Set von Strings.
	 * 
	 * @return, Set<String>
	 */
	public Set<String> getGroups() {
		synchronized (groups) {
			return groups;
		}
	}
	
	/**
	 * Sendet ein ECHO.RESPONSE.
	 * 
	 * @param ping
	 */
	private void pong(final MSG ping) {
		new Thread(new Runnable() {
			public void run() {
				send(new MSG(ping.getTimestamp(), MSGCode.ECHO_RESPONSE));
			}
		}).start();
	}

	/**
	 * Getter für die Child-Nodes.
	 * 
	 * @return
	 */
	public Set<Node> getChildren() {
		synchronized (children) {
			return children;
		}
	}
	
	/**
	 * TODO: Überprüfen!
	 * 
	 * Methode zum routen von Steuerungsanforderungen, beispielsweise <code>allNodesUpdate</code>.
	 * 
	 * @param toAdd
	 * @return
	 */
	public boolean addChildren(Collection<Node> toAdd) {
		synchronized (children) {
			return children.addAll(toAdd);
		}
	}

	/**
	 * Entfernt ein Child-Node vom eigenen Node. 
	 * 
	 * @param toRemove
	 * @return
	 */
	public boolean removeChildren(Collection<Node> toRemove) {
		synchronized (children) {
			return children.removeAll(toRemove);
		}
	}
	
	/**
	 * TODO: Kommentar!
	 * 
	 * @param toSet
	 * @return
	 */
	public boolean setChildren(Collection<Node> toSet) {
		synchronized (children) {
			int oldHash= children.hashCode();
			children.clear();
			children.addAll(toSet);
			return (oldHash!=children.hashCode());
			
		}
	}

	/**
	 * TODO: Überprüfen!
	 * 
	 * Der Reciever-Thread hört auf den Verbindungen ob eingende Nachrichten
	 * oder Steuerungsanforderungen ankommen und nach Typ beantwortet oder
	 * weitergeleitet werden müssen.
	 */
	class Reciever implements Runnable {
		public void run() {

			while (me != null && me.isConnected()) {
				Object readObject = null;
				try {
					readObject = line_in.readUnshared();

					if (readObject != null && readObject instanceof MSG) {
						MSG tmp = (MSG) readObject;
						if (tmp.getTyp() == NachrichtenTyp.SYSTEM) {
							switch (tmp.getCode()) {
							case ECHO_REQUEST:
								pong(tmp);
								break;
							case ECHO_RESPONSE:
								latency = System.currentTimeMillis()
										- (Long) tmp.getData();
								break;
							case NODE_UPDATE:
								me.getChildren().add((Node) tmp.getData());
							default:
								try {
									ne.handle(tmp, me);
								} catch (Exception e) {
									LogEngine.log("handle", e);
								}
							}
						} else {
							try {
								ne.handle(tmp, me);
							} catch (Exception e) {
								LogEngine.log("handle", e);
							}

						}
					} else
						LogEngine.log(me, "Empfangenes Objekt ist keine MSG");
				} catch (ClassNotFoundException e) {
					LogEngine.log("ConnectionHandler", e);
				} catch (IOException e) {
					LogEngine.log(e);
					break; // wenn ein Empfangen vom Socket nicht mehr möglich
							// ist -> Thread beenden
				}
			}
			close();
		}
	}	

	/**
	 * TODO: Kommentar!
	 *
	 */
	class Pinger implements Runnable {
		private final long PING_INTERVAL = Config.getConfig().getPingInterval();

		public void run() {
			hostname = line.getInetAddress().getHostName();
			while (isConnected()) {
				try {
					ping();
					Thread.sleep((long) (PING_INTERVAL * (1 + Math.random())));
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
