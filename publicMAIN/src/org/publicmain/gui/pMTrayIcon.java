package org.publicmain.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.*;

public class pMTrayIcon {
//    public static void main(String[] args) {
//        /* Use an appropriate Look and Feel */
//        try {
//            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
//            //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
//        } catch (UnsupportedLookAndFeelException ex) {
//            ex.printStackTrace();
//        } catch (IllegalAccessException ex) {
//            ex.printStackTrace();
//        } catch (InstantiationException ex) {
//            ex.printStackTrace();
//        } catch (ClassNotFoundException ex) {
//            ex.printStackTrace();
//        }
//        /* Turn off metal's use of bold fonts */
//        UIManager.put("swing.boldMetal", Boolean.FALSE);
//        //Schedule a job for the event-dispatching thread:
//        //adding TrayIcon.
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                createAndShowGUI();
//            }
//        });
//    }
    
    public static void createTrayIcon() {
        // Prüfung ob Systemtray unterstützt:
        if (!SystemTray.isSupported()) {
            System.err.println("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon = new TrayIcon(new ImageIcon("media/pM.gif").getImage());
        final SystemTray tray = SystemTray.getSystemTray();
        
        // Create a popup menu components
        MenuItem aboutItem = new MenuItem("pMain öffnen");
       
        Menu alerts = new Menu("Alert me");
        CheckboxMenuItem alertPrivMsg = new CheckboxMenuItem("private Messages");
        CheckboxMenuItem alertGroupMsg = new CheckboxMenuItem("group Messages");
        CheckboxMenuItem alertPublicMsg = new CheckboxMenuItem("public Massages");
        MenuItem exitItem = new MenuItem("Exit");
        
        //Add components to popup menu
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(alerts);
        alerts.add(alertPrivMsg);
        alerts.add(alertGroupMsg);
        alerts.add(alertPublicMsg);
        popup.addSeparator();
        popup.add(exitItem);
        
        trayIcon.setPopupMenu(popup);
        
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon konnte nicht hinzugefügt werden.");
            return;
        }
        
        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        "This dialog box is run from System Tray");
            }
        });
        
        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
//                JOptionPane.showMessageDialog(null,
//                        "This dialog box is run from the About menu item");
        		GUI.getGUI().setVisible(true);
//        		BufferedImage image;
//				try {
//					image = ImageIO.read(fenster.getClass().getResource("media/pM.gif"));
//					fenster.setIconImage(image);
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
                
            	
            }
        });
        
        
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
                tray.remove(trayIcon);
                System.exit(0);
            }
        });
    }
    
    //Obtain the image URL
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
