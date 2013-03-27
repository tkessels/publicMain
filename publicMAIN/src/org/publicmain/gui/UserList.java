package org.publicmain.gui;

import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JWindow;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * @author ATRM
 * 
 */

public class UserList extends JWindow {
	private int hoehe;
	private int breite;
	private JFrame parent;
	private JInternalFrame internalFrame;
	private JList<String> users;
	
	public UserList(JFrame parent) {
		this.parent=parent;
		this.internalFrame = new JInternalFrame("Userlist");
		this.hoehe = parent.getHeight();
		this.breite = 150;
		this.internalFrame.setFrameIcon(new ImageIcon(getClass().getResource("g18050.png")));
		this.users = new JList<String>(new UserListModel(this));
		
		this.users.getModel().addListDataListener(new ListDataListener() {
			@Override
			public void intervalRemoved(ListDataEvent arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void intervalAdded(ListDataEvent arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void contentsChanged(ListDataEvent arg0) {
			}
		});
		
		this.internalFrame.add(users);
		
		this.add(internalFrame);
		
		this.internalFrame.setVisible(true);
		
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

