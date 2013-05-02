package org.publicmain.nodeengine;

/**
 * Dieses Package enthält alle Bestandteile der NodeEngine der Anwendung pM nach dem erstellten
 * Schichtenmodell. In diesem Paket werden die Routingfunktionalität sowie die Verbindungen zu
 * den anderen Nodes verwaltet sowie folgende Funktionalitäten bereitgestellt:
 * 
 * - verbindet automatisch laufende Instanzen (pMNodes) im Netzwerk,
 * - erweitert das logische Netzwerk (pMnet) der pMNodes indem es den Datenstrom über andere Instanzen zum Ziel sendet,
 * - stellt Verbindungen zu pMNodes nach einem Verbindungsabbruch automatisch wieder her, wenn diese wieder zur Verfügung stehen,
 * - stellt Verbindungen bei Dateitransfers automatisch über Unicast her,
 * - routet Nachrichten im pMnet.
 *
 * Verantwortlich für das SQL-Package: Tobias Kessels
 * 
 * @author ATRM
 * 
 */