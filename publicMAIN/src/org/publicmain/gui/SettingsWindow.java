package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.publicmain.sql.BackupDBConnection;
import org.resources.Help;

public class SettingsWindow extends JDialog{
	
	private static SettingsWindow me;
	
	private BackupDBConnection bdb;
	private JLabel wellcomeLogo;
	private JLabel wellcomeLabel1;
	private JLabel nickNameLabel;
	private JTextField nickNameTextField;
	private JLabel userNameLabel;
	private JTextField userNameTextField;
	private JLabel passWordLabel;
	private JPasswordField passWordTextField;
	private JLabel firstNameLabel;
	private JTextField firstNameTextField;
	private JLabel lastNameLabel;
	private JTextField lastNameTextField;
	private JLabel eMailLabel;
	private JTextField eMailTextField;
	private JLabel birthDayLabel;
	private JTextField birthDayTextField;
	private JLabel backupserverIPLabel;
	private JTextField backupserverIPTextField;
	
	private JTextField statusTextField;
	
	private JButton cancelButton;
	private JButton applyButton;
	private JPanel buttonPanel;
	
	private GraphicsEnvironment ge;
	private GraphicsDevice gd;
	private DisplayMode dm;
	
	private GridBagConstraints c;
	private Insets set;
	
	public SettingsWindow() {
		this.me = this;
		this.wellcomeLogo			= 	new JLabel(Help.getIcon("textlogo.png",200,45));
		this.wellcomeLabel1			=	new JLabel("Please Enter your personal data");
		this.nickNameLabel			=	new JLabel("Nickname");
		this.nickNameTextField		=	new JTextField();
		this.userNameLabel			=	new JLabel("Username");
		this.userNameTextField 		=	new JTextField();
		this.passWordLabel			=	new JLabel("Password");
		this.passWordTextField		=	new JPasswordField();
		this.firstNameLabel			=	new JLabel("First name");
		this.firstNameTextField 	=	new JTextField();
		this.lastNameLabel			=	new JLabel("Last name");
		this.lastNameTextField 		=	new JTextField();
		this.eMailLabel				=	new JLabel("eMail");
		this.eMailTextField 		=	new JTextField();
		this.birthDayLabel			=	new JLabel("Birthday");
		this.birthDayTextField 		=	new JTextField();
		this.backupserverIPLabel	=	new JLabel("Backupserver IP");
		this.backupserverIPTextField=	new JTextField();
		
		this.statusTextField		=	new JTextField();
		
		this.cancelButton			=	new JButton("Cancel");
		this.applyButton			=	new JButton("Apply");
		this.buttonPanel			=	new JPanel(new BorderLayout());
		
		this.buttonPanel.add(applyButton);
		this.buttonPanel.add(cancelButton, BorderLayout.EAST);
		this.buttonPanel.setBackground(Color.WHITE);
		
		this.applyButton.addActionListener(new SettingsWindowButtonController());
		this.cancelButton.addActionListener(new SettingsWindowButtonController());
		
		this.ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		this.gd = ge.getDefaultScreenDevice();
		this.dm = gd.getDisplayMode();
		
		this.c 						= new GridBagConstraints();
		this.set 					= new Insets(5, 5, 5, 5);
		
		this.statusTextField.setBackground(new Color(229, 195, 0));
		this.statusTextField.setEditable(false);

		this.setLayout(new GridBagLayout());
		c.insets 	= set;
		c.fill 		= GridBagConstraints.HORIZONTAL;
		c.anchor	= GridBagConstraints.LINE_START;
		
		// hinzufügen der Komponenten:
		c.gridx 	= 0;
		c.gridy 	= 0;
		c.gridwidth = 2;
		this.add(wellcomeLogo ,c);
		
		c.gridy 	= 1;
		c.gridwidth = 2;
		this.add(wellcomeLabel1, c);
		
		c.gridy 	= 2;
		c.gridwidth = 1;
		this.add(nickNameLabel, c);
		
		c.gridx 	= 1;
		this.add(nickNameTextField, c);
		
		c.gridx 	= 0;
		c.gridy 	= 3;
		this.add(userNameLabel, c);
		
		c.gridx 	= 1;
		this.add(userNameTextField, c);
		
		c.gridx 	= 0;
		c.gridy 	= 5;
		this.add(passWordLabel, c);
		
		c.gridx 	= 1;
		this.add(passWordTextField, c);
		
		c.gridx 	= 0;
		c.gridy 	= 6;
		this.add(firstNameLabel, c);
		
		c.gridx 	= 1;
		this.add(firstNameTextField, c);
		
		c.gridx 	= 0;
		c.gridy 	= 7;
		this.add(lastNameLabel, c);
		
		c.gridx 	= 1;
		this.add(lastNameTextField, c);
		
		c.gridx 	= 0;
		c.gridy 	= 8;
		this.add(eMailLabel, c);
		
		c.gridx 	= 1;
		this.add(eMailTextField, c);
		
		c.gridx 	= 0;
		c.gridy 	= 9;
		this.add(birthDayLabel, c);
		
		c.gridx 	= 1;
		this.add(birthDayTextField, c);
		
		
		
		c.gridx 	= 0;
		c.gridy 	= 10;
		this.add(backupserverIPLabel, c);
		
		c.gridx 	= 1;
		this.add(backupserverIPTextField, c);
		
		c.gridx 	= 0;
		c.gridy 	= 11;
		c.gridwidth = 2;
		this.add(statusTextField, c);
		
		c.gridy 	= 12;
		c.gridwidth = 1;
		c.gridx 	= 1;
		this.add(buttonPanel, c);	
		
		this.setTitle("Settings");
		this.setModal(false);
		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		this.setIconImage(Help.getIcon("pM_Logo2.png").getImage());
		this.getContentPane().setBackground(Color.WHITE);
		this.setMinimumSize(new Dimension(250, GUI.getGUI().getHeight()));
		this.setPreferredSize(new Dimension(250, GUI.getGUI().getHeight()));
		this.pack();
		
		this.showIt();
	}
	
	public void showIt() {
		if ((GUI.getGUI().getLocation().x + GUI.getGUI().getWidth() + this.getWidth() < dm.getWidth())) {
			this.setLocation(GUI.getGUI().getLocation().x + GUI.getGUI().getWidth(), GUI.getGUI().getLocation().y);
		} else {
			this.setLocationRelativeTo(null);
		}
		this.setVisible(true);
	}
	
	public static void closethis(){
		if(me!=null)me.dispose();
	}

	public static void showthis(){
		if(me==null) new SettingsWindow();
		me.showIt();
	}
	
/*	public static SettingsWindow getMe(){
		if(me==null) new SettingsWindow();
		return me;
	}*/

	class SettingsWindowButtonController implements ActionListener {

		public SettingsWindowButtonController() {
		}

		public void actionPerformed(ActionEvent evt) {
			bdb = BackupDBConnection.getBackupDBConnection();
			JButton source = (JButton) evt.getSource();
			switch (source.getText()) {

			case "Apply":
				//TODO Hier wie gewünscht das Übernehmen der Änderungen initiieren 
				break;
			case "Cancel":
				me.dispose();
				break;
			}
		}
	}
}
