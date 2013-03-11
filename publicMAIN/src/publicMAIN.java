import javax.swing.SwingUtilities;

public class publicMAIN {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				org.publicmain.gui.pMTrayIcon.createAndShowGUI();
//			}
//		});

		org.publicmain.gui.pMTrayIcon.createAndShowGUI();

		org.publicmain.gui.GUI.getGUI();

	}

}
