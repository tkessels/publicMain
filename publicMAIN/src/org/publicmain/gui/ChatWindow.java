package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;

/**
 * @author ATRM
 * 
 */

public class ChatWindow extends JPanel implements ActionListener, Observer {

	// Deklarationen:
	private String name;
	private JButton sendenBtn;
	private JTextPane msgTextPane;
	private HTMLEditorKit htmlKit;
	private HTMLDocument htmlDoc;
	private JScrollPane jScrollPane;
	private JTextField eingabeFeld;
	private String gruppe;
	private long user;
	private GUI gui;
	private History keyHistory;

	/**
	 * Erstellt Content und macht Layout für das Chatpanel
	 */
	private void doWindowbuildingstuff() {
		// Layout für ChatWindow (JPanel) festlegen auf BorderLayout:
		this.setLayout(new BorderLayout());

		// Initialisierungen:
		this.sendenBtn = new JButton("send");
		this.msgTextPane = new JTextPane();
		this.htmlKit = new HTMLEditorKit();
		this.htmlDoc = new HTMLDocument();
		this.jScrollPane = new JScrollPane(msgTextPane,	JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.eingabeFeld = new JTextField();


		msgTextPane.setEditable(false);
		msgTextPane.setPreferredSize(new Dimension(300, 200));
		msgTextPane.setEditorKit(htmlKit);
		msgTextPane.setDocument(htmlDoc);

		eingabeFeld.setDocument(new SetMaxText(200)); // später über Configure-Datei

		// KeyListener für Nachrichtenhistorie hinzufügen
		eingabeFeld.addKeyListener(keyHistory);

		sendenBtn.addActionListener(this);
		sendenBtn.addMouseListener(new MouseListener() {
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

		eingabeFeld.addActionListener(this);

		this.add(jScrollPane, BorderLayout.CENTER);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(eingabeFeld, BorderLayout.CENTER);
		panel.add(sendenBtn, BorderLayout.EAST);
		this.add(panel, BorderLayout.SOUTH);

		this.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent arg0) {
			}

			public void focusGained(FocusEvent arg0) {
				// Focus auf eingabeFeld setzen:
				eingabeFeld.requestFocusInWindow();
			}
		});

		this.setVisible(true);
	}

	public ChatWindow(long uid, String username) {
		this.user = uid;
		this.name = username;
		this.gui = GUI.getGUI();
		keyHistory = new History();
		doWindowbuildingstuff();
	}

	public ChatWindow(String gruppenname) {
		gruppe = gruppenname;
		this.name = gruppenname;
		this.gui = GUI.getGUI();
		keyHistory = new History();
		doWindowbuildingstuff();
	}

	/**
	 * @return String für Tab..
	 */
	public String getChatWindowName() {
		return this.name;
	}

	/**
	 * In dieser Methode werden die Texteingaben aus dem eingabeFeld verarbeitet
	 */
	public void actionPerformed(ActionEvent e) {
		// Eingabe aus dem Textfeld in String eingabe speichern
		String eingabe = eingabeFeld.getText();

		// Prüfen ob etwas eingegeben wurde, wenn nicht dann auch nichts machen
		if (!eingabe.equals("")) {

			// Eingabe in der ArrayList eingabeHistorie speichern und
			// Eingabezähler
			// auf die neue Länge der ArrayList eingabeHistorie setzen
			keyHistory.add(eingabe);
			// Prüfen ob die Eingabe ein Befehl ist
			if (eingabe.startsWith("/")) {
				String[] tmp;

				// Prüfen ob die Eingabe ein einfacher Befehl ist
				if (eingabe.equals("/clear")) {
					msgTextPane.setText("");
				} else if (eingabe.equals("/help")) {
					// TODO: Hilfetext in das Ausgabefeld schreiben
				} else if (eingabe.equals("/exit")) {
					// TODO: Ordentliches herunterfahren des Nodes
					printMessage("Node wird heruntergefahren...");
					System.exit(0);
				}

				// Prüfen ob es ein Befehl mit Parametern ist und ob diese vorhanden sind
				else if (eingabe.startsWith("/w ") && (tmp = eingabe.split(" ", 3)).length == 3) {
					// TODO: Hier muss noch ein ChatWindow ins GUI oder
					// wenn schon vorhanden das focusiert werden.
					// long tmpUid = user;
					// gui.ce.send_private(tmpUid, tmp[2]);
					printMessage("Flüsternachrichten noch nicht möglich...");
				}
				else if (eingabe.startsWith("/g ")	&& (tmp = eingabe.split(" ", 3)).length == 3) {
					// TODO: Hier muss noch der Gruppenname eingefügt werden
					// gui.ce.send_group(tmp[1], tmp[2]);
					printMessage("Gruppennachrichten noch nicht möglich...");
				}
				else {
					printMessage("Befehl nicht gültig oder vollständig...");
				}
			}

			// Wenn es kein Befehl ist muss es wohl eine Nachricht sein
			else if (gruppe == null) {
				// ggf. eingabe durch Methode filtern
				gui.ce.send_private(user, eingabe);
			}
			else {
				// ggf. eingabe durch Methode filtern
				gui.ce.send_group(gruppe, eingabe);
			}
		// In jedem Fall wird das Eingabefeld gelöscht
		eingabeFeld.setText("");
		}
	}

	public void update(Observable sourceChannel, Object msg) {
		MSG tmp = (MSG) msg;
		try {
			htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(), "<font color='blue'>" + String.valueOf(tmp.getSender() % 10000) + ": </font><font color='black'>" + (String) tmp.getData() + "</font>", 0, 0, null);
			msgTextPane.setCaretPosition(htmlDoc.getLength());
		} catch (BadLocationException | IOException e) {
			System.out.println(e.getMessage());
		}
		LogEngine.log("Nachricht für Ausgabe:" + tmp.toString(), this, LogEngine.INFO);
	}

	/**
	 * Methode zur Benachrichtigung des Benutzers über das Textausgabefeld
	 * (msgTextArea), gleichzeitig wird die LogEngine informiert.
	 * 
	 * @param reason
	 */
	public void printMessage(String reason) {
		try {
			htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(), "<font color='red'>" + reason + "</font>", 0, 0, null);
			msgTextPane.setCaretPosition(htmlDoc.getLength());
		} catch (BadLocationException | IOException e) {
			System.out.println(e.getMessage());
		}
		LogEngine.log("Benachrichtigung an den Nutzer: " + reason, this, LogEngine.INFO);
	}
	
	/**
	 * KeyListener für Nachrichtenhistorie ggf. für andere Dinge verwendbar
	 */
		
		class History implements KeyListener{

			private ArrayList<String> eingabeHistorie;
			private int eingabeAktuell;
			
			public History() {
				eingabeHistorie=new ArrayList<String>();
				eingabeAktuell=0;
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
