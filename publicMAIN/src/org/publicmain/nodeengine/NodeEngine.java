package org.publicmain.nodeengine;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.NachrichtenTyp;
import org.publicmain.common.Node;

/**
 * Die NodeEngine ist für die Verbindungen zu anderen Nodes zuständig. Sie verwaltet die bestehenden Verbindungen, sendet Nachichten und Datein und ist für das Routing zuständig
 */
public class NodeEngine {
	protected static final long			CONNECTION_TIMEOUT		= 1000;									//Timeout bis der Node die Suche nach anderen Nodes aufgibt und sich zum Root erklärt
	protected static final long			ROOT_ANNOUNCE_TIMEOUT	= 2000;									//Zeitspanne die ein Root auf Root_Announces wartet um zu entscheiden wer ROOT bleibt. 
	private final InetAddress			group					= InetAddress.getByName("230.223.223.223"); //Default MulticastGruppe für Verbindungsaushandlung
	private final int					multicast_port			= 6789;									//Default Port für MulticastGruppe für Verbindungsaushandlung
	private final int					MAX_CLIENTS				= 5;										//Maximale Anzahl anzunehmender Verbindungen														

	private static volatile NodeEngine	ne;																//Statischer Zeiger auf einzige Instanz der NodeEngine
	private Node						meinNode;															//die NodeRepräsentation dieser NodeEngine
	private ChatEngine					ce;																//Zeiger auf parent ChatEngine

	private ServerSocket				server_socket;														//Server Socket für eingehende Verbindungen (Passiv/Childs)
	private ConnectionHandler			root_connection;													//TCP Socket zur Verbindung mit anderen Knoten (Aktiv/Parent/Root)
	private MulticastSocket				multi_socket;														//Multicast/Broadcast UDP-Socket zu Verbindungsaushandlung
	public List<ConnectionHandler>		connections;														//Liste bestehender Childverbindungen in eigener HüllKlasse
	private Set<String>					groups;															//Liste aller abonierten Gruppen

	private BlockingQueue<MSG>			root_announce_stash;												//Queue für Bewerberpakete bei Neuaushandlung vom Root-Status 
	private Set<Node>					allNodes;															//Alle dieser Nodenginge bekannten Knotten (sollten alle sein)

	private boolean						root;																//Dieser Knoten möchte Wurzel sein (und benimmt sich auch so)
	private boolean						online;															//Dieser Knoten möchte an sein und verbunden bleiben (signalisiert allen Threads wenn die Anwendung beendet wird)

	private Thread						multicastRecieverBot;												//Thread zum annehmen und verarbeiten der Multicast-Pakete
	private Thread						connectionsAcceptBot;												//Thread akzeptiert und schachtelt eingehen Verbindungen auf dem ServerSocket
	private Thread						discoverGame;														//Thread zur Aushandlung neuer Root Stellung Wenn der Baum segmentiert wurde 
	
	private List<Hook> hooks=new ArrayList<Hook>(); 

	public NodeEngine(ChatEngine parent) throws IOException {
		allNodes = new HashSet<Node>();
		groups = new HashSet<String>();
		connections = new ArrayList<ConnectionHandler>();
		ne = this;
		ce = parent;
		online = true;

		server_socket = new ServerSocket(0);
		multi_socket = new MulticastSocket(multicast_port);
		multi_socket.joinGroup(group);
		multi_socket.setLoopbackMode(true);
		multi_socket.setTimeToLive(10);

		meinNode = Node.getMe();
		allNodes.add(meinNode);

		LogEngine.log(this, "Multicast Socket geöffnet", LogEngine.INFO);

		connectionsAcceptBot = new Thread(new ConnectionsAccepter());
		connectionsAcceptBot.start();

		multicastRecieverBot = new Thread(new MulticastReciever());
		multicastRecieverBot.start();

		discover();

	}

	public static NodeEngine getNE() {
		return ne;
	}

	/**
	 * getMe() gibt das eigene NodeObjekt zurück
	 */
	public Node getME() {
		return meinNode; //TODO:nur zum test
	}

	private Node getBestNode() {
		// TODO Intelligente Auswahl des am besten geeigneten Knoten mit dem sich der Neue Verbinden darf.

		return meinNode;
	}

	/**
	 * isConnected() gibt "true" zurück wenn die laufende Nodeengin hochgefahren und mit anderen Nodes verbunden oder root ist, "false" wenn nicht.
	 */
	/*
	 * public boolean isOnline(){ return online; }
	 */

	private boolean hasChildren() {
		return connections.size() > 0;
	}

	/**
	 * isRoot() gibt "true" zurück wenn die laufende Nodeengin Root ist und "false" wenn nicht.
	 */
	public boolean isRoot() {
		return root || !hasParrent();
	}

