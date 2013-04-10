package org.publicmain.chatengine;
import org.publicmain.common.MSG;


public class KnotenKanal extends Kanal{
	
	public KnotenKanal(long node) {
		super(node);
	}

	@Override
	public boolean add(MSG nachricht) {
		if((nachricht.getSender()==(long)referenz)||(nachricht.getEmpfänger()==(long)referenz)){
			messages.add(nachricht);
			setChanged();
			notifyObservers(nachricht);
			return true;
		}
		return false;
	}

}
