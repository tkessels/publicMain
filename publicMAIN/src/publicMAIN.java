import java.io.IOException;

import javax.swing.JOptionPane;

import org.publicmain.common.Config;
import org.publicmain.common.LogEngine;
import org.publicmain.gui.GUI;
import org.publicmain.gui.StartWindow;
import org.publicmain.sql.LocalDBConnection;
import org.resources.Help;

/**
 * @author ATRM
 * 
 */

public class publicMAIN {

	/**
	 * TODO: Kommentar
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if(Config.getLock()){
			if(Config.getConfig().getUserID() == null){
				//TODO: hier darauf warten bis nutzer daten eingegeben und "submit" gedrückt hat. also zum Beispiel ein boolean im Startwindwo überprüfen
				//TODO: Will werte bei Submit-Click im Startwindow in config speichern...wie? ;-)
				boolean tom = StartWindow.getStartWindow();
				if (tom) {
					LocalDBConnection.getDBConnection();
					GUI.getGUI();
				}
			}else {
				LocalDBConnection.getDBConnection(); //TODO: Warum läuft das prog nicht direkt weiter wenn der DB-Server aus ist? Selbst wenn dieser Aufruf hier in nem extra Threat ist geht´s nicht.
				GUI.getGUI();
			}
			//Mir gefälltnicht das hier die Controlle weggegeben wird. Es sollte ein strukturierte und Kontrollierter Start formuliert werden der die Module in der richtigen Reihenfolge startet
			
		}
		else{
			JOptionPane.showMessageDialog(null, "Could not start publicMAIN\nAn instance is already running!", "publicMAIN", JOptionPane.ERROR_MESSAGE, Help.getIcon("pM_Logo2.png",48) );
			LogEngine.log("Could not start publicMAIN An instance is already running!", LogEngine.ERROR);
		}
		
	}
}
