package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JWindow;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.Node;
import org.resources.Help;

/**
 * Diese Klasse stellt eine Kontaktliste zur Verf�gung.
 * 
 * Diese Klasse stellt die Kontaktliste zur Verf�gung welche links am Hauptfenster
 * angedockt ist. Sie beinhaltet eine Liste mit allen Gruppen und eine Liste
 * mit allen erreichbaren Usern.
 * 
 * @author ATRM
 * 
 */
public class ContactList extends JWindow {

	private int breite;

	private JFrame parent;

	private JInternalFrame internalFrame;

	private JScrollPane		groupsScroller;
	private JPanel			groupPanel;
	private JList<String> 	groups;
	private JButton 		createGrp;

	private JSplitPane 		trenner;

	private JScrollPane 	usersScroller;
	private JPanel 			userPanel;
	private JList<Node>		users;

	public ContactList( JFrame parent ) {
		// Initialisierungen:
		
		this.parent			= parent;
		this.internalFrame	= new JInternalFrame( "contacts" );
		this.breite			= 150;
		
		// Userliste (Liste von Nodes) mit daszugeh�rigem Model
		this.users			= new JList<Node>( new UserListModel() );
		
		// Gruppenliste (Liste von Strings) mit daszugeh�rigem Model
		this.groups			= new JList<String>( new GroupListModel() );
		
		// Die Listen sollten vertical scrollbar sein falls n�tig
		this.usersScroller	= new JScrollPane( users, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		this.groupsScroller = new JScrollPane( groups, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		
		// Panel f�r Gruppenliste und create Button um zus�tzliche Gruppe zu erstellen
		this.groupPanel		= new JPanel( new BorderLayout() );
		this.createGrp		= new JButton( "Create Group" );
		
		// Panel f�r Userliste
		this.userPanel		= new JPanel( new BorderLayout() );
		
		// trenner zwischen den beiden Listen
		this.trenner		= new JSplitPane( JSplitPane.VERTICAL_SPLIT );

		// Layout InternalFrame
		this.internalFrame.setLayout( new BorderLayout() );
		this.internalFrame.setFrameIcon( Help.getIcon( "g18050.png" ) );

		// Layout Gruppenpanel
		this.groupPanel.setPreferredSize( new Dimension( breite, parent.getHeight()/4 ) );
		this.groupPanel.setBorder( BorderFactory.createTitledBorder( "GROUPS" ) );
		this.groupPanel.setBackground( Color.WHITE );

		// Layout Userpanel
		this.userPanel.setBorder( BorderFactory.createTitledBorder( "USERS" ) );
		this.userPanel.setBackground( Color.WHITE );

		// Konfiguration Userliste
		this.users.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
		this.users.setLayoutOrientation( JList.VERTICAL );
		this.users.setVisibleRowCount( -1 );
		this.users.setCellRenderer( new UsersListCellRenderer() );

		// Konfiguration Gruppenliste
		this.groups.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
		this.groups.setLayoutOrientation( JList.VERTICAL );
		this.groups.setVisibleRowCount( -1 );
		this.groups.setCellRenderer( new GroupsListCellRenderer() );

		// Listener hinzuf�gen
		this.parent.addComponentListener( new MyComponentListener() );
		this.users.addMouseListener( new MyMouseListener() );
		this.groups.addMouseListener( new MyMouseListener() );
		this.createGrp.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				String tmpGrpName = null;
				tmpGrpName = ( String )JOptionPane.showInputDialog( GUI.getGUI(), "Enter Groupname", "Groupname", JOptionPane.OK_CANCEL_OPTION, Help.getIcon( "gruppe.png" ), null, null );
				if ( ( tmpGrpName != null ) && GUI.getGUI().checkName( tmpGrpName, 0 ) ){
					tmpGrpName = GUI.getGUI().confName( tmpGrpName, true );
					GUI.getGUI().addGrpCW( tmpGrpName, true );
				} else {
					GUI.getGUI().info( "Illegal charakter in groupname!<br>Allowed charakters: a-z,A-Z,0-9,�,�,�,�,�,�,�,�,�,-,_", null, 2 );
				}
			}
		} );

		// Hinzuf�gen der Komponenten
		// GruppenPanel 
		this.groupPanel.add( groupsScroller, BorderLayout.CENTER );
		this.groupPanel.add( createGrp, BorderLayout.SOUTH );

