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
 * Diese Klasse repr�sentiert unser Datenpaket ggf. von Serializable auf
 * Externalized umstellen wenn Dateneingespart werden m�ssen da nicht alle
 * Paktearten alle Felder ben�tigen
 * 
 * @author ATRM
 * 
 */

public class MSG implements Serializable, Comparable<MSG> {
	private static int id_counter = 0;
	private static final long serialVersionUID = 270420135L;

	// Typisierung
	private final NachrichtenTyp typ;
	private MSGCode code;
	// Quelle und Eindeutigkeit
	private final long sender;
	private long timestamp;
	private final int id;
	// Optionale Datenfelder f�r beispielsweise Empf�nger
	private long empf�nger = -1;
	private String group;
	// Payload
	private Object data;

	/**
	 * Konstruktor erstellt eine Nachricht eines mitgegebenen Typs
	 * 
	 * @param typ der gew�nschte Nachrichtentyp
	 */
	private MSG(NachrichtenTyp typ) {
		this.id = getnextID();
		this.typ=typ;
		this.timestamp=System.currentTimeMillis();
		this.sender= NodeEngine.getNE().getMe().getNodeID();
	}

	/**
	 * Getter f�r einen Z�hler, addiert 1 dazu wenn er aufgerufen wird.
	 * 
	 * @return liefert die n�chste ID zur�ck 
	 */
	private static synchronized int getnextID() {
		return id_counter++;
	}

	/**
	 * Konstruktor f�r Nachrichten
	 * 
	 * @param typ	typ der zu erstellenden Nachricht 
	 * @param code	code der zu erstellenden Nachricht
	 * @param sender der in die Nachricht einzutragende Sender
	 * @param empf�nger der in die Nachricht einzutragende Empfaenger
	 * @param group	die in die Nachricht einzutragende Gruppe
	 * @param data	die in der Nachricht zu Speichernde Daten
	 */
	public MSG(NachrichtenTyp typ, MSGCode code, long sender, long empf�nger, String group, Object data) {
		this.typ = typ;
		this.code = code;
		this.sender = sender;
		this.timestamp = System.currentTimeMillis();
		this.id = getnextID();
		this.empf�nger = empf�nger;
		this.group = group;
		this.data = data;
	}

	/**
	 * An diesen Konstruktor kann Systemnachrichten speichern
	 * 
	 * @param payload Dateninhalt der SystemNachricht
	 * @param code	MSGCode der zu erstellenden Nachricht
	 */
	public MSG(Object payload, MSGCode code){
		this(NachrichtenTyp.SYSTEM);
		this.code = code;
		this.data = payload;
	}

	/**
	 * Dieser Konstruktor erstellt eine Nachricht
	 * 
	 * @param payload beinhaltet Dateninhalt 
	 * @param code	beinhaltet MSGCode
	 * @param recipient	Empfaenger
	 */
	public MSG(Object payload, MSGCode code, long recipient) {
		this(payload,code);
		this.empf�nger=recipient;
	}

	/**
	 * Konstruktor f�r System-Nachrichten
	 * 
	 * @param daNode
	 */
	public MSG(Node daNode){
		this(NachrichtenTyp.SYSTEM);
		this.code=MSGCode.NODE_UPDATE;
		this.data=daNode;
	}

	/**
	 * Konstruktor f�r Gruppen-Nachrichten
	 * 
	 * @param group
	 * @param text
	 */
	public MSG(String group,String text){
		this(NachrichtenTyp.GROUP);
		this.group=group.toLowerCase();
		this.data=text;
	}

	/**
	 * Konstruktor f�r Private-Nachrichten
	 * 
	 * @param user
	 * @param text
	 */
	public MSG(long user, String text){
		this(NachrichtenTyp.PRIVATE);
		this.empf�nger=user;
		this.data=text;
	}

//	Ggf. f�r die weitere Entwicklung ben�tigt.
//	/**
//	 * Konstruktor f�r den Dateiversand
//	 * 
//	 * @param datei
//	 * @param nid
//	 * @throws IOException
//	 */
//	public MSG(File datei, long nid) throws IOException {
//		this(NachrichtenTyp.DATA);
//		if(!datei.isFile())throw new IOException("Verzeichnisse werden nicht unterst�tzt.");
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
//		setEmpf�nger(nid);
//		group=datei.getName();
//	}

