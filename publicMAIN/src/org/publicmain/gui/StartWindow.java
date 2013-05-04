package org.publicmain.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.publicmain.common.Config;
import org.publicmain.common.LogEngine;
import org.publicmain.sql.DatabaseEngine;
import org.resources.Help;

public class StartWindow extends JFrame implements ActionListener{

	private StartWindow instanz;
	private JLabel welcomeLogo;
	private JLabel welcomeLabel1;

	private JLabel nickNameLabel;
	private JTextField nickNameTextField;
	private JButton goButton;
	private JButton pullButton;

	private JLabel wellcomeLabel2;
	private JLabel wellcomeLabel3;
	private JLabel userNameLabel;
	private JTextField userNameTextField;
	private JLabel passWordLabel;
	private JPasswordField passWordTextField;
	private JTextField statusTextField;
//	private JLabel backupserverIPLabel;
//	private JTextField backupserverIPTextField;
	private MouseListener txtFieldML;

	private GridBagConstraints c;
	private Insets set;

	private boolean plsRunGUI;

	private StartWindow() {

		instanz =this;
		this.welcomeLogo				= 	new JLabel(Help.getIcon("textlogo.png",307,78));
		this.welcomeLabel1				=	new JLabel("Enter your Nick and push \"GO\" if you just want to chat.");
		this.nickNameLabel				=	new JLabel("Nickname");
		this.nickNameTextField			=	new JTextField(System.getProperty("user.name"));

		this.goButton					=	new JButton("GO");
		this.pullButton					=	new JButton("PULL from Backup");

		this.c 							= 	new GridBagConstraints();
		this.set 						=	new Insets(5, 5, 5, 5);

		this.plsRunGUI					=	false;

		//Die, die dann noch dazu kommen wenn man "Pull from Backup" clickt
		this.wellcomeLabel2				=	new JLabel("For using backupserver enter Username, Password");
		this.wellcomeLabel3				=	new JLabel("and the IP of your backupserver");
		this.userNameLabel				=	new JLabel("Username");
		this.userNameTextField 			=	new JTextField();
		this.passWordLabel				=	new JLabel("Password");
		this.	passWordTextField		=	new JPasswordField();
		this.	statusTextField			=	new JTextField();
//		this.	backupserverIPLabel		=	new JLabel("Backupserver IP");
//		this.	backupserverIPTextField	=	new JTextField();
		this.txtFieldML = new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				nickNameTextField.setForeground(Color.BLACK);
				nickNameTextField.setText("");
			}
		};
		this.goButton.addActionListener(this);
		this.goButton.setActionCommand("GO");
		this.pullButton.addActionListener(this);
		this.pullButton.setActionCommand("PULL from Backup");
		this.nickNameTextField.addActionListener(this);
		this.nickNameTextField.setActionCommand("GO");
		this.nickNameTextField.addMouseListener(txtFieldML);
		this.userNameTextField.addMouseListener(txtFieldML);
