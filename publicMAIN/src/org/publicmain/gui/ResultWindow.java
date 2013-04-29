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
	
	public ResultWindow(){
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setLocationRelativeTo(null);
		
		this.resultTable = new JTable();
		
		this.pack();
		this.setVisible(true);
		
	}
	
}
