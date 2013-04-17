package org.publicmain.common;

public enum MSGCode {
	NODE_UPDATE,
	ALIAS_UPDATE,
	
	NODE_LOOKUP,
	
	ECHO_REQUEST,
	ECHO_RESPONSE,
	
	PATH_PING_REQUEST,
	PATH_PING_RESPONSE,
	
	
	ROOT_DISCOVERY,
	ROOT_REPLY,
	ROOT_ANNOUNCE,
	
	POLL_CHILDNODES,
	REPORT_CHILDNODES,
	
	POLL_ALLNODES,
	REPORT_ALLNODES,
	
	TREE_DATA_POLL,
	TREE_DATA,
	
	NODE_SHUTDOWN,
	CHILD_SHUTDOWN,
	
	GROUP_POLL,
	GROUP_REPLY,
	
	GROUP_JOIN,
	GROUP_LEAVE,
	GROUP_EMPTY,
	GROUP_ANNOUNCE,
	
	FILE_REQUEST,
	FILE_REPLY,
	FILE_RECIEVED,
	FILE_TCP_REQUEST,
	FILE_TCP_REPLY,
	FILE_TCP_ABORT,
	
	CMD_SHUTDOWN,
	CMD_RESTART,
	CMD_RECONNECT,

	CW_INFO_TEXT,
	CW_WARNING_TEXT,
	CW_ERROR_TEXT,
	CW_FILE_REQUEST,
	
	TRAY_INFO_TEXT,
	TRAY_WARNING_TEXT,
	TRAY_ERROR_TEXT,
	
	GUI_INFORM;
	
}
