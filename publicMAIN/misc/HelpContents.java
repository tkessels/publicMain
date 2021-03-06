package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import org.publicmain.common.LogEngine;
import org.resources.Help;

/**
 * Diese Klasse stellt die Hilfeseiten unter Help/Help im Men� zur Verf�gung. Die
 * Hilfeseiten werden �ber eine JTextPane mit einen HTML-Dokument erstellt. Die
 * JTextPane stellt HTML 3.X und CSS 1 zur Verf�gung um das Dokument zu formatieren,
 * auf einen Verweis zu Hilfe-Seiten im Internet wurde zugunsten des Gesamtkonzeptes
 * der Anwendung verzichtet. 
 * 
 * @author ATRM
 * 
 */

public class HelpContents extends JDialog implements HyperlinkListener{

	private static HelpContents me; 

	private JTextPane helpContentTxt;
	private JScrollPane sp;
	private File htmlFile;
	private java.net.URL fileURL;
	// Zum ermitteln der aktuellen Bildschirmaufl�sung
	private GraphicsEnvironment ge;
	private GraphicsDevice gd;
	private DisplayMode dm;

	/**
	 * Konstruktor f�r den Help Content Dialog
	 */
	public HelpContents() {
		
		this.me=this;
		
		this.setTitle("Help");
		this.setModal(false);
		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		this.setLayout(new BorderLayout());
		this.setIconImage(Help.getIcon("helpContentsIcon.png").getImage());
		this.setMinimumSize(new Dimension(300, GUI.getGUI().getHeight()));
		this.setPreferredSize(new Dimension(300, GUI.getGUI().getHeight()));
		
		this.helpContentTxt = new JTextPane();
		this.helpContentTxt.addHyperlinkListener(this);
		this.sp = new JScrollPane(helpContentTxt,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		this.gd = ge.getDefaultScreenDevice();
		this.dm = gd.getDisplayMode();

		helpContentTxt.setBackground(new Color(255, 255, 255));
		helpContentTxt.setEditable(false);

		new Thread (new Runnable() {
			public void run() {
				try {
					// Dateipfad in eine URL umwandeln
					fileURL = Help.class.getResource("helpcontent.html");
					// Datei in JTextPane laden
					helpContentTxt.setPage(fileURL);
				} catch (MalformedURLException e1) {
					LogEngine.log(this, e1);;
				} catch (IOException e) {
					LogEngine.log(this, e);
				}
		me.add(sp, BorderLayout.CENTER);
				
		me.pack();
		me.showIt();
			}}).start();
	}

	/**
	 * An dieser Stelle findet die Pr�fung statt, ob die rechtsseitig neben dem Anwendungsfenster eingeblendete Hilfe
	 * die aktuelle Aufl�sung �berschreiten w�rde. Ist dies der Fall wird die Hilfe zentriert auf dem Bildschirm
	 * angezeigt. Wenn die Bedingung nicht zutrifft, wird die Hilfe rechts neben dem Anwendungsfenster eingeblendet.
	 */
	public void showIt() {
		if ((GUI.getGUI().getLocation().x + GUI.getGUI().getWidth() + this.getWidth() < dm.getWidth())) {
			this.setLocation(GUI.getGUI().getLocation().x + GUI.getGUI().getWidth(), GUI.getGUI().getLocation().y);
		} else {
			this.setLocationRelativeTo(null);
		}
		this.setVisible(true);
	}
	
	public static HelpContents getMe() {
		return me;
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent arg0) {
		if (arg0.getEventType()==EventType.ACTIVATED) {
			try {
				openWebpage(new URL(arg0.getURL().toString().split("�")[1]).toURI());
			} catch (Exception e) {
				LogEngine.log(this, "Could not open Webbrowser!", LogEngine.ERROR);
			}
		}
		
	}
	
	public static void openWebpage(URI uri) throws IOException {
	    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	            desktop.browse(uri);
	    }
	}

}