package org.publicmain.chatengine;

import org.publicmain.common.MSG;

/**
 * @author ATRM
 * 
 */

public class KnotenKanal extends Kanal {

	/**
	 * TODO: Kommentar
	 */
	public KnotenKanal(long node) {
		super(node);
	}

	/**
	 * Nachricht zu dem instanzierten Knotenkanal hinzufügen
	 *   
	 */
	public boolean add(MSG nachricht) {
		 
		long sender_userID = ChatEngine.getCE().getNodeForNID(nachricht.getSender()).getUserID();
		long empf_userID = ChatEngine.getCE().getNodeForNID(nachricht.getEmpfänger()).getUserID();
		if ((sender_userID == (long) referenz) || (empf_userID== (long) referenz)) 
		{
		//	messages.add(nachricht);
			setChanged();
			notifyObservers(nachricht);
			return true;
		}
		return false;
	}

}
