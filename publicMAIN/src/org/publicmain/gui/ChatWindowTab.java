/**
 * 
 */
package org.publicmain.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.resources.Help;

/**
 * Diese Klasse stellt die Registerlasche (Tab) f�r ein ChatWindow bereit.
 * 
 * Diese Klasse stellt einen Tab f�r ein ChatWindow bereit und enth�lt ein Icon f�r
 * Privat bzw. Gruppenchat, einen Titel f�r den Chat und ein Icon um den Tab zu schlie�en.
 * Desweiteren wird daf�r gesorgt das bei inaktivem Tab mit ungelesener Nachricht der Titel blinkt.
 * Au�erdem wird ein Tab zu einem Offlinechatpartner ausgegraut. 
 * @author ATRM
 *
 */
public class ChatWindowTab extends JPanel{
	private JLabel lblTitle;
	private JLabel lblClose;
	private JLabel lblIcon;
	private ImageIcon tabCloseImgIcon;
	private JTabbedPane parent;
	private ChatWindow owner;
	private volatile Blinker blinker;

	/**
	 * Dieser Konstruktor erstellt einen ChatWindowTab.
	 * 
	 * @param title Der Titel des ChatWindowTabs.
	 * @param parent Das Eltern-JTabbedPane.
	 * @param owner Das ChatWindow zu dem der ChatWindowTab geh�ren soll.
	 */
	ChatWindowTab(String title, JTabbedPane parent, ChatWindow owner){
		// �bergebenen Besitzer und Parent setzen:
		this.parent=parent;
		this.owner=owner;

		// JPanel durchsichtig machen und Flowlayout anpassen:
		this.setOpaque(false);
		((FlowLayout) this.getLayout()).setHgap(5);

		// Tab Titel setzen und Listener adden:
		this.lblTitle = new JLabel(owner.getChatWindowName());
		this.lblTitle.addMouseListener(new MyMouseListener());

		// ImageIcon f�r Schlie�enLabel erstellen:
		if(owner.getOnlineState()){
			this.tabCloseImgIcon = Help.getIcon("TabCloseBlack.png",10);
		} else {
			this.tabCloseImgIcon = Help.getIcon("TabCloseGray.png",10);
		}

		// Schlie�enLabel f�r Tabbeschriftung erzeugen und gestalten:
		this.lblClose = new JLabel(tabCloseImgIcon);

		// Observer f�r das Image auf das lblClose setzen:
		this.tabCloseImgIcon.setImageObserver(lblClose);

		// MouseListener f�r Schlie�enlabel (lblClose) hinzuf�gen:
		this.lblClose.addMouseListener(new MyMouseListener());

		// Das Icon f�r den Tab erzeugen und je nach ChatTyp (privat/gruppe) setzen:
		this.lblIcon = new JLabel();
		if(owner.isPrivate()){
			if(owner.getOnlineState()){
				this.lblIcon.setIcon(Help.getIcon("private.png"));
			} else {
				this.lblIcon.setIcon(Help.getIcon("privateOffline.png"));
			}
		} else {
			this.lblIcon.setIcon(Help.getIcon("gruppe.png"));
		}

		// Das Icon (lblIcon), das TitelLabel (lblTitle) + Schlie�enLabel (btnClose) zum Tab (pnlTab) hinzuf�gen:
		this.add(lblIcon);
		this.add(lblTitle);
		this.add(lblClose);
	}


	/**
	 * Diese Methode setzt den Titel des ChatWindow Tabs neu.
	 * 
	 * Diese Methode wird verwendet um bei einem Aliaswechsel die Beschriftung des
	 * ChatWindowTabs zu aktualisieren.
	 */
	void updateAlias() {
		lblTitle.setText(owner.getChatWindowName());
	}

	/**
	 * Diese Methode setzt einen ChatWindowTab offline.
	 * 
	 * Diese Methode sorgt daf�r das die Elemente des ChatWindowTabs grau werden
	 * falls Chatpartner in einem privaten Chat offline geht.
	 */
	void setOffline(){
		this.lblTitle.setForeground(Color.GRAY);
		this.tabCloseImgIcon.setImage(Help.getIcon("TabCloseGray.png",10).getImage());
		this.lblIcon.setIcon(Help.getIcon("privateOffline.png"));
	}

	/**
	 * Diese Methode setzt einen ChatWindowTab online.
	 * 
	 * Diese Methode sorgt daf�r das die Elemente des ChatWindowTabs farbig werden
	 * wenn ein zuvor offline gegangener Chatpartner in einem privaten Chat wieder online kommt.
	 */
	void setOnline(){
		this.lblTitle.setForeground(Color.BLACK);
		this.tabCloseImgIcon.setImage(Help.getIcon("TabCloseBlack.png",10).getImage());
		this.lblIcon.setIcon(Help.getIcon("private.png"));
	}


