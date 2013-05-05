package org.publicmain.gui;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.AbstractListModel;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.LogEngine;

/**
 * Diese Klasse stellt ein AbstractListModel bereit
 * 
 * Diese Klasse stellt das AbstractListModel für die Gruppenliste zur
 * Verfügung 
 * 
 * @author ATRM
 *
 */
public class GroupListModel extends AbstractListModel<String>{

	private static final long serialVersionUID = 4321214633320536027L;
	private ArrayList<String> groups = new ArrayList<String>();
	private Thread groupListWriter;

	/**
	 * Konstruktor für das GroupListModel.
	 * 
	 * Der Konstruktor für das GroupListModel startet einen Thread
	 * welcher den Inhalt der Liste aktuell halten soll und somit nur
	 * aktuell existierende Gruppen anzeigt.
	 */
	public GroupListModel() {
		// Initialisieren des Threads
		this.groupListWriter = new Thread( new Runnable() {
			
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				// Endlosschleife
				while ( true ) {
					// die ArrayList<String> der Gruppen (groups) leeren
					groups.clear();
					// die Liste befüllen
					groups.addAll( ChatEngine.getCE().getAllGroups() );
					// hier wird der Liste die dieses Model hält 
					// bescheid gegeben das sich was geändert hat
					fireContentsChanged( this, 0, groups.size() );
					// synchronized Block für geregelten Zugriff auf die Schlüsselvariable .getAllGroups()
					synchronized ( ChatEngine.getCE().getAllGroups() ) {
						try {
							ChatEngine.getCE().getAllGroups().wait();
						} catch ( InterruptedException e ) {
							LogEngine.log(e);
						}
					}
				}
			}//eom run()
		} );
		// starten des Threads
		this.groupListWriter.start();

		// den Inhalt der Liste sortieren
		Collections.sort( groups );
	} //eom GroupListModel()
	
	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return groups.size();
	} //eom getSize()

	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public String getElementAt( int index ) {
		return groups.get( index );
	} //eom getElementAt ( int index )
	
	/**
	 * Diese Methode prüft ob eine Gruppe vorhanden ist.
	 * 
	 * Diese Methode prüft ob ein übergebener String (group) bereits in
	 * der Liste (groups) enthalten ist. Ist dies der Fall wird true 
	 * zurückgegeben falls nicht wird false zurückgegeben.
	 * 
	 * @param group String name der Gruppe
	 * @return boolean true wenn enthalten, false wenn nicht enthalten.
	 */
	boolean contains( String group ){
		synchronized ( groups ) {
			return groups.contains( group );
		}
	} //eom contains( String group )
} //eoc GroupListModel