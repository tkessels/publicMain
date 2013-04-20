package org.publicmain.nodeengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import org.publicmain.common.Config;
import org.publicmain.common.Node;

public class WeightedDistanceStrategy implements BestNodeStrategy {
	public static final int LINEAR =0;
	public static final int QUADRATIC =1;
	private double ratio;
	private int typ;

	public WeightedDistanceStrategy(double ratio,int typ) {
		this.ratio=ratio;
		this.typ =typ;
	}

	public Node getBestNode(Node root) {
		List<Node> allnodes = returnAllNodes(root);
		List<ScoreEntry> scores = new ArrayList<ScoreEntry>();
		int depth=root.getDepth();
		for (Node cur : allnodes) {
			scores.add(new ScoreEntry(cur, getScore(cur, depth)));
		}
		Collections.sort(scores, new Comparator<ScoreEntry>() {
			@Override
			public int compare(ScoreEntry o1, ScoreEntry o2) {
				return o1.score.compareTo(o2.score);
		
			}
		});			
		System.out.println(scores);

		
		return scores.get(0).item;
	}
	
	private double getScore(Node node,int depth) {
		double maxcon =Config.getConfig().getMaxConnections();
		double con = node.getChildCount();
		con = (con>maxcon)?maxcon:con;
		double conval=(typ==0)?con/maxcon:Math.pow(con, 2)/Math.pow(maxcon,2);
		double rootval=(typ==0)?(node.getLevel()/(double)depth):(Math.pow(node.getLevel(),2)/Math.pow((double)depth,2));
		return (conval*(1-ratio))+(rootval*ratio);
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
	
	class ScoreEntry{
		Node item;
		Double score;
		public ScoreEntry(Node item, double score) {
			this.item=item;
			this.score=score;
		}
		@Override
		public String toString() {
			return item.toString() + "["+ score +"]";
		}
	}

}
