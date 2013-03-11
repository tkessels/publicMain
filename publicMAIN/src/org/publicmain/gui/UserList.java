package org.publicmain.gui;

import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

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
	private JInternalFrame internalFrame;
	
	
	public UserList(JFrame parent) {
//		this.setLayout(new BorderLayout());
		this.parent=parent;
		this.internalFrame = new JInternalFrame("Userlist");
		this.hoehe = parent.getHeight();
		this.breite = 150;
		//TODO: prüfen ob man nicht besser ständig das selbe logo verwendet.
		internalFrame.setFrameIcon(new ImageIcon("res/pM16x16.png"));
		
		this.add(internalFrame);
		
		internalFrame.setVisible(true);
		
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
