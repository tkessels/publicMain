package org.publicmain.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataListener;

import org.publicmain.common.Node;

public class UserComboModel implements ComboBoxModel<Node> {
	private ArrayList<Node> data;
	private ArrayList<ListDataListener> listener;
	private int selected;
	public UserComboModel() {
	data = new ArrayList<Node>();
	listener = new ArrayList<ListDataListener>();
	selected =0;
	}
	

	@Override
	public int getSize() {
		return data.size();
	}

	@Override
	public Node getElementAt(int index) {
		return data.get(index);

	}

	@Override
	public void addListDataListener(ListDataListener l) {
		listener.add(l);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listener.remove(l);
		
	}

	@Override
	public void setSelectedItem(Object anItem) {
		if(data.indexOf(anItem)>=0)selected=data.indexOf(anItem);
		
	}

	@Override
	public Object getSelectedItem() {
		return data.get(selected);
	}

	public void add(Node tmp_node) {
		data.add(tmp_node);
		
	}
	

	

}


