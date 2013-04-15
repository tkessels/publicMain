package org.publicmain.gui;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;

import org.images.Help;

import org.publicmain.common.LogEngine;

/**
 * Diese Methode stellt das Icon in der SystemTray mit Kontextmenü bereit, hier
 * können Benachrichtigungen und andere Einstellungen getätigt werden.
 * 
 * @author ATRM
 * 
 */

public class pMTrayIcon {
    
	private LogEngine log;
	private TrayIcon trayIcon;
	private SystemTray sysTray;
	private PopupMenu popup;
	private MenuItem display;
	private MenuItem exit;
	private Menu notifies;
	private CheckboxMenuItem notifyPrivMsg;
	private CheckboxMenuItem notifyGroupMsg;
	private CheckboxMenuItem notifyPublicMsg;
	private CheckboxMenuItem sync;
	
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
        this.display = new MenuItem("Display");
        this.notifies = new Menu("Notify");
        this.notifyPrivMsg = new CheckboxMenuItem("Private Messages");
        this.notifyGroupMsg = new CheckboxMenuItem("Group Messages");
        this.notifyPublicMsg = new CheckboxMenuItem("Public Massages");
        this.sync = new CheckboxMenuItem("Synchronize");
        
        this.exit = new MenuItem("Exit");

        popup.add(display);
        popup.addSeparator();
        popup.add(notifies);
        notifies.add(notifyPrivMsg);
        notifies.add(notifyGroupMsg);
        notifies.add(notifyPublicMsg);
        popup.add(sync);
        popup.addSeparator();
        popup.add(exit);
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
        display.addActionListener(new ActionListener() {
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
        
        notifyPrivMsg.addActionListener(listener);
        notifyGroupMsg.addActionListener(listener);
        notifyPublicMsg.addActionListener(listener);
        exit.addActionListener(new ActionListener() {
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
