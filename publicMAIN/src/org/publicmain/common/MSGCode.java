package org.publicmain.common;

public enum MSGCode {
	NODE_UPDATE("beschreibungEinfügen"),
	ALIAS_UPDATE("beschreibungEinfügen"),
	
	NODE_LOOKUP("beschreibungEinfügen"),
	
	ECHO_REQUEST("beschreibungEinfügen"),
	ECHO_RESPONSE("beschreibungEinfügen"),
	
	PATH_PING_REQUEST("beschreibungEinfügen"),
	PATH_PING_RESPONSE("beschreibungEinfügen"),
	
	
	ROOT_DISCOVERY("beschreibungEinfügen"),
	ROOT_REPLY("beschreibungEinfügen"),
	ROOT_ANNOUNCE("beschreibungEinfügen"),
	
	POLL_CHILDNODES("beschreibungEinfügen"),
	REPORT_CHILDNODES("beschreibungEinfügen"),
	
	POLL_ALLNODES("beschreibungEinfügen"),
	REPORT_ALLNODES("beschreibungEinfügen"),
	
	TREE_DATA_POLL("beschreibungEinfügen"),
	TREE_DATA("beschreibungEinfügen"),
	
	NODE_SHUTDOWN("beschreibungEinfügen"),
	CHILD_SHUTDOWN("beschreibungEinfügen"),
	
	GROUP_POLL("beschreibungEinfügen"),
	GROUP_REPLY("beschreibungEinfügen"),
	
	GROUP_JOIN("beschreibungEinfügen"),
	GROUP_LEAVE("beschreibungEinfügen"),
	GROUP_EMPTY("beschreibungEinfügen"),
	GROUP_ANNOUNCE("beschreibungEinfügen"),
	
	FILE_REQUEST("beschreibungEinfügen"),
	FILE_REPLY("beschreibungEinfügen"),
	FILE_RECIEVED("beschreibungEinfügen"),
	FILE_TCP_REQUEST("beschreibungEinfügen"),
	FILE_TCP_REPLY("beschreibungEinfügen"),
	FILE_TCP_ABORT("beschreibungEinfügen"),
	
	CMD_SHUTDOWN("beschreibungEinfügen"),
	CMD_RESTART("beschreibungEinfügen"),
	CMD_RECONNECT("beschreibungEinfügen"),

	CW_INFO_TEXT("beschreibungEinfügen"),
	CW_WARNING_TEXT("beschreibungEinfügen"),
	CW_ERROR_TEXT("beschreibungEinfügen"),
	
	GUI_INFORM("beschreibungEinfügen");
	
private String description;
	
	private MSGCode(String description) {
        this.description = description;
    }
	public String getDescription() {
        return description;
    }
	
}