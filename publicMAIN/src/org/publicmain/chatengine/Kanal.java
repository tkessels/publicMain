package org.publicmain.chatengine;
import java.util.Iterator;
import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;

import org.publicmain.common.MSG;

/**
 * @author ATRM
 * 
 */

public abstract class Kanal extends Observable {
	protected final Object referenz;

	//protected TreeSet<MSG> messages = new TreeSet<MSG>();

	public abstract boolean add(MSG nachricht);

	/**
	 * TODO: Kommentar
	 */
	public Kanal(Object reference) {
		this.referenz = reference;
	}

	/**
	 * Gibt die letzten x Nachrichten aus einem Kanal als Set zurück
	 * 
	 * @param count, die gewünschte Anzahl von Nachrichten
	 * @return, Set der x-Letzen Nachrichten
	 */
	/*public Set<MSG> getLastMSGs(int count) {
		if (messages.size() >= count) {
			Iterator<MSG> x = messages.descendingIterator();
			MSG last = messages.last();
			while (x.hasNext() && (count > 0)) {
				count--;
				last = ((MSG) x.next());
			}
			return messages.tailSet(last, true);
		}
		return messages;
	}*/
/*
	*//**
	 * Löscht alle Nachrichten aus dem Kanal.
	 *//*
	public void purgeMSGs() {
		messages.clear();
	}
	*/
	/**
	 * TODO: Kommentar
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((referenz == null) ? 0 : referenz.hashCode());
		return result;
	}

	/**
	 * TODO: Kommentar
	 */
	public boolean equals(Object obj) {
		return referenz.equals(obj);

	}

	/**
	 * TODO: Kommentar
	 */
	public Object getReferenz() {
		return referenz;
	}

	/**
	 * TODO: Kommentar
	 */
	public boolean is(Object vergleich) {
		return (this.referenz.equals(vergleich));
	}

	public String toString() {
		return referenz.toString();
	}
}
