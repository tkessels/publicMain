import java.sql.Time;


/**GGF mal über java.util.logging nachdenken
 * @author tkessels
 *
 */
public class LogEngine {
	private static int verbosity=3;
	public static final int INFO=3;
	public static final int WARNING=2;
	public static final int ERROR=1;
	public static final int NONE=0;
	
	/**Gibt eine Exception auf dem Programm Fehlerstrom aus
	 * @param e Die zu dokumentierende Exception
	 */
	public static void log(Exception e) {
		if(verbosity>0){
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/** Setzt die Meldeschwelle ab welchem schweregrad eine Ausgabe erfolgen soll.
	 * @param x Das Verbositätslevel ?!?!
	 */
	public void serVerbosity(int x){
		verbosity=x;
	}
	
	/**Gibt eine Fehlermeldung auf dem Fehlerstrom aus 
	 * @param meldung Der Text der Fehlermeldung
	 * @param source Die Quelle des Fehlers
	 * @param errorLevel Das Niveau des Fehlers (INFO, WARNING oder ERROR)
	 */
	public static void log(String meldung, Object source,int errorLevel){
		if(errorLevel<=verbosity){
			System.err.println(new Time(System.currentTimeMillis()).toString()+":"+source.getClass().getName()+":meldung");
		}
	}

}
