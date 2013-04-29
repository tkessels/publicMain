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
		
		this.resultTable = new JTable();
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		
	}
	
}
