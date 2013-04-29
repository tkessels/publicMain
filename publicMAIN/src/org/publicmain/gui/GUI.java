
package org.publicmain.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.chatengine.KnotenKanal;
import org.publicmain.common.Config;
import org.publicmain.common.FileTransferData;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.Node;
import org.publicmain.sql.LocalDBConnection;
import org.resources.Help;


/**
 * @author ATRM
 * 
 */

public class GUI extends JFrame implements Observer, ChangeListener {

	private final int NAME_LENGTH = Config.getConfig().getMaxGroupLength();

	// Deklarationen:
	private ChatEngine ce;
	LogEngine log;

	private static GUI me;
	private List<ChatWindow> chatList;
	private JMenuBar menuBar;
	private JMenu pMAIN;
	private JMenu history;
	private JMenu help;
	private JMenu backupServer;
	private JMenuItem pullHistory;
	private JMenuItem pushHistory;
	private JMenuItem settings;
	private JMenuItem localHistory;
	private JMenuItem about;
	private JMenuItem helpContent;
	private JMenuItem exit;
	private JTabbedPane jTabbedPane;
	private JToggleButton contactListBtn;
	private boolean contactListActive;
	private ContactList contactListWin;
	private PMTrayIcon trayIcon;
	private LocalDBConnection locDBCon;
	private HTMLContentDialog hcdAbout;
	private HTMLContentDialog hcdHelp;

