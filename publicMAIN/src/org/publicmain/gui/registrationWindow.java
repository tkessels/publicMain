package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JWindow;

public class registrationWindow {
	
	private static registrationWindow me;
	private JWindow registrationJWin;
	private JInternalFrame internalFrame;
	private JButton submitButton;
	
	
	public registrationWindow() {
		this.registrationJWin = new JWindow();
		this.internalFrame = new JInternalFrame("contacts");
		
		registrationJWin.add(internalFrame);
		
		
		
		registrationJWin.setSize(400, 400);
		
		registrationJWin.pack();
		//TODO: Position setzen
		registrationJWin.setVisible(true);
	}
	
	
	public static registrationWindow getRegistrationWindow(){
		if (me == null) {
			me = new registrationWindow();
		}
		return me;
	}
}
