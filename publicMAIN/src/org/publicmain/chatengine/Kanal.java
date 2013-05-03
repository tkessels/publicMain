package org.publicmain.chatengine;
import java.util.Observable;

import org.publicmain.common.MSG;

/**
 * Abstrakte Klasse von welcher weitere Kan�le abgeleitet werden k�nnen.
 * 
 * @author ATRM
 * 
 */

public abstract class Kanal extends Observable {
	protected final Object referenz;

//	Ggf. f�r die weitere Entwicklung ben�tigt.
//	protected TreeSet<MSG> messages = new TreeSet<MSG>();

	/**
	 * Abstrakte Methode zum hinzuf�gen einer Nachricht zu einem Kanal.
	 * 
	 * @param nachricht
	 * @return boolean
	 */
	public abstract boolean add(MSG nachricht);

	/**
	 * TODO: Pr�fen ob die Gr�tze stimmt, die ich hier zu besten gebe. H�lt eine
	 * Referenz auf diese Instanz des Kanals.
	 * 
	 * @param reference
	 */
	public Kanal(Object reference) {
		this.referenz = reference;
	}

//	 Ggf. f�r die weitere Entwicklung ben�tigt.
//	/**
//	 * Gibt die letzten x Nachrichten aus einem Kanal als Set zur�ck
//	 * 
//	 * @param count, die gew�nschte Anzahl von Nachrichten
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
	
//	 Ggf. f�r die weitere Entwicklung ben�tigt.
//	/**
//	 * L�scht alle Nachrichten aus dem Kanal.
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
	 * Diese Methode �berl�d equals-Methode um die Kanalzuordnung pr�fen zu
	 * k�nnen.
	 */
	public boolean equals(Object obj) {
		return referenz.equals(obj);

	}

	/**
	 * Getter f�r die Referenz.
	 */
	public Object getReferenz() {
		return referenz;
	}

	/**
	 * Benutzt die equals-Methode um die Kanalzuordnung pr�fen zu k�nnen und
	 * liefert entsprechend <code>true</code> oder <code>false</code> zur�ck.
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
