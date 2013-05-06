package org.publicmain.gui;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JFrame;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.Config;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.NachrichtenTyp;
import org.resources.Help;

/**
 * Diese Klasse stellt das TrayIcon zur Verfügung.
 * 
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

	/**
	 * Der Konstruktor für PMTrayIcon
	 */
	public PMTrayIcon() {
		// Prüfung ob Systemtray unterstützt:
		if (!SystemTray.isSupported()) {
			LogEngine.log(this, "SystemTray is not supported", LogEngine.ERROR);
			return;
		}

		// Initialisierungen:
		this.popup = new PopupMenu();
		this.trayIcon = new TrayIcon(Help.getIcon("pM_Logo.png", 16).getImage());
		this.sysTray = SystemTray.getSystemTray();
		this.display = new MenuItem("Display");
		this.notifies = new Menu("Notify");
		this.notifyPrivMsg = new CheckboxMenuItem("Private Messages", Config
				.getConfig().getNotifyPrivate());
		this.notifyGroupMsg = new CheckboxMenuItem("Group Messages", Config
				.getConfig().getNotifyGroup());
		this.exit = new MenuItem("Exit");

		// Hinzufügen der komponenten
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
			LogEngine.log(this, "TrayIcon konnte nicht hinzugefügt werden.",
					LogEngine.ERROR);
			return;
		}

		// Listener hinzufügen
		// ActionListener für das TrayIcon selbst.
		trayIcon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (GUI.getGUI().getExtendedState() == JFrame.ICONIFIED) {
					GUI.getGUI().setExtendedState(JFrame.NORMAL);
				}
				GUI.getGUI().setVisible(true);
			}
		});

		// ActionListener für Menüeintrag display.
		display.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (GUI.getGUI().getExtendedState() == JFrame.ICONIFIED) {
					GUI.getGUI().setExtendedState(JFrame.NORMAL);
				}
				GUI.getGUI().setVisible(true);
			}
		});

		// Listener für die CheckboxMenuItems Private Messages / Group Messages.
		ItemListener listener = new ItemListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent
			 * )
			 */
			@Override
			public void itemStateChanged(ItemEvent e) {
				CheckboxMenuItem item = (CheckboxMenuItem) e.getSource();
				switch (item.getLabel()) {
				// wenn selektiert wird bei minimierten GUI ein Popup über neue
				// private Nachrichten angezeigt
				case "Private Messages":
					Config.getConfig().setNotifyPrivate(item.getState());
					Config.write();
					break;
				// wenn selektiert wird bei minimierten GUI ein Popup über neue
				// gruppen Nachrichten angezeigt
				case "Group Messages":
					Config.getConfig().setNotifyGroup(item.getState());
					Config.write();
					break;
				default:
					trayIcon.displayMessage("default", "default",
							TrayIcon.MessageType.NONE);
					break;
				}
			}
		};

		// Listener den CheckboxMenuItems hinzufügen
		notifyPrivMsg.addItemListener(listener);
		notifyGroupMsg.addItemListener(listener);

		// ActionListener für Menüeintrag exit
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeTray();
				GUI.getGUI().shutdown();
			}
		});
	}// eom PMTrayIcon()

	/**
	 * Diese Methode entfernt das TrayIcon.
	 */
	void removeTray() {
		sysTray.remove(trayIcon);
	}// eom removeTray()

	/**
	 * Diese Methode erlaubt das empfangen von Nachrichten im TrayIcon.
	 * 
	 * Diese Methode übernimmt eine MSG (msg) und zeigt diese ggf. als Popup an.
	 * 
	 * @param msg
	 *            MSG übergebene Nachricht.
	 */
	void recieveMSG(MSG msg) {
		// Das nervigste Geräusch das wir gefunden haben.
		Help.playSound("icq.au");
		String msgSender;

		if (msg.getTyp() == NachrichtenTyp.PRIVATE) {
			// Sender der Nachricht aus dem MSG paket holen
			msgSender = ChatEngine.getCE().getNodeForNID(msg.getSender())
					.getAlias();
			// Wenn getNotifyPrivate true (pirvate Nachrichten sollen angezeigt
			// werden) wird Popup ausgegeben.
			if (Config.getConfig().getNotifyPrivate()) {
				trayIcon.displayMessage(msgSender, (String) msg.getData(),
						TrayIcon.MessageType.NONE);
			}
		} else {
			// SenderGruppe aus dem MSG paket holen
			msgSender = msg.getGroup();
			// Wenn getNotifyGroup true (gruppen Nachrichten sollen angezeigt
			// werden) wird Popup ausgegeben.
			if (Config.getConfig().getNotifyGroup()) {
				trayIcon.displayMessage(msgSender, ChatEngine.getCE()
						.getNodeForNID(msg.getSender()).getAlias()
						+ ": " + (String) msg.getData(),
						TrayIcon.MessageType.NONE);
			}
		}

	}// eom recieveMSG()

	/**
	 * Diese Methode erlaubt das empfangen von Text im TrayIcon.
	 * 
	 * Diese Methode übernimmt einen String (text) und einen MSGCode (code) und
	 * zeigt je nach MSGCode unterschiedliche Arten von Popups im TrayIcon.
	 * 
	 * @param text
	 *            String auszugebende Text Nachricht
	 * @param code
	 *            MSGCode CW_WARNING_TEXT für Warnmeldung, CW_ERROR_TEXT für
	 *            Fehlermeldungen, CW_INFO_TEXT für Informartionen.
	 */
	void recieveText(String text, MSGCode code) {
		Help.playSound("notify.wav");
		if (code == MSGCode.CW_INFO_TEXT) {
			trayIcon.displayMessage("Incoming Info", text,
					TrayIcon.MessageType.INFO);
		} else if (code == MSGCode.CW_WARNING_TEXT) {
			trayIcon.displayMessage("Incoming Info", text,
					TrayIcon.MessageType.WARNING);
		} else if (code == MSGCode.CW_ERROR_TEXT) {
			trayIcon.displayMessage("Incoming Info", text,
					TrayIcon.MessageType.ERROR);
		}
	}// eom recieve Text()
}// eoc PMTrayIcon