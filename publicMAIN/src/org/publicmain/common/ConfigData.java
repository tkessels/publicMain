package org.publicmain.common;
import java.util.Properties;

public class ConfigData extends Properties {

	private static final long serialVersionUID = 7968268488626148729L;

	public ConfigData() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ConfigData(Properties defaults) {
		super(defaults);
		// TODO Auto-generated constructor stub
	}
	
	
	public int getCurrentVersion() {
		return Integer.parseInt(this.getProperty("System.Version"));
	}
	
	public void setCurrentVersion(int version) {
		this.setProperty("System.Version", String.valueOf(version));
	}
	
	
	//User Settings:
		//Getter:
	public String getAlias() {
		return this.getProperty("System.alias");
	}
	
	public Long getUserID() {
		String uid=this.getProperty("System.UserID");
		return (uid!=null)?Long.parseLong(uid):null;
	}

		//Setter:
	public void setAlias(String alias){
		this.setProperty("System.alias", alias);
	}
	
	public void setUserID(long userID) {
		this.setProperty("System.UserID", String.valueOf(userID) );
	}
	
	
	//Network parameters:
		//Getter:
	public String getMCGroup() {
		return this.getProperty("ne.multicast_group_ip");
	}
	
	public int getMCPort() {
		return Integer.parseInt(this.getProperty("ne.multicast_group_port"));
	}
	
	public int getMCTTL() {
		return Integer.parseInt(this.getProperty("ne.multicast_ttl"));
	}
	
	public long getDiscoverTimeout() {
		return Long.parseLong(this.getProperty("ne.discover_timeout"));
	}
	
	public long getRootClaimTimeout() {
		return Long.parseLong(this.getProperty("ne.root_claim_timeout"));
	}
	
	public int getMaxConnections() {
		return Integer.parseInt(this.getProperty("ne.max_clients"));
	}
	
	public long getTreeBuildTime() {
		return Long.parseLong(this.getProperty("ne.tree_build_time"));
	}
	
	public long getPingInterval() {
		return Long.parseLong(this.getProperty("ch.ping_intervall"));
	}
	
	public boolean getPingEnabled() {
		return Boolean.parseBoolean("ce.ping_enabled");
	}
			
		//Setter:
	public void setMCGroup(String multicast_ip) {
		this.setProperty("ne.multicast_group_ip", multicast_ip);		
	}

	public void setMCPort(int port) {
		this.setProperty("ne.multicast_group_port", String.valueOf(port));
	}

	public void setMCTTL(int ttl) {
		this.setProperty("ne.multicast_ttl",	String.valueOf(ttl));		
	}
	
	public void setDiscoverTimeout(long timeout) {
		this.setProperty("ne.discover_timeout", String.valueOf(timeout));
	}

	public void setRootClaimTimeout(long timeout) {
		this.setProperty("ne.root_claim_timeout", String.valueOf(timeout));
	}

	public void setMaxConnections(int connection_count) {
		this.setProperty("ne.max_clients",String.valueOf(connection_count));
	}

	public void setTreeBuildTime(long timout) {
		this.setProperty("ne.tree_build_time",String.valueOf(timout));
	}
	
	public void setPingInterval(long intervall) {
		this.setProperty("ch.ping_intervall",String.valueOf(intervall));
	}
	
	public void setPingEnabled(boolean enabled) {
		this.setProperty("ce.ping_enabled", String.valueOf(enabled));
	}
	
	
	//FileTransferParameters:
		//Getter:
	public int getMaxFileSize() {
		return Integer.parseInt(this.getProperty("ne.max_file_size")) ;
	}

	public long getFileTransferTimeout() {
		return Long.parseLong(this.getProperty("ne.file_transfer_timeout"));
	}
	
	public long getFileTransferInfoInterval() {
		return Long.parseLong(this.getProperty("ne.file_transfer_info_interval"));
	}
		
	public boolean getDisableFileTransfer(){
		return Boolean.parseBoolean(this.getProperty("ne.disable_file_transfer"));
	}

		//Setter:
	public void setMaxFileSize(long size) {
		this.setProperty("ne.max_file_size",String.valueOf(size));
	}

	public void setFileTransferTimeout(long timeout) {
		this.setProperty("ne.file_transfer_timeout",String.valueOf(timeout));
	}

	public void setFileTransferInfoInterval(long interval) {
		this.setProperty("ne.file_transfer_info_interval",String.valueOf(interval));		
	}
	
