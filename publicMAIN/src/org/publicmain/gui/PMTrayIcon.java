package org.publicmain.gui;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;


import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.Config;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.NachrichtenTyp;
import org.resources.Help;

/**
 * Diese Klasse stellt das Icon in der SystemTray mit Kontextmenü bereit, hier
 * können Benachrichtigungen und andere Einstellungen getätigt werden.
 * 
 * @author ATRM
 * 
 */

public class PMTrayIcon {
    
	private TrayIcon trayIcon;
	private SystemTray sysTray;
	private PopupMenu popup;
	private MenuItem display;
	private MenuItem exit;
	private Menu notifies;
	private CheckboxMenuItem notifyPrivMsg;
	private CheckboxMenuItem notifyGroupMsg;
	
	
    public PMTrayIcon() {
        // Prüfung ob Systemtray unterstützt:
        if (!SystemTray.isSupported()) {
            LogEngine.log(this, "SystemTray is not supported", LogEngine.ERROR);
            return;
        }
        
        this.popup = new PopupMenu();
        this.trayIcon = new TrayIcon(Help.getIcon("pM_Logo.png",16).getImage());
        this.sysTray = SystemTray.getSystemTray();
        this.display = new MenuItem("Display");
        this.notifies = new Menu("Notify");
        this.notifyPrivMsg = new CheckboxMenuItem("Private Messages",Config.getConfig().getNotifyPrivate());
        this.notifyGroupMsg = new CheckboxMenuItem("Group Messages",Config.getConfig().getNotifyGroup());
        
        this.exit = new MenuItem("Exit");

        popup.add(display);
        popup.addSeparator();
        popup.add(notifies);
        notifies.add(notifyPrivMsg);
        notifies.add(notifyGroupMsg);
        popup.addSeparator();
        popup.add(exit);
        trayIcon.setPopupMenu(popup);
        try {
            sysTray.add(trayIcon);
        } catch (AWTException e) {
        	LogEngine.log(this, "TrayIcon konnte nicht hinzugefügt werden.", LogEngine.ERROR);
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
        ItemListener listener = new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				 CheckboxMenuItem item = (CheckboxMenuItem)e.getSource();
	                switch (item.getLabel()){
		                case "Private Messages" :
		                	Config.getConfig().setNotifyPrivate(item.getState());
		                	Config.write();
		                	
//		                	notifyPriv = item.getState();
		                	break;
		                case "Group Messages" :
		                	Config.getConfig().setNotifyGroup(item.getState());
		                	Config.write();
//		                	notifyGrp = item.getState();
		                	break;
	                	default :
	                		trayIcon.displayMessage("default", "default", TrayIcon.MessageType.NONE);
	                		break;
	                }
			}
		};
        
        notifyPrivMsg.addItemListener(listener);
        notifyGroupMsg.addItemListener(listener);
        
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sysTray.remove(trayIcon);
                GUI.getGUI().shutdown();
            }
        });
    }

    protected void removeTray(){
    	sysTray.remove(trayIcon);
    }
    
	protected void recieveMSG(MSG msg) {
		String msgSender;
		
		if(msg.getTyp() == NachrichtenTyp.PRIVATE){
			msgSender = ChatEngine.getCE().getNodeForNID(msg.getSender()).getAlias();
			if(Config.getConfig().getNotifyPrivate()){
				trayIcon.displayMessage(msgSender, (String)msg.getData(), TrayIcon.MessageType.NONE);
			}
		} else {
			msgSender = msg.getGroup();
			if (Config.getConfig().getNotifyGroup()) {
				trayIcon.displayMessage( msgSender,
						ChatEngine.getCE().getNodeForNID(msg.getSender()).getAlias() + ": " + (String) msg.getData(),
						TrayIcon.MessageType.NONE );
			}
		}
		
	}

	protected void recieveText(String text, MSGCode code) {
		if(code == MSGCode.CW_INFO_TEXT){
			trayIcon.displayMessage("Incoming Info", text, TrayIcon.MessageType.INFO);
		} else if(code == MSGCode.CW_WARNING_TEXT){
			trayIcon.displayMessage("Incoming Info", text, TrayIcon.MessageType.WARNING);
		} else if(code == MSGCode.CW_ERROR_TEXT){
			trayIcon.displayMessage("Incoming Info", text, TrayIcon.MessageType.ERROR);
		}
	}
}
