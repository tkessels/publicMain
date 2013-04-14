package org.publicmain.common;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.publicmain.nodeengine.NodeEngine;




/**Diese Klasse repräsentiert unser Datenpaket
 * ggf. von Serializable auf Externalized umstellen wenn Dateneingespart werden müssen da nicht alle Paktearten alle Felder benötigen
 * @author tkessels
 *
 */
public class MSG implements Serializable,Comparable<MSG>{
	private static Integer id_counter=0;
	private static final long serialVersionUID = 899L;

	//Typisierung
	private final NachrichtenTyp typ;
	private MSGCode code;
	//Quelle und Eindeutigkeit
	private final long sender;
	private long timestamp;
	private final int id;
	//Optionale Datenfelder für beispielsweise Empfänger
	private long empfänger;
	private String group;
	//Payload
	private Object data;

	private MSG(NachrichtenTyp typ) {
		synchronized (id_counter) {
			this.id=id_counter;
			MSG.id_counter++;
		}
		this.typ=typ;
		this.timestamp=System.currentTimeMillis();
		this.sender= NodeEngine.getNE().getMe().getNodeID();
	}

	public MSG(Object payload, MSGCode code){
		this(NachrichtenTyp.SYSTEM);
		this.code = code;
		this.data = payload;
	}
	
	public MSG(Object payload, MSGCode code, long recipient) {
		this(payload,code);
		this.empfänger=recipient;
	}

	public MSG(Node daNode){
		this(NachrichtenTyp.SYSTEM);
		this.code=MSGCode.NODE_UPDATE;
		this.data=daNode;
	}

	public MSG(String group,String text){
		this(NachrichtenTyp.GROUP);
		this.group=group.toLowerCase();
		this.data=text;
	}

	public MSG(long user, String text){
		this(NachrichtenTyp.PRIVATE);
		this.empfänger=user;
		this.data=text;
	}

/*
	public MSG(File datei, long nid) throws IOException {
		this(NachrichtenTyp.DATA);
		if(!datei.isFile())throw new IOException("Verzeichnisse werden nicht unterstützt.");
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		GZIPOutputStream zip = new GZIPOutputStream(bout);
		BufferedOutputStream bos = new BufferedOutputStream(zip);
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(datei));
		byte[] cup=new byte[64000];
		int leng=-1;
		while((leng=bis.read(cup))!=-1){
				bos.write(cup,0,leng);
		}
		bos.flush();
		zip.finish();
		bos.close();
		Object tmp_data[]=new Object[2];
		tmp_data[0]=datei;
		tmp_data[1]=bout.toByteArray();
		data=tmp_data;
		setEmpfänger(nid);
		group=datei.getName();
	}
	*/
	public MSG(FileTransferData tmp_FR) throws IOException {
		this(NachrichtenTyp.DATA);
		this.empfänger=tmp_FR.getReceiver_nid();
		
		if(! tmp_FR.datei.isFile())throw new IOException("Verzeichnisse werden nicht unterstützt.");
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		GZIPOutputStream zip = new GZIPOutputStream(bout);
		BufferedOutputStream bos = new BufferedOutputStream(zip);
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(tmp_FR.datei));
		byte[] cup=new byte[64000];
		int leng=-1;
		while((leng=bis.read(cup))!=-1){
				bos.write(cup,0,leng);
		}
		bos.flush();
		zip.finish();
		bos.close();
		Object tmp_data[]=new Object[2];
		tmp_data[0]=tmp_FR;
		tmp_data[1]=bout.toByteArray();
		data=tmp_data;
		setEmpfänger(tmp_FR.getReceiver_nid());
	}

	public void save(File datei) throws IOException{
		if(typ==NachrichtenTyp.DATA&&data!=null&&data instanceof Object[]&&((Object[])data).length==2&&((Object[])data)[0]instanceof File&&((Object[])data)[1]instanceof byte[]){
			byte[] tmp_data=(byte[]) ((Object[])data)[1];
			System.out.println(tmp_data.length);

			ByteArrayInputStream bais = new ByteArrayInputStream(tmp_data);
			GZIPInputStream zip = new GZIPInputStream(bais);
			BufferedInputStream bis = new BufferedInputStream(zip);
			
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(datei));
			byte[] cup=new byte[64000];
			int leng=-1;
			while((leng=bis.read(cup))!=-1){
				bos.write(cup,0,leng);
			}
			bos.flush();
			bos.close();
		}
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + (int) (sender ^ (sender >>> 32));
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
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

	public long getEmpfänger() {
		return empfänger;
	}

	public long getSender() {
		return sender;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
	public void reStamp() {
		timestamp=System.currentTimeMillis();
	}

	public NachrichtenTyp getTyp() {
		return typ;
	}

	public MSGCode getCode() {
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
	public void setEmpfänger(long value) {
		empfänger=value;
	}


	
	
	@Override
	public String toString() {
		return "MSG [" + (typ != null ? "typ=" + typ + ", " : "")
				+ (code != null ? "code=" + code + ", " : "") + "sender="
				+ sender + ", timestamp=" + timestamp + ", id=" + id
				+ ", empfänger=" + empfänger + ", "
				+ (group != null ? "group=" + group + ", " : "")
				+ (data != null ? "data=" + data : "") + "]";
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
			LogEngine.log("MSG.getMSG",e);
		}
		return null;
	}

	@Override
	public int compareTo(MSG o) {
		if (this.getTimestamp() != o.getTimestamp())	return (this.getTimestamp() > o.getTimestamp()) ? 1 : -1;
			else if (this.getSender() != o.getSender())	return (this.getSender() > o.getSender()) ? 1 : -1;
			else if (this.getId() != o.getId())			return (this.getId() - o.getId());
			return 0;
		
	}

	public void setGroup(String string) {
		group=string;
	}
	
	
}
