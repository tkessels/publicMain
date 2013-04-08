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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.NachrichtenTyp;
import org.publicmain.common.Node;
import org.publicmain.gui.GUI;

/**
 * Die NodeEngine ist für die Verbindungen zu anderen Nodes zuständig. Sie verwaltet die bestehenden Verbindungen, sendet Nachichten und Datein und ist für das Routing zuständig
 */
public class NodeEngine {
 protected static final long CONNECTION_TIMEOUT = 200; 							//Timeout bis der Node die Suche nach anderen Nodes aufgibt und sich zum Root erklärt
 protected static final long ROOT_ANNOUNCE_TIMEOUT = 200; 					//Zeitspanne die ein Root auf Root_Announces wartet um zu entscheiden wer ROOT bleibt. 
 private final InetAddress group = InetAddress.getByName("230.223.223.223"); 	//Default MulticastGruppe für Verbindungsaushandlung
 private final int multicast_port = 6789; 													//Default Port für MulticastGruppe für Verbindungsaushandlung
 private final int MAX_CLIENTS = 5; 														//Maximale Anzahl anzunehmender Verbindungen 

 private static volatile NodeEngine ne; 	//Statischer Zeiger auf einzige Instanz der NodeEngine
 private long nodeID;
 private Node meinNode; 					//die NodeRepräsentation dieser NodeEngine
 private ChatEngine ce; 					//Zeiger auf parent ChatEngine
 private Hook angler = new Hook();		//Hookobjekt zum abfangen von Nachrichten		

 private ServerSocket server_socket; 				//Server Socket für eingehende Verbindungen (Passiv/Childs)
 private ConnectionHandler root_connection;	//TCP Socket zur Verbindung mit anderen Knoten (Aktiv/Parent/Root)
 private MulticastSocket multi_socket;			//Multicast/Broadcast UDP-Socket zu Verbindungsaushandlung
 public List<ConnectionHandler> connections; 	//Liste bestehender Childverbindungen in eigener HüllKlasse
 
 private Set<String> allGroups=new HashSet<String>();  //Liste aller Gruppen 
private Set<String> myGroups=new HashSet<String>(); //Liste aller abonierten Gruppen dieses und aller untergeordneter Knoten
 

 private BlockingQueue<MSG> root_claims_stash; //Queue für Bewerberpakete bei Neuaushandlung vom Root-Status 
 private Set<Node> allNodes; 						//Alle dieser Nodenginge bekannten Knotten (sollten alle sein)

 private volatile boolean rootMode; 			//Dieser Knoten möchte Wurzel sein (und benimmt sich auch so)
 private volatile boolean online; 				//Dieser Knoten möchte an sein und verbunden bleiben (signalisiert allen Threads wenn die Anwendung beendet wird)
 private volatile boolean rootDiscovering;	//Dieser Knoten ist gerade dabei ROOT_ANNOUNCES zu sammeln um einen neuen ROOT zu wählen

 private Thread multicastRecieverBot		= new Thread(new MulticastReciever());			//Thread zum annehmen und verarbeiten der Multicast-Pakete
 private Thread connectionsAcceptBot 	= new Thread(new ConnectionsAccepter()); 	//Thread akzeptiert und schachtelt eingehen Verbindungen auf dem ServerSocket
 private Thread rootMe	;																			//Thread der nach einem Delay Antrag auf RootMode stellt wird mit einem Discover gestartet
 private Thread rootClaimProcessor;												 				//Thread zum Sammeln und Auswerten von Root_Announces (Ansprüche auf Rootmode) wird beim empfang/versand eines RootAnnounce getstatet.
 

	public NodeEngine(ChatEngine parent) throws IOException {
		
		allNodes = new HashSet<Node>();
		connections = new ArrayList<ConnectionHandler>();
		root_claims_stash = new LinkedBlockingQueue<MSG>();
		
		setNodeID((long) (Math.random()*Long.MAX_VALUE));
		ne = this;
		ce = parent;
		online = true;

		server_socket = new ServerSocket(0);
		multi_socket = new MulticastSocket(multicast_port);
		multi_socket.joinGroup(group);
		multi_socket.setLoopbackMode(true);
		multi_socket.setTimeToLive(10);

		meinNode = new Node();
		allNodes.add(meinNode);

		LogEngine.log(this, "Multicast Socket geöffnet", LogEngine.INFO);

		connectionsAcceptBot.start();
		multicastRecieverBot.start();

		discover();
		
		

	}

