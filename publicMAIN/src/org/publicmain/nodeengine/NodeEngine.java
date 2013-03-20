package org.publicmain.nodeengine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.publicmain.common.*;
import org.publicmain.chatengine.ChatEngine;





/**
 * @author rpfaffner
 * Die NodeEngine ist f�r die Verbindungen zu anderen Nodes zust�ndig.
 * Sie verwaltet die bestehenden Verbindungen, sendet Nachichten und Datein 
 * und ist f�r das Routing zust�ndig 
 */
public class NodeEngine {
	private static volatile NodeEngine ich;
	
	private ServerSocket server_socket;
	private MulticastSocket multi_socket;
	private List<ConnectionHandler> connections;
	
	
	//-----nur zum test--------
	private Node meinNode;
	private Node[] meinNodeArray = new Node[2];
	private boolean isConnected;
	private boolean isRoot;
	private ChatEngine ce;
	private final InetAddress group = InetAddress.getByName("230.223.223.223");
	private final int port = 6789;
	
	private Thread msgRecieverBot;

	// -------------------------
	

	
	public NodeEngine(ChatEngine parent) throws IOException {
		ich=this;
		ce=parent;
		meinNode=Node.getMe();
		
		multi_socket = new MulticastSocket(port);
		multi_socket.joinGroup(group);
		multi_socket.setTimeToLive(10);
		isConnected=true;
		LogEngine.log("Multicast Socket ge�ffnet",this,LogEngine.INFO);
		
		
		msgRecieverBot=new Thread(new Runnable() {
			public void run() {
				while(isConnected){
					byte[] buff = new byte[65535];
					DatagramPacket tmp = new DatagramPacket(buff, buff.length);
					try {
						multi_socket.receive(tmp);
						MSG nachricht = MSG.getMSG(tmp.getData());
						LogEngine.log("nachricht empfange: " + nachricht.toString(),this,LogEngine.INFO);
						ce.put(nachricht);
					} catch (IOException e) {
						LogEngine.log(e);
					}
				}
			}
		});
		msgRecieverBot.start();
	}
	
	
	public static NodeEngine getNE(){
		
	/* if(ich==null){			//factory Method �berfl�ssig? NE wird sofort am anfang instanzier
			synchronized (NodeEngine.class) {
				if(ich==null)ich=new NodeEngine();				
			}
		}*/ 
		return ich;
	}
	

	/**Erzeugt eine Liste aller lokal vergebenen IP-Adressen mit ausnahme von Loopbacks und IPV6 Adressen
	 * @return Liste aller lokalen IPs
	 */
	public static List<InetAddress> getMyIPs() {
		List<InetAddress> addrList = new ArrayList<InetAddress>();
		try {
			for (InetAddress inetAddress : InetAddress.getAllByName(InetAddress.getLocalHost().getHostName())) { //Finde alle IPs die mit meinem hostname assoziert sind und 
			if (inetAddress.getAddress().length==4)addrList.add(inetAddress);									 //f�ge die meiner liste hinzu die IPV4 sind also 4Byte lang
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return addrList;
	}
	
	/**
	 * isConnected() gibt "true" zur�ck wenn die laufende Nodeengin hochgefahren und mit anderen Nodes verbunden oder root ist,
	 * "false" wenn nicht.
	 */
	public	boolean	isConnected	(){
		return isConnected;
	}
	
	/**
	 * isRoot() gibt "true" zur�ck wenn die laufende Nodeengin Root ist und
	 * "false" wenn nicht.
	 */
	public boolean	isRoot (){
		return isRoot;
	}
	
	
	/**
	 * getMe() gibt das eigene NodeObjekt zur�ck
	 */
	public Node getME (){
		return meinNode;					//TODO:nur zum test
		
	}
	
	/**
	 * getNodes() gibt ein NodeArray zur�ck welche alle verbundenen
	 * Nodes beinhaltet.
	 */
	public Node[] getNodes (){
		return meinNodeArray;				//TODO:nur zum test
	}
	
	
	/**
	 * Gibt ein StringArray aller vorhandenen Groups zur�ck
	 *
	 */
	public String[] getGroups	(){
		String[] grouparray = {"public","GruppeA", "GruppeB"};
		return grouparray;					//TODO:to implement
	}
	
	/**
	 * versendet Daten vom Typ MSG an ZielNodes oder git diese an send_file() weiter.
	 * pr�ft Dateigr��e (wenn < 5MB aufruf der send_file() sonst als Msg-Type Data)
	 * send_file() wird sowohl das Ziel als auch die Daten mitgegeben. D.h., dass das
	 * MSG-Paket hier in File und destination geteilt.
	 */
	public void send (MSG nachricht){
		byte[] buf = MSG.getBytes(nachricht);
		LogEngine.log("sende nachricht:" + nachricht.toString(), this, LogEngine.INFO);
		try {
			multi_socket.send(new DatagramPacket(buf,buf.length,group,6789));
		} catch (IOException e) {
			LogEngine.log(e);
		}
		
		
	}
	
	/**
	 * versendet Datein �ber eine TCP-Direktverbindung
	 * wird nur von send() aufgerufen nachdem festgestellt wurde, dass nachicht > 5MB
	 */
	public void send_file (String destination){		
		// bekommt ziel und FILE �bergeben
		
	}
	
	public static void main(String[] args) {
		System.out.println(getMyIPs());
		
	}
	
	

	/**Hier wird das Paket verarbeitet und weitergeleitet. Diese Methode wird ausschlie�lich von den ConnectionHandlern aufgerufen um empfange Pakete verarbeiten zu lassen.
	 * @param paket neue 
	 */
	public void handle(MSG paket) { // Muss Thread-Safe sein damit die ConnHandlers direkt damit arbeiten k�nnen.
		// TODO Auto-generated method stub
		
	}

}
