package webfootprint.engine.data;

import java.util.*;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import webfootprint.engine.util.Pair;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonElement;

import webfootprint.engine.VoteSetting;

public class Inference extends DefaultRecord {
	
	public static void main(String[] argv) {
		Inference inference = new Inference("linkedin", "adfadsf");
		Gson gson = new Gson();
    	Pair pair = new Pair("generation", inference);
    	String gsonString = gson.toJson(pair);
    	Pair pair2 = gson.fromJson(gsonString, Pair.class);
    	JsonElement element = gson.toJsonTree(gsonString, Pair.class);
    	System.out.println(gsonString);
	}
	
	public Inference(String site, String userId, HashMap<String, ArrayList> inferenceMap, ArrayList<String> inferenceList) {
		super(site, userId, inferenceMap, inferenceList);
	}
	
	public Inference(String site, String userId) {
		this(site, userId, new HashMap<String, ArrayList>(), new ArrayList<String>());
	}
	
	public Inference(String userId) {
		this(null, userId);
	}
	
	public Predict addInference(String attribute, String value, int algorithm) {
		Predict predict = new Predict(value, algorithm);
		super.addAttribute(attribute, predict);		
		return predict;
	}
	
	public boolean removeInference(String attribute) {
		return super.removeAttribute(attribute);
	}
	
	public boolean containsInference(String attribute) {
		return super.containsAttribute(attribute);
	}
	
	public ArrayList<String> getInferences() {
		return super.getAttributes();
	}
	
	public ArrayList getInferenceValue(String attribute) {
		return super.getAttributeValue(attribute);
	}
	
	public void vote(String attribute, int algorithm, VoteSetting setting) {
		ArrayList predicts = super.getAttributeValue(attribute);
		ArrayList array = new ArrayList<Predict>();
		if(algorithm == Constants.LDA || algorithm == Constants.NAIVE_BAYES || algorithm == Constants.ASSOCIATION_RULE_MINING) {
			for(int i = 0; i < predicts.size(); i++) {
				Predict predict = (Predict)predicts.get(i);
				if(predict.getAlgorithm() == algorithm) {
					array.add(predict);
					predicts.remove(i);
					i--;
				}
			}
			if(array.size() == 0) {
				return;
			}
		} else if(algorithm == Constants.ENSEMBLE) {
			for(int i = 0; i < predicts.size(); i++) {
				Predict predict = (Predict)predicts.get(i);
				array.add(predict);
				predicts.remove(i);
				i--;				
			}
			if(array.size() == 0) {
				return;
			}
		} else {
			return;
		}
		String firstStrategy = setting.getFirstStrategy();
		String secondStrategy = setting.getSecondStrategy();
		int number = setting.getNumber();
		int trend = setting.getTrend();
		if(algorithm == Constants.ASSOCIATION_RULE_MINING) {
			ArrayList returned = confidenceRank(array, firstStrategy, secondStrategy, number, trend);
			this.addAttribute(attribute, returned);
		} else if(algorithm == Constants.NAIVE_BAYES) {
			ArrayList votes = majorityVote(array, null, Constants.NAIVE_BAYES_MAJORITY_CONFIDENCE, false);
			ArrayList returned = confidenceRank(array, firstStrategy, secondStrategy, number, trend);
			this.addAttribute(attribute, returned);
		} else if(algorithm == Constants.LDA) {
			ArrayList votes = majorityVote(array, null, Constants.LDA_MAJORITY_VOTE_CONFIDENCE, true);
			ArrayList returned = confidenceRank(votes, secondStrategy, secondStrategy, number, trend);		
			this.addAttribute(attribute, returned);
		} else if(algorithm == Constants.ENSEMBLE){
			ArrayList votes = majorityVote(array, null, Constants.ENSEMBLE_MAJORITY_VOTE_CONFIDENCE, false);
			HashSet<String> confidences = new HashSet<String>();
			confidences.add(Constants.APRIORI_CONFIDENCE);
			confidences.add(Constants.NAIVE_BAYES_MAJORITY_CONFIDENCE);
			confidences.add(Constants.LDA_MAJORITY_VOTE_CONFIDENCE);
			defaultConfidence(votes, confidences, Constants.DEFAULT_CONFIDENCE);
			ArrayList returned = confidenceRank(votes, firstStrategy, secondStrategy, number, trend);
			this.addAttribute(attribute, returned);
		}
	}
	
