package org.publicmain.gui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.Config;
import org.publicmain.sql.DatabaseEngine;
import org.resources.Help;

public class SettingsWindow extends JDialog{
	private static final long serialVersionUID = 7576764930617798651L;

	private static SettingsWindow me;

	private JLabel		banner;

	private JPanel		cardButtonsPanel;
	private JToggleButton userBtn;
	private JToggleButton databaseBtn;
	private JToggleButton pushPullBtn;
	private ButtonGroup btnGrp;
	private JPanel		cardsPanel;
	private JPanel		cardUser;
	private JPanel		cardDB;
	private JPanel		cardPushPull;
	private CardLayout     cardsPanelLayout;

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

	private JPanel				fontChooserPanel;
	private JLabel				fontChooserLabel;
	private GraphicsEnvironment ge; 
	private List<String> 		fontNames;
	private JComboBox<String>	fontChooserComboBox;
	private JLabel				fontSizeLabel;
	private JSlider				fontSizeSlider;

	private JPanel		localDBPanel;
	private JLabel		portLocalDBLabel;
	private JTextField	portLocalDBTextField;
	private JLabel		userLocalDBLabel;
	private JTextField	userLocalDBTextField;
	private	JLabel		pwLocalDBLabel;
	private JPasswordField pwLocalDBPasswordField;

	private JPanel		pushPullPanel;
	private JLabel		userPushPullLabel;
	private JTextField	userPushPullTextField;
	private JLabel		pwPushPullLabel;
	private JPasswordField pwPushPullPasswordField;
	private JLabel		createPushPullLabel;
	private JButton		createPushPullBtn;
	private JLabel		deletePushPullLabel;
	private JButton		deletePushPullBtn;

	private JPanel		backupDBPanel;
	private JLabel		ipBackupLabel;
	private	JTextField	ipBackupTextField;
	private JLabel		portBackupLabel;
	private JTextField	portBackupTextField;
	private JLabel		userBackupLabel;
	private JTextField	userBackupTextField;
	private JLabel		pwBackupLabel;
	private JPasswordField pwBackPasswordField;

	private JPanel		buttonPanel;
	private JButton		resetBtn;
	private JButton		acceptBtn;
	private JButton		cancelBtn;


	public SettingsWindow(int card, boolean modal) {
		SettingsWindow.me = this;
		constructWindowContent();
		this.setModal(modal);
		showTab(card);
		this.setVisible(true);
	}

	private SettingsWindow() {
		SettingsWindow.me = this;
		constructWindowContent();
		this.setVisible(true);
	}

	public void showTab(int i) {
		String[] cards= {"User","DB","Push/Pull"};
		i=Math.abs(i)%cards.length;
		cardsPanelLayout.show(cardsPanel, cards[i]);
		userBtn.setSelected(i==0);
		databaseBtn.setSelected(i==1);
		pushPullBtn.setSelected(i==2);
		cardsPanel.requestFocus();

	}


