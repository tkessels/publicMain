
public class GruppenKanal extends Kanal {
	




	public GruppenKanal(String gruppe) {
		//super();
		this.referenz=gruppe.toLowerCase();
	}
	
//
//	private boolean vergleich(Object eins, Object zwei){
//		String seins=(String) eins;
//		String szwei=(String) zwei;
//		if(seins.length()!=szwei.length())return false;
//		for (int i = 0; i < seins.toCharArray().length; i++) {
//			if(seins.toCharArray()[i]!=szwei.toCharArray()[i]) return false;
//		}
//		return true;
//		
//	}
	
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