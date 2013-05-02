package org.publicmain.nodeengine;

/**
 * Dieses Package enth�lt alle Bestandteile der NodeEngine der Anwendung pM nach dem erstellten
 * Schichtenmodell. In diesem Paket werden die Routingfunktionalit�t sowie die Verbindungen zu
 * den anderen Nodes verwaltet sowie folgende Funktionalit�ten bereitgestellt:
 * 
 * - verbindet automatisch laufende Instanzen (pMNodes) im Netzwerk,
 * - erweitert das logische Netzwerk (pMnet) der pMNodes indem es den Datenstrom �ber andere Instanzen zum Ziel sendet,
 * - stellt Verbindungen zu pMNodes nach einem Verbindungsabbruch automatisch wieder her, wenn diese wieder zur Verf�gung stehen,
 * - stellt Verbindungen bei Dateitransfers automatisch �ber Unicast her,
 * - routet Nachrichten im pMnet.
 *
 * Verantwortlich f�r das SQL-Package: Tobias Kessels
 * 
 * @author ATRM
 * 
 */