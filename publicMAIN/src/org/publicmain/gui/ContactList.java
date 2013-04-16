package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JWindow;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import org.images.Help;
import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.Node;

/**
 * @author ATRM
 * 
 */
public class ContactList extends JWindow {
	private int breite;
	private JFrame parent;
	private JInternalFrame internalFrame;
	private JScrollPane usersScroller;
	private JScrollPane groupsScroller;
	private JPanel groupPanel;
	private JButton createGrp;
	private JPanel userPanel;
	private JList<Node> users;
	private JList<String> groups;
	private JSplitPane trenner;
	
	public ContactList(JFrame parent) {
		this.parent=parent;
		this.internalFrame = new JInternalFrame("contacts");
		this.breite = 150;
		this.internalFrame.setFrameIcon(new ImageIcon(Help.class.getResource("g18050.png")));
		this.users = new JList<Node>(new UserListModel());
		this.groups = new JList<String>(new GroupListModel());
		this.usersScroller = new JScrollPane(users, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.groupsScroller = new JScrollPane(groups, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.groupPanel = new JPanel(new BorderLayout());
		this.createGrp = new JButton("create Group");
		this.userPanel = new JPanel(new BorderLayout());
		this.trenner = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
         
		this.internalFrame.setLayout(new BorderLayout());
		
		this.groupPanel.setBorder(BorderFactory.createTitledBorder("GROUPS"));
		this.userPanel.setBorder(BorderFactory.createTitledBorder("USERS"));
		
		this.groupPanel.setPreferredSize(new Dimension(breite, parent.getHeight()/4));
		
		this.users.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		this.users.setLayoutOrientation(JList.VERTICAL);
		this.users.setVisibleRowCount(-1);
		//this.users.setCellRenderer(new MyListCellRenderer());
		
		this.groups.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		this.groups.setLayoutOrientation(JList.VERTICAL);
		this.groups.setVisibleRowCount(-1);
		
		// Listener adden
		this.users.addMouseListener(new myMouseAdapter());
		this.groups.addMouseListener(new myMouseAdapter());
		this.createGrp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//TODO: hier nochmal überprüfen!!! und evtl trim und upper oder lowercase...
				String tmpGrpName = null;
				tmpGrpName = (String)JOptionPane.showInputDialog(GUI.getGUI(), "Enter Groupname", "Groupname", JOptionPane.OK_CANCEL_OPTION, new ImageIcon(Help.class.getResource("gruppe.png")), null, null);
				if(tmpGrpName!=null && !tmpGrpName.equals("")){
					GUI.getGUI().addGrpCW(tmpGrpName, true);
				} 
				
			}
		});
		
		// adden der Komponenten
		this.groupPanel.add(groupsScroller, BorderLayout.CENTER);
		this.groupPanel.add(createGrp, BorderLayout.SOUTH);
		
		this.userPanel.add(usersScroller, BorderLayout.CENTER);
		
		this.trenner.add(groupPanel);
		this.trenner.add(userPanel);

		this.internalFrame.add(trenner, BorderLayout.CENTER);
		
		this.add(internalFrame);
		
		this.internalFrame.setVisible(true);
		
