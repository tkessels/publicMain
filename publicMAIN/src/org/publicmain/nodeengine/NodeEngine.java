package org.publicmain.nodeengine;

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.tree.TreeNode;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.Config;
import org.publicmain.common.ConfigData;
import org.publicmain.common.FileTransferData;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.Node;
import org.publicmain.gui.GUI;
import org.resources.Help;

/**
 * Die NodeEngine ist für die Verbindungen zu anderen Nodes zuständig. Sie
 * verwaltet die bestehenden Verbindungen, sendet Nachrichten und Dateien und ist
 * für das Routing zuständig.
 * 
 * @author ATRM
 * 
 */

public class NodeEngine {
	// Zeitspanne nach der ein Node die Suche nach anderen Nodes aufgibt und
	// sich selbst zur Root erklärt.
	private final long		       DISCOVER_TIMEOUT   = Config.getConfig().getDiscoverTimeout();
	// Zeitspanne die ein Root auf ROOT_ANNOUNCES wartet um zu entscheiden wer
	// Root bleibt.
	private final long		       ROOT_CLAIM_TIMEOUT = Config.getConfig().getRootClaimTimeout();
	// Statischer Zeiger auf die einzige Instanz der NodeEngine.
	private static volatile NodeEngine       ne;
	private final long		       nodeID;
	// Die Node-Repräsentation dieser NodeEngine.
	private final Node		       meinNode;
	// Zeiger auf die ChatEngine.
	private final ChatEngine		 ce;
	// Haken zum fangen von Nachrichten.
	private final Hook		       angler	     = new Hook();
	// Server-Socket für eingehende Verbindungen (Passiv/Childs).
	//private final ServerSocket server_socket;
	// TCP-Socket zur Verbindung mit anderen Nodes (Aktiv/Parent/Root).
	//private ConnectionHandler root_connection;
	// Multicast/Broadcat UDP-Socket zur Verbindungsaushandlung.
	private final MulticastConnectionHandler multi_socket;
	// Liste bestehender Child-Verbindungenin eigener Hüll-Klasse.
	//public List<ConnectionHandler> connections;
	// Set, vom Typ String, aller Gruppen.
	private final Set<String>		allGroups	  = new HashSet<String>();
	// Set, vom Typ String, aller <u>abonierten</u> Gruppen und aller
	// untergeordneter Nodes.
	private final Set<String>		myGroups	   = new HashSet<String>();
	// Warteschlange (Queue) für Bewerberpakete bei Neuverhandelung der Root.
	private final BlockingQueue<MSG>	 root_claims_stash;
	// Set, vom Typ Node, aller dieser NodeEngine bekannten Nodes.
	private final Set<Node>		  allNodes;
	// Dieser Knoten möchte die Root sein und benimmt sich dementsprechend.
	private volatile boolean		 rootMode;
	// Dieser Node möchte an sein und verbunden bleiben, alle Threads werden
	// informiert wenn die Anwednung beendet wird.
	private volatile boolean		 online;
	// Dieser Node sammelt gerade ROOT_ANNOUNCES um einen neuen Root zu
	// bestimmen.
	private volatile boolean		 rootDiscovering;
	// Dieser Thread akzeptiert und schachelt eingehende Verbindungen auf dem
	// Server-Socket.
	//private final Thread connectionsAcceptBot = new Thread(new ConnectionsAccepter());
	// Dieser Thread bestimmt nach einer Verzögerung diesen
	// Node zum Root, er wird durch einen Discover gestartet.
	private Thread			   rootMe;
	// Der Thread sammelt und wertet ROOT_ANNOUNCES aus, er wird beim Empfang
	// oder Versand eines ROOT_ANNOUNCE gestartet.
	private Thread			   rootClaimProcessor;
	private final Thread		     neMaintainer;

	/**
	 * Konstruktor für die NodeEngine.
	 * 
	 * @param parent
	 * 		die dazugehörige ChatEngine.
	 * @throws IOException
	 * 		wirft IOExceptions, wenn der Serverport nicht geöffnet werden kann.
	 */
	public NodeEngine(final ChatEngine parent) throws IOException {

		this.allNodes = Collections.synchronizedSet(new HashSet<Node>());
		//this.connections = new CopyOnWriteArrayList<ConnectionHandler>();

		this.root_claims_stash = new LinkedBlockingQueue<MSG>();

		this.nodeID = ((long) (Math.random() * Long.MAX_VALUE));
		ne = this;
		this.ce = parent;
		this.online = true;

		//this.server_socket = new ServerSocket(0);
		this.multi_socket = MulticastConnectionHandler.getMC();
		this.multi_socket.registerNodeEngine(this);

		this.meinNode = new Node(this.multi_socket.getPort(), this.nodeID, this.ce.getUserID(), System.getProperty("user.name"), this.ce.getAlias());
		this.allNodes.add(this.meinNode);

		//this.connectionsAcceptBot.start();

		this.neMaintainer = new Thread(new Maintainer());
		;
		this.neMaintainer.start();

		LogEngine.log(this, "Multicast Socket geöffnet", LogEngine.INFO);

	}

	/**
	 * Getter für die laufende Instanz der NodeEngine.
	 * 
	 * @return NodeEngine
	 */
	public static NodeEngine getNE() {
		return ne;
	}

