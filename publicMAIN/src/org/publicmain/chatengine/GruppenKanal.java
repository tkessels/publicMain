package org.publicmain.chatengine;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;

/**
 * @author ATRM
 *
 */

public class GruppenKanal extends Kanal {

	public GruppenKanal(String gruppe) {
		//super();
		this.referenz=gruppe.toLowerCase();
	}
	
	public boolean add(MSG nachricht){
		LogEngine.log("Nachricht auf Kanal " + referenz + " empfangen benachrichtige : " + this.countObservers(), this, LogEngine.INFO);
		if(nachricht.getGroup().equals(referenz)){
			messages.add(nachricht);
			setChanged();
			notifyObservers(nachricht);
			return true;
		}
		return false;
	}

	public boolean is(Object vergleich) {
		return (this.referenz.equals(vergleich));
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString()+messages.toString();
	}
}
/*


*/