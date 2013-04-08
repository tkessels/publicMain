package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.Node;

/**
 * @author ATRM
 * 
 */

public class ChatWindow extends JPanel implements ActionListener, Observer {

	// Deklarationen:
	private String name;
	private ArrayList<MSG> msgList;
	private JButton sendenBtn;
	private JTextPane msgTextPane;
	private HTMLEditorKit htmlKit;
	private HTMLDocument htmlDoc;
	private JScrollPane msgTextScroller;
	private JTextField eingabeFeld;
	private String gruppe;
	private long user;
	private boolean isPrivCW;
	private GUI gui;
	private History keyHistory;
	private ChatWindowTab myTab;
	private JPanel panel;
	private String helptext="<br><table color='green'>" +
			"<tr><td colspan='3'><b>Kurzbefehl</b></td><td><b>Erläuterung</b></td></tr>" +
			"<tr><td colspan='3'>/clear</td><td>Anzeige löschen</td></tr>" +
			"<tr><td colspan='3'>/exit</td><td>Programm beenden</td></tr>" +
			"<tr><td colspan='3'>/help</td><td>zeigt diese Hilfe an</td></tr>" +
			"<tr><td>/ignore</td><td colspan='2'>&lt;username&gt;</td><td>User ignorieren</td></tr>" +
			"<tr><td>/unignore</td><td colspan='2'>&lt;username&gt;</td><td>User nicht weiter ignorieren</td></tr>" +
			"<tr><td>/info</td><td colspan='2'>&lt;username&gt;</td><td>Informationen über User erhalten</td></tr>" +
			"<tr><td>/g</td><td>&lt;groupname&gt;</td><td>&lt;message&gt;</td>Nachricht an Gruppe</tr>" +
			"<tr><td>/w</td><td>&lt;username&gt;</td><td>&lt;message&gt;</td>Flüsternachricht</tr>" +
			"<tr><td>/s</td><td  colspan='2'>&lt;message&gt;</td>schreien</tr>" +
			"</table><br>";

	public ChatWindow(long uid, String username) {
		this.user = uid;
		this.name = username;
		this.isPrivCW = true;
		doWindowbuildingstuff();
	}

	public ChatWindow(String gruppenname) {
		gruppe = gruppenname;
		this.name = gruppenname;
		this.isPrivCW = false;
		doWindowbuildingstuff();
	}