	public boolean hasParrent() {
		return (root_connection != null && root_connection.isConnected());
	}

	/**
	 * getNodes() gibt ein NodeArray zurück welche alle verbundenen Nodes beinhaltet.
	 */
	public Set<Node> getNodes() {
		return allNodes;
	}

	public int getServer_port() {
		return server_socket.getLocalPort();
	}

	/**
	 * Gibt ein StringArray aller vorhandenen Groups zurück
	 * 
	 */
	public String[] getGroups() {
		String[] grouparray = { "public", "GruppeA", "GruppeB" };
		return grouparray; //TODO:to implement
	}

	private void sendroot(MSG msg) {
		if (root_connection != null && root_connection.isConnected()) root_connection.send(msg);
	}

	/**
	 * versendet Daten vom Typ MSG an ZielNodes oder git diese an send_file() weiter. prüft Dateigröße (wenn < 5MB aufruf der send_file() sonst als Msg-Type Data) send_file() wird sowohl das Ziel als auch die Daten mitgegeben. D.h., dass das
	 * MSG-Paket hier in File und destination geteilt.
	 */
	private void sendmutlicast(MSG nachricht) {
		byte[] buf = MSG.getBytes(nachricht);
		LogEngine.log(this, "sende", nachricht);
		try {
			if (buf.length < 65000) multi_socket.send(new DatagramPacket(buf, buf.length, group, 6789));
			else LogEngine.log(this, "MSG zu groß für UDP-Paket", LogEngine.ERROR);
		}
		catch (IOException e) {
			LogEngine.log(e);
		}
	}

	private void sendunicast(MSG msg, Node newRoot) {
		byte[] data = MSG.getBytes(msg);
		for (InetAddress x : newRoot.getSockets()) {
			DatagramPacket unicast = new DatagramPacket(data, data.length, x, multicast_port);//über Unicast
			try {
				multi_socket.send(unicast);
			}
			catch (IOException e) {
				LogEngine.log(e);
			}
		}
	}

	public void sendtcp(MSG nachricht) {
		if (!isRoot()) root_connection.send(nachricht); //vielleicht sendroot verwenden?
		for (ConnectionHandler x : connections)
			x.send(nachricht);
	}

	private void sendtcpexcept(MSG msg, ConnectionHandler ch) {
		if (!isRoot() && root_connection != ch) root_connection.send(msg);
		for (ConnectionHandler x : connections)
			if (x != ch) x.send(msg);
	}

	/**
	 * versendet Datein über eine TCP-Direktverbindung wird nur von send() aufgerufen nachdem festgestellt wurde, dass nachicht > 5MB
	 */

	public void send_file(String destination) {
		// bekommt ziel und FILE übergeben

	}

	private void discover(Node newRoot) {
		sendunicast(new MSG(meinNode, MSGCode.ROOT_DISCOVERY), newRoot);
	}

	private void discover() {
		sendmutlicast(new MSG(meinNode, MSGCode.ROOT_DISCOVERY));
		new Thread(new RootMe()).start();
	}

	private void sendDiscoverReply(Node quelle) {
		LogEngine.log(this, "sending Replay to " + quelle.toString(), LogEngine.INFO);
		sendunicast(new MSG(getBestNode(), MSGCode.ROOT_REPLY), quelle);
	}

	private void updateNodes() {
		if (isRoot()) {
			synchronized (allNodes) {
				allNodes.clear();
				allNodes.add(getME());
				allNodes.addAll(getChilds());
				allNodes.notify();
			}
		}
		else sendroot(new MSG(null, MSGCode.POLL_ALLNODES));
	}

	private void pollChilds() {
		for (ConnectionHandler x : connections) {
			x.send(new MSG(null, MSGCode.POLL_CHILDNODES));
		}
	}

	private Set<Node> getChilds() {
		Set<Node> rück = new HashSet<Node>();
		for (ConnectionHandler x : connections)
			rück.addAll(x.children);
		return rück;
	}

	/**
	 * Stelle Verbindung mit diesem <code>NODE</code> her!!!!
	 * 
	 * @param knoten
	 *            der Knoten
	 * @throws IOException
	 *             Wenn der hergestellte Socket
	 */
	private void connectTo(Node knoten) {

		Socket tmp_socket = null;
		for (InetAddress x : knoten.getSockets()) {
			try {
				tmp_socket = new Socket(x.getHostAddress(), knoten.getServer_port());

			}
			catch (UnknownHostException e) {
				LogEngine.log(e);
			}
			catch (IOException e) {
				LogEngine.log(e);
			}
			if (tmp_socket != null && tmp_socket.isConnected()) break;
		}
		if (tmp_socket != null) {
			try {
				root_connection = new ConnectionHandler(tmp_socket);
			}
			catch (IOException e) {
				LogEngine.log(e);
			}
			sendroot(new MSG(getME()));
			sendroot(new MSG(null, MSGCode.POLL_ALLNODES));
		}
	}

