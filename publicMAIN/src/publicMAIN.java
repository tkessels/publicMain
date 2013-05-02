import java.io.IOException;

import javax.swing.JOptionPane;

import org.publicmain.common.Config;
import org.publicmain.common.LogEngine;
import org.publicmain.gui.GUI;
import org.publicmain.gui.StartWindow;
import org.publicmain.nodeengine.MulticastConnectionHandler;
import org.publicmain.sql.DatabaseEngine;
import org.resources.Help;

/**
 * @author ATRM
 * 
 */

public class publicMAIN {

	/**
	 * Die Programmstart-Klasse für publicMAIN, hier wird geprüft ob bereits
	 * eine Instanz des Programms läuft. Sollte dies zutreffen wird der Start
	 * einer weiteren Instanz abgebrochen.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (Config.getLock()) {
			DatabaseEngine.getDatabaseEngine();
			if (Config.getConfig().getUserID() == null) {
				MulticastConnectionHandler.getMC().discoverBUS();
				// TODO: hier darauf warten bis nutzer daten eingegeben und
				// "submit" gedrückt hat. also zum Beispiel ein boolean im
				// Startwindwo überprüfen
				// TODO: Will Werte bei Submit-Klick im StartWindow in der
				// Config speichern...wie? ;-)
				boolean tom = StartWindow.getStartWindow();
				if (tom) {
					// Help.playSound("logon.wav");
					GUI.getGUI();
				}
			} else {
				// if autopull = true dann pull
				// Help.playSound("logon.wav");
				GUI.getGUI();
			}
			// Mir gefällt nicht das hier die Kontrolle weggegeben wird. Es
			// sollte ein strukturierte und kontrollierter Start formuliert
			// werden der die Module in der richtigen Reihenfolge startet.
		} else {
			JOptionPane.showMessageDialog(
							null,
							"Could not start publicMAIN\nAn instance is already running!",
							"publicMAIN", JOptionPane.ERROR_MESSAGE,
							Help.getIcon("pM_Logo.png", 48));
			LogEngine
					.log("Could not start publicMAIN An instance is already running!",
							LogEngine.ERROR);
		}
	}
}
