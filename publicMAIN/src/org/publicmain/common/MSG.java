package org.publicmain.common;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.publicmain.nodeengine.NodeEngine;



/**
 * Diese Klasse repräsentiert unser Datenpaket ggf. von Serializable auf
 * Externalized umstellen wenn Dateneingespart werden müssen da nicht alle
 * Paktearten alle Felder benötigen
 * 
 * @author ATRM
 * 
 */

/**
  
 * 	GROUP_MESSAGE
 * 
 * 	NODE_UPDATE("inform on node changes"),
	ALIAS_UPDATE("inform on alias changes"),
	NODE_LOOKUP("requests for nodeupdate"), 
	ECHO_REQUEST("ping request"),
	ECHO_RESPONSE("ping response"),
	NODE_SHUTDOWN("inform about own nodesshutdown"),
	
	GROUP_POLL("ask for group members"),
	GROUP_REPLY("group answer"),
	GROUP_JOIN("inform about group join"),
	GROUP_LEAVE("inform about group leave"),
	GROUP_EMPTY("inform about an empty group"),
	GROUP_ANNOUNCE("register a group"),

	FILE_REQUEST("beschreibungEinfügen"),
	FILE_REPLY("beschreibungEinfügen"),
	FILE_RECIEVED("beschreibungEinfügen"),
	FILE_TCP_REQUEST("beschreibungEinfügen"),
	FILE_TCP_REPLY("beschreibungEinfügen"),
	FILE_TCP_ABORT("file refused"),
 * 
 * 
 * @author Kaddi
 *
 */
public class MSG implements Serializable, Comparable<MSG> {



	
	private static int id_counter = 0;
	private static final long serialVersionUID = 270420135L;

	// Typisierung
	private byte code = -1;
	// Quelle und Eindeutigkeit
	private final long sender;
	//private long timestamp;
	private final int id;
	// Optionale Datenfelder für beispielsweise Empfänger
	private final long empfänger;
	private String group;
	// Payload
	private Object data;

	/**
	 * Konstruktor erstellt eine Nachricht eines mitgegebenen Typs
	 * @param empfänger TODO
	 * @param typ der gewünschte Nachrichtentyp
	 */
	private MSG(byte code, long empfänger) {
		this.id = getnextID();
		this.code=code;
		this.sender= NodeEngine.getNE().getMe().getNodeID();
		this.empfänger=-1;
	}

	/**
	 * Getter für einen Zähler, addiert 1 dazu wenn er aufgerufen wird.
	 * 
	 * @return liefert die nächste ID zurück 
	 */
	private static synchronized int getnextID() {
		return id_counter++;
	}

	/**
	 * Konstruktor für Nachrichten
	 * 
	 * @param typ	typ der zu erstellenden Nachricht 
	 * @param code	code der zu erstellenden Nachricht
	 * @param sender der in die Nachricht einzutragende Sender
	 * @param empfänger der in die Nachricht einzutragende Empfaenger
	 * @param group	die in die Nachricht einzutragende Gruppe
	 * @param data	die in der Nachricht zu Speichernde Daten
	 */
	public MSG(byte code, long sender, long empfänger, String group, Object data) {
		this.sender = sender;
		this.id = getnextID();
		this.empfänger = empfänger;
		this.group = group;
		this.data = data;
	}

	/**
	 * An diesen Konstruktor kann Systemnachrichten speichern
	 * 
	 * @param payload Dateninhalt der SystemNachricht
	 * @param code	MSGCode der zu erstellenden Nachricht
	 */
	public MSG(Object payload, byte code){
		this(code, -1);
		this.data = payload;
	}

	/**
	 * Dieser Konstruktor erstellt eine Nachricht
	 * 
	 * @param payload beinhaltet Dateninhalt 
	 * @param code	beinhaltet MSGCode
	 * @param recipient	Empfaenger
	 */
	public MSG(Object payload, byte code, long recipient) {
		this(code,recipient);
		this.data=payload;
	}

	/**
	 * Konstruktor für Node-Updates
	 * 
	 * @param daNode
	 */
	public MSG(Node daNode){
		this(daNode,MSGCode.NODE_UPDATE);
	}

	/**
	 * Konstruktor für Gruppen-Nachrichten
	 * 
	 * @param group
	 * @param text
	 */
	public MSG(String group,String text){
		this(MSGCode.GROUP_MESSAGE,-1);
		this.group=group.toLowerCase();
		this.data=text;
	}

