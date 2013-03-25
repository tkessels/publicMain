package org.publicmain.nodeengine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.text.StyledEditorKit.UnderlineAction;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.*;
import org.publicmain.gui.GUI;



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
	private Node connectedWith;
	private NodeEngine ne;
	private int zustand=NOT_CONNECTED;
	
	
	public ConnectionHandler(Socket underlying) throws IOException{
		System.out.println("Connected");
		ne=NodeEngine.getNE();
		System.out.println("Connected");
		pakets_rein_hol_bot = new Thread(new reciever());
		System.out.println("Connected");
		line = underlying;
		System.out.println("Connected");
		line_out=new ObjectOutputStream(new BufferedOutputStream(line.getOutputStream()));
		System.out.println("Connected");
		line_out.flush();
		System.out.println("Connected");
		line_in=new ObjectInputStream(new BufferedInputStream(line.getInputStream()));
		System.out.println("Connected");
		zustand=CONNECTED;
		System.out.println("Connected");
		line_out.writeObject(ne.getME());
		line_out.flush();
		
		
		
		try {//versuche erstes Paket zu interpretieren
			Object first=line_in.readObject();
			System.out.println("lese");	
			if (first instanceof Node){
				connectedWith = (Node) first;
				zustand=CHATMODE;
				LogEngine.log("Verbindung mit " + connectedWith + " hergestellt.", this, LogEngine.INFO);
				pakets_rein_hol_bot.start();
			}
			
			else if(first instanceof MSG &&((MSG)first).getTyp()==NachrichtenTyp.SYSTEM &&((MSG)first).getCode()==MSG.FILE_REQUEST ){
				LogEngine.log("Datenempfang gefordert von " + ((MSG)first).getSender() + ".", this, LogEngine.INFO);
				zustand=DATAMODE;
				File tmp = GUI.getGUI().request_File();
				//hier stream ich den Netzwerk müll in die Datei auf die Platte
			}
			
		} catch (ClassNotFoundException e) {
			LogEngine.log(e);
		}

		
		System.out.println("Finished");
		
		
	}
	
	
	



	/**Verschickt ein MSG-Objekt über den Soket.
	 * @param paket Das zu versendende Paket
	 * @throws IOException Wenn es zu einem Fehler beim senden auf dem TCP-Socket kommt
	 */
	public void send(MSG paket) throws IOException{
			line_out.writeObject(paket);
			line_out.flush();
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
					ne.handle(tmp);
				} 
				catch (ClassNotFoundException|IOException e) 
				{
					LogEngine.log(e);
				} 
			}
		}		
	}
	
	public Node getConnectionPartner(){
		return connectedWith;
	}

	public boolean isConnected() {
		return line.isConnected();
	}
}