	/**
	 * erstellt alle Fenster elemente
	 */
	private void constructWindowContent() {
		this.setResizable(false);
		this.setLayout(new FlowLayout(FlowLayout.CENTER));


		this.banner					 = new JLabel(Help.getIcon("textlogo.png",210,50));

		this.cardButtonsPanel		 = new JPanel(new GridLayout(1,3));
		this.userBtn				 = new JToggleButton("User", Help.getIcon("userSettingsSym.png",10,16), true);
		this.userBtn.setActionCommand("User");
		this.databaseBtn			 = new JToggleButton("DB", Help.getIcon("dbSettingsSym.png",12,16), false);
		this.databaseBtn.setActionCommand("DB");
		this.pushPullBtn			 = new JToggleButton("Push/Pull", false);
		this.pushPullBtn.setActionCommand("Push/Pull");
		this.btnGrp					 = new ButtonGroup();
		this.cardsPanelLayout 		 = new CardLayout();
		this.cardsPanel				 = new JPanel(cardsPanelLayout);
		this.cardUser				 = new JPanel(new FlowLayout(FlowLayout.CENTER));
		this.cardDB					 = new JPanel(new FlowLayout(FlowLayout.CENTER));
		this.cardPushPull			 = new JPanel(new FlowLayout(FlowLayout.CENTER));

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

		this.ge  = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fontNames = ge.getAvailableFontFamilyNames();
		Arrays.sort(fontNames);
		this.fontNames 				= Arrays.asList(fontNames);
		this.fontChooserPanel		= new JPanel(new GridLayout(2, 2));
		this.fontChooserLabel		= new JLabel("Font family");
		this.fontChooserComboBox	= new JComboBox<String>(fontNames);
		this.fontSizeSlider	 		= new JSlider(0, 20, 3);
		this.fontSizeLabel			= new JLabel("Font size               " + fontSizeSlider.getValue());

		this.localDBPanel			 = new JPanel(new GridLayout(3,2));
		this.portLocalDBLabel		 = new JLabel("Port");
		this.portLocalDBTextField	 = new JTextField();
		this.userLocalDBLabel		 = new JLabel("Username");
		this.userLocalDBTextField	 = new JTextField();
		this.pwLocalDBLabel			 = new JLabel("Password");
		this.pwLocalDBPasswordField	 = new JPasswordField();

		this.pushPullPanel 			 = new JPanel(new GridLayout(4,2));
		this.userPushPullLabel 		 = new JLabel("Username");
		this.userPushPullTextField	 = new JTextField();
		this.pwPushPullLabel 		 = new JLabel("Password");
		this.pwPushPullPasswordField = new JPasswordField();
		this.createPushPullLabel	 = new JLabel("Generate account");
		this.createPushPullBtn		 = new JButton("Create");
		this.deletePushPullLabel	 = new JLabel("Delete account");
		this.deletePushPullBtn		 = new JButton("Delete");

		this.backupDBPanel 		 = new JPanel(new GridLayout(4,2));
		this.ipBackupLabel 		 = new JLabel("IP address");
		this.ipBackupTextField 	 = new JTextField();
		this.portBackupLabel 		 = new JLabel("Port");
		this.portBackupTextField 	 = new JTextField();
		this.userBackupLabel 		 = new JLabel("Username");
		this.userBackupTextField 	 = new JTextField();
		this.pwBackupLabel 			 = new JLabel("Password");
		this.pwBackPasswordField 	 = new JPasswordField();

		this.buttonPanel 	= new JPanel(new GridLayout(1,3));
		this.resetBtn 		= new JButton("Reset");
		this.acceptBtn	 	= new JButton("Accept");
		this.cancelBtn 		= new JButton("Cancel");


		this.btnGrp.add(userBtn);
		this.btnGrp.add(databaseBtn);
		this.btnGrp.add(pushPullBtn);

		this.userBtn.addActionListener(new CardButtonController());
		this.databaseBtn.addActionListener(new CardButtonController());
		this.pushPullBtn.addActionListener(new CardButtonController());
		this.resetBtn.addActionListener(new SettingButtonController());
		this.acceptBtn.addActionListener(new SettingButtonController());
		this.cancelBtn.addActionListener(new SettingButtonController());
		this.fontSizeSlider.addChangeListener(new FontSizeSliderController());
		this.createPushPullBtn.addActionListener(new PushPullButtonController());
		this.deletePushPullBtn.addActionListener(new DeleteButtonController());

		this.cardButtonsPanel.setPreferredSize(new Dimension(230,25));
		this.cardButtonsPanel.setBackground(Color.WHITE);
		this.cardButtonsPanel.add(userBtn);
		this.cardButtonsPanel.add(databaseBtn);
		this.cardButtonsPanel.add(pushPullBtn);

		this.cardUser.setPreferredSize(new Dimension(230,62));
		this.cardUser.setBackground(Color.WHITE);
		this.cardUser.add(userSettingsPanel);
		this.cardUser.add(trayIconNotificationPanel);
		this.cardUser.add(fontChooserPanel);

		this.cardDB.setPreferredSize(new Dimension(230,62));
		this.cardDB.setBackground(Color.WHITE);
		this.cardDB.add(localDBPanel);
		this.cardDB.add(backupDBPanel);

		this.cardPushPull.setPreferredSize(new Dimension(230,62));
		this.cardPushPull.setBackground(Color.WHITE);
		this.cardPushPull.add(pushPullPanel);

		this.userSettingsPanel.setPreferredSize(new Dimension(230,62));
		this.userSettingsPanel.setBorder(BorderFactory.createTitledBorder("User"));
		this.userSettingsPanel.setBackground(Color.WHITE);
		this.fileTransferCheckBox.setBackground(Color.WHITE);
		this.userSettingsPanel.add(aliasLabel);
		this.userSettingsPanel.add(aliasTextField);
		this.userSettingsPanel.add(fileTransferLabel);
		this.userSettingsPanel.add(fileTransferCheckBox);

		this.trayIconNotificationPanel.setPreferredSize(new Dimension(230,62));
		this.trayIconNotificationPanel.setBorder(BorderFactory.createTitledBorder("Notification"));
		this.trayIconNotificationPanel.setBackground(Color.WHITE);
		this.grpMsgCheckBox.setBackground(Color.WHITE);
		this.privMsgCheckBox.setBackground(Color.WHITE);
		this.trayIconNotificationPanel.add(grpMsgLabel);
		this.trayIconNotificationPanel.add(grpMsgCheckBox);
		this.trayIconNotificationPanel.add(privMsgLabel);
		this.trayIconNotificationPanel.add(privMsgCheckBox);

		this.fontChooserPanel.setPreferredSize(new Dimension(230, 62));
		this.fontChooserPanel.setBorder(BorderFactory.createTitledBorder("Font settings"));
		this.fontChooserPanel.setBackground(Color.WHITE);
		this.fontChooserComboBox.setBackground(Color.WHITE);
		this.fontSizeSlider.setBackground(Color.WHITE);
		this.fontChooserPanel.add(fontChooserLabel);
		this.fontChooserPanel.add(fontChooserComboBox);
		this.fontChooserPanel.add(fontSizeLabel);
		this.fontChooserPanel.add(fontSizeSlider);

		this.localDBPanel.setPreferredSize(new Dimension(230,82));
		this.localDBPanel.setBorder(BorderFactory.createTitledBorder("local database"));
		this.localDBPanel.setBackground(Color.WHITE);
		this.localDBPanel.add(portLocalDBLabel);
		this.localDBPanel.add(portLocalDBTextField);
		this.localDBPanel.add(userLocalDBLabel);
		this.localDBPanel.add(userLocalDBTextField);
		this.localDBPanel.add(pwLocalDBLabel);
		this.localDBPanel.add(pwLocalDBPasswordField);

		this.pushPullPanel.setBorder(BorderFactory.createTitledBorder("push/pull to backup DB"));
		this.pushPullPanel.setPreferredSize(new Dimension(230,106));
		this.pushPullPanel.setBackground(Color.WHITE);
		this.pushPullPanel.add(userPushPullLabel);
		this.pushPullPanel.add(userPushPullTextField);
		this.pushPullPanel.add(pwPushPullLabel);
		this.pushPullPanel.add(pwPushPullPasswordField);
		this.pushPullPanel.add(createPushPullLabel);
		this.pushPullPanel.add(createPushPullBtn);
		this.pushPullPanel.add(deletePushPullLabel);
		this.pushPullPanel.add(deletePushPullBtn);

		this.backupDBPanel.setBorder(BorderFactory.createTitledBorder("backup database"));
		this.backupDBPanel.setPreferredSize(new Dimension(230,100));
		this.backupDBPanel.setBackground(Color.WHITE);
		this.backupDBPanel.add(ipBackupLabel);
		this.backupDBPanel.add(ipBackupTextField);
		this.backupDBPanel.add(portBackupLabel);
		this.backupDBPanel.add(portBackupTextField);
		this.backupDBPanel.add(userBackupLabel);
		this.backupDBPanel.add(userBackupTextField);
		this.backupDBPanel.add(pwBackupLabel);
		this.backupDBPanel.add(pwBackPasswordField);

		this.cardsPanel.setPreferredSize(new Dimension(230, 200));
		this.cardsPanel.setBackground(Color.WHITE);
		this.cardsPanel.add(cardUser, "User");
		this.cardsPanel.add(cardDB, "DB");
		this.cardsPanel.add(cardPushPull, "Push/Pull");

		this.buttonPanel.setPreferredSize(new Dimension(230,25));
		this.buttonPanel.setBackground(Color.WHITE);
		this.buttonPanel.add(resetBtn);
		this.buttonPanel.add(acceptBtn);
		this.buttonPanel.add(cancelBtn);

		this.add(banner);
		this.add(cardButtonsPanel);
		this.add(cardsPanel);
		this.add(buttonPanel);

		this.setTitle("Settings");
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setIconImage(Help.getIcon("pM_Logo.png").getImage());
		this.getContentPane().setBackground(Color.WHITE);

		this.setMinimumSize(new Dimension(250, 360));
		this.setMaximumSize(new Dimension(250, 360));
		this.setPreferredSize(new Dimension(250, 360));
		this.pack();

		this.getDefaults();
		this.setLocationRelativeTo(null);

	}

