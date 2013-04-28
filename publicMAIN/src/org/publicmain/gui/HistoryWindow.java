package org.publicmain.gui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeListener;

import org.resources.Help;

public class HistoryWindow extends JDialog{
		
	private static HistoryWindow me;

	private JPanel		searchTypePanel;
	private JLabel		searchTypeLabel;
	private JToggleButton userToggleButton;
	private JToggleButton aliasToggleButton;
	private JToggleButton groupToggleButton;
	private ButtonGroup	btnGrp;
	private JPanel		cardsPanel;
	private	JPanel		cardUserSearchPanel;
	private JPanel		cardAliasSearchPanel;
	private JPanel		cardGroupSearchPanel;
	
	private JPanel 		userSearchPanel;
	private JLabel		userSelectLabel;
	private JComboBox<String> userSelectComboBox;
	private JPanel		userBeginPanel;
	private JLabel		userBeginLabel;
	private JTextField	userBeginDateTextField;
	private JButton		userBeginDateButton;
	private JSpinner	userBeginHourSpinner;
	private JSpinner	userBeginMinSpinner;
	private JPanel		userEndPanel;
	private JLabel		userEndLabel;
	private JTextField	userEndDateTextField;
	private JButton		userEndDateButton;
	private	JSpinner	userEndHourSpinner;
	private JSpinner	userEndMinSpinner;
	private JLabel		userSearchTextLabel;
	private JTextField	userSearchTextTextField;
	
	private JPanel		aliasSearchPanel;
	private	JLabel		aliasSelectLabel;
	private JTextField	aliasSelectTextField;
	private JPanel		aliasBeginPanel;
	private JLabel		aliasBeginLabel;
	private JTextField	aliasBeginDateTextField;
	private JButton		aliasBeginDateButton;
	private JSpinner	aliasBeginHourSpinner;
	private JSpinner	aliasBeginMinSpinner;
	private JPanel		aliasEndPanel;
	private JLabel		aliasEndLabel;
	private JTextField	aliasEndDateTextField;
	private JButton		aliasEndDateButton;
	private	JSpinner	aliasEndHourSpinner;
	private JSpinner	aliasEndMinSpinner;
	private JLabel		aliasSearchTextLabel;
	private JTextField	aliasSearchTextTextField;
	
	private JPanel		groupSearchPanel;
	private	JLabel		groupSelectLabel;
	private JTextField	groupSelectTextField;
	private JPanel		groupBeginPanel;
	private JLabel		groupBeginLabel;
	private JTextField	groupBeginDateTextField;
	private JButton		groupBeginDateButton;
	private JSpinner	groupBeginHourSpinner;
	private JSpinner	groupBeginMinSpinner;
	private JPanel		groupEndPanel;
	private JLabel		groupEndLabel;
	private JTextField	groupEndDateTextField;
	private JButton		groupEndDateButton;
	private	JSpinner	groupEndHourSpinner;
	private JSpinner	groupEndMinSpinner;
	private JLabel		groupSearchTextLabel;
	private JTextField	groupSearchTextTextField;
	
	private JPanel		resultPanel;
	private JTable		resultTable;
	
	private JPanel		buttonPanel;
	private JButton		searchButton;
	private JButton		cancelButton;
	
	
	
	public HistoryWindow() {
		this.me = this;
		this.setResizable(false);
		this.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		this.searchTypePanel 		= new JPanel();
		this.searchTypeLabel		= new JLabel("type of search");
		this.userToggleButton		= new JToggleButton("User");
		this.aliasToggleButton		= new JToggleButton("Alias");
		this.groupToggleButton		= new JToggleButton("Group");
		this.btnGrp					= new ButtonGroup();
		this.cardsPanel				= new JPanel(new CardLayout());
		this.cardUserSearchPanel	= new JPanel(new FlowLayout(FlowLayout.CENTER));
		this.cardAliasSearchPanel	= new JPanel(new FlowLayout(FlowLayout.CENTER));
		this.cardGroupSearchPanel	= new JPanel(new FlowLayout(FlowLayout.CENTER));
		
		this.userSearchPanel		= new JPanel(new GridLayout(4,2));
		this.userSelectLabel		= new JLabel("Username");
		this.userSelectComboBox		= new JComboBox<>();
		this.userBeginLabel			= new JLabel("Begin");
		this.userBeginPanel			= new JPanel();
		this.userBeginDateTextField	= new JTextField(10);
		this.userBeginDateButton	= new JButton();
		this.userBeginHourSpinner	= new JSpinner();
		this.userBeginMinSpinner	= new JSpinner();
		this.userEndLabel			= new JLabel("End");;
		this.userEndPanel			= new JPanel();
		this.userEndDateTextField	= new JTextField();
		this.userEndDateButton		= new JButton();
		this.userEndHourSpinner		= new JSpinner();
		this.userEndMinSpinner		= new JSpinner();;
		this.userSearchTextLabel	= new JLabel("Message text");
		this.userSearchTextTextField= new JTextField();
		
		this.aliasSearchPanel		= new JPanel(new GridLayout(4,2));
		this.aliasSelectLabel		= new JLabel("Alias");
		this.aliasSelectTextField	= new JTextField();
		this.aliasBeginLabel		= new JLabel("Begin");
		this.aliasBeginPanel		= new JPanel();
		this.aliasBeginDateTextField= new JTextField();
		this.aliasBeginDateButton	= new JButton();
		this.aliasBeginHourSpinner	= new JSpinner();
		this.aliasBeginMinSpinner	= new JSpinner();
		this.aliasEndLabel			= new JLabel("End");
		this.aliasEndPanel			= new JPanel();
		this.aliasEndDateTextField	= new JTextField();
		this.aliasEndDateButton		= new JButton();
		this.aliasEndHourSpinner	= new JSpinner();
		this.aliasEndMinSpinner		= new JSpinner();
		this.aliasSearchTextLabel	= new JLabel("Message text");
		this.aliasSearchTextTextField= new JTextField();
		
		this.groupSearchPanel		= new JPanel(new GridLayout(4,2));
		this.groupSelectLabel		= new JLabel("Groupname");
		this.groupSelectTextField	= new JTextField();
		this.groupBeginLabel		= new JLabel("Begin");
		this.groupBeginPanel		= new JPanel();
		this.groupBeginDateTextField= new JTextField();
		this.groupBeginDateButton	= new JButton();
		this.groupBeginHourSpinner	= new JSpinner();
		this.groupBeginMinSpinner	= new JSpinner();
		this.groupEndLabel			= new JLabel("End");
		this.groupEndPanel			= new JPanel();
		this.groupEndDateTextField	= new JTextField();
		this.groupEndDateButton		= new JButton();
		this.groupEndHourSpinner	= new JSpinner();
		this.groupEndMinSpinner		= new JSpinner();
		this.groupSearchTextLabel	= new JLabel("Message text");
		this.groupSearchTextTextField= new JTextField();
		
		this.resultPanel			= new JPanel();
		this.resultTable			= new JTable();
		
		this.buttonPanel			= new JPanel();
		this.searchButton			= new JButton("Search");
		this.cancelButton			= new JButton("Cancel");
		
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

