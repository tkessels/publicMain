package org.publicmain.nodeengine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;



/**Wir eine Facade für unsere Sockets um Messeges zu empfangen und zu versenden
 * @author Kaddi
 *
 */
public class ConnectionHandler {
	private static final int NOT_CONNECTED = 0;
	private static final int CONNECTED = 1;
	private static final int CHATMODE = 2;
	private static final int DATAMODE = 2;
	
	private Socket line;
	private ObjectOutputStream line_out;
	private ObjectInputStream line_in;
	private Thread pakets_rein_hol_bot;
	private NodeEngine ne;
	private int zustand=NOT_CONNECTED;
	
	
	public ConnectionHandler(Socket underlying) throws IOException{
		ne=NodeEngine.getNE();
		pakets_rein_hol_bot = new Thread(new reciever());
		line = underlying;
		line_out=new ObjectOutputStream(new BufferedOutputStream(line.getOutputStream()));
		line_out.flush();
		line_in=new ObjectInputStream(new BufferedInputStream(line.getInputStream()));
		zustand=CONNECTED;
		System.out.println(this);
		LogEngine.log("Verbindung", this, LogEngine.INFO);
		pakets_rein_hol_bot.start();
	}



	/**Verschickt ein MSG-Objekt über den Soket.
	 * @param paket Das zu versendende Paket
	 * @throws IOException Wenn es zu einem Fehler beim senden auf dem TCP-Socket kommt
	 */
	public void send(MSG paket) throws IOException{
		if(isConnected()){
			line_out.writeObject(paket);
			line_out.flush();
		}
	}
	
	class reciever implements Runnable
	{
		public void run() 
		{
			while(zustand==CHATMODE&&line.isConnected())
			{
				try 
				{
					MSG tmp = (MSG) line_in.readObject();
					ne.handle(tmp,getIndexOfME());
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
	private int getIndexOfME(){
		return NodeEngine.getNE().connections.indexOf(this);
	}


	@Override
	public String toString() {
		return "ConnectionHandler ["
				+ (line != null ? "line=" + line + ", " : "")
				+ (line_out != null ? "line_out=" + line_out + ", " : "")
				+ (line_in != null ? "line_in=" + line_in + ", " : "")
				+ (pakets_rein_hol_bot != null ? "pakets_rein_hol_bot="
						+ pakets_rein_hol_bot + ", " : "")
				+ (ne != null ? "ne=" + ne + ", " : "") + "zustand=" + zustand
				+ "]";
	}
	

}
