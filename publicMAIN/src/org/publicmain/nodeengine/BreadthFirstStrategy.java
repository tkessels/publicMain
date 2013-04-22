package org.publicmain.nodeengine;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.publicmain.common.Config;
import org.publicmain.common.Node;


public class BreadthFirstStrategy implements BestNodeStrategy {

	public Node getBestNode() {

		Node tmp = NodeEngine.getNE().getTree();
		int level = tmp.getLevel();

		//cache for candidates
		Set<Node> candidates = new HashSet<Node>();

		//breadth first search
		Enumeration<Node> cursor = tmp.breadthFirstEnumeration();

		while (cursor.hasMoreElements()) {
			Node tree_cursor = cursor.nextElement();

			//new Level
			if (tree_cursor.getLevel() != level) {
				if (candidates.size() > 0) {
					return getMinNode(candidates);
				}
				level = tree_cursor.getLevel();
			}
			
			//last node
			if (!cursor.hasMoreElements()) {
				candidates.add(tree_cursor);
				return getMinNode(candidates);
			}
			//possible candidate
			if (tree_cursor.getChildCount() < Config.getConfig().getMaxConnections()){
				candidates.add(tree_cursor);
			}
		}
		return tmp;
	}

	private Node getMinNode(Collection<Node> selection) {
		Node theone = null;
		//pick the one with the least amount of childs
		for (Node node : selection) {
			if (theone == null)
				theone = node;
			else if (theone.getChildCount() < node.getChildCount())
				theone = node;
		}
		return theone;

	}
}
