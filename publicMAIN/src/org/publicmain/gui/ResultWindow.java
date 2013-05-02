/**
 * 
 */
package org.publicmain.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.resources.Help;

/**
 * @author ABerthold
 *
 */
public class ResultWindow extends JDialog {
	
	private JTable resultTable;
	private JScrollPane scroller;
	
	public ResultWindow(JTable result){
		this.resultTable = result;
		this.scroller = new JScrollPane(resultTable);
		
//		this.resultTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		this.resultTable.setFillsViewportHeight(true);
		
		this.add(scroller);
		
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setTitle("Found " + resultTable.getRowCount() + " messages");
		this.setIconImage(Help.getIcon("pM_Logo.png",64).getImage());
//		this.setMinimumSize(new Dimension(750, 200));
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	
}