	/**
	 * Getter für diese NodeEngine.
	 * 
	 * @return this
	 */
	public Node getMe() {
		return this.meinNode;
	}

	/**
	 * Gibt <code>true</code> zurück, wenn die Anwendung den Status online hat
	 * und <code>false</code> wenn nicht.
	 * 
	 * @return boolean
	 */
	public boolean isOnline() {
		return this.online;
	}

	/**
	 * Getter für ein Set von allen verbundenen Nodes.
	 * 
	 * @return boolean
	 */
	public Set<Node> getNodes() {
		synchronized (this.allNodes) {
			return this.allNodes;
		}
	}

	/**
	 * Liefert das NodeObjekt zu einer NodeID. Sollte der Node nicht bekannt
	 * sein, wird <code>null</code> zurückgeliefert. Befindet sich der Knoten, der
	 * die Abfrage ausführt, im RootMode, versucht er den Knoten über ein Lookup
	 * zu finden. Schlägt dieser Versuch ebenfalls fehl, wird ein Befehl an den
	 * Knoten geschickt, sich neu zu verbinden (noch nicht implementiert).
	 * 
	 * @param nid
	 *            NodeID
	 * @return Node-Objekt zu angegebenem NodeID oder null wenn
	 */
	public Node getNode(final long nid) {
		synchronized (this.allNodes) {
			for (final Node x : getNodes()) {
				if (x.getNodeID() == nid)
					return x;
			}
			return null;
		}
	}

	/**
	 * Liefert eine Node für eine bestimmte <code>uid</code>.
	 * 
	 * @param uid UserID des zu suchenden Nutzers
	 * @return zur UserID zugehöriges NodeObjekt
	 */
	public Node getNodeForUID(final long uid) {
		synchronized (this.allNodes) {
			for (final Node x : getNodes()) {
				if (x.getUserID() == uid)
					return x;
			}
			return null;
		}
	}

	/**
	 * Getter für ein Set, vom Typ String, für alle Gruppen.
	 * 
	 * @return Set von Gruppennamen
	 */
	public Set<String> getGroups() {
		return this.allGroups;
	}

	/**
	 * Sendet eine Nachricht an den übergeordneten Node, wenn einer existiert.
	 * 
	 * @param nachricht das zu versende Nachrichtenobjekt
	 */
	private void sendroot(final MSG nachricht) {
		sendmutlicast(nachricht);
	}

	/**
	 * Versendet Daten vom Typ MSG an bestimmte Nodes oder gibt diese an
	 * send_file() weiter. Hier wird geprüft, ob die Dateigröße < 5MB (Aufruf der
	 * send_file()) anderenfalls als Msg-Type Data
	 * 
	 * @param nachricht das zu versende Nachrichtenobjekt
	 */
	private void sendmutlicast(final MSG nachricht) {
		this.multi_socket.sendmutlicast(nachricht);
	}

	/**
	 * Sendet auf dem Multicast-Socket ein Unicast-Paket.
	 * 
	 * @param msg
	 *            ist die Nachricht, die verschickt werden soll (darf nicht
	 *            größer 64KB sein)
	 * @param target Zielnode für Unicastpaket
	 */
	private void sendunicast(final MSG msg, final Node target) {
		this.multi_socket.sendunicast(msg, target);
	}

	/**
	 * Sendet eine Nachricht an alle angeschlossenen Verbindungen.
	 * 
	 * @param nachricht Zu sendendes Nachrichtenobjekt
	 */
	public void sendtcp(final MSG nachricht) {
		sendmutlicast(nachricht);
	}

	/**
	 * Sendet eine Nachricht an alle angeschlossenen Verbindungen, außer an die
	 * mitgelieferte Verbindung.
	 * 
	 * @param msg zu versendendes Nachrichtenobjekt
	 * @param ch Beim Versand auszuschließende Verbindung
	 */
	private void sendtcpexcept(final MSG msg, final ConnectionHandler ch) {
		sendmutlicast(msg);
	}