	public static void closeThis(){
		if(me!=null) {
			me.dispose();
		}
		me = null;
	}

	public synchronized static SettingsWindow get(){
		if(me==null) {
			new SettingsWindow();
		}
		return me;

	}

	private void getDefaults(){
		this.aliasTextField.setText(Config.getConfig().getAlias());
		this.fileTransferCheckBox.setSelected(Config.getConfig().getDisableFileTransfer());

		this.grpMsgCheckBox.setSelected(Config.getConfig().getNotifyGroup());
		this.privMsgCheckBox.setSelected(Config.getConfig().getNotifyPrivate());

		this.fontChooserComboBox.setSelectedItem(Config.getConfig().getFontFamily());
		this.fontSizeSlider.setValue(Config.getConfig().getFontSize());

		this.userPushPullTextField.setText(Config.getConfig().getBackupDBChoosenUsername());
		this.pwPushPullPasswordField.setText(Config.getConfig().getBackupDBChoosenUserPassWord());
		if (userPushPullTextField.getText().equals("")) {
			this.deletePushPullBtn.setEnabled(false);
		}
		this.portLocalDBTextField.setText(Config.getConfig().getLocalDBPort());
		this.userLocalDBTextField.setText(Config.getConfig().getLocalDBUser());
		this.pwLocalDBPasswordField.setText(Config.getConfig().getLocalDBPw());

		this.ipBackupTextField.setText(Config.getConfig().getBackupDBIP());
		this.portBackupTextField.setText(Config.getConfig().getBackupDBPort());
		this.userBackupTextField.setText(Config.getConfig().getBackupDBUser());
		this.pwBackPasswordField.setText(Config.getConfig().getBackupDBPw());
		
                checkSettings();
		
		
	}
	
