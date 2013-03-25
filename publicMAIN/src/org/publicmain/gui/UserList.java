package org.publicmain.gui;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JWindow;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.images.Help;

/**
 * @author ATRM
 * 
 */

public class UserList extends JWindow {
	private int hoehe;
	private int breite;
	private JFrame parent;
	private JInternalFrame internalFrame;
	private JList users;
	
	public UserList(JFrame parent) {
		this.parent=parent;
		this.internalFrame = new JInternalFrame("Userlist");
		this.hoehe = parent.getHeight();
		this.breite = 150;
		this.internalFrame.setFrameIcon(new ImageIcon(getClass().getResource("g18050.png")));
		this.users = new JList(new UserListModel());
		
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
