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
 * Die NodeEngine ist für die Verbindungen zu anderen Nodes zuständig.
 * Sie verwaltet die bestehenden Verbindungen, sendet Nachichten und Datein 
 * und ist für das Routing zuständig 
 */
public class NodeEngine {
	protected static final long CONNECTION_TIMEOUT = 1000;
	protected static final long	ROOT_ANNOUNCE_TIMEOUT	= 2000;
	private static volatile NodeEngine ne;
	private Node meinNode;
	private ChatEngine ce;
	
	private ServerSocket server_socket;
	private ConnectionHandler root_connection;
	private MulticastSocket multi_socket;
	
	public List<ConnectionHandler> connections;
	public BlockingQueue<MSG> replies;
	private Set<Node> allNodes;
	
	//private boolean isOnline;
	private boolean isRoot;
	

	private Thread multicastRecieverBot;
	private Thread connectionsAcceptBot;
	private Thread discoverGame;

	private final InetAddress group = InetAddress.getByName("230.223.223.223");
	private final int multicast_port = 6789;
	private final int MAX_CLIENTS = 5;
	


	
	public NodeEngine(ChatEngine parent) throws IOException {
		allNodes =new HashSet<Node>();
		connections=new ArrayList<ConnectionHandler>();
		ne=this;
		ce=parent;
		
		server_socket = new ServerSocket(0);
		multi_socket = new MulticastSocket(multicast_port);
		multi_socket.joinGroup(group);
		multi_socket.setLoopbackMode(true);
		multi_socket.setTimeToLive(10);

		meinNode=Node.getMe();
		allNodes.add(meinNode);
		
		//hasConnectedRoot=false;

		LogEngine.log("Multicast Socket geöffnet",this,LogEngine.INFO);
		
		
		connectionsAcceptBot = new Thread(new Runnable() {
			public void run() {
				while (isOnline()&&connections.size()<=MAX_CLIENTS) {
					System.out.println("Listening on Port:" + server_socket.getLocalPort());
					try {
						ConnectionHandler tmp = new ConnectionHandler(server_socket.accept());
						connections.add(tmp);
						//routing.put(connections.indexOf(tmp),new ArrayList<Node>());
						//tmp.send(new MSG(allNodes,MSGCode.REPORT_ALLNODES));
					} catch (IOException e) {
						LogEngine.log(e);
					}
				}
			}
		});
		
		
		multicastRecieverBot=new Thread(new Runnable() {
			public void run() {
				while(true){
					byte[] buff = new byte[65535];
					DatagramPacket tmp = new DatagramPacket(buff, buff.length);
					try {
						multi_socket.receive(tmp);
						MSG nachricht = MSG.getMSG(tmp.getData());
						LogEngine.log("multicastRecieverBot","multicastRecieve",nachricht);
						handleMulticast(nachricht);
					} catch (IOException e) {
						LogEngine.log(e);
					}
				}
			}
		});
		multicastRecieverBot.start();
		
		discover();
		
	}

	

	public static NodeEngine getNE(){
		return ne;
	}
	
	/**
	 * getMe() gibt das eigene NodeObjekt zurück
	 */
	public Node getME (){
		return meinNode;					//TODO:nur zum test
	}

	private Node getBestNode() {
		// TODO Intelligente Auswahl des am besten geeigneten Knoten mit dem sich der Neue Verbinden darf.
		
		return meinNode;
	}
	
	/**
	 * isConnected() gibt "true" zurück wenn die laufende Nodeengin hochgefahren und mit anderen Nodes verbunden oder root ist,
	 * "false" wenn nicht.
	 */
	public	boolean	isOnline(){
		return (isRoot||(root_connection!=null&&root_connection.isConnected()));
	}
	
	/**
	 * isRoot() gibt "true" zurück wenn die laufende Nodeengin Root ist und
	 * "false" wenn nicht.
	 */
	public boolean	isRoot (){
		return ((root_connection==null)||(!root_connection.isConnected()&&isRoot));
	}
	
	
	
	/**
	 * getNodes() gibt ein NodeArray zurück welche alle verbundenen
	 * Nodes beinhaltet.
	 */
	public Collection<Node> getNodes (){
		System.out.println(allNodes);
		return allNodes;				
	}
	
	
	public int getServer_port(){
		return server_socket.getLocalPort();
	}


	/**
	 * Gibt ein StringArray aller vorhandenen Groups zurück
	 *
	 */
	public String[] getGroups	(){
		String[] grouparray = {"public","GruppeA", "GruppeB"};
		return grouparray;					//TODO:to implement
	}
	
	
	private void sendroot(MSG msg) {
		if(root_connection!=null&&root_connection.isConnected())root_connection.send(msg);
	}
	