	//	/**
	//	 * Versendet Datein über eine TCP-Direktverbindung. Wird nur von send()
	//	 * aufgerufen, nachdem festgestellt wurde, dass Nachricht > 5MB
	//	 * 
	//	 * @param datei Das zu versendende Fileobjekt
	//	 * @param receiver NodeID des Empfängers
	//	 */
	//	public void send_file(final File datei, final long receiver) {
	//		if (datei.isFile() && datei.exists() && datei.canRead()
	//				&& (datei.length() > 0)) {
	//			new Thread(new Runnable() {
	//				@Override
	//				public void run() {
	//					// Erstelle das Parameter Objekt für die Dateiübertragung
	//					final FileTransferData tmp_FR = new FileTransferData(datei,
	//							datei.length(), NodeEngine.this.meinNode, NodeEngine.this.getNode(receiver));
	//					// Wenn Datei unterhalb des Schwellwerts liegt: Als
	//					// Nachricht verschicken...
	//					if (datei.length() < Config.getConfig().getMaxFileSize()) {
	//						try {
	//							NodeEngine.this.routesend(new MSG(tmp_FR));
	//						} catch (final IOException e1) {
	//							LogEngine.log(e1);
	//						}
	//					} else {
	//						// Sonst: Direkt übertragen...
	//						// Server Thread
	//						new Thread(new Runnable() {
	//							@Override
	//							public void run() {
	//								// Datei holen, Socket öffnen
	//								try (final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(datei));
	//										final ServerSocket f_server = new ServerSocket(0)) {
	//									// Warten
	//									Socket client = null;
	//									f_server.setSoTimeout((int) Config.getConfig().getFileTransferTimeout());
	//									synchronized (tmp_FR) {
	//										tmp_FR.server_port = f_server.getLocalPort();
	//										tmp_FR.notify();
	//									}
	//
	//									// Server Close Thread
	//									new Thread(new Runnable() {
	//										@Override
	//										public void run() {
	//											final MSG tmp_msg = NodeEngine.this.angler.fishfor(NachrichtenTyp.SYSTEM, MSGCode.FILE_TCP_ABORT, tmp_FR.getReceiver_nid(), tmp_FR.hashCode(), true, Config.getConfig().getFileTransferTimeout());
	//											if (tmp_msg != null) {
	//												try {
	//													GUI.getGUI().info("User " + tmp_FR.receiver.getAlias() + "has denied recieving the file: " + tmp_FR.datei.getName(), tmp_FR.receiver.getUserID(), 0);
	//													f_server.close();
	//												} catch (final IOException e) {
	//												}
	//											}
	//										}
	//									}).start();
	//
	//									// Verbindung anbieten
	//									client = f_server.accept();
	//									try {
	//										f_server.close();
	//									} catch (final Exception e) {
	//									}
	//									// Übertragen
	//									if ((client != null) && client.isConnected() && !client.isClosed()) {
	//										final BufferedOutputStream bos = new BufferedOutputStream(client.getOutputStream());
	//										long infoupdate = System.currentTimeMillis() + Config.getConfig().getFileTransferInfoInterval();
	//										long transmitted = 0;
	//										final byte[] cup = new byte[65535];
	//										int len = -1;
	//										while ((len = bis.read(cup)) != -1) {
	//											bos.write(cup, 0, len);
	//											transmitted += len;
	//											if (System.currentTimeMillis() > infoupdate) {
	//												infoupdate = System.currentTimeMillis() + Config.getConfig().getFileTransferInfoInterval();
	//												GUI.getGUI().info(tmp_FR.datei.getName() + "(" + ((transmitted * 100) / tmp_FR.size) + "%)", tmp_FR.sender.getUserID(), 0);
	//											}
	//										}
	//										bos.flush();
	//										bos.close();
	//
	//										GUI.getGUI().info(tmp_FR.datei.getName() + " Done", tmp_FR.sender.getUserID(), 0);
	//									}
	//									// Ergebnis melden
	//								} catch (final FileNotFoundException e) {
	//									LogEngine.log("FileTransfer", e.getMessage(), LogEngine.ERROR);
	//								} catch (final SocketTimeoutException e) {
	//									LogEngine.log("FileTransfer", "Timed Out", LogEngine.ERROR);
	//									GUI.getGUI().info("User " + tmp_FR.receiver.getAlias() + " has not answered in time. Connection Timedout", tmp_FR.receiver.getUserID(), 0);
	//								} catch (final SocketException e) {
	//									LogEngine.log("FileTransfer", "Aborted", LogEngine.ERROR);
	//								} catch (final IOException e) {
	//									LogEngine.log("FileTransfer", e);
	//									GUI.getGUI().info("Transmission-Error, if this keeps happening buy a USB-Stick", tmp_FR.receiver.getUserID(), 0);
	//								}
	//							}
	//						}).start();
	//
	//						// Warten bis der Server-Thread fertig ist...
	//						synchronized (tmp_FR) {
	//							try {
	//								if (tmp_FR.server_port == -2) {
	//									tmp_FR.wait();
	//								}
	//							} catch (final InterruptedException e) {
	//							}
	//						}
	//						// ... send FileTransferRequest
	//						final MSG request = new MSG(tmp_FR, MSGCode.FILE_TCP_REQUEST,
	//								tmp_FR.getReceiver_nid());
	//						NodeEngine.this.routesend(request);
	//					}
	//				}
	//			}).start();
	//		}
	//	}

	//	/**
	//	 * Behandelt Dateitransferanfragen
	//	 * 
	//	 * @param data_paket Das FileRequest Paket des Senders
	//	 */
	//	private void recieve_file(final MSG data_paket) {
	//		final Object[] tmp = (Object[]) data_paket.getData();
	//		final FileTransferData tmp_file = (FileTransferData) tmp[0];
	//		final File destination;
	//		if (!Config.getConfig().getDisableFileTransfer()
	//				&& ((destination = this.ce.request_File(tmp_file)) != null)) {
	//			tmp_file.accepted = true;
	//			new Thread(new Runnable() {
	//				@Override
	//				public void run() {
	//					try {
	//						data_paket.save(destination);
	//					} catch (final IOException e) {
	//						e.printStackTrace();
	//					}
	//				}
	//			}).start();
	//		} else {
	//			tmp_file.accepted = false;
	//		}
	//		final MSG reply = new MSG(tmp_file, MSGCode.FILE_RECIEVED,
	//				data_paket.getSender());
	//		routesend(reply);
	//	}

