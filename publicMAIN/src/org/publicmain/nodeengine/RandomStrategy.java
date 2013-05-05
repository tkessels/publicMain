package org.publicmain.nodeengine;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import org.publicmain.common.Node;

/**
 * @author ATRM
 *  Strategie w�hlt aus allen bekannten Knoten zuf�llig einen aus.
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
	 * @return Liste aller Kindknoten einschlie�lich des Wurzelknotens
	 */
	public static List<Node> returnAllNodes(Node node){
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
}
