package org.publicmain.nodeengine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.Node;



/**Wir eine Facade für unsere Sockets um Messeges zu empfangen und zu versenden
 * @author Kaddi
 *
 */
public class ConnectionHandler {
	public Set<Node> children;
	private NodeEngine ne;
	private Socket line;
	private ObjectOutputStream line_out;
	private ObjectInputStream line_in;
	private Thread pakets_rein_hol_bot;
	private ConnectionHandler me;
	private String endpoint;
	
	
	public ConnectionHandler(Socket underlying) throws IOException{
		me=this;
		ne=NodeEngine.getNE();
		children=new HashSet<Node>();
		
		line = underlying;
		line.setTcpNoDelay(true);
		line_out=new ObjectOutputStream(new BufferedOutputStream(line.getOutputStream()));
		line_out.flush();
		line_in=new ObjectInputStream(new BufferedInputStream(line.getInputStream()));
		
		pakets_rein_hol_bot = new Thread(new reciever());
		pakets_rein_hol_bot.start();
		endpoint=line.getInetAddress().getHostName() ;
		LogEngine.log(this,"Verbunden");

	}

	/**Verschickt ein MSG-Objekt über den Soket.
	 * @param paket Das zu versendende Paket
	 * @throws IOException Wenn es zu einem Fehler beim senden auf dem TCP-Socket kommt
	 */
	public void send(MSG paket){
		if(isConnected()){
			try {
				LogEngine.log(this,"sending",paket);
				line_out.writeObject(paket);
				line_out.flush();
			} catch (IOException e) {
				LogEngine.log(e);
			}
		}
		else LogEngine.log(this,"dropped",paket);
	}
	
	public boolean isConnected() {
		return line.isConnected()&&!line.isClosed();
	}
	
	public void disconnect(){
		send(new MSG(ne.getME(),MSGCode.NODE_SHUTDOWN));
		close();
	}
	
	public void close() {
		try {
			line_out.close();
		}
		catch (IOException e) {
		}
		try {
			line_in.close();
		}
		catch (IOException e) {
		}
		try {
			line.close();
		}
		catch (IOException e) {
		}
		pakets_rein_hol_bot=null;
		me=null;
		ne.remove(this);

		LogEngine.log(me,"closed");
	}
	
	@Override
	public String toString() {
		return "ConnectionHandler [" +endpoint +"]" ;
	}
	
	class reciever implements Runnable
	{
		public void run() 
		{
			while(me!=null&&me.isConnected())
			{
				try 
				{
					MSG tmp = (MSG) line_in.readObject();
					ne.handle(tmp, me);
				} 
				catch (ClassNotFoundException e) {
					LogEngine.log(e,"ConnectionHandler");
				} 
				catch (IOException e) 
				{
					break; //wenn ein Empfangen vom Socket nicht mehr möglich ist Thread beenden
				}
			}
			close();
		}		
	}
	
	

}
