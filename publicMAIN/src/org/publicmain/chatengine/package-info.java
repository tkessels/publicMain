package org.publicmain.chatengine;

/**
 * Dieses Package enthält alle Bestandteile der ChatEngine nach dem erstellten Schichtenmodell.
 * Die ChatEngine und alle dazugehörigen Komponenten sind für die Verwaltung der aktuellen Session
 * verantwortlich und bearbeitet alle Vorgänge die oberhalb der Netzwerkschicht anfallen. Dazu zählen
 * unter anderem:
 * 
 * - Filtern der Nachrichten nach Gruppe,
 * - Filtern der Nachrichten nach Empfänger (private Nachrichten),
 * - Filtern der Nachrichten nach der <code>ignored</code>-Liste
 * - die Bereitstellung der Dienste für den Dateiversand unterhalb
 *   der grafischen Benutzerschnittstelle,
 * - benachrichtigen der GUI bei erforderlichen Interaktionen.     
 *
 * Verantwortlich für die ChatEngine: Martin Szymczak
 * 
 * @author ATRM
 * 
 */

