package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.resources.Help;


@SuppressWarnings("serial")
public class Kalender extends JDialog {

	private GregorianCalendar selectedMonth;
	private JLabel nowLabel;
	private MonthPanel mPanel;
	final private String[] MONATSNAMEN = {"Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"};
	final private String[] TAGENAMEN = {"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"};
	private JTextField target;
	private JDialog me;
	private JPanel monthPanel;
	private JButton preMonthButton;
	private JButton nxtMonthButton;
	
	
	/**
	 * Create the Kalendardialog ;)
	 */
	public Kalender(JTextField target) {
		this.me = this;
		this.target =target;
		this.selectedMonth = new GregorianCalendar();
		this.monthPanel = new JPanel();
		this.preMonthButton = new JButton("<");
		this.nxtMonthButton = new JButton(">");
		this.nowLabel = new JLabel( ( MONATSNAMEN[selectedMonth.get(Calendar.MONTH)] + " " + selectedMonth.get(Calendar.YEAR) ) );
		
		this.setLayout(new BorderLayout(1, 1));
		this.setBackground(Color.WHITE);
		
		this.monthPanel.setBackground(Color.WHITE);
		this.monthPanel.setBorder(new LineBorder(new Color(5,64,94), 1, true));
		this.monthPanel.setLayout(new BorderLayout(0, 0));
		
		this.nowLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.nowLabel.setFont(new Font("Arial", Font.BOLD, 14));
		this.nowLabel.setForeground(new Color(5,64,94));
				
		
		this.preMonthButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedMonth = new GregorianCalendar(selectedMonth.get(Calendar.YEAR), (selectedMonth.get(Calendar.MONTH)-1), 1 ); //ein Monat runter
				nowLabel.setText( ( MONATSNAMEN[selectedMonth.get(Calendar.MONTH)] + " " + selectedMonth.get(Calendar.YEAR) ) );
				me.remove(mPanel);
				mPanel = new MonthPanel();
				me.add(mPanel, BorderLayout.CENTER);
				mPanel.validate();
			}
		});
		
		this.nxtMonthButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedMonth = new GregorianCalendar(selectedMonth.get(Calendar.YEAR), (selectedMonth.get(Calendar.MONTH)+1), 1 ); //ein Monat drauf
				nowLabel.setText( ( MONATSNAMEN[selectedMonth.get(Calendar.MONTH)] + " " + selectedMonth.get(Calendar.YEAR) ) );
				me.remove(mPanel);
				mPanel = new MonthPanel();
				me.add(mPanel, BorderLayout.CENTER);
				mPanel.validate();
			}
		});
		
		this.monthPanel.add(nowLabel, BorderLayout.CENTER);
		this.monthPanel.add(preMonthButton, BorderLayout.WEST);
		this.monthPanel.add(nxtMonthButton, BorderLayout.EAST);
		
		this.mPanel = new MonthPanel();
		
		this.add(monthPanel, BorderLayout.NORTH);
		this.add(mPanel, BorderLayout.CENTER);

		this.setUndecorated(true);
		this.pack();
		this.setLocation(MouseInfo.getPointerInfo().getLocation());
		this.setVisible(true);
		this.requestFocus();
		addFL(this);
		
	}
	
	private void addFL(Component tmp){
		if(tmp instanceof Container){
			for (Component cur : ((Container) tmp).getComponents()) {
				addFL(cur);
			}
		}
		tmp.addFocusListener(new Disposer());
	}
	
	
	private final class Disposer extends FocusAdapter {
		@Override
		public void focusLost(FocusEvent e) {
			Component other = e.getOppositeComponent();
			if(other!=null){
//			System.out.println(other.getClass().getSimpleName());
			while (other.getParent()!=null) {
				other = other.getParent();
				if(other instanceof Kalender) break;
			}
			if(!(other instanceof Kalender)){
				dispose();
			}
			}
		}
	}


	/**
	 * Baut eine Matrix / Kalendarübersicht auf
	 * @author Volker
	 *
	 */
	public class MonthPanel extends JPanel {
	
		private GregorianCalendar heute;
		
		/**
		 * Konsruktor bau eine Monatsübersicht des gewählten Monats auf
		 */
		public MonthPanel(){
			heute = new GregorianCalendar();
			this.addFocusListener(new Disposer());
			this.setBorder(new LineBorder(new Color(5,64,94), 1, true));
			this.setLayout(new GridLayout(0, 7, 1, 1));
			this.setBackground(Color.WHITE);
			
			//MO bis So schreiben
			for (String tag : TAGENAMEN){ 
				JLabel lbl = new JLabel(tag.substring(0, 2));
				lbl.setHorizontalAlignment(SwingConstants.CENTER);
				lbl.setForeground(new Color(5,64,94));
				this.add(lbl);
			}
			
			MonatsDaten dieserMonat = new MonatsDaten();
			//tageOffset befüllen
			for (int i = 1; i <= dieserMonat.getTageOffset(); i++){
				this.add(new JLabel(""));
			}
			
			//Monate mit kTagen(Buttons) befüllen
			int tNr = 1;
			for( final kTag t : dieserMonat.getMonat()){
				t.setText(""+tNr); 
				t.setForeground(new Color(5,64,94));
				//heute markieren
				if ( 	heute.get(Calendar.YEAR) == selectedMonth.get(Calendar.YEAR) &&  //heute markieren
						heute.get(Calendar.MONTH) == selectedMonth.get(Calendar.MONTH) &&
						tNr == heute.get(Calendar.DAY_OF_MONTH)) {
					t.setBorder(new LineBorder(new Color(255,133,18), 2, true)); 
					t.setToolTipText("Heute");
				}
				
				// Fallse feiertag, bezeichnung setzen und markieren
				if ( t.getFeiertagBez() != null ){
					t.setBorder(new LineBorder(Color.RED, 2, true)); 
					t.setToolTipText(t.getFeiertagBez());
				}
				
				//Aktionlistener für inDst hinzufügen
				t.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						//todo Return that day
//						System.out.println(t.getActionCommand());
						target.setText(t.getActionCommand());
						dispose();
						
					}
				});
				tNr++;
				this.add(t);
			}	
		}
	}
	

	/**
	 * Enthält Daten alle des aktuell gewählten Monats
	 * in Form einer Liste aus kTagen
	 * und dem Versatz des Montages am Monatsanfang 
	 * @author Volker
	 *
	 */
	
	private class MonatsDaten{
		
		private int tageOffset;
		private GregorianCalendar ersterTag;
		private int letzterTag;
		private List<kTag> monat;
		private GregorianCalendar buttondate;
		
		public MonatsDaten(){
			this.ersterTag = new GregorianCalendar(selectedMonth.get(Calendar.YEAR), selectedMonth.get(Calendar.MONTH), 1 );
			this.letzterTag = ersterTag.getActualMaximum(Calendar.DAY_OF_MONTH);
			this.monat = new ArrayList<kTag>();
			this.buttondate = new GregorianCalendar(ersterTag.get(Calendar.YEAR), ersterTag.get(Calendar.MONTH), ersterTag.get(Calendar.DAY_OF_MONTH));
			
			//wann ist der erste Montag - offSet
			tageOffset = 7;
			while( ersterTag.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY ) { //Sa=1 bis So=7
				ersterTag.add(Calendar.DATE, 1);//ein Tag drauf
				tageOffset--;
			}
			if (tageOffset ==7) tageOffset =0;
						
			for(int i = 0 ; i<letzterTag ; i++){
				monat.add(new kTag(buttondate));
				buttondate.roll(Calendar.DATE, true);
			}
			
			//iCAl einlesen
			try {
				readICS(Help.getFile("Feiertage.ics"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		

		public List<kTag> getMonat() {
			return monat;
		}


		public int getTageOffset() {
			return tageOffset;
		}
		
		/**
		 * liest eine ICal Datei und fügt Feiertage der Aktuellen Monatsliste / kTag hinzu
		 * @param icsDatei
		 */
		private void readICS(File icsDatei){
			int year = 0;
			int month = 0;
			int day = 0;
			String desc = null;
			String zeile ="";
			if (!icsDatei.exists()); //TODO runterladen
			try (BufferedReader bfr = new BufferedReader(new FileReader(icsDatei))){
				while ((zeile=bfr.readLine()) !=null){
					//datum finden
					if(zeile.startsWith("DTSTART")) {
						day = Integer.parseInt(zeile.substring(zeile.length()-2, zeile.length()));
						month = Integer.parseInt(zeile.substring(zeile.length()-4, zeile.length()-2));
						year = Integer.parseInt(zeile.substring(zeile.length()-8, zeile.length()-4));
					}
					//beschreibung finden
					if(zeile.startsWith("DESCRIPTION")) {
						desc = zeile.substring(12);
					}
					//wenn jahr und monat passen, füge beschreibung zur liste hinzu
					if ( ersterTag.get(Calendar.YEAR) == year  && ersterTag.get(Calendar.MONTH) == (month-1) ){
						monat.get(day-1).setFeiertagBez(desc);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Klasse erweitert JButton um 2 Variablen.
	 * inDST - ob man in DST war
	 * feiertagBez - bezeichnung eines Feiertages
	 * und hat dementsprechende getter & setter
	 *
	 */
	private class kTag extends JButton{
		
		private GregorianCalendar meinTag;
		private DateFormat df;
		private boolean inDST;
		private String feiertagBez;
		
		public kTag(GregorianCalendar tag) {
		    this.df = DateFormat.getDateInstance(DateFormat.SHORT);
			this.meinTag = tag;
			this.setActionCommand(df.format(meinTag.getTime()));
		}
		
		public boolean isInDST() {
			return inDST;
		}
		public void setInDST(boolean inDST) {
			this.inDST = inDST;
		}
		public String getFeiertagBez() {
			return feiertagBez;
		}
		public void setFeiertagBez(String feiertagBez) {
			this.feiertagBez = feiertagBez;
		}	
	}

}
