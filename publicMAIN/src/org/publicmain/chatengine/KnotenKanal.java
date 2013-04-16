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
	 * Nachricht zu dem instanzierten Knotenkanal hinzuf�gen
	 *   
	 */
	public boolean add(MSG nachricht) {
		if ((nachricht.getSender() == (long) referenz)
				|| (nachricht.getEmpf�nger() == (long) referenz)) {
		//	messages.add(nachricht);
			setChanged();
			notifyObservers(nachricht);
			return true;
		}
		return false;
	}

}
