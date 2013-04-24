package org.publicmain.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.publicmain.common.Config;
import org.resources.Help;

public class SettingsWindow extends JDialog{
	
	private static SettingsWindow me;
				
	private GraphicsEnvironment ge;
	private GraphicsDevice 		gd;
	private DisplayMode 		dm;
	
	private JLabel		banner;
	
	private JPanel 		userSettingsPanel;
	private JLabel 		aliasLabel;
	private JTextField 	aliasTextField;
	private JLabel 		fileTransferLabel;
	private JCheckBox 	fileTransferCheckBox;
	
	private JPanel 		trayIconNotificationPanel;
	private JLabel		grpMsgLabel;
	private JCheckBox	grpMsgCheckBox;
	private JLabel		privMsgLabel;
	private JCheckBox	privMsgCheckBox;
	
	private JPanel		backupServerPanel;
	private JLabel		ipBackupLabel;
	private	JTextField	ipBackupTextField;
	private JLabel		portBackupLabel;
	private JTextField	portBackupTextField;
	private JLabel		userBackupLabel;
	private JTextField	userBackupTextField;
	private JLabel		pwBackupLabel;
	private JPasswordField pwBackPasswordField;
	
	private JPanel		pushPullPanel;
	private JLabel		userPushPullLabel;
	private JTextField	userPushPullTextField;
	private JLabel		pwPushPullLabel;
	private JPasswordField	pwPushPullPasswordField;
	
	private JPanel		buttonPanel;
	private JButton		resetBtn;
	private JButton		acceptBtn;
	private JButton		cancelBtn;

