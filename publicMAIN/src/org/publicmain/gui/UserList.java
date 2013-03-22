package org.publicmain.gui;

import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JWindow;

/**
 * @author ATRM
 * 
 */

public class UserList extends JWindow {
	private int hoehe;
	private int breite;
	private JFrame parent;
	private JInternalFrame internalFrame;
	
	public UserList(JFrame parent) {
//		this.setLayout(new BorderLayout());
		this.parent=parent;
		this.internalFrame = new JInternalFrame("Userlist");
		this.hoehe = parent.getHeight();
		this.breite = 150;
		//TODO: prüfen ob man nicht besser ständig das selbe logo verwendet.
		internalFrame.setFrameIcon(new ImageIcon(getClass().getResource("g18050.png")));
		
		this.add(internalFrame);
		
		internalFrame.setVisible(true);
		
		parent.addComponentListener(new ComponentListener() {
			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
			}
			@Override
			public void componentMoved(ComponentEvent e) {
				repaint();
			}
			@Override
			public void componentResized(ComponentEvent e) {
				validate();
				repaint();
			}
			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub
			}
		});
	}

	@Override
	public void repaint() {
		Rectangle tmp=parent.getBounds();
		setBounds((int)(tmp.getX()-breite),(int)tmp.getY(), breite, tmp.height);
		super.repaint();
		
	}
	
}
