package org.publicmain.chatengine;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;

import org.publicmain.common.MSG;

public abstract class Kanal extends Observable {
	protected Object 	referenz;


	protected TreeSet<MSG> messages = new TreeSet<MSG>(new Comparator<MSG>() {
		public int compare(MSG o1, MSG o2) {
			if (o1.getTimestamp() != o2.getTimestamp())	return (o1.getTimestamp() > o2.getTimestamp()) ? 1 : -1;
			else if (o1.getSender() != o2.getSender())	return (o1.getSender() > o2.getSender()) ? 1 : -1;
			else if (o1.getId() != o2.getId())			return (o1.getId() - o2.getId());
			return 0;
		}
	});

	public abstract boolean add(MSG nachricht);

	public abstract boolean is(Object vergleich);

	/**Gibt die letzten x Nachrichten aus einem Kanal als Set zurück
	 * @param count Die gewünschte Anzahl von Nachrichten
	 * @return Set der x-Letzen Nachrichten
	 */
	public Set<MSG> getLastMSGs(int count) {
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
	}
	
	/**
	 * Löscht alle Nachrichten aus dem Kanal.
	 */
	public void purgeMSGs(){
		messages.clear();
	}
	
	@Override
	public boolean equals(Object obj) {
		return referenz.equals(((Kanal)obj).referenz);
		
	}
	
}
