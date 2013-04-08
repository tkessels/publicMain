package org.publicmain.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


public class Config {
	private Properties settings;
	private int state;
	public Config() {
		Properties sourceSettings = new Properties();
		
		sourceSettings.put("ne.multicast_group_ip", "230.223.223.223");
		sourceSettings.put("ne.multicast_group_port", 6789);
		sourceSettings.put("ne.multicast_ttl",	10);
		sourceSettings.put("ne.discover_timeout", 200l);
		sourceSettings.put("ne.root_claim_timeout", 200l);
		sourceSettings.put("ne.max_clients",5);
		
		sourceSettings.put("ch.ping_intervall",30000l);
		sourceSettings.put("ch.ping_enabled", false);
		
		sourceSettings.put("ce.ping_enabled", false);
		
		
		
		
		
		
		
		
		
		
		
		
		Properties defaultSettings = new Properties(sourceSettings);
		
		
		
		
		
		
		try(FileInputStream in = new FileInputStream("config.ini")){
			defaultSettings.load(in);
		} catch (FileNotFoundException e) {
			
			LogEngine.log(this, "default config not found: generating",LogEngine.WARNING);
		} catch (IOException e) {
			LogEngine.log(this, "default config could not be read. reason:"+e.getMessage(),LogEngine.WARNING);
		}
		
		
		
		
		
		settings=new Properties(defaultSettings);
	}

}
