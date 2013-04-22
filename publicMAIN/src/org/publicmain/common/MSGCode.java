package org.publicmain.common;

public enum MSGCode {
	NODE_UPDATE("beschreibungEinf�gen"),
	ALIAS_UPDATE("beschreibungEinf�gen"),
	
	NODE_LOOKUP("beschreibungEinf�gen"),
	
	ECHO_REQUEST("beschreibungEinf�gen"),
	ECHO_RESPONSE("beschreibungEinf�gen"),
	
	PATH_PING_REQUEST("beschreibungEinf�gen"),
	PATH_PING_RESPONSE("beschreibungEinf�gen"),
	
	
	ROOT_DISCOVERY("beschreibungEinf�gen"),
	ROOT_REPLY("beschreibungEinf�gen"),
	ROOT_ANNOUNCE("beschreibungEinf�gen"),
	
	POLL_CHILDNODES("beschreibungEinf�gen"),
	REPORT_CHILDNODES("beschreibungEinf�gen"),
	
	POLL_ALLNODES("beschreibungEinf�gen"),
	REPORT_ALLNODES("beschreibungEinf�gen"),
	
	TREE_DATA_POLL("beschreibungEinf�gen"),
	TREE_DATA("beschreibungEinf�gen"),
	
	NODE_SHUTDOWN("beschreibungEinf�gen"),
	CHILD_SHUTDOWN("beschreibungEinf�gen"),
	
	GROUP_POLL("beschreibungEinf�gen"),
	GROUP_REPLY("beschreibungEinf�gen"),
	
	GROUP_JOIN("beschreibungEinf�gen"),
	GROUP_LEAVE("beschreibungEinf�gen"),
	GROUP_EMPTY("beschreibungEinf�gen"),
	GROUP_ANNOUNCE("beschreibungEinf�gen"),
	
	FILE_REQUEST("beschreibungEinf�gen"),
	FILE_REPLY("beschreibungEinf�gen"),
	FILE_RECIEVED("beschreibungEinf�gen"),
	FILE_TCP_REQUEST("beschreibungEinf�gen"),
	FILE_TCP_REPLY("beschreibungEinf�gen"),
	FILE_TCP_ABORT("beschreibungEinf�gen"),
	
	CMD_SHUTDOWN("beschreibungEinf�gen"),
	CMD_RESTART("beschreibungEinf�gen"),
	CMD_RECONNECT("beschreibungEinf�gen"),

	CW_INFO_TEXT("beschreibungEinf�gen"),
	CW_WARNING_TEXT("beschreibungEinf�gen"),
	CW_ERROR_TEXT("beschreibungEinf�gen"),
	
	GUI_INFORM("beschreibungEinf�gen");
	
private String description;
	
	private MSGCode(String description) {
        this.description = description;
    }
	public String getDescription() {
        return description;
    }
	
}