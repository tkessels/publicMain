package org.publicmain.chatengine;
import org.publicmain.common.MSG;


public class KnotenKanal extends Kanal{
	
	public KnotenKanal(long node) {
		super(node);
		//this.referenz=node;
	}

	@Override
	public boolean add(MSG nachricht) {
		System.out.println(nachricht);
		System.out.println(referenz);
		if((nachricht.getSender()==(long)referenz)||nachricht.getEmpfänger()==(long)referenz){
			setChanged();
			notifyObservers(nachricht);
			messages.add(nachricht);
			return true;
		}
		return false;
	}

}
