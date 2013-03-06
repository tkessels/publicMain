
public class LogEngine {
	public static void log(Exception e) {
		System.err.println(e.getMessage());
		e.printStackTrace();
		
		
	}

}
