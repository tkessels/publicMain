
package org.publicmain.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import org.publicmain.sql.DatabaseEngine;
import org.publicmain.sql.LocalDBConnection;
import org.resources.Help;

/**
 * Diese Klasse stellt das Hauptfenster der Anwendung publicMAIN bereit.
 * 
 * Diese Klasse stellt das GUI f�r die Anwendung publicMAIN zur Verf�gung.
 * 
 * @author ATRM
 */

@SuppressWarnings("serial")
public class GUI extends JFrame implements Observer, ChangeListener {

	private final int NAME_LENGTH = Config.getConfig().getMaxGroupLength();

	// Deklarationen:
	private ChatEngine ce;
	LogEngine log;

	private static GUI 	me;
	private List<ChatWindow> chatList;
	private JMenuBar	menuBar;
	private JMenu 		pMAIN;
	private JMenu 		history;
	private JMenu 		help;
	private JMenu 		localDB;
	private JMenu 		backupServer;
	private JMenuItem 	pullHistory;
	private JMenuItem 	pushHistory;
	private JMenuItem 	delBackupHistory;
	private JMenuItem 	settings;
	private JMenuItem 	searchLocalHistory;
	private JMenuItem 	deleteLocalHistory;
	private JMenuItem 	about;
	private JMenuItem 	helpContent;
	private JMenuItem 	exit;
	private JTabbedPane jTabbedPane;
	private JToggleButton contactListBtn;
	private boolean 	contactListActive;
	private ContactList contactListWin;
	private PMTrayIcon 	trayIcon;
	private LocalDBConnection locDBCon;
	private HTMLContentDialog hcdAbout;
	private HTMLContentDialog hcdHelp;
	private boolean 	afkStatus;	

	/**
	 * Konstruktor f�r das GUI mit Initialisierungen
	 */
	private GUI() {
		// Das Look&Feel auf Systemeinstellungen setzen
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		} catch ( Exception ex ) {
			LogEngine.log( ex );
		}

		// Initialisierungen:
		try {
			this.ce = new ChatEngine();
		} catch ( Exception e ) {
			LogEngine.log( e );
		}

		GUI.me 					= this;
		this.log 				= new LogEngine();
		this.about 				= new JMenuItem( "About", Help.getIcon( "aboutSym.png" ) );
		this.helpContent		= new JMenuItem( "Help Contents", Help.getIcon( "helpSym.png", 12, 16 ) );
		this.exit				= new JMenuItem( "Exit", Help.getIcon( "exitSym.png" ) );
		this.chatList 			= Collections.synchronizedList( new ArrayList< ChatWindow >() );
		this.jTabbedPane 		= new JTabbedPane();
		this.contactListBtn 	= new JToggleButton( Help.getIcon( "g18025.png" ) );
		this.contactListActive 	= false;
		this.menuBar 			= new JMenuBar();
		this.pMAIN	 			= new JMenu( "pMAIN" );
		this.history 			= new JMenu( "History" );
		this.help	 			= new JMenu( "Help" );
		this.localDB			= new JMenu( "Local-DB" );
		this.backupServer		= new JMenu( "Backup-Server" );
		this.pushHistory		= new JMenuItem( "Push History", Help.getIcon( "pushDBSym.png" ) );
		this.pullHistory		= new JMenuItem( "Pull History", Help.getIcon( "pullDBSym.png" ) );
		this.delBackupHistory	= new JMenuItem( "Delete History", Help.getIcon( "delBackupHistory.png", 14, 16 ) );
		this.settings 			= new JMenuItem( "Settings", Help.getIcon( "settingsSym.png" ) );
		this.searchLocalHistory	= new JMenuItem( "Search", Help.getIcon( "historySym.png" ) );
		this.deleteLocalHistory	= new JMenuItem( "Delete", Help.getIcon( "delHistorySym.png" ) );
		this.trayIcon 			= new PMTrayIcon();
		this.afkStatus 			= false;

		// Erstellen ben�tigter Listener
		this.addWindowListener( new winController() );
		this.jTabbedPane.addChangeListener( this );
		this.exit.addActionListener( new menuContoller() );			
		this.about.addActionListener( new menuContoller() );
		this.helpContent.addActionListener( new menuContoller() );
		this.searchLocalHistory.addActionListener( new menuContoller() );
		this.deleteLocalHistory.addActionListener( new menuContoller() );
		this.pullHistory.addActionListener( new menuContoller() );
		this.pushHistory.addActionListener( new menuContoller() );
		this.delBackupHistory.addActionListener( new menuContoller() );
		this.settings.addActionListener( new menuContoller() );

