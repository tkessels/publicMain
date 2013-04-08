package org.publicmain.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.LogEngine;
import org.publicmain.common.Node;

/**
 * @author ATRM
 *
 */

public class UserListModel extends AbstractListModel<String>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3915185276474553682L;
	private List<String> users = (new ArrayList<String>());
	private Thread userListWriter;
	
	public UserListModel() {
		this.userListWriter = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					users.clear();
					for (Node userAlias : ChatEngine.getCE().getUsers()) {
						users.add(userAlias.getAlias());
					}
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
    	
	    Collections.sort(users);
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
    public String getElementAt(int index) {
    return users.get(index);
    }
}