	public void setDisableFileTransfer(boolean disable){
		this.setProperty("ne.disable_file_transfer", String.valueOf(disable));
	}
	
	
	//usability settings:
		//Getter:
	public int getLogVerbosity() {
		return Integer.parseInt(this.getProperty("log.verbosity"));
	}
	
	public int getMaxAliasLength() {
		return Integer.parseInt(this.getProperty("gui.max_alias_length"));
	}
	
	public int getMaxGroupLength() {
		return Integer.parseInt(this.getProperty("gui.max_group_length"));
	}
	
	public String getNamePattern() {
		return this.getProperty("gui.name_pattern");
	}
	
	public boolean getNotifyGroup(){
		return Boolean.parseBoolean(this.getProperty("gui.tray_notify_group"));
	}
	
	public boolean getNotifyPrivate(){
		return Boolean.parseBoolean(this.getProperty("gui.tray_notify_private"));
	}
	
		//Setter:
	public void setLogVerbosity(int verbosity) {
		 this.setProperty("log.verbosity", String.valueOf(verbosity));
	}

	public void setMaxAliasLength(int length) {
		this.setProperty("gui.max_alias_length",String.valueOf(length));		
	}

	public void setMaxGroupLength(int length) {
		this.setProperty("gui.max_group_length",String.valueOf(length));
	}

	public void setNamePattern(String pattern) {
		this.setProperty("gui.name_pattern", pattern);
	}
	
	public void setNotifyGroup(boolean group_notify){
		this.setProperty("gui.tray_notify_group", String.valueOf(group_notify));
	}
	
	public void setNotifyPrivate(boolean private_notify){
		this.setProperty("gui.tray_notify_private", String.valueOf(private_notify));
	}

	
	//local mySQL Database settings:
		//Getter:
	public int getLocalDBVersion(){
		return Integer.parseInt(this.getProperty("sql.local_db_version"));
	}
	
	public String getLocalDBDatabasename() {
		return this.getProperty("sql.local_db_databasename");
	}
	
	public String getLocalDBPort() {
		return this.getProperty("sql.local_db_port");
	}

	public String getLocalDBUser() {
		return this.getProperty("sql.local_db_user");
	}
	
	public String getLocalDBPw() {
		return this.getProperty("sql.local_db_password");
	}
	
		//Setter:
	public void setLocalDBVersion(int version){
		this.setProperty("sql.local_db_version", String.valueOf(version) );
	}
	
	public void setLocalDBDatabasename(String database_name) {
		this.setProperty("sql.local_db_databasename",database_name);
	}

	public void setLocalDBPort(String port) {
		this.setProperty("sql.local_db_port",port);		
	}

	public void setLocalDBUser(String user_name) {
		this.setProperty("sql.local_db_user",user_name);		
	}

	public void setLocalDBPw(String user_password) {
		this.setProperty("sql.local_db_password",user_password);		
	}
		
	
	//daten für externen DB-Backup-Server:
		//Getter:
	public String getBackupDBDatabasename(){
		return this.getProperty("sql.backup_db_databasename");
	}
	
	public String getBackupDBPort() {
		return this.getProperty("sql.backup_db_port");
	}

	public String getBackupDBUser(){
		return this.getProperty("backup_db_user");
	}

	public String getBackupDBPw(){
		return this.getProperty("backup_db_password");
	}
	
	public String getBackupDBIP(){
		return this.getProperty("sql.backup_db_ip");
	}
	
	public String getBackupDBChoosenUsername(){
		return this.getProperty("sql.backup_db_choosen_username");
	}
	
	public String getBackupDBChoosenUserPassWord(){
		return this.getProperty("sql.backup_db_choosen_user_password");
	}
	
		//Setter:
	public void setBackupDBDatabasename(String database_name) {
		this.setProperty("sql.backup_db_databasename",database_name);	//TODO: inhalt anpassen
	}

	public void setBackupDBPort(String port) {
		this.setProperty("sql.backup_db_port",port);		
	}

	public void setBackupDBUser(String user_name) {
		this.setProperty("sql.backup_db_user",user_name);
	}

	public void setBackupDBPw(String user_password) {
		this.setProperty("sql.backup_db_password",user_password);		
	}
	
	public void setBackupDBIP(String ip){
		this.setProperty("sql.backup_db_ip", ip);
	}
	
	public void setBackupDBChoosenUsername(String usrName){
		this.setProperty("sql.backup_db_choosen_username", usrName);
	}
	
	public void setBackupDBChoosenUserPassWord(String userPassWord){
		this.setProperty("sql.backup_db_choosen_user_password", userPassWord);
	}
}
