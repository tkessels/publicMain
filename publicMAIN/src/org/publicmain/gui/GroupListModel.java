package org.publicmain.gui;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.AbstractListModel;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.LogEngine;

/**
 * TODO: Kommentar
 * 
 * 
 * @author ATRM
 *
 */
public class GroupListModel extends AbstractListModel<String>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4321214633320536027L;
	private ArrayList<String> groups = new ArrayList<String>();
	private Thread groupListWriter;

	public GroupListModel() {
		this.groupListWriter = new Thread( new Runnable() {
			@Override
			public void run() {
				while ( true ) {
					groups.clear();
					groups.addAll( ChatEngine.getCE().getAllGroups() );
					fireContentsChanged( this, 0, groups.size() );
					synchronized ( ChatEngine.getCE().getAllGroups() ) {
						try {
							ChatEngine.getCE().getAllGroups().wait();
						} catch ( InterruptedException e ) {
							LogEngine.log(e);
						}
					}
				}
			}
		} );
		this.groupListWriter.start();

		Collections.sort( groups );
	}
	@Override
	public int getSize() {
		return groups.size();
	}

	public boolean contains( String group ){
		synchronized ( groups ) {
			return groups.contains( group );
		}
	}

	@Override
	public String getElementAt( int index ) {
		return groups.get( index );
	}
}
