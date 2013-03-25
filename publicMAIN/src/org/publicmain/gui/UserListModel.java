package org.publicmain.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.LogEngine;
import org.publicmain.common.Node;

public class UserListModel extends AbstractListModel{

	private List users = new ArrayList();

    public UserListModel() {
    	
//    	try {
//    		for(String grpName : ChatEngine.getCE().getGroupList()){
//    			users.add(grpName);
//    		}
//			for(Node userAlias : ChatEngine.getCE().getUsers()){
//				users.add(userAlias.getAlias());
//			}
//		} catch (Exception e) {
//			LogEngine.log(e);
//		}
    	
    	users.add("test");
    	
	    Collections.sort(users);
    }

    @Override
    public int getSize() {
    	return users.size();
    }

    @Override
    public Object getElementAt(int index) {
    return users.get(index);
    }

	
}
