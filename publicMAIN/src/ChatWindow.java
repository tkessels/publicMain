import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout.Constraints;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

//import com.nilo.plaf.nimrod.NimRODLookAndFeel;

public class ChatWindow extends JPanel implements ActionListener{

	private String name;
	private JButton sBtn;
	private JTextArea jTa;
	private JScrollPane jSp;
	private JTextField jTf;
	private String cwTyp;
	
	public ChatWindow(String name){
		
		//Layout für ChatWindow (JPanel) festlegen auf BorderLayout:
		this.setLayout(new BorderLayout());
		
//		//LookAndFeel auf NimRODLookAndFeel setzen:
//		try {
//			// Set System L&F
//			UIManager.setLookAndFeel(new NimRODLookAndFeel());
//		} 
//		catch (UnsupportedLookAndFeelException e) {
//			System.out.println(e.getMessage());
//		}
	
		this.name = name;
		this.sBtn = new JButton("send");
		this.jTa = new JTextArea(10,30);
		this.jSp = new JScrollPane(jTa, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.jTf = new JTextField();
		
		jTa.setEditable(true); // später ändern!!!
		jTa.setLineWrap(true);
		
		sBtn.addActionListener(this);
		
		this.add(jSp, BorderLayout.CENTER);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(jTf, BorderLayout.CENTER);
		panel.add(sBtn, BorderLayout.EAST);
		this.add(panel, BorderLayout.SOUTH);
		
		this.setVisible(true);
	}
	
	public String getName(){
		return this.name;
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(jTa.getText().equals("")){
			jTa.setText(jTf.getText());
			jTf.setText("");
		} else {
			jTa.setText(jTa.getText() + "\n" + jTf.getText());
			jTf.setText("");
		}
	
		
	}
	
}