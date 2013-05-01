/**
 * 
 */
package org.publicmain.gui;

import java.awt.ScrollPane;

import javax.swing.JDialog;
import javax.swing.JTable;

/**
 * @author ABerthold
 *
 */
public class ResultWindow extends JDialog {
	
	private JTable resultTable;
	
	public ResultWindow(JTable result){
		this.resultTable = result;
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setTitle("rows:"+resultTable.getRowCount());
		ScrollPane scroller = new ScrollPane();
		scroller.add(resultTable);
		this.add(scroller);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		
	}
	
}
