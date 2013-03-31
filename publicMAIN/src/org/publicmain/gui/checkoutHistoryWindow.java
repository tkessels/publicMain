package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.publicmain.common.NachrichtenTyp;


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
	private JPanel searchPanel;
	private JLabel msgTyp;
	private JComboBox<String> msgTypCombo;
	private JLabel aliasOrGroupName;
	private JTextField aliasOrGroupNameSearchField;
	private JLabel date;
	private JTextField dateFromSearchField;
	private JLabel dateHyphen;
	private JTextField dateToSearchField;
	private JButton searchButton;
	private JTextPane historyContentTxt;
	private HTMLEditorKit htmlKit;
	private HTMLDocument htmlDoc;
	private GridBagConstraints c;
	private Insets set;

	public checkoutHistoryWindow() {
	
		this.historyFrame = new JFrame("checkout History");
		this.searchPanel = new JPanel();
		this.msgTyp = new JLabel("Typ");
		this.msgTypCombo = new JComboBox<String>();
		this.aliasOrGroupName = new JLabel("Alias or Groupname:");
		this.aliasOrGroupNameSearchField = new JTextField("...type in here.");
		this.date = new JLabel("Date");
		this.dateFromSearchField = new JTextField("xx.xx.xxxx");
		this.dateHyphen = new JLabel("-");
		this.dateToSearchField = new JTextField("xx.xx.xxxx");
		this.searchButton = new JButton("Search");
		this.c = new GridBagConstraints();
		this.set = new Insets(5, 5, 5, 5);
		
		this.historyContentTxt = new JTextPane();
		this.htmlKit = new HTMLEditorKit();
		this.htmlDoc = new HTMLDocument();

		this.msgTypCombo.addItem(NachrichtenTyp.PRIVATE.toString());
		this.msgTypCombo.addItem(NachrichtenTyp.GROUP.toString());
		this.msgTypCombo.addItem(NachrichtenTyp.DATA.toString());
		this.msgTypCombo.addItem(NachrichtenTyp.SYSTEM.toString());
		
		historyFrame.setLocationRelativeTo(null);
		historyFrame.setIconImage(new ImageIcon(getClass().getResource("pM_Logo2.png")).getImage());
		historyFrame.setMinimumSize(new Dimension(250, 400));
		
		historyContentTxt.setBackground(new Color(229, 195, 0));
		historyContentTxt.setEditable(false);
		historyContentTxt.setEditorKit(htmlKit);
		historyContentTxt.setDocument(htmlDoc);

		searchPanel.setLayout(new GridBagLayout());
		c.insets = set;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.BOTH;
		// hinzufügen der Komponenten zum searchpanel
		c.gridx = 0;
		c.gridy = 0;
		searchPanel.add(msgTyp ,c);
		
		c.gridx = 1;
		c.gridwidth = 3;
		searchPanel.add(msgTypCombo, c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		searchPanel.add(aliasOrGroupName, c);
		
		c.gridx = 1;
		c.gridwidth = 3;
		searchPanel.add(aliasOrGroupNameSearchField, c);
		
		c.gridy = 2;
		c.gridx = 0;
		c.gridwidth = 1;
		searchPanel.add(date, c);
		
		c.gridx = 1;
		searchPanel.add(dateFromSearchField, c);
		
		c.gridx = 2;
		searchPanel.add(dateHyphen, c);
		
		c.gridx = 3;
		searchPanel.add(dateToSearchField, c);
		
		c.gridy = 3;
		c.gridx = 0;
		c.gridwidth = 4;
		searchPanel.add(searchButton, c);
		
		// hinzufügen der Komponenten zum historyFrame
		historyFrame.add(searchPanel, BorderLayout.NORTH);
		historyFrame.add(historyContentTxt, BorderLayout.CENTER);
		
		historyFrame.pack();
		historyFrame.setVisible(true);
	}
}
