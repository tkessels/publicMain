import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout.Constraints;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.nilo.plaf.nimrod.NimRODLookAndFeel;

public class ChatWindow extends JPanel implements ActionListener, Observer{

	private String name;
	private JButton sendenBtn;
	private JTextArea jTextArea;
	private JScrollPane jScrollPane;
	private JTextField eingabeFeld;
//	private String cwTyp;
	private String gruppe;
	private long user;
	
	
	private GUI gui;
	
	/**
	 * Erstellt Content und macht Layout für das Chatpanel
	 */
	private void doWindowbuildingstuff(){
		//Layout für ChatWindow (JPanel) festlegen auf BorderLayout:
				this.setLayout(new BorderLayout());
				
				//LookAndFeel auf NimRODLookAndFeel setzen:
				try {
					// Set System L&F
					UIManager.setLookAndFeel(new NimRODLookAndFeel());
				} 
				catch (UnsupportedLookAndFeelException e) {
					System.out.println(e.getMessage());
				}
			
				this.sendenBtn = new JButton("send");
				this.jTextArea = new JTextArea(10,30);
				this.jScrollPane = new JScrollPane(jTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				this.eingabeFeld = new JTextField();
				
				jTextArea.setEditable(true); // später ändern!!!
				jTextArea.setLineWrap(true);
				
				sendenBtn.addActionListener(this);
				eingabeFeld.addActionListener(this);
				
				
				this.add(jScrollPane, BorderLayout.CENTER);
				JPanel panel = new JPanel(new BorderLayout());
				panel.add(eingabeFeld, BorderLayout.CENTER);
				panel.add(sendenBtn, BorderLayout.EAST);
				this.add(panel, BorderLayout.SOUTH);
				
				this.setVisible(true);
	}
	

	
	public ChatWindow(long uid, String username){
		this.user=uid;
		this.name=username;
		this.gui = GUI.getGUI();
		doWindowbuildingstuff();
	}
	
	public ChatWindow(String gruppenname){
		gruppe=gruppenname;
		this.name=gruppenname;
		this.gui = GUI.getGUI();
		doWindowbuildingstuff();
		
		
	}
	
	/**
	 * @return String für Tab..
	 */
	public String getTabText(){
		return name;
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(!eingabeFeld.getText().equals("")){

		if(eingabeFeld.getText().startsWith("/")){ //commando
			//TODO:Commandos erkennen und ausführen
			
		} else { //ansonsten senden
			if(gruppe==null) {
				gui.ce.send_private(user, eingabeFeld.getText()); //ggf.: eingabeFeld.getText() durch Methode filtern
			} else {
				gui.ce.send_group(gruppe, eingabeFeld.getText()); //ggf.: eingabeFeld.getText() durch Methode filtern
			}
		}
		
		echo();
		}
		
	}

	
	
	private void echo(){
		if(jTextArea.getText().equals("")){
			jTextArea.setText(eingabeFeld.getText());
			eingabeFeld.setText("");
		} else {
			jTextArea.setText(jTextArea.getText() + "\n" + eingabeFeld.getText());
			eingabeFeld.setText("");
		}
	}
	
	
	
	
	@Override
	public void update(Observable sourceChannel, Object msg) {
		String ausgabe="";
		gui.getNode(((MSG)msg).getSender());
		
	}
	
}