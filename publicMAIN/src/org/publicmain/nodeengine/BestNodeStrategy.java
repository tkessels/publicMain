package org.publicmain.nodeengine;
import org.publicmain.common.Node;

/**Interface f�r ein Strategy-Pattern zur Ermittlung eines Kandidaten (Knoten) aus einem Baum.
 * @author ATRM
 *
 */
public interface BestNodeStrategy {
	/** TODO
	 * @return
	 */
	Node getBestNode();

}
