import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**Wir eine Facade für unsere Sockets um Messeges zu empfangen und zu versenden
 * @author Kaddi
 *
 */
public class ConnectionHandler {
	private Socket line;
	private ObjectOutputStream line_out;
	private ObjectInputStream line_in;
	//private BlockingQueue<MSG> outbox;
	private Thread pakets_rein_hol_bot;
	private NodeEngine ne;
	
	
	public ConnectionHandler(Socket underlying) throws IOException{
		ne=NodeEngine.getNE();
		line = underlying;
		//outbox = new LinkedBlockingQueue<MSG>();
		line_out=new ObjectOutputStream(line.getOutputStream());
		line_in=new ObjectInputStream(line.getInputStream());
		
		pakets_rein_hol_bot = new Thread(new Runnable() {
			public void run() {
				while(line.isConnected()){
					try {
						MSG tmp = (MSG) line_in.readObject();
						ne.handle(tmp);
					} catch (ClassNotFoundException e) {
						LogEngine.log(e);
					} catch (IOException e) {
						LogEngine.log(e);
					}
				}
			}
		});
		pakets_rein_hol_bot.start();
	}
	
	/**Verschickt ein MSG-Objekt über den Soket.
	 * @param paket Das zu versendende Paket
	 * @throws IOException Wenn es zu einem Fehler beim senden auf dem TCP-Socket kommt
	 */
	public void send(MSG paket) throws IOException{
			line_out.writeObject(paket);
			line_out.flush();
	}
	

}
