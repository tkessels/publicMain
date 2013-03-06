import java.io.File;
import java.util.List;
import java.util.Observer;



/**
 * @author tkessels
 *
 */
public class ChatEngine {
	private static ChatEngine ce;
	public NodeEngine ne;
	public LogEngine log;
	public GUI gui;
	private List<Node> nodes;
	
	
	public static synchronized ChatEngine getCE(){
		if(ce==null) ce=new ChatEngine();
		return ce;
	}

	
	/**Weisst die ChatEngine an einen <code>text</code> an den Nutzer mit der entsprechen <code>uid</code> zu schicken. 
	 * @param uid UID des Empfängers
	 * @param text Nachricht
	 */
	public void send_private(long uid, String text){
		//TODO: CODE HERE
	}
	
	/**Weisst die ChatEngine an einen <code>text</code> an eine gruppe <code>group</code> zu schicken.
	 * @param group Gruppenbezeichnung
	 * @param text Nachricht
	 */
	public void send_group(String group, String text){
		//TODO: CODE HERE	
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
	public	Node[]	getUsers(){
		return (Node[]) nodes.toArray();
	}
	
	/** tritt einer Gruppe bei
	 * @param gruppen_name Gruppennamen sind CaseInSensitiv und bestehen aus alphanumerischen Zeichen
	 */
	public void group_join(String gruppen_name){
		//TODO: CODE HERE
	}
	
	/**verlässt eine gruppe wieder
	 * @param gruppen_name Gruppennamen sind CaseInSensitiv und bestehen aus alphanumerischen Zeichen
	 */
	public	void	group_leave(String gruppen_name){
		//TODO: CODE HERE		
	}
	
	/**Liefert eine Liste der verfügbaren Gruppenstrings
	 * @return Array der verfügbaren Gruppenstrings
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
		return gui.request_File();
	}
	
	/**Veranlasst das Nachrichten vom user mit der <code>uid</code> nicht mehr angezeigt werden.
	 * @param uid
	 */
	public	void	ignore_user(long uid){
		//TODO: CODE HERE
	}
	
	/**Verändert den Anzeigenamen des Nutzers
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
		//TODO:Code Here
	}
	
	
	/** Meldet einen Nachrichten-Listener an einem privaten - Nachrichten Kanal an 
	 * @param chatPanel Das abonierende Fenster
	 * @param gruppen_name zu abonierender Gruppen Kanal
	 */
	public void	add_MSGListener(Observer chatPanel,long UID){
		//TODO:Code Here		
	}
	/** Entefert ein Chatpannel aus allen Kanälen
	 * @param chatPanel
	 */
	public	void	remove_MSGListener(Observer chatPanel){
		//TODO:Code Here		
	}
	public	void	put(MSG nachricht){
		//WAS SOLL DAS HIER NOCHMAL KÖNNEN?
		
	}

	

}


