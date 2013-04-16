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

public class startWindow extends JFrame implements ActionListener{

	private startWindow instanz;
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
	private JLabel backupserverIPLabel;
	private JTextField backupserverIPTextField;
	
	private GridBagConstraints c;
	private Insets set;
	private boolean pushedGo;	//TODO: Diese Variante zu prüfen ob startWindow "fertig" ist gefällt mir (noch) nicht -> aber gerade keine andere idee ist ja schon spät :-)
	
	private startWindow() {
		
		instanz =this;
		this.welcomeLogo			= 	new JLabel(new ImageIcon(getClass().getResource("textlogo.png")));
		this.welcomeLabel1			=	new JLabel("Enter your Nick an push \"GO\" if you just want to chat.");
		this.nickNameLabel			=	new JLabel("Nickname");
		this.nickNameTextField		=	new JTextField();

		this.goButton				=	new JButton("GO");
		this.pullButton				=	new JButton("PULL from Backup");

		this.c 						= 	new GridBagConstraints();
		this.set 					=	new Insets(5, 5, 5, 5);
		
		this.pushedGo				=   false;	//TODO: Diese Variante zu prüfen ob startWindow "fertig" ist gefällt mir (noch) nicht -> aber gerade keine andere idee ist ja schon spät :-)

		this.goButton.addActionListener(this);
		this.pullButton.addActionListener(this);
		
		this.setTitle("Welcome!");
		this.setIconImage(new ImageIcon(getClass().getResource("pM_Logo2.png")).getImage());
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public static boolean getStartWindow(){
		startWindow me = new startWindow();
		boolean x = me.bla();
		me.dispose();
		return x;

		
	}

	public boolean isGoPushed(){
		return pushedGo;
	}
//TODO: noch komplett umbauen da jetzt ?Elementklasse? und somit zugriff auf variablen - > vorher seperate Klasse
@Override
	public void actionPerformed(ActionEvent evt) {
		synchronized (instanz) {
			//TODO: evtl. boolean setezen dessen status in ner methode aus der Starterklasse abgerufen werden kann.
			
			
			
			JButton sourceButton = (JButton)evt.getSource();
			switch(sourceButton.getText()){
			case "GO":
				//TODO: Daten überprüfen, in .cfg setzen
				this.setVisible(false);
				pushedGo = true;//TODO: Diese Variante gefällt mir (noch) nicht -> aber keina andere idee
				synchronized (instanz) {
					//TODO: evtl. boolean setezen dessen status in ner methode aus der Starterklasse abgerufen werden kann.
					this.notifyAll();
				}
				break;
				
			case "PULL from Backup":
				wellcomeLabel2			=	new JLabel("For using backupserver enter Username, Password");
				wellcomeLabel3			=	new JLabel("and the IP of your backupserver");
				userNameLabel			=	new JLabel("Username");
				userNameTextField 		=	new JTextField();
				passWordLabel			=	new JLabel("Password");
				passWordTextField		=	new JPasswordField();
				statusTextField			=	new JTextField();
				backupserverIPLabel		=	new JLabel("Backupserver IP");
				backupserverIPTextField=	new JTextField();
				
				statusTextField.setBackground(new Color(229, 195, 0));
				statusTextField.setEditable(false);
				sourceButton.setText("PULL from Backup & GO");
				
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
				
				c.gridx 	= 0;
				c.gridy 	= 8;
				this.add(backupserverIPLabel, c);
				
				c.gridx 	= 1;
				this.add(backupserverIPTextField, c);
				
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
			break;
			
			case "PULL from Backup & GO":						//Vorsicht...ist ein bissl undurchsichtig! Ist der selbe Button wie "oben" nur wurde der Text
				System.out.println("wenn das klappt geht´s");	//umbenannt und neu positioniert. Daher trifft ein anderer case im (selben) Buttoncontroller zu.
				// Hier noch viel sinnvolles implementieren!!!
				this.notifyAll();
			break;
		}	
	}
}
}