	private void checkSettings() {
		new Thread(new Runnable() {
			public void run() {
				int i = DatabaseEngine.getDatabaseEngine().getStatusBackup();
				localDBPanel.setBackground((DatabaseEngine.getDatabaseEngine().getStatusLocal())?Color.WHITE:Color.ORANGE);
				pushPullPanel.setBackground((i == 1) ? Color.ORANGE : Color.WHITE);
				backupDBPanel.setBackground((i == 0) ? Color.ORANGE : Color.WHITE);
				localDBPanel.repaint();
				pushPullPanel.repaint();
				backupDBPanel.repaint();
			}
		}).start();
	}

	private void acceptSettings_old(){
		boolean changes = false;

		if(!aliasTextField.getText().equals(Config.getConfig().getAlias())){
			GUI.getGUI().changeAlias(aliasTextField.getText());
			//hier wird bewusst changes nicht auf true gesetz, da die Methode changeAlias()
			//das schreiben der config übernimmt.
		}
		if(Config.getConfig().getDisableFileTransfer() != fileTransferCheckBox.isSelected()){
			Config.getConfig().setDisableFileTransfer(fileTransferCheckBox.isSelected());
			changes = true;
		}

		if(Config.getConfig().getNotifyGroup() != grpMsgCheckBox.isSelected()){
			Config.getConfig().setNotifyGroup(grpMsgCheckBox.isSelected());
			changes = true;
		}
		if(Config.getConfig().getNotifyPrivate() != privMsgCheckBox.isSelected()){
			Config.getConfig().setNotifyPrivate(privMsgCheckBox.isSelected());
			changes = true;
		}

		if(!fontChooserComboBox.getSelectedItem().equals(Config.getConfig().getFontFamily())){
			Config.getConfig().setFontFamily((String)fontChooserComboBox.getSelectedItem());
			changes = true;
		}
		if(fontSizeSlider.getValue() != Config.getConfig().getFontSize()){
			Config.getConfig().setFontSize(fontSizeSlider.getValue());
			changes = true;
		}

		if (!userPushPullTextField.getText().equals("") || (Config.getConfig().getBackupDBChoosenUsername() != null))
		{
			String tmpUserPushPull = userPushPullTextField.getText();
			if (!tmpUserPushPull.equals(Config.getConfig().getBackupDBChoosenUsername())) {
				Config.getConfig().setBackupDBChoosenUsername(userPushPullTextField.getText());
				changes = true;
			}
		}
		if((pwPushPullPasswordField.getPassword().length != 0) || (Config.getConfig().getBackupDBChoosenUserPassWord() != null))
		{
			String tmp_password = new String(pwPushPullPasswordField.getPassword());
			if(!tmp_password.equals(Config.getConfig().getBackupDBChoosenUserPassWord())) {
				Config.getConfig().setBackupDBChoosenUserPassWord(tmp_password);
//				deletePushPullBtn.setEnabled(true);
				changes = true;
			}
		}

		if (portLocalDBTextField.getText().length()>1)
		{
			String tmp_port = new String(portLocalDBTextField.getText());
			if (!tmp_port.equals(Config.getConfig().getLocalDBPort())) {
				Config.getConfig().setLocalDBPort(portLocalDBTextField.getText());
				changes = true;
			}
		}
		if (userLocalDBTextField.getText().length()>1)
		{
			String tmp_user = new String(userLocalDBTextField.getText());
			if (!tmp_user.equals(Config.getConfig().getLocalDBUser())) {
				Config.getConfig().setLocalDBUser(userLocalDBTextField.getText());
				changes = true;
			}
		}
		if(pwLocalDBPasswordField.getPassword().length>1)
		{
			String tmp_password = new String(pwLocalDBPasswordField.getPassword());
			if(!tmp_password.equals(Config.getConfig().getLocalDBPw())) {
				Config.getConfig().setLocalDBPw(tmp_password);
				changes = true;
			}
		}

		if (!ipBackupTextField.getText().equals(""))
		{
			String tmpBackupIp = ipBackupTextField.getText();
			if (!tmpBackupIp.equals(Config.getConfig().getBackupDBIP())) {
				Config.getConfig().setBackupDBIP(ipBackupTextField.getText());
				changes = true;
			}
		}
		if (portBackupTextField.getText().length()>1)
		{
			String tmp_port = new String(portBackupTextField.getText());
			if (!tmp_port.equals(Config.getConfig().getBackupDBPort())){
				Config.getConfig().setBackupDBPort(
						portBackupTextField.getText());
				changes = true;
			}
		}
		if (userBackupTextField.getText().length()>1)
		{
			String tmp_user = new String(userBackupTextField.getText());
			if (!tmp_user.equals(Config.getConfig().getBackupDBUser())) {
				Config.getConfig().setBackupDBUser(userBackupTextField.getText());
				changes = true;
			}
		}
		if(pwBackPasswordField.getPassword().length>1)
		{
			String tmp_password = new String(pwBackPasswordField.getPassword());
			if(!tmp_password.equals(Config.getConfig().getBackupDBPw())) {
				Config.getConfig().setBackupDBPw(tmp_password);
				changes = true;
			}
		}


		if(changes){
			Config.write();
		}
	}
	