		// Button f�r die Kontaktliste konfigurieren und Listener hinzuf�gen
		this.contactListBtn.setMargin( new Insets( 2, 3, 2, 3 ) );
		this.contactListBtn.setToolTipText( "show contacts" );
		this.contactListBtn.addActionListener( new ActionListener() {

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed( ActionEvent e ) {
				JToggleButton source = ( JToggleButton )e.getSource();
				if ( source.isSelected() ) {
					contactListAufklappen();
				} else {
					contactListZuklappen();
				}
			}//eom actionPerformed()
		} );

		//Men�komponenten hinzuf�gen
		this.history.add( localDB );
		this.history.add( backupServer );
		this.pMAIN.add( settings );
		this.pMAIN.add( exit );
		this.help.add( helpContent );
		this.help.add( about );
		this.localDB.add( searchLocalHistory );
		this.localDB.add( deleteLocalHistory );
		this.backupServer.add( pushHistory );
		this.backupServer.add( pullHistory );
		this.backupServer.add( delBackupHistory );
		this.menuBar.setLayout( new BoxLayout( menuBar, BoxLayout.LINE_AXIS ) );
		this.menuBar.add( contactListBtn );
		this.menuBar.add( pMAIN );
		this.menuBar.add( history );
		this.menuBar.add( help );

		// Einkommentieren wenn Logo gew�nscht:
		// this.menuBar.add(Box.createHorizontalGlue());
		// this.menuBar.add(new JLabel(new
		// ImageIcon(Help.class.getResource("miniSpin.gif"))));

		// GUI Komponenten hinzuf�gen:
		this.setJMenuBar( menuBar );
		this.add( jTabbedPane );

		// StandardGruppe erstellen:
		this.addGrpCW( "public", true );
		// StandardGruppe joinen:

		// Registriert Hauptfenster als Empf�nger f�r noch nicht gefangene
		// Privatnachrichten.
		this.ce.register_defaultMSGListener( this );

