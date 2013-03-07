import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/**
 * 
 */

/**
 * @author ABerthold
 *
 */
public class UserList extends JWindow {
	private int hoehe;
	private int breite;
	private JFrame parent;
	
	
	public UserList(JFrame parent) {
		this.parent=parent;
		this.hoehe = parent.getHeight();
		this.breite = 150;
	}
	
	public void aufklappen(){
		setBounds(parent.getX()-breite, parent.getY(), breite, hoehe);
		this.setVisible(true);
	}
	
	public void zuklappen(){
		// Falls wir das animiert haben wollen:
//		for (int i = 150; i > 0; i--){
//			try {
//				Thread.sleep(3);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			setBounds((int) (getBounds().getX()+1),parent.getY(),i,parent.getHeight());
//			repaint((int) (getBounds().getX()+1),parent.getY(),i,parent.getHeight());
//		}
		
		this.setVisible(false);
	}
	
}
