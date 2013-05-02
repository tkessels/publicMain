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
import java.awt.event.MouseAdapter;
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

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.Config;
import org.publicmain.common.Node;
import org.resources.Help;

/**
 * Diese Klasse stellt die Kontaktliste zur Verfügung
 * 
 * Diese Klasse 
 * 
 * @author ATRM
 * 
 */
public class ContactList extends JWindow {
	
	private int breite;
	
	private JFrame parent;
	
	private JInternalFrame internalFrame;
	
	private JScrollPane groupsScroller;
	private JPanel groupPanel;
	private JList<String> groups;
	private JButton createGrp;
	
	private JSplitPane trenner;
	
	private JScrollPane usersScroller;
	private JPanel userPanel;
	private JList<Node> users;
	
	public ContactList(JFrame parent) {
		this.parent			= parent;
		this.internalFrame	= new JInternalFrame("contacts");
		this.breite			= 150;
		this.users			= new JList<Node>(new UserListModel());
		this.groups			= new JList<String>(new GroupListModel());
		this.usersScroller	= new JScrollPane(users, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.groupsScroller = new JScrollPane(groups, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.groupPanel		= new JPanel(new BorderLayout());
		this.createGrp		= new JButton("Create Group");
		this.userPanel		= new JPanel(new BorderLayout());
		this.trenner		= new JSplitPane(JSplitPane.VERTICAL_SPLIT);
         
		this.internalFrame.setLayout(new BorderLayout());
		this.internalFrame.setFrameIcon(Help.getIcon("g18050.png"));
		
		this.groupPanel.setPreferredSize(new Dimension(breite, parent.getHeight()/4));
		this.groupPanel.setBorder(BorderFactory.createTitledBorder("GROUPS"));
		this.groupPanel.setBackground(Color.WHITE);
		
		this.userPanel.setBorder(BorderFactory.createTitledBorder("USERS"));
		this.userPanel.setBackground(Color.WHITE);
		
		this.users.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		this.users.setLayoutOrientation(JList.VERTICAL);
		this.users.setVisibleRowCount(-1);
		this.users.setCellRenderer(new UsersListCellRenderer());
		
		this.groups.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		this.groups.setLayoutOrientation(JList.VERTICAL);
		this.groups.setVisibleRowCount(-1);
		this.groups.setCellRenderer(new GroupsListCellRenderer());
		
		// Listener adden
		this.users.addMouseListener(new MyMouseListener());
		this.groups.addMouseListener(new MyMouseListener());
		this.createGrp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String tmpGrpName = null;
				tmpGrpName = (String)JOptionPane.showInputDialog(GUI.getGUI(), "Enter Groupname", "Groupname", JOptionPane.OK_CANCEL_OPTION, Help.getIcon("gruppe.png"), null, null);
				if(tmpGrpName!=null && GUI.getGUI().checkName(tmpGrpName,0)){
					tmpGrpName = GUI.getGUI().confName(tmpGrpName, true);
					GUI.getGUI().addGrpCW(tmpGrpName, true);
				} else {
					GUI.getGUI().info("Illegal charakter in groupname!<br>Allowed charakters: a-z,A-Z,0-9,ö,ä,ü,Ö,Ä,Ü,ß,é,á,-,_", null, 2);
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
	
	public boolean groupExists(String name){
		return ((GroupListModel)groups.getModel()).contains(name);
	}

	public boolean aliasExists(String alias) {
		return ((UserListModel)users.getModel()).contains(alias);
	}
	
	@Override
	public void repaint() {
		Rectangle tmp=parent.getBounds();
		setBounds((int)(tmp.getX()-breite),(int)tmp.getY(), breite, tmp.height);
		super.repaint();
	}
	
	class MyMouseListener extends MouseAdapter {

		public void mouseClicked(MouseEvent e) {
			JList source = (JList) e.getSource();
			// Doppelklick:
			if (e.getClickCount() == 2) {
				Object o = source.getSelectedValue();
				if (source.getModel().getClass().getSimpleName().startsWith("Group")) {
					GUI.getGUI().addGrpCW(o.toString(), true);
				}
				if (source.getModel().getClass().getSimpleName().startsWith("User")) {
					if (!(users.getSelectedValue().getUserID() == ChatEngine.getCE().getUserID())) {
						GUI.getGUI().addPrivCW(((Node) o).getUserID(), true);
					}
				}
			}
			// rechte Maustaste:
			if ( e.getModifiersEx() == 256){
				int index = source.locationToIndex(e.getPoint());
				if((index >= 0)&&(index<source.getModel().getSize())){
					source.setSelectedIndex(index);
					Object o = source.getModel().getElementAt(index);
//					if(source.getModel().getClass().getSimpleName().startsWith("User")){
					if(o instanceof Node){
						Node tmp = (Node)o;
						PopupUser popupUsr = new PopupUser(new popupListener(tmp.getAlias()));
						popupUsr.show(source, e.getX(), e.getY());
					}
					if(source.getModel().getClass().getSimpleName().startsWith("Group")){
						PopupGroup popupGrp = new PopupGroup(o.toString(), new popupListener(o.toString()));
						popupGrp.show(source, e.getX(), e.getY());
					}
					
					
				}
			}
		}
	}

	class PopupUser extends JPopupMenu{
		
		private JMenuItem whisper;
		private JMenuItem sendFile;
		private JMenuItem ignore;
		private JMenuItem unignore;
		private JMenuItem info;
		private JMenuItem changeAlias;
		private JMenuItem afkStatus;
		
		public PopupUser(ActionListener popupListener){
			String user=users.getSelectedValue().getAlias();
			long userID = users.getSelectedValue().getUserID();

			this.whisper 	 = new JMenuItem("whisper to " + user);
			this.sendFile 	 = new JMenuItem("send File to " + user);
			this.ignore 	 = new JMenuItem("ignore " + user);
			this.unignore	 = new JMenuItem("unignore " + user);
			this.info 		 = new JMenuItem("info's about " + user);
			this.changeAlias = new JMenuItem("change Alias");
			this.afkStatus	 = new JMenuItem("AFK (on/off)");
			
			this.whisper.addActionListener(popupListener);
			this.sendFile.addActionListener(popupListener);
			this.ignore.addActionListener(popupListener);
			this.unignore.addActionListener(popupListener);
			this.info.addActionListener(popupListener);
			this.changeAlias.addActionListener(popupListener);
			this.afkStatus.addActionListener(popupListener);
			
			if (!(userID==ChatEngine.getCE().getUserID())) {
				this.add(whisper);
				this.add(new Separator());
				this.add(sendFile);
				this.add(new Separator());
				if(!ChatEngine.getCE().is_uid_ignored(userID)){
					this.add(ignore);
				} else {
					this.add(unignore);
				}
				this.add(new Separator());
			} else {
				this.add(changeAlias);
				this.add(afkStatus);
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
				GUI.getGUI().getActiveCW().printInfo(users.getSelectedValue());
			}
			else if(source.getText().startsWith("send")){
				GUI.getGUI().sendFile(users.getSelectedValue().getUserID());
			}
			else if(source.getText().startsWith("AFK")){
				GUI.getGUI().afk();
			}
			else if(source.getText().startsWith("change")){
				String tmpAlias = null;
				tmpAlias = (String)JOptionPane.showInputDialog(GUI.getGUI(), "Enter new Alias", "Change Alias", JOptionPane.OK_CANCEL_OPTION, Help.getIcon("private.png"), null, null);
				if(tmpAlias != null && !tmpAlias.equals("")){
					GUI.getGUI().changeAlias(tmpAlias);
				} 
			}
		}
	}
	
	//Experimentel 
	class UsersListCellRenderer extends JLabel implements ListCellRenderer {
        public UsersListCellRenderer() {
            setOpaque(true);
        }
        public Component getListCellRendererComponent(JList paramlist, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
			//die Anzeige des Alias in der Userliste anpassen:
			String tmpText = value.toString();
			String[] cutText = tmpText.split("@", 2);
			setText(cutText[0]);
			Node tmp = (Node) value;
			if (ChatEngine.getCE().is_ignored(tmp.getNodeID())){
				setText(cutText[0] + " (ignored)");
				setForeground(Color.RED);
			} else if(ChatEngine.getCE().getMyNodeID()==tmp.getNodeID() && GUI.getGUI().isAFK()){
				setText(cutText[0] + " (afk)");
				setForeground(Color.GRAY);
			}
			else if (ChatEngine.getCE().getMyNodeID() == tmp
					.getNodeID())
				setForeground(new Color(255, 133, 18));
			else
				setForeground(Color.BLACK);
			
			
			if (isSelected)
				setBackground(new Color(25, 169, 241));
			else
				setBackground(Color.WHITE);
			if (cellHasFocus)
				setBackground(getBackground().darker());
			return this;
			
        }
    }
	
	class GroupsListCellRenderer extends JLabel implements ListCellRenderer {
		public GroupsListCellRenderer() {
			setOpaque(true);
		}
		public Component getListCellRendererComponent(JList paramlist, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			String tmpText = value.toString();
			setText(tmpText);
			if( tmpText.equals("public")){
				setForeground(new Color(255, 133, 18));
			} else {
				setForeground(Color.BLACK);
			}
			
			
			if (isSelected){
				setBackground(new Color(25, 169, 241));
			} else {
				setBackground(Color.WHITE);
			}
			if (cellHasFocus) {
				setBackground(getBackground().darker());
			}
			return this;
		}
		
	}
}
