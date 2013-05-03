package org.publicmain.common;

/**
 * Dieses Enum enthält alle derzeit möglichen Nachrichten-Codes welche an andere
 * Nodes übermittelt werden und dort ein Ereignis auslösen.
 * 
 * @author ATRM
 * 
 */

public enum MSGCode {
	
	NODE_UPDATE("inform on node changes"),
	ALIAS_UPDATE("inform on alias changes"),
	NODE_LOOKUP("beschreibungEinfügen"), // TODO
	ECHO_REQUEST("ping request"),
	ECHO_RESPONSE("ping response"),
	PATH_PING_REQUEST("pathping request"),
	PATH_PING_RESPONSE("pathping response"),
	ROOT_DISCOVERY("request for root"),
	ROOT_REPLY("answer from root"),
	ROOT_ANNOUNCE("root announce for this node"),
	POLL_CHILDNODES("ask childnodes"),
	REPORT_CHILDNODES("inform childnodes"),
	POLL_ALLNODES("ask allnodes"),
	REPORT_ALLNODES("inform allnodes"),
	TREE_DATA_POLL("report topologie"),
	TREE_DATA("beschreibungEinfügen"),// TODO
	NODE_SHUTDOWN("inform about own nodesshutdown"),
	CHILD_SHUTDOWN("inform about childshutdown"),

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
	
	CMD_SHUTDOWN("beschreibungEinfügen"),
	CMD_RESTART("beschreibungEinfügen"),
	CMD_RECONNECT("beschreibungEinfügen"),

	CW_INFO_TEXT("beschreibungEinfügen"),
	CW_WARNING_TEXT("beschreibungEinfügen"),
	CW_ERROR_TEXT("beschreibungEinfügen"),
	
	BACKUP_SERVER_DISCOVER("find active backupservers on the net"),
	BACKUP_SERVER_OFFER("offer backupserver services on the net"),
	
	GUI_INFORM("notify the gui about an event");

	private String description;

	/**
	 * Liefert einen MSGCode für eine Description.
	 * 
	 * @param description
	 */
	private MSGCode(String description) {
		this.description = description;
	}
	
	/**
	 * Liefert eine Description.
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}
}