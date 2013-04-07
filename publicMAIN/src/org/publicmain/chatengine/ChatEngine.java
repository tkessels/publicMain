package org.publicmain.chatengine;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
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
import org.publicmain.sql.DBConnection;

/**
 * @author ATRM
 *
 */

public class ChatEngine extends Observable{

	private static ChatEngine ce;
	public NodeEngine ne;
	public LogEngine log;
	public DBConnection db;
	private Set<Node> ignored;
	
	private long userID;
	private String alias;
	
	private Set<GruppenKanal> group_channels;
	private Set<KnotenKanal> private_channels;
	private KnotenKanal default_channel;
	
	private Thread msgSorterBot=new Thread(new MsgSorter());	//verteilt eingehende MSGs auf die Kanäle
//	private Thread neMaintenance;//warted die NE
	
	private BlockingQueue<MSG> inbox;
	
	//private Set<String> allGroups=new HashSet<String>();
	private Set<String> myGroups=new HashSet<String>();
	
	/**Liefert die Instanz der CE
	 * @return
	 */
	public static ChatEngine getCE() {
		return ce;
	}
	
	public ChatEngine() throws IOException{
		ce = this;
		//TODO:Load Settings & UserDATA
		//this.db = db.getDBConnection();
		
		//temporär
		 setUserID((long) (Math.random()*Long.MAX_VALUE));
		 setAlias(System.getProperties().getProperty("user.name")+(int)(Math.random()*100));
		
		 this.ne = new NodeEngine(this);
		
		group_channels=new HashSet<GruppenKanal>();
		private_channels=new HashSet<KnotenKanal>();
		default_channel=new KnotenKanal(getUserID());
		ignored=new HashSet<Node>();
		inbox=new LinkedBlockingQueue<MSG>();
		
		//temporäre Initialisierung der GruppenListe mit default Groups 
		ne.getGroups().addAll(Arrays.asList(new String[]{"public","hs5"}));
		//myGroups.addAll(ne.getGroups());
		// TODO:GUI müsste für all diese Gruppen je ein Fenster anlgegen beim Starten
		//TODO: All diese Gruppen müssten gejoint werden.
		
		msgSorterBot.start();
	}
	
	/**Findet zu NodeID zugehörigen Node in der Liste
	 * @param nid NodeID
	 * @return Node-Objekt zu angegebenem NodeID
	 */
	public Node getNode(long nid){
		return ne.getNode(nid);
	}
	
	public long getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	/**Gibt den aktuellen Anzeigenamen zurück 
	 * @return den Anzeigenamen
	 */
	public String getAlias() {
		return alias;
	}

	/**Verändert den Anzeigenamen des Nutzers
	 * @param alias neuer Anzeigename [a-zA-Z0-9]{12} 
	 */
	public void setAlias(String alias) {
		this.alias = alias;
		if(ne!=null&&ne.isOnline())ne.updateAlias();
	}
	
	/**Weisst die ChatEngine an einen <code>text</code> an den Nutzer mit der entsprechen <code>uid</code> zu schicken. 
	 * @param uid UID des Empfängers
	 * @param text Nachricht
	 */
	public void send_private(long uid, String text){
		MSG tmp = new MSG(uid,text);
		put(tmp);
		ne.sendtcp(tmp);
	}
	
	/**Weisst die ChatEngine an einen <code>text</code> an eine gruppe <code>group</code> zu schicken.
	 * @param group Gruppenbezeichnung
	 * @param text Nachricht
	 */
	public void send_group(String group, String text){
		MSG tmp = new MSG(group,text);
		put(tmp);
		ne.sendtcp(tmp);
	}
	
	/**Weisst die ChatEngine an einen <code>datei</code> an einen Nutzer mit der entsprechenden <code>uid</code> zu schicken.
	 * @param uid UID des Empfängers
	 * @param datei Datei
	 * @return id des Dateitransfers für spätere Rückfragen
	 */
	public int send_file(long uid, File datei){
		//TODO: CODE HERE
		return 0;
	}
	
	/** Gibt den Zustand der Übertragung einer Datei an
	 * @param file_transfer_ID
	 * @return <ul>	<li><code>-1</code> Dateitransfer nicht möglich</li>
	 * 				<li><code>-2</code> Benutzer lehnt transfer ab</li>
	 * 				<li><code>0</code> - <code>100</code> Vortschritt der Datenübertragung in Prozent 
	 */
	public int file_transfer_status(int file_transfer_ID){
		//TODO: CODE HERE
		return 0;
	}
	
	/**Fragt ein Array alle User ab 
	 * @return Array aller verbundener Nodes
	 */
	public	Set<Node> getUsers(){
		return ne.getNodes();
	}
	
