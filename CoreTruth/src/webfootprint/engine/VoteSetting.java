package webfootprint.engine;

import java.util.*;

import webfootprint.engine.data.Constants;

public class VoteSetting {
	
	public int algorithm;
	public String firstStrategy;
	public String secondStrategy;
	public int number;
	public int trend;
	
	public static HashMap<Integer, VoteSetting> defaultSettings;
	
	public VoteSetting(int algorithm, String firstStrategy, String secondStrategy, int number, int trend) {
		this.algorithm = algorithm;
		this.firstStrategy = firstStrategy;
		this.secondStrategy = secondStrategy;
		this.number = number;
		this.trend = trend;
	}
	
	public int getAlgorithm() {
		return this. algorithm;
	}
	
	public String getFirstStrategy() {
		return this.firstStrategy;
	}
	
	public String getSecondStrategy() {
		return this.secondStrategy;
	}
	
	public int getNumber() {
		return this.number;
	}
	
	public int getTrend() {
		return this.trend;
	}
	
	public static HashMap<Integer, VoteSetting> getDefaultSettings() {
		defaultSettings = new HashMap<Integer,VoteSetting>();
		VoteSetting apriori = new VoteSetting(Constants.ASSOCIATION_RULE_MINING, Constants.APRIORI_CONFIDENCE, Constants.APRIORI_CONFIDENCE, 1, Constants.DESCENDING);
		VoteSetting nb = new VoteSetting(Constants.NAIVE_BAYES, Constants.NAIVE_BAYES_PROBABILITY, Constants.NAIVE_BAYES_SELECTIVITY, 1, Constants.DESCENDING);
		VoteSetting lda = new VoteSetting(Constants.LDA, null, Constants.LDA_MAJORITY_VOTE_CONFIDENCE, 1, Constants.DESCENDING);
		VoteSetting ensemble = new VoteSetting(Constants.ENSEMBLE, Constants.ENSEMBLE_MAJORITY_VOTE_CONFIDENCE, Constants.DEFAULT_CONFIDENCE, 1, Constants.DESCENDING);
		defaultSettings.put(Constants.ASSOCIATION_RULE_MINING, apriori);
		defaultSettings.put(Constants.NAIVE_BAYES, nb);
		defaultSettings.put(Constants.LDA, lda);
		defaultSettings.put(Constants.ENSEMBLE, ensemble);
		return defaultSettings;
	}
	
	public static void main(String[] args) {
		
	}
	
}