	/**
	 * Konstruktor für Private-Nachrichten
	 * 
	 * @param user
	 * @param text
	 */
	public MSG(long user, String text){
		this(MSGCode.PRIVATE_MESSAGE,user);
		this.data=text.getBytes();
	}

//	Ggf. für die weitere Entwicklung benötigt.
//	/**
//	 * Konstruktor für den Dateiversand
//	 * 
//	 * @param datei
//	 * @param nid
//	 * @throws IOException
//	 */
//	public MSG(File datei, long nid) throws IOException {
//		this(NachrichtenTyp.DATA);
//		if(!datei.isFile())throw new IOException("Verzeichnisse werden nicht unterstützt.");
//		ByteArrayOutputStream bout = new ByteArrayOutputStream();
//		GZIPOutputStream zip = new GZIPOutputStream(bout);
//		BufferedOutputStream bos = new BufferedOutputStream(zip);
//		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(datei));
//		byte[] cup=new byte[64000];
//		int leng=-1;
//		while((leng=bis.read(cup))!=-1){
//				bos.write(cup,0,leng);
//		}
//		bos.flush();
//		zip.finish();
//		bos.close();
//		Object tmp_data[]=new Object[2];
//		tmp_data[0]=datei;
//		tmp_data[1]=bout.toByteArray();
//		data=tmp_data;
//		setEmpfänger(nid);
//		group=datei.getName();
//	}

//	/**
//	 * Konstruktor für den Dateiversand
//	 * 
//	 * @param tmp_FR
//	 * @throws IOException
//	 */
//	public MSG(FileTransferData tmp_FR) throws IOException {
//		this(NachrichtenTyp.DATA);
//		this.empfänger = tmp_FR.getReceiver_nid();
//		if (!tmp_FR.datei.isFile())
//			throw new IOException("Verzeichnisse werden nicht unterstützt.");
//		ByteArrayOutputStream bout = new ByteArrayOutputStream();
//		GZIPOutputStream zip = new GZIPOutputStream(bout);
//		BufferedOutputStream bos = new BufferedOutputStream(zip);
//		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
//				tmp_FR.datei));
//		byte[] cup = new byte[64000];
//		int leng = -1;
//		while ((leng = bis.read(cup)) != -1) {
//			bos.write(cup, 0, leng);
//		}
//		bos.flush();
//		zip.finish();
//		bos.close();
//		bis.close();
//		Object tmp_data[] = new Object[2];
//		tmp_data[0] = tmp_FR;
//		tmp_data[1] = bout.toByteArray();
//		data = tmp_data;
//	}

//	/**
//	 * Methode zum Abspeichern von empfangenen Dateien. 
//	 * 
//	 * @param datei
//	 * @throws IOException
//	 */
//	public void save(File datei) throws IOException {
//		if ((typ == NachrichtenTyp.DATA) && (data != null)
//				&& (data instanceof Object[])
//				&& (((Object[]) data).length == 2)
//				&& (((Object[]) data)[0] instanceof FileTransferData)
//				&& (((Object[]) data)[1] instanceof byte[])) {
//			byte[] tmp_data = (byte[]) ((Object[]) data)[1];
//
//			ByteArrayInputStream bais = new ByteArrayInputStream(tmp_data);
//			GZIPInputStream zip = new GZIPInputStream(bais);
//			BufferedInputStream bis = new BufferedInputStream(zip);
//
//			BufferedOutputStream bos = new BufferedOutputStream(
//					new FileOutputStream(datei));
//			byte[] cup = new byte[64000];
//			int leng = -1;
//			while ((leng = bis.read(cup)) != -1) {
//				bos.write(cup, 0, leng);
//			}
//			bos.flush();
//			bos.close();
//		}
//	}

	/**
	 * Diese Methode errechnet einen Hashcode für diese Message
	 * unabhängig vom Inhalt um sie in der Hashmap vergleichen zu können
	 * 
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + id;
		result = (prime * result) + (int) (sender ^ (sender >>> 32));
		return result;
	}

	/**
	 * Methode für den String-Vergleich, liefert abhängig vom Vergleich
	 * <code>true</code> oder <code>false</code> zurück.
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MSG other = (MSG) obj;
		if (id != other.id)
			return false;
		if (sender != other.sender)
			return false;
		return true;
	}

	/**
	 * Getter für die Empfänger NodeID.
	 * 
	 * @return die nodeID des Empfängers
	 */
	public long getEmpfänger() {
		return empfänger;
	}
	
	/**
	 * Getter für die Sender NodeID
	 * 
	 * @return die nodeID des Empfängers
	 */
	public long getSender() {
		return sender;
	}


	/**
	 * Getter fürden Nachrichten-Code.
	 * 
	 * @return der MSGCode dieser Nachricht
	 */
	public byte getCode() {
		return code;
	}
	
	/**
	 * Getter für für die Daten einer Nachricht
	 * 
	 * @return die Daten dieser Nachricht
	 */
	public Object getData() {
		return data;
	}
	
	/**
	 * Getter für die Empfänger-Gruppe. 
	 * 
	 * @return die Gruppe dieser Nachricht
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Getter für die ID der Nachricht
	 * 
	 * @return die ID dieser Nachricht
	 */
	public int getId() {
		return id;
	}


	/**
	 * Eigene toString()-Methode
	 */
	public String toString() {
		return "MSG [" + (code >=0 ? "code=" + code + ", " : "") + "sender="
				+ sender + ", id=" + id
				+ ", empfänger=" + empfänger + ", "
				+ (group != null ? "group=" + group + ", " : "")
				+ (data != null ? "data=" + data : "") + "]";
	}

	/**
	 * Methode zum umwandeln von Objekten in einen Byte-Strom, liefert ein
	 * Byte-Array zurück.
	 * 
	 * @param x die Nachricht die umgewandelt werden soll 
	 * @return ein ByteArray
	 */
	public static byte[] getBytes(MSG x) {
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

	/**
	 * Methode zum umwandeln eines Byte-Arrays in ein MSG-Objekt.
	 * 
	 * @param data umzuwandeldes ByteArray
	 * @return	umgewandeltes ByteArray in Form einer MSG
	 */
	public static MSG getMSG(byte[] data) {
		try {
			ObjectInputStream obin = new ObjectInputStream(
					new ByteArrayInputStream(data));
			MSG tmp = (MSG) obin.readObject();
			return tmp;

		} catch (Exception e) {
			LogEngine.log("MSG.getMSG", e);
		}
		return null;
	}

	/**
	 * Eigene Vergleichsmethode.
	 */
	public int compareTo(MSG o) {
		if (this.getSender() != o.getSender()) {
			return (this.getSender() > o.getSender()) ? 1 : -1;
		} else if (this.getId() != o.getId()) {
			return (this.getId() - o.getId());
		}
		return 0;
	}

	/**
	 * Setter für den Gruppen-Namen. 
	 * 
	 * @param string zu setzender Gruppenname
	 */
	public void setGroup(String string) {
		group=string;
	}
}
