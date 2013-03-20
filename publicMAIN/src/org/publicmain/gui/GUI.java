package org.publicmain.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.LogEngine;
import org.publicmain.common.Node;

//import com.nilo.plaf.nimrod.NimRODLookAndFeel;

/**
 * @author ABerthold
 * 
 */
public class GUI extends JFrame implements Observer {

	// Deklarationen:
	public ChatEngine ce;
	public LogEngine log;

	private static GUI me;
	private List<Node> nodes;
	private List<ChatWindow> chatList;
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenu configMenu;
	private JMenu helpMenu;
	private JMenuItem aboutPMAIN;
	private JMenuItem menuItemRequestFile;
	private JMenu lafMenu;
	private ButtonGroup btnGrp;
	private JMenuItem lafNimROD;
	private JTabbedPane jTabbedPane;
	private JToggleButton userListBtn;
	private UserList userListWin;
	private pMTrayIcon trayIcon;

	/**
	 * Konstruktor f�r GUI
	 */
	private GUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

		// Initialisierungen:
		try {
			this.ce = ChatEngine.getCE();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		this.me = this;
		this.log = new LogEngine();
		this.menuBar = new JMenuBar();
		this.fileMenu = new JMenu("File");
		this.configMenu = new JMenu("Settings");
		this.helpMenu = new JMenu("Help");
		this.aboutPMAIN = new JMenuItem("About pMAIN");
		this.menuItemRequestFile = new JMenuItem("Test(request_File)");
		this.lafMenu = new JMenu("Switch Design");
		this.btnGrp = new ButtonGroup();
		this.chatList = new ArrayList<ChatWindow>();
		this.jTabbedPane = new JTabbedPane();
		this.userListBtn = new JToggleButton(new ImageIcon(getClass().getResource("UserListAusklappen.png")));
		this.lafNimROD = new JRadioButtonMenuItem("NimROD");
		this.trayIcon.createTrayIcon();
		
		// Anlegen der Men�eintr�ge f�r Designwechsel (installierte
		// LookAndFeels)
		// + hinzuf�gen zum lafMenu ("Designwechsel")
		// + hinzuf�gen der ActionListener (lafController)
		for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
			JRadioButtonMenuItem tempJMenuItem = new JRadioButtonMenuItem(laf.getName());
			System.out.println();
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
		this.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			// Wird das GUI minimiert wird die Userlist zugeklappt und der
			// userListBtn zur�ckgesetzt:
			public void windowIconified(WindowEvent arg0) {
				if (userListBtn.isSelected()) {
					userListBtn.setIcon(new ImageIcon(getClass().getResource("UserListAusklappen.png")));
					userListBtn.setToolTipText("Userlist einblenden");
					userListBtn.setSelected(false);
					userListWin.zuklappen();
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
		});

		// Focus Listener auf ChatWindow
		this.addWindowFocusListener(new WindowAdapter() {
			public void windowGainedFocus(WindowEvent e) {
				// Focus auf erste ChatWindow in chatList setzen:
				chatList.get(0).requestFocusInWindow();
			}
		});

		// TODO: Sp�ter auskommentieren damit NimRODLookAndFeel l�uft!
		// ActionListener f�r das JRadioButtonMenuItemNimRoD
		// this.lafNimROD.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(ActionEvent arg0) {
		// try{
		// UIManager.setLookAndFeel(new NimRODLookAndFeel());
		// } catch (Exception ex){
		// System.out.println(ex.getMessage());
		// }
		// SwingUtilities.updateComponentTreeUI(GUI.me);
		// GUI.me.pack();
		// }
		// });

		// ActionListener f�r die MenuItemRequestFile:
		this.menuItemRequestFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				request_File();
			}
		});

		// ActionListener f�r Help/About pMAIN:
		this.aboutPMAIN.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new AboutPublicMAIN(me, "About publicMAIN", true);
			}
		});

		// Konfiguration userListBtn
		this.userListBtn.setMargin(new Insets(2, 3, 2, 3));
		this.userListBtn.setToolTipText("Userlist einblenden");
		this.userListBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JToggleButton source = (JToggleButton) e.getSource();
				if (source.isSelected()) {
					userListBtn.setToolTipText("Userlist ausblenden");
					userListBtn.setIcon(new ImageIcon(getClass().getResource("UserListEinklappen.png")));
					userListWin = new UserList(GUI.me);
					userListWin.aufklappen();
				} else {
					userListBtn.setToolTipText("Userlist einblenden");
					userListBtn.setIcon(new ImageIcon(getClass().getResource("UserListAusklappen.png")));
					userListWin.zuklappen();
				}
			}
		});
		
		// Men�s hinzuf�gen:
		this.btnGrp.add(lafNimROD);
		this.lafMenu.add(lafNimROD);
		this.configMenu.add(lafMenu);
		this.fileMenu.add(menuItemRequestFile);
		this.helpMenu.add(aboutPMAIN);
		this.menuBar.add(userListBtn);
		this.menuBar.add(fileMenu);
		this.menuBar.add(configMenu);
		this.menuBar.add(helpMenu);

		// GUI Komponenten hinzuf�gen:
		this.setJMenuBar(menuBar);
		this.add(jTabbedPane);
		this.addChat(new ChatWindow("public"));

		// GUI JFrame Einstellungen:
		this.setIconImage(new ImageIcon(getClass().getResource("pM_Logo2.png")).getImage());
		this.pack();
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("publicMAIN");
		this.setVisible(true);
	}

	/**
	 * Diese Methode f�gt ein ChatWindow hinzu
	 * 
	 * Diese Methode f�gt ein ChatWindow zu GUI hinzu und setzt dessen
	 * Komponenten
	 * 
	 * @param cw
	 */
	public void addChat(final ChatWindow cw) {
		// TODO: evtl. noch Typunterscheidung hinzuf�gen (Methode
		// getCwTyp():String)
		String title = cw.getChatWindowName();

		// neues ChatWindow (cw) zur Chatliste (ArrayList<ChatWindow>)
		// hinzuf�gen:
		this.chatList.add(cw);
		// erzeugen von neuem Tab f�r neues ChatWindow:
		this.jTabbedPane.addTab(title, cw);

		// ChatWindow am NachrichtenListener (MSGListener) anmelden:
		// ce.group_join(title);
		ce.add_MSGListener(cw, title);

		// Index vom ChatWindow im JTabbedPane holen um am richtigen Ort
		// einzuf�gen:
		int index = jTabbedPane.indexOfComponent(cw);

		// JPanel f�r Tabbeschriftung erzeugen und durchsichtig machen:
		JPanel pnlTab = new JPanel();
		((FlowLayout) pnlTab.getLayout()).setHgap(5);
		pnlTab.setOpaque(false);

		// TitelLabel f�r Tabbeschriftung erzeugen:
		JLabel lblTitle = new JLabel(title);
		// MouseListener zu JLabel (lblTitle) hinzuf�gen:
		lblTitle.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
			}
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
			}
			@Override
			// beim verlassen der Maus von JLabel (lblTitle) wird die Schrift
			// schwarz
			public void mouseExited(MouseEvent e) {
				JLabel source = (JLabel) e.getSource();
				source.setForeground(Color.BLACK);
			}
			@Override
			// beim betreten der Maus von JLabel (lblTitle) wird die Schrift rot
			public void mouseEntered(MouseEvent e) {
				JLabel source = (JLabel) e.getSource();
				source.setForeground(new Color(255, 130, 13));
			}
			@Override
			// Mittlere Maustastenklick (=512) auf Label schlie�t das ChatWindow
			// jeder andere Klick f�hrt zur Auswahl des ChatWindows:
			public void mouseClicked(MouseEvent e) {
				if (e.getModifiersEx() == 512) {
					getGUI().delChat(cw);
				} else {
					jTabbedPane.setSelectedComponent(cw);
				}
			}
		});

		// ImageIcon f�r Schlie�enLabel erstellen:
		final ImageIcon tabCloseImgIcon = new ImageIcon(getClass().getResource("TabCloseBlack.png"));
		// Schlie�enLabel f�r Tabbeschriftung erzeugen und gestalten:
		JLabel lblClose = new JLabel(tabCloseImgIcon);
		// Observer f�r das Image auf das lblClose setzen:
		tabCloseImgIcon.setImageObserver(lblClose);
		// MouseListener f�r Schlie�enlabel (lblClose) hinzuf�gen:
		lblClose.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			// Bei Klick ChatWindow (cw) schliesen:
			public void mouseClicked(MouseEvent arg0) {
				getGUI().delChat(cw);
			}
			@Override
			// bei Mouseover wird das "x" des Schlie�enbutton (btnClose) rot:
			public void mouseEntered(MouseEvent e) {
				tabCloseImgIcon.setImage(new ImageIcon(getClass().getResource("TabCloseOrange.png")).getImage());
			}
			@Override
			// beim verlassen der Maus wird das "x" des Schlie�enbutton
			// (btnClose) schwarz:
			public void mouseExited(MouseEvent e) {
				tabCloseImgIcon.setImage(new ImageIcon(getClass().getResource("TabCloseBlack.png")).getImage());
			}
		});

		// TitelLabel (lblTitle) + Schlie�enLabel (btnClose) zum Tab (pnlTab) hinzuf�gen:
		pnlTab.add(lblTitle);
		pnlTab.add(lblClose);

		// den neuen Tab an die Stelle von index setzen:
		this.jTabbedPane.setTabComponentAt(index, pnlTab);
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
		// ChatWindow (cw) aus jTabbedPane entfernen:
		this.jTabbedPane.remove(cw);
		// ChatWindow aus Chatliste entfernen:
		this.chatList.remove(cw);
		// ChatWindow aus Gruppe entfernen (MSGListener abschalten):
		ce.group_leave(cw.getChatWindowName());
		// Falls keine ChatWindows mehr wird public ge�ffnet:
		if (chatList.isEmpty()) {
			// TODO: Hier evtl. noch anderen Programmablauf implementier
			// z.B. schlie�en des Programms wenn letztes ChatWindow geschlossen
			// wird
			addChat(new ChatWindow("public"));
		}
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
	 * Diese Methode liefert ein Fileobjekt
	 * 
	 * Diese Methode bittet die GUI(den Nutzer) um ein Fileobjekt zur Ablage der
	 * empfangenen Datei
	 * 
	 * @return File
	 */
	public File request_File() {
		// TODO: hier stimmt noch nix! sp�ter �berarbeiten!
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showSaveDialog(me);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("You chose to save this file: " + fileChooser.getSelectedFile().getName());
		}
		return fileChooser.getSelectedFile();
	}

	/**
	 * Diese Methode soll �ber �nderungen informieren
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
		// TODO Auto-generated method stub
	}

	/**
	 * Diese Methode stellt das Node bereit
	 * 
	 * Diese Methode ist ein Getter f�r das Node
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

		public lafController(JMenuItem lafMenu, UIManager.LookAndFeelInfo laf) {
			this.lafMenu = lafMenu;
			this.laf = laf;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				UIManager.setLookAndFeel(laf.getClassName());
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
			SwingUtilities.updateComponentTreeUI(GUI.me);
			GUI.me.pack();
		}
	}

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