	private void acceptSettings(){
			Config.getConfig().setDisableFileTransfer(fileTransferCheckBox.isSelected());
			Config.getConfig().setNotifyGroup(grpMsgCheckBox.isSelected());
			Config.getConfig().setNotifyPrivate(privMsgCheckBox.isSelected());
			Config.getConfig().setFontFamily((String)fontChooserComboBox.getSelectedItem());
			Config.getConfig().setFontSize(fontSizeSlider.getValue());
			
		String pushpulluser = userPushPullTextField.getText().trim();
		if (!pushpulluser.equals(Config.getConfig().getBackupDBChoosenUsername()))
		{
			Config.getConfig().setBackupDBChoosenUsername(pushpulluser);
		}
		
		String pushpullpass = pwPushPullPasswordField.getText().trim();
		if(!pushpullpass.equals(Config.getConfig().getBackupDBChoosenUserPassWord()))
		{
			Config.getConfig().setBackupDBChoosenUserPassWord(pushpullpass);
		}
		
		String localdbport = portLocalDBTextField.getText().trim();
		if (!localdbport.equals(Config.getConfig().getLocalDBPort())) {
			Config.getConfig().setLocalDBPort(localdbport);
		}
		
		String localdbuser = userLocalDBTextField.getText().trim();
		if (!localdbuser.equals(Config.getConfig().getLocalDBUser())) {
			Config.getConfig().setLocalDBUser(localdbuser);
		}
		
		String localdbpass = pwLocalDBPasswordField.getText().trim();
		if(!localdbpass.equals(Config.getConfig().getLocalDBPw())) {
			Config.getConfig().setLocalDBPw(localdbpass);
		}
		
		String backupdbip = ipBackupTextField.getText().trim();
		if (!backupdbip.equals(Config.getConfig().getBackupDBIP())) {
			Config.getConfig().setBackupDBIP(backupdbip);
		}
		
		String backupdbport = portBackupTextField.getText().trim();
		if (!backupdbport.equals(Config.getConfig().getBackupDBPort())){
				Config.getConfig().setBackupDBPort(backupdbport);
		}
		
		String backupdbuser = userBackupTextField.getText().trim();
		if (!backupdbuser.equals(Config.getConfig().getBackupDBUser())) {
			Config.getConfig().setBackupDBUser(backupdbuser);
		}
		
		String backupdbpass = pwBackPasswordField.getText().trim();
		if(!backupdbpass.equals(Config.getConfig().getBackupDBPw())) {
			Config.getConfig().setBackupDBPw(backupdbpass);
		}

		if(ChatEngine.getCE()!=null)GUI.getGUI().changeAlias(aliasTextField.getText().trim());
		else Config.write();
	}

	
	
