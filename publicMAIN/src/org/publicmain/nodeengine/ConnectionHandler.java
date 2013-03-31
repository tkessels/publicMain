package org.publicmain.nodeengine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import javax.swing.text.AsyncBoxView.ChildState;

import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.NachrichtenTyp;
import org.publicmain.common.Node;



/**Wir eine Facade für unsere Sockets um Messeges zu empfangen und zu versenden
 * @author Kaddi
 *
 */
public class ConnectionHandler {
	public Set<Node> children;
	public Node otherEnd;
	private NodeEngine ne;
	private Socket line;
	private ObjectOutputStream line_out;
	private ObjectInputStream line_in;
	private Thread pakets_rein_hol_bot;
	private ConnectionHandler me;
	private String endpoint;
	
	private Thread pingpongBot=new Thread(new Pinger());
	private long latency=Integer.MAX_VALUE;

	
	
	public ConnectionHandler(Socket underlying) throws IOException{
		me=this;
		ne=NodeEngine.getNE();
		children=new HashSet<Node>();
		
		line = underlying;
		line.setTcpNoDelay(true);
		line.setKeepAlive(true);
		line.setSoTimeout(0);
		line_out=new ObjectOutputStream(new BufferedOutputStream(line.getOutputStream()));
		line_out.flush();
		line_in=new ObjectInputStream(new BufferedInputStream(line.getInputStream()));
		
		endpoint=line.getInetAddress().getHostAddress();
		pakets_rein_hol_bot = new Thread(new Reciever());
		pakets_rein_hol_bot.start();
		pingpongBot.start();
		endpoint=line.getInetAddress().getHostName() ;
		
		LogEngine.log(this,"Verbunden");

	}

	/**
	 * Verschickt ein MSG-Objekt über den Soket.
	 * 
	 * @param paket
	 *            Das zu versendende Paket
	 * @throws IOException
	 *             Wenn es zu einem Fehler beim senden auf dem TCP-Socket kommt
	 */
	public void send(MSG paket) {
		if (isConnected()) {
			try {
				LogEngine.log(this, "sending", paket);
				line_out.writeObject(paket);
				line_out.flush();
			}
			catch (IOException e) {
				LogEngine.log(this, "failure", paket);
			}
		}
		else LogEngine.log(this, "dropped", paket);
	}
	
	/**Prüft ob die Verbindung noch besteht.
	 * @return <code>true</code> wenn die Verbindung noch besteht
	 * <code>false</code> wenn nicht
	 */
	public boolean isConnected() {
		return (line!=null&&line.isConnected()&&!line.isClosed());
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
		LogEngine.log(me,"closed");
		me=null;
		//pakets_rein_hol_bot.stop();
		pakets_rein_hol_bot=null;
		ne.remove(this);


	}
	
	@Override
	public String toString() {
		return "ConnectionHandler [" +endpoint +"]" +  ((latency<10000)?"["+latency+"]":"");
	}
	
	class Reciever implements Runnable
	{
		public void run() 
		{
			while (me != null && me.isConnected()) {
				Object readObject = null;
				try {
					readObject = line_in.readObject();
					MSG tmp = (MSG) readObject;
					if (tmp.getTyp() == NachrichtenTyp.SYSTEM&&tmp.getCode()==MSGCode.NODE_UPDATE)me.children.add((Node) tmp.getData());
					/*
					if (tmp.getTyp() == NachrichtenTyp.SYSTEM) {
						if (tmp.getCode() == MSGCode.ECHO_REQUEST) {
							send(MSG.createReply(tmp));
						}
						if (tmp.getCode() == MSGCode.ECHO_RESPONSE) {
							latency = System.currentTimeMillis() - (Long) tmp.getData();
						}
					}
					else ne.handle(tmp, me);*/
					ne.handle(tmp, me);
					
				}
				catch (ClassNotFoundException e) {
					LogEngine.log(e, "ConnectionHandler");
				}
				catch (IOException e) {
					LogEngine.log(e);
					break; //wenn ein Empfangen vom Socket nicht mehr möglich ist -> Thread beenden
				}
				catch (Exception e) {
					System.out.println(readObject);
					if (readObject != null) System.out.println((readObject instanceof MSG)?((MSG)readObject).toString():readObject.toString());
					System.out.println(me);
				}
			}
			close();
		}		
	}
	
	class Pinger implements Runnable{
		private static final long	PING_INTERVAL	= 30000;

		public void run() {
			while(isConnected()) {
				try {
					send(new MSG(null, MSGCode.ECHO_REQUEST));
					Thread.sleep((long) (PING_INTERVAL*(1+Math.random()))); //ping randomly mit PING_INTERVAL bis 2xPING_INTERVAL Pausen
				}
				catch (InterruptedException e) {
				}
			}
		}
	}
	

}
