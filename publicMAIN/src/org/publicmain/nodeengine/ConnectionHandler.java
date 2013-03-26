package org.publicmain.nodeengine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.Node;



/**Wir eine Facade für unsere Sockets um Messeges zu empfangen und zu versenden
 * @author Kaddi
 *
 */
public class ConnectionHandler {
	public List<Node> children;
	private NodeEngine ne;
	private Socket line;
	private ObjectOutputStream line_out;
	private ObjectInputStream line_in;
	private Thread pakets_rein_hol_bot;
	private ConnectionHandler me;
	
	
	public ConnectionHandler(Socket underlying) throws IOException{
		me=this;
		ne=NodeEngine.getNE();
		children=new ArrayList<Node>();
		
		
		
		line = underlying;
		line_out=new ObjectOutputStream(new BufferedOutputStream(line.getOutputStream()));
		line_out.flush();
		line_in=new ObjectInputStream(new BufferedInputStream(line.getInputStream()));
		
		pakets_rein_hol_bot = new Thread(new reciever());
		pakets_rein_hol_bot.start();
		
		LogEngine.log("Verbindung", this, LogEngine.INFO);
	}



	/**Verschickt ein MSG-Objekt über den Soket.
	 * @param paket Das zu versendende Paket
	 * @throws IOException Wenn es zu einem Fehler beim senden auf dem TCP-Socket kommt
	 */
	public void send(MSG paket){
		if(isConnected()){
			try {
				line_out.writeObject(paket);
				line_out.flush();
			} catch (IOException e) {
				LogEngine.log(e);
			}
		}
		else LogEngine.log(this,"dropped",paket);
	}
	
	class reciever implements Runnable
	{
		public void run() 
		{
			while(line.isConnected())
			{
				try 
				{
					MSG tmp = (MSG) line_in.readObject();
					ne.handle(tmp,me);
				} 
				catch (ClassNotFoundException|IOException e) 
				{
					LogEngine.log(e);
				} 
			}
		}		
	}
	
	public boolean isConnected() {
		return line.isConnected();
	}
	
	public void disconnect(){
		send(new MSG(ne.getME(),MSGCode.NODE_SHUTDOWN));
		try {
			line.close();
		} catch (IOException e) {
		}
		ne.remove(this);
	}
	

	@Override
	public String toString() {
		return "ConnectionHandler ["
				+ (line != null ? "line=" + line + ", " : "")
				+ (line_out != null ? "line_out=" + line_out + ", " : "")
				+ (line_in != null ? "line_in=" + line_in + ", " : "")
				+ (pakets_rein_hol_bot != null ? "pakets_rein_hol_bot="
						+ pakets_rein_hol_bot + ", " : "")
				+ (ne != null ? "ne=" + ne + ", " : "") + "]";
	}
	

}
