package webfootprint.engine.nb;

import webfootprint.engine.data.Constants;
import webfootprint.engine.util.Pair;

public class ProbPair {
	
	private Pair pair;
	
	public ProbPair(Double prob, boolean exist) {
		int type = Constants.NAIVE_BAYES_EXIST;
		if(!exist) {
			type = Constants.NAIVE_BAYES_SMOOTHING;
		}
		this.pair = new Pair(prob, new Integer(type));
	}
	
	public Double getProb() {
		return (Double)pair.getFirst();
	}
	
	public boolean isSmoothing() {
		int type = ((Integer)pair.getSecond()).intValue();
		if(type == Constants.NAIVE_BAYES_SMOOTHING) {
			return true;
		} else {
			return false;
		}
	}
}
