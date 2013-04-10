package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JWindow;

public class registrationWindow {
	
	private static registrationWindow me;
	private JFrame registrationWindowFrame;
	private JLabel wellcomeLogo;
	private JLabel wellcomeLabel1;
	private JLabel wellcomeLabel2;
	private JLabel nickNameLabel;
	private JTextField nickNameTextField;
	private JLabel userNameLabel;
	private JTextField userNameTextField;
	private JLabel passWordLabel;
	private JPasswordField passWordTextField;
	private JLabel BackupserverIPLabel;
	private JTextField BackupserverIPTextField;
	
	private JTextField statusTextField;
	
	private JButton backButton;
	private JButton submitButton;
	
	private GridBagConstraints c;
	private Insets set;
	
	public registrationWindow(JFrame startWindowFrame) {
		this.registrationWindowFrame=	new JFrame();
		this.wellcomeLogo			= 	new JLabel(new ImageIcon(getClass().getResource("textlogo.png")));
		this.wellcomeLabel1			=	new JLabel("Please Enter your personal data and");
		this.wellcomeLabel2			=	new JLabel("push \"Submit & Login\"-Button");
		this.nickNameLabel			=	new JLabel("Nickname");
		this.nickNameTextField		=	new JTextField();
		this.userNameLabel			=	new JLabel("Username");
		this.userNameTextField 		=	new JTextField();
		this.passWordLabel			=	new JLabel("Password");
		this.passWordTextField		=	new JPasswordField();
		this.BackupserverIPLabel	=	new JLabel("IP of your Backupserver");
		this.BackupserverIPTextField=	new JTextField();
		
		this.statusTextField		=	new JTextField();
		
		this.backButton				=	new JButton("< Back");
		this.submitButton			=	new JButton("Submit & Login");
		
		this.submitButton.addActionListener(new registrationWindowButtonController(registrationWindowFrame, startWindowFrame));
		this.backButton.addActionListener(new registrationWindowButtonController(registrationWindowFrame, startWindowFrame));
		
		this.c 						= new GridBagConstraints();
		this.set 					= new Insets(5, 5, 5, 5);
		
		registrationWindowFrame.setTitle("Registration");
		registrationWindowFrame.setIconImage(new ImageIcon(getClass().getResource("pM_Logo2.png")).getImage());
		registrationWindowFrame.getContentPane().setBackground(Color.WHITE);
		registrationWindowFrame.setMinimumSize(new Dimension(200, 180));
		
		statusTextField.setBackground(new Color(229, 195, 0));
		statusTextField.setEditable(false);

		registrationWindowFrame.setLayout(new GridBagLayout());
		c.insets 	= set;
		c.fill 		= GridBagConstraints.HORIZONTAL;
		c.anchor	= GridBagConstraints.LINE_START;
		
		// hinzufügen der Komponenten zum startWindowFrame
		c.gridx 	= 0;
		c.gridy 	= 0;
		c.gridwidth = 2;
		registrationWindowFrame.add(wellcomeLogo ,c);
		
		c.gridy 	= 1;
		c.gridwidth = 2;
		registrationWindowFrame.add(wellcomeLabel1, c);
		
		c.gridy 	= 2;
		c.gridwidth = 1;
		registrationWindowFrame.add(nickNameLabel, c);
		
		c.gridx 	= 1;
		registrationWindowFrame.add(nickNameTextField, c);
		
		c.gridx 	= 0;
		c.gridy 	= 3;
		registrationWindowFrame.add(userNameLabel, c);
		
		c.gridx 	= 1;
		registrationWindowFrame.add(userNameTextField, c);
		
		c.gridx 	= 0;
		c.gridy 	= 5;
		registrationWindowFrame.add(passWordLabel, c);
		
		c.gridx 	= 1;
		registrationWindowFrame.add(passWordTextField, c);
		
		c.gridx 	= 0;
		c.gridy 	= 6;
		registrationWindowFrame.add(BackupserverIPLabel, c);
		
		c.gridx 	= 1;
		registrationWindowFrame.add(BackupserverIPTextField, c);
		
		c.gridx 	= 0;
		c.gridy 	= 7;
		c.gridwidth = 2;
		registrationWindowFrame.add(statusTextField, c);
		
		c.gridy 	= 8;
		c.gridwidth = 1;
		registrationWindowFrame.add(backButton, c);	
		
		c.gridx 	= 1;
		registrationWindowFrame.add(submitButton, c);
		
		registrationWindowFrame.pack();
		registrationWindowFrame.setResizable(false);
		registrationWindowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		registrationWindowFrame.setLocationRelativeTo(null);
		registrationWindowFrame.setVisible(true);
	}
	
	
	public static registrationWindow getRegistrationWindow(JFrame startWindowFrame){
		if (me == null) {
			me = new registrationWindow(startWindowFrame);
			return me;
		} else {
			me.registrationWindowFrame.setVisible(true);
			return me;
		}
	}
}
class registrationWindowButtonController implements ActionListener{
	private JFrame registrationWindowFrame;
	private JFrame startWindowFrame;
	public registrationWindowButtonController(JFrame registrationWindowFrame, JFrame startWindowFrame) {
		this.registrationWindowFrame =	registrationWindowFrame;
		this.startWindowFrame =	startWindowFrame;
	}

	public void actionPerformed(ActionEvent evt) {
		JButton source = (JButton)evt.getSource();
		switch(source.getText()){
		
		case "< Back":
			registrationWindowFrame.setVisible(false);
			startWindowFrame.setVisible(true);
			break;
		case "Submit & Login":
			registrationWindowFrame.setVisible(false);
			GUI.getGUI();
		break;
		}
	}
}

