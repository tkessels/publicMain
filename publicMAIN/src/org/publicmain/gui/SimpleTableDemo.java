package org.publicmain.gui;

 
/*
 * SimpleTableDemo.java requires no other files.
 */
 
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
 
public class SimpleTableDemo extends JPanel {
    private boolean DEBUG = true;
 
    public SimpleTableDemo(String cols[], Object[][] daten) {
        super(new GridLayout(1,0));
 
        String[] columnNames = cols;
//        String[] columnNames = {"First Name",
//                                "Last Name",
//                                "Sport",
//                                "# of Years",
//                                "Vegetarian"};
// 
       
 
        final JTable table = new JTable(daten, cols);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
 
 
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
 
        //Add the scroll pane to this panel.
        add(scrollPane);
        createAndShowGUI(this);
    }
 
    private static void createAndShowGUI(SimpleTableDemo me) {
	        //Create and set up the window.
	        JFrame frame = new JFrame("SimpleTableDemo");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	 
	        //Create and set up the content pane.
	        SimpleTableDemo newContentPane = me;
	        newContentPane.setOpaque(true); //content panes must be opaque
	        frame.setContentPane(newContentPane);
	 
	        //Display the window.
	        frame.pack();
	        frame.setVisible(true);
	    }
    
 

}