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
 * Diese Klasse stellt die Registerlasche (Tab) für ein ChatWindow bereit.
 * 
 * Diese Klasse stellt einen Tab für ein ChatWindow bereit und enthält ein Icon für
 * Privat bzw. Gruppenchat, einen Titel für den Chat und ein Icon um den Tab zu schließen.
 * Desweiteren wird dafür gesorgt das bei inaktivem Tab mit ungelesener Nachricht der Titel blinkt.
 * Außerdem wird ein Tab zu einem Offlinechatpartner ausgegraut. 
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
	 * @param owner Das ChatWindow zu dem der ChatWindowTab gehören soll.
	 */
	ChatWindowTab(String title, JTabbedPane parent, ChatWindow owner){
		// übergebenen Besitzer und Parent setzen:
		this.parent=parent;
		this.owner=owner;

		// JPanel durchsichtig machen und Flowlayout anpassen:
		this.setOpaque(false);
		((FlowLayout) this.getLayout()).setHgap(5);

		// Tab Titel setzen und Listener adden:
		this.lblTitle = new JLabel(owner.getChatWindowName());
		this.lblTitle.addMouseListener(new MyMouseListener());

		// ImageIcon für SchließenLabel erstellen:
		if(owner.getOnlineState()){
			this.tabCloseImgIcon = Help.getIcon("TabCloseBlack.png",10);
		} else {
			this.tabCloseImgIcon = Help.getIcon("TabCloseGray.png",10);
		}

		// SchließenLabel für Tabbeschriftung erzeugen und gestalten:
		this.lblClose = new JLabel(tabCloseImgIcon);

		// Observer für das Image auf das lblClose setzen:
		this.tabCloseImgIcon.setImageObserver(lblClose);

		// MouseListener für Schließenlabel (lblClose) hinzufügen:
		this.lblClose.addMouseListener(new MyMouseListener());

		// Das Icon für den Tab erzeugen und je nach ChatTyp (privat/gruppe) setzen:
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

		// Das Icon (lblIcon), das TitelLabel (lblTitle) + SchließenLabel (btnClose) zum Tab (pnlTab) hinzufügen:
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
	 * Diese Methode sorgt dafür das die Elemente des ChatWindowTabs grau werden
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
	 * Diese Methode sorgt dafür das die Elemente des ChatWindowTabs farbig werden
	 * wenn ein zuvor offline gegangener Chatpartner in einem privaten Chat wieder online kommt.
	 */
	void setOnline(){
		this.lblTitle.setForeground(Color.BLACK);
		this.tabCloseImgIcon.setImage(Help.getIcon("TabCloseBlack.png",10).getImage());
		this.lblIcon.setIcon(Help.getIcon("private.png"));
	}


	/**
	 * Diese Methode lässt den Tabtitel die Farbe wechseln.
	 * 
	 * Diese Methode sorg dafür das der Tabtitel (lblTitle) blinkt falls eine
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
	 * Diese Methode startet den Thread der für das Blinken zuständig ist
	 */
	public synchronized void startBlink() {
		if(blinker==null){
			blinker=new Blinker(500);
			blinker.start();
		}
	}

	/**
	 * Diese Methode stoppt den Thread der für das Blinken zuständig ist
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
	 * Diese Elementklasse stellt einen MouseAdapter für das ChatWindowTab bereit
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
				// Wird der Tabtitel mit der mittleren Maustaste gedrückt wird dieses ChatWindow geschlossen
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
			// Eventquelle Schließenlabel[X] (lblClose)
			if (e.getSource() == lblClose){
				// Ist die Maus über den lblClose und der Chatpartner online wird die Farbe orange
				if(owner.getOnlineState()){
					tabCloseImgIcon.setImage(Help.getIcon("TabCloseOrange.png",10).getImage());
					// Ist der Chatpartner offline wird die Farbe schwarz
				} else {
					tabCloseImgIcon.setImage(Help.getIcon("TabCloseBlack.png",10).getImage());
				}
			} else {
				JLabel source = (JLabel) e.getSource();
				// Ist die Maus über dem Tabtitel und der Chatpartner online wird der Titel orange
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
			// Eventquelle Schließenlabel[X] (lblClose)
			if (e.getSource() == lblClose){
				// Verläßt die Maus den lblClose und der Chatpartner ist online wird die Farbe orange 
				if(owner.getOnlineState()){
					tabCloseImgIcon.setImage(Help.getIcon("TabCloseBlack.png",10).getImage());
					// Ist der Chatpartner offline wird die Farbe grau
				} else {
					tabCloseImgIcon.setImage(Help.getIcon("TabCloseGray.png",10).getImage());
				}
			} else {
				JLabel source = (JLabel) e.getSource();
				// Verläßt die Maus den Tabtitel und der Chatpartner ist online wird der Titel orange
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
	 * Diese Klasse ermöglicht es dem ChatWindowTab zu blinken
	 * @author ATRM
	 *
	 */
	class Blinker extends Thread {
		int delay;
		volatile boolean active;

		/**
		 * Konstruktor für den BlinkerThread.
		 * 
		 * Dieser Konstruktor übernimmt ein int (delay) für die Verzögerung und
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
		// lässt den Thread starten und setzt aktive auf true
		public void run() {
			active = true;
			// lässt den Thread, solange aktiv, für die übergebene Zeit (delay) schlafen und weckt ihn wieder
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