	private void defaultConfidence(ArrayList votes, HashSet<String> confidences, String defaultConfidence) {
		for(int i = 0; i < votes.size(); i++) {
			Predict predict = (Predict)votes.get(i);
			for(Iterator iter = confidences.iterator(); iter.hasNext(); ) {
				String key = (String)iter.next();
				if(predict.getUserData().containsUserDatumKey(key)) {
					Double confidence = (Double)predict.getUserData().getUserDatum(key);
					predict.getUserData().addUserDatum(defaultConfidence, confidence);
				}
			}
		}
	}
	
	private ArrayList confidenceRank(ArrayList predicts, String firstStrategy, String secondStrategy, int number, int trend) {
		Object[] temp = predicts.toArray();
		Predict[] arrays = Arrays.copyOf(temp, temp.length, Predict[].class);
		if(trend == Constants.DESCENDING) {
			Arrays.sort(arrays, new Descending<Predict>(firstStrategy, secondStrategy));
		} else {
			Arrays.sort(arrays, new Ascending<Predict>(firstStrategy, secondStrategy));
		}
		ArrayList returned = new ArrayList();
		for(int i = 0; i < number && i < arrays.length; i++) {
			returned.add(arrays[i]);
		}
		return returned;
	}
	
	private ArrayList majorityVote(ArrayList array, String voteWeight, String voteName, boolean replace) {
		HashMap<String, Double> votes = new HashMap<String, Double>();
		for(int i = 0; i < array.size(); i++) {
			Predict predict = (Predict)array.get(i);
			String answer = predict.getAnswer();
			Double weight = 1.0;
			if(voteWeight != null) {
				weight = (Double)predict.getUserData().getUserDatum(voteWeight);
			}
			if(votes.containsKey(answer)) {
				votes.put(answer, votes.get(answer) + weight);
			} else {
				votes.put(answer, weight);
			}
		}
		
		Double sum = 0.0;
		for(Iterator iterator = votes.keySet().iterator(); iterator.hasNext(); ) {
			String answer = (String)iterator.next();
			sum += votes.get(answer);
		}
		if(replace) {
			ArrayList predicts = new ArrayList();
			for(Iterator iterator = votes.keySet().iterator(); iterator.hasNext(); ) {
				String answer = (String)iterator.next();
				Double vote = (Double)votes.get(answer);
				Predict predict = new Predict(answer, ((Predict)array.get(0)).getAlgorithm());
				predict.getUserData().addUserDatum(voteName, vote / sum);
				predicts.add(predict);
			}	
			return predicts;
		} else {
			for(int i = 0; i < array.size(); i++) {
				Predict predict = (Predict)array.get(i);
				String answer = predict.getAnswer();
				Double vote = (Double)votes.get(answer);
				predict.getUserData().addUserDatum(voteName, vote / sum);
			}
			return array;
		}
	}
	
	private class Ascending<T> implements Comparator<T> {
		
		String firstStrategy;
		String secondStrategy;
		
		public Ascending(String firstStrategy, String secondStrategy) {
			this.firstStrategy = firstStrategy;
			this.secondStrategy = secondStrategy;
		}
		
		public int compare(T predict1, T predict2) {
			Double value1 = (Double)((Predict)predict1).getUserData().getUserDatum(firstStrategy);
			Double value2 = (Double)((Predict)predict2).getUserData().getUserDatum(firstStrategy);
			if(value1.compareTo(value2) != 0) {
				return value1.compareTo(value2);		
			} else {
				value1 = (Double)((Predict)predict1).getUserData().getUserDatum(secondStrategy);
				value2 = (Double)((Predict)predict2).getUserData().getUserDatum(secondStrategy);
				return value1.compareTo(value2);
			}
		}
	}
	
	private class Descending<T> implements Comparator<T> {
		
		String firstStrategy;
		String secondStrategy;
		
		public Descending(String firstStrategy, String secondStrategy) {
			this.firstStrategy = firstStrategy;
			this.secondStrategy = secondStrategy;
		}
		
		public int compare(T predict1, T predict2) {
			Double value1 = (Double)((Predict)predict1).getUserData().getUserDatum(firstStrategy);
			Double value2 = (Double)((Predict)predict2).getUserData().getUserDatum(firstStrategy);
			if(value1.compareTo(value2) != 0) {
				return value2.compareTo(value1);
			} else {
				value1 = (Double)((Predict)predict1).getUserData().getUserDatum(secondStrategy);
				value2 = (Double)((Predict)predict2).getUserData().getUserDatum(secondStrategy);
				return value2.compareTo(value1);
			}
		}
	}
	
	
}
