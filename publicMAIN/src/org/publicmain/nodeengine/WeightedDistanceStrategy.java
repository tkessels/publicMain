package org.publicmain.nodeengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.publicmain.common.Config;
import org.publicmain.common.Node;

/**
 * @author tkessels
 * Erzeugt ein Scoring für alle Knoten mit justierbaren Parametern und wählt dann den am besten bewerteten Knoten
 *
 */
public class WeightedDistanceStrategy extends BestNodeStrategy {
	public static final int LINEAR =0;
	public static final int QUADRATIC =1;
	private double ratio;
	private int scale_connections;
	private int scale_root_distance;

	/**
	 * Erstellt eine neue dynamische Regel zur Bestimmung von Verbindungskandidaten. 
	 * Diese Strategie erlaubt es das Verhältnis zwischen der Distanz eines Knoten zur Wurzel und der Anzahl von bereits 
	 * bestehenden Verbindungen zur eingestellten Obergrenze in sowohl linear als auch quadratisch zu bewerten und die beiden Teilscore mit einem Ratio zu relativieren.
	 * @param ratio Verhältnis zwischen Verbindungsauschöpfung und Wurzelknotendistanz
	 * @param scale_connections Skala für Verbindungen ist <br> <ul><li>1- Linear <li>2- Quadratisch</ul>
	 * @param scale_root_distance Skala für Wurzeldistanz ist <br> <ul><li>1- Linear <li>2- Quadratisch</ul>
	 */
	public WeightedDistanceStrategy(double ratio,int scale_connections,int scale_root_distance) {
		this.ratio=ratio;
		this.scale_connections =scale_connections;
	}

	/* (non-Javadoc)
	 * @see org.publicmain.nodeengine.BestNodeStrategy#getBestNode()
	 */
	public Node getBestNode() {
		Node root = NodeEngine.getNE().getTree();
		List<Node> allnodes = returnAllNodes(root);
		List<ScoreEntry> scores = new ArrayList<ScoreEntry>();
		int depth=root.getDepth();
		for (Node cur : allnodes) {
			scores.add(new ScoreEntry(cur, getScore(cur, depth)));
		}
		Collections.sort(scores, new Comparator<ScoreEntry>() {
			public int compare(ScoreEntry o1, ScoreEntry o2) {
				return o1.score.compareTo(o2.score);
			}
		});			
		return scores.get(0).item;
	}

	/**Ermittelt den Score eines Knoten nach den Voreingestellten Regeln
	 * @param node der zu bewertende Knoten
	 * @param depth Maximale Baumtiefe
	 * @return Scorewertung des Knoten
	 */
	private double getScore(Node node,int depth) {
		double maxcon =Config.getConfig().getMaxConnections();
		double con = node.getChildCount();
		con = (con>maxcon)?maxcon:con;
		double conval=(scale_connections==LINEAR)?con/maxcon:Math.pow(con, 2)/Math.pow(maxcon,2);
		double rootval=(scale_root_distance==LINEAR)?(node.getLevel()/(double)depth):(Math.pow(node.getLevel(),2)/Math.pow(depth,2));
		return (conval*(1-ratio))+(rootval*ratio);
	}
	
	/**
	 * @author tkessels
	 * DatenObjekt für die Scoringtabelle
	 */
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
