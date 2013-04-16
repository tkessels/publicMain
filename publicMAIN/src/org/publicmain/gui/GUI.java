package org.publicmain.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.images.Help;
import org.publicmain.chatengine.ChatEngine;
import org.publicmain.chatengine.KnotenKanal;
import org.publicmain.common.Config;
import org.publicmain.common.FileTransferData;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.Node;
import org.publicmain.sql.LocalDBConnection;

import com.nilo.plaf.nimrod.NimRODLookAndFeel;

/**
 * @author ATRM
 * 
 */

public class GUI extends JFrame implements Observer , ChangeListener{

	private final int GRP_NAME_LENGTH = Config.getConfig().getMaxGroupLength();
	private final int PRIV_NAME_LENGTH = Config.getConfig().getMaxAliasLength();
	
	// Deklarationen:
	private ChatEngine ce;
	LogEngine log;

	private static GUI me;
	private List<ChatWindow> chatList;
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenu configMenu;
	private JMenu lafMenu;
	private JMenu helpMenu;
	private JMenu historyMenu;
	private JMenu backupServer;
	private JMenuItem pullHistoryFromBackUpServer;
	private JMenuItem pushHistoryToBackUpServer;
	private JMenuItem backUpServerSettings;
	private JMenuItem checkoutHistory;
	private JMenuItem aboutPMAIN;
	private JMenuItem helpContents;
	private JMenuItem exit;
	private JMenuItem lafNimROD;
	private ButtonGroup btnGrp;
	
//	private DragableJTabbedPane jTabbedPane;
	private JTabbedPane jTabbedPane;
	private JToggleButton contactListBtn;
	private boolean contactListActive;
	private ContactList contactListWin;
	private pMTrayIcon trayIcon;
	private LocalDBConnection locDBCon;

	/**
	 * Konstruktor f�r GUI
	 */
	private GUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			log.log(ex);
		}

		// Initialisierungen:
		try {
			this.ce=new ChatEngine();
		} catch (Exception e) {
			log.log(e);
		}
		this.me 			= this;
		this.log 			= new LogEngine();
		//this.locDBCon 		= LocalDBConnection.getDBConnection();
		this.aboutPMAIN 	= new JMenuItem("About pMAIN");