		// GUI JFrame Einstellungen
		this.setIconImage( Help.getIcon( "pM_Logo.png", 64 ).getImage() );
		this.getContentPane().setBackground( Color.WHITE );
		this.setMinimumSize( new Dimension( 250, 250 ) );
		this.pack();
		this.setLocationRelativeTo( null );
		this.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		this.setTitle( "publicMAIN" );
		this.contactListWin = new ContactList( me );
		this.setVisible( true );
		this.chatList.get(0).focusEingabefeld();
		this.contactListAufklappen();
	}//eom GUI()

	/**
	 * Diese Methode klappt die Kontaktliste auf.
	 * 
	 * Diese Methode klappt die Kontaktliste auf und konfiguriert den
	 * JButton (contactListBtn) entsprechend.
	 */
	private void contactListAufklappen(){

		if( !contactListActive ){
			this.contactListBtn.setToolTipText( "hide contacts" );
			this.contactListBtn.setIcon( Help.getIcon( "g20051.png" ) );
			this.contactListBtn.setSelected( true );
			this.contactListWin.repaint();
			this.contactListWin.setVisible( true );
			contactListActive = true;
		}
	}//eom contactListAufklappen()

	/**
	 * Diese Methode klappt die Kontaktliste zu.
	 * 
	 * Diese Methode klappt die Kontaktliste zu und konfiguriert den
	 * JButton (contactListBtn) entsprechend.
	 */
	private void contactListZuklappen() {

		if ( contactListActive ) {
			this.contactListBtn.setToolTipText( "show contacts" );
			this.contactListBtn.setIcon( Help.getIcon( "g18025.png" ) );
			this.contactListBtn.setSelected( false );
			this.contactListWin.setVisible( false );
			this.contactListActive = false;
		}
	}//eom contactListZuklappen()

	/** 
	 * Diese Methode erstellt ein neues ChatFenster.
	 * 
	 * Diese Methode erstellt ein neues ChatFenster. Ist der Parameter ein {@link String} erstellt
	 * sie ein Gruppenfenster ist er {@link Long} ein PrivatChatFenster.
	 * 
	 * @param referenz, f�r was soll ein Fenster erstellt werden. 
	 * @return ChatWindow das erstellte ChatWindow.
	 */
	private ChatWindow createChat( Object referenz ) {

		ChatWindow cw;
		if ( referenz instanceof String ) {
			cw = new ChatWindow( ( String )referenz );
		} else if ( referenz instanceof Long) {
			cw = new ChatWindow( ( Long )referenz );
		} else
			return null;

		// Titel festlegen
		String title = cw.getChatWindowName();

		// Neues ChatWindow (cw) zur Chatliste (ArrayList<ChatWindow>)
		// hinzuf�gen
		this.chatList.add( cw );
		// erzeugen von neuem Tab f�r neues ChatWindow
		this.jTabbedPane.addTab( title, cw );
		// Index vom ChatWindow im JTabbedPane holen um am richtigen Ort
		// einzuf�gen
		int index = jTabbedPane.indexOfComponent( cw );
		// den neuen Tab an die Stelle von index setzen
		this.jTabbedPane.setTabComponentAt( index, cw.getWindowTab() );
		// je nach Typ des ChatWindows den entsprechenden MSGListener hinzuf�gen
		if ( cw.isGroup() ) {
			ce.add_MSGListener( cw, ( String )referenz );
		} else {
			ce.add_MSGListener( cw, ( Long )referenz );
		}

		return cw;
	}//eom createChat()

	/**
	 * Diese Methode erstellt ein Gruppen ChatWindow.
	 * 
	 * Diese Methode erstellt ein ChatWindow f�r Gruppen, falls ChatWindow
	 * bereits vorhanden, wird dieses fokusiert.
	 * 
	 * @param grpName String Name der Gruppe.
	 * @param focus boolean true wenn Focus auf das neue ChatWindow gesetzt werden soll.
	 */
	void addGrpCW( String grpName, boolean focus ) {
		// Hol ref. auf Gruppenfenster wenn existent
		ChatWindow tmp_cw = getCW( grpName );
		// wenn ref. leer dann erstelle neues Gruppenfesnter
		if ( tmp_cw == null ) {
			tmp_cw = createChat( grpName );
		}
		// fokusiere das Gruppenfenster
		if ( focus ) {
			focus( tmp_cw );
		}
	}//eom addGrpCW()

	/**
	 * Diese Methode erstellt ein privates ChatWindow.
	 * 
	 * Diese Methode erstellt ein ChatWindow f�r Gruppen, falls ChatWindow
	 * bereits vorhanden, wird dieses fokusiert.
	 * 
	 * @param uid long UserID.
	 * @param focus boolean true wenn Focus auf das neue ChatWindow gesetzt werden soll.
	 */
	void addPrivCW( long uid, boolean focus ){
		ChatWindow tmp = getCW( uid );
		if( tmp == null ){
			tmp = createChat( uid );
		}
		if ( focus ){
			focus( tmp );
		}
	}//eom addPrivCW()

	/**
	 * Diese Methode entfernt ein ChatWindow.
	 * 
	 * Diese Methode sorgt daf�r das ChatWindows aus der ArrayList "chatList"
	 * entfernt werden und im GUI nicht mehr angezeigt werden. Au�erdem
	 * entfernt sie den MSGListener.
	 * 
	 * @param cw ChatWindow das zu l�schende ChatWindow
	 */
	void delChat( ChatWindow cw ) {
		// Falls nur noch ein ChatWindow �brig kann dieses nicht entfernt werden
		if( chatList.size() >= 2 ){
			// ChatWindow (cw) aus jTabbedPane entfernen:
			this.jTabbedPane.remove( cw );
			// ChatWindow aus Chatliste entfernen:
			this.chatList.remove( cw );
			// ChatWindow aus Gruppe entfernen (MSGListener abschalten):
			ce.remove_MSGListener( cw );
		}
	}//eom delChat()

	/**
	 * Diese Methode entfernt ein ChatWindow.
	 * 
	 * Diese Methode entfernt ein ChatWindow anhand eines �bergebenen
	 * Referenzobjektes.
	 * 
	 * @param refObject Object zu entfernendes ChatWindow.
	 */
	void delChat( Object refObject ) {
		delChat( getCW( refObject ) );
	}//eom delChat()

	/**
	 * Diese Methode Fokussiert das angegebene ChatWindow.
	 * 
	 * Diese ethode sorgt daf�r das das �bergebene ChatWindow fokusiert wird.
	 * 
	 * @param cw, das zu fokussierende Chatwindow
	 */
	private void focus( ChatWindow cw ) {
		int index = jTabbedPane.indexOfComponent( cw );
		if ( index >= 0 ) {
			jTabbedPane.setSelectedIndex( index );
		}
		cw.focusEingabefeld();
	}//eom focus()

	/**
	 * Diese Methode �berpr�ft einen String (Gruppenname oder Alias) auf G�ltigkeit
	 * 
	 * Diese Methode �berpr�ft den �bergebenen String auf unerlaubte Zeichen und returnt false
	 * falls unerlaubte Zeichen enthalten sind.
	 * Wird dieser Name bereits f�r eine Gruppe (typ = 0) oder einen Alias (typ = 1) verwendet
	 * wird ebenfalls false returnt. Ansonsten wird true returnt.
	 * 
	 * @param name: der String der �berpr�ft werden soll
	 * @param typ: 0 = Gruppenname, 1 = Alias
	 * @return true wenn Name korrekt false wenn Name falsche Zeichen enth�lt
	 */
	boolean checkName( String name, int typ ){
		if ( name.matches( Config.getConfig().getNamePattern() ) ){
			if ( typ == 0 ){
				if ( contactListWin == null ){
					return true;
				}
				return !contactListWin.groupExists( name );
			} else if ( typ == 1 ){
				return !contactListWin.aliasExists( name );
			} else {
				return false;
			}
		} else
			return false;
	}//eom checkName()

	/**
	 * Diese Methode konfiguriert einen String
	 * 
	 * Diese Methode pr�ft einen �bergebenen Namen auf g�ltige l�nge und 
	 * k�rzt diesen gegebenenfalls. Au�erdem besteht die M�glichkeit den Namen
	 * in Kleinbuchstaben zu setzen.
	 *  
	 * @param name: String der zu konfigurierende Name
	 * @param lowern: true = name wird gelowercased, false = keine Ver�nderung an Gro�buchstaben
	 * @return String: den gek�rzten und ggf. gelowercasedten namen
	 */
	String confName( String name, boolean lowern ){
		if ( name.length() > NAME_LENGTH ) {
			name = name.substring( 0, NAME_LENGTH );
		}
		if ( lowern ){
			name = name.toLowerCase();
		}
		return name;
	}//eom confName()

	/**
	 * Diese Methode setzt einen Alias neu.
	 * 
	 * Diese Methode setzt den Alias auf den �bergebenen String und schreibt diesen
	 * in die Config, falls Alias ung�ltig wird eine entsprechende Fehlermeldung ausgegeben.
	 * 
	 * @param alias String neuer Aliasname
	 */
	void changeAlias( String alias ){
		alias = confName( alias, false );
		if ( checkName( alias, 1 ) ) {
			ce.updateAlias( alias );
			Config.getConfig().setAlias( alias );
			Config.write();
		} else {
			info( "Illegal charakter in username!<br>Allowed charakters: a-z,A-Z,0-9,�,�,�,�,�,�,�,�,�,-,_", null, 1 );
		}
	}//eom changeAlias()

	/**
	 * Diese Methode gibt das aktive ChatWindow zur�ck.
	 * 
	 * Diese Methode holt das selektierte ChatWindow des JTabbedPane und
	 * liefert es zur�ck.
	 * 
	 * @return ChatWindow: das aktive ChatWindow
	 */
	ChatWindow getActiveCW() {
		return ( ChatWindow )jTabbedPane.getSelectedComponent();
	}//eom getActiveCW()

	/**
	 * Diese Methode setzt den AFK Status (afkStatus).
	 * 
	 * Diese Methode setzt afkStatus auf false wenn true bzw.
	 * auf true wenn false
	 */
	public void afk() {
		if ( afkStatus ) {
			afkStatus = false;
			info( "You are <b>online</b>!", null, 2 );
		} else {
			afkStatus = true;
			info( "You are <b>A</b>way <b>F</b>rom <b>K</b>eyboard!", null, 2 );
		}
		this.notifyGUI();
	}//eom afk()

	/**
	 * Diese Methode gibt den AFK Status zur�ck.
	 * 
	 * Diese Methode gibt true zur�ck falls der User afk ist ansonsten false.
	 * 
	 * @return boolean afkStatus true falls afk, ansonsten false.
	 */
	public boolean isAFK() {
		return afkStatus;
	}//eom isAFK()


	/**
	 * Diese Methode beendet die Anwendung publicMAIN.
	 * 
	 * Diese Methode f�hrt das Programm ordnungsgem�� herrunter.
	 */
	void shutdown() {
		new Thread( new Runnable() {
			public void run() {
				try {
					Thread.sleep( 2000 );
				} catch ( InterruptedException e ) {
				}
				System.exit( 0 );
			}
		} ).start();
		LogEngine.log( this, "Shutdown initiated!", LogEngine.INFO );
		// Wenn es ein About-Fenster gibt, Fenster ausblenden
		if ( hcdAbout != null ) {
			hcdAbout.hideIt();
		}
		// Wenn es ein Help-Fenster gibt, Fenster ausblenden
		if ( hcdHelp != null ) {
			hcdHelp.hideIt();
		}
		//		Help.playSound("logoff.wav");
		// Kontaktliste schlie�en
		contactListZuklappen();
		// SettingsWindow schlie�en
		SettingsWindow.closeThis();
		// HistoryWindow schlie�en
		HistoryWindow.closeThis();
		// Symbol in der SystemTray schliessen
		trayIcon.removeTray();
		// ChatEngine beenden
		ce.shutdown();
		// Datenbankverbindung schliessen
		if ( locDBCon != null ) {
			locDBCon.shutdownLocDB();
		}
	}//eom shutdown()

	/**
	 * Die Methode liefert das zu einer Referenz geh�rende {@link ChatWindow}.
	 * 
	 * <br>Sie ersetzt die alte <code>existCW</code> und liefert ein ChatWindow zu
	 * einer Referenz. Die Referenz muss zwansl�ufig entweder ein {@link String}
	 * oder ein {@link Long}. Ein <code>String</code> bezieht sich auf
	 * GruppenChatFenster und liefert falls vorhanden das ChatFenster zu dieser
	 * Gruppe. Ein <code>Long</code> liefert entsprechend das PrivatChatFenster
	 * wenn der �bergebene wert eine g�ltige UID war und einFenster zu diesem
	 * Nutzer bereits existiert. Sollte eine ung�ltige Referenz �bergeben werden
	 * oder kein Fenster zu dieser Referenz existieren liefert die Methode
	 * <code>null</code> zur�ck.
	 * 
	 * @param referenz, der Gruppenname (String) oder die UserID (Long) zu der ein
	 *            		Fenster gesucht werden soll.
	 * @return, 		die laufende Instanz des Chatwindows zur angegebenen Referenz
	 *         			oder <code>null</code> falls keine Instanz gefunden.
	 */
	private ChatWindow getCW( Object referenz ) {
		if ( referenz != null ) {
			for ( ChatWindow cur : chatList ) {
				if ( cur.equals( referenz ) ) {
					return cur;
				}
			}
		}
		return null;
	}// eom getCW()

	/**
	 * Diese Methode versendet private Nachrichten.
	 * 
	 * Diese Methode wird in einem privaten ChatWindow zum versenden der
	 * Nachricht verwendet. 
	 * 
	 * @param empfUID, long Empf�ngerUID
	 * @param msg, String die Nachricht
	 */
	void privSend( long empfUID, String msg ) {
		if ( empfUID != ce.getUserID() ) {
			ce.send_private( empfUID, msg );
		} else {
			info( "Message to yourself, is not allowed", null, 2 );
		}
	}//eom privSend()

	/**
	 * Diese Methode versendet Gruppen Nachrichten.
	 * 
	 * Diese Methode wird f�r das Senden von Gruppennachrichten verwendet Falls
	 * noch kein ChatWindow f�r diese Gruppe besteht wird eines erzeugt.
	 * 
	 * @param empfGrp, String Empf�ngergruppe
	 * @param msg, String die Nachricht/Msg
	 * @param cw, ChatWindow das aufrufende ChatWindow
	 */
	void groupSend( String empfGrp, String msg ) {

		ChatWindow tmpCW = getCW( empfGrp );
		if ( tmpCW == null ){
			empfGrp = confName( empfGrp, true );
			if ( checkName( empfGrp, 0 ) ) {
				tmpCW = new ChatWindow( empfGrp );
				addGrpCW( empfGrp, false );
			}
			else {
				info( "Illegal charakter in groupname!<br>Allowed charakters: a-z,A-Z,0-9,�,�,�,�,�,�,�,�,�,-,_", null, 2 );
			}
		} else {
			//	focus(tmpCW);
		}
		ce.send_group( empfGrp, msg );
	}//eom groupSend()

	/**
	 * Diese Methode sorgt daf�r das ein User ignoriert wird.
	 * 
	 * Diese Methode sorgt daf�r das der User mit der �bergebenen UserID
	 * ignoriert wird, d.h. es werden keine weiteren Nachrichten von diesem
	 * User angezeigt.
	 * 
	 * @param alias, String Alias des Users
	 * @returns, true Wenn User gefunden
	 */
	boolean ignoreUser( long uid ) {
		if ( ce.ignore_user( uid  ) ) {
			notifyGUI();
			info( ce.getNodeForUID( uid ) + " is <b>ignored!</b>", null, 2 );
			return true;
		}
		return false;
	}//eom ignoreUser()

	/**
	 * Diese Methode sorgt daf�r das ein User nicht mehr ignoriert wird.
	 * 
	 * Diese Methode sorgt daf�r das ein User mit der �bergebenene UserID
	 * nicht weiter ignoriert wird und alle Nachrichten von diesem User wieder
	 * angezeigt werden.
	 * 
	 * @param alias, String Alias des Users
	 * @return, true Wenn User gefunden
	 */
	boolean unignoreUser( long uid ) {
		if ( ce.unignore_user( uid ) ) {
			notifyGUI();
			info( ce.getNodeForUID( uid ) + " is <b>unignored!</b>", null, 2 );
			return true;
		}
		return false;
	}//eom unignoreUser()

	/**
	 * Diese Methode wird verwendet um Datei auszuw�hlen die verschickt werden soll.
	 * 
	 * Diese Methode �ffnet einen FileChooser in dem man die zu versendende Datei ausw�hlen
	 * kann. Diese Datei wird dann versendet.
	 * 
	 * @param uid
	 */
	void sendFile( long uid ) {
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog( me );
		File selectedFile = fileChooser.getSelectedFile();
		if ( ( returnVal == JFileChooser.APPROVE_OPTION) && ( selectedFile != null ) ) {
			sendFile( selectedFile, uid );
		}
	}//eom sendFile()

	/**
	 * Diese Methode versendet eine Datei.
	 * 
	 * Diese Methode pr�ft die Gr��e, die Rechte und die Auswahl der �bergebenen Datei.
	 * Ist die Pr�fung erfolgreich, d.h. die Datei ist gr��er als 0 Bytes, es bestehen
	 * Leserechte und es wurde nur eine Datei ausgew�hlt, dann wird die Datei an den 
	 * �bergebenen User (UserID) versendet.
	 * 
	 * @param datei File die versendet werden soll.
	 * @param uid long UserID des Empf�ngers der Datei.
	 */
	void sendFile( File datei, long uid ) {
		if ( datei.isFile() ) {
			if ( datei.canRead() ) {
				if ( datei.length() > 0 ) {
					ce.send_file( datei, uid );
				} else {
					info( "File has a size of 0 bytes!", uid, 3 );
				}
			} else {
				info( "Cant read file \"" + datei.getName() + "\"!", uid, 3 );
			}
		} else {
			info( "Only single Files are supported!", uid, 3 );
		}
	}//eom sendFile()

	/**
	 * Diese Methode erm�glicht es Nachrichten im TrayIcon anzuzeigen.
	 * 
	 * Diese Methode bietet die M�glichkeit Nachrichten �ber das TrayIcon in Form
	 * von Popups anzuzeigen falls das GUI minimiert ist.
	 * 
	 * @param msg MSG die angezeigt werden soll.
	 */
	void msgToTray( MSG msg ){
		if( this.getExtendedState() == JFrame.ICONIFIED ){
			trayIcon.recieveMSG( msg );
		}
	}//eom msgToTray()

	/**
	 * Diese Methode erm�glicht es Text im TrayIcon anzuzeigen.
	 * 
	 * Diese Methode bietet die M�glichkeit Text �ber das TrayIcon in Form
	 * von Popups anzuzeigen falls das GUI minimiert ist.
	 * 
	 * @param text
	 */
	void textToTray(String text, MSGCode code){
		if( this.getExtendedState() == JFrame.ICONIFIED ){
			trayIcon.recieveText( text, code );
		}
	}//eom textToTray()

	/**
	 * Diese Methode zeigt eine Nachricht im ChatWindow an.
	 * 
	 * Diese Methode zeigt eine Nachricht im �bergebenen referenz ChatWindow an
	 * (Group/UID) oder sie wird im aktuellen ChatWindow angezeigt falls die
	 * Referenz null ist.
	 * 
	 * @param nachricht, Text of the message
	 * @param reference, Groupname or UID of ChatWindow to put the message in or
	 *            		 <code>null</code> to take the active one.
	 * @param typ, <ul><li>0 - info<li>1 - warning<li>2 - error</ul>
	 */
	public void info( String nachricht, Object reference, int typ ) {
		ChatWindow tmp = getCW( reference );
		if ( tmp == null ) {
			tmp = getActiveCW();
		}
		if ( tmp != null ) {
			if ( typ == 0 ) {
				tmp.info( nachricht );
				textToTray( nachricht, MSGCode.CW_INFO_TEXT );
			} else if ( typ == 1 ) {
				tmp.warn( nachricht );
				textToTray( nachricht, MSGCode.CW_WARNING_TEXT );
			} else {
				tmp.error( nachricht );
				textToTray( nachricht, MSGCode.CW_ERROR_TEXT );
			}
		}
	}//eom info()

	/**
	 * Diese Methode wird zum speichern empfangener Dateien verwendet.
	 *
	 * Diese Methode liefert ein Fileobjekt, sie benachrichtigt �ber die GUI den
	 * Nutzer und fordert einen Ablageort an.
	 * 
	 * @param fr FileTransferData
	 * @return File request_File
	 */
	@SuppressWarnings("deprecation")
	public File request_File( FileTransferData fr ) {
		final long timeout = Config.getConfig().getFileTransferTimeout() - 1000;
		String dateiname = fr.datei.getName();
		JOptionPane yesno_pane = new JOptionPane( "M�chten sie die Datei \"" + dateiname + "\" von " + fr.sender.getAlias() + " annehmen? (" + fr.getNiceSize() + ")", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION );
		final JDialog yesno_dialog = yesno_pane.createDialog( me, "Dateitransfer" );
		final JFileChooser fileChooser = new JFileChooser();

		Thread timoutBot = new Thread( new Runnable() {
			public void run() {
				try {
					Thread.sleep( timeout );
				} catch ( InterruptedException e ) {
				}
				yesno_dialog.dispose();
				fileChooser.cancelSelection();
			}
		} );
		if( fr.size >= Config.getConfig().getMaxFileSize() ) {
			timoutBot.start();
		}

		yesno_dialog.show();
		Object  x = yesno_pane.getValue();


		if( ( x != null ) && ( x instanceof Integer ) && ( ( ( Integer )x ) == 0 ) ) {
			if ( dateiname != null ) {
				fileChooser.setSelectedFile( new File( dateiname ) );
			}
			int returnVal = fileChooser.showSaveDialog( me );
			if ( returnVal == JFileChooser.APPROVE_OPTION )
				//				System.out.println("You chose to save this file: " + fileChooser.getSelectedFile().getName());
				return fileChooser.getSelectedFile();
			return null;
		} else
			return null;
	}//eom request_File()

	/**
	 * Diese Methode informiert das GUI �ber �nderungen.
	 * 
	 * Diese Methode sorgt daf�r das bei �nderungen von Aliasnamen etc.
	 * das GUI informiert und aktuallisiert wird.
	 */
	public void notifyGUI() {
		for ( ChatWindow cw : chatList ) {
			cw.updateName();
		}
		contactListWin.repaint();
	}//eom notifyGUI()

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update( Observable o, Object arg ) {
		//FIXME : vielleicht nochmal �berarbeiten... wenn Zeit ist
		if ( o instanceof KnotenKanal ){
			MSG tmp = ( MSG ) arg;
			Node tmp_node =null;
			if ( tmp.getSender() != ce.getMyNodeID() ) {
				tmp_node = ce.getNodeForNID( tmp.getSender() );
			} else if ( tmp.getEmpf�nger() != ce.getMyNodeID() ) {
				tmp_node = ce.getNodeForNID( tmp.getEmpf�nger() );
			}
			if ( tmp_node != null ){
				me.addPrivCW( tmp_node.getUserID(), false );
				ce.put( tmp );
			}
		}
	}//eom update()

	/**
	 * Diese Methode stellt das Node bereit und holt das {@link NODE}-Objekt f�r eine UserID.
	 * 
	 * @param uid long
	 * @return Node
	 */
	Node getNodeForUID( long uid ) {
		return ce.getNodeForUID( uid );
	}//eom getNodeForUID()

	/**
	 * Diese Methode stellt das GUI bereit.
	 * 
	 * Diese Methode stellt das GUI f�r andere Klassen bereit um einen Zugriff
	 * auf GUI Attribute zu erm�glichen.
	 * 
	 * @return GUI
	 */
	public static GUI getGUI() {
		if ( me == null ) {
			me = new GUI();
		}
		return me;
	}//eom getGUI()

	/**
	 * Diese Methode stellt das jTabbedPane f�r andere Klassen bereit.
	 * 
	 * @return JTabbedPane jTabbedPane
	 */
	JTabbedPane getTabbedPane(){
		return this.jTabbedPane;
	}//eom getTabbedPane()

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		( ( ChatWindow )jTabbedPane.getSelectedComponent() ).focusEingabefeld();
	}//eom stateChanged()


	/**
	 * ActionListener f�r Menu's
	 */
	class menuContoller implements ActionListener{

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed( ActionEvent e ) {

			JMenuItem source = ( JMenuItem )e.getSource();

			switch ( source.getText() ) {

			case "Exit":
				shutdown();
				break;
			case "About":
				if ( hcdAbout == null ) {
					hcdAbout = new HTMLContentDialog( "About", "pM_Logo.png", "about.html" );
				} else {
					hcdAbout.showIt();
				}
				break;
			case "Help Contents":
				if ( hcdHelp == null ) {
					hcdHelp = new HTMLContentDialog( "Help", "helpSym.png", "helpcontent.html" );
				} else {
					hcdHelp.showIt();
				}
				break;
			case "Search":
				if ( DatabaseEngine.getDatabaseEngine().getStatusLocal() ) {
					new HistoryWindow();
				} else {
					new SettingsWindow( 1, true );
				}
				break;
			case "Delete":
				if ( JOptionPane.showConfirmDialog( me, "Do you really want to delete the local history?", "Delete local history", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE ) == 0 ) {
					DatabaseEngine.getDatabaseEngine().deleteLocalHistory();
				}
				break;
			case "Settings":
				SettingsWindow.get().setVisible( true );
				break;
			case "Push History":
				if ( DatabaseEngine.getDatabaseEngine().getStatusLocal() ) {
					int statusBackup = DatabaseEngine.getDatabaseEngine().getStatusBackup();
					if ( statusBackup >= 1 ) {
						if ( statusBackup >= 2 ){
							DatabaseEngine.getDatabaseEngine().push();
						} else {
							new SettingsWindow( 2, true );
						}
					} else {
						new SettingsWindow( 1, true );
					}
				} else {
					new SettingsWindow( 1, true );
				}
				break;
			case "Pull History":
				if ( DatabaseEngine.getDatabaseEngine().getStatusLocal() ) {
					int statusBackup = DatabaseEngine.getDatabaseEngine().getStatusBackup();
					if ( statusBackup >= 1 ) {
						if ( statusBackup >= 2 ){
							DatabaseEngine.getDatabaseEngine().pull();
						} else {
							new SettingsWindow( 2, true );
						}
					} else {
						new SettingsWindow( 1, true );
					}
				} else {
					new SettingsWindow( 1, true );
				}
				break;
			case "Delete History":
				if( JOptionPane.showConfirmDialog( me, "Do you really want to delete the backup history?", "Delete backup history", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE ) == 0 ) {
					DatabaseEngine.getDatabaseEngine().deleteBackupMessages();
				}
				break;
			}
		}
	}//eoc menuContoller

	/**
	 * WindowListener f�r GUI
	 */
	class winController extends WindowAdapter{
		// Wird das GUI minimiert wird die Userlist zugeklappt und der
		// userListBtn zur�ckgesetzt:
		/* (non-Javadoc)
		 * @see java.awt.event.WindowAdapter#windowIconified(java.awt.event.WindowEvent)
		 */
		public void windowIconified( WindowEvent arg0 ) {
			if ( contactListBtn.isSelected() ) {
				contactListZuklappen();
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
		 */
		public void windowClosed( WindowEvent arg0 ) {
			shutdown();
		}

		/* (non-Javadoc)
		 * @see java.awt.event.WindowAdapter#windowActivated(java.awt.event.WindowEvent)
		 */
		public void windowActivated( WindowEvent arg0 ) {
			if ( contactListBtn.isSelected() ) {
				contactListWin.toFront();
			}
			me.toFront();
		}
	}//eoc winController


	/**
	 * Diese Methode gibt die Default Settings des aktuellen L&F in der Console aus und
	 * kann verwendet werden um evtl. �nderungen am Layout vorzunehmen.
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
}//eoc GUI
