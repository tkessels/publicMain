package org.publicmain.gui;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.AbstractListModel;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.LogEngine;
import org.publicmain.common.Node;

public class UserListModel extends AbstractListModel<String>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3915185276474553682L;
	private ArrayList<String> users = new ArrayList<String>();

	private Thread userListWriter;
	private static final long TIMEOUT = 1000;
	private int userListLength;
	
	public UserListModel() {
    	
    	userListLength = 0;
    	
		userListWriter = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {

					// for(String grpName : ChatEngine.getCE().getGroupList()){
					// users.add(grpName);
					// }
					// for (Node userAlias : ChatEngine.getCE().getUsers()) {
					// users.add(userAlias.getAlias());
					//
					// }

					synchronized (ChatEngine.getCE().getUsers()) {
						try {
							ChatEngine.getCE().getUsers().wait();
							System.out.println("Änderung");
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
			}
		});
    	//userListWriter.setDaemon(true);
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
