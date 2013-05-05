package org.publicmain.nodeengine;
import org.publicmain.common.Node;

/**Interface für ein Strategy-Pattern zur Ermittlung eines Kandidaten (Knoten) aus einem Baum.
 * @author ATRM
 *
 */
public interface BestNodeStrategy {
	/** Sucht in einem Baum aus TreeNodes den Besten Kandidaten für eine Verbindung. Die Wahl wird durch die Implementierung bestimmt
	 * @return Kandidat für Verbindung
	 */
	Node getBestNode();

}
