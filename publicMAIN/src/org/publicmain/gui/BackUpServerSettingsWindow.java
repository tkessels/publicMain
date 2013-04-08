package org.publicmain.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JTextField;

import org.publicmain.sql.BackupDBConnection;

public class BackUpServerSettingsWindow {
	
	private BackupDBConnection bdb;
	private JFrame backUpServerSettingsFrame;
	private JLabel serverIPLabel;
	private JTextField serverIPTextField;
//	private JLabel serverPortLabel;						// brauchma nicht
//	private JTextField serverPortTextField;				// brauchma nicht
	private JLabel userNameLabel;
	private JTextField userNameTextField;
	private JLabel passWordLabel;
	private JTextField passWordTextField;
	
	private JTextField statusTextField;
	
	private JButton connectToButton;
	private JButton createThisUserButton;
	
	private GridBagConstraints c;
	private Insets set;

	public BackUpServerSettingsWindow() {

		this.backUpServerSettingsFrame 		= new JFrame("Backupserver Settings");
		this.serverIPLabel					= new JLabel("Backupserver IP");
		this.serverIPTextField				= new JTextField("typ in the Backupserver IP here");
		this.userNameLabel					= new JLabel("Username");
		this.userNameTextField				= new JTextField("typ in the your Username here");
		this.passWordLabel					= new JLabel("Password");
		this.passWordTextField				= new JTextField("typ in the your Password here");
		
		this.statusTextField				= new JTextField();
		
		this.connectToButton				= new JButton("Apply and connect to Backup Server");
		this.createThisUserButton			= new JButton("create this User");
		
		this.connectToButton.addActionListener(new backUpServerSettingsWindowButtonController());
		
		this.c 								= new GridBagConstraints();
		this.set 							= new Insets(5, 5, 5, 5);
		
		
		backUpServerSettingsFrame.setLocationRelativeTo(null);
		backUpServerSettingsFrame.setIconImage(new ImageIcon(getClass().getResource("pM_Logo2.png")).getImage());
		backUpServerSettingsFrame.setMinimumSize(new Dimension(200, 180));
		
		statusTextField.setBackground(new Color(229, 195, 0));
		statusTextField.setEditable(false);

		backUpServerSettingsFrame.setLayout(new GridBagLayout());
		c.insets 	= set;
		c.fill 		= GridBagConstraints.HORIZONTAL;
		c.anchor	= GridBagConstraints.LINE_START;
		
		// hinzufügen der Komponenten zum searchpanel
		c.gridx 	= 0;
		c.gridy 	= 0;
		backUpServerSettingsFrame.add(serverIPLabel ,c);
		
		c.gridx 	= 1;
		c.gridy 	= 0;
		backUpServerSettingsFrame.add(serverIPTextField, c);
		
		c.gridx 	= 0;
		c.gridy 	= 1;
		backUpServerSettingsFrame.add(userNameLabel, c);
		
		c.gridx 	= 1;
		backUpServerSettingsFrame.add(userNameTextField, c);
		
		c.gridx 	= 0;
		c.gridy 	= 2;
		backUpServerSettingsFrame.add(passWordLabel, c);
		
		c.gridx 	= 1;
		backUpServerSettingsFrame.add(passWordTextField, c);
		
		c.gridx		= 0;
		c.gridy 	= 3;
		c.gridwidth = 2;
		backUpServerSettingsFrame.add(statusTextField, c);
		
		c.gridy 	= 4;
		c.gridwidth = 1;
		backUpServerSettingsFrame.add(connectToButton, c);
		
		c.gridx 	= 1;
		backUpServerSettingsFrame.add(createThisUserButton, c);
		
		backUpServerSettingsFrame.pack();
		backUpServerSettingsFrame.setVisible(true);
	}
	
	class backUpServerSettingsWindowButtonController implements ActionListener{
		
		public void actionPerformed(ActionEvent e) {
			bdb = BackupDBConnection.getBackupDBConnection();
			JButton source = (JButton)e.getSource();
			
			switch(source.getText()){
			
			case "Apply and connect to Backup Server":
				break;
			case "create this User":
				bdb.createNewUser(statusTextField, serverIPTextField, userNameTextField, passWordTextField);
				break;
			}
		}
		
	}
	
}
