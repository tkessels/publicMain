package org.publicmain.nodeengine;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.publicmain.common.Node;

/**Interface f�r ein Strategy-Pattern zur Ermittlung eines Kandidaten (Knoten) aus einem Baum.
 * @author ATRM
 *
 */
public abstract class BestNodeStrategy {
	/**Ermittelt alle Kindknoten eines Knoten
	 * @param node Wurzelknoten
	 * @return Liste aller Kindknoten einschlie�lich des Wurzelknotens
	 */
	public static List<Node> returnAllNodes(Node node) {
		List<Node> listOfNodes = new ArrayList<Node>();
		addAllNodes(node, listOfNodes);
		return listOfNodes;
	}

	/** F�gt alle Kinder und Kindeskinder mit einer Breitensuche einer Liste von Knoten hinzu.
	 * @param node Wurzel des zu druchlaufenden Baums
	 * @param listOfNodes zu bef�llende Liste
	 */
	private static void addAllNodes(Node node, List<Node> listOfNodes) {
		if (node != null) {
			Enumeration<Node> cursor = node.breadthFirstEnumeration();
			while(cursor.hasMoreElements()) {
				listOfNodes.add(cursor.nextElement());
			}
		}
	}


	/** Sucht in einem Baum aus TreeNodes den Besten Kandidaten f�r eine Verbindung. Die Wahl wird durch die Implementierung bestimmt
	 * @return Kandidat f�r Verbindung
	 */
	abstract Node getBestNode();

}