//		this.helpContents	= new JMenuItem("Help Contents", Help.getIcon("helpContentsIcon.png")));	// evtl. noch anderes Icon w�hlen
		this.helpContents	= new JMenuItem("Help Contents", Help.getIcon("helpContentsIcon.png"));	// evtl. noch anderes Icon w�hlen
		this.exit			= new JMenuItem("Exit");
		this.lafMenu		= new JMenu("Switch Design");
		this.btnGrp 		= new ButtonGroup();
		this.chatList 		= Collections.synchronizedList(new ArrayList<ChatWindow>());
		//this.jTabbedPane 	= new DragableJTabbedPane();
		this.jTabbedPane 	= new JTabbedPane();
		this.contactListBtn = new JToggleButton(Help.getIcon("UserListAusklappen.png"));
		this.contactListActive = false;
		this.menuBar 		= new JMenuBar();
		this.fileMenu 		= new JMenu("File");
		this.configMenu 	= new JMenu("Settings");
		this.helpMenu 		= new JMenu("Help");
		this.historyMenu 	= new JMenu("History");
		this.backupServer	= new JMenu("Backup-Server");
		this.pushHistoryToBackUpServer 		= new JMenuItem("push History to Backup-Server");
		this.pullHistoryFromBackUpServer	= new JMenuItem("pull History from Backup-Server");
		this.backUpServerSettings 			= new JMenuItem("Backup-Server Settings");
		this.checkoutHistory				= new JMenuItem("checkout History");
		this.lafNimROD 		= new JRadioButtonMenuItem("NimROD");
		this.trayIcon 		= new pMTrayIcon();
		
		// Anlegen der Men�eintr�ge f�r Designwechsel (installierte
		// LookAndFeels)
		// + hinzuf�gen zum lafMenu ("Designwechsel")
		// + hinzuf�gen der ActionListener (lafController)
		for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
			JRadioButtonMenuItem tempJMenuItem = new JRadioButtonMenuItem(laf.getName());
			if((laf.getName().equals("Windows")) &&
					(UIManager.getSystemLookAndFeelClassName().equals("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"))){
				tempJMenuItem.setSelected(true);
			}
			lafMenu.add(tempJMenuItem);
			btnGrp.add(tempJMenuItem);
			tempJMenuItem.addActionListener(new lafController(lafMenu, laf));
		}

		// Anlegen ben�tigter Controller und Listener:
		// WindowListener f�r das GUI-Fenster:
		this.addWindowListener(new winController());
		
		// ChangeListener f�r Focus auf Eingabefeld
		this.jTabbedPane.addChangeListener(this);

		// ActionListener f�r Menu's:
		this.exit.addActionListener(new menuContoller());
		this.aboutPMAIN.addActionListener(new menuContoller());
		this.helpContents.addActionListener(new menuContoller());
		this.lafNimROD.addActionListener(new lafController(lafNimROD, null));
		this.checkoutHistory.addActionListener(new menuContoller());
		this.backUpServerSettings.addActionListener(new menuContoller());
		
		// Konfiguration contactListBtn:
		this.contactListBtn.setMargin(new Insets(2, 3, 2, 3));
		this.contactListBtn.setToolTipText("show contacts");
		this.contactListBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JToggleButton source = (JToggleButton) e.getSource();
				if (source.isSelected()) {
					contactListAufklappen();
				} else {
					contactListZuklappen();
				}
			}
		});
		
		//lafNimROD zur ButtonGroup btnGrp hinzuf�gen:
		this.btnGrp.add(lafNimROD);
		
		// Men�s hinzuf�gen:
		this.lafMenu.add(lafNimROD);
		
		this.configMenu.add(lafMenu);
		
		this.fileMenu.add(exit);
		
		this.helpMenu.add(aboutPMAIN);
		this.helpMenu.add(helpContents);
		
		this.historyMenu.add(checkoutHistory);
		this.historyMenu.add(backupServer);
		
		this.backupServer.add(pushHistoryToBackUpServer);
		this.backupServer.add(pullHistoryFromBackUpServer);
		this.backupServer.add(backUpServerSettings);
		
		this.menuBar.setLayout(new BoxLayout(menuBar, BoxLayout.LINE_AXIS));
		this.menuBar.add(contactListBtn);
		this.menuBar.add(fileMenu);
		this.menuBar.add(configMenu);
		this.menuBar.add(helpMenu);
		this.menuBar.add(historyMenu);
		
		//Einkommentieren wenn Logo gew�nscht:
