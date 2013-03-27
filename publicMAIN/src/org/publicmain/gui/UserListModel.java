package org.publicmain.gui;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.LogEngine;
import org.publicmain.common.Node;

public class UserListModel extends DefaultListModel<String>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3915185276474553682L;
	private ArrayList<String> users = new ArrayList<String>();
	private Thread userListWriter;
	private UserList parent;
	
	public UserListModel(final UserList parent) {
    	this.parent = parent;
		this.userListWriter = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					// for(String grpName : ChatEngine.getCE().getGroupList()){
					// users.add(grpName);
					// }
					users.clear();
					for (Node userAlias : ChatEngine.getCE().getUsers()) {
						users.add(userAlias.getAlias());
					}
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
    	
	    Collections.sort(users);
    }

    @Override
    public int getSize() {
    	return users.size();
    }

    @Override
    public String getElementAt(int index) {
    return users.get(index);
    }
    
   
    
}
