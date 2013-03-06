
public class KnotenKanal extends Kanal{
	
	private long node;
	public KnotenKanal(long node) {
		this.node=node;
	}

	@Override
	public boolean add(MSG nachricht) {
		if((nachricht.getSender()==(long)node)||nachricht.getEmpfänger()==(long)node){
			setChanged();
			notifyObservers(nachricht);
			return true;
		}
		return false;
	}

	@Override
	public boolean is(Object vergleich) {
		return (node==(long)vergleich);
	}

}
