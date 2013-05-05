package org.publicmain.nodeengine;
import org.publicmain.common.Node;

/**Interface f�r ein Strategy-Pattern zur Ermittlung eines Kandidaten (Knoten) aus einem Baum.
 * @author ATRM
 *
 */
public interface BestNodeStrategy {
	/** Sucht in einem Baum aus TreeNodes den Besten Kandidaten f�r eine Verbindung. Die Wahl wird durch die Implementierung bestimmt
	 * @return Kandidat f�r Verbindung
	 */
	Node getBestNode();

}
