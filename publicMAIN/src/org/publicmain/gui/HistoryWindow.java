package org.publicmain.gui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
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
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;

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
	
	private Date		date;
	private SpinnerDateModel sdmBegin;
	private SpinnerDateModel sdmEnd;
	private JSpinner.DateEditor dateEditorBegin;
	private JSpinner.DateEditor dateEditorEnd;
	
	private JPanel 		userSearchPanel;
	private JLabel		userSelectLabel;
	private JComboBox<String> userSelectComboBox;
	
	private JPanel		aliasSearchPanel;
	private	JLabel		aliasSelectLabel;
	private JTextField	aliasSelectTextField;
	
	private JPanel		groupSearchPanel;
	private	JLabel		groupSelectLabel;
	private JTextField	groupSelectTextField;
	
	private JPanel		myPanel;
	private JPanel		BeginPanel;
	private JLabel		BeginLabel;
	private JTextField	BeginDateTextField;
	private JSpinner	BeginSpinner;
	private JPanel		EndPanel;
	private JLabel		EndLabel;
	private JTextField	EndDateTextField;
	private	JSpinner	EndSpinner;
	private JLabel		SearchTextLabel;
	private JTextField	SearchTextTextField;
	
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
		
		this.date 					= new Date();
		this.sdmBegin 					= new SpinnerDateModel(date, null, null, Calendar.HOUR_OF_DAY);
		
		this.userSearchPanel		= new JPanel(new GridLayout(4,2));
		this.userSelectLabel		= new JLabel("Username");
		this.userSelectComboBox		= new JComboBox<>();
		
		this.aliasSearchPanel		= new JPanel(new GridLayout(4,2));
		this.aliasSelectLabel		= new JLabel("Alias");
		this.aliasSelectTextField	= new JTextField();
		
		this.groupSearchPanel		= new JPanel(new GridLayout(4,2));
		this.groupSelectLabel		= new JLabel("Groupname");
		this.groupSelectTextField	= new JTextField();
		
		this.myPanel				= new JPanel();
		this.BeginLabel				= new JLabel("Begin");
		this.BeginPanel				= new JPanel();
		this.BeginDateTextField		= new JTextField(10);
		this.BeginSpinner			= new JSpinner(sdmBegin);
		this.EndLabel				= new JLabel("End");;
		this.EndPanel				= new JPanel();
		this.EndDateTextField		= new JTextField(10);
		this.EndSpinner				= new JSpinner(sdmEnd);
		this.SearchTextLabel		= new JLabel("Message text");
		this.SearchTextTextField	= new JTextField();
		
		this.dateEditorBegin		= new JSpinner.DateEditor(BeginSpinner, "hh:mm");
		this.dateEditorEnd			= new JSpinner.DateEditor(EndSpinner, "hh:mm");
		this.BeginSpinner.setEditor(dateEditorBegin);
		this.EndSpinner.setEditor(dateEditorEnd);
		
		this.resultPanel			= new JPanel();
		this.resultTable			= new JTable();
		
		this.buttonPanel			= new JPanel();
		this.searchButton			= new JButton("Search");
		this.cancelButton			= new JButton("Cancel");
		
		
		this.btnGrp.add(userToggleButton);
		this.btnGrp.add(aliasToggleButton);
		this.btnGrp.add(groupToggleButton);
		
		this.userToggleButton.addActionListener(new CardButtonController(cardsPanel));
		this.aliasToggleButton.addActionListener(new CardButtonController(cardsPanel));
		this.groupToggleButton.addActionListener(new CardButtonController(cardsPanel));
		this.searchButton.addActionListener(new HistoryButtonController());
		this.cancelButton.addActionListener(new HistoryButtonController());
		
		this.searchTypePanel.setPreferredSize(new Dimension(230,25));
		this.searchTypePanel.setBackground(Color.WHITE);
		this.searchTypePanel.add(userToggleButton);
		this.searchTypePanel.add(aliasToggleButton);
		this.searchTypePanel.add(groupToggleButton);
		
		this.cardUserSearchPanel.setPreferredSize(new Dimension(230,270));
		this.cardUserSearchPanel.setBackground(Color.WHITE);
		this.cardUserSearchPanel.add(userSearchPanel);
		
		this.cardAliasSearchPanel.setPreferredSize(new Dimension(230,270));
		this.cardAliasSearchPanel.setBackground(Color.WHITE);
		this.cardAliasSearchPanel.add(aliasSearchPanel);
		
		this.cardGroupSearchPanel.setPreferredSize(new Dimension(230,270));
		this.cardGroupSearchPanel.setBackground(Color.WHITE);
		this.cardGroupSearchPanel.add(groupSearchPanel);
		
		this.userSearchPanel.setBorder(BorderFactory.createTitledBorder("User search"));
		this.userSearchPanel.setPreferredSize(new Dimension(230,270));
		this.userSearchPanel.setBackground(Color.WHITE);
		this.userSearchPanel.add(userSelectLabel);
		this.userSearchPanel.add(userSelectComboBox);
		
		this.aliasSearchPanel.setBorder(BorderFactory.createTitledBorder("Alias search"));
		this.aliasSearchPanel.setPreferredSize(new Dimension(230,270));
		this.aliasSearchPanel.setBackground(Color.WHITE);
		this.aliasSearchPanel.add(aliasSelectLabel);
		this.aliasSearchPanel.add(aliasSelectTextField);
		
		this.groupSearchPanel.setBorder(BorderFactory.createTitledBorder("Group search"));
		this.groupSearchPanel.setPreferredSize(new Dimension(230,270));
		this.groupSearchPanel.setBackground(Color.WHITE);
		this.groupSearchPanel.add(groupSelectLabel);
		this.groupSearchPanel.add(groupSelectTextField);
		
		this.cardsPanel.setPreferredSize(new Dimension(230,270));
		this.cardsPanel.setBackground(Color.WHITE);
		this.cardsPanel.add(cardUserSearchPanel, "User");
		this.cardsPanel.add(cardAliasSearchPanel, "Alias");
		this.cardsPanel.add(cardGroupSearchPanel, "Group");
		
		this.myPanel.add(BeginLabel);
		this.BeginPanel.setBackground(Color.WHITE);
		this.BeginPanel.add(BeginDateTextField);
		this.BeginPanel.add(BeginSpinner);
		this.myPanel.add(BeginPanel);
		this.myPanel.add(EndLabel);
		this.EndPanel.setBackground(Color.WHITE);
		this.EndPanel.add(EndDateTextField);
		this.EndPanel.add(EndSpinner);
		this.myPanel.add(EndPanel);
		this.myPanel.add(SearchTextLabel);
		this.myPanel.add(SearchTextTextField);
		
		this.resultPanel.setPreferredSize(new Dimension(230,62));
		this.resultPanel.setBackground(Color.WHITE);
		this.resultPanel.add(resultTable);
		
		this.buttonPanel.setPreferredSize(new Dimension(230,25));
		this.buttonPanel.setBackground(Color.WHITE);
		this.buttonPanel.add(searchButton);
		this.buttonPanel.add(cancelButton);
		
		this.add(searchTypePanel);
		this.add(cardsPanel);
		this.add(myPanel);
		this.add(buttonPanel);
		this.add(resultPanel);
		
		
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
	
	class HistoryButtonController implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			JButton source = (JButton)e.getSource();
			
			switch(source.getText()){
			case "Search" :
				break;
			case "Cancel" :
				break;
			
			}
		}
		
		
		
	}
}

