import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

//import com.nilo.plaf.nimrod.NimRODLookAndFeel;

/**
 * @author ABerthold
 *
 */
public class GUI extends JFrame implements Observer {
	
	public ChatEngine ce;
	public LogEngine log;
	
	private static GUI me;
	private List<Node> nodes;
	private List<ChatWindow> chatList;
	private JTabbedPane jTabbedPane;
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem menuItemRequestFile;
	private JMenu lafMenu;
	private JMenuItem lafNimROD;
	private JToggleButton userListBtn;
	private UserList userListWin;
	
	
	/**
	 * Konstruktor für GUI 
	 */
	private GUI(){
		try {
			this.ce = ChatEngine.getCE();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		
		this.log = new LogEngine();
		this.me=this;
		this.menuBar = new JMenuBar();
		this.fileMenu = new JMenu("Datei");
		this.menuItemRequestFile = new JMenuItem("request_File()");
		this.lafMenu = new JMenu("Design wechsel");
		this.chatList = new ArrayList<ChatWindow>();
		this.jTabbedPane = new JTabbedPane();
		this.userListBtn = new JToggleButton("<");
		this.lafNimROD = new JMenuItem("NimROD");
		
		
		for(UIManager.LookAndFeelInfo laf:UIManager.getInstalledLookAndFeels()){
			JMenuItem tempJMenuItem = new JMenuItem(laf.getName());
			lafMenu.add(tempJMenuItem);
			tempJMenuItem.addActionListener(new lafController(lafMenu, laf));
		}
		
		
		// Listener zu den einzelen Komponenten hinzufügen:
		// ActionListener für das MenuItemNimRoD
//		this.lafNimROD.addActionListener(new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				try{
//					UIManager.setLookAndFeel(new NimRODLookAndFeel());
//				} catch (Exception ex){
//					System.out.println(ex.getMessage());
//				}
//				SwingUtilities.updateComponentTreeUI(GUI.me);
//				GUI.me.pack();
//			}
//		});
		
		
		// ActionListener für die MenuItemRequestFile:
		this.menuItemRequestFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				request_File();
			}
		});
		
		
		this.userListBtn.setMargin(new Insets(5, 5, 5, 5));
		this.userListBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JToggleButton source = (JToggleButton)e.getSource();
				if(source.isSelected()){
					userListWin = new UserList(GUI.me);
					userListWin.aufklappen();
				} else {
					userListWin.zuklappen();
				}
			}
		});
		
		
		
		
		// Menüs hinzufügen:
		this.fileMenu.add(menuItemRequestFile);
		this.fileMenu.add(lafMenu);
		this.lafMenu.add(lafNimROD);
		this.menuBar.add(userListBtn);
		this.menuBar.add(fileMenu);
		this.setJMenuBar(menuBar);
		
		
		// GUI Komponenten hinzufügen:
		this.add(jTabbedPane);
		this.addChat(new ChatWindow("public"));

		
		// GUI Einstellungen:
		this.setIconImage(new ImageIcon("res/pM_Logo2.png").getImage());
		this.setLocationRelativeTo(null);
		this.pack();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("pMAIN");
		this.setVisible(true);
	}
	
	/**
	 * Diese Methode fügt ein ChatWindow hinzu
	 * @param cw
	 */
	public void addChat(final ChatWindow cw){
		//evtl noch Typunterscheidung hinzufügn
		String title = cw.getTabText();
		
		this.chatList.add(cw);
		this.jTabbedPane.addTab(title, cw);
		
		int index = jTabbedPane.indexOfComponent(cw);
		
		JPanel pnlTab = new JPanel(new BorderLayout());
		pnlTab.setOpaque(false);
		
		JLabel lblTitle = new JLabel(title + "   ");
		lblTitle.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
			}
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getModifiersEx() == 2048){
					getGUI().delChat(cw);
				} else {
					jTabbedPane.setSelectedComponent(cw);
				}
			}
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
			}
		});
		
		
		JButton btnClose = new JButton("x");
		btnClose.setFont(new Font("mei", Font.PLAIN, 10));
		btnClose.setOpaque(false);
		btnClose.setMargin(new Insets(0, 4, 2, 4));
		btnClose.setBorderPainted(false);
		btnClose.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void mouseClicked(MouseEvent arg0) {
				getGUI().delChat(cw);
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				JButton source = (JButton)e.getSource();
	//			btnClose.setOpaque(true);
				source.setForeground(Color.RED);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				JButton source = (JButton)e.getSource();
	//			btnClose.setOpaque(false);
				source.setForeground(Color.BLACK);
			}
		});
		
		
		
		
		pnlTab.add(lblTitle, BorderLayout.CENTER);
		pnlTab.add(btnClose, BorderLayout.EAST);
		
		this.jTabbedPane.setTabComponentAt(index, pnlTab);
		
	}
	
	/**
	 * Diese Methode entfernt ein ChatWindow
	 * 
	 * Diese Methode sorgt dafür das ChatWindows aus der ArrayList "chats" entfernt werden
	 * und im GUI nicht mehr angezeigt werden.
	 * @param cw
	 */
	public void delChat(ChatWindow cw){
		this.jTabbedPane.remove(cw);
		this.chatList.remove(cw);
		if(chatList.isEmpty()){
			addChat(new ChatWindow("public"));
		}
	}
	
	/**
	 * Diese Methode stellt das GUI bereit
	 * 
	 * Diese Methode stellt das GUI für andere Klassen bereit
	 * um einen Zugriff auf GUI Attribute zu ermöglichen
	 * @return GUI
	 */
	public static GUI getGUI(){
		if(me == null){
			me = new GUI();
		}
		return me;
	}
	
	/**
	 * Diese Methode liefert ein Fileobjekt
	 * 
	 * Diese Methode bittet die GUI(den Nutzer) um ein Fileobjekt
	 * zur Ablage der empfangenen Datei
	 * @return File
	 */
	public File request_File(){
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showSaveDialog(getGUI());
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	       System.out.println("You chose to save this file: " +
	            fileChooser.getSelectedFile().getName());
	    }
		return fileChooser.getSelectedFile();
	}
	
	/**
	 * Diese Methode soll über Änderungen informieren
	 */
	public void notifyGUI(){
		// da muss noch was gemacht werden !!!
		// evtl fliegt die Methode auch raus wenn wir das
		// mit den Observerpattern machen...
	}
	
 	/**
 	 * MAIN
 	 * 
 	 * Diese Methode startet das Programm und erstellt ein GUI Objekt 
 	 * @param args
 	 */
 	public static void main(String[] args) {

		getGUI();
		
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

	public Node getNode(long sender) {
		for (Node x : nodes) if(x.getNodeID()==sender)return x;
		return null;
		}
	
	/**
	 * ActionListener für Design wechsel (LookAndFeel)
	 * @author ABerthold
	 *
	 */
	class lafController implements ActionListener{
		
		private JMenuItem lafMenu;
		private UIManager.LookAndFeelInfo laf;
		
		public lafController(JMenuItem lafMenu, UIManager.LookAndFeelInfo laf){
			this.lafMenu = lafMenu;
			this.laf = laf;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			try{
				UIManager.setLookAndFeel(laf.getClassName());
			} catch (Exception ex){
				System.out.println(ex.getMessage());
			}
			SwingUtilities.updateComponentTreeUI(GUI.me);
			GUI.me.pack();
		}
	}
		
}