	/**
	 * Alle verbundenen Nodes benachrichtigen.
	 */
	private void updateNodes() {
		synchronized (this.allNodes) {
			this.allNodes.clear();
			this.allNodes.add(getMe());
			this.allNodes.addAll(getChilds());
			pollChilds();
			this.allNodes.notifyAll();
		}
	}

	// Ggf. für die weitere Entwicklung benötigt.

	private boolean updateAllNodes(Collection<Node> report) {
		if (!(report.containsAll(getConnected()) && report.contains(meinNode))) {
			pollChilds();
			LogEngine.log(this, "Unvollständige NodeListe erhalten", LogEngine.ERROR);
		}
		return allnodes_set(report);
	}

	private void pollChilds() {
		sendmutlicast(new MSG(null, MSGCode.NODE_LOOKUP));
	}

	/**
	 * Getter für ein Set, vom Typ Node, aller ChildNodes.
	 * 
	 * @return Set aller ChildNodes
	 */
	private Set<Node> getChilds() {
		return allNodes;
	}

	private Set<Node> getConnected() {
		return allNodes;
	}

	/**
	 * Stelle Verbindung mit diesem <code>NODE</code> her!!!!
	 * 
	 * @param knoten
	 *            der Knoten
	 * @throws IOException
	 *             Wenn der hergestellte Socket
	 */
	private void connectTo(final Node knoten) {
		System.out.println("WARUM WIRD HIER VERSUCH SICH ZU VERBINDEN");
	}

	/**
	 * Informiert alle angeschlossenen Nodes über das unmittelbar bevorstehende
	 * Herunterfahren, meldet sich bei den abonierten Gruppen ab und schliesst
	 * den Multicast-Socket.
	 */
	@SuppressWarnings("deprecation")
	public void disconnect() {
		this.online = false;
		//this.connectionsAcceptBot.stop();
		//		multicastRecieverBot.stop();
		sendtcp(new MSG(this.meinNode, MSGCode.NODE_SHUTDOWN));
		sendroot(new MSG(this.myGroups, MSGCode.GROUP_LEAVE));

		this.multi_socket.close();
	}

	/**
	 * Entfernt eine Verbindung wieder.
	 * 
	 * @param conn Die zu Entfernende Verbindung
	 */

	/**
	 * Sendet eine ROOT_ANNOUNCE und initiert so die Neuverhandlung der Root.
	 */
	private void sendRA() {
		final MSG ra = new MSG(this.meinNode, MSGCode.NODE_UPDATE);
		sendmutlicast(ra);
	}

	/**
	 * Hier wird das Paket verarbeitet und weitergeleitet. Diese Methode wird
	 * ausschließlich vom MulticastSocketHandler aufgerufen.
	 * 
	 * @param paket
	 *            Das empfangene MulticastPaket
	 */
	public void handleMulticast(final MSG paket) {
		LogEngine.log(this, "handling [MC]", paket);
		if (this.angler.check(paket))
			return;
		if (this.online) {
			switch (paket.getCode()) {
			case MSGCode.NODE_LOOKUP:
				if ((long) paket.getData() == this.meinNode.getNodeID()) {
					sendroot(new MSG(this.meinNode));
				}
				break;
			case MSGCode.ALIAS_UPDATE:
				this.updateAlias((String) paket.getData(), paket.getSender());
				break;

			case MSGCode.GROUP_MESSAGE:
			case MSGCode.PRIVATE_MESSAGE:
				if (!this.ce.is_ignored(paket.getSender())) {
					this.ce.put(paket);
				}
				break;
			case MSGCode.NODE_UPDATE:

				allnodes_add((Node) paket.getData());
				break;

			default:
				LogEngine.log(this, "handling [MC]:undefined", paket);
			}
		}
	}