	/**
	 * Konstruktor f�r den Dateiversand
	 * 
	 * @param tmp_FR
	 * @throws IOException
	 */
	public MSG(FileTransferData tmp_FR) throws IOException {
		this(NachrichtenTyp.DATA);
		this.empf�nger = tmp_FR.getReceiver_nid();
		if (!tmp_FR.datei.isFile())
			throw new IOException("Verzeichnisse werden nicht unterst�tzt.");
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		GZIPOutputStream zip = new GZIPOutputStream(bout);
		BufferedOutputStream bos = new BufferedOutputStream(zip);
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
				tmp_FR.datei));
		byte[] cup = new byte[64000];
		int leng = -1;
		while ((leng = bis.read(cup)) != -1) {
			bos.write(cup, 0, leng);
		}
		bos.flush();
		zip.finish();
		bos.close();
		bis.close();
		Object tmp_data[] = new Object[2];
		tmp_data[0] = tmp_FR;
		tmp_data[1] = bout.toByteArray();
		data = tmp_data;
	}

	/**
	 * Methode zum Abspeichern von empfangenen Dateien. 
	 * 
	 * @param datei
	 * @throws IOException
	 */
	public void save(File datei) throws IOException {
		if ((typ == NachrichtenTyp.DATA) && (data != null)
				&& (data instanceof Object[])
				&& (((Object[]) data).length == 2)
				&& (((Object[]) data)[0] instanceof FileTransferData)
				&& (((Object[]) data)[1] instanceof byte[])) {
			byte[] tmp_data = (byte[]) ((Object[]) data)[1];

			ByteArrayInputStream bais = new ByteArrayInputStream(tmp_data);
			GZIPInputStream zip = new GZIPInputStream(bais);
			BufferedInputStream bis = new BufferedInputStream(zip);

			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(datei));
			byte[] cup = new byte[64000];
			int leng = -1;
			while ((leng = bis.read(cup)) != -1) {
				bos.write(cup, 0, leng);
			}
			bos.flush();
			bos.close();
		}
	}

	/**
	 * Diese Methode errechnet einen Hashcode f�r diese Message
	 * unabh�ngig vom Inhalt um sie in der Hashmap vergleichen zu k�nnen
	 * 
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + id;
		result = (prime * result) + (int) (sender ^ (sender >>> 32));
		result = (prime * result) + (int) (timestamp ^ (timestamp >>> 32));
		return result;
	}

	/**
	 * Methode f�r den String-Vergleich, liefert abh�ngig vom Vergleich
	 * <code>true</code> oder <code>false</code> zur�ck.
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
		if (timestamp != other.timestamp)
			return false;
		return true;
	}

	/**
	 * Getter f�r die Empf�nger NodeID.
	 * 
	 * @return die nodeID des Empf�ngers
	 */
	public long getEmpf�nger() {
		return empf�nger;
	}
	
	/**
	 * Getter f�r die Sender NodeID
	 * 
	 * @return die nodeID des Empf�ngers
	 */
	public long getSender() {
		return sender;
	}
	
	/**
	 * Getter f�r den Zeitstempel.
	 * 
	 * @return den Zeitstempel der Nachricht
	 */
	public long getTimestamp() {
		return timestamp;
	}
	
	/**
	 * Diese Methode setzt den Zeitstempel, auf die aktuelle Zeit.
	 */
	public void reStamp() {
		timestamp = System.currentTimeMillis();
	}

	/**
	 * Getter f�r den Nachrichtentyp.
	 * 
	 * @return der NachrichtenTyp dieser Nachricht
	 */
	public NachrichtenTyp getTyp() {
		return typ;
	}

	/**
	 * Getter f�rden Nachrichten-Code.
	 * 
	 * @return der MSGCode dieser Nachricht
	 */
	public MSGCode getCode() {
		return code;
	}
	
	/**
	 * Getter f�r f�r die Daten einer Nachricht
	 * 
	 * @return die Daten dieser Nachricht
	 */
	public Object getData() {
		return data;
	}
	
	/**
	 * Getter f�r die Empf�nger-Gruppe. 
	 * 
	 * @return die Gruppe dieser Nachricht
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Getter f�r die ID der Nachricht
	 * 
	 * @return die ID dieser Nachricht
	 */
	public int getId() {
		return id;
	}

//	Ggf. f�r die weitere Entwicklung ben�tigt.
//	/**
//	 * Setter f�r den Empf�nger
//	 * 
//	 * @param value
//	 */
//	public void setEmpf�nger(long value) {
//		empf�nger=value;
//	}

	/**
	 * Eigene toString()-Methode
	 */
	public String toString() {
		return "MSG [" + (typ != null ? "typ=" + typ + ", " : "")
				+ (code != null ? "code=" + code + ", " : "") + "sender="
				+ sender + ", timestamp=" + timestamp + ", id=" + id
				+ ", empf�nger=" + empf�nger + ", "
				+ (group != null ? "group=" + group + ", " : "")
				+ (data != null ? "data=" + data : "") + "]";
	}

	/**
	 * Methode zum umwandeln von Objekten in einen Byte-Strom, liefert ein
	 * Byte-Array zur�ck.
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
		if (this.getTimestamp() != o.getTimestamp()) {
			return (this.getTimestamp() > o.getTimestamp()) ? 1 : -1;
		} else if (this.getSender() != o.getSender()) {
			return (this.getSender() > o.getSender()) ? 1 : -1;
		} else if (this.getId() != o.getId()) {
			return (this.getId() - o.getId());
		}
		return 0;
	}

	/**
	 * Setter f�r den Gruppen-Namen. 
	 * 
	 * @param string zu setzender Gruppenname
	 */
	public void setGroup(String string) {
		group=string;
	}
}
