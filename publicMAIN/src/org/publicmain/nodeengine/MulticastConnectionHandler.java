package org.publicmain.nodeengine;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.publicmain.common.Config;
import org.publicmain.common.ConfigData;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.NachrichtenTyp;
import org.publicmain.common.Node;

public class MulticastConnectionHandler {


	private final InetAddress MULTICAST_GROUP;	//MulticastGruppe für Verbindungsaushandlung
	private final int MULTICAST_PORT;			//Port für MulticastGruppe für Verbindungsaushandlung
	private final int MULTICAST_TTL;				//Port für MulticastGruppe für Verbindungsaushandlung

	private static MulticastConnectionHandler me;
	private static MulticastSocket multi_socket;
	private static Thread multicastReciever;			//Thread
	private static NodeEngine ne;

	private MulticastConnectionHandler() throws IOException  {
		this(Config.getConfig().getMCGroup(),Config.getConfig().getMCPort(),Config.getConfig().getMCTTL());
	}

	private MulticastConnectionHandler(String multicast_IP, int port, int ttl) throws IOException {

		MULTICAST_GROUP = InetAddress.getByName(multicast_IP); 	
		MULTICAST_PORT = port;						 			
		MULTICAST_TTL = ttl;							 			

		multi_socket = new MulticastSocket(MULTICAST_PORT);
		multi_socket.setLoopbackMode(true);
		multi_socket.joinGroup(MULTICAST_GROUP);
		multi_socket.setTimeToLive(MULTICAST_TTL);
		multicastReciever = new Thread(new MulticastReciever());
		multicastReciever.start();
	}


	public static MulticastConnectionHandler getMC() {
		if (me!=null) return me;
		else return getMC(Config.getConfig().getMCGroup(),Config.getConfig().getMCPort(),Config.getConfig().getMCTTL());
	}
	public static synchronized MulticastConnectionHandler getMC(String multicast_IP, int port, int ttl) {
		if (me!=null) return me;
		else {
			try {
				if(me==null) {
					me = new MulticastConnectionHandler(multicast_IP,port,ttl);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return me;

	}

	public void close() {
		multicastReciever.stop();
		multi_socket.close();
	}

	public synchronized void sendmutlicast(MSG nachricht) {
		if (multi_socket!=null&&multi_socket.isBound()) {
			try {
				multi_socket.send(msg2UDP(nachricht, MULTICAST_GROUP, MULTICAST_PORT));
				LogEngine.log(this, "sende [MC]", nachricht);
			} catch (Exception e) {
				LogEngine.log(this, e);
			}
		}
		else LogEngine.log(this, "dropped [MC]", nachricht);
	}
	
	
	public void discoverBUS() {
		MulticastConnectionHandler.getMC().sendmutlicast(new MSG(NachrichtenTyp.SYSTEM, MSGCode.BACKUP_SERVER_DISCOVER, -1, -1, null, null));
	}

	public synchronized void sendunicast(MSG nachricht, Node target) {
		for (InetAddress x : target.getSockets()) {
			if (!Node.getMyIPs().contains(x)) {
				try {
					multi_socket.send(msg2UDP(nachricht, x, MULTICAST_PORT));
					LogEngine.log(this, "sende [" + x.toString() + "]", nachricht);
				} catch (Exception e) {
					LogEngine.log(e);
				}
			}
		}
	}
	public void registerNodeEngine(NodeEngine ne) {
		this.ne=ne;
	}

	private DatagramPacket msg2UDP(MSG nachricht,InetAddress recipient, int port) throws IllegalArgumentException {
		byte[] data = MSG.getBytes(nachricht);
		if (data.length < 65000)
			return new DatagramPacket(data, data.length,recipient,port);
		else
			throw new IllegalArgumentException(nachricht.toString()+" to big for UDP - Datagram");
	}
	private void handle(MSG paket) {
		if(paket.getTyp()==NachrichtenTyp.SYSTEM) {
			if(paket.getCode()==MSGCode.BACKUP_SERVER_OFFER) {
				ConfigData tmp = (ConfigData) paket.getData();
				System.out.println(tmp);
				Config.getConfig().setBackupDBIP(tmp.getBackupDBIP());
				Config.getConfig().setBackupDBPort(tmp.getBackupDBPort());
				Config.getConfig().setBackupDBUser(tmp.getBackupDBUser());
				Config.getConfig().setBackupDBPw(tmp.getBackupDBPw());
				Config.getConfig().setBackupDBDatabasename(tmp.getBackupDBDatabasename());
				Config.write();
			}
		}
	}


	private final class MulticastReciever implements Runnable {
		public void run() {
			if(multi_socket==null)return;
			while (true) {
				byte[] buff = new byte[65535];
				DatagramPacket tmp = new DatagramPacket(buff, buff.length);
				try {
					multi_socket.receive(tmp);
					MSG nachricht = MSG.getMSG(tmp.getData());
					LogEngine.log("multicastRecieverBot", "multicastRecieve", nachricht);
					if (nachricht != null) {
						if (ne!=null) {
							ne.handleMulticast(nachricht);
						} else {
							handle(nachricht);
						}
					}

				}
				catch (IOException e) {
					LogEngine.log(e);
				}
			}
		}
	}

}
