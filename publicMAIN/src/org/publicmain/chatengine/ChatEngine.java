package org.publicmain.chatengine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.NachrichtenTyp;
import org.publicmain.common.Node;
import org.publicmain.gui.GUI;
import org.publicmain.nodeengine.NodeEngine;

/**
 * @author ATRM
 *
 */

public class ChatEngine extends Observable{
	private static ChatEngine ce;
	public NodeEngine ne;
	public LogEngine log;
	private Set<Node> nodes;
	private Set<Node> ignored;
	private List<GruppenKanal> group_channels;
	private List<KnotenKanal> private_channels;
	
	private Thread msgSorterBot;//verteilt eingehende MSGs
	private Thread neMaintenance;//warted die NE
	
	
	private BlockingQueue<MSG> inbox;
	
	
	
	/**Liefert die Instanz der CE
	 * @return
	 */
	public static synchronized ChatEngine getCE() throws Exception{
		if(ce==null) ce=new ChatEngine();
		return ce;
	}

	
	private ChatEngine() throws IOException{
		
		ne = new NodeEngine(this);
		ce=this;
		
		group_channels=new ArrayList<GruppenKanal>();
		private_channels=new ArrayList<KnotenKanal>();
		nodes=new HashSet<Node>();
		ignored=new HashSet<Node>();
		nodes.addAll(Arrays.asList(ne.getNodes()));
		inbox=new LinkedBlockingQueue<MSG>();
		//group_join("public");
		
		//hier m�ssten die default kan�le angelegt werden und die GUI m�sste angebunden werden.
		
		
		
		msgSorterBot=new Thread(new Runnable() { //nimmt Nachrichten aus der inbox und verteilt sie auf die Kan�le
			public void run() {
				while (true) {
					try {
						MSG tmp = inbox.take(); 
						LogEngine.log("verarbeite Nachricht: " + tmp.toString(), this, LogEngine.INFO);
						if (tmp.getTyp() == NachrichtenTyp.GROUP) for (GruppenKanal x : group_channels)if (x.add(tmp)) break;
						else if (tmp.getTyp() == NachrichtenTyp.PRIVATE) for (KnotenKanal y : private_channels) if (y.add(tmp))break;
					} catch (InterruptedException e) {//Unterbrochen beim Warten... hmmm ist das Schlimm?
					}
				}
			}}
		);
		msgSorterBot.start();
		
		
	}
	
	
	/**Weisst die ChatEngine an einen <code>text</code> an den Nutzer mit der entsprechen <code>uid</code> zu schicken. 
	 * @param uid UID des Empf�ngers
	 * @param text Nachricht
	 */
	public void send_private(long uid, String text){
		MSG tmp = new MSG(uid,text);
		ne.send(tmp);
	}
	
	/**Weisst die ChatEngine an einen <code>text</code> an eine gruppe <code>group</code> zu schicken.
	 * @param group Gruppenbezeichnung
	 * @param text Nachricht
	 */
	public void send_group(String group, String text){
		MSG tmp = new MSG(group,text);
		ne.send(tmp);
	}
	
	/**Weisst die ChatEngine an einen <code>datei</code> an einen Nutzer mit der entsprechenden <code>uid</code> zu schicken.
	 * @param uid UID des Empf�ngers
	 * @param datei Datei
	 * @return id des Dateitransfers f�r sp�tere R�ckfragen
	 */
	public int send_file(long uid, File datei){
		//TODO: CODE HERE
		return 0;
	}
	
	/** Gibt den Zustand der �bertragung einer Datei an
	 * @param file_transfer_ID
	 * @return <ul>	<li><code>-1</code> Dateitransfer nicht m�glich</li>
	 * 				<li><code>-2</code> Benutzer lehnt transfer ab</li>
	 * 				<li><code>0</code> - <code>100</code> Vortschritt der Daten�bertragung in Prozent 
	 */
	public int file_transfer_status(int file_transfer_ID){
		
		//TODO: CODE HERE
		return 0;
	}
	
	/**Fragt ein Array alle User ab 
	 * @return Array aller verbundener Nodes
	 */
	public	Node[]	getUsers(){
		return (Node[]) nodes.toArray();
	}
	
