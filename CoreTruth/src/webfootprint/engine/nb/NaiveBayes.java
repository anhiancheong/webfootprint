package webfootprint.engine.nb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.sql.SQLException;

import webfootprint.engine.data.Constants;
import webfootprint.engine.data.Profile;
import webfootprint.engine.data.Inference;
import webfootprint.engine.data.Truth;
import webfootprint.engine.data.Predict;
import webfootprint.engine.data.WebUser;
import webfootprint.engine.util.Pair;
import webfootprint.engine.util.db.Database;

public class NaiveBayes {
	
	protected Database database;
	protected String site;
	protected Double smoothing;
	protected Double confidence;
	
	public NaiveBayes(Database database, String site, Double smoothing, Double confidence) {
		this.database = database;
		this.site = site;
		this.smoothing = smoothing;
		this.confidence = confidence;
	}	
	
	public void classify(ArrayList<String> posts, WebUser user, String prior, HashMap<String, CondFeature> featureMap) throws SQLException {
		ArrayList<CondFeature> features = new ArrayList<CondFeature>();
		for(int i = 0; i < posts.size(); i++) {
			String post = posts.get(i);
			String key = prior + Constants.CONDITIONAL_PROBABILITY_SEPERATOR + post;
			if(!featureMap.containsKey(key)) {
				CondFeature feature = new CondFeature(prior, post, database, site);
				if(feature.readFromDb()) {
					featureMap.put(key, feature);
				} else {
					return;
				}
			}
			CondFeature feature = featureMap.get(key);
			features.add(feature);
		}
		//Double temp = - Double.MAX_VALUE;
		Pair[] array = new Pair[features.get(0).getPriors().size()];
		//String tempCategory = "";
		for(int i = 0; i < features.get(0).getPriors().size(); i++) {
			String category = features.get(0).getPriors().get(i);
			Double totalProb = 1.0;
			for(int j = 0; j < features.size(); j++) {
				CondFeature feature = features.get(j);
				String postAttr = posts.get(j);
				ArrayList values = user.getProfile().getAttributeValue(postAttr);
				Double condProbAverage = 0.0;
				int count = 0;
				for(int k = 0; k < values.size(); k++) {
					String answer = ((Truth)values.get(k)).getAnswer();
					if(feature.containPost(category, answer)) {
						condProbAverage += feature.getCondProb(category, answer);
						count++;
					}
				}
				if(condProbAverage == 0.0 || count == 0) {
					break;
				}
				totalProb *= condProbAverage / count;
			}
			if(Double.isInfinite(totalProb)) {
				System.exit(1);
			}
			Pair pair = new Pair(totalProb, category);
			array[i] = pair;		
		}
		
		Arrays.sort(array, new Comparator<Pair>() {
			public int compare(Pair pair1, Pair pair2) {
				Double value1 = (Double)pair1.getFirst();
				Double value2 = (Double)pair2.getFirst();
				return value2.compareTo(value1);
			}
		});
		
		Double value0 = (Double)array[0].getFirst();
		Double value1 = (Double)array[1].getFirst();
		Double confidence;
		if(value1.doubleValue() != 0.0) {
			confidence = value0 / value1;
		} else {
			confidence = 5.0;
		}
		
		if(confidence <= this.confidence) {
			return;
		}

		Inference inference = user.getInference();
		Predict predict = inference.addInference(prior, (String)array[0].getSecond(), Constants.NAIVE_BAYES);
		predict.getUserData().addUserDatum(Constants.NAIVE_BAYES_PROBABILITY, value0);
		predict.getUserData().addUserDatum(Constants.NAIVE_BAYES_SELECTIVITY, Double.valueOf(posts.size()));
	}
	
}
