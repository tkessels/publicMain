package org.publicmain.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.LogEngine;
import org.publicmain.common.Node;

/**
 * @author ATRM
 *
 */

public class UserListModel extends AbstractListModel<Node>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3915185276474553682L;
	private List<Node> users;
	private Thread userListWriter;
	
	public UserListModel() {
		users = new ArrayList<Node>();
		this.userListWriter = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					users.clear();
					users.addAll(ChatEngine.getCE().getUsers());
					fireContentsChanged(this, 0, users.size());
					synchronized (ChatEngine.getCE().getUsers()) {
						try {
							ChatEngine.getCE().getUsers().wait();
						} catch (InterruptedException e) {
							LogEngine.log(e);
						}
					}
				}
			}
		});
    	userListWriter.start();
    	
	    Collections.sort(users, new Comparator<Node>() {

			@Override
			public int compare(Node o1, Node o2) {
				return o1.getAlias().compareTo(o2.getAlias());
			}
		});
    }
    @Override
    public int getSize() {
    	return users.size();
    }
    
    public boolean contains(String user){
    	synchronized (users) {
    		return users.contains(user);
		}
    }
    
    @Override
    public Node getElementAt(int index) {
    	return users.get(index);
    }
}
