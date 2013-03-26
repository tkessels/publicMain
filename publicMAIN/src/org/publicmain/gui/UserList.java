package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.text.StyledEditorKit.AlignmentAction;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.LogEngine;
import org.publicmain.common.Node;

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
		
		this.internalFrame.setLayout(new GridLayout(50,1));
		
		
		try {
//    		for(String grpName : ChatEngine.getCE().getGroupList()){
//    			this.internalFrame.add(new JLabel(grpName));
//    		}
			for(Node userAlias : ChatEngine.getCE().getUsers()){
				this.internalFrame.add(new JLabel(userAlias.getAlias()));
			}
		} catch (Exception e) {
			LogEngine.log(e);
		}
		
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
