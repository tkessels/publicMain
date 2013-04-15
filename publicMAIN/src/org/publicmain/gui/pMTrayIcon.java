package org.publicmain.gui;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;

import org.images.Help;

import org.publicmain.common.LogEngine;

/**
 * @author ATRM
 * 
 */

public class pMTrayIcon {
    
	private LogEngine log;
	private TrayIcon trayIcon;
	private SystemTray sysTray;
	private PopupMenu popup;
	private MenuItem pMainOpenItem;
	private MenuItem exitItem;
	private Menu alerts;
	private CheckboxMenuItem alertPrivMsg;
	private CheckboxMenuItem alertGroupMsg;
	private CheckboxMenuItem alertPublicMsg;
	
	
	/**
	 * TODO: Kommentar
	 */
    public pMTrayIcon() {
    	this.log = new LogEngine();
        // Prüfung ob Systemtray unterstützt:
        if (!SystemTray.isSupported()) {
            log.log(this, "SystemTray is not supported", LogEngine.ERROR);
            return;
        }
        
        this.popup = new PopupMenu();
        this.trayIcon = new TrayIcon(new ImageIcon(getClass().getResource("TrayIcon.png")).getImage());
        this.sysTray = SystemTray.getSystemTray();
        this.pMainOpenItem = new MenuItem("pMain öffnen");
        this.alerts = new Menu("Alert me");
        this.alertPrivMsg = new CheckboxMenuItem("private Messages");
        this.alertGroupMsg = new CheckboxMenuItem("group Messages");
        this.alertPublicMsg = new CheckboxMenuItem("public Massages");
        this.exitItem = new MenuItem("Exit");
        popup.add(pMainOpenItem);
        popup.addSeparator();
        popup.add(alerts);
        alerts.add(alertPrivMsg);
        alerts.add(alertGroupMsg);
        alerts.add(alertPublicMsg);
        popup.addSeparator();
        popup.add(exitItem);
        trayIcon.setPopupMenu(popup);
        try {
            sysTray.add(trayIcon);
        } catch (AWTException e) {
        	log.log(this, "TrayIcon konnte nicht hinzugefügt werden.", LogEngine.ERROR);
            return;
        }

        /**
    	 * TODO: Kommentar
    	 */
        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if(GUI.getGUI().getExtendedState() == JFrame.ICONIFIED){
        			GUI.getGUI().setExtendedState(JFrame.NORMAL);
        		}
            	GUI.getGUI().setVisible(true);
            }
        });
        
    	/**
    	 * TODO: Kommentar
    	 */
        pMainOpenItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
        		if(GUI.getGUI().getExtendedState() == JFrame.ICONIFIED){
        			GUI.getGUI().setExtendedState(JFrame.NORMAL);
        		}
            	GUI.getGUI().setVisible(true);
            }
        });
        
    	/**
    	 * TODO: Kommentar
    	 */
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MenuItem item = (MenuItem)e.getSource();
                switch (item.getLabel()){
	                case "private Messages" :
	                	trayIcon.displayMessage("Sun TrayIcon Demo", "Message von Tobi", TrayIcon.MessageType.ERROR);
	                	break;
	                case "group Messages" :
	                	trayIcon.displayMessage("Sun TrayIcon Demo", "Message von Gruppe", TrayIcon.MessageType.WARNING);
	                	break;
	                case "public Messages" :
	                	 trayIcon.displayMessage("Sun TrayIcon Demo", "Message von Public", TrayIcon.MessageType.INFO);
	                	break;
                	default :
                		trayIcon.displayMessage("Sun TrayIcon Demo", "Martin", TrayIcon.MessageType.NONE);
                		break;
                }
            }
        };
        
        alertPrivMsg.addActionListener(listener);
        alertGroupMsg.addActionListener(listener);
        alertPublicMsg.addActionListener(listener);
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sysTray.remove(trayIcon);
                System.exit(0);
            }
        });
    }
    
	/**
	 * TODO: Kommentar
	 */
    protected static Image createImage(String path, String description) {
        URL imageURL = pMTrayIcon.class.getResource(path);
        
        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
}