	public static NodeEngine getNE() {
		return ne;
	}

	/**
	 * getMe() gibt das eigene NodeObjekt zurück
	 */
	public Node getMe() {
		return meinNode; 
	}

	
	
	
	private Node getBestNode() {
		// TODO Intelligente Auswahl des am besten geeigneten Knoten mit dem sich der Neue Verbinden darf.
		/*Random x = new Random(meinNode.getNodeID());
		Node[] tmp = null;
		allNodes.toArray(tmp);
		return tmp[x.nextInt(allNodes.size())];*/
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
		return rootMode && !hasParent();
	}
	
	public boolean isOnline() {
		return online;
	}

	public boolean hasParent() {
		return (root_connection != null && root_connection.isConnected());
	}

	public int getServer_port() {
		return server_socket.getLocalPort();
	}
	
	/**
	 * getNodes() gibt ein NodeArray zurück welche alle verbundenen Nodes beinhaltet.
	 */
	public Set<Node> getNodes() {
		synchronized (allNodes) {
			return allNodes;
		}
	}


	/**Findet zu NodeID zugehörigen Node in der Liste
	 * 
	 * Liefert das NodeObjekt zu einer NodeID solte der Knoten nicht bekannt sein wird <code>null</code> zurück geliefert.
	 * Befindet sich der Knoten der die Abfrage ausführ im RootMode dann wird er versuchen den Knoten über ein Lookup aufzuspüren und ihn nachzutragen.
	 * Schlägt dieser Versuch auch Fehl wird er einen Befehl an den Knoten schicken sich neu zu verbinden. (not yet implemented)
	 * 
	 * @param nid NodeID
	 * @return Node-Objekt zu angegebenem NodeID oder null wenn
	 */
	public Node getNode(long nid){
		synchronized (allNodes) {
			for (Node x : getNodes()) {
				if (x.getNodeID() == nid)
					return x;
			}
			if (isRoot())
				return retrieve(nid);
			else
				return null;
		}
	}
	
	


	/**
	 * Gibt ein StringArray aller vorhandenen Groups zurück
	 * 
	 */
	public Set<String> getGroups() {
		return allGroups;
	}

	private void sendroot(MSG msg) {
		if (hasParent()) root_connection.send(msg);
	}

	/**
	 * versendet Daten vom Typ MSG an ZielNodes oder git diese an send_file() weiter. prüft Dateigröße (wenn < 5MB aufruf der send_file() sonst als Msg-Type Data) send_file() wird sowohl das Ziel als auch die Daten mitgegeben. D.h., dass das
	 * MSG-Paket hier in File und destination geteilt.
	 */
	private void sendmutlicast(MSG nachricht) {
		byte[] buf = MSG.getBytes(nachricht);
		try {
			if (buf.length < 65000) {
				multi_socket.send(new DatagramPacket(buf, buf.length, group, 6789));
				LogEngine.log(this, "sende [MC]", nachricht);
			}
			else LogEngine.log(this, "MSG zu groß für UDP-Paket", LogEngine.ERROR);
		}
		catch (IOException e) {
			LogEngine.log(e);
		}
	}

	private void sendunicast(MSG msg, Node newRoot) {
		byte[] data = MSG.getBytes(msg);
		if (data.length < 65000) {
			for (InetAddress x : newRoot.getSockets()) {
				if (!meinNode.getSockets().contains(x)) {
					DatagramPacket unicast = new DatagramPacket(data,
							data.length, x, multicast_port);//über Unicast
					try {
						multi_socket.send(unicast);
						LogEngine
								.log(this, "sende [" + x.toString() + "]", msg);
					} catch (IOException e) {
						LogEngine.log(e);
					}
				}
			}
		}
	}

	public void sendtcp(MSG nachricht) {
		if (hasParent()) 		sendroot(nachricht); 
		if (hasChildren()) 	for (ConnectionHandler x : connections) x.send(nachricht);
	}

	private void sendtcpexcept(MSG msg, ConnectionHandler ch) {
//		if (!isRoot() && root_connection != ch) root_connection.send(msg);
		if (hasParent()&&root_connection != ch) root_connection.send(msg);
		if (hasChildren())sendchild(msg, ch);
	}
	