		// UserPanel
		this.userPanel.add( usersScroller, BorderLayout.CENTER );

		// Der Trenner muss die beiden zu trennenden Elemente enthalten
		this.trenner.add( groupPanel );
		this.trenner.add( userPanel );

		// und wird dem internalFrame hinzugef�gt
		this.internalFrame.add( trenner, BorderLayout.CENTER );

		this.add( internalFrame );

		this.internalFrame.setVisible( true );
	}//eom ContactList(JFrame parent)

	/**
	 * Diese Methode pr�ft ob eine Gruppe bereits existier.
	 * 
	 * Diese Methode �berpr�ft die Gruppenliste ob eine Gruppe mit dem �bergebenen String (name)
	 * bereits in der Gruppenliste enthalten ist und gibt dementsprechend true zur�ck.
	 * Ist eine Gruppe mit dem �bergebenen String nicht enthalten wird false zur�ckgeliefert.
	 * 
	 * @param name String name der Gruppe
	 * @return boolean true wenn existent, false falls nicht
	 */
	boolean groupExists( String name ){
		return ( ( GroupListModel )groups.getModel() ).contains( name );
	}//eom groupExists()

	/**
	 * Diese Methode pr�ft ob ein User mit dem selben Alias in der Userliste existiert.
	 * 
	 * Diese Methode �berpr�ft die Userliste ob ein User mit dem �bergebenen String (alias)
	 * bereits in der Userliste enthalten ist und gibt dementsprechend true zur�ck.
	 * Ist ein User mit dem �bergebenen String nicht enthalten wird false zur�ckgeliefert.
	 * 
	 * @param alias String name des Users
	 * @return boolean true wenn existent, false falls nicht
	 */
	boolean aliasExists( String alias ) {
		return ( ( UserListModel )users.getModel() ).contains( alias );
	}//eom aliasExists()

	/**
	 * Diese Methode sorgt daf�r das die Kontaktliste neu gezeichnet wird.
	 * 
	 * Diese Methode l�sst die Kontaktliste neu zeichen und wird verwendet wenn
	 * das Fenster verschoben wird oder die Gr��e ver�ndert wird.
	 */
	@Override
	public void repaint() {
		Rectangle tmp = parent.getBounds();
		setBounds( ( int )( tmp.getX()-breite ),( int )tmp.getY(), breite, tmp.height );
		super.repaint();
	}//eom repaint()

	/**
	 * Diese Elementklasse stellt einen ComponentAdapter bereit.
	 * 
	 * Diese Elementklasse stellt einen ComponentAdapter zur Verf�gung welcher
	 * die Kontaktliste bei Vergr��ern und Verschieben neuzeichnet.
	 * 
	 * @author ATRM
	 *
	 */
	private class MyComponentListener extends ComponentAdapter {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ComponentAdapter#componentMoved(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentMoved( ComponentEvent e ) {
			repaint();
		}//eom componentMoved()
		
		/* (non-Javadoc)
		 * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentResized( ComponentEvent e ) {
			validate();
			repaint();
		}//eom componentResized()
	}//eoc MyComponentListener
	
	/**
	 * Diese Elementklasse stellt einen MouseAdapter bereit.
	 * 
	 * Diese Elementklasse stellt einen MouseAdapter zur Verf�gung welcher
	 * auf Doppelklick bzw. auf rechte Maustaste reagiert.
	 * 
	 * @author ATRM
	 *
	 */
	private class MyMouseListener extends MouseAdapter {

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked( MouseEvent e ) {
			JList source = ( JList )e.getSource();
			// Doppelklick:
			if ( e.getClickCount() == 2 ) {
				Object o = source.getSelectedValue();
				// Klick auf Gruppenliste
				if ( source.getModel().getClass().getSimpleName().startsWith( "Group" ) ) {
					// Gruppe beitreten bzw. focusieren
					GUI.getGUI().addGrpCW( o.toString(), true );
				}
				// Klick auf Userliste
				if ( source.getModel().getClass().getSimpleName().startsWith( "User" ) ) {
					// Pr�fung ob klick auf sich selbst
					if ( !( users.getSelectedValue().getUserID() == ChatEngine.getCE().getUserID() ) ) {
						// privaten Chat zu User erstellen oder fokusieren
						GUI.getGUI().addPrivCW( ( ( Node )o).getUserID(), true );
					}
				}
			}
			// rechte Maustaste:
			if ( e.getModifiersEx() == 256 ){
				// wo wird hingeklickt
				int index = source.locationToIndex( e.getPoint() );
				// klick innerhalb der Kontaktliste
				if ( ( index >= 0 ) && ( index < source.getModel().getSize() ) ) {
					// Auswahl setzen da wo hingeklickt wurde
					source.setSelectedIndex( index );
					Object o = source.getModel().getElementAt( index );
					// wenn das Element vom Typ Node ist wird ein Popup f�r die Userlist ge�ffnet
					if ( o instanceof Node ) {
						Node tmp = ( Node )o;
						PopupUser popupUsr = new PopupUser(new popupListener( tmp.getAlias() ) );
						popupUsr.show( source, e.getX(), e.getY() );
					}
					// wenn in der Gruppenliste geklickt wurde wird ein Popup f�r die Gruppenliste ge�ffnet
					if ( source.getModel().getClass().getSimpleName().startsWith( "Group" ) ) {
						PopupGroup popupGrp = new PopupGroup( o.toString(), new popupListener( o.toString() ) );
						popupGrp.show( source, e.getX(), e.getY() );
					}
				}
			}
		}//eom mouseClicked()
	}//eoc MyMouseListener

	/**
	 * Diese Elementklasse stellt das PopupMenu f�r die Userliste bereit.
	 * 
	 * Diese Elementklasse stellt das PopupMenu f�r die Userliste zur Verf�gung
	 * welches ein Kontextmen� darstellt in dem man einem User Fl�sternachrichten
	 * und Dateien senden kann. In diesem Men� k�nnen User ignored/unignored werden
	 * sowie Informationen zu diesen ausgegeben werden. Man kann seinen eigenen Alias
	 * �ndern und sich selbst als AFK markieren.
	 * 
	 * @author ATRM
	 *
	 */
	private class PopupUser extends JPopupMenu{

		private JMenuItem whisper;
		private JMenuItem sendFile;
		private JMenuItem ignore;
		private JMenuItem unignore;
		private JMenuItem info;
		private JMenuItem changeAlias;
		private JMenuItem afkStatus;

		/**
		 * Konstruktor f�r das PopupMenu f�r User.
		 * 
		 * Dieser Konstruktor �bernimmt einen ActionListener und erstellt
		 * ein PopupMenu f�r einen User.
		 * 
		 * @param popupListener ActionListener
		 */
		private PopupUser( ActionListener popupListener ){
			String user = users.getSelectedValue().getAlias();
			long userID = users.getSelectedValue().getUserID();

			// Initialisierungen der Men�eintr�ge + Icons
			this.whisper 	 = new JMenuItem( "whisper to " + user, Help.getIcon( "whisperSym.png", 14, 16 ) );
			this.sendFile 	 = new JMenuItem( "send File to " + user, Help.getIcon( "sendFileSym.png", 14, 16 ) );
			this.ignore 	 = new JMenuItem( "ignore " + user, Help.getIcon( "ignoreSym.png", 12, 16 ) );
			this.unignore	 = new JMenuItem( "unignore " + user, Help.getIcon( "unignoreSym.png", 14, 16 ) );
			this.info 		 = new JMenuItem( "info's about " + user, Help.getIcon( "infoSym.png" ) );
			this.changeAlias = new JMenuItem( "change Alias", Help.getIcon( "changeAliasSym.png", 14, 16 ) );
			this.afkStatus	 = new JMenuItem( "AFK (on/off)", Help.getIcon( "afkSym.png", 16, 12 ) );

			// Hinzuf�gen der Listener
			this.whisper.addActionListener( popupListener );
			this.sendFile.addActionListener( popupListener );
			this.ignore.addActionListener( popupListener );
			this.unignore.addActionListener( popupListener );
			this.info.addActionListener( popupListener );
			this.changeAlias.addActionListener( popupListener );
			this.afkStatus.addActionListener( popupListener );

			// �berpr�fen ob Men� f�r sich selbst oder nicht
			// Men� f�r andere User
			if ( !( userID==ChatEngine.getCE().getUserID() ) ) {
				this.add( whisper );
				this.add( new Separator() );
				this.add( sendFile );
				this.add( new Separator() );
				// falls User schon ignoriert ist wird unignore Men� gezeigt
				// und andersherum
				if ( !ChatEngine.getCE().is_uid_ignored( userID ) ) {
					this.add( ignore );
				} else {
					this.add( unignore );
				}
				// trenner hinzuf�gen
				this.add( new Separator() );
			// Men� f�r sich selbst
			} else {
				this.add( changeAlias );
				this.add( afkStatus );
			}
			// In jedem Fall wird das InfoMen� hinzugef�gt
			this.add( info );
		}//eom PopupUser()
	}//eoc PopupUser

	/**
	 * Diese Elementklasse stellt das PopupMenu f�r die Gruppenliste bereit.
	 * 
	 * Diese Elementklasse stellt das PopupMenu f�r die Gruppenliste zur Verf�gung
	 * welches ein Kontextmen� darstellt in dem man einer Gruppe beitreten oder diese
	 * verlassen kann.
	 * 
	 * @author ATRM
	 *
	 */
	private class PopupGroup extends JPopupMenu{

		private JMenuItem	join;
		private JMenuItem	leave;
		private String		group;

		/**
		 * Konstruktor f�r das PopupMenu f�r Gruppen.
		 * 
		 * Dieser Konstruktor �bernimmt einen Namen f�r die Gruppe, einen
		 * ActionListener und erstellt ein PopupMenu f�r einen User.
		 * 
		 * @param group String 
		 * @param popupListener ActionListener
		 */
		private PopupGroup( String group, ActionListener popupListener ) {
			this.group = group;
			
			// Initialisierungen der Men�eintr�ge
			this.join = new JMenuItem( "join " + group, Help.getIcon( "joinGrpSym.png" ) );
			this.leave = new JMenuItem( "leave " + group, Help.getIcon( "leaveGrpSym.png" ) );

			// Listener hinzuf�gen
			this.join.addActionListener( popupListener );
			this.leave.addActionListener( popupListener );

			// Men�eintr�ge hinzuf�gen
			this.add( join );
			this.add( leave );
		}//eom PopupGroup()
	}//eoc PopupGroup

	/**
	 * Diese Elementklasse stellt einen ActionListener bereit.
	 * 
	 * Diese Elementklasse stellt einen ActionListener f�r die PopupMen�s
	 * bereit.
	 * 
	 * @author ATRM
	 *
	 */
	private class popupListener implements ActionListener{

		private String chatname;

		/**
		 * Konstruktor f�r den PopupMenu ActionListener.
		 * 
		 * Dieser Konstruktor �bernimmt einen String (chatname)
		 * und setzt die Variable chatname auf den �bergebenen String.
		 * 
		 * @param chatname Strin
		 */
		private popupListener( String chatname ){
			this.chatname = chatname;
		}//eom popupListener( String chatname )

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed( ActionEvent e ) {
			JMenuItem source = ( JMenuItem )e.getSource();

			// Klick auf "join group"
			if ( source.getText().startsWith( "join" ) ) {
				GUI.getGUI().addGrpCW( chatname, true );
			}
			// Klick auf "leave group"
			else if ( source.getText().startsWith( "leave" ) ) {
				GUI.getGUI().delChat(chatname);
			}
			// Klick auf "whisper to <username>"
			else if ( source.getText().startsWith( "whisper" ) ) {
				GUI.getGUI().addPrivCW( users.getSelectedValue().getUserID(), true );
			}
			// Klick auf "ignore <user>"
			else if ( source.getText().startsWith( "ignore" ) ) {
				GUI.getGUI().ignoreUser( users.getSelectedValue().getUserID() );
			}
			// Klick auf "unignore <user>"
			else if ( source.getText().startsWith( "unignore" ) ) {
				GUI.getGUI().unignoreUser( users.getSelectedValue().getUserID() );
			}
			// Klick auf "info's about <user>"
			else if ( source.getText().startsWith( "info" ) ) {
				GUI.getGUI().getActiveCW().printInfo( users.getSelectedValue() );
			}
			// Klick auf "send file to <user>"
			else if ( source.getText().startsWith( "send" ) ) {
				GUI.getGUI().sendFile( users.getSelectedValue().getUserID() );
			}
			// Klick auf "AFK (on/off)"
			else if ( source.getText().startsWith( "AFK" ) ) {
				GUI.getGUI().afk();
			}
			// Klick auf "change Alias"
			else if ( source.getText().startsWith( "change" ) ) {
				String tmpAlias = null;
				tmpAlias = ( String )JOptionPane.showInputDialog( GUI.getGUI(), "Enter new Alias", "Change Alias", JOptionPane.OK_CANCEL_OPTION, Help.getIcon( "private.png" ), null, null );
				if ( ( tmpAlias != null ) && !tmpAlias.equals( "" ) ){
					GUI.getGUI().changeAlias( tmpAlias );
				} 
			}
		}//eom actionPerformed()
	}//eoc popupListener

	/**
	 * Diese Elementklasse rendert die Elemente der Userlist
	 * 
	 * Diese Elementklasse erm�glicht es die Elemente der Userliste wie gew�nscht darzustellen.
	 * 
	 * @author ATRM
	 *
	 */
	private class UsersListCellRenderer extends JLabel implements ListCellRenderer {
		
		/**
		 * Konstruktor f�r den ListCellRenderer
		 */
		private UsersListCellRenderer() {
			setOpaque( true );
		}//eom UsersListCellRenderer()
		
		/* (non-Javadoc)
		 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
		 */
		public Component getListCellRendererComponent( JList paramlist, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
			//die Anzeige des Alias in der Userliste anpassen:
			String tmpText = value.toString();
			String[] cutText = tmpText.split( "@", 2 );
			//es soll nur der Alias angezeigt werden ohne Rechnername
			setText( cutText[0] );
			Node tmp = ( Node )value;
			// wenn ein Nutzer ignoriert wird soll er rot angezeigt werden und einen
			// Zusatz zum Alias "(ignored)" erhalten
			if ( ChatEngine.getCE().is_ignored( tmp.getNodeID() ) ) {
				setText( cutText[0] + " (ignored)" );
				setForeground( Color.RED );
			// Falls man AFK geht wird der eigene Name in der Liste grau und mit einen
			// Zusatz "(afk)" versehen
			} else if ( ( ChatEngine.getCE().getMyNodeID() == tmp.getNodeID() ) && GUI.getGUI().isAFK() ){
				setText( cutText[0] + " (afk)" );
				setForeground( Color.GRAY );
			// Der eigene Name soll orange angezeigt werden
			} else if ( ChatEngine.getCE().getMyNodeID() == tmp.getNodeID() ) {
				setForeground( new Color( 255, 133, 18 ) );
			// alle anderen schwarz
			} else {
				setForeground( Color.BLACK );
			}

			// Listenelement ausgew�hlt wird Hintergrund blau
			if ( isSelected ) {
				setBackground( new Color( 25, 169, 241 ) );
			// ansonsten wei�
			} else {
				setBackground( Color.WHITE );
			}
			if ( cellHasFocus ) {
				setBackground( getBackground().darker() );
			}
			return this;
		}// eom getListCellRendererComponent()
	}//eoc UserListCellRenderer

	/**
	 * Diese Elementklasse rendert die Elemente der Gruppenliste
	 * 
	 * Diese Elementklasse erm�glicht es die Elemente der Gruppenliste wie gew�nscht darzustellen.
	 * 
	 * @author ATRM
	 *
	 */
	private class GroupsListCellRenderer extends JLabel implements ListCellRenderer {
		
		/**
		 * Konstruktor f�r den ListCellRenderer
		 */
		private GroupsListCellRenderer() {
			setOpaque( true );
		}//eom GroupsListCellRenderer()
		
		/* (non-Javadoc)
		 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
		 */
		public Component getListCellRendererComponent( JList paramlist, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
			String tmpText = value.toString();
			setText( tmpText );
			// Die Standardgruppe "public" soll orange dargestellt werden
			if ( tmpText.equals( "public" ) ) {
				setForeground( new Color( 255, 133, 18 ) );
			// alle anderen schwarz
			} else {
				setForeground( Color.BLACK );
			}

			// Ausgew�hlte Listenelemente sollen blau hinterlegt sein
			if ( isSelected ) {
				setBackground( new Color( 25, 169, 241 ) );
			// alle andere wei�
			} else {
				setBackground( Color.WHITE );
			}
			if ( cellHasFocus ) {
				setBackground( getBackground().darker() );
			}
			return this;
		}//eom getListCellRendererComponent()
	}//eoc GroupListCellRenderer
}//eoc ContactList