//		this.menuBar.add(Box.createHorizontalGlue());
//		this.menuBar.add(new JLabel(new ImageIcon(Help.class.getResource("miniSpin.gif"))));

		// GUI Komponenten hinzuf�gen:
		this.setJMenuBar(menuBar);
		this.add(jTabbedPane);
		
		// StandardGruppe erstellen:
		this.addGrpCW("public", true);
		// StandardGruppe joinen:
		//this.ce.group_join("public"); //�berfl�ssig weil die addGrpCW das macht
		
		//registriert Hauptfenster als Empf�nger f�r noch nicht gefangene Privatnachrichten
		this.ce.register_defaultMSGListener(this);

		// GUI JFrame Einstellungen:
		this.setIconImage(Help.getIcon("pM_Logo2.png").getImage());
		this.setMinimumSize(new Dimension(250,250));
		this.pack();
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setTitle("publicMAIN");
		this.contactListWin = new ContactList(me);
		this.setVisible(true);
		this.chatList.get(0).focusEingabefeld();
		this.contactListAufklappen();
		
	}
	
	/**
	 * Diese Methode klappt die Contactlist auf
	 * 
	 * 
	 */
	private void contactListAufklappen(){

		if(!contactListActive){
			
			this.contactListBtn.setToolTipText("hide contacts");
			this.contactListBtn.setIcon(Help.getIcon("UserListEinklappen.png"));
			this.contactListBtn.setSelected(true);
			this.contactListWin.repaint();
			this.contactListWin.setVisible(true);
			contactListActive = true;
		}

	}
	
	/**
	 * Diese Methode klappt die Contactlist zu
	 * 
	 * 
	 */
	private void contactListZuklappen(){

		if(contactListActive){
			
			this.contactListBtn.setToolTipText("show contacts");
			this.contactListBtn.setIcon(Help.getIcon("UserListAusklappen.png"));
			this.contactListBtn.setSelected(false);
			
			this.contactListWin.setVisible(false);
			
			this.contactListActive = false;
		}
	}
	

	/** Diese Methode erstellt ein neues ChatFenster. Ist das der Parameter ein {@link String} erstellt sie ein Gruppenfenster ist er {@link Long} ein PrivatChatFenster
	 * @param referenz F�r was soll ein Fenster erstellt werden. 
	 * @return
	 */
	public ChatWindow createChat(Object referenz) {
		
		ChatWindow cw;
		if(referenz instanceof String)cw=new ChatWindow((String)referenz);
		else if (referenz instanceof Long)cw=new ChatWindow((Long)referenz);
		else return null;

		// Title festlegen:
		String title = cw.getChatWindowName();
		

		// neues ChatWindow (cw) zur Chatliste (ArrayList<ChatWindow>)
		// hinzuf�gen:
		this.chatList.add(cw);
		// erzeugen von neuem Tab f�r neues ChatWindow:
		this.jTabbedPane.addTab(title, cw);

		// Index vom ChatWindow im JTabbedPane holen um am richtigen Ort
		// einzuf�gen:
		int index = jTabbedPane.indexOfComponent(cw);
		// den neuen Tab an die Stelle von index setzen:
		this.jTabbedPane.setTabComponentAt(index, cw.getWindowTab());
		
		if(cw.isGroup()) ce.add_MSGListener(cw, (String) referenz);
		else ce.add_MSGListener(cw, (Long) referenz);
		
		return cw;
	}

	/**
	 * Diese Methode erstellt ein ChatWindow f�r Gruppen, falls ChatWindow bereits vorhanden, wird dieses fokusiert.
	 * @param grpName
	 * @param focus TODO
	 */
	public void addGrpCW(String grpName, boolean focus){
		// Gruppenname auf Konvention pr�fen, ggf. �nderungen vornehmen.
		String clean_group = sanatizeGroupname(grpName);
		// Hol ref. auf Gruppenfenster wenn existent
		ChatWindow tmp_cw=getCW(clean_group);
		//wenn ref. leer dann erstelle neues Gruppenfesnter
		if(tmp_cw == null)  tmp_cw=createChat(clean_group);
		//fokusiere das Gruppenfenster
		if(focus)focus(tmp_cw);
	}

	/** S�ubert einen eingegebenen Gruppenstring von ungewollten Zeichen
	 * @param grpName Der vom Benutzer eingegebene Gruppenname
	 * @return einen ges�uberten String zu verwendung als Gruppenname
	 */
	private String sanatizeGroupname(String grpName) {
		String clean_grpname = grpName;
		if(clean_grpname.length() > GRP_NAME_LENGTH){
			clean_grpname = clean_grpname.substring(0, GRP_NAME_LENGTH); 
		}
		clean_grpname = clean_grpname.trim();
		clean_grpname = clean_grpname.replaceAll("[^a-zA-Z0-9\\-_]", "");
//		grp_name = grp_name.replaceAll("[&#*?\\/@<>�\\t\\n\\x0B\\f\\r]*", "");
		clean_grpname = clean_grpname.toLowerCase();
		return clean_grpname;
	}
	
	/**
	 * @param uid
	 * @param focus
	 */
	public void addPrivCW(long uid,boolean focus){
		ChatWindow tmp = getCW(uid);
		if(tmp == null)tmp=createChat(uid);
		if (focus) focus(tmp);
	}

	/**Fokussiert das angegebene ChatWindow
	 * @param cw Das zu fokussierende Chatwindow
	 */
	private void focus(ChatWindow cw) {
		int index = jTabbedPane.indexOfComponent(cw);
		if(index>=0)jTabbedPane.setSelectedIndex(index);
	}
	
	/**
	 * Diese Methode entfernt ein ChatWindow
	 * 
	 * Diese Methode sorgt daf�r das ChatWindows aus der ArrayList "chatList"
	 * entfernt werden und im GUI nicht mehr angezeigt werden.
	 * 
	 * @param ChatWindow
	 */
	public void delChat(ChatWindow cw) {
		// TODO: Hier evtl. noch anderen Programmablauf implementier
		// z.B. schlie�en des Programms wenn letztes ChatWindow geschlossen
		// wird
		
		// Falls nur noch ein ChatWindow �brig kann dieses nicht entfernt werden
		if(chatList.size() >= 2){
			// ChatWindow (cw) aus jTabbedPane entfernen:
			this.jTabbedPane.remove(cw);
			// ChatWindow aus Chatliste entfernen:
			this.chatList.remove(cw);
			// ChatWindow aus Gruppe entfernen (MSGListener abschalten):
			ce.remove_MSGListener(cw);
		}
	}
	
	/**
	 * Diese Methode entfernt ein ChatWindow anhand des Namens
	 * 
	 * Diese Methode sorgt daf�r das ChatWindows aus der ArrayList "chatList"
	 * entfernt werden und im GUI nicht mehr angezeigt werden.
	 * 
	 * @param chatname
	 */
	public void delChat(Object refObject){
		delChat(getCW(refObject));
	}
	
	/**
	 * Diese Methode nimmt die �nderungsanforderung entgegen und pr�ft ob der Name bereits an einen anderen
	 * Benutzer oder an eine Gruppe vergeben wurde. Existiert der Alias wird false zur�ckgeliefert in jedem
	 * anderen Fall wird der Name �ber die ChatEngine ge�ndert.
	 *   
	 * @param alias
	 * @return boolean
	 */
	public boolean changeAlias(String alias){
		if(ce.getNodeforAlias(alias)!=null){
			System.out.println("GUI: Name existiert bereits! Wird nicht ge�ndert!");
			return false;
		} else {
			//TODO: ChatWindowTab Name �ndern;
			ce.setAlias(alias);
			return true;
		}
	}
	
	public ChatWindow getActiveCW(){
		return (ChatWindow) jTabbedPane.getSelectedComponent();
	}
	
	
	
	/**
	 * F�hrt das Programm ordnungsgem�� runter
	 */
	void shutdown() {
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				System.exit(0);
			}
		}).start();
		LogEngine.log(this, "Shutdown initiated!", LogEngine.INFO);
		ce.shutdown();
		if(locDBCon!=null)locDBCon.shutdownLocDB();
	}

	/**
	 * Diese Methode pr�ft ob ein ChatWindow bereits vorhanden ist
	 * 
	 * Diese Methode pr�ft ob ein ChatWindow vorhanden ist falls ja wird dieses returned
	 * falls nein wird null returned
	 * @param empfGrp Name Chatwindow
	 * @return ChatWindow or null 
	 */
	private ChatWindow existCW(ChatWindow referenz){
//		for(ChatWindow x : chatList){
//			if(x.getChatWindowName().equals(chatWindowname)){
//				return x;
//			}
//		}
//		return null;
		int index = chatList.indexOf(referenz);
		return (index>=0)?chatList.get(index):null;
	}
	
	/** Die Methode  liefert das zu einer Referenz geh�rende {@link ChatWindow}.
	 *    
	 *    <br>Sie ersetzt die alte <code>existCW</code> und liefert ein ChatWindow zu einer Referenz. Die Referenz muss zwansl�ufig entweder ein {@link String} oder ein {@link Long}.
	 *    Ein <code>String</code> bezieht sich auf GruppenChatFenster und liefert falls vorhanden das ChatFenster zu dieser Gruppe. Ein <code>Long</code> liefert entsprechend das 
	 *    PrivatChatFenster wenn der �bergebene wert eine g�ltige UID war und einFenster zu diesem Nutzer bereits existiert. Sollte eine ung�ltige Referenz �bergeben werden oder kein
	 *     Fenster zu dieser Referenz existieren liefert die Methode <code>null</code> zur�ck.
	 * @param referenz Der Gruppenname (String) oder die UserID (Long) zu der ein Fenster gesucht werden soll.
	 * @return die laufende Instanz des Chatwindows zur angegebenen Referenz oder <code>null</code> falls keine Instanz gefunden.
	 */
	private ChatWindow getCW(Object referenz){
		if(referenz!=null) {
			for (ChatWindow cur : chatList) if (cur.equals(referenz)) return cur;
		}
		return null;
	}
	
	/**
	 * Diese Methode wird in einem privaten ChatWindow zum versenden der Nachricht verwendet
	 * @param empfUID long Empf�ngerUID
	 * @param msg String die Nachricht
	 */
	void privSend(long empfUID,String msg){
		ce.send_private(empfUID, msg);
	}
	
	/**
	 * Diese Methode sendet eine private Nachricht durch /w
	 * 
	 * Diese Methode wird vom ChatWindow durch die Eingabe von /w aufgerufen
	 * zun�chst wird gepr�ft ob schon ein ChatWindow f�r den privaten Chat existiert
	 * falls nicht wird eines angelegt und die private nachricht an die UID versendet
	 * @param empfAlias String Empf�nger Alias
	 * @param msg String die Nachricht
	 *//*
	void privSend(String empfAlias, String msg){
		ChatWindow tmpCW = existCW(empfAlias);
		long tmpUID = -1;
		tmpUID = ce.getNodeforAlias(empfAlias).getUserID();
		if(tmpUID != -1){
			if(tmpCW == null){
				addPrivCW(tmpUID);
//					tabNr = jTabbedPane.indexOfComponent(existCW(empfAlias));
//					jTabbedPane.setSelectedIndex(tabNr);
			} else {
				jTabbedPane.setSelectedIndex(jTabbedPane.indexOfComponent(tmpCW));
			}
			ce.send_private(tmpUID, msg);
		}
	}
	*/
	/**
	 * Diese Methode wird f�r das Senden von Gruppennachrichten verwendet
	 * Falls noch kein ChatWindow f�r diese Gruppe besteht wird eines erzeugt.
	 * @param empfGrp String Empf�ngergruppe
	 * @param msg String die Nachricht/Msg
	 * @param cw ChatWindow das aufrufende ChatWindow
	 */
	void groupSend(String empfGrp, String msg){
		
		ChatWindow tmpCW = getCW(empfGrp);
		if(tmpCW == null){
			tmpCW= new ChatWindow(empfGrp);
			addGrpCW(empfGrp, true);
			focus(tmpCW);
		} else {
			focus(tmpCW);
		}
		ce.send_group(empfGrp, msg);
	}
	
	/**
	 * Diese Methode ist f�r das Ignorien eines users
	 * @param alias String Alias des Users
	 * @returns true Wenn User gefunden
	 */
	boolean ignoreUser(long uid){
		return ce.ignore_user(uid);
	}

	/**
	 * Diese Methode ist f�r das nicht weitere Ignorieren eines users
	 * @param alias String Alias des Users
	 * @return true Wenn User gefunden
	 */
	boolean unignoreUser(long uid){
			return ce.unignore_user(uid);
	}
	

	public void sendFile(long uid) {
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog(me);
		File selectedFile = fileChooser.getSelectedFile();
		if(selectedFile!=null)sendFile(selectedFile, uid);
	}
	
	public void sendFile(File datei, long uid) {
		if(datei.isFile()) {
			if(datei.canRead()) {
				if(datei.length()>0)ce.send_file(datei, uid);
				else info("File has a size of 0 bytes!", uid, 3);
			}
			else info("Cant read file \""+datei.getName()+"\"!", uid, 3);
		}
		else info("Only single Files are supported!", uid, 3);
	}
	
	
	/**Displays a text message in the referenced ChatWindow (Group/UID) or the active Window if reference is <code>null</code>
	 * @param nachricht Text of the message
	 * @param reference Groupname or UID of ChatWindow to put the message in or <code>null</code> to take the active one. 
	 * @param typ <ul><li>0 - info<li>1 - warning<li>2 - error</ul>
	 */
	public void info(String nachricht, Object reference,int typ) {
		ChatWindow tmp=getCW(reference);;
		if(tmp==null)tmp=getActiveCW();
		if(tmp!=null){
			if (typ == 0)tmp.info(nachricht);
			else if (typ == 1)tmp.warn(nachricht);
			else tmp.error(nachricht);
		}
	}
	
	
	/**
	 * Diese Methode liefert ein Fileobjekt
	 * 
	 * Diese Methode bittet die GUI(den Nutzer) um ein Fileobjekt zur Ablage der
	 * empfangenen Datei
	 * @param filename TODO
	 * 
	 * @return File
	 */
	public File request_File(FileTransferData fr) {
		String dateiname = fr.datei.getName();
		Long size = fr.size;
		int x = JOptionPane.showConfirmDialog(null, "M�chten sie eine die Datei "+dateiname+ " annehmen? ("+size +")","Dateiversand",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
		if(x==JOptionPane.YES_OPTION) {
			JFileChooser fileChooser = new JFileChooser();
			if(dateiname!=null)fileChooser.setSelectedFile(new File(dateiname));
			int returnVal = fileChooser.showSaveDialog(me);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				System.out.println("You chose to save this file: " + fileChooser.getSelectedFile().getName());
			}
			return fileChooser.getSelectedFile();
		}else {
			return null;
		}
		
	}

	/**
	 * Diese Methode soll �ber �nderungen informieren
	 */
	public void notifyGUI() {
		for (ChatWindow cw : chatList) {
			cw.updateName();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		//FIXME : vielleicht nochmal �berarbeiten... wenn Zeit ist
		if(o instanceof KnotenKanal){
			MSG tmp = (MSG) arg;
			Node tmp_node = ce.getNodeForNID(tmp.getSender());
			me.addPrivCW(tmp_node.getUserID(), false);
			ce.put(tmp);
		}
	}

	/**
	 * Diese Methode stellt das Node bereit
	 * 
	 * Diese Methode holt das {@link NODE}-Objekt f�r eine UserID
	 * 
	 * @param uid
	 * @return Node
	 */
	public Node getNodeForUID(long uid) {
		return ce.getNodeForUID(uid);
	}
	
	/**
	 * Diese Methode stellt das GUI bereit
	 * 
	 * Diese Methode stellt das GUI f�r andere Klassen bereit um einen Zugriff
	 * auf GUI Attribute zu erm�glichen
	 * 
	 * @return GUI
	 */
	public static GUI getGUI() {
		if (me == null) {
			me = new GUI();
		}
		return me;
	}
	
	/**
	 * @return
	 */
	JTabbedPane getTabbedPane(){
		return this.jTabbedPane;
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		((ChatWindow)jTabbedPane.getSelectedComponent()).focusEingabefeld();
	}
	
	/**
	 * ActionListener f�r Design wechsel (LookAndFeel)
	 * 
	 * hier wird das Umschalten des LookAndFeels im laufenden Betrieb erm�glicht
	 * 
	 * @author ABerthold
	 * 
	 */
	class lafController implements ActionListener {

		private JMenuItem lafMenu;
		private UIManager.LookAndFeelInfo laf;
		private boolean userListWasActive;

		public lafController(JMenuItem lafMenu, UIManager.LookAndFeelInfo laf) {
			this.lafMenu = lafMenu;
			this.laf = laf;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			userListWasActive = contactListActive;
			JMenuItem source = (JMenuItem)e.getSource();
			
			contactListZuklappen();
			
			if(source.getText().equals("NimROD")){
				try{
					UIManager.setLookAndFeel(new NimRODLookAndFeel());
				} catch (Exception ex){
					LogEngine.log(ex);
				}
			} else {
				try {
					UIManager.setLookAndFeel(laf.getClassName());
				} catch (Exception ex) {
					LogEngine.log(ex);
				}
			}
			SwingUtilities.updateComponentTreeUI(GUI.me);
			GUI.me.pack();
			if(userListWasActive)contactListAufklappen();
			
		}
	}
	
	/**
	 * ActionListener f�r Menu's
	 * 
	 * @author ABerthold
	 *
	 */
	class menuContoller implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			
			JMenuItem source = (JMenuItem)e.getSource();
			
			switch(source.getText()){
			
			case "Exit":
				shutdown();
				break;
			case "About pMAIN":
				new AboutPublicMAIN(me, "About publicMAIN", true);
				break;
			case "Help Contents":
				//TODO: HelpContents HTML schreiben
				new HelpContents();
				break;
			case "checkout History":
				new checkoutHistoryWindow();
				break;
			case "Backup-Server Settings":
				//TODO: hier noch eine vern�nfige variante der Implementierung
				registrationWindow.getRegistrationWindow();
				break;
			}
			
		}
		
	}
	
	/**
	 * WindowListener f�r GUI
	 * 
	 * 
	 * 
	 * @author ABerthold
	 *
	 */
	class winController implements WindowListener{
		public void windowOpened(WindowEvent arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		// Wird das GUI minimiert wird die Userlist zugeklappt und der
		// userListBtn zur�ckgesetzt:
		public void windowIconified(WindowEvent arg0) {
			if (contactListBtn.isSelected()) {
				contactListZuklappen();
			}
		}
		@Override
		public void windowDeiconified(WindowEvent arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void windowDeactivated(WindowEvent arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void windowClosing(WindowEvent arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void windowClosed(WindowEvent arg0) {
			contactListZuklappen();
			shutdown();
			// Object[] eventCache =
			// {"super, so ne scheisse","deine Mama liegt im Systemtray"};
			// Object anchor = true;
			// JOptionPane.showInputDialog(me,
			// "pMAIN wird ins Systemtray gelegt!",
			// "pMAIN -> Systemtray", JOptionPane.PLAIN_MESSAGE, new
			// ImageIcon("media/pM16x16.png"), eventCache, anchor);
		}
		@Override
		public void windowActivated(WindowEvent arg0) {
			if (contactListBtn.isSelected()) {
				contactListWin.toFront();
			}
		}
	}
	
	/**
	 * Diese Methode gibt die Default Settings des aktuellen L&F in der Console aus
	 */
	private void getLookAndFeelDefaultsToConsole(){
		UIDefaults def = UIManager.getLookAndFeelDefaults();
		Vector<?> vec = new Vector<Object>(def.keySet());
		Collections.sort(vec, new Comparator<Object>() {
			public int compare(Object arg0, Object arg1) {
				return arg0.toString().compareTo(arg1.toString());
			}
		});
		for (Object obj : vec) {
			System.out.println(obj + "\n\t" + def.get(obj));
		}
	}




}