	public SettingsWindow() {
		this.me = this;
		this.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		this.ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		this.gd = ge.getDefaultScreenDevice();
		this.dm = gd.getDisplayMode();
		
		this.banner					 = 	new JLabel(Help.getIcon("textlogo.png",210,50));
		
		this.userSettingsPanel		 = new JPanel(new GridLayout(2,2));
		this.aliasLabel				 = new JLabel("Alias");
		this.aliasTextField			 = new JTextField();
		this.fileTransferLabel		 = new JLabel("Deny file transfer");
		this.fileTransferCheckBox	 = new JCheckBox();
		
		this.trayIconNotificationPanel = new JPanel(new GridLayout(2,2));
		this.grpMsgLabel 			 = new JLabel("Group messages");
		this.grpMsgCheckBox			 = new JCheckBox();
		this.privMsgLabel			 = new JLabel("Private messages");
		this.privMsgCheckBox		 = new JCheckBox();
		
		this.backupServerPanel 		 = new JPanel(new GridLayout(4,2));
		this.ipBackupLabel 			 = new JLabel("IP address");
		this.ipBackupTextField 		 = new JTextField();
		this.portBackupLabel 		 = new JLabel("Port");
		this.portBackupTextField 	 = new JTextField();
		this.userBackupLabel 		 = new JLabel("Username");
		this.userBackupTextField 	 = new JTextField();
		this.pwBackupLabel 			 = new JLabel("Password");
		this.pwBackPasswordField 	 = new JPasswordField();
		
		this.pushPullPanel 			 = new JPanel(new GridLayout(2,2));
		this.userPushPullLabel 		 = new JLabel("Username");
		this.userPushPullTextField	 = new JTextField();
		this.pwPushPullLabel 		 = new JLabel("Password");
		this.pwPushPullPasswordField = new JPasswordField();
		
		this.buttonPanel 	= new JPanel(new GridLayout(1,3));
		this.resetBtn 		= new JButton("Reset");
		this.acceptBtn	 	= new JButton("Accept");
		this.cancelBtn 		= new JButton("Cancel");
		

		this.userSettingsPanel.setBorder(BorderFactory.createTitledBorder("User"));
		this.userSettingsPanel.setPreferredSize(new Dimension(230,62));
		this.userSettingsPanel.setBackground(Color.WHITE);
		this.fileTransferCheckBox.setBackground(Color.WHITE);
		this.userSettingsPanel.add(aliasLabel);
		this.userSettingsPanel.add(aliasTextField);
		this.userSettingsPanel.add(fileTransferLabel);
		this.userSettingsPanel.add(fileTransferCheckBox);
		
		this.trayIconNotificationPanel.setBorder(BorderFactory.createTitledBorder("Notification"));
		this.trayIconNotificationPanel.setPreferredSize(new Dimension(230,62));
		this.trayIconNotificationPanel.setBackground(Color.WHITE);
		this.grpMsgCheckBox.setBackground(Color.WHITE);
		this.privMsgCheckBox.setBackground(Color.WHITE);
		this.trayIconNotificationPanel.add(grpMsgLabel);
		this.trayIconNotificationPanel.add(grpMsgCheckBox);
		this.trayIconNotificationPanel.add(privMsgLabel);
		this.trayIconNotificationPanel.add(privMsgCheckBox);
		
		this.backupServerPanel.setBorder(BorderFactory.createTitledBorder("Backup-Server"));
		this.backupServerPanel.setPreferredSize(new Dimension(230,100));
		this.backupServerPanel.setBackground(Color.WHITE);
		this.backupServerPanel.add(ipBackupLabel);
		this.backupServerPanel.add(ipBackupTextField);
		this.backupServerPanel.add(portBackupLabel);
		this.backupServerPanel.add(portBackupTextField);
		this.backupServerPanel.add(userBackupLabel);
		this.backupServerPanel.add(userBackupTextField);
		this.backupServerPanel.add(pwBackupLabel);
		this.backupServerPanel.add(pwBackPasswordField);
		
		this.pushPullPanel.setBorder(BorderFactory.createTitledBorder("push/pull Backup"));
		this.pushPullPanel.setPreferredSize(new Dimension(230,62));
		this.pushPullPanel.setBackground(Color.WHITE);
		this.pushPullPanel.add(userPushPullLabel);
		this.pushPullPanel.add(userPushPullTextField);
		this.pushPullPanel.add(pwPushPullLabel);
		this.pushPullPanel.add(pwPushPullPasswordField);
		
		this.buttonPanel.setBorder(BorderFactory.createCompoundBorder());
		this.buttonPanel.setPreferredSize(new Dimension(230,25));
		this.buttonPanel.setBackground(Color.WHITE);
		this.buttonPanel.add(resetBtn);
		this.buttonPanel.add(acceptBtn);
		this.buttonPanel.add(cancelBtn);
		
		this.add(banner);
		this.add(userSettingsPanel);
		this.add(trayIconNotificationPanel);
		this.add(backupServerPanel);
		this.add(pushPullPanel);
		this.add(buttonPanel);
		
		this.setTitle("Settings");
		this.setModal(false);
		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		this.setIconImage(Help.getIcon("pM_Logo2.png").getImage());
		this.getContentPane().setBackground(Color.WHITE);
		this.setMinimumSize(new Dimension(250, GUI.getGUI().getHeight()));
		this.setMaximumSize(new Dimension(250, GUI.getGUI().getHeight()));
		this.setPreferredSize(new Dimension(250, GUI.getGUI().getHeight()));
		this.pack();
		
		this.getDefaults();
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
	
	private void getDefaults(){
		this.aliasTextField.setText(Config.getConfig().getAlias());
		this.fileTransferCheckBox.setSelected(Config.getConfig().getDisableFileTransfer());
		this.grpMsgCheckBox.setSelected(Config.getConfig().getNotifyGroup());
		this.privMsgCheckBox.setSelected(Config.getConfig().getNotifyPrivate());
		this.ipBackupTextField.setText(Config.getConfig().getBackupDBIP());
		this.portBackupTextField.setText(Config.getConfig().getBackupDBPort());
		this.userBackupTextField.setText(Config.getConfig().getBackupDBUser());
		this.pwBackPasswordField.setText(Config.getConfig().getBackupDBPw());
		this.userPushPullTextField.setText(Config.getConfig().getBackupDBChoosenUsername());
		this.pwPushPullPasswordField.setText(Config.getConfig().getBackupDBChoosenUserPassWord());
	}
	
	public static void closeThis(){
		if(me!=null)me.dispose();
	}

	public static void showthis(){
		if(me==null) new SettingsWindow();
		me.showIt();
	}
}
