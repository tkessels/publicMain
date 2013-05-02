/**
 * 
 */
package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLClientInfoException;
import java.sql.Time;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.Node;
import org.resources.Help;

/**
 * @author ABerthold
 *
 */
public class ResultWindow extends JDialog {
	
	private JTable resultTable;
	private JScrollPane scroller;
	private JButton autoSizeButton;
	
	private JTextPane msgTextPane;
	private HTMLEditorKit htmlKit;
	private HTMLDocument htmlDoc;
	private JScrollPane hisScroller;
	
	public ResultWindow(JTable result){
		this.resultTable = result;
		this.scroller = new JScrollPane(resultTable);
		
//		this.resultTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
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
		this.setVisible(true);
		
		/*
		 * 
		 *  // automatically resize the columns whenever the data in the table changes
        table.getModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                ColumnsAutoSizer.sizeColumnsToFit(table);
            }
        });
		 */
	}

	public ResultWindow(Object[][] history){
		this.msgTextPane 	= new JTextPane();
		this.htmlKit 		= new HTMLEditorKit();
		this.htmlDoc 		= new HTMLDocument();
		this.hisScroller 	= new JScrollPane(msgTextPane,	JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		this.msgTextPane.setEditable(false);
		this.msgTextPane.setPreferredSize(new Dimension(400, 300));
		this.msgTextPane.setEditorKit(htmlKit);
		this.msgTextPane.setDocument(htmlDoc);
		
		for(int i=0; i<history.length; i++)
		{
			printHis(history[i]);
		}
		
		this.add(hisScroller);
		
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setIconImage(Help.getIcon("pM_Logo.png",64).getImage());
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	
	private void printHis(Object[] history) {
		try {
			Time time = new Time((long)history[0]);
			String senderName = (String)history[1];
			String msg	= (String)history[2];
			
			htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(), "<font color='orange'>" + time.toString() + " " + senderName +": </font><font color='black'>" + msg + "</font>", 0, 0, null);
		} catch (BadLocationException | IOException e) {
			LogEngine.log(e);
		}
		
		msgTextPane.setCaretPosition(htmlDoc.getLength());
	}
}
