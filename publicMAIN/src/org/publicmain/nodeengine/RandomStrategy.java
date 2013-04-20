package org.publicmain.nodeengine;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import org.publicmain.common.Node;

public class RandomStrategy implements BestNodeStrategy {
	private Random gen;
	public RandomStrategy(long seed) {
		gen= new Random(seed);
	}

	public Node getBestNode(Node root) {
		List<Node> tmp=returnAllNodes(root);
		return tmp.get(gen.nextInt(tmp.size()));
	}
	
	public static List<Node> returnAllNodes(Node node){
		    List<Node> listOfNodes = new ArrayList<Node>();
		    addAllNodes(node, listOfNodes);
		    return listOfNodes;
		}

		private static void addAllNodes(Node node, List<Node> listOfNodes) {
		    if (node != null) {
			    Enumeration<Node> cursor = node.breadthFirstEnumeration();
			    while(cursor.hasMoreElements()) {
				    listOfNodes.add(cursor.nextElement());
			    }
			}
		}

}