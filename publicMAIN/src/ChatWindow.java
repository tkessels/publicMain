import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

//import com.nilo.plaf.nimrod.NimRODLookAndFeel;

/**
 * @author ABerthold
 *
 */
public class ChatWindow extends JPanel implements ActionListener, Observer{

	// Deklarationen:
	private String name;
	private JButton sendenBtn;
	private JTextArea msgTextArea;
	private JScrollPane jScrollPane;
	private JTextField eingabeFeld;
	private String cwTyp;
	private String gruppe;
	private long user;
	
	
	private GUI gui;
	
	/**
	 * Erstellt Content und macht Layout für das Chatpanel
	 */
	private void doWindowbuildingstuff(){
		//Layout für ChatWindow (JPanel) festlegen auf BorderLayout:
		this.setLayout(new BorderLayout());
	
		// Initialisierungen:
		this.sendenBtn = new JButton("send");
		this.msgTextArea = new JTextArea(10,30);
		this.jScrollPane = new JScrollPane(msgTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.eingabeFeld = new JTextField();
		
		
		msgTextArea.setEditable(true); // später ändern!!!
		msgTextArea.setLineWrap(true);
		
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
		this.cwTyp="privat";
		this.gui = GUI.getGUI();
		doWindowbuildingstuff();
	}
	
	public ChatWindow(String gruppenname){
		gruppe=gruppenname;
		this.name=gruppenname;
		this.cwTyp="gruppe";
		this.gui = GUI.getGUI();
		doWindowbuildingstuff();
	}
	
	/**
	 * @return String für Tab..
	 */
	public String getTabText(){
		return name;
	}
	
	public String getCwTyp(){
		return this.cwTyp;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(!eingabeFeld.getText().equals("")){

		//Prüfen ob die Eingabe mit "/" beginnt und diese dann in drei Teile zerlegt ins Array speichern.
		if(eingabeFeld.getText().startsWith("/")){ 
			
			// für UserID vom Empfänger zwischen zu speichern! falls Alias nicht gefunden wird, wird Nachricht an einen selbst geschickt
			long tmpUid = user; 
			
			String[] tmp;
			tmp = eingabeFeld.getText().split(" ", 3);
						
			for(int i = 0; i < gui.ce.getUsers().length; i++){
				if(tmp[1].equals(gui.ce.getUsers()[i].getAlias())){
					tmpUid = gui.ce.getUsers()[i].getUserID();
				} else {
					//TODO: Hier muss noch Fehlermeldung in der msgTextArea erzeugt werden!! am besten bund
					System.err.println("Alias (User) nicht gefunden! [ChatWindow:actionPerformed:eingabeFeld]");
				}
			}
			
			switch(tmp[0]){
			
			case "/w":
				//TODO: Hier muss noch ein ChatWindow ins GUI, oder wenn schon vorhanden das focusiert werden 
				gui.ce.send_private(tmpUid, tmp[2]);
				
				break;
			case "/g":
				//TODO: Hier muss noch der gruppenname eingefügt werden;
//				gui.ce.send_group(group, tmp[2]);
				System.out.println("Senden an Gruppen noch nicht möglich [ChatWindow:actionPerformed:eingabeFeld]");
				break;
			default :
				//TODO:  Hier muss noch Fehlermeldung in der msgTextArea erzeugt werden!! am besten BUND 
				System.err.println("kein gültiger Befehl!");
				break;
				
			}
			
		} else { //ansonsten senden
			if(gruppe==null) {
				gui.ce.send_private(user, eingabeFeld.getText()); //ggf.: eingabeFeld.getText() durch Methode filtern
			} else {
				gui.ce.send_group("public", eingabeFeld.getText()); //ggf.: eingabeFeld.getText() durch Methode filtern
			}
		}
		
		echo();
		}
		
	}

	private void echo(){
		if(msgTextArea.getText().equals("")){
			msgTextArea.setText(eingabeFeld.getText());
			eingabeFeld.setText("");
		} else {
			msgTextArea.setText(msgTextArea.getText() + "\n" + eingabeFeld.getText());
			eingabeFeld.setText("");
		}
	}
	
	@Override
	public void update(Observable sourceChannel, Object msg) {
		String ausgabe="";
		gui.getNode(((MSG)msg).getSender());
		
	}
	
}
