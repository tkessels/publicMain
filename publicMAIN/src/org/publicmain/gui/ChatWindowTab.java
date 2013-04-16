/**
 * 
 */
package org.publicmain.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.images.Help;

/**
 * @author ABerthold
 *
 */
public class ChatWindowTab extends JPanel implements MouseListener{
	private JLabel lblTitle;
	private JLabel lblClose;
	private JLabel lblIcon;
	private ImageIcon tabCloseImgIcon;
	private JTabbedPane parent;
	private ChatWindow owner;
	private volatile Blinker blinker;

	public ChatWindowTab(String title, JTabbedPane parent, ChatWindow owner){
		// JPanel für Tabbeschriftung erzeugen und durchsichtig machen:
		this.parent=parent;
		this.owner=owner;
		((FlowLayout) this.getLayout()).setHgap(5);
		this.setOpaque(false);

		// TitelLabel für Tabbeschriftung erzeugen:
		this.lblTitle = new JLabel(owner.getChatWindowName());
		// MouseListener zu JLabel (lblTitle) hinzufügen:
		this.lblTitle.addMouseListener(this);

		// ImageIcon für SchließenLabel erstellen:
		if(owner.getOnlineState()){
			this.tabCloseImgIcon = Help.getIcon("TabCloseBlack.png");
		} else {
			this.tabCloseImgIcon = Help.getIcon("TabCloseGray.png");
		}
		// SchließenLabel für Tabbeschriftung erzeugen und gestalten:
		this.lblClose = new JLabel(tabCloseImgIcon);
		// Observer für das Image auf das lblClose setzen:
		this.tabCloseImgIcon.setImageObserver(lblClose);
		// MouseListener für Schließenlabel (lblClose) hinzufügen:
		this.lblClose.addMouseListener(this);

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
		
		// TitelLabel (lblTitle) + SchließenLabel (btnClose) zum Tab (pnlTab) hinzufügen:
		this.add(lblIcon);
		this.add(lblTitle);
		this.add(lblClose);

		// den neuen Tab an die Stelle von index setzen:
	}
	
	void updateAlias() {
		lblTitle.setText(owner.getChatWindowName());
	}
	

	/**
	 * 
	 */
	void setOffline(){
		this.lblTitle.setForeground(Color.GRAY);
		this.tabCloseImgIcon.setImage(Help.getIcon("TabCloseGray.png").getImage());
		this.lblIcon.setIcon(Help.getIcon("privateOffline.png"));
	}
	
	/**
	 * 
	 */
	void setOnline(){
		this.lblTitle.setForeground(Color.BLACK);
		this.tabCloseImgIcon.setImage(Help.getIcon("TaCloseBlack.png").getImage());
		this.lblIcon.setIcon(Help.getIcon("private.png"));
	}
	
	/**
	 * @param name
	 */
	void setTabTitle(String name){
		this.lblTitle.setText(name);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == lblTitle) {
			if (e.getModifiersEx() == 512) {
				GUI.getGUI().delChat(owner);
			} else {
				this.parent.setSelectedComponent(owner);
				stopBlink();
			}
		} else {
			GUI.getGUI().delChat(owner);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (e.getSource() == lblClose){
			if(owner.getOnlineState()){
				this.tabCloseImgIcon.setImage(Help.getIcon("TabCloseOrange.png").getImage());
			} else {
				this.tabCloseImgIcon.setImage(Help.getIcon("TabCloseBlack.png").getImage());
			}
		} else {
			JLabel source = (JLabel) e.getSource();
			if(owner.getOnlineState()){
				source.setForeground(new Color(255, 130, 13));
			} else {
				source.setForeground(Color.BLACK);
			}
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (e.getSource() == lblClose){
			if(owner.getOnlineState()){
				this.tabCloseImgIcon.setImage(Help.getIcon("TabCloseBlack.png").getImage());
			} else {
				this.tabCloseImgIcon.setImage(Help.getIcon("TabCloseGray.png").getImage());
			}
		} else {
			JLabel source = (JLabel) e.getSource();
			if(owner.getOnlineState()){
				source.setForeground(Color.BLACK);
			} else {
				source.setForeground(Color.GRAY);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}
	@Override
	public void mouseReleased(MouseEvent e) {
	}

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

	public synchronized void startBlink() {
		if(blinker==null){
			blinker=new Blinker(500);
			blinker.start();
		}
	}
	
	public void stopBlink(){
		if(blinker!=null){
			blinker.stopit();
			blinker=null;
		}
		lblTitle.setForeground(Color.BLACK);
	}
	
	class Blinker extends Thread {
		int delay;
		volatile boolean active;
		
		public Blinker(int delay) {
			this.delay = delay;
			active = false;
		}
		
		@Override
		public void run() {
			active = true;
			while (active) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
				}
				blink();
			}
		}
		
		public void stopit() {
			active = false;
		}
		

	}
}
	
	

