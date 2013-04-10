package org.publicmain.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.publicmain.gui.BackUpServerSettingsWindow.backUpServerSettingsWindowButtonController;
import org.publicmain.sql.BackupDBConnection;

public class startWindow {

	private static startWindow me;
	private JFrame startWindowFrame;
	private JLabel wellcomeLogo;
	private JLabel wellcomeLabel;
	private JLabel wellcomeLabel2;
	private JLabel wellcomeLabel3;
	private JLabel userNameLabel;
	private JTextField userNameTextField;
	private JLabel passWordLabel;
	private JPasswordField passWordTextField;
	
	private JTextField statusTextField;
	
	private JButton loginButton;
	private JButton registerButton;
	private boolean registerButtonPushed;
	
	private GridBagConstraints c;
	private Insets set;
	
	private startWindow() {
		
		this.startWindowFrame		=	new JFrame();
		this.wellcomeLogo			= 	new JLabel(new ImageIcon(getClass().getResource("textlogo.png")));
		this.wellcomeLabel			=	new JLabel("Wellcome!");
		this.wellcomeLabel2			=	new JLabel("Please Enter your Username and Password");
		this.wellcomeLabel3			=	new JLabel("If you are new, press \"Register\"-Button");
		this.userNameLabel			=	new JLabel("Username");
		this.userNameTextField 		=	new JTextField();
		this.passWordLabel			=	new JLabel("Password");
		this.passWordTextField		=	new JPasswordField();
		
		this.statusTextField		=	new JTextField();
		
		this.loginButton			=	new JButton("Login");
		this.registerButton			=	new JButton("Register");
		this.registerButtonPushed 	= 	false;
		
		this.loginButton.addActionListener(new startWindowButtonController(startWindowFrame));
		this.registerButton.addActionListener(new startWindowButtonController(registerButtonPushed));

		this.c 						= new GridBagConstraints();
		this.set 					= new Insets(5, 5, 5, 5);
		
		startWindowFrame.setIconImage(new ImageIcon(getClass().getResource("pM_Logo2.png")).getImage());
		startWindowFrame.getContentPane().setBackground(Color.WHITE);
		startWindowFrame.setMinimumSize(new Dimension(200, 180));
		
		statusTextField.setBackground(new Color(229, 195, 0));
		statusTextField.setEditable(false);

		startWindowFrame.setLayout(new GridBagLayout());
		c.insets 	= set;
		c.fill 		= GridBagConstraints.HORIZONTAL;
		c.anchor	= GridBagConstraints.LINE_START;
		
		// hinzufügen der Komponenten zum startWindowFrame
		c.gridx 	= 0;
		c.gridy 	= 0;
		c.gridwidth = 2;
		startWindowFrame.add(wellcomeLogo ,c);
		
		c.gridy 	= 1;
		c.gridwidth = 2;
		startWindowFrame.add(wellcomeLabel, c);
		
		c.gridy 	= 2;
		c.gridwidth = 2;
		startWindowFrame.add(wellcomeLabel2, c);
		
		c.gridy 	= 3;
		c.gridwidth = 2;
		startWindowFrame.add(wellcomeLabel3, c);
		
		c.gridy 	= 4;
		c.gridwidth = 1;
		startWindowFrame.add(userNameLabel, c);
		
		c.gridx 	= 1;
		startWindowFrame.add(userNameTextField, c);
		
		c.gridx 	= 0;
		c.gridy 	= 5;
		startWindowFrame.add(passWordLabel, c);
		
		c.gridx 	= 1;
		startWindowFrame.add(passWordTextField, c);
		
		c.gridx 	= 0;
		c.gridy 	= 6;
		c.gridwidth = 2;
		startWindowFrame.add(statusTextField, c);
		
		c.gridy 	= 7;
		c.gridwidth = 1;
		startWindowFrame.add(loginButton, c);
		
		c.gridx 	= 1;
		startWindowFrame.add(registerButton, c);
		
		startWindowFrame.pack();
		startWindowFrame.setResizable(false);
		startWindowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		startWindowFrame.setLocationRelativeTo(null);
		startWindowFrame.setVisible(true);
	}

	public static startWindow getStartWindow(){
		if (me == null) {
			me = new startWindow();
		}
		return me;
	}
}

class startWindowButtonController implements ActionListener{
	JFrame startWindowFrame;
	boolean registerButtonPushed;
	public startWindowButtonController() {

	}
	public startWindowButtonController(JFrame startWindowFrame) {
		this.startWindowFrame = startWindowFrame;
	}
	public startWindowButtonController(boolean registerButtonPushed) {
		this.registerButtonPushed = registerButtonPushed;
	}

	public void actionPerformed(ActionEvent evt) {
		
		JButton source = (JButton)evt.getSource();
		switch(source.getText()){
		
		case "Login":
			startWindowFrame.setVisible(false);
			GUI.getGUI();
			
			//TODO:
			break;
		case "Register":
			if (registerButtonPushed){
				registerButtonPushed = false;
			} else if (!registerButtonPushed){
				registrationWindow.getRegistrationWindow();
				registerButtonPushed = true;
			}
			break;
		}
	}
	
}