package org.publicmain.chatengine;
import java.util.Observable;

import org.publicmain.common.MSG;

/**
 * Abstrakte Klasse von welcher weitere Kanäle abgeleitet werden können.
 * 
 * @author ATRM
 * 
 */

public abstract class Kanal extends Observable {
	protected final Object referenz;

//	Ggf. für die weitere Entwicklung benötigt.
//	protected TreeSet<MSG> messages = new TreeSet<MSG>();

	/**
	 * Abstrakte Methode zum hinzufügen einer Nachricht zu einem Kanal.
	 * 
	 * @param nachricht
	 * @return boolean
	 */
	public abstract boolean add(MSG nachricht);

	/**
	 * TODO: Prüfen ob die Grütze stimmt, die ich hier zu besten gebe. Hält eine
	 * Referenz auf diese Instanz des Kanals.
	 * 
	 * @param reference
	 */
	public Kanal(Object reference) {
		this.referenz = reference;
	}

//	 Ggf. für die weitere Entwicklung benötigt.
//	/**
//	 * Gibt die letzten x Nachrichten aus einem Kanal als Set zurück
//	 * 
//	 * @param count, die gewünschte Anzahl von Nachrichten
//	 * @return, Set der x-Letzen Nachrichten
//	 */
//	public Set<MSG> getLastMSGs(int count) {
//		if (messages.size() >= count) {
//			Iterator<MSG> x = messages.descendingIterator();
//			MSG last = messages.last();
//			while (x.hasNext() && (count > 0)) {
//				count--;
//				last = ((MSG) x.next());
//			}
//			return messages.tailSet(last, true);
//		}
//		return messages;
//	}
	
//	 Ggf. für die weitere Entwicklung benötigt.
//	/**
//	 * Löscht alle Nachrichten aus dem Kanal.
//	 */
//	public void purgeMSGs() {
//		messages.clear();
//	}

	/**
	 * Generiert einen Hashwert von diesem Kanal.
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((referenz == null) ? 0 : referenz.hashCode());
		return result;
	}

	/**
	 * Diese Methode überläd equals-Methode um die Kanalzuordnung prüfen zu
	 * können.
	 */
	public boolean equals(Object obj) {
		return referenz.equals(obj);

	}

	/**
	 * Getter für die Referenz.
	 */
	public Object getReferenz() {
		return referenz;
	}

	/**
	 * Benutzt die equals-Methode um die Kanalzuordnung prüfen zu können und
	 * liefert entsprechend <code>true</code> oder <code>false</code> zurück.
	 */
	public boolean is(Object vergleich) {
		return (this.referenz.equals(vergleich));
	}

	/**
	 * Eigene toString-Methode.
	 */
	public String toString() {
		return referenz.toString();
	}
}