	/**
	 * Erstellt Content und macht Layout für das Chatpanel
	 */
	private void doWindowbuildingstuff() {
		this.myTab =  new ChatWindowTab(name, GUI.getGUI().getTabbedPane(), this); 
		// Layout für ChatWindow (JPanel) festlegen auf BorderLayout:
		this.setLayout(new BorderLayout());

		// Initialisierungen:
		this.gui = GUI.getGUI();
		this.msgList = new ArrayList<MSG>();
		this.sendenBtn = new JButton("send");
		this.msgTextPane = new JTextPane();
		this.htmlKit = new HTMLEditorKit();
		this.htmlDoc = new HTMLDocument();
		this.msgTextScroller = new JScrollPane(msgTextPane,	JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.eingabeFeld = new JTextField();
		this.panel = new JPanel(new BorderLayout());

		this.msgTextPane.setEditable(false);
		this.msgTextPane.setPreferredSize(new Dimension(400, 300));
		this.msgTextPane.setEditorKit(htmlKit);
		this.msgTextPane.setDocument(htmlDoc);

		this.eingabeFeld.setDocument(new SetMaxText(200)); // später über Configure-Datei

		// KeyListener für Nachrichtenhistorie hinzufügen
		this.eingabeFeld.addKeyListener(new History(eingabeFeld));

		this.sendenBtn.addActionListener(this);
		this.sendenBtn.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
				JButton source = (JButton) e.getSource();
				source.setForeground(Color.BLACK);
			}

			public void mouseEntered(MouseEvent e) {
				JButton source = (JButton) e.getSource();
				source.setForeground(new Color(255, 130, 13));
			}

			public void mouseClicked(MouseEvent e) {
			}
		});

		this.eingabeFeld.addActionListener(this);
		this.keyHistory=new History(eingabeFeld);

		this.panel.add(eingabeFeld, BorderLayout.CENTER);
		this.panel.add(sendenBtn, BorderLayout.EAST);
		
		this.add(msgTextScroller, BorderLayout.CENTER);
		this.add(panel, BorderLayout.SOUTH);

		this.setVisible(true);
	}
	
	/**
	 * @return String für Tab..
	 */
	public String getChatWindowName() {
		return this.name;
	}
	
	/**
	 * @return
	 */
	public JPanel getWindowTab(){
		return this.myTab;
	}
	
	void focusEingabefeld(){
		this.eingabeFeld.requestFocusInWindow();
		this.myTab.stopBlink();
	}
	
	/**
	 * @return ture wenn privates ChatWindow
	 */
	public boolean isPrivate(){
		return this.isPrivCW;
	}
	
	/**
	 * @return true wenn Gruppen ChatWindow 
	 */
	public boolean isGroup(){
		return !this.isPrivCW;
	}
	
	/**
	 * @param x
	 */
	private void info(String x){
		putMSG(new MSG(x,MSGCode.CW_INFO_TEXT));
	}
	
	/**
	 * @param x
	 */
	private void warn(String x){
		putMSG(new MSG(x,MSGCode.CW_WARNING_TEXT));
	}
	
	/**
	 * @param x
	 */
	private void error(String x){
		putMSG(new MSG(x,MSGCode.CW_ERROR_TEXT));
	}
	
	/**
	 * In dieser Methode werden die Texteingaben aus dem eingabeFeld verarbeitet
	 */
	public void actionPerformed(ActionEvent e) {
		// Eingabe aus dem Textfeld in String eingabe speichern
		String eingabe = eingabeFeld.getText();

		// HTML-Elemente verhindern
		eingabe = eingabe.replaceAll("<", "&lt;").replaceAll(">", "&gt;");

		// erlaubte HTML-Elemente mit anderem Syntax einfügen
		eingabe = eingabe.replaceAll("(\\[)(?=/?(b|u|i|strike)\\])", "<");
		eingabe = eingabe.replaceAll("(?<=((</?)(b|u|i|strike)))(\\])", ">");
		
		// Prüfen ob etwas eingegeben wurde, wenn nicht dann auch nichts machen
		if (!eingabe.equals("")) {

			// Prüfen ob die Eingabe ein Befehl ist
			if (eingabe.startsWith("/")) {
				String[] tmp;
				
				// Prüfen ob die Eingabe ein einfacher Befehl ist
				if (eingabe.equals("/clear")) {
					this.msgTextPane.setText("");
				} else if (eingabe.equals("/help")) {
					info(helptext);
				} else if (eingabe.equals("/exit")) {
					this.gui.shutdown();
				}

				// Prüfen ob es ein Befehl mit Parametern ist und ob diese vorhanden sind
				else if (eingabe.startsWith("/ignore ")	&& (tmp = eingabe.split(" ", 2)).length == 2) {
					if(this.gui.ignoreUser(tmp[1])){
						info(tmp[1] + " wird ignoriert!");
					} else {
						warn("Ignorieren nicht möglich! " + tmp[1] + " wurde nicht gefunden!");
					}
				}else if (eingabe.startsWith("/debug ") && (tmp = eingabe.split(" ", 3)).length >= 2) {
					ChatEngine.getCE().debug(tmp[1],(tmp.length>2)?tmp[2]:"");
				}
				else if (eingabe.startsWith("/alias ") && (tmp = eingabe.split(" ", 2)).length == 2) {
					GUI.getGUI().changeAlias(tmp[1]);
				}
				else if (eingabe.startsWith("/unignore ") && (tmp = eingabe.split(" ", 2)).length == 2){
					if(this.gui.unignoreUser(tmp[1])){
						info(tmp[1] + " wird nicht weiter ignoriert!");
					} else {
						warn(tmp[1] + "wurde nicht gefunden!");
					}
				}
				else if (eingabe.startsWith("/w ") && (tmp = eingabe.split(" ", 3)).length == 3) {
					this.gui.privSend(tmp[1], tmp[2]);
					warn("Flüsternachrichten noch nicht möglich...");
				}
				else if (eingabe.startsWith("/g ")	&& (tmp = eingabe.split(" ", 3)).length == 3) {
					this.gui.groupSend(tmp[1], tmp[2]);
				}
				else {
					error("Befehl nicht gültig oder vollständig...");
				}
			}

			// Wenn es kein Befehl ist muss es wohl eine Nachricht sein
			else if (gruppe == null) {
				// ggf. eingabe durch Methode filtern
				this.gui.privSend(user, eingabe);
			}
			else {
				// ggf. eingabe durch Methode filtern
				this.gui.groupSend(gruppe, eingabe);
						}
		// In jedem Fall wird das Eingabefeld gelöscht
		this.eingabeFeld.setText("");
		}
	}

	public void update(Observable sourceChannel, Object msg) {
		if(GUI.getGUI().getTabbedPane().indexOfComponent(this)!=GUI.getGUI().getTabbedPane().getSelectedIndex()){
			this.myTab.startBlink();
		}
		MSG tmpMSG = (MSG) msg;
		this.putMSG(tmpMSG);
		LogEngine.log(this,"ausgabe",tmpMSG);
	}
	
	/**
	 * @param msg
	 */
	public void putMSG(MSG msg){
		this.msgList.add(msg);
		this.printMSG(msg);
	}
	/**
	 * Methode zur Benachrichtigung des Benutzers über das Textausgabefeld
	 * (msgTextArea), gleichzeitig wird die LogEngine informiert.
	 * 
	 * @param reason
	 */
	private void printMSG(MSG msg) {
		String color = "black";
		Node sender = ChatEngine.getCE().getNode(msg.getSender());
		String senderalias = (sender!=null)? sender.getAlias():"unknown";
		
		switch(msg.getTyp()){
		
		case SYSTEM:
			if(msg.getCode() == MSGCode.CW_INFO_TEXT){
				color = "green";
			} else {
				color = "red";
			}
			try {
				htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(),"<font color='" + color + "'>System: " + (String) msg.getData() + "</font>", 0, 0, null);
			} catch (BadLocationException | IOException e) {
				LogEngine.log(e);
			}
			break;
		case GROUP:
			try {
				htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(), "<font color='orange'>" + senderalias +": </font><font color='black'>" + (String) msg.getData() + "</font>", 0, 0, null);
			} catch (BadLocationException | IOException e) {
				LogEngine.log(e);
			}
			break;
		case PRIVATE:
			try {
				htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(), "<font color='blue'>" + senderalias + ": </font><font color='black'>" + (String) msg.getData() + "</font>", 0, 0, null);
			} catch (BadLocationException | IOException e) {
				LogEngine.log(e);
			}
			break;
		
		}
		msgTextPane.setCaretPosition(htmlDoc.getLength());
		//LogEngine.log(this,"printing",msg);
	}
	
	/**
	 * KeyListener für Nachrichtenhistorie ggf. für andere Dinge verwendbar
	 */
		class History implements KeyListener{

			private ArrayList<String> eingabeHistorie;
			private int eingabeAktuell;
			
			public History(JTextField target) {
				eingabeHistorie=new ArrayList<String>();
				eingabeAktuell=0;
				target.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
					add(((JTextField)e.getSource()).getText());	
					}
				});
				target.addKeyListener(this);
			}

			public void keyTyped(KeyEvent arg0) {
			}

			public void add(String eingabe) {
				eingabeHistorie.add(eingabe);
				eingabeAktuell = eingabeHistorie.size();
			}

			public void keyReleased(KeyEvent arg0) {
			}

			public void keyPressed(KeyEvent arg0) {
				JTextField tmp = (JTextField) arg0.getSource();
				if (arg0.getKeyCode() == 38 && eingabeAktuell > 0) {
					eingabeAktuell--;
					tmp.setText(eingabeHistorie.get(eingabeAktuell));
				} else if (arg0.getKeyCode() == 40 && eingabeAktuell < eingabeHistorie.size()) {
					eingabeAktuell++;
					if (eingabeAktuell < eingabeHistorie.size()) {
						tmp.setText(eingabeHistorie.get(eingabeAktuell));
					} else
						tmp.setText("");
				} else {

				}
		}
	};
}
