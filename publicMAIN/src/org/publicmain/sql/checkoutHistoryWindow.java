package org.publicmain.sql;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.publicmain.gui.GUI;

/**
 * Diese Klasse stellt das AnzeigeFenster und den dazugehörogen Inhalt für die History bereit
 * Mithilfe dieses Fensters soll der nutzer seine History der lokalen DB durchsuchen können.
 * 
 * @author rpfaffner
 * 
 */
// TODO Auswahlmöglichkeiten für Datum(DropdownKalender implementierbar?)von-bis, Uhrzeit(Dropdown)von-bis, Gesprächspartner/-Gruppe Name, public/private
// TODO nicht ausgewählte Felder werden nicht beachtet!
// TODO anders Layout für Suchbereich nehmen (gridBagLayout) -> siehe Zeichnung!
// TODO Anzeigebereich bestenfalls HTML-Formatiert
public class checkoutHistoryWindow {
	private JFrame historyFrame;
	private JLabel aliasOrGroupName;
	private JTextField searchField;
	private JButton searchButton;
	private JPanel searchPanel;
	private JTextPane helpContentTxt;
	private HTMLEditorKit htmlKit;
	private HTMLDocument htmlDoc;

	public checkoutHistoryWindow() {
	
		this.historyFrame = new JFrame("checkout History");
		this.aliasOrGroupName = new JLabel("Alias or Groupname:");
		this.searchField = new JTextField("Type in the Alias or Groupname you are searching for.");
		this.searchButton = new JButton("Search");
		this.searchPanel = new JPanel();
		this.helpContentTxt = new JTextPane();
		this.htmlKit = new HTMLEditorKit();
		this.htmlDoc = new HTMLDocument();

		historyFrame.setLocationRelativeTo(null);
		// TODO das mit dem Logo... geht das noch schöner ohne ne Kopie vom .png in das Packet zu haun?
		historyFrame.setIconImage(new ImageIcon(GUI.getGUI().getClass().getResource("pM_Logo2.png")).getImage());
		historyFrame.setMinimumSize(new Dimension(250, 400));
		
		helpContentTxt.setBackground(new Color(229, 195, 0));
		helpContentTxt.setEditable(false);
		helpContentTxt.setEditorKit(htmlKit);
		helpContentTxt.setDocument(htmlDoc);

		searchPanel.setLayout(new BorderLayout());
		
		
		// hinzufügen
		historyFrame.add(searchPanel, BorderLayout.NORTH);
		searchPanel.add(aliasOrGroupName, BorderLayout.WEST);
		searchPanel.add(searchField, BorderLayout.CENTER);
		searchPanel.add(searchButton, BorderLayout.EAST);
		historyFrame.add(helpContentTxt, BorderLayout.CENTER);
		historyFrame.pack();
		historyFrame.setVisible(true);
	}
}
