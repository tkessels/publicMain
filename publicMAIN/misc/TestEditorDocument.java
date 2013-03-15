package org.publicmain.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;

public class TestEditorDocument extends JPanel implements ActionListener,
		ItemListener {

	private static final long serialVersionUID = 2232612551746729177L;

	// Textpane zur Darstellung
	private JTextPane pane = null;
	// Unser Document
	private EditorDocument doc = null;

	// Button um etwas fett zu markieren
	private JButton bold = null;
	// Button um etwas krusiv zu markieren
	private JButton italic = null;
	// Button um etwas zu unterstreichen
	private JButton underline = null;
	// Button um die Schriftfarbe zu ändern
	private JButton foreground = null;
	// Button um die Hintergrundfarbe zu ändern
	private JButton background = null;
	// Button um ein Bild einzufügen
	private JButton pic = null;

	// JComboBox mit allen verfügbaren Schriftarten
	private JComboBox fonts = null;
	// JComboBox mit unterschiedlichen Schriftgrößen
	private JComboBox size = null;

	// Toolbar für die Buttons und ComboBoxen
	private JToolBar bar = null;

	// JFileChooser um ggf. ein Bild auswählen zu können
	private JFileChooser imch = null;

	public TestEditorDocument() {

		setLayout(new BorderLayout());

		// Anzeigebereich erzeugen
		this.doc = new EditorDocument();
		this.pane = new JTextPane(this.doc);

		// Tools initialisieren
		this.bold = new JButton("F");
		this.italic = new JButton("I");
		this.underline = new JButton("U");
		this.foreground = new JButton("SF");
		this.background = new JButton("HF");
		this.pic = new JButton("Bild");

		this.fonts = new JComboBox();
		this.size = new JComboBox();

		// restliche Komponenten initialisieren
		this.bar = new JToolBar();

		this.imch = new JFileChooser();

		// Einen FileFilter für die Bildauswahl setzen
		// es dürfen nur jpg und png Bilder eingefügt werden
		this.imch.setFileFilter(new FileFilter() {

			public boolean accept(File f) {

				if (f.isDirectory()) {
					return true;
				}
				if (f.getAbsolutePath().toLowerCase().endsWith(".png")) {
					return true;
				}
				if (f.getAbsolutePath().toLowerCase().endsWith(".jpg")) {
					return true;
				}
				return f.getAbsolutePath().toLowerCase().endsWith(".jpeg");
			}

			public String getDescription() {
				return "Image (*.jpg, *.jpeg, *.png)";
			}
		});

		// Verschiedene Schriftgrößen initialisieren
		for (int i = 8; i < 30; i += 2) {
			this.size.addItem(i);
		}

		// Alle verfügbaren Schriftarten auslesen und in der JComboBox anzeigen
		String[] font = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getAvailableFontFamilyNames();

		for (int i = 0; i < font.length; i++) {
			this.fonts.addItem(font[i]);
		}

		// GUI zusammenbauen
		add(this.pane);
		add(this.bar, BorderLayout.NORTH);

		this.bar.add(this.bold);
		this.bar.add(this.italic);
		this.bar.add(this.underline);
		this.bar.add(this.fonts);
		this.bar.add(this.size);
		this.bar.add(this.foreground);
		this.bar.add(this.background);
		this.bar.add(this.pic);

		// Listener an die Tools hängen
		this.bold.addActionListener(this);
		this.italic.addActionListener(this);
		this.underline.addActionListener(this);
		this.foreground.addActionListener(this);
		this.background.addActionListener(this);
		this.pic.addActionListener(this);

		this.fonts.addItemListener(this);
		this.size.addItemListener(this);
	}

	public void actionPerformed(ActionEvent evt) {

		// Start und End Position der Selektion auslesen
		int start = this.pane.getSelectionStart();
		int end = this.pane.getSelectionEnd();
		// Falls keine sinnvolle Selektion => kompletten Text bearbeiten
		if (start >= end) {
			start = 0;
			end = this.pane.getText().length();
		}
		// Fett setzen
		if (evt.getSource() == this.bold) {
			// Falls das erste, selektierte Zeichen bereits fett dargestellt
			// wird, die Fett-Formatierung aufheben, ansonsten den selektierten
			// Bereich fett darstellen
			this.doc.setBold(start, end, !this.doc.isBold(start));
		}
		// Kursiv setzen
		else if (evt.getSource() == this.italic) {
			// siehe "Fett setzen"
			this.doc.setItalic(start, end, !this.doc.isItalic(start));
		}
		// Unterstreichen
		else if (evt.getSource() == this.underline) {
			// siehe "Fett setzen"
			this.doc.setUnderline(start, end, !this.doc.isUnderline(start));
		}
		// Schriftfarbe verändern
		else if (evt.getSource() == this.foreground) {
			Color col = JColorChooser.showDialog(this,
					"Schriftfarbe auswählen", Color.BLACK);
			if (col != null) {
				this.doc.setForeground(start, end, col);
			}
		}
		// Hintergrundfarbe verändern
		else if (evt.getSource() == this.background) {
			Color col = JColorChooser.showDialog(this,
					"Hintergrundfarbe auswählen", Color.WHITE);
			if (col != null) {
				this.doc.setBackground(start, end, col);
			}
		}
		// Bild einfügen
		else if (evt.getSource() == this.pic) {
			int retval = this.imch.showOpenDialog(this);
			if (retval == JFileChooser.APPROVE_OPTION) {
				this.pane.replaceSelection("");
				try {
					this.doc.setIcon(this.pane.getCaretPosition(),
							new ImageIcon(this.imch.getSelectedFile()
									.getAbsolutePath()));
				} catch (BadLocationException ble) {
					ble.printStackTrace();
				}
			}
		}
	}

	public void itemStateChanged(ItemEvent evt) {

		// Nur reagieren, falls etwas neues selektiert wurde
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			// Start und End Position der Selektion auslesen
			int start = this.pane.getSelectionStart();
			int end = this.pane.getSelectionEnd();
			// Falls keine sinnvolle Selektion => kompletten Text bearbeiten
			if (start >= end) {
				start = 0;
				end = this.pane.getText().length();
			}
			// Schriftart setzen
			if (evt.getSource() == this.fonts) {
				this.doc.setFont(start, end, this.fonts.getSelectedItem()
						.toString());
			}
			// Schriftgröße setzen
			else if (evt.getSource() == this.size) {
				this.doc.setFontSize(start, end, Integer.parseInt(this.size
						.getSelectedItem().toString()));
			}
		}
	}

	// Main Methode zum Testen
	public static void main(String[] args) {

		JFrame frame = new JFrame("EditorComponent Test");
		frame.add(new JScrollPane(new TestEditorDocument()));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 300);
		frame.setVisible(true);
	}
}