	private void sendchild(MSG msg, ConnectionHandler ch) {
		for (ConnectionHandler x : connections)if (x != ch||ch==null) x.send(msg);
	}

	/**
	 * versendet Datein über eine TCP-Direktverbindung wird nur von send() aufgerufen nachdem festgestellt wurde, dass nachicht > 5MB
	 */

	public void send_file(String destination) {
		// bekommt ziel und FILE übergeben

	}

	/*private void discover(Node newRoot) {
		sendunicast(new MSG(meinNode, MSGCode.ROOT_DISCOVERY), newRoot);
	}*/

	private void discover() {
		new Thread(new Runnable() {
			public void run() {
				sendmutlicast(new MSG(meinNode, MSGCode.ROOT_DISCOVERY));
				rootMe = new Thread(new RootMe());
				rootMe.start();
			}
		}).start();
	}

	private void sendDiscoverReply(Node quelle) {
		LogEngine.log(this, "sending Replay to " + quelle.toString(), LogEngine.INFO);
		sendunicast(new MSG(getBestNode(), MSGCode.ROOT_REPLY), quelle);
	}

	private void updateNodes() {
		/*
		 * if (isRoot()) { synchronized (allNodes) { allNodes.clear(); allNodes.add(getME()); allNodes.addAll(getChilds()); allNodes.notify(); } } else sendroot(new MSG(null, MSGCode.POLL_ALLNODES));
		 */
		synchronized (allNodes) {
			allNodes.clear();
			allNodes.add(getMe());
			allNodes.addAll(getChilds());
			allNodes.notify();
		}
		if (hasParent()) sendroot(new MSG(allNodes, MSGCode.REPORT_ALLNODES));

	}

	private void pollChilds() {
		for (ConnectionHandler x : connections) {
			x.send(new MSG(null, MSGCode.POLL_CHILDNODES));
		}
	}

