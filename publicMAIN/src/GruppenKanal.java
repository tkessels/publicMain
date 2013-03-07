
public class GruppenKanal extends Kanal {




	public GruppenKanal(String gruppe) {
		super();
		this.referenz=gruppe.toLowerCase();
	}
	

	public boolean add(MSG nachricht){
		if(nachricht.getGroup().equals(this.referenz)){
			setChanged();
			notifyObservers(nachricht);
			messages.add(nachricht);
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