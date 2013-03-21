package org.publicmain.gui;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
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

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.chatengine.GruppenKanal;
import org.publicmain.chatengine.KnotenKanal;
import org.publicmain.common.LogEngine;
import org.publicmain.common.Node;

import com.nilo.plaf.nimrod.NimRODLookAndFeel;


/**
 * @author ATRM
 * 
 */

public class GUI extends JFrame implements Observer {

	// Deklarationen:
	ChatEngine ce;
	LogEngine log;

	private static GUI me;
	private List<Node> nodes;
	private List<ChatWindow> chatList;
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenu configMenu;
	private JMenu helpMenu;
	private JMenuItem aboutPMAIN;
	private JMenuItem helpContents;
	private JMenuItem menuItemRequestFile;
	private JMenu lafMenu;
	private ButtonGroup btnGrp;
	private JMenuItem lafNimROD;
	private JTabbedPane jTabbedPane;
	private JToggleButton userListBtn;
	private boolean userListActive;
	private UserList userListWin;
	private pMTrayIcon trayIcon;
	

	/**
	 * Konstruktor für GUI
	 */
	private GUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			log.log(ex);
		}

		// Initialisierungen:
		try {
			this.ce = ChatEngine.getCE();
		} catch (Exception e) {
			log.log(e);
		}
		this.me = this;
		this.log = new LogEngine();
		this.menuBar = new JMenuBar();
		this.fileMenu = new JMenu("File");
		this.configMenu = new JMenu("Settings");
		this.helpMenu = new JMenu("Help");
		this.aboutPMAIN = new JMenuItem("About pMAIN");
		this.helpContents = new JMenuItem("Help Contents", new ImageIcon(getClass().getResource("media/HelpContentsIcon.png")));	// evtl. noch anderes Icon wählen
		this.menuItemRequestFile = new JMenuItem("Test(request_File)");
		this.lafMenu = new JMenu("Switch Design");
		this.btnGrp = new ButtonGroup();
		this.chatList = new ArrayList<ChatWindow>();
		this.jTabbedPane = new JTabbedPane();
		this.userListBtn = new JToggleButton(new ImageIcon(getClass().getResource("media/UserListAusklappen.png")));
		this.userListActive = false;
		this.lafNimROD = new JRadioButtonMenuItem("NimROD");
		this.trayIcon = new pMTrayIcon();
		
		// Anlegen der Menüeinträge für Designwechsel (installierte
		// LookAndFeels)
		// + hinzufügen zum lafMenu ("Designwechsel")
		// + hinzufügen der ActionListener (lafController)
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

		// Anlegen benötigter Controller und Listener:
		// WindowListener für das GUI-Fenster:
		this.addWindowListener(new winController());

		// Focus Listener auf ChatWindow
		this.addWindowFocusListener(new WindowAdapter() {
			public void windowGainedFocus(WindowEvent e) {
				// Focus auf erste ChatWindow in chatList setzen:
				chatList.get(0).requestFocusInWindow();
			}
		});

		// ActionListener für Menu's:
		this.menuItemRequestFile.addActionListener(new menuContoller());
		this.aboutPMAIN.addActionListener(new menuContoller());
		this.helpContents.addActionListener(new menuContoller());
		this.lafNimROD.addActionListener(new lafController(lafNimROD, null));
		
		// Konfiguration userListBtn:
		this.userListBtn.setMargin(new Insets(2, 3, 2, 3));
		this.userListBtn.setToolTipText("Userlist einblenden");
		this.userListBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JToggleButton source = (JToggleButton) e.getSource();
				if (source.isSelected()) {
					userListAufklappen();
				} else {
					userListZuklappen();
				}
			}
		});
		
		// Menüs hinzufügen:
		this.btnGrp.add(lafNimROD);
		this.lafMenu.add(lafNimROD);
		this.configMenu.add(lafMenu);
		this.fileMenu.add(menuItemRequestFile);
		this.helpMenu.add(aboutPMAIN);
		this.helpMenu.add(helpContents);
		this.menuBar.add(userListBtn);
		this.menuBar.add(fileMenu);
		this.menuBar.add(configMenu);
		this.menuBar.add(helpMenu);

		// GUI Komponenten hinzufügen:
		this.setJMenuBar(menuBar);
		this.add(jTabbedPane);
		this.addChat(new ChatWindow("public"));
		this.addChat(new ChatWindow("grupp1"));
		this.addChat(new ChatWindow(123 ,"private1"));

		// GUI JFrame Einstellungen:
		this.setIconImage(new ImageIcon(getClass().getResource("media/pM_Logo2.png")).getImage());
		this.pack();
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("publicMAIN");
		this.setVisible(true);
	}

	/**
	 * Diese Methode klappt die Userliste auf
	 * 
	 * 
	 */
	private void userListAufklappen(){
		if(!userListActive){
			
			this.userListBtn.setToolTipText("Userlist ausblenden");
			this.userListBtn.setIcon(new ImageIcon(getClass().getResource("media/UserListEinklappen.png")));
			this.userListBtn.setSelected(true);
			
			this.userListWin = new UserList(GUI.me);
			this.userListWin.repaint();
			this.userListWin.setVisible(true);
			
			this.userListActive = true;
		}
	}
	
	/**
	 * Diese Methode klappt die Userliste zu
	 * 
	 * 
	 */
	private void userListZuklappen(){
		if(userListActive){
			
			this.userListBtn.setToolTipText("Userlist einblenden");
			this.userListBtn.setIcon(new ImageIcon(getClass().getResource("media/UserListAusklappen.png")));
			this.userListBtn.setSelected(false);
			
			this.userListWin.setVisible(false);
			
			this.userListActive = false;
		}
	}
	
	/**
	 * Diese Methode fügt ein ChatWindow hinzu
	 * 
	 * Diese Methode fügt ein ChatWindow zu GUI hinzu und setzt dessen
	 * Komponenten
	 * 
	 * @param cw
	 */
	public void addChat(final ChatWindow cw) {
		// TODO: evtl. noch Typunterscheidung hinzufügen (Methode
		// getCwTyp():String)
		String title = cw.getChatWindowName();

		// neues ChatWindow (cw) zur Chatliste (ArrayList<ChatWindow>)
		// hinzufügen:
		this.chatList.add(cw);
		// erzeugen von neuem Tab für neues ChatWindow:
		this.jTabbedPane.addTab(title, cw);

		// ChatWindow am NachrichtenListener (MSGListener) anmelden:
		// ce.group_join(title);
		ce.add_MSGListener(cw, title);

		// Index vom ChatWindow im JTabbedPane holen um am richtigen Ort
		// einzufügen:
		int index = jTabbedPane.indexOfComponent(cw);

		// den neuen Tab an die Stelle von index setzen:
		this.jTabbedPane.setTabComponentAt(index, cw.getWindowTab());
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
		// ChatWindow (cw) aus jTabbedPane entfernen:
		this.jTabbedPane.remove(cw);
		// ChatWindow aus Chatliste entfernen:
		this.chatList.remove(cw);
		// ChatWindow aus Gruppe entfernen (MSGListener abschalten):
		ce.group_leave(cw.getChatWindowName());
		// Falls keine ChatWindows mehr wird public geöffnet:
		if (chatList.isEmpty()) {
			// TODO: Hier evtl. noch anderen Programmablauf implementier
			// z.B. schließen des Programms wenn letztes ChatWindow geschlossen
			// wird
			addChat(new ChatWindow("public"));
		}
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
	 * Diese Methode liefert ein Fileobjekt
	 * 
	 * Diese Methode bittet die GUI(den Nutzer) um ein Fileobjekt zur Ablage der
	 * empfangenen Datei
	 * 
	 * @return File
	 */
	public File request_File() {
		// TODO: hier stimmt noch nix! später überarbeiten!
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showSaveDialog(me);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("You chose to save this file: " + fileChooser.getSelectedFile().getName());
		}
		return fileChooser.getSelectedFile();
	}

	/**
	 * Diese Methode soll über Änderungen informieren
	 */
	public void notifyGUI() {
		// TODO:
		// da muss noch was gemacht werden !!!
		// evtl fliegt die Methode auch raus wenn wir das
		// mit den Observerpattern machen...
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof GruppenKanal){
			if (o.countObservers()==1){
			//erzeuge gruppenfenster füge nachricht ein sei happy
			}
			else{
				//
			}
			
		}
		if(o instanceof KnotenKanal&&o.countObservers()==1){
			//erzeuge gruppen
		}
		// TODO Auto-generated method stub
	}

	/**
	 * Diese Methode stellt das Node bereit
	 * 
	 * Diese Methode ist ein Getter für das Node
	 * 
	 * @param sender
	 * @return Node
	 */
	public Node getNode(long sender) {
		for (Node x : nodes)
			if (x.getNodeID() == sender)
				return x;
		return null;
	}
	
	JTabbedPane getTabbedPane(){
		return this.jTabbedPane;
	}

	/**
	 * ActionListener für Design wechsel (LookAndFeel)
	 * 
	 * hier wird das Umschalten des LookAndFeels im laufenden Betrieb ermöglicht
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
			
			userListWasActive = userListActive;
			JMenuItem source = (JMenuItem)e.getSource();
			
			userListZuklappen();
			
			if(source.getText().equals("NimROD")){
				try{
					UIManager.setLookAndFeel(new NimRODLookAndFeel());
				} catch (Exception ex){
					System.out.println(ex.getMessage());
				}
			} else {
				try {
					UIManager.setLookAndFeel(laf.getClassName());
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
			}
			SwingUtilities.updateComponentTreeUI(GUI.me);
			GUI.me.pack();
			if(userListWasActive)userListAufklappen();
			
		}
	}
	
	/**
	 * ActionListener für Menu's
	 * 
	 * @author ABerthold
	 *
	 */
	class menuContoller implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			
			JMenuItem source = (JMenuItem)e.getSource();
			
			switch(source.getText()){
			
			case "Test(request_File)":
				request_File();
				break;
			case "About pMAIN":
				new AboutPublicMAIN(me, "About publicMAIN", true);
				break;
			case "Help Contents":
				//TODO: HelpContents HTML schreiben
				new HelpContents();
				break;
			
			}
			
		}
		
	}
	
	/**
	 * WindowListener für GUI
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
		// userListBtn zurückgesetzt:
		public void windowIconified(WindowEvent arg0) {
			if (userListBtn.isSelected()) {
				userListZuklappen();
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
			if (userListBtn.isSelected()) {
				userListWin.toFront();
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
