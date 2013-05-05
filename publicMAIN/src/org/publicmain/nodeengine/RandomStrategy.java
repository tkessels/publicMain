package org.publicmain.nodeengine;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import org.publicmain.common.Node;

/**
 * @author ATRM
 *  Strategie wählt aus allen bekannten Knoten zufällig einen aus.
 */
public class RandomStrategy implements BestNodeStrategy {
	private Random gen;
	public RandomStrategy(long seed) {
		gen= new Random(seed);
	}

	/* (non-Javadoc)
	 * @see org.publicmain.nodeengine.BestNodeStrategy#getBestNode()
	 */
	public Node getBestNode() {
		List<Node> tmp=new ArrayList<>(NodeEngine.getNE().getNodes());
		return tmp.get(gen.nextInt(tmp.size()));
	}

	/**Ermittelt alle Kindknoten eines Knoten
	 * @param node Wurzelknoten
	 * @return Liste aller Kindknoten einschließlich des Wurzelknotens
	 */
	public static List<Node> returnAllNodes(Node node){
		List<Node> listOfNodes = new ArrayList<Node>();
		addAllNodes(node, listOfNodes);
		return listOfNodes;
	}

	/** Fügt alle Kinder und Kindeskinder mit einer Breitensuche einer Liste von Knoten hinzu.
	 * @param node Wurzel des zu druchlaufenden Baums
	 * @param listOfNodes zu befüllende Liste
	 */
	private static void addAllNodes(Node node, List<Node> listOfNodes) {
		if (node != null) {
			Enumeration<Node> cursor = node.breadthFirstEnumeration();
			while(cursor.hasMoreElements()) {
				listOfNodes.add(cursor.nextElement());
			}
		}
	}
}
