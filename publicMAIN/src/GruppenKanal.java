

public class GruppenKanal extends Kanal {

	


	public GruppenKanal(String gruppe) {
		super(gruppe);
	}

	public boolean check(MSG nachricht){
		if(nachricht.getGroup().equals(identifier)){
			setChanged();
			notifyObservers(nachricht);
			return true;
		}
		return false;
	}
}
/*


*/