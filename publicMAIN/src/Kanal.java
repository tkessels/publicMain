import java.util.Observable;


public abstract class Kanal extends Observable {
	protected Object identifier;
	public abstract boolean check(MSG nachricht);
	public boolean isName(Object name){
		return(identifier.equals(name));
		
	}
	
	public Kanal(Object tmp) {
		identifier=tmp;
	}
	

}
