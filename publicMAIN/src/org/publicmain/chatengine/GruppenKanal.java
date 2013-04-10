package org.publicmain.chatengine;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.NachrichtenTyp;

/**
 * @author ATRM
 *
 */

public class GruppenKanal extends Kanal {

	public GruppenKanal(String gruppe) {
		super(gruppe.toLowerCase());
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	public boolean add(MSG nachricht){
		LogEngine.log(this, "Nachricht auf Kanal " + referenz + " empfangen benachrichtige : " + this.countObservers(), LogEngine.INFO);
		if(nachricht.getTyp()==NachrichtenTyp.GROUP&&nachricht.getGroup().equals(referenz)){
			messages.add(nachricht);
			setChanged();
			notifyObservers(nachricht);
			return true;
		}
		return false;
	}

}
/*


*/