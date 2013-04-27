package org.publicmain.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.Properties;


public class Config {
	private static final String APPNAME 			=     (System.getProperty("appname")==null)?"publicMAIN":System.getProperty("appname");
	private static final int CURRENTVERSION		=	5;
	private static final int MINVERSION				=	5;
	private static final String APPDATA=System.getenv("APPDATA")+File.separator+"publicMAIN"+File.separator;
	private static final String JARLOCATION=Config.class.getProtectionDomain().getCodeSource().getLocation().getFile();
	private static final String	lock_file_name		= 	APPNAME+".loc";
	private static final String	system_conf_name	= 	APPNAME+"_sys.cfg";
	private static final String	user_conf_name		= 	APPNAME+".cfg"; 
	private static File loc_file						=	new File(APPDATA,lock_file_name);
	private static File system_conf					=	new File(new File(JARLOCATION).getParentFile(),system_conf_name);
	private static File user_conf					=	new File(APPDATA,user_conf_name);
	private static Config me;
	
	private ConfigData settings;

	public static synchronized ConfigData getConfig() {
		if (me==null) {
			me = new Config();
		}
		return me.settings;
	}
	
	public static synchronized boolean write() {
		if (me==null) {
			me = new Config();
		}
		me.getConfig().setCurrentVersion(CURRENTVERSION);
		return me.savetoDisk();
	}
	
	public static synchronized boolean writeSystemConfiguration() {
		try(FileOutputStream fos = new FileOutputStream(system_conf)){
			getSourceSettings().store(fos,"publicMAIN - SYSTEM - SETTINGS");
			LogEngine.log(Config.class, "System configurations file written to " + system_conf, LogEngine.INFO);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			LogEngine.log(Config.class, "Could not write system settings: "+system_conf +" reason : "+e.getMessage(), LogEngine.WARNING);
			return false;
		}
	}

	
	
	/**Method tries to Lock a file <code>pm.loc</code> in Users <code>APPDATA\publicMAIN</code> folder. And returns result as boolen.
	 * It also adds a shutdown hook to the VM to remove Lock from File if Program exits.
	 * @return <code>true</code> if File could be locked <code>false</code> if File has already been locked
	 */
	public static boolean getLock() {
        try {
            if(!loc_file.getParentFile().exists())loc_file.getParentFile().mkdirs();
            final RandomAccessFile randomAccessFile = new RandomAccessFile(loc_file, "rw");
            final FileLock fileLock = randomAccessFile.getChannel().tryLock();
            if (fileLock != null) {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        try {
                            fileLock.release();
                            randomAccessFile.close();
                            loc_file.delete();
                        } catch (Exception e) {
                            LogEngine.log("ShutdownHook","Unable to remove lock file: " + loc_file, LogEngine.ERROR);
                        }
                    }
                });
                return true;
            }
        } catch (Exception e) {
        	LogEngine.log("Config","Unable to create and/or lock file: " + loc_file, LogEngine.ERROR);
        }
        return false;
    }

	private Config() {
		me=this;

		System.out.println(loc_file.getParent() + " : " +loc_file.exists()+ ":" + loc_file);
		System.out.println(system_conf.getParent() + " : " +system_conf.exists()+ ":" + system_conf);
		System.out.println(user_conf.getParent() + " : " +user_conf.exists()+ ":" + user_conf);
		
		settings = new ConfigData(getSourceSettings());
		LogEngine.log(this, "default settings loaded from source",LogEngine.INFO);
		
		//try to overload system-settings from Jars Location
		if (system_conf.canRead()) {
			try (FileInputStream in = new FileInputStream(system_conf)) {
				ConfigData system = new ConfigData(settings);
				system.load(in);
				LogEngine.log(this, "system settings loaded from "+system_conf,LogEngine.INFO);
				this.settings = system;
			} catch (IOException e1) {
				e1.printStackTrace();
				LogEngine.log(this, "error while loading system settings from "+system_conf,LogEngine.ERROR);
			}
		}
		
		ConfigData user = new ConfigData(settings);
		//try to overload user-settings from AppData folder
		if (user_conf.canRead()) {
			try (FileInputStream in = new FileInputStream(user_conf)) {
				user.load(in);
				if(user.getCurrentVersion()<MINVERSION) {
					settings.setUserID(user.getUserID());
					settings.setAlias(user.getAlias());
					LogEngine.log(this, "user settings outdated only userid and alias will be used from " + user_conf, LogEngine.INFO);
				}
				else settings=user;
				LogEngine.log(this, "user settings loaded from " + user_conf, LogEngine.INFO);
			} catch (IOException e) {
				LogEngine.log(this, "default config could not be read. reason:" + e.getMessage(), LogEngine.WARNING);
			}
		}
		
		
		
		
	}

	
	
	private static ConfigData getSourceSettings() {
		ConfigData tmp = new ConfigData();
		
		tmp.setCurrentVersion(CURRENTVERSION);

		//Network parameters
		tmp.setMCGroup("230.223.223.223");
		tmp.setMCPort(6789);
		tmp.setMCTTL(10);
		tmp.setDiscoverTimeout(200);
		tmp.setRootClaimTimeout(200);
		tmp.setMaxConnections(5);
		tmp.setTreeBuildTime(1000);
		tmp.setPingInterval(30000);
		tmp.setPingEnabled(false);
		
		//FileTransferParameters
		tmp.setMaxFileSize(5000000);
		tmp.setFileTransferTimeout(120000);
		tmp.setFileTransferInfoInterval(30000);
		tmp.setDisableFileTransfer(false);
		
		//usability settings
		tmp.setLogVerbosity(4);
		tmp.setMaxAliasLength(19);
		tmp.setMaxGroupLength(19);
		tmp.setNamePattern("((([-_]?)([a-zA-Z0-9öäüÖÄÜßéá♥])+))+([-_])?");
		tmp.setNotifyGroup(false);
		tmp.setNotifyPrivate(false);
		
		//local mySQL Database settings
		tmp.setLocalDBVersion(0);
		tmp.setLocalDBDatabasename("db_publicMain");
		tmp.setLocalDBPort("3306");
		tmp.setLocalDBUser("root");
		tmp.setLocalDBPw("");
		
		// daten für externen DB-Backup-Server
		tmp.setBackupDBDatabasename("db_publicMain");
		tmp.setBackupDBPort("3306");
		tmp.setBackupDBUser("root");
		tmp.setBackupDBPw("");
//		
//		
		return tmp;
	}


	

	private boolean savetoDisk(){
		try(FileOutputStream fos = new FileOutputStream(user_conf)) 
		{
			settings.store(fos,"publicMAIN - USER - SETTINGS");
			LogEngine.log(this, "User settings written to " + user_conf, LogEngine.WARNING);
			return true;
		} catch (IOException e1) {
			LogEngine.log(this, "Could not write user settings: "+user_conf +" reason : "+e1.getMessage(), LogEngine.WARNING);
		}
		return false;
	}



}

