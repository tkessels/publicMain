package org.publicmain.nodeengine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.publicmain.common.Node;

/**
 * @author ATRM
 *  Strategie wählt aus allen bekannten Knoten zufällig einen aus.
 */
public class RandomStrategy extends BestNodeStrategy {
	private Random gen;
	public RandomStrategy(long seed) {
		gen= new Random(seed);
	}

	public Node getBestNode() {
		List<Node> tmp=new ArrayList<>(NodeEngine.getNE().getNodes());
		return tmp.get(gen.nextInt(tmp.size()));
	}
}
