package org.publicmain.chatengine;

import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.NachrichtenTyp;

/**
 * @author ATRM
 * 
 */

public class GruppenKanal extends Kanal {

	/**
	 * Konstruktor für den GruppenKanal
	 */
	public GruppenKanal(String gruppe) {
		super(gruppe.toLowerCase());
	}

	/**
	 * Liefert den Namensstring des Gruppenkanals als Hash-Wert.
	 */
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * Nachricht zu dem instanzierten Gruppenkanal hinzufügen
	 *   
	 */
	public boolean add(MSG nachricht) {
		if ((nachricht.getTyp() == NachrichtenTyp.GROUP)
				&& nachricht.getGroup().equals(referenz)) {
			LogEngine.log(this, "[" + referenz + "]" + countObservers() + ":",
					nachricht);
			//	messages.add(nachricht);
			setChanged();
			notifyObservers(nachricht);
			return true;
		}
		return false;
	}
}