	/**
	 * Hier wird das Paket verarbeitet und weitergeleitet. Diese Methode wird
	 * ausschließlich von den ConnectionHandlern aufgerufen, um empfangene Pakete
	 * verarbeiten zu lassen.
	 * 
	 * @param paket
	 *            zu verarbeitendes Paket
	 * @param quelle
	 *            Quelle des Pakets
	 */
	@SuppressWarnings("unchecked")
	//	public void handle(final MSG paket, final ConnectionHandler quelle) {
	//		LogEngine.log(this, "handling[" + quelle + "]", paket);
	//		if (this.angler.check(paket))
	//			return;
	//		if((paket.getEmpfänger() != -1) && (paket.getEmpfänger() != this.nodeID)) {
	//			routesend(paket);
	//		}
	//		else {
	//			switch (paket.getTyp()) {
	//			case PRIVATE:
	//				if(!this.ce.is_ignored(paket.getSender())) {
	//					this.ce.put(paket);
	//				}
	//				break;
	//			case GROUP:
	//				groupRouteSend(paket,quelle);
	//				if(!this.ce.is_ignored(paket.getSender())) {
	//					this.ce.put(paket);
	//				}
	//				break;
	//			case SYSTEM:
	//				switch (paket.getCode()) {
	//				case NODE_UPDATE:
	//
	//					allnodes_add((Node) paket.getData());
	//					sendtcpexcept(paket, quelle);
	//					break;
	//
	//				case POLL_ALLNODES:
	//					if (quelle != this.root_connection) {
	//						quelle.send(new MSG(getNodes(), MSGCode.REPORT_ALLNODES));
	//					}
	//					break;
	//				case REPORT_ALLNODES:
	//					Collection<Node> report =(Collection<Node>) paket.getData();
	//					if(!report.contains(meinNode))sendroot(new MSG(meinNode));
	//					updateAllNodes(report);
	//					sendchild(new MSG(report,MSGCode.REPORT_ALLNODES), null);
	//
	//					break;
	//				case POLL_CHILDNODES:
	//					if (quelle == this.root_connection) {
	////						final Set<Node> tmp = getChilds();
	//						final Collection<Node> tmp = BestNodeStrategy.returnAllNodes(getTree());
	////						tmp.add(this.meinNode);
	//
	//						sendroot(new MSG(tmp, MSGCode.REPORT_CHILDNODES));
	//						pollChilds();
	//					}
	//					break;
	//				case REPORT_CHILDNODES:
	//					if (quelle != this.root_connection) {
	//						quelle.setChildren((Collection<Node>) paket.getData());
	//					}
	//					break;
	//				case NODE_SHUTDOWN:
	//					allnodes_remove(quelle.getChildren());
	//					quelle.close();
	//					break;
	//				case CHILD_SHUTDOWN:
	//					final Collection<Node> shutted = (Collection<Node>) paket.getData();
	//					if (quelle != this.root_connection) {
	//						quelle.removeChildren(shutted);
	//					}
	//					if(shutted.contains(this.meinNode)){
	//						shutted.remove(this.meinNode);
	//						quelle.send(new MSG(this.meinNode));
	//					}
	//					allnodes_remove(shutted);
	//					sendtcpexcept(new MSG(shutted,MSGCode.CHILD_SHUTDOWN), quelle);
	//					break;
	//				case GROUP_JOIN:
	//					quelle.add((Collection<String>) paket.getData());
	//					updateMyGroups();
	//					break;
	//				case GROUP_LEAVE:
	//					quelle.remove((Collection<String>) paket.getData());
	//					updateMyGroups();
	//					break;
	//				case GROUP_ANNOUNCE:
	//					if (addGroup((Collection<String>) paket.getData())) {
	//						sendchild(paket, null);
	//					}
	//					break;
	//				case GROUP_EMPTY:
	//					if (removeGroup((Collection<String>) paket.getData())) {
	//						sendchild(paket, null);
	//					}
	//					break;
	//				case GROUP_POLL:
	//					quelle.send(new MSG(this.allGroups, MSGCode.GROUP_REPLY));
	//					break;
	//				case GROUP_REPLY:
	//					final Set<String> groups = (Set<String>) paket.getData();
	//					quelle.add(groups);
	//					updateMyGroups();
	//					addGroup(groups);
	//					break;
	//				case FILE_TCP_REQUEST:
	//					final FileTransferData tmp = (FileTransferData) paket.getData();
	//					if(!this.ce.is_ignored(paket.getSender())) {
	//						this.recieve_file(tmp);
	//					}
	//					break;
	//				case FILE_RECIEVED:
	//					if(!this.ce.is_ignored(paket.getSender())) {
	//						this.ce.inform((FileTransferData) paket.getData());
	//					}
	//					break;
	//				case NODE_LOOKUP:
	//					Node tmp_node = null;
	//					if ((tmp_node = getNode((long) paket.getData())) != null) {
	//						quelle.send(new MSG(tmp_node));
	//					} else {
	//						sendroot(paket);
	//					}
	//					break;
	//				case PATH_PING_REQUEST:
	//					if (paket.getEmpfänger() == this.nodeID) {
	//						routesend(new MSG(paket.getData(),
	//								MSGCode.PATH_PING_RESPONSE, paket.getSender()));
	//					} else {
	//						routesend(paket);
	//					}
	//					break;
	//				case PATH_PING_RESPONSE:
	//					if (paket.getEmpfänger() == this.nodeID) {
	//						routesend(new MSG(paket.getData(),
	//								MSGCode.PATH_PING_RESPONSE, paket.getSender()));
	//					} else {
	//						routesend(paket);
	//					}
	//					break;
	//				case TREE_DATA_POLL:
	//					sendroot(new MSG(getTree(), MSGCode.TREE_DATA));
	//					break;
	//				case CMD_SHUTDOWN:
	//					System.exit(0);
	//					break;
	//				default:
	//					LogEngine.log(this, "handling[" + quelle + "]:undefined",
	//							paket);
	//					break;
	//				}
	//				break;
	//			case DATA:
	//				if (paket.getEmpfänger() != this.nodeID) {
	//					routesend(paket);
	//				} else {
	//					if(!this.ce.is_ignored(paket.getSender())) {
	//						this.recieve_file(paket);
	//					}
	//				}
	//				break;
	//			default:
	//			}
	//		}
	//	}
	/**
	 * Verwaltung des Dateiempfangs, abhängig von der Konfiguration des Benutzers.
	 * 
//	 * @param tmp Das {@link FileTransferData} Objekt mit allen Informationen über den Dateitransfer
//	 */
//	private void recieve_file(final FileTransferData tmp) {
//		if (!Config.getConfig().getDisableFileTransfer()) {
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					final long until = (System.currentTimeMillis() + Config.getConfig().getFileTransferTimeout()) - 1000;
//					final File destination = NodeEngine.this.ce.request_File(tmp);
//					if (System.currentTimeMillis() < until) {
//						tmp.accepted = (destination != null);
//						MSG reply;
//						if (tmp.accepted) {
//							reply = new MSG(tmp, MSGCode.FILE_RECIEVED, tmp.getSender_nid());
//						} else {
//							reply = new MSG(tmp.hashCode(), MSGCode.FILE_TCP_ABORT, tmp.getSender_nid());
//						}
//						NodeEngine.this.routesend(reply);
//						if (destination != null) {
//							Socket data_con = null;
//							for (final InetAddress ip : tmp.sender.getSockets()) {
//								if (!NodeEngine.this.meinNode.getSockets().contains(ip)) {
//									try {
//										data_con = new Socket(ip, tmp.server_port);
//									} catch (final IOException e) {
//										e.printStackTrace();
//									}
//								}
//							}
//							if (data_con != null) {
//								try (final BufferedInputStream bis = new BufferedInputStream(data_con.getInputStream()); final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destination))) {
//									long infoupdate = System.currentTimeMillis() + Config.getConfig().getFileTransferInfoInterval();
//									long transmitted = 0;
//									final byte[] cup = new byte[65535];
//									int len = -1;
//									while ((len = bis.read(cup)) != -1) {
//										bos.write(cup, 0, len);
//										transmitted += len;
//										if (System.currentTimeMillis() > infoupdate) {
//											infoupdate = System.currentTimeMillis() + Config.getConfig().getFileTransferInfoInterval();
//											GUI.getGUI().info(tmp.datei.getName() + "(" + ((transmitted * 100) / tmp.size) + "%)", tmp.sender.getUserID(), 0);
//										}
//									}
//									bos.flush();
//									bos.close();
//									data_con.close();
//									GUI.getGUI().info(tmp.datei.getName() + " Done", tmp.sender.getUserID(), 0);
//								} catch (final SocketException e) {
//									GUI.getGUI().info(tmp.datei.getName() + " Done", tmp.sender.getUserID(), 0);
//								} catch (final IOException e) {
//									e.printStackTrace();
//								}
//							}
//						}
//					}
//				}
//			}).start();
//		} else {
//			tmp.accepted = false;
//			routesend(new MSG(tmp.hashCode(), MSGCode.FILE_TCP_ABORT, tmp.getSender_nid()));
//		}
//	}

