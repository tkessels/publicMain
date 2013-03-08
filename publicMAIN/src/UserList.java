import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import org.omg.CORBA.Bounds;

/**
 * 
 */

/**
 * @author ABerthold
 *
 */
public class UserList extends JWindow implements ComponentListener, WindowListener {
	private int hoehe;
	private int breite;
	private JFrame parent;
	
	
	public UserList(JFrame parent) {
//		this.setLayout(new BorderLayout());
		this.parent=parent;
		this.hoehe = parent.getHeight();
		this.breite = 150;
		parent.addComponentListener(this);
		parent.addWindowListener(this);
		
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

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		Rectangle tmp = e.getComponent().getBounds();
		this.setBounds((int)(tmp.getX()-getBounds().width),(int)tmp.getY(), getBounds().width, getBounds().height);
	}

	@Override
	public void componentResized(ComponentEvent e) {
		Rectangle tmp = e.getComponent().getBounds();
		this.setBounds((int)(tmp.getX()-getBounds().width),(int)tmp.getY(), getBounds().width, tmp.height);
		this.validate();
		this.repaint();
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		this.setVisible(true);
	}

	@Override
	public void windowIconified(WindowEvent e) {
		this.setVisible(false);
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
