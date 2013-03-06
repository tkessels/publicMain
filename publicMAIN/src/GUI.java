import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

//import com.nilo.plaf.nimrod.NimRODLookAndFeel;

public class GUI extends JFrame {
	
//	public ChatEngine ce;
//	public LogEngine log;
	
	private static GUI me; 

	private List<ChatWindow> chats;
	private JTabbedPane jTp;
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem menuItem;
	
	/**
	 * Konstruktor für GUI 
	 */
	public GUI(){
//		ce = new ChatEngine();
//		log = new LogEngine();
		
		GUI.me=this;
//		try {
//			// LookAndFeel auf NimRODLookAndFeel setzen:
//			UIManager.setLookAndFeel(new NimRODLookAndFeel());
//		} 
//		catch (UnsupportedLookAndFeelException e) {
//			System.out.println(e.getMessage());
//		}
		
		this.menuBar = new JMenuBar();
		this.fileMenu = new JMenu("Datei");
		this.menuItem = new JMenuItem("Speichern unter");
		this.chats = new ArrayList<ChatWindow>();
		this.jTp = new JTabbedPane();
		
		jTp.setTabPlacement(JTabbedPane.TOP);
//		jTp.setBackground(Color.CYAN);
//		jTp.setForeground(Color.RED);
		
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				request_File();
				
			}
		});
		
		
		fileMenu.add(menuItem);
		this.menuBar.add(fileMenu);
		this.setJMenuBar(menuBar);
		
		
		this.add(jTp);
		
		addChat(new ChatWindow("public"));
		addChat(new ChatWindow("mattin"));

		this.setIconImage(new ImageIcon("pM_Logo2.png").getImage());
		this.setLocationRelativeTo(null);
		this.pack();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("pMain");
		this.setVisible(true);
	}
	
	public void addChat(final ChatWindow cw){
		//evtl noch Typunterscheidung hinzufügn
		String title = cw.getName();
		
		chats.add(cw);
		jTp.addTab(title, cw);
		
		int index = jTp.indexOfTab(title);
		
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
					jTp.setSelectedComponent(cw);
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
		
		jTp.setTabComponentAt(index, pnlTab);
		
	}
	
	/**
	 * Diese Methode entfernt ein ChatWindow
	 * 
	 * Diese Methode sorgt dafür das ChatWindows aus der ArrayList "chats" entfernt werden
	 * und im GUI nicht mehr angezeigt werden.
	 * @param cw
	 */
	public void delChat(ChatWindow cw){
		jTp.remove(cw);
		chats.remove(cw);
		if(chats.isEmpty()){
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
		return GUI.me;
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
		int returnVal = fileChooser.showOpenDialog(getParent());
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	       System.out.println("You chose to open this file: " +
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

		new GUI();
		
	}
}
