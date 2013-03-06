import java.util.Collection;
import java.util.Comparator;
import java.util.Observable;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;


public abstract class Kanal extends Observable {
	
	protected Set<MSG> messages =	new TreeSet<MSG>(new Comparator<MSG>() {
					public int compare(MSG o1, MSG o2) {
						if (o1.getTimestamp()!=o2.getTimestamp())return (o1.getTimestamp()>o2.getTimestamp())?1:-1;  
						else if(o1.getSender()!=o2.getSender())return (o1.getSender()>o2.getSender())?1:-1;
						else if(o1.getId()!=o2.getId()) return (o1.getId()-o2.getId());
						return 0;
					}});


	
	public abstract boolean add(MSG nachricht);
	public abstract boolean is(Object vergleich);

}
