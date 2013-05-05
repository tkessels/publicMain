package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerDateModel;

import org.publicmain.common.Node;
import org.publicmain.sql.DatabaseDaten;
import org.publicmain.sql.DatabaseEngine;
import org.resources.Help;

public class HistoryWindow extends JDialog{

	private static HistoryWindow me;

	private JLabel		banner;

	private JPanel		searchTypePanel;
	private JToggleButton userToggleButton;
	private JToggleButton aliasToggleButton;
	private JToggleButton groupToggleButton;
	private ButtonGroup	btnGrp;
	private JPanel		cardsPanel;
	private	JPanel		cardUserSearchPanel;
	private JPanel		cardAliasSearchPanel;
	private JPanel		cardGroupSearchPanel;

	private Date		beginDate;
	private Date		endDate;
	private SpinnerDateModel sdmBegin; //müssen das attribute sein
	private SpinnerDateModel sdmEnd;   //müsen das attribute sein
	private JSpinner.DateEditor dateEditorBegin;
	private JSpinner.DateEditor dateEditorEnd;

	private JPanel 		userSearchPanel;
	private JLabel		userSelectLabel;
	private JComboBox<Node> userSelectComboBox;

	private JPanel		aliasSearchPanel;
	private	JLabel		aliasSearchLabel;
	private JTextField	aliasSearchTextField;

	private JPanel		groupSearchPanel;
	private	JLabel		groupSearchLabel;
	private JTextField	groupSearchTextField;

	private JPanel		myPanel;
	private JPanel		beginPanel;
	private JLabel		beginLabel;
	private JTextField	beginDateTextField;
	private JSpinner	beginSpinner;
	private JPanel		endPanel;
	private JLabel		endLabel;
	private JTextField	endDateTextField;
	private	JSpinner	endSpinner;
	private JLabel		searchTextLabel;
	private JTextField	textSearchTextField;

	private JPanel		outputFormatPanel;
	private JLabel spaltenauswahl_label;
	private JTextField spaltenauswahl;
	private JLabel formatString_label;
	private JTextArea formatString;
	private JLabel darstellungsStil_label;
	private JComboBox<String> darstellungsStil;

	private JPanel		buttonPanel;
	private JButton		searchButton;
	private JButton		cancelButton;

	private GregorianCalendar beginGregCal;
	private GregorianCalendar endGregCal;

	private String activeCard = "User";
	private String activeTyp = "Tabbed";
	private String[] tabbed_format= {"1,2,3,4,5,6,7","7,6,5,4,3,2,1","3,4,6"};
	private String[] text_format= {"$6$:$3$:$5$","7,6,5,4,3,2,1","<font color='orange'>$2d% $2t% $3$ ($6$): </font><font color='black'>$5$</font>"};



