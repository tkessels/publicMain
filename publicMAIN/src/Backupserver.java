

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.publicmain.common.Config;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.NachrichtenTyp;
import org.publicmain.common.Node;

public class Backupserver {
	private final static  int BACKUP_DATABASE_VERSION = 12345;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.setProperty("appname", "pmBackupServer");
		String username="root";
		String password="";
		String port="3306";
		String databasename="db_publicMain_backup";
		String ip=Node.getMyIPs().get(0).getHostAddress();
		
		//if available load settings from file
		if(Config.getConfig().getBackupDBUser()!=null)	username				=	Config.getConfig().getBackupDBUser(); 
		if(Config.getConfig().getBackupDBPw()!=null)	password				=	Config.getConfig().getBackupDBPw(); 
		if(Config.getConfig().getBackupDBIP()!=null)		ip						=	Config.getConfig().getBackupDBIP(); 
		if(Config.getConfig().getBackupDBPort()!=null)	port						=	Config.getConfig().getBackupDBPort(); 
		if(Config.getConfig().getBackupDBDatabasename()!=null)	databasename	=	Config.getConfig().getBackupDBDatabasename(); 

		
		Random x = new Random(System.currentTimeMillis());
		long nid = x.nextLong();
		nid *=Long.signum(nid);
		long uid = -1;

		//parse parameters
		if(args.length>0) {
			if ((args.length%2)==0) {
				for (int i = 0; i < args.length/2; i++) {
					switch (args[i]) {
					case "-u":
					case "--user":
						username=args[i+1];
						break;
					case "-p":
					case "--pass":
						password=args[i+1];
						break;
					case "-s":
					case "--server":
					case "--ip":
						String[] split = args[i+1].split(":");
						if (split.length==2) {
							port=split[1];
							}
						ip=split[0];
						break;
					case "--port":
						port = args[i+1];
						break;
					case "-n":
						databasename = args[i+1];
					break;
					default:
						//odd parameters give usage info and exit
						printUsageInfoExit();
					}
				}
			}
			else {
				// odd parameters give usage info and exit
				printUsageInfoExit();

			}
		}
			
		
		//test for local mySQL Server
		if(checkConnection(ip,port,username,password)) {
			//test version of pm-Database on mySQL server OR create it 
			if(checkVersion(ip,port,username,password) || createDatabase(ip, port, username, password)) {
				
				//everything is fine start offering backupserver
				try (MulticastSocket backupserver = new MulticastSocket(Config.getConfig().getMCPort())){
					InetAddress mcgroup = InetAddress.getByName(Config.getConfig().getMCGroup());
					backupserver.setLoopbackMode(true);
					backupserver.joinGroup( mcgroup );
					backupserver.setTimeToLive(Config.getConfig().getMCTTL());
					
					
					if (!ip.equals(Config.getConfig().getBackupDBIP()))Config.getConfig().setBackupDBIP(ip);
					if (!port.equals(Config.getConfig().getBackupDBPort()))Config.getConfig().setBackupDBPort(port);
					if (!username.equals(Config.getConfig().getBackupDBUser()))Config.getConfig().setBackupDBUser(username);
					if (!password.equals(Config.getConfig().getBackupDBPw()))Config.getConfig().setBackupDBPw(password);
					if (!databasename.equals(Config.getConfig().getBackupDBDatabasename()))Config.getConfig().setBackupDBDatabasename(databasename);
					Config.write();
					
					while (true) {
						byte[] buff = new byte[65535];
						DatagramPacket tmp = new DatagramPacket(buff, buff.length);
						try {
							backupserver.receive(tmp);
							MSG nachricht = MSG.getMSG(tmp.getData());
							LogEngine.log("BackupServer", "recieved", nachricht);
							
							if(nachricht.getTyp()==NachrichtenTyp.SYSTEM&&nachricht.getCode()==MSGCode.BACKUP_SERVER_DISCOVER) {
								MSG reply = new MSG(NachrichtenTyp.SYSTEM, MSGCode.BACKUP_SERVER_OFFER, nid, nachricht.getSender(), "", Config.getConfig());

								byte[] buf = MSG.getBytes(reply);
								if (buf.length < 65000) {
									backupserver.send(new DatagramPacket(buf, buf.length, mcgroup, Config.getConfig().getMCPort()));
									LogEngine.log("BackupServer", "sende", nachricht);
								}
								else LogEngine.log("BackupServer", "MSG zu groß für UDP-Paket", LogEngine.ERROR);
							}
						}
						catch (IOException e) {
							LogEngine.log(e);
						}
					}
				} catch (IOException e) {
					LogEngine.log("BackupServer", e);
				}
			}
			else {
				System.err.println("Server is online but database is outdatet or not present and couldn't be created!");
			}
		}else {
			System.err.println("Could not connect to given Database. Shutting down!");
			System.err.println("server\t\t:" + ip+":"+port);
			System.err.println("user\t\t\t:" + username);
			System.err.println("password\t:" + password);
			System.err.println("database\t\t:" + Config.getConfig().getBackupDBDatabasename());
		}

	}

	/**
	 * 
	 */
	private static void printUsageInfoExit() {
		String ip = "127.0.0.1";
		if (Node.getMyIPs().size()>=1)ip = Node.getMyIPs().get(0).getHostAddress();
		System.err.println("Usage:");
		System.err.println("	java Backupserver -u <username> -p <password> -s <ip:port> -n <database_name>");
		System.err.println("	java Backupserver --user <username> --pass <password> --ip <ip> --port <port> --database <database_name>");
		System.err.println("\nNot provided parameters will be set to default values!\n");
		System.err.println("Example:");
		System.err.println("	java Backupserver -u admin --pass secret -n db1");
		System.err.println("		Will setup the backupserver for a local Database ("+ip+":3306) with user \"root\" and empty password ");
		System.exit(1);
	}

	private static boolean checkConnection(String ip, String port, String username, String password) {
		String databasename = Config.getConfig().getBackupDBDatabasename();
		//TODO:Testen ob Datenbank vorhanden .... bei ner lokalen backup db kann hier auch stat der übergebenen IP 127.0.0.1 genommen werden oder?
		return true;
	}
	private static boolean checkVersion(String ip, String port, String username, String password) {
		String databasename = Config.getConfig().getBackupDBDatabasename();
		int x = 123123;
		//TODO:Testen ob Datenbank Version die richtige ist
		
		if (BACKUP_DATABASE_VERSION==x) {
			return true;
		}
		return false;
	}
	
	private static boolean createDatabase(String ip, String port, String username, String password) {
		String databasename = Config.getConfig().getBackupDBDatabasename();
		
		//CreateScript auf der Database ausführen und true zurückliefern wenn erfolgreich!!!
		return true;
	}

}
