import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JFrame;
import javax.swing.JWindow;

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
//		this.setLayout(new BorderLayout());
		this.parent=parent;
		this.hoehe = parent.getHeight();
		this.breite = 150;
		parent.addComponentListener(new ComponentListener() {
			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
			}
			@Override
			public void componentMoved(ComponentEvent e) {
				Rectangle tmp = e.getComponent().getBounds();
				setBounds((int)(tmp.getX()-getBounds().width),(int)tmp.getY(), getBounds().width, getBounds().height);
			}
			@Override
			public void componentResized(ComponentEvent e) {
				Rectangle tmp = e.getComponent().getBounds();
				setBounds((int)(tmp.getX()-getBounds().width),(int)tmp.getY(), getBounds().width, tmp.height);
				validate();
				repaint();
			}
			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub
			}
		});
		
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
