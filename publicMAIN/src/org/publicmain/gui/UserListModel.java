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
 * Diese Klasse stellt ein AbstractListModel bereit.
 * 
 * Diese Klasse stellt das AbstractListModel für die Userliste zur Verfügung.
 * 
 * @author ATRM
 *
 */
public class UserListModel extends AbstractListModel<Node>{

	private static final long serialVersionUID = 3915185276474553682L;
	private List<Node> users;
	private Thread userListWriter;

	/**
	 * Konstruktor für das UserListModel.
	 * 
	 * Der Konstruktor für das UserListModel startet einen Thread welcher den
	 * Inhalt der Liste aktuell halten soll und somit nur aktuell erreichbare
	 * User anzeigt.
	 */
	public UserListModel() {
		// Initialisierungen:
		users = new ArrayList<Node>();
		this.userListWriter = new Thread( new Runnable() {
			
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				//Endlosschleife
				while ( true ) {
					//Liste leeren
					users.clear();
					//und befüllen
					users.addAll( ChatEngine.getCE().getUsers() );
					// hier wird der Liste die dieses Model hält
					// bescheid gegeben das sich was geändert hat
					fireContentsChanged( this, 0, users.size() );
					// synchronized Block für geregelten Zugriff auf die
					// Schlüsselvariable .getUsers()
					synchronized ( ChatEngine.getCE().getUsers() ) {
						try {
							ChatEngine.getCE().getUsers().wait();
						} catch ( InterruptedException e ) {
							LogEngine.log(e);
						}
					}
				}
			}//eom run()
		});
		// Thread starten
		userListWriter.start();

		//Liste sortieren
		Collections.sort( users, new Comparator<Node>() {

			/**
			 * Diese Methode vergleicht die Nodes die in der Liste enthalten sind.
			 * 
			 * Diese Methode vergleicht die Nodes die in der Liste enthalten sind somit ist
			 * eine Sortierung nach Namen möglich.
			 * 
			 * @param o1 Node vergleiche ein Node
			 * @param o2 Node mit dem anderen Node
			 * @return int
			 */
			@Override
			public int compare( Node o1, Node o2 ) {
				return o1.getAlias().compareTo( o2.getAlias() );
			}//eom compare()
		});
	}//eom UserListModel
	
	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return users.size();
	}//eom getSize()

	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public Node getElementAt( int index ) {
		return users.get( index );
	}//eom getElementAt()
	
	/**
	 * @param user
	 * @return
	 */
	public boolean contains( String user ){
		synchronized ( users ) {
			return users.contains(user);
		}
	}//eom contains()
}//eoc UserListModel