	/**
	 * versendet Daten vom Typ MSG an ZielNodes oder git diese an send_file() weiter.
	 * prüft Dateigröße (wenn < 5MB aufruf der send_file() sonst als Msg-Type Data)
	 * send_file() wird sowohl das Ziel als auch die Daten mitgegeben. D.h., dass das
	 * MSG-Paket hier in File und destination geteilt.
	 */
	private void sendmutlicast (MSG nachricht){
		byte[] buf = MSG.getBytes(nachricht);
		LogEngine.log(this,"sende",nachricht);
		try {
			if(buf.length<65000)multi_socket.send(new DatagramPacket(buf,buf.length,group,6789));
			else LogEngine.log("MSG zu groß für UDP-Paket",this, LogEngine.ERROR);
		} catch (IOException e) {
			LogEngine.log(e);
		}
	}
	
	private void sendunicast(MSG msg, Node newRoot) {
		byte[] data=MSG.getBytes(msg);
		for(InetAddress x : newRoot.getSockets()){
			DatagramPacket unicast =new DatagramPacket(data,data.length,x,multicast_port);//über Unicast
			try {
				multi_socket.send(unicast);
			} catch (IOException e) {
				LogEngine.log(e);
			}
		}
}

	
	public void sendtcp(MSG nachricht){
		if(!isRoot())root_connection.send(nachricht); //vielleicht sendroot verwenden?
		for (ConnectionHandler x : connections) x.send(nachricht);
	}
	
	private void sendtcpexcept(MSG msg,ConnectionHandler ch){
		if(!isRoot()&&root_connection!=ch)root_connection.send(msg);
		for (ConnectionHandler x : connections) if(x!=ch)x.send(msg);
	}
	
	/**
	 * versendet Datein über eine TCP-Direktverbindung
	 * wird nur von send() aufgerufen nachdem festgestellt wurde, dass nachicht > 5MB
	 */

	public void send_file (String destination){		
		// bekommt ziel und FILE übergeben
		
	}
	
	
	
	
	

private void discover(Node newRoot) {
	sendunicast(new MSG(meinNode,MSGCode.ROOT_DISCOVERY),newRoot);
}
	
