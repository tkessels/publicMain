import java.io.IOException;

import javax.swing.JOptionPane;

import org.publicmain.common.Config;
import org.publicmain.common.LogEngine;
import org.publicmain.gui.GUI;
import org.publicmain.gui.StartWindow;
import org.publicmain.sql.LocalDBConnection;

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
				//TODO: hier darauf warten bis nutzer daten eingegeben und "submit" gedr�ckt hat. also zum Beispiel ein boolean im Startwindwo �berpr�fen
				//TODO: Will werte bei Submit-Click im Startwindow in config speichern...wie? ;-)
				boolean tom = StartWindow.getStartWindow();
				if (tom) {
					LocalDBConnection.getDBConnection();
					GUI.getGUI();
				}
			}else {
				LocalDBConnection.getDBConnection(); //TODO: Warum l�uft das prog nicht direkt weiter wenn der DB-Server aus ist? Selbst wenn dieser Aufruf hier in nem extra Threat ist geht�s nicht.
				GUI.getGUI();
			}
			//Mir gef�lltnicht das hier die Controlle weggegeben wird. Es sollte ein strukturierte und Kontrollierter Start formuliert werden der die Module in der richtigen Reihenfolge startet
			
		}
		else{
			JOptionPane.showMessageDialog(null, "publicMAIN konnte nicht gestartet werden weil bereits eine Instanz der Software l�uft");
			LogEngine.log("publicMAIN konnte nicht gestartet werden weil bereits eine Instanz der Software l�uft", LogEngine.ERROR);
		}
		
	}
}