	/**
	 * Versendet ein Paket geroutet
	 * 
	 * Das Paket wird in abhängigkeit von seiner ZielnodeID auf der passenden Verbindung weitergeleitet
	 * 
	 * @param paket Zu versendendes Paketobjekt
	 */
	public void routesend(final MSG paket) {
		final long empfänger = paket.getEmpfänger();
		sendunicast(paket, getNode(empfänger));
	}

	/**
	 * Versendet eine Gruppennachricht geroutet
	 * 
	 * Das Paket wird in abhängigkeit von seiner Gruppe auf der passenden Verbindung weitergeleitet
	 * 
	 * @param paket Zu versendendes Paketobjekt
	 * 
	 * @param quelle Verbindung auf der das Paket empfangen wurde
	 */
	public void groupRouteSend(final MSG paket, final ConnectionHandler quelle) {
		sendroot(paket);
	}

	/**
	 * Die eigenen abonierten Gruppen aktualisieren, hinzufügen oder entfernen.
	 * 
	 * 
	 *
	 */
	public void updateMyGroups() {
		final Set<String> aktuell = computeGroups();
		this.myGroups.clear();
		this.myGroups.addAll(aktuell);
	}

	/**
	 * Entfernt alle Knoten einer Kollektion aus der Gesamtliste
	 * 
	 * @param data Zu entfernende Nodes
	 */
	private void allnodes_remove(final Collection<Node> data) {
		synchronized (this.allNodes) {
			final int hash = this.allNodes.hashCode();
			this.allNodes.removeAll(data);
			this.allNodes.add(this.meinNode);
			if (this.allNodes.hashCode() != hash) {
				this.allNodes.notifyAll();
			}
		}
	}

	/**
	 * Fügt Nodes zur Gesamtliste hinzu
	 * 
	 * @param data neue Nodes
	 */
	private void allnodes_add(final Node data) {
		synchronized (this.allNodes) {
			final int hash = this.allNodes.hashCode();
			this.allNodes.add(data);
			if (this.allNodes.hashCode() != hash) {
				this.allNodes.notifyAll();
			}
		}
	}

	/**
	 * Setz die Gesamtliste der Nodes neu
	 * 
	 * @param data neue Gesamtliste
	 */
	private boolean allnodes_set(final Collection<Node> data) {
		Set<Node> tmp = new HashSet<Node>(data);
		synchronized (this.allNodes) {
			if (tmp.hashCode() != allNodes.hashCode()) {
				this.allNodes.clear();
				this.allNodes.addAll(data);
				this.allNodes.add(meinNode);
				this.allNodes.notifyAll();
				return true;
			} else
				return false;

		}
	}