	public void disconnect() {
		online = false;
		sendtcp(new MSG(meinNode, MSGCode.NODE_SHUTDOWN));
		root_connection.disconnect();
		for (final ConnectionHandler con : connections)
			(new Thread(new Runnable() {
				public void run() {
					con.disconnect();
				}
			})).start();// Threadded Disconnect für jede Leitung
		remove(root_connection);
	}

	/**
	 * Entfernt eine Verbindung wieder
	 * 
	 * @param conn
	 */
	public void remove(ConnectionHandler conn) {
		if (conn == root_connection) {
			root_connection = null;
			if (online == true) sendmutlicast(new MSG(allNodes, MSGCode.ROOT_ANNOUNCE));
		}
		else {
			connections.remove(conn);
			sendtcp(new MSG(conn.children, MSGCode.CHILD_SHUTDOWN));
		}
		updateNodes();
	}

	private synchronized void discover_game(MSG paket) {
		root_announce_stash.offer(paket);
		if (discoverGame == null) {
			Object[] payload = { allNodes, meinNode };
			sendmutlicast(new MSG(payload, MSGCode.ROOT_ANNOUNCE));
			discoverGame = new Thread(new DiscoverGame());
		}
	}

	/**
	 * Hier wird das Paket verarbeitet und weitergeleitet. Diese Methode wird ausschließlich vom MulticastSocketHandler aufegrufen.
	 * 
	 * @param paket
	 *            Das empfangene MulticastPaket
	 */
	public void handleMulticast(MSG paket) {
		LogEngine.log(this, "handling [MC]", paket);
		if (paket.getTyp() == NachrichtenTyp.SYSTEM) {
			switch (paket.getCode()) {
				case ROOT_REPLY:
					if (online && isRoot()) {
						connectTo((Node) paket.getData());
					}
					break;
				case ROOT_DISCOVERY:
					if (isRoot()) sendDiscoverReply((Node) paket.getData());
					break;
				case ROOT_ANNOUNCE:
					discover_game(paket);
					break;
				default:
					LogEngine.log(this, "handling [MC]:undefined", paket);
			}
		}
	}

	/**
	 * Hier wird das Paket verarbeitet und weitergeleitet. Diese Methode wird ausschließlich von den ConnectionHandlern aufgerufen um empfange Pakete verarbeiten zu lassen.
	 * 
	 * @param paket
	 *            Zu verarbeitendes Paket
	 * @param quelle
	 *            Quelle des Pakets
	 */
	public void handle(MSG paket, ConnectionHandler quelle) {
		LogEngine.log(this, "handling[" + quelle + "]", paket);
		if (hook(paket)) return;
		switch (paket.getTyp()) {
			case GROUP:
				sendtcpexcept(paket, quelle);
				ce.put(paket);
				break;
			case SYSTEM:
				switch (paket.getCode()) {
					case NODE_UPDATE:
						synchronized (allNodes) {
							allNodes.add((Node) paket.getData());
							allNodes.notifyAll();
						}
						if (quelle != root_connection) quelle.children.add((Node) paket.getData());
						sendtcpexcept(paket, quelle);
						//				else LogEngine.log("NODE_UPDATE von root bekommen", this, LogEngine.WARNING);
						break;

					case POLL_ALLNODES:
						if (root_connection != quelle) quelle.send(new MSG(allNodes, MSGCode.REPORT_ALLNODES));
						//				else LogEngine.log("POLL_ALLNODES von root bekommen", this, LogEngine.WARNING);
						break;
					case REPORT_ALLNODES:
						if (quelle == root_connection) {
							synchronized (allNodes) {
								allNodes.clear();
								allNodes.addAll((Set<Node>) paket.getData());
								allNodes.add(meinNode);
								allNodes.notifyAll();
							}
						}
						//				else LogEngine.log("REPORT_ALLNODES von komischer Quelle bekommen:", this, LogEngine.WARNING);
						break;
					case POLL_CHILDNODES:
						if (quelle == root_connection) {
							Set<Node> tmp = new HashSet<Node>();
							for (ConnectionHandler x : connections)
								tmp.addAll(x.children);
							sendroot(new MSG(tmp, MSGCode.REPORT_CHILDNODES));
						}
						//				else LogEngine.log("POLL_CHILDNODES von komischer Quelle bekommen:", this, LogEngine.WARNING);
						break;
					case REPORT_CHILDNODES:
						if (quelle != root_connection) {
							quelle.children.clear();
							quelle.children.addAll((Set<Node>) paket.getData());
						}
						//				else LogEngine.log(this,"handling ",paket);
						break;
					case NODE_SHUTDOWN:
						//sendtcpexcept(new MSG(quelle.children,MSGCode.CHILD_SHUTDOWN), quelle);
						synchronized (allNodes) {
							allNodes.removeAll(quelle.children);
							allNodes.notifyAll();
						}
						quelle.close();
						break;

					case CHILD_SHUTDOWN:
						if (quelle != root_connection) quelle.children.removeAll((Collection<Node>) paket.getData());
						synchronized (allNodes) {
							allNodes.removeAll((Collection<Node>) paket.getData());
							allNodes.notifyAll();
						}
						sendtcpexcept(paket, quelle);

						break;
					case NODE_LOOKUP:
						quelle.send(MSG.createReply(paket));
						break;
					default:
						LogEngine.log(this, "handling[" + quelle + "]:undefined", paket);
						break;
				}
				break;
			case DATA:
				break;
			default:
		}
	}