	/** tritt einer Gruppe bei
	 * @param gruppen_name Gruppennamen sind CaseInSensitiv und bestehen aus alphanumerischen Zeichen
	 */
	public void group_join(String gruppen_name){
			synchronized (myGroups) {
				if(myGroups.add(gruppen_name)) {
					ne.joinGroup(Arrays.asList(gruppen_name), null);
				}
			}
	}
	
	/**verlässt eine gruppe wieder
	 * @param gruppen_name Gruppennamen sind CaseInSensitiv und bestehen aus alphanumerischen Zeichen
	 */
	public void group_leave(String gruppen_name){
		synchronized (myGroups) {
			if(myGroups.remove(gruppen_name)){
				ne.leaveGroup(Arrays.asList(gruppen_name), null);
			}
		}
	}
	
	/**Liefert eine Liste der verfügbaren Gruppenstrings
	 * @return Array der verfügbaren Gruppenstrings
	 */
	public	Set<String> getAllGroups(){
		synchronized (ne.getGroups()) {
			return ne.getGroups();
		}
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
	
	/**
	 * Veranlasst das Nachrichten vom user mit der <code>uid</code> wieder angezeigt werden.
	 * @param uid
	 */
	public void 	unignore_user(long uid){
		//TODO: CODE HERE
	}
	
	
	/** Meldet einen Nachrichten-Listener an einem Gruppen - Nachrichten Kanal an 
	 * @param chatPanel Das abonierende Fenster
	 * @param gruppen_name zu abonierender Gruppen Kanal
	 */
	public void add_MSGListener(Observer chatPanel,String gruppen_name){
		for (Kanal cur : group_channels) {
				if(cur.is(gruppen_name)) {
					cur.addObserver(chatPanel);
					return;
				}
			}
		GruppenKanal tmp =new GruppenKanal(gruppen_name);
		tmp.addObserver(chatPanel);
		group_channels.add(tmp);
		group_join(gruppen_name);
	}
	
	
	/** Meldet einen Nachrichten-Listener an einem privaten - Nachrichten Kanal an 
	 * @param chatPanel Das abonierende Fenster
	 * @param gruppen_name zu abonierender Gruppen Kanal
	 */
	public void add_MSGListener(Observer chatPanel,long uid){
		for (KnotenKanal cur : private_channels) {
				if(cur.is(uid)) {
					cur.addObserver(chatPanel);
					return;
				}
		}
		KnotenKanal tmp = new KnotenKanal(uid);
		tmp.addObserver(chatPanel);
		private_channels.add(tmp);
	}
	
	/** Entefert ein Chatpannel aus allen Kanälen
	 * @param chatPanel
	 */
	public	void	remove_MSGListener(Observer chatPanel){
		Set<Kanal> empty=new HashSet<Kanal>();
		
		for (Kanal x : group_channels) {
			x.deleteObserver(chatPanel);
			if(x.countObservers()==0) { //wenn kanal leer ist
				empty.add(x);
				group_leave((String) x.referenz);
			}
		}
		group_channels.removeAll(empty);
		
		empty.clear();
		for (Kanal x : private_channels) {
			x.deleteObserver(chatPanel);
			if(x.countObservers()==0)empty.add(x); //wenn kanal leer
		}
		private_channels.removeAll(empty);

	}
	
	/**Wir von der NodeEngine aufgerufen um für den User interressante Nachrichten an die ChatEngine zu übermitteln
	 * @param nachricht Die neue Nachricht.
	 */
	public void put(MSG nachricht){
		inbox.add(nachricht);
		if(db!=null)db.saveMsg(nachricht);
	}
	
	private final class MsgSorter implements Runnable {
		public void run() {
			while (true) {
				try {
					MSG tmp = inbox.take(); 
					LogEngine.log("msgSorterBot","sorting",tmp);
					if (tmp.getTyp() == NachrichtenTyp.GROUP) for (Kanal x : group_channels)if (x.add(tmp)) break;
					else if (tmp.getTyp() == NachrichtenTyp.PRIVATE) {
						for (KnotenKanal y : private_channels) if (y.add(tmp))break;
						//Kein CW angemeldet um die Nachricht aufzunehmen  sende es an GUI via DEFAULT CHANNEL
						
					}
				} catch (InterruptedException e) {//Unterbrochen beim Warten... hmmm ist das Schlimm?
				}
			}
		}
	}

	public Set<String> getMyGroups() {
		synchronized (myGroups) {
			return  myGroups;
		}
	}
	
	public void updateAlias(String newAlias) {
		setAlias(newAlias);
		ne.updateAlias();
	}

	public void debug(String command,String parameter) {
		switch (command) {
		case "alias":
			setAlias(parameter);
			break;
		default:
			ne.debug(command,parameter);
			break;
		}
		// TODO Auto-generated method stub
		
	}
}