	/**
	 * Startet Lookup für {@link Node} mit der NodeID <code>nid</code>.
	 * Versucht ihn neu verbinden zu lassen, falls die Verbindung fehl
	 * schlägt.
	 * 
	 * @param nid
	 *            ID des Nodes
	 * @return das {@link Node}-Objekt oder <code>null</code>, wenn der Knoten
	 *         nicht gefunden wurde.
	 */
	private Node retrieve(final long nid) {
		sendmutlicast(new MSG(nid, MSGCode.NODE_LOOKUP));
		
		final MSG x = this.angler.fishfor(MSGCode.NODE_UPDATE, nid, null, false, 1000);
		if (x != null)
			return (Node) x.getData();
		else {
			LogEngine.log("retriever", "NodeID:[" + nid + "] konnte nicht aufgespürt werden und sollte neu Verbinden!!!", LogEngine.ERROR);
			//sendmutlicast(new MSG(nid, MSGCode.CMD_RECONNECT));
			return null;
		}
	}

	/**
	 * Setter für den RootMode.
	 * 
	 * @param rootmode
	 */
	private void setRootMode(final boolean rootmode) {
		this.rootMode = rootmode;
		GUI.getGUI().setTitle("publicMAIN" + ((rootmode) ? "[ROOT]" : ""));
		if (rootmode) {
			setGroup(this.myGroups);
		}
	}

	/**
	 * Getter für die eigene <code>nid</code>.
	 * 
	 * @return Die eigene NodeID
	 */
	public long getNodeID() {
		return this.nodeID;
	}

	/**
	 * Entfernt Gruppen aus der Gruppenübersicht
	 * 
	 * @param gruppen_name die zu entfernenden Gruppennamen
	 * @return <code>true</code> Wenn sich die Gruppenliste durch die aktion geändert hat, sonst <code>false</code>
	 */
	public boolean removeGroup(final Collection<String> gruppen_name) {
		synchronized (this.allGroups) {
			final boolean x = this.allGroups.removeAll(gruppen_name);
			this.allGroups.notifyAll();
			return x;
		}
	}

	/**
	 * Setzt Liste verfügbarer Gruppen neu
	 * 
	 * @param groups Neue Gruppenliste
	 */
	public void setGroup(final Collection<String> groups) {
		synchronized (this.allGroups) {
			this.allGroups.clear();
			this.allGroups.addAll(groups);
			this.allGroups.notifyAll();
		}
	}

	/**
	 * Fügt Gruppen der Liste hinzu
	 * 
	 * @param groups Neue Gruppen
	 * @return  <code>true</code> Wenn sich die Gruppenliste durch die aktion geändert hat, sonst <code>false</code>
	 */
	public boolean addGroup(final Collection<String> groups) {
		synchronized (this.allGroups) {
			final boolean x = this.allGroups.addAll(groups);
			this.allGroups.notifyAll();
			return x;
		}
	}

	/**
	 * Verlässt eine Gruppe
	 * 
	 * @param gruppen_name Zu verlassende Gruppe
	 * @return  <code>true</code> Wenn sich die Gruppenliste durch die aktion geändert hat, sonst <code>false</code>
	 */
	public boolean removeMyGroup(final String gruppen_name) {
		synchronized (this.myGroups) {
			return this.myGroups.remove(gruppen_name);
		}
	}

	/**
	 * Tritt einer Gruppe bei
	 * 
	 * @param gruppen_name Zu betretende Gruppe
	 * @return  <code>true</code> Wenn sich die Gruppenliste durch die aktion geändert hat, sonst <code>false</code>
	 */
	public boolean addMyGroup(final String gruppen_name) {
		synchronized (this.myGroups) {
			return this.myGroups.add(gruppen_name);
		}
	}

	/**
	 * Errechnet die Mitglidschaften alle Kindknoten zusammengenommen
	 * 
	 * @return Vereinigung aller Gruppenmitgliedschaften
	 */
	public Set<String> computeGroups() {
		final Set<String> tmpGroups = new HashSet<String>();

		tmpGroups.addAll(this.ce.getMyGroups());
		return tmpGroups;
	}

	/**
	 * Den eigenen Alias ändern und dies allen anderen Nodes mitteilen.
	 */
	public void updateAlias() {
		final String alias = this.ce.getAlias();
		if (this.online && (!alias.equals(this.meinNode.getAlias()))) {
			sendmutlicast(new MSG(alias, MSGCode.ALIAS_UPDATE));
			this.updateAlias(alias, this.nodeID);
		}
	}

//	/**
//	 * Zeigt die Latenz von mir zu allen mir bekannten Knoten über den Baum im aktuellen Chatfenster an
//	 */
//	public void pathPing() {
//		for (final Node cur : getNodes()) {
//			GUI.getGUI().info(cur.toString() + ":" + this.pathPing(cur), null, 0);
//
//		}
//	}

//	/**
//	 * Sendet ein Ping Paket an einen Knoten und registriert einen Hook um die Antwort auszuwerten
//	 * 
//	 * @param remote Der zu pingende Knoten
//	 * @return Latenz in Millisekunden oder -1 wenn keine Antwort innerhalt 1 Sekunde empfangen wurde
//	 */
//	public long pathPing(final Node remote) {
//		if (remote.equals(this.meinNode))
//			return 0;
//		else {
//			final long currentTimeMillis = System.currentTimeMillis();
//			final MSG paket = new MSG(currentTimeMillis, MSGCode.PATH_PING_REQUEST, remote.getNodeID());
//			final MSG response = this.angler.fishfor(NachrichtenTyp.SYSTEM, MSGCode.PATH_PING_RESPONSE, remote.getNodeID(), currentTimeMillis, true, 1000, paket);
//			if (response == null)
//				return -1;
//			else
//				return (System.currentTimeMillis() - currentTimeMillis);
//
//		}
//	}

