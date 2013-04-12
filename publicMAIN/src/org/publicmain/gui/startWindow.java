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

public class startWindow {

	private static startWindow me;
	private JFrame startWindowFrame;
	private JLabel welcomeLogo;
	private JLabel welcomeLabel1;

	private JLabel nickNameLabel;
	private JTextField nickNameTextField;
	private JButton goButton;
	private JButton pullButton;
	
	private GridBagConstraints c;
	private Insets set;
	private boolean pushedGo;	//TODO: Diese Variante zu prüfen ob startWindow "fertig" ist gefällt mir (noch) nicht -> aber gerade keine andere idee ist ja schon spät :-)
	
	private startWindow() {
		
		this.startWindowFrame		=	new JFrame();
		this.welcomeLogo			= 	new JLabel(new ImageIcon(getClass().getResource("textlogo.png")));
		this.welcomeLabel1			=	new JLabel("Enter your Nick an push \"GO\" if you just want to chat.");
		this.nickNameLabel			=	new JLabel("Nickname");
		this.nickNameTextField		=	new JTextField();

		this.goButton				=	new JButton("GO");
		this.pullButton				=	new JButton("PULL from Backup");

		this.c 						= 	new GridBagConstraints();
		this.set 					=	new Insets(5, 5, 5, 5);
		
		this.pushedGo				=   false;	//TODO: Diese Variante zu prüfen ob startWindow "fertig" ist gefällt mir (noch) nicht -> aber gerade keine andere idee ist ja schon spät :-)
		
		this.goButton.addActionListener(new startWindowButtonController(startWindowFrame, c, goButton));
		this.pullButton.addActionListener(new startWindowButtonController(startWindowFrame, c, goButton));
		
		startWindowFrame.setTitle("Welcome!");
		startWindowFrame.setIconImage(new ImageIcon(getClass().getResource("pM_Logo2.png")).getImage());
		startWindowFrame.getContentPane().setBackground(Color.WHITE);
		startWindowFrame.setMinimumSize(new Dimension(200, 180));
		
		startWindowFrame.setLayout(new GridBagLayout());
		c.insets 	= set;
		c.fill 		= GridBagConstraints.HORIZONTAL;
		c.anchor	= GridBagConstraints.LINE_START;
		
		// hinzufügen der Komponenten zum startWindowFrame
		c.gridx 	= 0;
		c.gridy 	= 0;
		c.gridwidth = 2;
		startWindowFrame.add(welcomeLogo ,c);
		
		c.gridy 	= 1;
		startWindowFrame.add(welcomeLabel1, c);
		
		c.gridy 	= 2;
		c.gridwidth = 1;
		startWindowFrame.add(nickNameLabel, c);
		
		c.gridx 	= 1;
		startWindowFrame.add(nickNameTextField, c);
		
		c.gridx 	= 0;
		c.gridy 	= 3;
		startWindowFrame.add(goButton, c);
		
		c.gridx 	= 1;
		startWindowFrame.add(pullButton, c);
		
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
	public boolean isGoPushed(){
		return pushedGo;
	}
//TODO: noch komplett umbauen da jetzt ?Elementklasse? und somit zugriff auf variablen - > vorher seperate Klasse
	class startWindowButtonController implements ActionListener{
		private JFrame startWindowFrame;
		private JLabel wellcomeLabel2;
		private JLabel wellcomeLabel3;
		private JLabel userNameLabel;
		private JTextField userNameTextField;
		private JLabel passWordLabel;
		private JPasswordField passWordTextField;
		private JTextField statusTextField;
		private JLabel backupserverIPLabel;
		private JTextField backupserverIPTextField;
		private GridBagConstraints c;
		
		private JButton goButton;
		
		public startWindowButtonController(JFrame startWindowFrame, GridBagConstraints c, JButton goButton) {
			this.startWindowFrame = startWindowFrame;
			this.goButton = goButton;
			this.c = c;
		}
	
		public void actionPerformed(ActionEvent evt) {
			
			JButton sourceButton = (JButton)evt.getSource();
			switch(sourceButton.getText()){
			
			case "GO":
				//TODO: Daten überprüfen, in .cfg setzen
				startWindowFrame.setVisible(false);
				pushedGo = true;//TODO: Diese Variante gefällt mir (noch) nicht -> aber keina andere idee
				//TODO: evtl. boolean setezen dessen status in ner methode aus der Starterklasse abgerufen werden kann.
				break;
			case "PULL from Backup":
				this.wellcomeLabel2			=	new JLabel("For using backupserver enter Username, Password");
				this.wellcomeLabel3			=	new JLabel("and the IP of your backupserver");
				this.userNameLabel			=	new JLabel("Username");
				this.userNameTextField 		=	new JTextField();
				this.passWordLabel			=	new JLabel("Password");
				this.passWordTextField		=	new JPasswordField();
				this.statusTextField		=	new JTextField();
				this.backupserverIPLabel	=	new JLabel("Backupserver IP");
				this.backupserverIPTextField=	new JTextField();
				
				statusTextField.setBackground(new Color(229, 195, 0));
				statusTextField.setEditable(false);
				sourceButton.setText("PULL from Backup & GO");
				
				c.gridx 	= 0;
				c.gridy 	= 3;
				c.gridwidth = 2;
				startWindowFrame.add(goButton, c);
				
				c.gridy 	= 4;
				c.gridx 	= 0;
				c.gridwidth = 2;
				startWindowFrame.add(wellcomeLabel2, c);
				
				c.gridy 	= 5;
				startWindowFrame.add(wellcomeLabel3, c);
				
				c.gridx 	= 0;
				c.gridy 	= 6;
				c.gridwidth = 1;
				startWindowFrame.add(userNameLabel, c);
				
				c.gridx 	= 1;
				startWindowFrame.add(userNameTextField, c);
				
				c.gridx 	= 0;
				c.gridy 	= 7;
				startWindowFrame.add(passWordLabel, c);
				
				c.gridx 	= 1;
				startWindowFrame.add(passWordTextField, c);
				
				c.gridx 	= 0;
				c.gridy 	= 8;
				startWindowFrame.add(backupserverIPLabel, c);
				
				c.gridx 	= 1;
				startWindowFrame.add(backupserverIPTextField, c);
				
				c.gridx 	= 0;
				c.gridy 	= 9;
				c.gridwidth = 2;
				startWindowFrame.add(statusTextField, c);
				
				c.gridx 	= 0;
				c.gridy 	= 10;
				c.gridwidth = 2;
				startWindowFrame.add(sourceButton, c);
				sourceButton.setVisible(true);
				
				startWindowFrame.pack();
			break;
			
			case "PULL from Backup & GO":						//Vorsicht...ist ein bissl undurchsichtig! Ist der selbe Button wie "oben" nur wurde der Text
				System.out.println("wenn das klappt geht´s");	//umbenannt und neu positioniert. Daher trifft ein anderer case im (selben) Buttoncontroller zu.
				// Hier noch viel sinnvolles implementieren!!!
				break;
			}
		}
	}
}