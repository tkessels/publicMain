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
		if ((nachricht.getSender() == (long) referenz)
				|| (nachricht.getEmpfänger() == (long) referenz)) {
		//	messages.add(nachricht);
			setChanged();
			notifyObservers(nachricht);
			return true;
		}
		return false;
	}

}
