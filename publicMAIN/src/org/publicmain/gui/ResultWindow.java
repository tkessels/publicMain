/**
 * 
 */
package org.publicmain.gui;

import java.awt.Dimension;
import java.awt.ScrollPane;

import javax.swing.JDialog;
import javax.swing.JTable;

import org.resources.Help;

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
		this.setIconImage(Help.getIcon("pM_Logo.png",64).getImage());
		this.setMinimumSize(new Dimension(750, 200));
		ScrollPane scroller = new ScrollPane();
		scroller.add(resultTable);
		this.add(scroller);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		
	}
	
}
