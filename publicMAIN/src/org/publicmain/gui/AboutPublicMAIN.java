/**
 * 
 */
package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;

/**
 * Diese Klasse stellt den Dialog für Help/About pMAIN bereit
 * 
 * @author Alfred
 *
 */
public class AboutPublicMAIN extends JDialog {

	private JTextArea aboutPMAINtextArea;
	
	
	/**
	 * Konstruktor
	 * @param owner
	 * @param title
	 * @param modal
	 */
	public AboutPublicMAIN(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
		
		aboutPMAINtextArea = new JTextArea("pMAIN:" + "\n\n" +
				"Public Messaging Appliance of Independent Nodes" + "\n\n" +
				"(c) Copyright pMAIN.  All rights reserved." + "\n\n" + "Visit: http://www.publicmain.de");
		aboutPMAINtextArea.setEditable(false);
		aboutPMAINtextArea.setBackground(Color.BLACK);
		aboutPMAINtextArea.setForeground(Color.WHITE);
		this.add(new JLabel(new ImageIcon("media/Mainbluepersp.png")), BorderLayout.WEST);
		this.add(aboutPMAINtextArea, BorderLayout.CENTER);
		this.getContentPane().setBackground(new Color(255, 255, 255, 0));
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		
	}
	
}