	public HistoryWindow() {
		HistoryWindow.me = this;
		this.setResizable(false);
		this.setLayout(new FlowLayout(FlowLayout.CENTER));

		this.banner					 = new JLabel(Help.getIcon("textlogo.png",210,50));



		this.searchTypePanel 			= new JPanel(new GridLayout(1,3));
		this.userToggleButton			= new JToggleButton("User");
		this.aliasToggleButton			= new JToggleButton("Alias");
		this.groupToggleButton		= new JToggleButton("Group");
		this.btnGrp					= new ButtonGroup();
		this.cardsPanel				= new JPanel(new CardLayout());
		this.cardUserSearchPanel	= new JPanel(new FlowLayout(FlowLayout.CENTER));
		this.cardAliasSearchPanel	= new JPanel(new FlowLayout(FlowLayout.CENTER));
		this.cardGroupSearchPanel	= new JPanel(new FlowLayout(FlowLayout.CENTER));

		this.beginDate 				= new Date();
		this.sdmBegin 				= new SpinnerDateModel(beginDate, null, null, Calendar.HOUR_OF_DAY);
		this.endDate 					= new Date();
		this.sdmEnd					= new SpinnerDateModel(endDate, null, null, Calendar.HOUR_OF_DAY);

		this.userSearchPanel			= new JPanel(new GridLayout(1,2));
		this.userSelectLabel			= new JLabel("Username");
		this.userSelectComboBox		= DatabaseEngine.getDatabaseEngine().getUsers();


		this.aliasSearchPanel		= new JPanel(new GridLayout(1,2));
		this.aliasSearchLabel		= new JLabel("Alias");
		this.aliasSearchTextField	= new JTextField();
		this.aliasSearchTextField.setActionCommand("Search");
		this.aliasSearchTextField.addActionListener(new HistoryButtonController());


		this.groupSearchPanel		= new JPanel(new GridLayout(1,2));
		this.groupSearchLabel		= new JLabel("Groupname");
		this.groupSearchTextField	= new JTextField();
		this.groupSearchTextField.setActionCommand("Search");
		this.groupSearchTextField.addActionListener(new HistoryButtonController());

		this.myPanel				= new JPanel(new GridLayout(3,2));
		this.beginLabel			= new JLabel("Begin (date/time)");
		this.beginPanel			= new JPanel(new BorderLayout());
		this.beginDateTextField	= new JTextField(8);
		this.beginSpinner			= new JSpinner(sdmBegin);
		this.endLabel				= new JLabel("End (date/time)");;
		this.endPanel			= new JPanel(new BorderLayout());
		this.endDateTextField		= new JTextField(8);
		this.endSpinner			= new JSpinner(sdmEnd);
		this.searchTextLabel		= new JLabel("Message text");

		this.textSearchTextField	= new JTextField();
		this.textSearchTextField.setActionCommand("Search");
		this.textSearchTextField.addActionListener(new HistoryButtonController());

	
		this.outputFormatPanel		= new JPanel();

		this.dateEditorBegin		= new JSpinner.DateEditor(beginSpinner, "HH:mm");
		this.dateEditorEnd			= new JSpinner.DateEditor(endSpinner, "HH:mm");
		this.beginSpinner.setEditor(dateEditorBegin);
		this.endSpinner.setEditor(dateEditorEnd);

		this.buttonPanel			= new JPanel();
		this.searchButton			= new JButton("Search");
		this.searchButton.setActionCommand("Search");

		this.cancelButton			= new JButton("Cancel");
		this.cancelButton.setActionCommand("Cancel");

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

		this.cardUserSearchPanel.setBackground(Color.WHITE);
		this.cardUserSearchPanel.add(userSearchPanel);

		this.cardAliasSearchPanel.setBackground(Color.WHITE);
		this.cardAliasSearchPanel.add(aliasSearchPanel);

		this.cardGroupSearchPanel.setBackground(Color.WHITE);
		this.cardGroupSearchPanel.add(groupSearchPanel);

		this.userSearchPanel.setBorder(BorderFactory.createTitledBorder("User search"));
		this.userSearchPanel.setPreferredSize(new Dimension(230,42));
		this.userSearchPanel.setBackground(Color.WHITE);
		this.userSearchPanel.add(userSelectLabel);
		this.userSearchPanel.add(userSelectComboBox);

		this.aliasSearchPanel.setBorder(BorderFactory.createTitledBorder("Alias search"));
		this.aliasSearchPanel.setPreferredSize(new Dimension(230,42));
		this.aliasSearchPanel.setBackground(Color.WHITE);
		this.aliasSearchPanel.add(aliasSearchLabel);
		this.aliasSearchPanel.add(aliasSearchTextField);

		this.groupSearchPanel.setBorder(BorderFactory.createTitledBorder("Group search"));
		this.groupSearchPanel.setPreferredSize(new Dimension(230,42));
		this.groupSearchPanel.setBackground(Color.WHITE);
		this.groupSearchPanel.add(groupSearchLabel);
		this.groupSearchPanel.add(groupSearchTextField);

		this.cardsPanel.setPreferredSize(new Dimension(230,50));
		this.cardsPanel.setBackground(Color.WHITE);
		this.cardsPanel.add(cardUserSearchPanel, "User");
		this.cardsPanel.add(cardAliasSearchPanel, "Alias");
		this.cardsPanel.add(cardGroupSearchPanel, "Group");

		this.myPanel.setBorder(BorderFactory.createTitledBorder("Search options"));
		this.myPanel.setPreferredSize(new Dimension(230,80));
		this.myPanel.setBackground(Color.WHITE);
		this.myPanel.add(beginLabel);
		this.beginPanel.setBackground(Color.WHITE);
		this.beginPanel.add(beginDateTextField,BorderLayout.CENTER);
		this.beginDateTextField.setEditable(false);
		this.beginDateTextField.setToolTipText("click to configure date");
		this.beginDateTextField.setText("<click>");
		this.beginDateTextField.addMouseListener(new MyMouseAdapter());
		this.beginPanel.add(beginSpinner,BorderLayout.EAST);
		this.myPanel.add(beginPanel);
		this.myPanel.add(endLabel);
		this.endPanel.setBackground(Color.WHITE);
		this.endPanel.add(endDateTextField,BorderLayout.CENTER);
		this.endDateTextField.setEditable(false);
		this.endDateTextField.setToolTipText("click to configure date");
		this.endDateTextField.setText("<click>");
		this.endDateTextField.addMouseListener(new MyMouseAdapter());
		this.endPanel.add(endSpinner,BorderLayout.EAST);
		this.myPanel.add(endPanel);
		this.myPanel.add(searchTextLabel);
		this.myPanel.add(textSearchTextField);


		this.outputFormatPanel.setPreferredSize(new Dimension(230,137));
		this.outputFormatPanel.setBackground(Color.WHITE);
		String[] typs = {"Tabbed","Text"};
		this.darstellungsStil_label = new JLabel("Typ");
		this.darstellungsStil=new JComboBox<String>(typs) ;
		this.darstellungsStil.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					        JComboBox cb = (JComboBox)e.getSource();
					        String typ= (String)cb.getSelectedItem();
					        toggleTyp(typ);
			}
		});
		 this.formatString_label = new JLabel("Format-String:");
		 this.formatString = new JTextArea();
		 
		formatString.setFont(new Font("Serif", Font.ITALIC, 12));
		formatString.setText(tabbed_format[0]);
		formatString.setLineWrap(true);
		formatString.setWrapStyleWord(true);
		JScrollPane areaScrollPane = new JScrollPane(formatString);
		areaScrollPane.setBackground(Color.white);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(230, 105));
		areaScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Format String"),BorderFactory.createEmptyBorder(1,1,1,1)),areaScrollPane.getBorder()));
		JPanel tmp_pnl = new JPanel();
		tmp_pnl.setBackground(Color.white);
		tmp_pnl.add(darstellungsStil_label);
		tmp_pnl.add(darstellungsStil);
		this.outputFormatPanel.add(tmp_pnl);
		this.outputFormatPanel.add(areaScrollPane);
		
		this.buttonPanel.setPreferredSize(new Dimension(230,27));
		this.buttonPanel.setBackground(Color.WHITE);
		this.buttonPanel.add(searchButton);
		this.buttonPanel.add(cancelButton);

		this.add(banner);
		this.add(searchTypePanel);
		this.add(cardsPanel);
		this.add(myPanel);
		this.add(outputFormatPanel);
		this.add(buttonPanel);


		this.setTitle("History");
		this.setModal(false);
		//		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setIconImage(Help.getIcon("pM_Logo.png").getImage());
		this.getContentPane().setBackground(Color.WHITE);
		this.setMinimumSize(new Dimension(250, GUI.getGUI().getHeight()));
		this.setMaximumSize(new Dimension(250, GUI.getGUI().getHeight()));
		this.setPreferredSize(new Dimension(250, GUI.getGUI().getHeight()));
		this.pack();

		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}


	protected void toggleTyp(String typ) {
		int i = Arrays.asList(new String[] {"User","Alias","Group"}).indexOf(activeCard);
		if(!activeTyp.equals(typ)) {
			if(typ.equals("Tabbed")) {
				text_format[i]=formatString.getText();
				formatString.setText(tabbed_format[i]);
			}else {
				tabbed_format[i]=formatString.getText();
				formatString.setText(text_format[i]);
			}
			activeTyp=typ;
		}
		
	}


	void setEnd(GregorianCalendar temp){
		endGregCal=temp;
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		endDateTextField.setText(df.format(temp.getTime()));

	}
	void setBegin(GregorianCalendar temp){
		beginGregCal=temp;
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		beginDateTextField.setText(df.format(temp.getTime()));
	}

	static void closeThis(){
		if(me!=null) {
			me.dispose();
		}
	}

	static void showThis(){
		if(me==null) {
			new HistoryWindow();
		}
		me.setVisible(true);
	}


	class CardButtonController implements ActionListener{

		private JPanel ref;

		public CardButtonController(JPanel ref){
			this.ref = ref;
		}

		public void actionPerformed(ActionEvent e) {
			
			
			CardLayout card = (CardLayout) ref.getLayout();
			int i = Arrays.asList(new String[] {"User","Alias","Group"}).indexOf(activeCard);
			String text = ((JToggleButton)e.getSource()).getText();
			int j = Arrays.asList(new String[] {"User","Alias","Group"}).indexOf(text);

			
			if(i!=j) {
				if(darstellungsStil.getSelectedItem().equals("Tabbed")) {
					tabbed_format[i]=formatString.getText();
					formatString.setText(tabbed_format[j]);
				}else {
					text_format[i]=formatString.getText();
					formatString.setText(text_format[j]);

				}
			}
			
			card.show( ref, text );
			activeCard=text;
			
		}
	}

	class HistoryButtonController implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			switch(e.getActionCommand()){
			case "Search" :

				Date value = (Date) beginSpinner.getValue();
				if(beginGregCal!=null){
					beginGregCal.set(Calendar.HOUR_OF_DAY,value.getHours() );
					beginGregCal.set(Calendar.MINUTE,value.getMinutes() );
					System.out.println(beginGregCal);
				}
				if(endGregCal!=null){
					value = (Date) endSpinner.getValue();
					endGregCal.set(Calendar.HOUR_OF_DAY,value.getHours() );
					endGregCal.set(Calendar.MINUTE,value.getMinutes() );
					System.out.println(endGregCal);
				}
				DatabaseDaten querry= null;
				switch(activeCard){
				case "User":
					long uid; 
					Node selectedNode = (Node) userSelectComboBox.getSelectedItem();
					uid = (userSelectComboBox.getSelectedItem()!=null)?selectedNode.getUserID() : -1;
					querry = DatabaseEngine.getDatabaseEngine().selectMSGsByUser(uid,beginGregCal, endGregCal, textSearchTextField.getText());
					if(querry!=null) {
						new ResultWindow(querry,formatString.getText(),darstellungsStil.getSelectedItem().equals("Text"));
					}
				
					break;
				case "Group": 
					querry = DatabaseEngine.getDatabaseEngine().selectMSGsByGroup(groupSearchTextField.getText(), beginGregCal, endGregCal, textSearchTextField.getText());
					
					if(querry!=null) {
						new ResultWindow(querry,formatString.getText(),darstellungsStil.getSelectedItem().equals("Text"));
					}
					
					break;
				case "Alias":
					querry = DatabaseEngine.getDatabaseEngine().selectMSGsByAlias(aliasSearchTextField.getText(), beginGregCal,endGregCal,textSearchTextField.getText());
					if(querry!=null) {
						new ResultWindow(querry,formatString.getText(),darstellungsStil.getSelectedItem().equals("Text"));
					}
					break;
				default:
				}

				
				


				break;
			case "Cancel" :
				closeThis();
				break;
			}
		}
	}

	class MyMouseAdapter extends MouseAdapter{
		public void mouseClicked(MouseEvent e) {
			if(e.getSource()==beginDateTextField){
				new Kalender( me, true);
			}
			if(e.getSource()==endDateTextField){
				new Kalender( me, false);
			}
		}
	}
}