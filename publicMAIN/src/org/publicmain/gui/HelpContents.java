package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.images.Help;

/**
 * Diese Klasse stellt die Hilfeseiten Help/Help Content zur Verfügung.
 * TODO: Kommentar
 * 
 * @author ATRM
 * 
 */

public class HelpContents {
	
	private JDialog hcDialog;
	private JTextField searchField;
	private JButton searchButton;
	private JPanel searchPanel;
	private JTextPane helpContentTxt;
	private HTMLEditorKit htmlKit;
	private HTMLDocument htmlDoc;

	/**
	 * Konstruktor für das Help Content Frame
	 */
	public HelpContents() {
		
		this.hcDialog 		= new JDialog(GUI.getGUI(), "Help Content", false);
		this.searchField 	= new JTextField("Search Keyword...");
		this.searchButton 	= new JButton("Find");
		this.searchPanel 	= new JPanel();
		this.helpContentTxt = new JTextPane();
		this.htmlKit 		= new HTMLEditorKit();
		this.htmlDoc 		= new HTMLDocument();

		helpContentTxt.setBackground(new Color(229, 195, 0));
		helpContentTxt.setEditable(false);
		helpContentTxt.setEditorKit(htmlKit);
		helpContentTxt.setDocument(htmlDoc);

		searchPanel.setLayout(new BorderLayout());
		
		// hinzufügen
		hcDialog.add(searchPanel, BorderLayout.NORTH);
		searchPanel.add(searchField, BorderLayout.CENTER);
		searchPanel.add(searchButton, BorderLayout.EAST);
		hcDialog.add(helpContentTxt, BorderLayout.CENTER);

		addIndex();
//		addChapter();
		
		hcDialog.setIconImage(new ImageIcon(Help.class.getResource("pM_Logo2.png")).getImage());
		hcDialog.setMinimumSize(new Dimension(250, GUI.getGUI().getHeight()));
		hcDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		hcDialog.pack();
		hcDialog.setLocation(GUI.getGUI().getLocation().x+GUI.getGUI().getWidth(), GUI.getGUI().getLocation().y);
		hcDialog.setVisible(true);
	}

	/**
	 * TODO: Kommentar
	 */
	private void addIndex() {
		try {
			htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(),
					"<h1>Index</h1><br>" + "<h2>Kapitel 1</h2><br>"
							+ "<h3>Kapitel 2</h3><br>", 0, 0, null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * TODO: Kommentar
	 */
	private void addChapter() {
		try {
			htmlKit.insertHTML(
					htmlDoc,
					htmlDoc.getLength(),
					"<h2>Kapitel 1</h2><br>"
							+ "hier steht witziger erklärungstext vom ersten kapitel<br>"
							+ "<h3>Kapitel 2</h3><br>"
							+ "hier steht witziger erklärungstext vom zweiten kapitel<br>", 0, 0, null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}