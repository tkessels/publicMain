package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.sound.sampled.Clip;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

import org.resources.Help;

/**
 * Diese Klasse stellt einen JDialog bereit welcher mit Titel, Icon und
 * zu ladenendem HTML-Dokument �ber einen Konstruktor bef�llt wird.
 * <b>Wichtig: Das ist unser Skript Anteil :-)</b>
 * 
 * Bedingt durch die JTextPane werden HTML-Dokumente nur in der
 * Version 3 und Stylesheets nur in Version 1 unterst�tzt.
 * 
 * @author ATRM
 * 
 */

public class HTMLContentDialog extends JDialog {

	private JDialog htmlDialog;
	private JTextPane htmlContentPane;
	private JScrollPane sp;
	private java.net.URL fileURL;
	private HyperLinkController hlc;
	private GraphicsEnvironment ge;
	private GraphicsDevice gd;
	private DisplayMode dm;
	private Clip sound;

	/**
	 * Konstruktor zum F�llen des JDialog mit Titel, Icon und HTML-Dokument.
	 * 
	 * @param title
	 * @param icon
	 * @param htmlFile
	 */
	public HTMLContentDialog(String title, String icon, final String htmlFile) {

		htmlDialog = this;

		this.setTitle(title);
		this.setModal(false);
		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		this.setLayout(new BorderLayout());
		this.setIconImage(Help.getIcon(icon).getImage());
		this.setMinimumSize(new Dimension(250, GUI.getGUI().getHeight()));
		this.setPreferredSize(new Dimension(250, GUI.getGUI().getHeight()));


		hlc = new HyperLinkController();

		htmlContentPane = new JTextPane();
		htmlContentPane.addHyperlinkListener(hlc);

		this.sp = new JScrollPane(htmlContentPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		this.gd = ge.getDefaultScreenDevice();
		this.dm = gd.getDisplayMode();

		htmlContentPane.setBackground(new Color(255, 255, 255));
		htmlContentPane.setEditable(false);

		//		try {
		//			this.sound = Applet.newAudioClip(Help.class.getResource("fahrstuhl.mp3"));
		//		} catch (Exception e2) {
		//			System.out.println(e2.getMessage());
		//		}

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

	/**
	 * Die Position des HTMLContentDialog festlegen und auf sichtbar setzen. 
	 */
	public void showIt() {
		if (((GUI.getGUI().getLocation().x + GUI.getGUI().getWidth()
				+ this.getWidth()) < dm.getWidth())) {
			this.setLocation(GUI.getGUI().getLocation().x
					+ GUI.getGUI().getWidth(), GUI.getGUI().getLocation().y);
		} else {
			this.setLocationRelativeTo(null);
		}
		this.setVisible(true);
		sound = Help.getSound("test.wav");
		if(sound!=null){
			sound.start();
		}
	}

	/**
	 * Den HTMLContentDialog ausblenden.
	 * TODO: �berpr�fen ob diese Methode genutzt wird!
	 */
	public void hideIt() {
		this.setVisible(false);
		if(sound!=null){
			sound.stop();
		}
	}

	@Override
	@Deprecated
	public void hide() {
		if(sound!=null) {
			sound.stop();
		}
		super.hide();
	}

	@Override
	public void dispose() {
		if(sound!=null) {
			sound.stop();
		}
		super.dispose();
	}


}

