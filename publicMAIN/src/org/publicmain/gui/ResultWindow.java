/**
 * 
 */
package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.publicmain.common.LogEngine;
import org.publicmain.sql.DatabaseDaten;
import org.resources.Help;


public class ResultWindow extends JDialog {
	private static final long serialVersionUID = 8171114238370399022L;
	private JButton autoSizeButton;
	private JScrollPane hisScroller;
	private HTMLDocument htmlDoc;
	private HTMLEditorKit htmlKit;
	private JTextPane msgTextPane;
	private JTable resultTable;
	private JScrollPane scroller;
	
	
//	public ResultWindow(DatabaseDaten querry, int columnSelection) {
//		constructWithTable(new JTable( new DefaultTableModel(querry.getData(columnSelection),querry.getHeader(columnSelection))));
//	}
//	
//	public ResultWindow(DatabaseDaten querry, int columnSelection, String format) {
//		constructWithPanel(querry.getData(columnSelection), format);
//	}
	public ResultWindow(DatabaseDaten querry, String format, boolean text) {
		if(text) constructWithPanel(querry.getData(127), format);
		else {
			constructWithTable(querry,format);
		}
	}

	private void constructWithTable(DatabaseDaten querry, String format) {
		String[] array= format.replaceAll("[\\D]+"," ").trim().split(" ");
		Vector<Integer> cols = new Vector<Integer>();
		
		for (int i = 0; i < array.length; i++) {
			 int tmp = Integer.parseInt(array[i])-1;
			if(tmp>=0&&tmp<querry.getCols())cols.add(tmp);
		}
		
		String [] header = new String[cols.size()];
		for (int i = 0; i < header.length; i++) {
			header[i]=querry.getHeader()[cols.get(i)];
		}
		
		String [][] newDaten = new String[querry.getRows()][cols.size()];
		for (int i = 0; i < querry.getData().length; i++) {
			for (int j = 0 ; j < cols.size(); j++) {
				if(header[j].equals("time"))newDaten[i][j]=makeTime(querry.getData()[i][cols.get(j)]); 
				else newDaten[i][j]=querry.getData()[i][cols.get(j)];
			}
			
		}
		constructWithTable(new JTable(new DefaultTableModel(newDaten,header)));
		
		
//		int[] cols = new int [array.length];
//		new JTable( new DefaultTableModel(
		
	}

	/**
	 * @param history
	 */
	private void constructWithPanel(Object[][] history,String format) {
		if (format==null) format = "<font color='orange'>$0d% $0t% $1$ : </font><font color='black'>$2$</font>";
		this.msgTextPane 	= new JTextPane();
		this.htmlKit 		= new HTMLEditorKit();
		this.htmlDoc 		= new HTMLDocument();
		this.hisScroller 	= new JScrollPane(msgTextPane,	JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	
		this.msgTextPane.setEditable(false);
		this.msgTextPane.setPreferredSize(new Dimension(400, 300));
		this.msgTextPane.setEditorKit(htmlKit);
		this.msgTextPane.setDocument(htmlDoc);
	
		for (Object[] element : history) {
			printHis(element,format);
		}
	
		this.add(hisScroller);
	
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setIconImage(Help.getIcon("pM_Logo.png",64).getImage());
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}


	/**
	 * @param result
	 */
	private void constructWithTable(JTable result) {
		this.resultTable = result;
		this.scroller = new JScrollPane(resultTable);
	
		this.resultTable.setFillsViewportHeight(true);
		this.resultTable.getModel().addTableModelListener(new TableModelListener() {
	
			@Override
			public void tableChanged(TableModelEvent e) {
				ColumnsAutoSizer.sizeColumnsToFit(resultTable);
	
			}
		});
		
	
		this.autoSizeButton = new JButton("Auto-size columns");
	
		// resize the columns when the user clicks the button
		autoSizeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ColumnsAutoSizer.sizeColumnsToFit(resultTable);
			}
		});
		this.add(autoSizeButton,BorderLayout.SOUTH);
	
	
		this.add(scroller);
	
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setTitle("Found " + resultTable.getRowCount() + " messages");
		this.setIconImage(Help.getIcon("pM_Logo.png",64).getImage());
		this.pack();
		this.setLocationRelativeTo(null);
		ColumnsAutoSizer.sizeColumnsToFit(resultTable);
		this.setVisible(true);
	
	}
	private String makeTime(Object data) {
		try {
			String tmp = (String) data;
			long x = Long.parseLong(tmp);
			String time = new Time(x).toString();
			String date = new Date(x).toString();
			return date + "  " + time; 
		} catch (Exception e) {
		}
		return data.toString();
	}


	private void printHis(Object[] history,String format) {
		try {
			for (int i = 0; i < history.length; i++) {
				format=format.replace("$"+i+"$", (String) history[i]);
				try {
					long x = Long.parseLong((String) history[i]);
					String time = new Time(x).toString();
					String date = new Date(x).toString();
					format=format.replace("$"+i+"d%", date);
					format=format.replace("$"+i+"t%", time);	
				} catch(Exception ignored) {}
			}
			htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(), format, 0, 0, null);
		} catch (BadLocationException | IOException e) {
			LogEngine.log(e);
		}
	}
	

	
	
}
