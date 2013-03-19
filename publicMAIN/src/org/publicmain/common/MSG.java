package org.publicmain.common;
import org.publicmain.nodeengine.NodeEngine;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;




/**Diese Klasse repräsentiert unser Datenpaket
 * ggf. von Serializable auf Externalized umstellen wenn Dateneingespart werden müssen da nicht alle Paktearten alle Felder benötigen
 * @author tkessels
 *
 */
public class MSG implements Serializable{
	private static Integer id_counter=0;
	private static final long serialVersionUID = -2010661171218754968L;
	//SystemMessage Codes
	public static final byte NODE_UPDATE		=		0;
	public static final byte ALIAS_UPDATE		=		1;
	
	public static final byte ECHO_REQUEST		=		10;
	public static final byte ECHO_RESPONSE		=	-	10;
	public static final byte ROOT_DISCOVERY		=		20;
	public static final byte ROOT_REPLY			=	-	20;
	public static final byte POLL_CHILDNODES	=		30;
	public static final byte REPORT_CHILDNODES	=	-	30;

	public static final byte NODE_SHUTDOWN		=		40;
	
	
	public static final byte GROUP_POLL			=		50;
	public static final byte GROUP_REPLY		=		51;
	public static final byte GROUP_JOIN			=		52;
	public static final byte GROUP_LEAVE		=		53;
	public static final byte GROUP_EMPTY		=		54;
	
	public static final byte FILE_REQUEST		=		60;
	
	public static final byte CMD_SHUTDOWN		=		70;
	public static final byte CMD_RESTART		=		71;
	

	
	
	
	
	
	
	//Typisierung
	private NachrichtenTyp typ;
	private byte code;
	//Quelle und Eindeutigkeit
	private long sender;
	private long timestamp;
	private int id;
	//Empfänger
	private long empfänger;
	private String group;
	//Payload
	private Object data;
	

	private MSG() {
		synchronized (id_counter) {
			this.id=id_counter;
			MSG.id_counter++;
		}
		this.timestamp=System.currentTimeMillis();
		this.sender= NodeEngine.getNE().getME().getNodeID();
	}
	
	public MSG(String text){
		this("public",text);
	}
	
	public MSG(String group,String text){
		this();
		this.typ=NachrichtenTyp.GROUP;
		this.group=group.toLowerCase();
		this.data=text;
	}
	
	public MSG(long user, String text){
		this();
		this.typ=NachrichtenTyp.PRIVATE;
		this.empfänger=user;
		this.data=text;
	}
	
	public long getEmpfänger() {
		return empfänger;
	}

	public long getSender() {
		return sender;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public NachrichtenTyp getTyp() {
		return typ;
	}

	public byte getCode() {
		return code;
	}

	public Object getData() {
		return data;
	}
	
	public String getGroup() {
		return group;
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "MSG [" + (typ != null ? "typ=" + typ + ", " : "") + "code="
				+ code + ", sender=" + sender + ", timestamp=" + timestamp
				+ ", id=" + id + ", empfänger=" + empfänger + ", "
				+ (group != null ? "group=" + group + ", " : "")
				+ (data != null ? "data=" + data : "") + "]";
	}
	
	
	/**Erzeugt eine Liste aller lokal vergebenen IP-Adressen mit ausnahme von Loopbacks und IPV6 Adressen
	 * @return Liste aller lokalen IPs
	 */
	public static List<InetAddress> getMyIPs() {
		List<InetAddress> addrList = new ArrayList<InetAddress>();
		try {
			for (InetAddress inetAddress : InetAddress.getAllByName(InetAddress.getLocalHost().getHostName())) { //Finde alle IPs die mit meinem hostname assoziert sind und 
			if (inetAddress.getAddress().length==4)addrList.add(inetAddress);									 //füge die meiner liste hinzu die IPV4 sind also 4Byte lang
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return addrList;
	}
	
	
	public static byte[] getBytes(MSG x){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream obout = new ObjectOutputStream(bos);
			obout.writeObject(x);
			obout.flush();
		} catch (IOException e) {
			LogEngine.log(e);
		}
		return bos.toByteArray();
	}
	
	public static MSG getMSG(byte[] data){
		try {
			ObjectInputStream obin=new ObjectInputStream( new ByteArrayInputStream(data));
			MSG tmp = (MSG)obin.readObject();
			return tmp;
			
		} catch (Exception e) {
			LogEngine.log(e);
		}
		return null;
	}

	



}