	class CardButtonController implements ActionListener{
		public void actionPerformed(final ActionEvent e) {
			cardsPanelLayout.show(cardsPanel, e.getActionCommand());
		}
	}

	class SettingButtonController implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			JButton source = (JButton)e.getSource();

			switch(source.getText()){
			case "Reset" :
				getDefaults();
				break;
			case "Accept" :
				acceptSettings();
				dispose();
				//				closeThis();
				break;
			case "Cancel" :
				dispose();
				//				closeThis();
				break;
			}
		}

	}
	class FontSizeSliderController implements ChangeListener{

		/* (non-Javadoc)
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		public void stateChanged(ChangeEvent e) {
			fontSizeLabel.setText("Font size               " + fontSizeSlider.getValue());
		}

	}

	class PushPullButtonController implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			JButton source = (JButton)e.getSource();

			switch(source.getText()){
			case "Create" :
				String username=userPushPullTextField.getText();
				String password=pwPushPullPasswordField.getText();
				int res = DatabaseEngine.getDatabaseEngine().createUser(username, password);{
					switch(res){
					case 3:
						userPushPullTextField.setBackground(Color.green);
						pwPushPullPasswordField.setBackground(Color.green);
						Config.write();
						DatabaseEngine.getDatabaseEngine().push();
						//alles Sahne
						break;

					case 2:
						userPushPullTextField.setBackground(Color.RED);
						pwPushPullPasswordField.setBackground(Color.RED);
						JOptionPane.showMessageDialog(me,"Username or Password doesn´t match requirements!","BackupServer",JOptionPane.INFORMATION_MESSAGE);
						//Nutzername und Pwd entspricht nicht den anforderungen
						break;	 
					case 1:
						//user gab es bereits
						JOptionPane.showMessageDialog(me,"Username already exists!","BackupServer",JOptionPane.INFORMATION_MESSAGE);

						break;
					case 0:
						//Backupserver nicht erreichbar Datanbank fehlt
						me.showTab(1);
						me.backupDBPanel.setBackground(Color.orange);
						JOptionPane.showMessageDialog(me,"Unable to connect to Backupserver!\n Please check your settings regarding the backupserver.","BackupServer",JOptionPane.ERROR_MESSAGE);

						break;
					}
				}
				break;
			}
		}

	}

	class DeleteButtonController implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			JButton source = (JButton)e.getSource();

			switch(source.getText()){
			case "Delete" :
				userPushPullTextField.getText();
				pwPushPullPasswordField.getText();
				int res = DatabaseEngine.getDatabaseEngine().deleteBackupUserAccount();{

					switch(res){
					case 2:
						userPushPullTextField.setText("");
						pwPushPullPasswordField.setText("");
						userPushPullTextField.setBackground(Color.WHITE);
						pwPushPullPasswordField.setBackground(Color.WHITE);
						Config.getConfig().clearBackupDBChoosenUser();		//TODO: einkommentieren sobald methode da!
						deletePushPullBtn.setEnabled(false);
						JOptionPane.showMessageDialog(me,"User deleted!","BackupServer",JOptionPane.INFORMATION_MESSAGE);
						//alles Sahne
						break;
					case 1:
						//fehler beim Löschen des Nutzers
						JOptionPane.showMessageDialog(me,"Error while deleating choosen User!","BackupServer",JOptionPane.INFORMATION_MESSAGE);

						break;
					case 0:
						//Backupserver nicht erreichbar Datanbank fehlt
						me.showTab(1);
						me.backupDBPanel.setBackground(Color.orange);
						JOptionPane.showMessageDialog(me,"Unable to connect to Backupserver!\n Please check your settings regarding the backupserver.","BackupServer",JOptionPane.ERROR_MESSAGE);

						break;
					}
				}
				break;
			}
		}
	}
}
