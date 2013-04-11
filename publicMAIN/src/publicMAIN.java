import java.io.IOException;

import javax.swing.JOptionPane;

import org.publicmain.common.Config;
import org.publicmain.common.LogEngine;
import org.publicmain.gui.startWindow;

/**
 * @author ATRM
 * 
 */

public class publicMAIN {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if(Config.getConfig().getLock()){
			
			//Mir gefälltnicht das hier die Controlle weggegeben wird. Es sollte ein strukturierte und Kontrollierter Start formuliert werden der die Module in der richtigen Reihenfolge startet
			startWindow.getStartWindow();
		}
		else{
			JOptionPane.showMessageDialog(null, "publicMAIN konnte nicht gestartet werden weil bereits eine Instanz der Software läuft");
			LogEngine.log("publicMAIN konnte nicht gestartet werden weil bereits eine Instanz der Software läuft", LogEngine.ERROR);
		}
	}
}
