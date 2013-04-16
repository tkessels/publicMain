package org.publicmain.common;

import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;


public class FileTransferData implements Serializable{
	private static final long	serialVersionUID	= 1221L;
	public final File	datei;
	public final long	size;
	
	public final Node sender;
	public final Node receiver;
	
	
	public boolean accepted;
	public int server_port =-2;

	public FileTransferData(File datei, long size, Node sender,Node receiver)  {
		this.datei = datei;
		this.size = size;
		this.sender = sender;
		this.receiver= receiver;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((datei == null) ? 0 : datei.hashCode());
		result = prime * result + (int) (getReceiver_nid() ^ (getReceiver_nid() >>> 32));
		result = prime * result + (int) (getSender_nid() ^ (getSender_nid() >>> 32));
		result = prime * result + (int) (size ^ (size >>> 32));
		return result;
	}

	public long getSender_nid() {
		return sender.getNodeID();
	}

	public long getReceiver_nid() {
		return receiver.getNodeID();
	}
	
	
	public String getNiceSize() {
		if(size <= 0) return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

}