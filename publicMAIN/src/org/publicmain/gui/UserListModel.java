package org.publicmain.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;

public class UserListModel extends AbstractListModel{

	private List users = new ArrayList();

    public UserListModel() {
	    users.add("test1");
	    users.add("test4");
	    users.add("test2");
	    users.add("test3");
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
