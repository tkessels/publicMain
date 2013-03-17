package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Diese Klasse stellt den Dialog für Help/About pMAIN bereit
 * 
 * @author rpfaffner
 *
 */

public class HelpContents {

	JFrame 		hcframe;
	JTextField 	searchField;
	JButton 	SearchButton;
	JTextArea	helpContentTxt;
	
	// ToDo: Suche ermöglichen! Auf HTML umbauen! Icon hinzufügen!
	
	
	public HelpContents() {
		this.hcframe = new JFrame("Help Contents");
		this.searchField = new JTextField("What are you searchting for?");
		this.SearchButton = new JButton("Search");
		this.helpContentTxt = new JTextArea("Hier kommt der Help-Content rein - Am besten in HTML", 20, 5);
		
		
		hcframe.setLocationRelativeTo(null);
		
		searchField.setPreferredSize(new Dimension(500, 1));
		
		helpContentTxt.setBackground(Color.YELLOW);
		helpContentTxt.setEditable(false);
		
		// hinzufügen
		hcframe.add(searchField, BorderLayout.WEST);
		hcframe.add(SearchButton, BorderLayout.EAST);
		hcframe.add(helpContentTxt, BorderLayout.SOUTH);
		
		
		hcframe.pack();
		hcframe.setVisible(true);
	}
	
	
	
	
	
	
	

	
	
}