		parent.addComponentListener(new ComponentListener() {
			@Override
			public void componentHidden(ComponentEvent e) {
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
			}
		});
	}
	
	public boolean nameExists(String name){
		return (((UserListModel)users.getModel()).contains(name) || ((GroupListModel)groups.getModel()).contains(name));
	}


	@Override
	public void repaint() {
		Rectangle tmp=parent.getBounds();
		setBounds((int)(tmp.getX()-breite),(int)tmp.getY(), breite, tmp.height);
		super.repaint();
	}
	
	class myMouseAdapter implements MouseListener {

		public void mouseClicked(MouseEvent e) {
			JList<String> source = (JList<String>) e.getSource();
			// Doppelklick:
			if (e.getClickCount() == 2) {
				int index = source.locationToIndex(e.getPoint());
				if (index >= 0) {
					Object o = source.getModel().getElementAt(index);
					if (source.getModel().getClass().getSimpleName().startsWith("Group")) {
						GUI.getGUI().addGrpCW(o.toString(), true);
					}
					if (source.getModel().getClass().getSimpleName().startsWith("User")) {
						if (!(users.getSelectedValue().getUserID() == ChatEngine.getCE().getUserID())) {
							GUI.getGUI().addPrivCW(((Node) o).getUserID(), true);
						}
					}
				}
			}
			// rechte Maustaste:
			if ( e.getModifiersEx() == 256){
				int index = source.locationToIndex(e.getPoint());
				if(index >= 0){
					source.setSelectedIndex(index);
					Object o = source.getModel().getElementAt(index);
					if(source.getModel().getClass().getSimpleName().startsWith("User")){
						PopupUser popupUsr = new PopupUser(o.toString(), new popupListener(o.toString()));
						popupUsr.show(source, e.getX(), e.getY());
					}
					if(source.getModel().getClass().getSimpleName().startsWith("Group")){
						PopupGroup popupGrp = new PopupGroup(o.toString(), new popupListener(o.toString()));
						popupGrp.show(source, e.getX(), e.getY());
					}
					
					
				}
			}
		}
		@Override
		public void mouseEntered(MouseEvent e) {
		}
		@Override
		public void mouseExited(MouseEvent e) {
		}
		@Override
		public void mousePressed(MouseEvent e) {
		}
		@Override
		public void mouseReleased(MouseEvent e) {
		}
	}

	class PopupUser extends JPopupMenu{
		
		private JMenuItem whisper;
		private JMenuItem sendFile;
		private JMenuItem ignore;
		private JMenuItem unignore;
		private JMenuItem info;
		private String user;
		
		public PopupUser(String user, ActionListener popupListener){
			this.user = user;
			this.whisper = new JMenuItem("whisper to " + user);
			this.sendFile = new JMenuItem("send File to " + user);
			this.ignore = new JMenuItem("ignore " + user);
			this.unignore = new JMenuItem("unignore " + user);
			this.info = new JMenuItem("info's about " + user);
			
			this.whisper.addActionListener(popupListener);
			this.sendFile.addActionListener(popupListener);
			this.ignore.addActionListener(popupListener);
			this.unignore.addActionListener(popupListener);
			this.info.addActionListener(popupListener);
			
			if (!(users.getSelectedValue().getUserID()==ChatEngine.getCE().getUserID())) {
				this.add(whisper);
				this.add(new Separator());
				this.add(sendFile);
				this.add(new Separator());
				this.add(ignore);
				this.add(unignore);
				this.add(new Separator());
			}
			this.add(info);
		}
	
	}
	
	class PopupGroup extends JPopupMenu{
		
		private JMenuItem join;
		private JMenuItem leave;
		private String group;
		
		public PopupGroup(String group, ActionListener popupListener){
			this.group = group;
			this.join = new JMenuItem("join " + group);
			this.leave = new JMenuItem("leave " + group);
			
			this.join.addActionListener(popupListener);
			this.leave.addActionListener(popupListener);
			
			this.add(join);
			this.add(leave);
		}
	}
	
	class popupListener implements ActionListener{
		
		private String chatname;
		
		public popupListener(String chatname){
			this.chatname = chatname;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JMenuItem source = (JMenuItem)e.getSource();
			
			if(source.getText().startsWith("join")){
				GUI.getGUI().addGrpCW(chatname, true);
			}
			else if(source.getText().startsWith("leave")){
				GUI.getGUI().delChat(chatname);
			}
			else if(source.getText().startsWith("whisper")){
				
//				GUI.getGUI().addPrivCW(ChatEngine.getCE().getNodeforAlias(chatname).getUserID());
				GUI.getGUI().addPrivCW(users.getSelectedValue().getUserID(), true);
			}
			else if(source.getText().startsWith("ignore")){
				GUI.getGUI().ignoreUser(users.getSelectedValue().getUserID());
			}
			else if(source.getText().startsWith("unignore")){
				GUI.getGUI().unignoreUser(users.getSelectedValue().getUserID());
			}
			else if(source.getText().startsWith("info")){
				//TODO: CODE HERE
			}
			else if(source.getText().startsWith("send")){
				GUI.getGUI().sendFile(users.getSelectedValue().getUserID());
			}
		}
	}
	
	//Experimentel 
	class MyListCellRenderer extends JLabel implements ListCellRenderer {
	        public MyListCellRenderer() {
	            setOpaque(true);
	        }
	        public Component getListCellRendererComponent(JList paramlist, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	            setText(value.toString());
	            Node tmp = (Node) value;
	            if (ChatEngine.getCE().is_ignored(tmp.getNodeID()))setForeground(new Color(255, 133,18));
	            else setForeground(Color.BLACK);
	            if(isSelected) setBackground(new Color(25, 169, 241));
	            else setBackground(Color.WHITE);
	            if(cellHasFocus)setBackground(getBackground().darker());
	            return this;
	        }
	    }
}
