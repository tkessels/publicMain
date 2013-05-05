import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import org.publicmain.common.Config;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.NachrichtenTyp;
import org.publicmain.common.Node;
import org.resources.Help;

/**
 * @author ATRM
 * Diese Klasse beinhaltet die Funktionalit�t des Backupserverprogrammes und die notwendige Starterklasse
 * Der Backupserver wird als Datensicherungsm�glichkeit f�r das Chatprogramm publicMain angeboten
 *
 */
public class Backupserver {
	private final static  int BACKUP_DATABASE_VERSION = 12345;
	private static Connection con;
	private static Statement stmt;
	private static String local_username= "root";
	private static String local_password="";
	/**
	 * Diese Methode ist der Einstiegspunkt f�r den Backupserver
	 * Sie �berpr�ft die mitgegebene Parameter und setzt diese entsprechend um 
	 * @param args	mitgegebene(r) Parameter
	 */
	public static void main(String[] args) {
		System.setProperty("appname", "pmBackupServer");
		String username = Config.getConfig().getBackupDBUser();
		String password = Config.getConfig().getBackupDBPw();
		String port=Config.getConfig().getLocalDBPort();
		String databasename=Config.getConfig().getBackupDBDatabasename();
		String ip=Node.getMyIPs().get(0).getHostAddress();

		Random x = new Random(System.currentTimeMillis());
		long nid = x.nextLong();
		nid *=Long.signum(nid);
		//parse parameters
		if(args.length>0) {
			if ((args.length%2)==0) {
				for (int i = 0; i < (args.length/2); i++) {
					switch (args[i]) {
					case "-u":
					case "--user":
						local_username=args[i+1];
						break;
					case "-p":
					case "--pass":
						local_password=args[i+1];
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
		if(checkConnection(ip,port,local_username,local_password)) {
			//test version of pm-Database on mySQL server OR create it 
			if(checkVersion() || createDatabase()) {

				//everything is fine start offering backupserver
				try (MulticastSocket backupserver = new MulticastSocket(Config.getConfig().getMCPort())){
					InetAddress mcgroup = InetAddress.getByName(Config.getConfig().getMCGroup());
					backupserver.setLoopbackMode(true);
					backupserver.joinGroup( mcgroup );
					backupserver.setTimeToLive(Config.getConfig().getMCTTL());


					if (!ip.equals(Config.getConfig().getBackupDBIP())) {
						Config.getConfig().setBackupDBIP(ip);
					}
					if (!port.equals(Config.getConfig().getBackupDBPort())) {
						Config.getConfig().setBackupDBPort(port);
					}
					if (!username.equals(Config.getConfig().getBackupDBUser())) {
						Config.getConfig().setBackupDBUser(username);
					}
					if (!password.equals(Config.getConfig().getBackupDBPw())) {
						Config.getConfig().setBackupDBPw(password);
					}
					if (!databasename.equals(Config.getConfig().getBackupDBDatabasename())) {
						Config.getConfig().setBackupDBDatabasename(databasename);
					}
					Config.write();
					System.out.println("BackupServer is Running:");
					System.out.println(Config.getConfig());
					while (true) {
						byte[] buff = new byte[65535];
						DatagramPacket tmp = new DatagramPacket(buff, buff.length);
						try {
							backupserver.receive(tmp);
							MSG nachricht = MSG.getMSG(tmp.getData());
							LogEngine.log("BackupServer", "recieved", nachricht);

							if((nachricht.getTyp()==NachrichtenTyp.SYSTEM)&&(nachricht.getCode()==MSGCode.BACKUP_SERVER_DISCOVER)) {
								MSG reply = new MSG(NachrichtenTyp.SYSTEM, MSGCode.BACKUP_SERVER_OFFER, -1,-1, "", Config.getConfig());

								byte[] buf = MSG.getBytes(reply);
								if (buf.length < 65000) {
									backupserver.send(new DatagramPacket(buf, buf.length, tmp.getAddress(),tmp.getPort()));
									LogEngine.log("BackupServer", "sending", reply);
								} else {
									LogEngine.log("BackupServer", "MSG zu gro� f�r UDP-Paket", LogEngine.ERROR);
								}
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
				System.exit(1);
			}
		}else {
			System.err.println("Could not connect to given Database. Shutting down!");
			System.err.println("server\t\t:" + ip+":"+port);
			System.err.println("user\t\t\t:" + local_username);
			System.err.println("password\t:" + local_password);
			System.err.println("database\t\t:" + Config.getConfig().getBackupDBDatabasename());
			System.exit(1);
		}

	}

	/**
	 * Diese Methode gibt usage-Infos zum starten aus.
	 */
	private static void printUsageInfoExit() {
		String ip = "127.0.0.1";
		if (Node.getMyIPs().size()>=1) {
			ip = Node.getMyIPs().get(0).getHostAddress();
		}
		System.err.println("Usage:");
		System.err.println("	java Backupserver -u <username> -p <password> -s <ip:port> -n <database_name>");
		System.err.println("	java Backupserver --user <username> --pass <password> --ip <ip> --port <port> --database <database_name>");
		System.err.println("\nNot provided parameters will be set to default values!\n");
		System.err.println("Example:");
		System.err.println("	java Backupserver -u admin --pass secret -n db1");
		System.err.println("		Will setup the backupserver for a local Database ("+ip+":"+Config.getConfig().getBackupDBPort()+") with user \""+Config.getConfig().getBackupDBUser()+"\" and password:\""+Config.getConfig().getBackupDBPw()+"\" ");
		System.err.println("		Necessary databases and users will be created if database is localy reachable with user:" +local_username + " password:"+local_password  );
		System.exit(1);
	}


	/**
	 * Diese Methode stellt eine Verbindung zum SQL-Server her und nutzt dazu die gegebenen Parameter
	 * @param ip		IP-Adresse des Servers zu dem verbunden werden soll
	 * @param port		Port des Servers, welcher f�r die Verbindung genutzt werden soll
	 * @param username	Username mit welchem Verbunden werden soll
	 * @param password	Passwort mit welchem Verbunden werden soll
	 * @return true		Verbindung hergestellt
	 * @return false	Verbindung konnte nicht hergestellt werden
	 */
	private static boolean checkConnection(String ip, String port, String username, String password) {	
		Config.getConfig().getBackupDBDatabasename();
		String url			= "jdbc:mysql://localhost:"+ port + "/";	//TODO: IP not used. Caused faults. Just "localhost" and "127.0.0.1" seems to be possible.
		try {
			con = DriverManager.getConnection(url, username, password);
			stmt = con.createStatement();
			return true;
		} catch (SQLException e) {
			LogEngine.log("Fehler beim verbinden mit " + url + " : " + e.getMessage(),LogEngine.ERROR);
			return false;
		}
	}
	
	/**
	 * Diese Methode �berpr�ft die gespeicherte BackupDB-Version in der Config mit der lokalen
	 * @return true		Version aktuell
	 * @return fasle	Version nicht aktuell
	 */
	private static boolean checkVersion() {
		Config.getConfig().getBackupDBDatabasename();
		int x = 123123;
		//TODO:Testen ob Datenbank Version die richtige ist

		if (BACKUP_DATABASE_VERSION==x)
			return true;
		return false;
	}

	/**
	 * Diese Methode legt die f�r den Betrieb notwenidge Datebank mithilfe eines Scripts sowie
	 * Prozeduren an
	 * @return	true
	 * @return	false
	 */
	private static boolean createDatabase() {
		String databasename = Config.getConfig().getBackupDBDatabasename();
		synchronized (stmt) {
			try {
				stmt.execute("use " + databasename);
				return true;
			} catch (SQLException e) {
				String read=null;
				try (BufferedReader in = new BufferedReader(new FileReader(Help.getFile("create_BackupDB.sql")))){
					while((read = in.readLine()) != null) {
						while (!read.endsWith(";") && !read.endsWith("--")){
							read = read + in.readLine();
						}
						stmt.execute(read);
					}
					stmt.addBatch("DROP procedure IF EXISTS `db_publicmain`.`p_t_msgType`;");
					stmt.addBatch("CREATE PROCEDURE `db_publicmain_backup`.`p_t_msgType`(IN newMsgTypeID INT,IN newName VARCHAR(45),IN newDescription VARCHAR(45)) BEGIN INSERT INTO t_msgType (msgTypeID, name, description) VALUES (newMsgTypeID,newName,newDescription) ON DUPLICATE KEY UPDATE name=VALUES(name),description=VALUES(description); END;");

					//Hier wird die Tabelle t_msgType automatisch bef�llt!
					for (MSGCode c : MSGCode.values()){
						stmt.addBatch("CALL p_t_msgType(" + c.ordinal() + ",'" + c.name() + "','" +  c.getDescription() + "')");
					}
					stmt.executeBatch();
					createClientUser();
					return true;
				} catch (FileNotFoundException e1) {
					LogEngine.log("Error caused by reading create_BackupDB.sql: " + e1.getMessage(), LogEngine.ERROR);
					return false;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SQLException e1) {
					LogEngine.log("Error caused executing a sql statment: " + e1.getMessage(), LogEngine.ERROR);
				}
				return false;
			}
		}
	}

	/**
	 * Diese Methode l�scht zun�chst den Anonymos-Nutzer und legt anschlie�end 
	 * den Nutzer f�r den Normalbetieb 'backupPublicMain' an
	 * @return true		Anlegen erfolgreich
	 * @return false	Fehler beim Anlegen
	 */
	private static boolean createClientUser(){
		try {

			stmt.addBatch("DELETE FROM mysql.user WHERE user =''");
			stmt.addBatch("CREATE USER 'backupPublicMain' IDENTIFIED BY 'backupPublicMain'");
			//			stmt.addBatch("GRANT ALL ON `db_publicmain_backup`.* TO 'backupPublicMain'");
			stmt.addBatch("GRANT ALL PRIVILEGES ON * . * TO  'backupPublicMain'@'%' WITH GRANT OPTION MAX_QUERIES_PER_HOUR 0 MAX_CONNECTIONS_PER_HOUR 0 MAX_UPDATES_PER_HOUR 0 MAX_USER_CONNECTIONS 0");
			stmt.executeBatch();
			return true;
		} catch (SQLException e) {
			//Benutzer gab es evtl schonmal
			return false;
		}
	}
}
