import java.net.InetAddress;

public class Node {

	private long nodeID;
	private long userID;
	private String alias;
	private InetAddress[] sockets;
	private String hostname;
	private boolean isRoot;

	public long getNodeID() {
		return nodeID;
	}

	public void setNodeID(long nodeID) {
		this.nodeID = nodeID;
	}

	public long getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public InetAddress[] getSockets() {
		return sockets;
	}

	public void setSockets(InetAddress[] sockets) {
		this.sockets = sockets;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public boolean isRoot() {
		return isRoot;
	}

	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

}
