package org.publicmain.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


public class Config {
	private static Config me; 
	private Properties settings;
	
	
	public static synchronized Config getConfig() {
		if (me==null) {
			me = new Config();
		}
		return me;
	}

	//private int state;
	private Config() {
		me=this;
		Properties sourceSettings = new Properties();
		
		sourceSettings.put("ne.multicast_group_ip", "230.223.223.223");
		sourceSettings.put("ne.multicast_group_port", "6789");
		sourceSettings.put("ne.multicast_ttl",	"10");
		sourceSettings.put("ne.discover_timeout", "200");
		sourceSettings.put("ne.root_claim_timeout", "200");
		sourceSettings.put("ne.max_clients","5");
		
		sourceSettings.put("ch.ping_intervall","30000");
		sourceSettings.put("ch.ping_enabled", "false");
		
		sourceSettings.put("ce.ping_enabled", "false");
		sourceSettings.put("log.verbosity", "4");
		
		sourceSettings.put("gui.max_group_length","12");
		sourceSettings.put("gui.group_pattern","[a-z0-9\\-_]{2,}");
		
		sourceSettings.put("sql.local_db_port","3306");
		sourceSettings.put("sql.local_db_user","root");
		sourceSettings.put("sql.local_db_password","");
		
		
		
//		this.settings = new Properties(sourceSettings);
		this.settings = sourceSettings;
		
		
		
		try(FileInputStream in = new FileInputStream("config.cfg")){
			settings.load(in);
		} catch (FileNotFoundException e) {
			LogEngine.log(this, "default config not found: generating",LogEngine.WARNING);
			write();
		} catch (IOException e) {
			LogEngine.log(this, "default config could not be read. reason:"+e.getMessage(),LogEngine.WARNING);
		}
		
		
		
		
	}
	
	public void write(){
		try {
			settings.store(new FileOutputStream("config.cfg"), "publicMAIN Config");
		} catch (IOException e1) {
			LogEngine.log(this, "Could not generate config.cfg. reason:"+e1.getMessage(), LogEngine.WARNING);
		}
	}

	public long getPingInterval() {
		return Long.parseLong(settings.getProperty("ch.ping_intervall"));
	}

	public long getDiscoverTimeout() {
		return Long.parseLong(settings.getProperty("ne.discover_timeout"));
	}

	public long getRootClaimTimeout() {
		return Long.parseLong(settings.getProperty("ne.root_claim_timeout"));
	}

	public String getMCGroup() {
		return settings.getProperty("ne.multicast_group_ip");
	}

	public int getMCPort() {
		return Integer.parseInt(settings.getProperty("ne.multicast_group_port"));
	}

	public int getMaxConnections() {
		return Integer.parseInt(settings.getProperty("ne.max_clients"));
	}

	public String getLocalDBUser() {
		return settings.getProperty("sql.local_db_user");
	}

	public String getLocalDBPw() {
		return settings.getProperty("sql.local_db_password");
	}

	public String getLocalDBPort() {
		return settings.getProperty("sql.local_db_port");
	}

	public boolean getPingEnabled() {
		return Boolean.parseBoolean("ce.ping_enabled");
	}

	public int getMaxGroupLength() {
		String property = settings.getProperty("gui.max_group_length");
		System.out.println(property);
		return Integer.parseInt(property);
	}
	

}