	/**
	 * Diese Methode l�sst den Tabtitel die Farbe wechseln.
	 * 
	 * Diese Methode sorg daf�r das der Tabtitel (lblTitle) blinkt falls eine
	 * Nachricht empfangen wird und dieses ChatWindow nicht aktiv ist.
	 * Ist der Tabtitel schwarz wird er orange gesetzt und umgekehrt.
	 * Wenn das ChatWindow selectiert wird, wird das blinken gestoppt.
	 */
	private void blink() {
		if (lblTitle.getForeground() == Color.BLACK){
			this.lblTitle.setForeground(new Color(255, 130, 13));
		} else {
			this.lblTitle.setForeground(Color.BLACK);
		}
		if (parent.indexOfComponent(owner) == parent.getSelectedIndex()){
			this.stopBlink();
		}
	}

	/**
	 * Diese Methode startet den Thread der f�r das Blinken zust�ndig ist
	 */
	public synchronized void startBlink() {
		if(blinker==null){
			blinker=new Blinker(500);
			blinker.start();
		}
	}

	/**
	 * Diese Methode stoppt den Thread der f�r das Blinken zust�ndig ist
	 * und setzt den Tabtitel auf schwarz.
	 */
	public void stopBlink(){
		if(blinker!=null){
			blinker.stopit();
			blinker=null;
		}
		lblTitle.setForeground(Color.BLACK);
	}


	/**
	 * Diese Elementklasse stellt einen MouseAdapter f�r das ChatWindowTab bereit
	 * 
	 * @author ATRM
	 *
	 */
	private class MyMouseListener extends MouseAdapter{
		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {
			// Eventquelle Tabtitel (lblTitle)
			if (e.getSource() == lblTitle) {
				// Wird der Tabtitel mit der mittleren Maustaste gedr�ckt wird dieses ChatWindow geschlossen
				if (e.getModifiersEx() == 512) {
					GUI.getGUI().delChat(owner);
					// Wird der Tabtitel anders angeklickt wird der Tab selektiert und eventuelles blinken ausgeschalten
				} else {
					parent.setSelectedComponent(owner);
					stopBlink();
				}
				// Bei einer anderen Eventquelle wird das ChatWindow geschlossen
			} else {
				GUI.getGUI().delChat(owner);
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
			// Eventquelle Schlie�enlabel[X] (lblClose)
			if (e.getSource() == lblClose){
				// Ist die Maus �ber den lblClose und der Chatpartner online wird die Farbe orange
				if(owner.getOnlineState()){
					tabCloseImgIcon.setImage(Help.getIcon("TabCloseOrange.png",10).getImage());
					// Ist der Chatpartner offline wird die Farbe schwarz
				} else {
					tabCloseImgIcon.setImage(Help.getIcon("TabCloseBlack.png",10).getImage());
				}
			} else {
				JLabel source = (JLabel) e.getSource();
				// Ist die Maus �ber dem Tabtitel und der Chatpartner online wird der Titel orange
				if(owner.getOnlineState()){
					source.setForeground(new Color(255, 130, 13));
					// Ist der Chatpartner offline wird der Titel schwarz
				} else {
					source.setForeground(Color.BLACK);
				}
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
			// Eventquelle Schlie�enlabel[X] (lblClose)
			if (e.getSource() == lblClose){
				// Verl��t die Maus den lblClose und der Chatpartner ist online wird die Farbe orange 
				if(owner.getOnlineState()){
					tabCloseImgIcon.setImage(Help.getIcon("TabCloseBlack.png",10).getImage());
					// Ist der Chatpartner offline wird die Farbe grau
				} else {
					tabCloseImgIcon.setImage(Help.getIcon("TabCloseGray.png",10).getImage());
				}
			} else {
				JLabel source = (JLabel) e.getSource();
				// Verl��t die Maus den Tabtitel und der Chatpartner ist online wird der Titel orange
				if(owner.getOnlineState()){
					source.setForeground(Color.BLACK);
					// Ist der Chatpartner offline wird der Titel grau
				} else {
					source.setForeground(Color.GRAY);
				}
			}
		}
	}

	/**
	 * Diese Klasse stellt einen Thread bereit
	 * 
	 * Diese Klasse erm�glicht es dem ChatWindowTab zu blinken
	 * @author ATRM
	 *
	 */
	class Blinker extends Thread {
		int delay;
		volatile boolean active;

		/**
		 * Konstruktor f�r den BlinkerThread.
		 * 
		 * Dieser Konstruktor �bernimmt ein int (delay) f�r die Verz�gerung und
		 * setzt die Variable active auf false.
		 * @param delay
		 */
		public Blinker(int delay) {
			this.delay = delay;
			active = false;
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		// l�sst den Thread starten und setzt aktive auf true
		public void run() {
			active = true;
			// l�sst den Thread, solange aktiv, f�r die �bergebene Zeit (delay) schlafen und weckt ihn wieder
			while (active) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
				}
				blink();
			}
		}

		/**
		 * Diese Methode setzt den Thread inaktiv
		 * 
		 * Diese Methode setzt die Variable active auf false und deaktiviert somit den Thread
		 */
		public void stopit() {
			active = false;
		}
	}
}