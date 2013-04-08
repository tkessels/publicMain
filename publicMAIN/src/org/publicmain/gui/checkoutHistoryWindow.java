package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.publicmain.common.NachrichtenTyp;
import org.publicmain.sql.LocalDBConnection;


/**
 * Diese Klasse stellt das AnzeigeFenster und den dazugehörogen Inhalt für die History bereit
 * Mithilfe dieses Fensters soll der nutzer seine History der lokalen DB durchsuchen können.
 * 
 * @author rpfaffner
 * 
 */
// TODO Auswahlmöglichkeiten für Datum(DropdownKalender implementierbar?)von-bis, Uhrzeit(Dropdown)von-bis, Gesprächspartner/-Gruppe Name, public/private
// TODO nicht ausgewählte Felder werden nicht beachtet!
// TODO Anzeigebereich bestenfalls HTML-Formatiert
public class checkoutHistoryWindow {
	private LocalDBConnection db;
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
	private JLabel time;
	private JComboBox<SimpleDateFormat> timeFromSearchField ;
	private JLabel timeHyphen;
	private JComboBox<SimpleDateFormat> timeToSearchField;
	private JButton searchButton;
	private JTextPane historyContentTxt;
	private HTMLEditorKit htmlKit;
	private HTMLDocument htmlDoc;
	private GridBagConstraints c;
	private Insets set;
	private ArrayList<String> timeArray = new ArrayList<>();
	private SimpleDateFormat timeDateFormat;

	public checkoutHistoryWindow() {

		this.db 							= db.getDBConnection();
		this.historyFrame					= new JFrame("checkout History");
		this.searchPanel 					= new JPanel();
		this.msgTyp 						= new JLabel("Typ");
		this.msgTypCombo 					= new JComboBox<String>() ;
		this.aliasOrGroupName 				= new JLabel("Alias or Groupname:");
		this.aliasOrGroupNameSearchField	= new JTextField("...type in here.");
		this.date 							= new JLabel("Date");
		this.dateFromSearchField 			= new JTextField("01.01.1984");
		this.dateHyphen 					= new JLabel("-");
		this.dateToSearchField 				= new JTextField("01.01.2014");
		this.time							= new JLabel("Time");
		this.timeDateFormat					= new SimpleDateFormat("HH:mm");
		this.fillTimeArray();
		this.timeFromSearchField 			= new JComboBox(timeArray.toArray());
		this.timeHyphen						= new JLabel("-");
		this.timeToSearchField				= new JComboBox(timeArray.toArray());
		
		this.searchButton 					= new JButton("Search");
		this.c 								= new GridBagConstraints();
		this.set 							= new Insets(5, 5, 5, 5);
		
		this.historyContentTxt				= new JTextPane();
		this.htmlKit 						= new HTMLEditorKit();
		this.htmlDoc 						= new HTMLDocument();

		msgTypCombo.addItem("ALL");
		msgTypCombo.addItem(NachrichtenTyp.GROUP.toString());
		msgTypCombo.addItem(NachrichtenTyp.PRIVATE.toString());
		msgTypCombo.addItem(NachrichtenTyp.DATA.toString());
		msgTypCombo.addItem(NachrichtenTyp.SYSTEM.toString());
		
		searchButton.addActionListener(new searchContoller());
		
		historyFrame.setLocationRelativeTo(null);
		historyFrame.setIconImage(new ImageIcon(getClass().getResource("pM_Logo2.png")).getImage());
		historyFrame.setMinimumSize(new Dimension(200, 400));
		
		historyContentTxt.setBackground(new Color(229, 195, 0));
		historyContentTxt.setEditable(false);
		historyContentTxt.setEditorKit(htmlKit);
		historyContentTxt.setDocument(htmlDoc);

		searchPanel.setLayout(new GridBagLayout());
		c.insets 	= set;
		c.fill 		= GridBagConstraints.HORIZONTAL;
//		c.anchor	= GridBagConstraints.LINE_START;
		
		// hinzufügen der Komponenten zum searchpanel
		c.gridx 	= 0;
		c.gridy 	= 0;
		searchPanel.add(msgTyp ,c);
		
		c.gridx 	= 1;
		c.gridwidth = 3;
		searchPanel.add(msgTypCombo, c);
		
		c.gridx 	= 0;
		c.gridy 	= 1;
		c.gridwidth = 1;
		searchPanel.add(aliasOrGroupName, c);
		
		c.gridx 	= 1;
		c.gridwidth = 3;
		searchPanel.add(aliasOrGroupNameSearchField, c);
		
		c.gridy 	= 2;
		c.gridx 	= 0;
		c.gridwidth = 1;
		searchPanel.add(date, c);
		
		c.gridx 	= 1;
		searchPanel.add(dateFromSearchField, c);
		
		c.gridx		= 2;
		searchPanel.add(dateHyphen, c);
		
		c.gridx 	= 3;
		searchPanel.add(dateToSearchField, c);
		
		c.gridy 	= 3;
		c.gridx 	= 0;
		searchPanel.add(time, c);
		
		c.gridx 	= 1;
		searchPanel.add(timeFromSearchField, c);	
		
		c.gridx 	= 2;
		searchPanel.add(timeHyphen, c);
		
		c.gridx 	= 3;
		searchPanel.add(timeToSearchField, c);
		
		c.gridy 	= 4;
		c.gridx 	= 0;
		c.gridwidth = 4;
		searchPanel.add(searchButton, c);
		
		// hinzufügen der Komponenten zum historyFrame
		historyFrame.add(searchPanel, BorderLayout.NORTH);
		historyFrame.add(historyContentTxt, BorderLayout.CENTER);
		
		historyFrame.pack();
		historyFrame.setVisible(true);
	}
	private void fillTimeArray (){
		// timeArray befüllen
		String myTime = "00:00";
		try {
			do{
				timeArray.add(myTime);
				Date d = timeDateFormat.parse(myTime); 
				Calendar cal = Calendar.getInstance();
				cal.setTime(d);
				cal.add(Calendar.MINUTE, 30);
				myTime = timeDateFormat.format(cal.getTime());
			} while (!myTime.equals("00:00"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	class searchContoller implements ActionListener{
		private String chosenNTyp;
		private String chosenAliasOrGrpName;
		private Date chosenFromDateTime;
		private Date ChosenToDateTime;
		private SimpleDateFormat splDateForm;
		
		public void actionPerformed(ActionEvent arg0) {
			this.splDateForm = new SimpleDateFormat("dd.MM.yyy HH:mm:ss");
			
			try {
				chosenNTyp = msgTypCombo.getItemAt(msgTypCombo.getSelectedIndex());
				chosenAliasOrGrpName = aliasOrGroupNameSearchField.getText();
				chosenFromDateTime = splDateForm.parse(dateFromSearchField.getText() + " " + (timeFromSearchField.getItemAt(timeFromSearchField.getSelectedIndex())) + ":00");
				ChosenToDateTime = splDateForm.parse(dateToSearchField.getText() + " " + (timeToSearchField.getItemAt(timeToSearchField.getSelectedIndex())) + ":00");
				
				db.searchInHistory(chosenNTyp, chosenAliasOrGrpName, chosenFromDateTime, ChosenToDateTime , htmlKit, htmlDoc);
				
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			
			
			
		}
		
	}
}

