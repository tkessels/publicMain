
public class GruppenKanal extends Kanal {

	String gruppe;


	public GruppenKanal(String gruppe) {
		this.gruppe=gruppe.toLowerCase();
	}
	

	public boolean add(MSG nachricht){
		if(nachricht.getGroup().equals(this.gruppe)){
			setChanged();
			notifyObservers(nachricht);
			return true;
		}
		return false;
	}



	public boolean is(Object vergleich) {
		return (this.gruppe.equals(vergleich));
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString()+messages.toString();
	}
	
}
/*


*/