//		this.backupserverIPTextField.addMouseListener(txtFieldML);


		this.setTitle("Welcome!");
		this.setIconImage(Help.getIcon("pM_Logo.png").getImage());
		this.getContentPane().setBackground(Color.WHITE);
		this.setMinimumSize(new Dimension(200, 180));

		this.setLayout(new GridBagLayout());
		c.insets 	= set;
		c.fill 		= GridBagConstraints.HORIZONTAL;
		c.anchor	= GridBagConstraints.LINE_START;

		// hinzufügen der Komponenten zum this
		c.gridx 	= 0;
		c.gridy 	= 0;
		c.gridwidth = 2;
		this.add(welcomeLogo ,c);

		c.gridy 	= 1;
		this.add(welcomeLabel1, c);

		c.gridy 	= 2;
		c.gridwidth = 1;
		this.add(nickNameLabel, c);

		c.gridx 	= 1;
		this.add(nickNameTextField, c);

		c.gridx 	= 0;
		c.gridy 	= 3;
		this.add(goButton, c);

		c.gridx 	= 1;
		this.add(pullButton, c);

		this.pack();
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	private synchronized boolean bla(){
		try {
			this.wait();
		} catch (InterruptedException e) {
			LogEngine.log(this, "Fehler in der syncorinized Methode" + e.getMessage(), LogEngine.ERROR);
		}
		//CONFIG VALID?
		return Config.getConfig().isvalid();
	}

	public static boolean getStartWindow(){
		StartWindow me = new StartWindow();
		boolean x = me.bla();
		me.dispose();
		return x;
	}

	private void changeStructure(JButton sourceButton){
		statusTextField.setBackground(new Color(229, 195, 0));
		statusTextField.setEditable(false);
		statusTextField.setText("Checking  Backupserver availability...");
		sourceButton.setText("PULL from Backup & GO");
		sourceButton.setActionCommand("PULL from Backup & GO");

		c.gridx 	= 0;
		c.gridy 	= 3;
		c.gridwidth = 2;
		this.add(goButton, c);

		c.gridy 	= 4;
		c.gridx 	= 0;
		c.gridwidth = 2;
		this.add(wellcomeLabel2, c);

		c.gridy 	= 5;
		this.add(wellcomeLabel3, c);

		c.gridx 	= 0;
		c.gridy 	= 6;
		c.gridwidth = 1;
		this.add(userNameLabel, c);

		c.gridx 	= 1;
		this.add(userNameTextField, c);

		c.gridx 	= 0;
		c.gridy 	= 7;
		this.add(passWordLabel, c);

		c.gridx 	= 1;
		this.add(passWordTextField, c);
		
//		c.gridx 	= 0;
//		c.gridy 	= 8;
//		this.add(backupserverIPLabel, c);
		
//		c.gridx 	= 1;
//		this.add(backupserverIPTextField, c);
		
		c.gridx 	= 0;
		c.gridy 	= 9;
		c.gridwidth = 2;
		this.add(statusTextField, c);

		c.gridx 	= 0;
		c.gridy 	= 10;
		c.gridwidth = 2;
		this.add(sourceButton, c);
		sourceButton.setVisible(true);

		this.pack();
		new Thread(new Runnable() {
			public void run() {
				if (DatabaseEngine.getDatabaseEngine().getStatusBackup() >= 1){
					statusTextField.setText("Backupserver available");
					statusTextField.setBackground(Color.GREEN);
				} else {
					statusTextField.setText("Backupserver not available");
					statusTextField.setBackground(Color.RED);
				}
			}
		}).start();
	}

	public void actionPerformed(ActionEvent evt) {
		synchronized (instanz) {
			long userID = (long) (Math.random() * Long.MAX_VALUE);
			String choosenAlias = nickNameTextField.getText();
			String choosenBackupDBUserName = userNameTextField.getText().trim();
			String choosenBackupDBPassword = passWordTextField.getText().trim();
			
//			String choosenBackupDBIP		= backupserverIPTextField.getText();
			
			Pattern nickNamePattern = Pattern.compile(Config.getConfig().getNamePattern());
			Matcher nickNameMatcher = nickNamePattern.matcher(choosenAlias);

			Pattern choosenBackupDBUserNamePattern = Pattern.compile(Config.getConfig().getNamePattern());
			choosenBackupDBUserNamePattern.matcher(choosenBackupDBUserName);

//			Pattern choosenBackupDBIPPattern = Pattern.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");
//			Matcher choosenBackupDBIPMatcher = choosenBackupDBIPPattern.matcher(choosenBackupDBIP);
			

			switch(evt.getActionCommand()){
			case "GO":
				if (choosenAlias.equals("") || !nickNameMatcher.find() || (choosenAlias.length() > Config.getConfig().getMaxAliasLength())){
					nickNameTextField.setForeground(Color.RED);
					nickNameTextField.setText("Not allowed characters or nick to long!");
				} else {
					Config.getConfig().setUserID(userID);
					Config.getConfig().setAlias(choosenAlias);
					Config.write();
					plsRunGUI = true;
					synchronized (instanz) {
						this.notifyAll();
					}
					this.setVisible(false);
				}
				break;

			case "PULL from Backup":
				JButton sourceButton = (JButton)evt.getSource();
				changeStructure(sourceButton);
				break;

			case "PULL from Backup & GO":						
				System.out.println(DatabaseEngine.getDatabaseEngine().getStatusBackup());
				if (DatabaseEngine.getDatabaseEngine().getStatusBackup()>=1) {
					int config_result = DatabaseEngine.getDatabaseEngine().getConfig(choosenBackupDBUserName, choosenBackupDBPassword);
					if (config_result == 2) {
						if (Config.getConfig().isvalid()) {
							plsRunGUI = true;
							synchronized (instanz) {
								this.notifyAll();
							}
							this.setVisible(false);
						} else {
							// Fehler beim laden der config keine gültige USER ID gefunden
							statusTextField.setText("Error while loading Settings couldn´t find USER ID");
							statusTextField.setBackground(new Color(229, 195, 0));
							System.out.println("Fehler beim laden der config keine gültige USER ID gefunden");
						}
					} else if (config_result == 1) {
						// Fehler beim pullen der config (null) returned
						statusTextField.setText("Error: No Settings saved.");
						statusTextField.setBackground(new Color(229, 195, 0));
						System.out.println("Fehler beim pullen der config (null) returned");
					} else if (config_result == 0) {
						// Angegebener nutzer exisitert nicht oder password falsch
						statusTextField.setText("Error: User doesn´t exists or UN or PW wrong.");
						statusTextField.setBackground(new Color(229, 195, 0));
						System.out.println("Angegebener nutzer exisitert nicht oder password falsch");
					}
				} else {
					//Database not there check settings
					System.out.println("Database not there check settings");
					statusTextField.setText("Backupserver not available");
					statusTextField.setBackground(Color.RED);
					new SettingsWindow(1, true);
				}


				break;
			}	
		}
	}
}