	/** tritt einer Gruppe bei
	 * @param gruppen_name Gruppennamen sind CaseInSensitiv und bestehen aus alphanumerischen Zeichen
	 */
	public void group_join(String gruppen_name){
		//GruppenKanal tmp =new GruppenKanal(gruppen_name);
		//group_channels.add(tmp);
		/*Lege KAnal an 
		 * informiere NodeEngine �ber neue gruppe wenn noch nicht vorhanden so das ander nodes diese anzeigen
		 * vielleicht machen wirdas auch einfach indem wir ein group announce paket forgen
		*/
		//TODO: CODE HERE
	}
	
	/**verl�sst eine gruppe wieder
	 * @param gruppen_name Gruppennamen sind CaseInSensitiv und bestehen aus alphanumerischen Zeichen
	 */
	public void group_leave(String gruppen_name){
		
		//TODO: CODE HERE		
	}
	
	/**Liefert eine Liste der verf�gbaren Gruppenstrings
	 * @return Array der verf�gbaren Gruppenstrings
	 */
	public	String[]	group_list(){
		//TODO: CODE HERE		
		return null;
	}
	
	/** Bittet die ChatEngine um ein Fileobjekt zur Ablage der empfangenen Datei
	 * wird von der NodeEnginge aufgerufen und soll an die GUI weiterleiten
	 * @return abstraktes Fileobjekt zu speicherung einer Datei. 
	 * 	Null Wenn der Nutzer den Empfang ablehnt 
	 */
	public	File	request_File(){
		//TODO: CODE HERE
		return GUI.getGUI().request_File();
	}
	
	/**Veranlasst das Nachrichten vom user mit der <code>uid</code> nicht mehr angezeigt werden.
	 * @param uid
	 */
	public	void	ignore_user(long uid){
		//TODO: CODE HERE
	}
	
	/**Ver�ndert den Anzeigenamen des Nutzers
	 * @param alias neuer Anzeigename [a-zA-Z0-9]{12} 
	 */
	public	void	set_alias(String alias){
		//TODO: CODE HERE
	}
	
	/**Liefert den aktuellen Anzeigenamen
	 * @return aktueller Anzeigename
	 */
	public	String	get_alias(){
		return ne.getME().getAlias();
	}
	
	
	/** Meldet einen Nachrichten-Listener an einem Gruppen - Nachrichten Kanal an 
	 * @param chatPanel Das abonierende Fenster
	 * @param gruppen_name zu abonierender Gruppen Kanal
	 */
	public void add_MSGListener(Observer chatPanel,String gruppen_name){
		GruppenKanal tmp =new GruppenKanal(gruppen_name);
		int i = group_channels.indexOf(tmp);
		if(i>=0)group_channels.get(i).addObserver(chatPanel);
		else{
			tmp.addObserver(chatPanel);
			group_channels.add(tmp);
		}
	}
	
	
	/** Meldet einen Nachrichten-Listener an einem privaten - Nachrichten Kanal an 
	 * @param chatPanel Das abonierende Fenster
	 * @param gruppen_name zu abonierender Gruppen Kanal
	 */
	public void	add_MSGListener(Observer chatPanel,long uid){
		KnotenKanal tmp =new KnotenKanal(uid);
		int i = group_channels.indexOf(tmp);
		if(i>=0)group_channels.get(i).addObserver(chatPanel);
		else{
			
			tmp.addObserver(chatPanel);
			private_channels.add(tmp);
		}
	}
	
	/** Entefert ein Chatpannel aus allen Kan�len
	 * @param chatPanel
	 */
	public	void	remove_MSGListener(Observer chatPanel){
		for (Kanal x : group_channels) {
			x.deleteObserver(chatPanel);
		}
		
		for (Kanal x : private_channels) {
			x.deleteObserver(chatPanel);
		}
		
		
	}
	
	
	/**Wir von der NodeEngine aufgerufen um f�r den User interressante Nachrichten an die ChatEngine zu �bermitteln
	 * @param nachricht Die neue Nachricht.
	 */
	public void put(MSG nachricht){
		inbox.add(nachricht);
	}
	
	/**Findet zu UserID zugeh�rigen Node in der Liste
	 * @param uid UserID
	 * @return Node-Objekt zu angegebenem User
	 */
	private Node getNodeforUser(long uid){
		for (Node x : nodes) {
			if(x.getUserID()==uid) return x;
		}
		return null;
	}
}


