package org.publicmain.gui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import org.resources.Help;

public class HistoryWindow extends JDialog{
		
	private static HistoryWindow me;

	private JPanel		searchTypePanel;
	private JLabel		searchTypeLabel;
	private JToggleButton user;
	private JToggleButton alias;
	private JToggleButton group;
	
	private JPanel 		searchUserPanel;
	private JLabel		selectUserLabel;
	private JComboBox<String> selectUserComboBox;
	private JLabel		beginLabel;
	private JTextField	beginDateTextField;
	private JButton		beginDateButton;
	
	
	public HistoryWindow() {
		this.me = this;
		this.setResizable(false);
		this.setLayout(new FlowLayout(FlowLayout.CENTER));
		

		
		this.setTitle("History");
		this.setModal(false);
		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		this.setIconImage(Help.getIcon("pM_Logo.png").getImage());
		this.getContentPane().setBackground(Color.WHITE);
		this.setMinimumSize(new Dimension(250, GUI.getGUI().getHeight()));
		this.setMaximumSize(new Dimension(250, GUI.getGUI().getHeight()));
		this.setPreferredSize(new Dimension(250, GUI.getGUI().getHeight()));
		this.pack();
		
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	
	public static void closeThis(){
		if(me!=null)me.dispose();
	}

	public static void showThis(){
		if(me==null) new HistoryWindow();
		me.setVisible(true);
	}
	
	
	class CardButtonController implements ActionListener{

		private JPanel ref;
		
		public CardButtonController(JPanel ref){
			this.ref = ref;
		}
		
		public void actionPerformed(ActionEvent e) {
			CardLayout card = (CardLayout) ref.getLayout();
			card.show( ref, ((JToggleButton)e.getSource()).getText() );
		}
	}
}

