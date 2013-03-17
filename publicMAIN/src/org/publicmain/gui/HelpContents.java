package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 * Diese Klasse stellt das HilfeFenster und den dazugehörogen Inhalt bereit
 * 
 * @author rpfaffner
 * 
 */

public class HelpContents {

	private JFrame hcframe;
	private JTextField searchField;
	private JButton SearchButton;
	private JTextPane helpContentTxt;
	private HTMLEditorKit htmlKit;
	private HTMLDocument htmlDoc;

	// TODO: Suche ermöglichen! Mehrere Intanzen zuassen?

	public HelpContents() {
		this.hcframe = new JFrame("Help Contents");
		this.searchField = new JTextField("What are you searchting for?");
		this.SearchButton = new JButton("Search");
		this.helpContentTxt = new JTextPane();
		this.htmlKit = new HTMLEditorKit();
		this.htmlDoc = new HTMLDocument();

		hcframe.setLocationRelativeTo(null);
		hcframe.setIconImage(new ImageIcon("media/pM_Logo2.png").getImage());

		searchField.setPreferredSize(new Dimension(500, 1));

		helpContentTxt.setBackground(Color.YELLOW);
		helpContentTxt.setEditable(false);
		helpContentTxt.setEditorKit(htmlKit);
		helpContentTxt.setDocument(htmlDoc);

		// hinzufügen
		hcframe.add(searchField, BorderLayout.WEST);
		hcframe.add(SearchButton, BorderLayout.EAST);
		hcframe.add(helpContentTxt, BorderLayout.SOUTH);

		addIndex();
		addChapter();
		hcframe.pack();
		hcframe.setVisible(true);
	}

	private void addIndex() {
		//TODO: herausfinden warum überschriften <h1,2,3,> nicht interpretiert werden! Was wird überhaupt alles interpretiert?
		try {
			htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(),	"<h1>Index</h1>		<br>" +
																"<h2>Kapitel 1</h2>		<br>" + 
																"<h3>Kapitel 2</h3>		<br>", 0, 0, null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void addChapter(){
		//TODO mit Inhalt füllen
		try {
			htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(),	"<h2>Kapitel 1</h2>		<br>" +
																"hier steht witziger erklärungstext vom ersten kapitel		<br>" +
//																"<img src='http://www.cms.hu-berlin.de/ueberblick/stellen/feed-icon16x16.png'> und unter umständen noch symbole" +
																"<h3>Kapitel 2</h3>		<br>" +
																"hier steht witziger erklärungstext vom zweiten kapitel		<br>" //+
															//	"<img src='./media/g4174.png'>"
																, 0, 0, null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