	private boolean hook(MSG paket) {
		boolean tmp = false;
		for (Hook x : hooks) tmp |= x.check(paket);
		return tmp;
	}

	

	/**
	 * Definiert diesen Node nach einem Timeout als Wurzelknoten falls bis dahin keine Verbindung aufgebaut wurde.
	 */
	private final class RootMe implements Runnable {
		public void run() {
			try {
				Thread.sleep(CONNECTION_TIMEOUT);
			}
			catch (InterruptedException e) {
			}
			if (!hasParrent()) {
				root = true;
			}
		}
	}

	/**
	 * Warte eine gewisse Zeit und Werte dann alle gesammelten RoOt_AnOunCEs aus forder anschließend vom Gewinner einen Knoten zum Verbinden an. Wenn der Knoten selber Gewonnen hat
	 */
	private final class DiscoverGame implements Runnable {
		public void run() {
			long until = System.currentTimeMillis() + ROOT_ANNOUNCE_TIMEOUT;
			while (System.currentTimeMillis() < until)
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
				}
			Node toConnectTo = meinNode;
			int maxPenunte = allNodes.size();
			MSG[] ra_stash = (MSG[]) root_announce_stash.toArray(); //lokale Kopie aller RA um Nachzügler zu ignorieren
			for (MSG x : ra_stash) {
				@SuppressWarnings("unchecked")
				// extrahieren aller informationen dieses ROOTANNOUNCE
				Set<Node> tmp_allnodes = (Set<Node>) ((Object[]) x.getData())[0]; // Cast Payload in ein Object Array und das 1. Object in ein Set aus Nodes
				Node tmp_node = (Node) ((Object[]) x.getData())[1]; //	Cast Payload in ein Object Array und das 2. Object dieses Arrays in einen Node
				if (tmp_allnodes.size() > maxPenunte || ((tmp_allnodes.size() == maxPenunte) && (tmp_node.getNodeID() > toConnectTo.getNodeID()))) {
					toConnectTo = tmp_node;
					maxPenunte = tmp_allnodes.size();
				}
			}
			if (toConnectTo != meinNode) discover(toConnectTo);
			try {
				Thread.sleep(ROOT_ANNOUNCE_TIMEOUT);
			}
			catch (InterruptedException e) {
			}
			root_announce_stash.clear();
			discoverGame = null;
		}
	}

	private final class MulticastReciever implements Runnable {
		public void run() {
			while (true) {
				byte[] buff = new byte[65535];
				DatagramPacket tmp = new DatagramPacket(buff, buff.length);
				try {
					multi_socket.receive(tmp);
					MSG nachricht = MSG.getMSG(tmp.getData());
					LogEngine.log("multicastRecieverBot", "multicastRecieve", nachricht);
					handleMulticast(nachricht);
				}
				catch (IOException e) {
					LogEngine.log(e);
				}
			}
		}
	}

	private final class ConnectionsAccepter implements Runnable {
		public void run() {
			while (online && connections.size() <= MAX_CLIENTS) {
				System.out.println();
				LogEngine.log("ConnectionsAccepter", "Listening on Port:" + server_socket.getLocalPort(), LogEngine.INFO);
				try {
					ConnectionHandler tmp = new ConnectionHandler(server_socket.accept());
					connections.add(tmp);
					tmp.isConnected();

				}
				catch (IOException e) {
					LogEngine.log(e);
				}
			}
		}
	}

	public Node retrieve(long nid) {
		Hook x = new Hook(NachrichtenTyp.SYSTEM,MSGCode.NODE_LOOKUP_REPLY, nid, null, null,null);
		sendtcp(new MSG(nid,MSGCode.NODE_LOOKUP));
		hooks.add(x);
		synchronized (x) {
			try {
				x.wait(1000);
			}
			catch (InterruptedException e) {
			}
		}
		if(x.getHookedMSG()!=null) {
			return (Node)x.getHookedMSG().getData();
		}
		else return null;
		
		

	}

}