	/**
	 * Geänderten Alias der GUI mitteilen, damit der Alias korrekt auf der GUI
	 * dargestellt wird.
	 * 
	 * @param newAlias Neuer Anzeigename des Knoten
	 * @param nid Der zu aktualisierende Knoten
	 * 
	 * @return boolean
	 */
	private boolean updateAlias(final String newAlias, final long nid) {
		Node tmp;
		synchronized (this.allNodes) {
			if ((tmp = getNode(nid)) != null) {
				if (!tmp.getAlias().equals(newAlias)) {
					tmp.setAlias(newAlias);
					this.allNodes.notifyAll();
					GUI.getGUI().notifyGUI();
					// Zur Aktualisierung der Datenbank wegschreiben.
					LogEngine.log(this, "User " + tmp.getAlias() + " has changed ALIAS to " + newAlias, LogEngine.INFO);
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Debug-Parameter (/debug) für die Eingabe auf der GUI werden hier
	 * interpretiert.
	 * 
	 * @param command Debug-Befehl
	 * @param parameter Debug-Befehl-Parameter
	 */
	public void debug(final String command, final String parameter) {
		switch (command) {
		case "ra":
			sendRA();
			break;
		case "play":
			Help.playSound(parameter);
			break;
		case "sound":
			//			if("on".equals(parameter.toLowerCase())){
			//				Config.getConfig().setSoundActivated(true);
			//			}else Config.getConfig().setSoundActivated(false);
			Config.getConfig().setSoundActivated("on".equals(parameter));
			break;
		case "stop":
			Help.stopSound();
		case "gc":
			System.gc();
			break;
//		case "poll":
//			sendroot(new MSG(null, MSGCode.POLL_ALLNODES));
//			break;
		case "nup":
			sendtcp(new MSG(this.meinNode, MSGCode.NODE_UPDATE));
			break;
//		case "pingall":
//			this.pathPing();
//			break;
//		case "_kick":
//			final Node tmp = this.ce.getNodeforAlias(parameter);
//			if (tmp != null) {
//				routesend(new MSG(null, MSGCode.CMD_SHUTDOWN, tmp.getNodeID()));
//			}
//			break;
		case "maxcon":
			Config.getConfig().setMaxConnections(Integer.parseInt(parameter));
			break;
		case "update":
			GUI.getGUI().notifyGUI();
			break;
		case "conf":
			Config.writeSystemConfiguration();
			break;
//		case "reconnect_all":
//			sendmutlicast(new MSG(-1337l, MSGCode.CMD_RECONNECT));
//			break;
//		case "poll_bus":
//			this.multi_socket.discoverBUS();
//			break;
		default:
			LogEngine.log(this, "debug command not found", LogEngine.ERROR);
			break;
		}
	}

	//	/**
	//	 * Erstellt den Topologie-Baum für das Debug-Kommando (/debug tree).
	//	 *
	//	 * @return TreeNode der die Baumtopologie repräsentiert
	//	 */
	//	public Node getTree() {
	//		Node root = (Node) meinNode.clone();
	//		// Zuerst werden alle Knoten hergestellt...
	//		for (final ConnectionHandler con : connections) {
	//			Runnable tmp = new Runnable() {
	//				public void run() {
	//					con.send(new MSG(null, MSGCode.TREE_DATA_POLL));
	//				}
	//			};
	//			MSG polled_tree = angler.fishfor(NachrichtenTyp.SYSTEM,
	//					MSGCode.TREE_DATA, null, null, true, Config.getConfig()
	//					.getTreeBuildTime(), tmp);
	//			if (polled_tree != null) {
	//				root.add((Node) polled_tree.getData());
	//			}
	//		}
	//		return root;
	//	}
	//

	/**
	 * Liefert die <code>uid</code> für eine <code>nid</code>.
	 * 
	 * @param nid NodeID des gesuchten Users
	 * @return UserID des Nodes
	 */
	public long getUIDforNID(final long nid) {
		final Node node = getNode(nid);
		if (node != null)
			return node.getUserID();
		else
			return -1;
	}

	/**
	 * Visualisiert den Topologie-Baum für das Debug-Kommando (/debug tree).
	 * 
	 * @param root Wurzel des Baums der dargestellt werden soll.
	 */
	public void showTree(final TreeNode root) {
		// Der Wurzelknoten wird dem neuen JTree im Konstruktor übergeben
		final JTree tree = new JTree(root);
		// Ein Frame herstellen, um den Tree anzuzeigen
		final JFrame frame = new JFrame("publicMAIN - Topology");
		frame.add(new JScrollPane(tree));
		frame.setIconImage(Help.getIcon("pM_Logo.png").getImage());
		frame.setMinimumSize(new Dimension(250, 400));
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	/**
	 * @author tkessels
	 *
	 * Führt einige Tasks periodisch aus um den Betrieb des Netzes zu gewährleisten
	 *
	 */
	private final class Maintainer implements Runnable {
		@Override
		public void run() {
			while (NodeEngine.this.online) {

				sendroot(new MSG(NodeEngine.this.meinNode));

				try {
					Thread.sleep(5000);
				} catch (final InterruptedException e) {
				}
			}
		}
	}

}