	private Set<Node> getChilds() {
		Set<Node> rück = new HashSet<Node>();
		for (ConnectionHandler x : connections)
			rück.addAll(x.getChildren());
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
			if (!meinNode.getSockets().contains(x)) {
				try {
					tmp_socket = new Socket(x.getHostAddress(),
							knoten.getServer_port());
				} catch (UnknownHostException e) {
					LogEngine.log(e);
				} catch (IOException e) {
					LogEngine.log(e);
				}
				if (tmp_socket != null && tmp_socket.isConnected())
					break; // wenn eine Verbindung mit einer der IPs des
							// Knotenaufgebaut wurden konnte. Hör auf
			}
		}
		if (tmp_socket != null) {
			try {
				root_connection = new ConnectionHandler(tmp_socket);
				setRootMode(false);
				setGroup(myGroups);// FIXME:Bleibt das hier
				sendroot(new MSG(getMe()));
				sendroot(new MSG(myGroups, MSGCode.GROUP_REPLY));
				sendroot(new MSG(null, MSGCode.POLL_ALLNODES));
				sendroot(new MSG(null, MSGCode.GROUP_POLL));
			} catch (IOException e) {
				LogEngine.log(e);
			}
		}
	}
	

	public void disconnect() {
		online = false;
		connectionsAcceptBot.stop();
		multicastRecieverBot.stop();
		sendtcp(new MSG(meinNode, MSGCode.NODE_SHUTDOWN));
		sendroot(new MSG(myGroups,MSGCode.GROUP_LEAVE));
		root_connection.disconnect();
		for (final ConnectionHandler con : connections)
			(new Thread(new Runnable() {
				public void run() {
					con.disconnect();
				}
			})).start();// Threadded Disconnect für jede Leitung
		multi_socket.close();
		try {
			server_socket.close();
		}
		catch (IOException e) {
			LogEngine.log(e);
		}
	}

	/**
	 * Entfernt eine Verbindung wieder
	 * 
	 * @param conn
	 */
	public void remove(ConnectionHandler conn) {
		LogEngine.log(conn, "removing");
		if (conn == root_connection) {
			LogEngine.log(this, "Lost Root", LogEngine.INFO);
			root_connection = null;
			if (online) {
				updateNodes();
				//setGroup(myGroups); //FIXME: prüfen wo myGroups=allGroups den meisten macht.
				discover();
			}
		}
		else {
			LogEngine.log(this, "Lost Child", LogEngine.INFO);
			connections.remove(conn);
			sendtcp(new MSG(conn.getChildren(), MSGCode.CHILD_SHUTDOWN));
			updateMyGroups();
			allnodes_remove(conn.getChildren());
		}
		//updateNodes();
	}


	private void sendRA() {
		MSG ra= new MSG(meinNode, MSGCode.ROOT_ANNOUNCE);
		ra.setEmpfänger(getNodes().size());
		sendmutlicast(ra);
		root_claims_stash.add(ra);
	}

	private void handleRootClaim(MSG paket) {
		if(paket!=null) {
			paket.reStamp(); //change timestamp to recieved time
			root_claims_stash.offer(paket);
		}
		claimRoot();
	}

	/**
	 * Hier wird das Paket verarbeitet und weitergeleitet. Diese Methode wird ausschließlich vom MulticastSocketHandler aufegrufen.
	 * 
	 * @param paket
	 *            Das empfangene MulticastPaket
	 */
	public void handleMulticast(MSG paket) {
		LogEngine.log(this, "handling [MC]", paket);
		if (angler.check(paket)) return;
		if (online && (paket.getTyp() == NachrichtenTyp.SYSTEM)) {
			switch (paket.getCode()) {
				case ROOT_REPLY:
					if (!hasParent())connectTo((Node) paket.getData());
					break;
				case ROOT_DISCOVERY:
					if (isRoot()) sendDiscoverReply((Node) paket.getData());
					break;
				case ROOT_ANNOUNCE:
					if (!hasParent()) handleRootClaim(paket);
					break;
				case NODE_LOOKUP:
					if((long)paket.getData()==meinNode.getNodeID())sendroot(new MSG(meinNode));
					break;
				case ALIAS_UPDATE:
					updateAlias((String) paket.getData(), paket.getSender());
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
	@SuppressWarnings("unchecked")
	public void handle(MSG paket, ConnectionHandler quelle) {
		LogEngine.log(this, "handling[" + quelle + "]", paket);
		if (angler.check(paket)) return;
		switch (paket.getTyp()) {
		case PRIVATE:
			if(paket.getEmpfänger()==nodeID)ce.put(paket);
			else routesend(paket);
			break;

		case GROUP:
			//sendtcpexcept(paket, quelle);
			groupRouteSend(paket,quelle);
			ce.put(paket);
			break;
		case SYSTEM:
			switch (paket.getCode()) {
			case NODE_UPDATE:
				allnodes_add((Node) paket.getData());
				sendtcpexcept(paket, quelle);
				break;

			case POLL_ALLNODES:
				if (quelle != root_connection)
					quelle.send(new MSG(getNodes(), MSGCode.REPORT_ALLNODES));
				break;
			case REPORT_ALLNODES:
				allnodes_set((Set<Node>) paket.getData());
				break;
			case POLL_CHILDNODES:
				if (quelle == root_connection) {
					Set<Node> tmp = new HashSet<Node>();
					for (ConnectionHandler x : connections)
						tmp.addAll(x.getChildren());
					sendroot(new MSG(tmp, MSGCode.REPORT_CHILDNODES));
				}
				break;
			case REPORT_CHILDNODES:
				if (quelle != root_connection) {
					quelle.setChildren((Collection<Node>) paket.getData());
				}
				break;
			case NODE_SHUTDOWN:
				allnodes_remove(quelle.getChildren());
				quelle.close();
				break;
			case CHILD_SHUTDOWN:
				if (quelle != root_connection) quelle.removeChildren((Collection<Node>) paket.getData());
				allnodes_remove((Collection<Node>) paket.getData());
				sendtcpexcept(paket, quelle);
				break;
			case GROUP_JOIN:
				quelle.add((Collection<String>) paket.getData());
				joinGroup((Collection<String>) paket.getData(), quelle);
				break;
			case GROUP_LEAVE:
				quelle.remove((Collection<String>) paket.getData());
				leaveGroup((Collection<String>) paket.getData(), quelle);
				break;
			case GROUP_ANNOUNCE:
				if (addGroup((Collection<String>) paket.getData()))
					sendchild(paket, null);
				break;
			case GROUP_EMPTY:
				if (removeGroup((Collection<String>) paket.getData()))
					sendchild(paket, null);
				break;
			case GROUP_POLL:
				quelle.send(new MSG(allGroups, MSGCode.GROUP_REPLY));
				break;
			case GROUP_REPLY:
				Set<String> groups = (Set<String>) paket.getData();
				quelle.add(groups);
				updateMyGroups();
				addGroup(groups);
				break;
			case NODE_LOOKUP:
				Node tmp = null;
				if ((tmp = getNode((long) paket.getData())) != null)quelle.send(new MSG(tmp));
				else
					sendroot(paket);
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
	
	private void routesend(MSG paket) {
		long empfänger = paket.getEmpfänger();
		for (ConnectionHandler con : connections) {
			if(con.hasChild(empfänger)) {
				con.send(paket);
				return;
			}
		}
		if(hasParent())sendroot(paket);
		else if(isRoot()) {//Node ist Wurzel des Baums und weiss nicht wo der Empfänger ist
			Node tmp = retrieve(empfänger); //versuche Empfänger aufszuspüren
			if(tmp!=null) routesend(paket); //und Paket zuzustellen
			else sendchild(new MSG(empfänger,MSGCode.NODE_SHUTDOWN),null); //weiss alle Clients an diesen Empfänger zu entfernen und alle offnen Fenster zu deaktivieren
		}
	}
	
	private void groupRouteSend(MSG paket,ConnectionHandler quelle) {
		String gruppe = paket.getGroup();
		for (ConnectionHandler con : connections) {
			if((quelle!=con)&&con.getGroups().contains(gruppe)) {
				con.send(paket);
			}
			if(hasParent())sendroot(paket);
		}
	}

	private boolean updateMyGroups() {
		Set<String> aktuell = computeGroups();
		synchronized (myGroups) {

			if (aktuell.hashCode() != myGroups.hashCode()) {
				Set<String> dazu = new HashSet<String>(aktuell);
				dazu.removeAll(myGroups);
				if(dazu.size()>0) {
					sendroot(new MSG(dazu, MSGCode.GROUP_JOIN));
					if(addGroup(dazu))sendchild(new MSG(dazu, MSGCode.GROUP_ANNOUNCE),null);
				}

				Set<String> weg = new HashSet<String>(myGroups);
				weg.removeAll(aktuell);
				if(weg.size()>0) {
					sendroot(new MSG(weg, MSGCode.GROUP_LEAVE));
					if(isRoot()) {
						if(removeGroup(weg))sendchild(new MSG(weg, MSGCode.GROUP_EMPTY),null);
					}
				}

				myGroups.clear();
				myGroups.addAll(aktuell);
				return true;
			}
			return false;
		}
	}

	private void allnodes_remove(Collection<Node> data) {
		synchronized (allNodes) {
			int hash = allNodes.hashCode();
			allNodes.removeAll(data);
			allNodes.add(meinNode);
			if(allNodes.hashCode()!=hash)allNodes.notifyAll();
		}
	}
	
	private void allnodes_remove(Node data) {
		synchronized (allNodes) {
			int hash = allNodes.hashCode();
			allNodes.remove(data);
			if(allNodes.hashCode()!=hash)allNodes.notifyAll();
		}
	}
	
	private void allnodes_add(Collection<Node> data) {
		synchronized (allNodes) {
			int hash = allNodes.hashCode();
			allNodes.addAll(data);
			if(allNodes.hashCode()!=hash)allNodes.notifyAll();
		}
	}
	
	private void allnodes_add(Node data) {
		synchronized (allNodes) {
			int hash = allNodes.hashCode();
			allNodes.add(data);
			if(allNodes.hashCode()!=hash)allNodes.notifyAll();
		}
	}
	
	private void allnodes_set(Collection<Node> data) {
		synchronized (allNodes) {
			int hash = allNodes.hashCode();
			allNodes.clear();
			allNodes.addAll(data);
			allNodes.add(meinNode);
			if(allNodes.hashCode()!=hash)allNodes.notifyAll();
		}
	}
	
	
	
	
	

	/**
	 * Definiert diesen Node nach einem Timeout als Wurzelknoten falls bis dahin keine Verbindung aufgebaut wurde.
	 */
	private final class RootMe implements Runnable {
		public void run() {
			if (!online&&!isRoot()&&!rootDiscovering)	return;
			long until = System.currentTimeMillis() + CONNECTION_TIMEOUT;
			while (System.currentTimeMillis() < until) {
				try {
					Thread.sleep(500);
				}
				catch (InterruptedException e) {
				}
			}
			if (online&&!hasParent()) {
				LogEngine.log("RootMe", "no Nodes detected: claiming Root", LogEngine.INFO);
				claimRoot();
			}
		}

		/**
		 * 
		 */
		
	}
	private synchronized void claimRoot() {
			if(rootDiscovering==false) {
				rootClaimProcessor=new Thread(new RootClaimProcessor());
				rootClaimProcessor.start();
			}
	}
	/**
	 * Warte eine gewisse Zeit und Wertet dann alle gesammelten RoOt_AnOunCEs aus forder anschließend vom Gewinner einen Knoten zum Verbinden an. Wenn der Knoten selber Gewonnen hat
	 */
	private final class RootClaimProcessor implements Runnable {
		public void run() {
			rootDiscovering=true;
			LogEngine.log("DiscoverGame","started",LogEngine.INFO);
			sendRA();
			long until = System.currentTimeMillis() + ROOT_ANNOUNCE_TIMEOUT;
			while (System.currentTimeMillis() < until) {
				try {
					Thread.sleep(500);
				}
				catch (InterruptedException e) {
				}
			}
			
			List <MSG> ra_replies=new ArrayList<MSG>();
			ra_replies.addAll(root_claims_stash);
			Collections.sort(ra_replies);
			/*for (MSG msg : ra_replies) {
				System.out.println(((Node)msg.getData()).getHostname());
			}*/
			long deadline  = ra_replies.get(0).getTimestamp()+2* ROOT_ANNOUNCE_TIMEOUT;
			
			Node toConnectTo = meinNode;
			long maxPenunte = getNodes().size();
			for (MSG x : root_claims_stash) {
				if (x.getTimestamp() <= deadline) {
					long tmp_size = x.getEmpfänger();
					Node tmp_node = (Node) x.getData(); //	Cast Payload in ein Object Array und das 2. Object dieses Arrays in einen Node
					if (tmp_size > maxPenunte || ((tmp_size == maxPenunte) && (tmp_node.getNodeID() > toConnectTo.getNodeID()))) {
						toConnectTo = tmp_node;
						maxPenunte = tmp_size;
					}
				}
			}
			
			LogEngine.log("DiscoverGame","Finished:" + ((toConnectTo != meinNode)?"lost":"won")+"(" + root_claims_stash.size() +" participants)",LogEngine.INFO);
			
			if (toConnectTo == meinNode) setRootMode(true);
			else discover(); //another root won and should be answeringconnectTo(toConnectTo);
			root_claims_stash.clear();
			rootDiscovering=false;
		}
	}

	private final class MulticastReciever implements Runnable {
		public void run() {
			if(multi_socket==null)return;
			while (true) {
				byte[] buff = new byte[65535];
				DatagramPacket tmp = new DatagramPacket(buff, buff.length);
				try {
					multi_socket.receive(tmp);
					MSG nachricht = MSG.getMSG(tmp.getData());
					LogEngine.log("multicastRecieverBot", "multicastRecieve", nachricht);
					if (nachricht != null) handleMulticast(nachricht);
				}
				catch (IOException e) {
					LogEngine.log(e);
				}
			}
		}
	}

	private final class ConnectionsAccepter implements Runnable {
		public void run() {
			if(connections==null||server_socket==null)return;
			while (online) {
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

	/** Starte Lookup für {@link Node} mit der NodeID <code>nid</code>. Und versucht ihn neu Verbinden zu lassen bei Misserfolg.
	 * @param nid ID des Nodes
	 * @return das {@link Node}-Objekt oder <code>null</code> wenn der Knoten nicht gefunden wurde.
	 */
	private Node retrieve(long nid) {
			sendmutlicast(new MSG(nid, MSGCode.NODE_LOOKUP));
			MSG x = angler.fishfor(NachrichtenTyp.SYSTEM,MSGCode.NODE_UPDATE,nid,false,1000);
			if (x != null) return (Node) x.getData();
			else {
				LogEngine.log("retriever", "NodeID:["+nid+"] konnte nicht aufgespürt werden und sollte neu Verbinden!!!",LogEngine.ERROR);
				sendmutlicast(new MSG(nid, MSGCode.CMD_RECONNECT));
				return null;
			}
	}

	

	private void setRootMode(boolean rootmode) {
		this.rootMode = rootmode;
		GUI.getGUI().setTitle("publicMAIN"+((rootmode)?"[ROOT]":"" ));
		if(rootmode) setGroup(myGroups) ;
	}

	public long getNodeID() {
		return nodeID;
	}

	public void setNodeID(long nodeID) {
		this.nodeID = nodeID;
	}

	public void joinGroup(Collection<String> gruppen_namen, ConnectionHandler con) {
		//FIXME: vielleicht wäre es besser bei update my Groups einen Differenzsatz zu berechnen und für alle  wegfallenden ein leave group zu erstellen und für alle neuen ein Join Group
		/*if(updateMyGroups()){//Wenn sich was geänderhat melden vielleciht noch eingrenzen 
			sendroot(new MSG(gruppen_namen,MSGCode.GROUP_JOIN));
		}*/
		updateMyGroups();
		//if(addGroup(gruppen_namen))sendchild(new MSG(gruppen_namen,MSGCode.GROUP_ANNOUNCE), con);
	}

	public void leaveGroup(Collection<String> gruppen_namen, ConnectionHandler con) {
		if(updateMyGroups()){
			//sendroot(new MSG(gruppen_name,MSGCode.GROUP_LEAVE));
/*			if (isRoot()) {
				removeGroup(gruppen_namen);
				sendchild(new MSG(gruppen_namen,MSGCode.GROUP_EMPTY), null);
			}
*/		}
	}
	
	
	public boolean removeGroup(Collection<String> gruppen_name) {
		synchronized (allGroups) {
			boolean x = allGroups.removeAll(gruppen_name);
			allGroups.notifyAll();
			return x;
		}
	}
	/*
	public boolean removeGroup(String gruppen_name) {
		synchronized (allGroups) {
			boolean x = allGroups.remove(gruppen_name);
			allGroups.notifyAll();
			return x;
		}
	}
	*/
	
	public void setGroup(Collection<String> groups) {
		synchronized (allGroups) {
			allGroups.clear();
			allGroups.addAll(groups);
			allGroups.notifyAll();
		}
	}
	
	
	public boolean addGroup(Collection<String> groups) {
		synchronized (allGroups) {
		boolean x = allGroups.addAll(groups);
		allGroups.notifyAll();
		return x;
		}
	}
	/*
	public boolean addGroup(String gruppen_name) {
		synchronized (allGroups) {
			boolean x = allGroups.add(gruppen_name);
			allGroups.notifyAll();
			return x;
		}
	}
	*/
	public boolean removeMyGroup(String gruppen_name) {
		synchronized (myGroups) {
			return myGroups.remove(gruppen_name);
		}
	}
	
	public boolean addMyGroup(String gruppen_name) {
		synchronized (myGroups) {
			return myGroups.add(gruppen_name);
		}
	}
	
	public Set<String>computeGroups(){
		Set<String> tmpGroups = new HashSet<String>();
		for (ConnectionHandler cur : connections) {
			tmpGroups.addAll(cur.getGroups());
		}
		tmpGroups.addAll(ce.getMyGroups());
		return tmpGroups;
	}

	public void updateAlias() {
		String alias = ce.getAlias();
		if(online&&(!alias.equals(meinNode.getAlias()))) {
			meinNode.setAlias(alias);
			sendmutlicast(new MSG(alias, MSGCode.ALIAS_UPDATE));
			updateAlias(alias,nodeID);
			LogEngine.log(this,"User has changed ALIAS to " + alias,LogEngine.INFO);
		}
	}
	
	private boolean updateAlias(String newAlias, long nid) {
		Node tmp;
		
		synchronized (allNodes) {
			if ((tmp = getNode(nid)) != null) {
				if (tmp.getAlias() != newAlias) {
					tmp.setAlias(newAlias);
					allNodes.notifyAll();
					LogEngine.log(this,"User " +tmp.getAlias() + " has changed ALIAS to " + newAlias,LogEngine.INFO);
					return true;
				}
			}
			return false;
		}
	}

	public void debug(String command, String parameter) {
		switch (command) {
		

		default:
			LogEngine.log(this, "debug command not found", LogEngine.ERROR);
			break;
		}
		
	}
}
