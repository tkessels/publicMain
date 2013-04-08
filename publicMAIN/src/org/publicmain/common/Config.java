package org.publicmain.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


public class Config {
	private Properties settings;
	//private int state;
	public Config() {
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
		
		
		Properties settings = new Properties(sourceSettings);
		
		
		
		try(FileInputStream in = new FileInputStream("config.cfg")){
			settings.load(in);
		} catch (FileNotFoundException e) {
			
			LogEngine.log(this, "default config not found: generating",LogEngine.WARNING);
			write();
		} catch (IOException e) {
			LogEngine.log(this, "default config could not be read. reason:"+e.getMessage(),LogEngine.WARNING);
		}
		
		
		LogEngine.setVerbosity(Integer.getInteger(settings.getProperty("log.verbosity")));
		
		
	}
	
	public void write(){
		try {
			settings.store(new FileOutputStream("config.cfg"), "publicMAIN Config");
		} catch (IOException e1) {
			LogEngine.log(this, "Could not generate config.cfg. reason:"+e1.getMessage(), LogEngine.WARNING);
		}
	}
	public static void main(String[] args) {
		Config test = new Config();
		
	}

}
