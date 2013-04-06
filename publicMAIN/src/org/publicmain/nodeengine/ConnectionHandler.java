package org.publicmain.nodeengine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.NachrichtenTyp;
import org.publicmain.common.Node;

/**
 * Hüll Klasse für einen TCP-Socket zur übertragung von MSG Objekten
 * 
 * 
 */
public class ConnectionHandler {
	public Set<Node>			children;
	private Set<String>			groups;
	public Node					otherEnd;
	private NodeEngine			ne;
	private Socket				line;
	private ObjectOutputStream	line_out;
	private ObjectInputStream	line_in;
	private volatile ConnectionHandler	me;
	private String				endpoint;
	private long					latency				= Integer.MAX_VALUE;

	private Thread				pakets_rein_hol_bot	= new Thread(new Reciever());
	private Thread				pingpongBot			= new Thread(new Pinger());

	public ConnectionHandler(Socket underlying) throws IOException {

		ne = NodeEngine.getNE();
		children = new HashSet<Node>();
		groups = new HashSet<String>();

		line = underlying;
		line.setTcpNoDelay(true);
		line.setKeepAlive(true);
		line.setSoTimeout(0);
		line_out = new ObjectOutputStream(new BufferedOutputStream(line.getOutputStream()));
		line_out.flush();
		line_in = new ObjectInputStream(new BufferedInputStream(line.getInputStream()));

		endpoint = line.getInetAddress().getHostAddress();

//		ping();
//		endpoint = line.getInetAddress().getHostName();
		//pingpongBot.start();

		LogEngine.log(this, "Verbunden");
		me = this;
		pakets_rein_hol_bot.start();

	}

	/**
	 * Verschickt ein MSG-Objekt über den Soket.
	 * 
	 * @param paket
	 *            Das zu versendende Paket
	 * @throws IOException
	 *             Wenn es zu einem Fehler beim senden auf dem TCP-Socket kommt
	 */
	public void send(final MSG paket) {
				if (isConnected()) {
					try {
						LogEngine.log(ConnectionHandler.this, "sending", paket);
						line_out.writeObject(paket);
						line_out.flush();
					} catch (IOException e) {
						LogEngine.log(ConnectionHandler.this, "failure", paket);
					}
				} else
					LogEngine.log(ConnectionHandler.this, "dropped", paket);
	}

	/**
	 * Prüft ob die Verbindung noch besteht.
	 * 
	 * @return <code>true</code> wenn die Verbindung noch besteht <code>false</code> wenn nicht
	 */
	public boolean isConnected() {
		return (line != null && line.isConnected() && !line.isClosed());
	}

	public void disconnect() {
		send(new MSG(ne.getMe(), MSGCode.NODE_SHUTDOWN));
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
		LogEngine.log(me, "closed");
		me = null;
		//pakets_rein_hol_bot.stop();
		pakets_rein_hol_bot = null;
		ne.remove(this);

	}

	public String toString() {
		return "ConnectionHandler [" + endpoint + "]" + ((latency < 10000) ? "[" + latency + "]" : "");
	}

	private void ping() {
		send(new MSG(null, MSGCode.ECHO_REQUEST));
	}
	
	public boolean add(Collection<String> gruppe) {
		synchronized (groups) {
			return groups.addAll(gruppe);
		}
	}
	public boolean remove(Collection<String> gruppe) {
		synchronized (groups) {
			return groups.removeAll(gruppe);
		}
	}
	
	/*
	public boolean add(String gruppe) {
		synchronized (groups) {
			return groups.add(gruppe);
		}
	}
	
	public boolean remove(String gruppe) {
		synchronized (groups) {
			return groups.remove(gruppe);
		}
	}
	*/
	public Set<String> getGroups(){
		synchronized (groups) {
			return groups;
		}
	}

	private void pong(final MSG ping) {
		new Thread(new Runnable() {
			public void run() {
				send(new MSG(ping.getTimestamp(), MSGCode.ECHO_RESPONSE));
			}
		}).start();
	}

	class Reciever implements Runnable {
		public void run() {
			while(me==null)System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!ME WAR NULL!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			while (me != null && me.isConnected()) {
				Object readObject = null;
				try {
					readObject = line_in.readObject();
					if (readObject != null && readObject instanceof MSG) {
						MSG tmp = (MSG) readObject;
						if (tmp.getTyp() == NachrichtenTyp.SYSTEM) {
							switch (tmp.getCode()) {
								case ECHO_REQUEST:
									pong(tmp);
									break;
								case ECHO_RESPONSE:
									latency = System.currentTimeMillis() - (Long) tmp.getData();
									break;
								case NODE_UPDATE:
									me.children.add((Node) tmp.getData());
								default:
									ne.handle(tmp, me);
							}
						}
						else ne.handle(tmp, me);
					}
					else LogEngine.log(me, "Empfangenes Objekt ist keine MSG");
				}
				catch (ClassNotFoundException e) {
					LogEngine.log(e, "ConnectionHandler");
				}
				catch (IOException e) {
					LogEngine.log(e);
					break; //wenn ein Empfangen vom Socket nicht mehr möglich ist -> Thread beenden
				}
				catch (Exception e) {
					System.out.println(me);
					System.out.println(readObject);
					System.out.println(e.getMessage());
					e.printStackTrace();
					if (readObject != null) System.out.println((readObject instanceof MSG) ? ((MSG) readObject).toString() : readObject.toString());
				}
			}
			close();
		}
}
	

	class Pinger implements Runnable {
		private static final long	PING_INTERVAL	= 30000;

		public void run() {
			while (isConnected()) {
				try {
					ping();
					Thread.sleep((long) (PING_INTERVAL * (1 + Math.random()))); //ping randomly mit PING_INTERVAL bis 2xPING_INTERVAL Pausen
				}
				catch (InterruptedException e) {
				}
			}
		}

	}

}