	/**
	 * Konstruktor für das GUI mit Initialisierungen
	 */
	private GUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			LogEngine.log(ex);
		}

		// Initialisierungen:
		try {
			this.ce = new ChatEngine();
		} catch (Exception e) {
			LogEngine.log(e);
		}
		
		GUI.me 					= this;
		this.log 				= new LogEngine();
		// this.locDBCon 		= LocalDBConnection.getDBConnection();
		this.about 				= new JMenuItem("About", Help.getIcon("aboutSym.png"));
		// TODO: evtl. noch anderes Icon wählen
		this.helpContent		= new JMenuItem("Help Contents", Help.getIcon("helpContentsIcon.png"));
		this.exit				= new JMenuItem("Exit", Help.getIcon("exitSym.png"));
		this.chatList 			= Collections.synchronizedList(new ArrayList<ChatWindow>());
		this.jTabbedPane 		= new JTabbedPane();
		this.contactListBtn 	= new JToggleButton(Help.getIcon("g18025.png"));
		this.contactListActive 	= false;
		this.menuBar 			= new JMenuBar();
		this.pMAIN	 			= new JMenu("pMAIN");
		this.history 			= new JMenu("History");
		this.help	 			= new JMenu("Help");
		this.backupServer		= new JMenu("Backup-Server");
		this.pushHistory		= new JMenuItem("Push History");
		this.pullHistory		= new JMenuItem("Pull History");
		this.settings 			= new JMenuItem("Settings", Help.getIcon("settingsSym.png"));
		this.localHistory		= new JMenuItem("Local");
		this.trayIcon 			= new PMTrayIcon();
		
		/**
		 * Erstellen erforderlicher Controller und Listener
		 */
		this.addWindowListener(new winController());				// WindowListener für das GUI-Fenster
		this.jTabbedPane.addChangeListener(this);					// ChangeListener für den Focus auf dem Eingabefeld
		this.exit.addActionListener(new menuContoller());			
		this.about.addActionListener(new menuContoller());
		this.helpContent.addActionListener(new menuContoller());
		this.localHistory.addActionListener(new menuContoller());
		this.settings.addActionListener(new menuContoller());
		
		this.contactListBtn.setMargin(new Insets(2, 3, 2, 3));
		this.contactListBtn.setToolTipText("show contacts");
		this.contactListBtn.addActionListener(new ActionListener() {

			/**
			 * TODO: Kommentar
			 */
			public void actionPerformed(ActionEvent e) {
				JToggleButton source = (JToggleButton) e.getSource();
				if (source.isSelected()) {
					contactListAufklappen();
				} else {
					contactListZuklappen();
				}
			}
		});
		
		/**
		 * Menü-Komponenten hinzufügen
		 */
		this.history.add(localHistory);
		this.history.add(backupServer);
		this.pMAIN.add(settings);
		this.pMAIN.add(exit);
		this.help.add(helpContent);
		this.help.add(about);
		this.backupServer.add(pushHistory);
		this.backupServer.add(pullHistory);
		this.menuBar.setLayout(new BoxLayout(menuBar, BoxLayout.LINE_AXIS));
		this.menuBar.add(contactListBtn);
		this.menuBar.add(pMAIN);
		this.menuBar.add(history);
		this.menuBar.add(help);

		// Einkommentieren wenn Logo gewünscht:
		// this.menuBar.add(Box.createHorizontalGlue());
		// this.menuBar.add(new JLabel(new
		// ImageIcon(Help.class.getResource("miniSpin.gif"))));

		// GUI Komponenten hinzufügen:
		this.setJMenuBar(menuBar);
		this.add(jTabbedPane);

		// StandardGruppe erstellen:
		this.addGrpCW("public", true);
		// StandardGruppe joinen:

		// Registriert Hauptfenster als Empfänger für noch nicht gefangene
		// Privatnachrichten.
		this.ce.register_defaultMSGListener(this);

		// GUI JFrame Einstellungen
		this.setIconImage(Help.getIcon("pM_Logo.png",64).getImage());
		this.getContentPane().setBackground(Color.WHITE);
		this.setMinimumSize(new Dimension(250, 250));
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
	 * Diese Methode klappt die Benutzerliste auf.
	 */
	private void contactListAufklappen(){

		if(!contactListActive){
			
			this.contactListBtn.setToolTipText("hide contacts");
			this.contactListBtn.setIcon(Help.getIcon("g20051.png"));
			this.contactListBtn.setSelected(true);
			this.contactListWin.repaint();
			this.contactListWin.setVisible(true);
			contactListActive = true;
		}
	}
	
	/**
	 * Diese Methode klappt die Benutzerliste zu.
	 */
	private void contactListZuklappen() {

		if (contactListActive) {
			this.contactListBtn.setToolTipText("show contacts");
			this.contactListBtn.setIcon(Help.getIcon("g18025.png"));
			this.contactListBtn.setSelected(false);
			this.contactListWin.setVisible(false);
			this.contactListActive = false;
		}
	}

	/** 
	 * Diese Methode erstellt ein neues ChatFenster. Ist das der Parameter ein {@link String} erstellt
	 * sie ein Gruppenfenster ist er {@link Long} ein PrivatChatFenster.
	 * 
	 * @param referenz, für was soll ein Fenster erstellt werden. 
	 * @return
	 */
	public ChatWindow createChat(Object referenz) {

		ChatWindow cw;
		if (referenz instanceof String) {
			cw = new ChatWindow((String) referenz);
		} else if (referenz instanceof Long) {
			cw = new ChatWindow((Long) referenz);
		} else {
			return null;
		}

		// Titel festlegen
		String title = cw.getChatWindowName();

		// Neues ChatWindow (cw) zur Chatliste (ArrayList<ChatWindow>)
		// hinzufügen
		this.chatList.add(cw);
		// erzeugen von neuem Tab für neues ChatWindow
		this.jTabbedPane.addTab(title, cw);
		// Index vom ChatWindow im JTabbedPane holen um am richtigen Ort
		// einzufügen
		int index = jTabbedPane.indexOfComponent(cw);
		// den neuen Tab an die Stelle von index setzen
		this.jTabbedPane.setTabComponentAt(index, cw.getWindowTab());
		if (cw.isGroup()) {
			ce.add_MSGListener(cw, (String) referenz);
		} else {
			ce.add_MSGListener(cw, (Long) referenz);
		}

		return cw;
	}

	/**
	 * Diese Methode erstellt ein ChatWindow für Gruppen, falls ChatWindow
	 * bereits vorhanden, wird dieses fokusiert.
	 * 
	 * @param grpName
	 * @param focus
	 */
	public void addGrpCW(String grpName, boolean focus) {
		// Hol ref. auf Gruppenfenster wenn existent
		ChatWindow tmp_cw = getCW(grpName);
		// wenn ref. leer dann erstelle neues Gruppenfesnter
		if (tmp_cw == null) {
			tmp_cw = createChat(grpName);
		}
		// fokusiere das Gruppenfenster
		if (focus) {
			focus(tmp_cw);
		}
	}

	/**
	 * TODO: Kommentar
	 * 
	 * @param uid
	 * @param focus
	 */
	public void addPrivCW(long uid,boolean focus){
		ChatWindow tmp = getCW(uid);
		if(tmp == null)tmp=createChat(uid);
		if (focus) focus(tmp);
	}

	/**
	 * Diese Methode entfernt ein ChatWindow
	 * 
	 * Diese Methode sorgt dafür das ChatWindows aus der ArrayList "chatList"
	 * entfernt werden und im GUI nicht mehr angezeigt werden.
	 * 
	 * @param ChatWindow
	 */
	public void delChat(ChatWindow cw) {
		// TODO: Hier evtl. noch anderen Programmablauf implementier
		// z.B. schließen des Programms wenn letztes ChatWindow geschlossen
		// wird
		
		// Falls nur noch ein ChatWindow übrig kann dieses nicht entfernt werden
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
	 * Diese Methode entfernt ein ChatWindow anhand des Namens, sie sorgt dafür
	 * das ChatWindows aus der ArrayList "chatList" entfernt werden und im GUI
	 * nicht mehr angezeigt werden.
	 * 
	 * @param chatname
	 */
	public void delChat(Object refObject) {
		delChat(getCW(refObject));
	}
	
	/**
	 * Fokussiert das angegebene ChatWindow
	 * 
	 * @param cw, das zu fokussierende Chatwindow
	 */
	private void focus(ChatWindow cw) {
		int index = jTabbedPane.indexOfComponent(cw);
		if (index >= 0)
			jTabbedPane.setSelectedIndex(index);
	}
	
	/**
	 * Diese Methode überprüft einen String (Gruppenname oder Alias) auf Gültigkeit
	 * 
	 * Diese Methode überprüft den übergebenen String auf unerlaubte Zeichen und returnt false
	 * falls unerlaubte Zeichen enthalten sind.
	 * Wird dieser Name bereits für eine Gruppe (typ = 0) oder einen Alias (typ = 1) verwendet
	 * wird ebenfalls false returnt. Ansonsten wird true returnt.
	 * 
	 * @param name: der String der überprüft werden soll
	 * @param typ: 0 = Gruppenname, 1 = Alias
	 * @return true wenn Name korrekt false wenn Name falsche Zeichen enthält
	 */
	boolean checkName(String name, int typ){
		if(name.matches(Config.getConfig().getNamePattern())){
			if(typ == 0){
				if(contactListWin==null) return true;
				return !contactListWin.groupExists(name);
			} else if(typ == 1){
				return !contactListWin.aliasExists(name);
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	/**
	 * Diese Methode prüft einen übergebenen Namen auf gültige länge und 
	 * kürzt diesen gegebenenfalls. Außerdem besteht die Möglichkeit den Namen
	 * zu in Kleinbuchstaben zu setzen.
	 *  
	 * @param name
	 * @param lowern: true = name wird gelowercased, false = keine Veränderung an Großbuchstaben
	 * @return String: den gekürzten und ggf. gelowercasedten namen
	 */
	String confName(String name, boolean lowern){
		if (name.length() > Config.getConfig().getMaxGroupLength()) {
			name = name.substring(0, Config.getConfig().getMaxGroupLength());
		}
		if(lowern){
			name = name.toLowerCase();
		}
		return name;
	}
	
	/**
	 * Diese Methode setzt den Alias auf den übergebenen String und schreibt diesen
	 * in die Config, falls Alias ungültig wird eine entsprechende Fehlermeldung ausgegeben.
	 * 
	 * @param alias
	 * @return boolean
	 */
	public void changeAlias(String alias){
		alias = confName(alias, false);
		if(checkName(alias, 1)){
			ce.updateAlias(alias);
			Config.getConfig().setAlias(alias);
			Config.write();
		} else {
			info("Illegal charakter in username!<br>Allowed charakters: a-z,A-Z,0-9,ö,ä,ü,Ö,Ä,Ü,ß,é,á,-,_", null, 1);
		}
	}
	
	/**
	 * TODO: Kommentar
	 * 
	 * @return
	 */
	public ChatWindow getActiveCW() {
		return (ChatWindow) jTabbedPane.getSelectedComponent();
	}	
	
	
	/**
	 * Fährt das Programm ordnungsgemäß runter
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
		// Wenn es ein About-Fenster gibt, Fenster ausblenden
		if(hcdAbout != null) {
			hcdAbout.hideIt();
		}
		// Wenn es ein Help-Fenster gibt, Fenster ausblenden
		if(hcdHelp != null) {
			hcdHelp.hideIt();
		}
		// Symbol in der SystemTray schliessen
		trayIcon.removeTray();
		// ChatEngine beenden
		ce.shutdown();
		// Datenbankverbindung schliessen
		if (locDBCon != null) {
			locDBCon.shutdownLocDB();
		}
	}

	/**
	 * Die Methode liefert das zu einer Referenz gehörende {@link ChatWindow}.
	 * 
	 * <br>Sie ersetzt die alte <code>existCW</code> und liefert ein ChatWindow zu
	 * einer Referenz. Die Referenz muss zwansläufig entweder ein {@link String}
	 * oder ein {@link Long}. Ein <code>String</code> bezieht sich auf
	 * GruppenChatFenster und liefert falls vorhanden das ChatFenster zu dieser
	 * Gruppe. Ein <code>Long</code> liefert entsprechend das PrivatChatFenster
	 * wenn der übergebene wert eine gültige UID war und einFenster zu diesem
	 * Nutzer bereits existiert. Sollte eine ungültige Referenz übergeben werden
	 * oder kein Fenster zu dieser Referenz existieren liefert die Methode
	 * <code>null</code> zurück.
	 * 
	 * @param referenz, der Gruppenname (String) oder die UserID (Long) zu der ein
	 *            		Fenster gesucht werden soll.
	 * @return, 		die laufende Instanz des Chatwindows zur angegebenen Referenz
	 *         			oder <code>null</code> falls keine Instanz gefunden.
	 */
	private ChatWindow getCW(Object referenz) {
		if (referenz != null) {
			for (ChatWindow cur : chatList) {
				if (cur.equals(referenz)) {
					return cur;
				}
			}
		}
		return null;
	}
	
	/**
	 * Diese Methode wird in einem privaten ChatWindow zum versenden der
	 * Nachricht verwendet
	 * 
	 * @param empfUID, long EmpfängerUID
	 * @param msg, String die Nachricht
	 */
	void privSend(long empfUID, String msg) {
		ce.send_private(empfUID, msg);
	}

	/**
	 * Diese Methode wird für das Senden von Gruppennachrichten verwendet Falls
	 * noch kein ChatWindow für diese Gruppe besteht wird eines erzeugt.
	 * 
	 * @param empfGrp, String Empfängergruppe
	 * @param msg, String die Nachricht/Msg
	 * @param cw, ChatWindow das aufrufende ChatWindow
	 */
	void groupSend(String empfGrp, String msg) {

		ChatWindow tmpCW = getCW(empfGrp);
		if (tmpCW == null){
			empfGrp = confName(empfGrp, true);
			if (checkName(empfGrp, 0)) {
				tmpCW = new ChatWindow(empfGrp);
				addGrpCW(empfGrp, true);
				focus(tmpCW);
			}
			else {
				info("Illegal charakter in groupname!<br>Allowed charakters: a-z,A-Z,0-9,ö,ä,ü,Ö,Ä,Ü,ß,é,á,-,_", null, 2);
			}
		} else {
			focus(tmpCW);
		}
		ce.send_group(empfGrp, msg);
	}
	
	/**
	 * Diese Methode ist für das Ignorien eines users
	 * 
	 * @param alias, String Alias des Users
	 * @returns, true Wenn User gefunden
	 */
	boolean ignoreUser(long uid) {
		if(ce.ignore_user(uid)) {
			notifyGUI();
			return true;
		}
		return false;
	}

	/**
	 * Diese Methode ist für das nicht weitere Ignorieren eines Benutzers.
	 * 
	 * @param alias, String Alias des Users
	 * @return, true Wenn User gefunden
	 */
	boolean unignoreUser(long uid) {
		if(ce.unignore_user(uid)) {
			notifyGUI();
			return true;
		}
		return false;
	}	

	/**
	 * TODO: Kommentar
	 * 
	 * @param uid
	 */
	public void sendFile(long uid) {
		JFileChooser fileChooser = new JFileChooser();
		int returnVal=fileChooser.showOpenDialog(me);
		File selectedFile = fileChooser.getSelectedFile();
		if((returnVal== JFileChooser.APPROVE_OPTION)&&selectedFile!=null)sendFile(selectedFile, uid);
	}

	/**
	 * TODO: Kommentar
	 * 
	 * @param datei
	 * @param uid
	 */
	public void sendFile(File datei, long uid) {
		if (datei.isFile()) {
			if (datei.canRead()) {
				if (datei.length() > 0) {
					ce.send_file(datei, uid);
				} else {
					info("File has a size of 0 bytes!", uid, 3);
				}
			} else {
				info("Cant read file \"" + datei.getName() + "\"!", uid, 3);
			}
		} else {
			info("Only single Files are supported!", uid, 3);
		}
	}	
	
	/**
	 * @param msg
	 */
	protected void msgToTray(MSG msg){
		if(this.getExtendedState() == JFrame.ICONIFIED){
			trayIcon.recieveMSG(msg);
		}
	}
	
	/**
	 * @param text
	 */
	protected void textToTray(String text, MSGCode code){
		if(this.getExtendedState() == JFrame.ICONIFIED){
			trayIcon.recieveText(text, code);
		}
	}
	
	/**
	 * Displays a text message in the referenced ChatWindow (Group/UID) or the
	 * active Window if reference is <code>null</code>
	 * 
	 * @param nachricht, Text of the message
	 * @param reference, Groupname or UID of ChatWindow to put the message in or
	 *            		 <code>null</code> to take the active one.
	 * @param typ, <ul><li>0 - info<li>1 - warning<li>2 - error</ul>
	 */
	public void info(String nachricht, Object reference, int typ) {
		ChatWindow tmp = getCW(reference);
		if (tmp == null) {
			tmp = getActiveCW();
		}
		if (tmp != null) {
			if (typ == 0) {
				tmp.info(nachricht);
				textToTray(nachricht, MSGCode.CW_INFO_TEXT);
			} else if (typ == 1) {
				tmp.warn(nachricht);
				textToTray(nachricht, MSGCode.CW_WARNING_TEXT);
			} else {
				tmp.error(nachricht);
				textToTray(nachricht, MSGCode.CW_ERROR_TEXT);
			}
		}
	}
	
	/**
	 * Diese Methode liefert ein Fileobjekt, sie benachrichtigt über die GUI den
	 * Nutzer und fordert einen Ablageort an.
	 * 
	 * @param filename
	 * @return File
	 */
	public File request_File(FileTransferData fr) {
		final long timeout = Config.getConfig().getFileTransferTimeout() - 1000;
		String dateiname = fr.datei.getName();
		JOptionPane yesno_pane= new  JOptionPane("Möchten sie die Datei \""+dateiname+ "\" von "+ fr.sender.getAlias() +" annehmen? ("+fr.getNiceSize() +")",JOptionPane.QUESTION_MESSAGE,JOptionPane.YES_NO_OPTION);
		final JDialog yesno_dialog=yesno_pane.createDialog(me, "Dateitransfer");
		final JFileChooser fileChooser = new JFileChooser();
		
		Thread timoutBot = new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(timeout);
				} catch (InterruptedException e) {
				}
				yesno_dialog.dispose();
				fileChooser.cancelSelection();
			}
		});
		if(fr.size>=Config.getConfig().getMaxFileSize())timoutBot.start();
		
		yesno_dialog.show();
		Object  x = yesno_pane.getValue();
		
		
		if(x!=null&&x instanceof Integer &&((Integer)x)==0) {
			if (dateiname != null)
				fileChooser.setSelectedFile(new File(dateiname));
			int returnVal = fileChooser.showSaveDialog(me);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
//				System.out.println("You chose to save this file: " + fileChooser.getSelectedFile().getName());
				return fileChooser.getSelectedFile();
			}
			return null;
		} else {
			return null;
		}
	}

	/**
	 * Diese Methode  informiert die GUI über Änderungen.
	 */
	public void notifyGUI() {
		for (ChatWindow cw : chatList) {
			cw.updateName();
		}
		contactListWin.repaint();
	}

	public void update(Observable o, Object arg) {
		//FIXME : vielleicht nochmal überarbeiten... wenn Zeit ist
		if(o instanceof KnotenKanal){
			MSG tmp = (MSG) arg;
			Node tmp_node = ce.getNodeForNID(tmp.getSender());
			me.addPrivCW(tmp_node.getUserID(), false);
			ce.put(tmp);
		}
	}

	/**
	 * Diese Methode stellt das Node bereit und holt das {@link NODE}-Objekt für eine UserID
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
	 * Diese Methode stellt das GUI für andere Klassen bereit um einen Zugriff
	 * auf GUI Attribute zu ermöglichen
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
	 * TODO: Kommentar
	 * 
	 * @return
	 */
	JTabbedPane getTabbedPane(){
		return this.jTabbedPane;
	}

	/**
	 * TODO: Kommentar
	 */

	public void stateChanged(ChangeEvent e) {
		((ChatWindow)jTabbedPane.getSelectedComponent()).focusEingabefeld();
	}
	
	
	/**
	 * ActionListener für Menu's
	 */
	class menuContoller implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			
			JMenuItem source = (JMenuItem)e.getSource();
			
			switch (source.getText()) {

			case "Exit":
				shutdown();
				break;
			case "About":
				if(hcdAbout == null) {
					hcdAbout = new HTMLContentDialog("About", "helpContentsIcon.png", "about.html");
				} else {
					hcdAbout.showIt();
				}
				break;
			case "Help Contents":
				if(hcdHelp == null) {
					hcdHelp = new HTMLContentDialog("Help", "helpContentsIcon.png", "helpcontent.html");
				} else {
					hcdHelp.showIt();
				}
				break;
			case "Local":
				new HistoryWindow();
				break;
			case "Settings":
				SettingsWindow.showThis();
				break;

			case "Push History":
				// TODO: Daten auf den DB-Server schieben
				break;
			case "Pull History":
				// TODO: Daten vom DB-Server holen
				break;
			}
		}
	}
	
	/**
	 * WindowListener für GUI
	 */
	class winController extends WindowAdapter{
		// Wird das GUI minimiert wird die Userlist zugeklappt und der
		// userListBtn zurückgesetzt:
		public void windowIconified(WindowEvent arg0) {
			if (contactListBtn.isSelected()) {
				contactListZuklappen();
			}
		}
		
		public void windowClosed(WindowEvent arg0) {
			contactListZuklappen();
//			if(HelpContents.getMe()!=null)HelpContents.getMe().dispose();
			SettingsWindow.closeThis();
			shutdown();
		}

		public void windowActivated(WindowEvent arg0) {
			if (contactListBtn.isSelected()) {
				contactListWin.toFront();
			}
		}
	}
	
	/**
	 * Diese Methode gibt die Default Settings des aktuellen L&F in der Console aus
	 */
/*	private void getLookAndFeelDefaultsToConsole(){
		UIDefaults def = UIManager.getLookAndFeelDefaults();
		Vector<?> vec = new Vector<Object>(def.keySet());
		Collections.sort(vec, new Comparator<Object>() {
			public int compare(Object arg0, Object arg1) {
				return arg0.toString().compareTo(arg1.toString());
			}
		});
		for (Object obj : vec) {
//			System.out.println(obj + "\n\t" + def.get(obj));
		}
	}
*/
}
