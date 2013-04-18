package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.images.Help;

/**
 * Diese Klasse stellt die Hilfeseiten Help/Help Content zur Verfügung. TODO:
 * Kommentar
 * 
 * @author ATRM
 * 
 */

public class HelpContents extends JDialog {

	private static HelpContents me; 

	public static HelpContents getMe() {
		return me;
	}

	private JTextPane helpContentTxt;
	private JScrollPane sp;
	private File htmlFile;
	private java.net.URL fileURL;
	private GraphicsEnvironment ge;
	private GraphicsDevice gd;
	private DisplayMode dm;

	/**
	 * Konstruktor für das Help Content Frame
	 */
	public HelpContents() {
		
		this.me=this;
		this.setTitle("Help Content");
		this.setModal(false);
		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		this.setLayout(new BorderLayout());
		this.setIconImage(Help.getIcon("helpContentsIcon.png").getImage());
		this.setMinimumSize(new Dimension(250, GUI.getGUI().getHeight()));
		this.setPreferredSize(new Dimension(250, GUI.getGUI().getHeight()));

		this.helpContentTxt = new JTextPane();
		this.sp = new JScrollPane(helpContentTxt,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.htmlFile = new File(getClass().getResource("helpcontent.html").getFile());
		this.ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		this.gd = ge.getDefaultScreenDevice();
		this.dm = gd.getDisplayMode();

		helpContentTxt.setBackground(new Color(25, 169, 241));
		helpContentTxt.setEditable(false);

		try {
			// Dateipfad in eine URL umwandeln
			fileURL = htmlFile.toURI().toURL();
			// Datei in JTextPane laden
			helpContentTxt.setPage(fileURL);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.add(sp, BorderLayout.CENTER);
		this.pack();
		
		showIt();
	}

	public void showIt() {
		if ((GUI.getGUI().getLocation().x + GUI.getGUI().getWidth() + this.getWidth() < dm.getWidth())) {
			this.setLocation(GUI.getGUI().getLocation().x + GUI.getGUI().getWidth(), GUI.getGUI().getLocation().y);
		} else {
			this.setLocationRelativeTo(null);
		}
		this.setVisible(true);
	}
}