	private void discover() {
		sendmutlicast(new MSG(meinNode, MSGCode.ROOT_DISCOVERY));
		Thread selbstZumRootErklärer = new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(CONNECTION_TIMEOUT);
				} catch (InterruptedException e) {
				}
				if(!isOnline()){
					isRoot=true;
				}
				connectionsAcceptBot.start();
			}
		});
		selbstZumRootErklärer.start();
	}
	
	private void sendDiscoverReply(Node quelle) {
		LogEngine.log("sending Replay to " + quelle.toString(),this,LogEngine.INFO);
		sendunicast(new MSG(getBestNode(), MSGCode.ROOT_REPLY), quelle);
		/*byte[] data=MSG.getBytes(new MSG(getBestNode(), MSGCode.ROOT_REPLY));
		for(InetAddress x : quelle.getSockets()){
			DatagramPacket discover =new DatagramPacket(data,data.length,x,multicast_port);
			try {
				multi_socket.send(discover);
			} catch (IOException e) {
				LogEngine.log(e);
			}
			
		}
*/
		
	}

	private void updateNodes() {
		if (isRoot()) {
			synchronized (allNodes) {
				allNodes.clear();
				allNodes.add(getME());
				allNodes.notify();
			for (ConnectionHandler x : connections) {
				allNodes.addAll(x.children);
			}
			}
		} else
			sendroot(new MSG(null, MSGCode.POLL_ALLNODES));
	}
	
	private void updateChilds(){
		for(ConnectionHandler x : connections){
			x.send(new MSG(null,MSGCode.POLL_CHILDNODES));
		}
	}
	
	private Set<Node> getChilds(){
		Set<Node> rück = new HashSet<Node>();
	   for(ConnectionHandler x :connections)rück.addAll(x.children);
	   return rück;
	}
	



	/** Stelle Verbindung mit diesem <code>NODE</code> her!!!!
	 * @param knoten der Knoten
	 * @throws IOException 
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
			sendmutlicast(new MSG(getME()));
			sendroot(new MSG(getME()));
			sendroot(new MSG(null, MSGCode.POLL_ALLNODES));
			//hasConnectedRoot = true;

		}
	}



	/** Entfernt eine Verbindung wieder
	 * @param conn
	 */
	public void remove(ConnectionHandler conn) {
		if (conn==root_connection) {
			root_connection=null;
			//TODO:wir habeb die Verbindung nach oben verloren und müssten was tun
		}
		connections.remove(conn);
		sendtcp(new MSG(conn.children,MSGCode.CHILD_SHUTDOWN));
		//sendmutlicast(new MSG(conn.children,MSGCode.CHILD_SHUTDOWN));
		//updateChilds();
		//updateNodes();
	}
	
	
	private synchronized void discover_game(MSG paket) {
		replies.add(paket);
		if (discoverGame == null) {
			Object[] payload = { allNodes, meinNode };
			sendmutlicast(new MSG(payload, MSGCode.ROOT_ANNOUNCE));
			discoverGame = new Thread(new Runnable() {
				public void run() {
					long until = System.currentTimeMillis() + ROOT_ANNOUNCE_TIMEOUT;
					while (System.currentTimeMillis() < until)
						try {Thread.sleep(100);}
						catch (InterruptedException e) {}
					Node toConnectTo = meinNode;
					int maxPenunte = allNodes.size();
					for (Object x : replies.toArray()) { 																					// extrahieren aller informationen dieses ROOTANNOUNCE
						@SuppressWarnings("unchecked")
						Set<Node> tmp_allnodes = (Set<Node>) ((Object[]) ((MSG) x).getData())[0]; 	// Caste Payload in ein Object Array und das 1. Object in ein Set aus Nodes
						Node tmp_node = (Node) ((Object[]) ((MSG) x).getData())[1];  							//	Caste Payload in ein Object Array und das 2. Object dieses Arrays in einen Node
						if (tmp_allnodes.size() > maxPenunte) {
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
					replies.clear();
					discoverGame=null;
				}
			});
		}
	}

	/**Hier wird das Paket verarbeitet und weitergeleitet. Diese Methode wird ausschließlich vom MulticastSocketHandler aufegrufen.
	 * @param paket  Das empfangene MulticastPaket
	 */
	public void handleMulticast(MSG paket) {
		LogEngine.log(this, "handling [MC]", paket);
		if (paket.getTyp() == NachrichtenTyp.SYSTEM) {
			switch (paket.getCode()) {
				case ROOT_REPLY:
					if (!isOnline()&&isRoot()) {
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
					LogEngine.log(this, "handling [MC]:undefined",paket);
			}
		}
	}

	



	/**Hier wird das Paket verarbeitet und weitergeleitet. Diese Methode wird ausschließlich von den ConnectionHandlern aufgerufen um empfange Pakete verarbeiten zu lassen.
	 * @param paket Zu verarbeitendes Paket
	 * @param quelle Quelle des Pakets
	 */
	public void handle(MSG paket, ConnectionHandler quelle) {
		LogEngine.log(this,"handling["+quelle+"]",paket);
		switch (paket.getTyp()){
		case GROUP:
			sendtcpexcept(paket, quelle);
			ce.put(paket);
			break;
		case SYSTEM:
			switch(paket.getCode()){
			case NODE_UPDATE:
				allNodes.add((Node) paket.getData());
				if(quelle!=root_connection)quelle.children.add((Node) paket.getData());
				sendtcpexcept(paket, quelle);
//				else LogEngine.log("NODE_UPDATE von root bekommen", this, LogEngine.WARNING);
				break;
			
			case POLL_ALLNODES:
				if(root_connection!=quelle)quelle.send(new MSG(allNodes,MSGCode.REPORT_ALLNODES));
//				else LogEngine.log("POLL_ALLNODES von root bekommen", this, LogEngine.WARNING);
				break;
			case REPORT_ALLNODES:
				if(quelle==root_connection){
					allNodes.clear();
					allNodes.addAll((List<Node>)paket.getData());
					allNodes.add(meinNode);
				}
//				else LogEngine.log("REPORT_ALLNODES von komischer Quelle bekommen:", this, LogEngine.WARNING);
				break;
			case POLL_CHILDNODES:
				if(quelle==root_connection){
				List<Node> tmp=new ArrayList<Node>();
				for(ConnectionHandler x :connections) tmp.addAll(x.children);
				sendroot(new MSG(tmp,MSGCode.REPORT_CHILDNODES));
				}
//				else LogEngine.log("POLL_CHILDNODES von komischer Quelle bekommen:", this, LogEngine.WARNING);
				break;
			case REPORT_CHILDNODES:
				if(quelle!=root_connection){
				   quelle.children.clear();
				   quelle.children.addAll((List<Node>) paket.getData());
				}
//				else LogEngine.log(this,"handling ",paket);
				break;
			case NODE_SHUTDOWN:
				sendtcpexcept(new MSG(quelle.children,MSGCode.CHILD_SHUTDOWN), quelle);
				allNodes.removeAll(quelle.children);
				quelle.close();
				break;
				
			
			case CHILD_SHUTDOWN:
				if(quelle!=root_connection)quelle.children.removeAll((Collection<Node>) paket.getData());
				allNodes.removeAll((Collection<Node>) paket.getData());
				sendtcpexcept(paket, quelle);
				
				break;
			}
			break;
		case DATA:
			break;
		default:
		
		}
	}
	
}
