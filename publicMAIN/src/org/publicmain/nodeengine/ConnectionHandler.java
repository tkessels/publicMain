package org.publicmain.nodeengine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
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
	private Hook				hookmanager;

	private Thread				pakets_rein_hol_bot	= new Thread(new Reciever());
	private Thread				pingpongBot			= new Thread(new Pinger());

	/**
	 * 
	 * Statische Methode zum Erzeugen eines ConnectionHandlers mit Übergabeparameter, zu welchem
	 * Node eine Verbindung hergestellt werden soll.
	 * 
	 * @param knoten 
	 * 			Knoten, mit dem die Verbindung hergestellt werden soll.
	 * @throws IOException
	 * 			wenn bei der Erzeugung des Sockets ein IO-Fehler auftritt.
	 */

	public static ConnectionHandler connectTo(Node knoten) throws IOException{
		Socket tmp_socket = null;
		for (InetAddress x : knoten.getSockets()) {
			if (!Node.getMyIPs().contains(x)) {
				tmp_socket = new Socket(x.getHostAddress(),knoten.getServer_port());
				if ((tmp_socket != null) && tmp_socket.isConnected())
				{
					break; // wenn eine Verbindung mit einer der IPs des  Knotenaufgebaut wurden konnte. Hör auf
				}
			}
		}
		ConnectionHandler tmp =new ConnectionHandler(tmp_socket);
		tmp.host_node=knoten;
		return tmp; 
	}

	/**
	 * Konstruktor zum Erstellen eines ConnectionHandlers um einen bestehenden Socket.
	 * 
	 * @param underlying
	 * 			Socket, der von dem ConnectionHandler dekoriert werden soll.
	 * @throws IOException
	 * 			wenn es bei der Einbindung des Sockets zu IO-Fehlern kommt.
	 */
	public ConnectionHandler(Socket underlying) throws IOException {

		ne = NodeEngine.getNE();
		children = new HashSet<Node>();
		groups = new HashSet<String>();
		hookmanager=new Hook();
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
	 * @param paket
	 *            , das zu versendende Paket.
	 * @throws IOException
	 *             , wenn es zu einem Fehler beim Senden auf dem TCP-Socket
	 *             kommt.
	 */
	public synchronized void send(final MSG paket) {
		if (isConnected()) {
			try {
				LogEngine.log(ConnectionHandler.this, "sending", paket);
				line_out.writeUnshared(paket);
				line_out.flush();
			} catch (IOException e) {
				LogEngine.log(ConnectionHandler.this, "failure", paket);
//				System.out.println(e.getMessage());
			}
		} else {
			LogEngine.log(ConnectionHandler.this, "dropped", paket);
		}
	}

	/**
	 * Prüft, ob der eigene Knoten noch verbunden ist.
	 * 
	 * @return <code>true</code> wenn die Verbindung noch besteht, wenn nicht
	 *         <code>false</code>.
	 */
	public boolean isConnected() {
		return ((line != null) && line.isConnected() && !line.isClosed());
	}

	/**
	 * Sendet eine Information über das unmittelbar bevorstehende Herunterfahren
	 * des Nodes an andere Nodes.
	 */
	public void disconnect() {
		send(new MSG(ne.getMe(), MSGCode.NODE_SHUTDOWN));
		close();
	}

	/**
	 * Schliesst die ein- und ausgehenden Verbindungen. Drei try/catch-Blöcke um
	 * zu verhindern, dass nicht bei der Ausführung der ersten Anweisung eine
	 * Exception ausgelöst wird und die weiteren Anweisungen nicht mehr
	 * ausgeführt werden.
	 */
	public void close() {
		me=null;

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
	
	
	
	
	
	
	
	
	
	

	public String toString2() {
		return "ConnectionHandler ["
				+ (children != null ? "children=" + children + ", " : "")
				+ (groups != null ? "groups=" + groups + ", " : "")
				+ (host_node != null ? "host_node=" + host_node + ", " : "")
				+ (ne != null ? "ne=" + ne + ", " : "")
				+ (line != null ? "line=" + line + ", " : "")
				+ (line_out != null ? "line_out=" + line_out + ", " : "")
				+ (line_in != null ? "line_in=" + line_in + ", " : "")
				+ (me != null ? "me=" + me + ", " : "")
				+ (hostname != null ? "hostname=" + hostname + ", " : "")
				+ "latency="
				+ latency
				+ ", "
				+ (hookmanager != null ? "hookmanager=" + hookmanager + ", "
						: "")
				+ (pakets_rein_hol_bot != null ? "pakets_rein_hol_bot="
						+ pakets_rein_hol_bot + ", " : "")
				+ (pingpongBot != null ? "pingpongBot=" + pingpongBot : "")
				+ "]";
	}

	/**
	 * Sendet ein ECHO_REQUEST Paket zum anderen Verbindungsende.
	 */
	private void ping() {
		send(new MSG(null, MSGCode.ECHO_REQUEST));
	}

	/**
	 * 
	 * Methode für das Routen von Gruppen-Nachrichten. Diese Methode speichert,
	 * welche Gruppen auf welcher Verbindung existieren.
	 * 
	 * @param gruppe
	 *            Kollektion von Gruppenstrings, die an dieser Verbindung
	 *            existieren.
	 * @return <code>true</code> wenn sich die Liste durch die Aktion verändert
	 *         hat, anderfalls <code>false</code>
	 */
	public boolean add(Collection<String> gruppe) {
		synchronized (groups) {
			return groups.addAll(gruppe);
		}
	}

	/**
	 * Entfernt eine Kollektion von Gruppenstrings von dieser Verbindung.
	 * 
	 * @param gruppe
	 * 		Kollektion von Gruppenstrings, die von dieser Verbindung entfernt werden sollen.
	 * @return
	 * <code>true</code> wenn sich die Liste durch die Aktion verändert
	 *         hat, anderfalls <code>false</code>
	 */		
	public boolean remove(Collection<String> gruppe) {
		synchronized (groups) {
			return groups.removeAll(gruppe);
		}
	}

	/**
	 * Prüft, ob der eigene Node weitere Child-Nodes hat und liefert
	 * entsprechend <code>true</code> oder <code>false</code> zurück.
	 * 
	 * @param nid
	 * 		NodeID des zu prüfenden Knotens.
	 * @return
	 * 		<code>true</code> wenn der angegebene Knoten Child-Nodes hat, andernfalls <code>false</code>
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
	 * @return
	 * 		Set aller dieser Verbindung zugeordneten Gruppen.
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
	 * 		erzeugt auf ein ping-Paket ein entsprechendes Antwortpaket.
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
	 * 		gibt alle über die Verbindung erreichbaren Knoten zurück.
	 */
	public Set<Node> getChildren() {
		synchronized (children) {
			return children;
		}
	}

	/**
	 * Vergleichbar mit "route add" fügt diese Methode diese Verbindung als
	 * Gateway für alle Nodes aus <code>toAdd</code> hinzu.
	 * 
	 * @param toAdd
	 *            Collection von Nodes, die über diese TCP-Verbindung zu
	 *            erreichen sind.
	 * @return <code>true</code> wenn sich die Liste durch die Aktion verändert
	 *         hat, anderfalls <code>false</code>
	 */
	public boolean addChildren(Collection<Node> toAdd) {
		synchronized (children) {
			return children.addAll(toAdd);
		}
	}

	/**
	 * Entfernt alle angegebenen Knoten von der Verbindung.
	 * 
	 * @param toRemove
	 * 		Kollektion von Nodes, die von dieser Verbindung entfernt werden sollen.
	 * @return
	 * 		<code>true</code> wenn sich die Liste durch die Aktion verändert
	 * 		hat, andernfalls <code>false</code>
	 */
	public boolean removeChildren(Collection<Node> toRemove) {
		synchronized (children) {
			return children.removeAll(toRemove);
		}
	}

	/**
	 * Aktualisiert die Liste der über die Verbindung angebundenen Nodes.
	 * 
	 * @param toSet
	 *            Neue Liste von Nodes.
	 * @return <code>true</code> wenn sich die Liste durch die Aktion verändert
	 *         hat, anderfalls <code>false</code>
	 */
	public boolean setChildren(Collection<Node> toSet) {
		synchronized (children) {
			int oldHash= children.hashCode();
			children.clear();
			children.addAll(toSet);
			return (oldHash!=children.hashCode());

		}
	}
	
	public Hook getHook() {
		return hookmanager;
	}

	/**
	 * Der Reciever-Thread hört auf der Verbindung, ob eingehende Nachrichten oder
	 * Steuerungsanforderungen ankommen und behandelt einige LowLevel
	 * Nachrichten selber, bevor er diese ggf. weiterleitet.
	 */
	class Reciever implements Runnable {
		public void run() {

			while ((me != null) && me.isConnected()) {
				Object readObject = null;
				try {
					readObject = line_in.readUnshared();

					if ((readObject != null) && (readObject instanceof MSG)) {
						MSG tmp = (MSG) readObject;

						if (!hookmanager.check(tmp)) {
							if (tmp.getTyp() == NachrichtenTyp.SYSTEM) {
								switch (tmp.getCode()) {
								case ECHO_REQUEST:
									pong(tmp);
									break;
								case ECHO_RESPONSE:
									latency = System.currentTimeMillis() - (Long) tmp.getData();
									break;
								case NODE_UPDATE:
									me.getChildren().add((Node) tmp.getData());
									if(host_node==null) host_node=(Node) tmp.getData();
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
						}
					} else {
						LogEngine.log(me, "Empfangenes Objekt ist keine MSG");
					}
				} catch (ClassNotFoundException e) {
					LogEngine.log("ConnectionHandler", e);
				} catch (IOException e) {
					LogEngine.log(e);
					break; // wenn ein Empfangen vom Socket nicht mehr möglich
					// ist -> Thread beenden
				}
			}
			if(me!=null) {
				me.close();
			}
		}
	}	

	/**
	 * Der Pinger ist als prüfende Instanz für die Verbindungsgüteprüfung
	 * konzipiert, wird jedoch noch nicht verwendet. Die NodeEngine sollte auf
	 * Grundlage von Verbindungsauslastung und Verbindungsqualität zu einem
	 * spätern Zeitpunkt einzelne Verbindungen austauschen können. (Noch nicht
	 * implementiert)
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
