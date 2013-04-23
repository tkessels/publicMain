package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

import org.publicmain.gui.HyperLinkController;
import org.resources.Help;

/**
 * @author ATRM
 * 
 */

public class HTMLContentDialog {

	private JDialog htmlDialog;
	private JTextPane htmlContentPane;
	private JScrollPane sp;
	private java.net.URL fileURL;
	private HyperLinkController hlc;
	private GraphicsEnvironment ge;
	private GraphicsDevice gd;
	private DisplayMode dm;

	/**
	 * Konstruktor zum F�llen des JDialog mit Titel, Icon und HTML-Dokument.
	 * 
	 * @param title
	 * @param icon
	 * @param htmlFile
	 */
	public HTMLContentDialog(String title, String icon, final String htmlFile) {
		
		htmlDialog = new JDialog();
		
		htmlDialog.setTitle(title);
		htmlDialog.setModal(false);
		htmlDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		htmlDialog.setLayout(new BorderLayout());
		htmlDialog.setIconImage(Help.getIcon(icon).getImage());
		htmlDialog.setMinimumSize(new Dimension(300, GUI.getGUI().getHeight()));
		htmlDialog.setPreferredSize(new Dimension(300, GUI.getGUI().getHeight()));
		
		hlc = new HyperLinkController();
		
		htmlContentPane = new JTextPane();
		htmlContentPane.addHyperlinkListener(hlc);
		
		this.sp = new JScrollPane(htmlContentPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		this.gd = ge.getDefaultScreenDevice();
		this.dm = gd.getDisplayMode();

		htmlContentPane.setBackground(new Color(255, 255, 255));
		htmlContentPane.setEditable(false);

		/**
		 * Dieser Thread l�d das HTML-Dokument auf die htmlContentPane, ohne die Anwendung
		 * zu blockieren.
		 */
		new Thread (new Runnable() {
			public void run() {
				try {
					// Dateipfad in eine URL umwandeln
					fileURL = Help.class.getResource(htmlFile);
					// Datei in JTextPane laden
					htmlContentPane.setPage(fileURL);
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
		htmlDialog.add(sp, BorderLayout.CENTER);
		htmlDialog.pack();
		}}).start();
		showIt();
	}
	
	public void showIt() {
		if ((GUI.getGUI().getLocation().x + GUI.getGUI().getWidth()
				+ htmlDialog.getWidth() < dm.getWidth())) {
			htmlDialog.setLocation(GUI.getGUI().getLocation().x
					+ GUI.getGUI().getWidth(), GUI.getGUI().getLocation().y);
		} else {
			htmlDialog.setLocationRelativeTo(null);
		}
		htmlDialog.setVisible(true);
	}
	
	public void hideIt() {
		htmlDialog.setVisible(false);
	}
}

