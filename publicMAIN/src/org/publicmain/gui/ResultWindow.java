/**
 * 
 */
package org.publicmain.gui;

import javax.swing.JDialog;
import javax.swing.JTable;

/**
 * @author ABerthold
 *
 */
public class ResultWindow extends JDialog {
	
	private JTable resultTable;
	
	public ResultWindow(JTable result){
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		this.resultTable = result;
		this.add(resultTable);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		
	}
	
}
