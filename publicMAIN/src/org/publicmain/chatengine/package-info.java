package org.publicmain.chatengine;

/**
 * Dieses Package enth�lt alle Bestandteile der ChatEngine nach dem erstellten Schichtenmodell.
 * Die ChatEngine und alle dazugeh�rigen Komponenten sind f�r die Verwaltung der aktuellen Session
 * verantwortlich und bearbeitet alle Vorg�nge die oberhalb der Netzwerkschicht anfallen. Dazu z�hlen
 * unter anderem:
 * 
 * - Filtern der Nachrichten nach Gruppe,
 * - Filtern der Nachrichten nach Empf�nger (private Nachrichten),
 * - Filtern der Nachrichten nach der <code>ignored</code>-Liste
 * - die Bereitstellung der Dienste f�r den Dateiversand unterhalb
 *   der grafischen Benutzerschnittstelle,
 * - benachrichtigen der GUI bei erforderlichen Interaktionen.     
 *
 * Verantwortlich f�r die ChatEngine: Martin Szymczak
 * 
 * @author ATRM
 * 
 */

