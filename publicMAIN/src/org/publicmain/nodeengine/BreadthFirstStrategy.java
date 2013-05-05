package org.publicmain.nodeengine;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.publicmain.common.Config;
import org.publicmain.common.Node;

/**
 * Klasse für die BreadthFirstStrategy, die Breitensuche.
 * 
 * @author ATRM
 *
 */
public class BreadthFirstStrategy implements BestNodeStrategy {

	public Node getBestNode() {

		Node tmp = NodeEngine.getNE().getTree();
		int level = tmp.getLevel();
		// Cache für die Kandidaten 
		Set<Node> candidates = new HashSet<Node>();
		// Erste Suche
		@SuppressWarnings("unchecked")
		Enumeration<Node> cursor = tmp.breadthFirstEnumeration();

		while (cursor.hasMoreElements()) {
			Node tree_cursor = cursor.nextElement();
			// Neues Level
			if (tree_cursor.getLevel() != level) {
				if (candidates.size() > 0)
					return getMinNode(candidates);
				level = tree_cursor.getLevel();
			}
			// Letzter Node
			if (!cursor.hasMoreElements()) {
				candidates.add(tree_cursor);
				return getMinNode(candidates);
			}
			// Möglicher Kandidat
			if (tree_cursor.getChildCount() < Config.getConfig()
					.getMaxConnections()) {
				candidates.add(tree_cursor);
			}
		}
		return tmp;
	}

	/**
	 * Sucht aus einer Kollektion von Nodes den, mit den wenigsten ChildNodes.
	 * 
	 * @param selection
	 *            die Kollektion von Nodes.
	 * @return Knoten mit den wenigsten Childs oder bei gleich vielen den Ersten.
	 */
	private Node getMinNode(Collection<Node> selection) {
		Node theone = null;
		// Suche den einen mit den wenigsten ChildNodes
		for (Node node : selection) {
			if (theone == null) {
				theone = node;
			} else if (theone.getChildCount() < node.getChildCount()) {
				theone = node;
			}
		}
		return theone;
	}
}
