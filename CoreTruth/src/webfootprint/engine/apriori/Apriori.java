package webfootprint.engine.apriori;

import java.io.*;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.*;

import webfootprint.engine.data.Profile;
import webfootprint.engine.data.Constants;
import webfootprint.engine.data.Truth;
import webfootprint.engine.exception.AprioriException;
import webfootprint.engine.util.Pair;
import webfootprint.engine.util.Triplet;
import webfootprint.engine.util.db.Database;
import webfootprint.engine.util.db.Schema;

public class Apriori {
	
	int maxFreItemsetLength;
	double support;
	double confidence;
	int maxValNumPerAttr;
	ArrayList<AssociationRule> inferenceRules;
	
	public Apriori(int maxFreItemsetLength, double support, double confidence, int maxValNumPerAttr) {
		this.maxFreItemsetLength = maxFreItemsetLength;
		this.support = support;
		this.confidence = confidence;
		this.maxValNumPerAttr = maxValNumPerAttr;
	}
	
	public int getMaxFreqItemsetLength() {
		return this.maxFreItemsetLength;
	}
	
	public double getSupport() {
		return this.support;
	}
	
	public double getConfidence() {
		return this.confidence;
	}
	
	public ArrayList<AssociationRule> getRules() {
		return this.inferenceRules;
	}

    public ArrayList<AssociationRule> train(ArrayList<Profile> profiles, ArrayList<String> attributes) throws AprioriException {
    	HashMap<String, Integer> tokenToNum = new HashMap<String, Integer>();
        HashMap<Integer, String> numToToken = new HashMap<Integer, String>();
        ArrayList<Integer[]> transactions = mapTokensToNums(profiles, tokenToNum, numToToken);
        
        configure();
        ArrayList<Integer[]> frequentItemsets = generateFrequentItemsets(getMaxFreqItemsetLength(), transactions, tokenToNum, getSupport());
        //stripSubsets(frequentItemsets);
    	ArrayList<String> rules = generateRules(frequentItemsets);
    	HashMap<String, Double> goodRules = testRules(rules, transactions, confidence);        
        inferenceRules = reMap(goodRules, numToToken);
        return inferenceRules;    	
    }
    
	/** sets transaction file, minConf, and minSup */
	private void configure() throws AprioriException {
			
		if (support > 1 || support < 0) {
			throw new AprioriException("minSup: bad value");
		}
			
		if (confidence > 1 || confidence < 0) {
			throw new AprioriException("minConf: bad value");
		}		
	}
    
    private ArrayList<Integer[]> mapTokensToNums(ArrayList<Profile> profiles, HashMap<String, Integer> tokenToNum, HashMap<Integer, String> numToToken) {
    	
    	ArrayList<Integer[]> transactions = new ArrayList<Integer[]>();
    	for(int i = 0; i < profiles.size(); i++) {
    		Profile profile = profiles.get(i);
    		ArrayList<Profile> simplifiedProfiles = profile.singleValWGroupConstraint(this.maxValNumPerAttr);
    		for(int j = 0; j < simplifiedProfiles.size(); j++) {
    			Profile simplifiedProfile = simplifiedProfiles.get(j);
    			
	    		Integer[] transaction = new Integer[simplifiedProfile.size()];
	    		int count = 0;
	    		ArrayList<String> attributes = simplifiedProfile.getAttributes();
	    		for(int k = 0; k < attributes.size(); k++) {
	    			String attribute = attributes.get(k);
	    			Truth truth = (Truth)simplifiedProfile.getAttributeValue(attribute).get(0);
	    			String keyValuePair = attribute + "=" + truth.getAnswer();
	    			
	    			if(!tokenToNum.containsKey(keyValuePair)) {
	    				tokenToNum.put(keyValuePair, tokenToNum.size());
	    				numToToken.put(tokenToNum.size() - 1, keyValuePair);
	    			}    			
	    			transaction[count] = tokenToNum.get(keyValuePair);
	    			count++;
	    		}
	    		transactions.add(transaction);    		
    		}
    	}
    	return transactions;
    } 
    
    private ArrayList<AssociationRule> reMap(HashMap<String, Double> rules, HashMap<Integer, String> numToToken) {
    	ArrayList<AssociationRule> textRules = new ArrayList<AssociationRule>();
    	for(Iterator<String> ruleIterator = rules.keySet().iterator(); ruleIterator.hasNext(); ) {
    		String ruleString = ruleIterator.next();
    		String[] rule = ruleString.split(" ~ ");
    		String[] antecedent = rule[0].split("[ ]+");
    		String[] consequent = rule[1].split("[ ]+");
    		RuleConstituent textAntecedent = new RuleConstituent(Constants.ANTECEDENT);
    		for(int j = 0; j < antecedent.length; j++) {
    			if(!antecedent[j].equals("")) {
    				String[] tokens = numToToken.get(Integer.valueOf(antecedent[j])).split("=");
    				textAntecedent.addItem(tokens[0], tokens[1]);
    			}
    		}
    		if(textAntecedent.size() == 0) {
    			continue;
    		}
    		
    		RuleConstituent textConsequent = new RuleConstituent(Constants.CONSEQUENT);
    		for(int j = 0; j < consequent.length; j++) {
    			if(!consequent[j].equals("")) {
    				String[] tokens = numToToken.get(Integer.valueOf(consequent[j])).split("=");
    				textConsequent.addItem(tokens[0], tokens[1]);
    			}
    		}
    		if(textConsequent.size() == 0) {
    			continue;
    		}
    		Double confidence = rules.get(ruleString);
    		AssociationRule singleRule = new AssociationRule(textAntecedent, textConsequent, confidence);
    		
    		textRules.add(singleRule);
    	}
    	return textRules;
    }
    
    public void stripSubsets(ArrayList<Integer[]> frequentItemsets) {
		
    	for(int i = frequentItemsets.size() - 1; i >= 0; i--) {
    		Integer[] set1 = frequentItemsets.get(i);
    		for(int j = 0; j < frequentItemsets.size(); j++) {
    			Integer[] set2 = frequentItemsets.get(j);
    			if(contains(set1, set2) && set1.length != set2.length) {
    				frequentItemsets.remove(j);
    				if(j < i) {
    					i--;
    				}
    				j--;
    			}
    		}
    	}
	}	
    
    private boolean contains(Integer[] set1, Integer[] set2) {
    	for(int i = 0; i < set2.length; i++) {
    		boolean find = false;
    		for(int j = 0; j < set1.length; j++) {
    			if(set1[j].equals(set2[i])) {
    				find = true;
    			}
    		}
    		if(!find) {
    			return false;
    		}
    	}    	
    	return true;
    }    
   
    private void log(String message) {
    	System.err.println(message);
    }    
    
    private ArrayList<Integer[]> generateFrequentItemsets(Integer maxFrequentItemsetLength, ArrayList<Integer[]> transactions, HashMap<String, Integer> tokenToNum, Double minSupport) {    	
     
    	long startTime = System.currentTimeMillis();
		ArrayList<Integer[]> frequentItemsets = new ArrayList<Integer[]>();
		ArrayList<Integer[]> frequentCandidates = createFrequentItemsetsOfSizeOne(tokenToNum);
		
		ArrayList<Integer[]> frequentItemsetsCurrentIteration = calculateFrequentItemsets(transactions, frequentItemsets, frequentCandidates, tokenToNum, minSupport);
		while (frequentItemsetsCurrentIteration.size() > 1) {					
			
			if (frequentItemsetsCurrentIteration.get(0).length >= maxFrequentItemsetLength) {
				break;
			}
			frequentCandidates = createFrequentCandidatesFromPrevious(frequentItemsetsCurrentIteration);
			//stripSubsets(frequentCandidates);
			if(frequentCandidates.size() == 0) {
				break;
			}
			frequentItemsetsCurrentIteration = calculateFrequentItemsets(transactions, frequentItemsets, frequentCandidates, tokenToNum, minSupport);
		}
	
		long endTime = System.currentTimeMillis();
		System.out.println("Execution time is: "+((double)(endTime-startTime)/1000) + " seconds.");
		log("Found " + frequentItemsets.size() + " frequents sets for support "+ (minSupport * 100) + "% (absolute "+Math.round (transactions.size() * minSupport) + ")");
		log("Done");
		
		return frequentItemsets;
	}
	
	private void line2booleanArray(Integer[] line, boolean[] transaction) {
		Arrays.fill(transaction, false);
		for(int i = 0; i < line.length; i++) {
			transaction[line[i]]= true;
		}	
	}
	
	private ArrayList<Integer[]> createFrequentItemsetsOfSizeOne(HashMap<String, Integer> tokenToNum) {
		ArrayList<Integer[]> itemsets = new ArrayList<Integer[]>();
		for(Iterator<String> itemIterator = tokenToNum.keySet().iterator(); itemIterator.hasNext(); ) {
			Integer item = Integer.valueOf(tokenToNum.get(itemIterator.next()));
			Integer[] candidate = {item};
			itemsets.add(candidate);        	
		}
		return itemsets;
	}    
	
	private ArrayList<Integer[]> calculateFrequentItemsets(ArrayList<Integer[]> transactions, ArrayList<Integer[]> frequentItemsets, ArrayList<Integer[]> frequentCandidates, HashMap<String, Integer> tokenToNum, Double minSupport) {
	
		boolean match; //whether the transaction has all the items in an itemset	
	
		ArrayList<Integer[]> frequentItemsetsCurrentIteration = new ArrayList<Integer[]>();
		double[] count = new double[frequentCandidates.size()];
		boolean[] transaction = new boolean[tokenToNum.size()];
		for (int i = 0; i < transactions.size(); i++) {
			line2booleanArray(transactions.get(i), transaction);
			for(int j = 0; j < frequentCandidates.size(); j++) {
				Integer[] candidate = frequentCandidates.get(j);
				
				match = true; // reset match to false
				for (Integer item : candidate) {
					if (transaction[item] == false) {
						match = false;
						break;
					}
				}
					
				if (match) { // if at this point it is a match, increase the count
					count[j]++;
				}				   
			}
		}
		
		for(int i = 0; i < count.length; i++) {
			if ((count[i] / (double) (transactions.size())) >= minSupport) {
				frequentItemsetsCurrentIteration.add(frequentCandidates.get(i));			
			}
		}
		
		frequentItemsets.addAll(frequentItemsetsCurrentIteration);		
		System.out.println("generate " + frequentItemsetsCurrentIteration.size() + " frequent itemsets of size " + frequentCandidates.get(0).length);
		return frequentItemsetsCurrentIteration;
	}
		
	private ArrayList<Integer[]> createFrequentCandidatesFromPrevious(ArrayList<Integer[]> frequentItemsetsCurrentIteration) {
		
		int currentSizeOfItemsets = frequentItemsetsCurrentIteration.get(0).length;
		ArrayList<Integer[]> candidates = new ArrayList<Integer[]>();
		// compare each pair of itemsets of size n-1
		for(int i = 0; i < frequentItemsetsCurrentIteration.size(); i++) {
			Integer[] X = frequentItemsetsCurrentIteration.get(i);
		    for(int j = i + 1; j < frequentItemsetsCurrentIteration.size(); j++) {
		        Integer[] Y = frequentItemsetsCurrentIteration.get(j);                
		        Integer [] candidate = new Integer[currentSizeOfItemsets + 1];
		        for(int k = 0; k < candidate.length - 1; k++) {
		        	candidate[k] = X[k];
		        }
		        
		        int numDifferent = 0;                
		        // Find the missing value
		        for(int k = 0; k < Y.length; k++) {
		        	boolean found = false;
		        	// is Y[s1] in X?
		            for(int l = 0; l < X.length; l++) {
		            	if (X[l] == Y[k]) { 
		            		found = true;
		            		break;
		            	}
		        	}
		        	if (!found){ // Y[s1] is not in X
		        		numDifferent++;
		        		candidate[candidate.length - 1] = Y[k];
		        	}            	
		    	}
		        
		        if (numDifferent == 1) {
		        	candidates.add(candidate);
		        }
		    }
		}      
		
		System.out.println("generate " + candidates.size() + " frequent itemsets candidates of size " + String.valueOf(frequentItemsetsCurrentIteration.get(0).length + 1));
		return candidates;
	}
    
    private ArrayList<String> generateRules(ArrayList<Integer[]> frequentItemsets) {
    	System.out.println("generating rules...");
    	ArrayList<String> rules = new ArrayList<String>();
    	for(int i = 0; i < frequentItemsets.size(); i++) {
			//This loop selects all but one item from the set to be the antecedent
    		Integer[] itemset = frequentItemsets.get(i);
    		
			for(int j = 0; j < itemset.length; j++) {
				String rule = "";
				for(int l = 0; l < itemset.length; l++) {
					if(l != j) {
						rule += String.valueOf(itemset[l]);
						rule += " ";
					}
				}
				//And these statements add the remaining item as the consequent
				rule += " ~ ";
				rule += String.valueOf(itemset[j]);
				rules.add(rule);
			}
		}
    	System.out.println("Done.");
    	return rules;
    }
    
    //This method compares each rule generated by the frequent item sets against the transaction file
    //to find which rules meet the specified minimum confidence
    private HashMap<String, Double> testRules(ArrayList<String> rules, ArrayList<Integer[]> transactions, Double minConfidence) {
    	
    	System.out.println("Validate rules...");
    	HashMap<String, Double> goodRules = new HashMap<String, Double>();
    	for(int i = 0; i < rules.size(); i++) {
    		//Take each rule and compare it to the transaction file, line by line for matches
    		String rule = rules.get(i);
    					
			double antecedentCount = 0;
	    	double consequentCount = 0;
	    				
			for(int j = 0; j < transactions.size(); j++) {
				
				Integer[] transaction = transactions.get(j);
				StringTokenizer tokenizer = new StringTokenizer(rule, " ");
				while(tokenizer.hasMoreTokens()) {
					String substring = tokenizer.nextToken();
					if(substring.matches("~")) {
						antecedentCount++;
	    				if(contains(transaction, Integer.valueOf(tokenizer.nextToken()))) {
							consequentCount++;
						}
					}else if(contains(transaction, Integer.valueOf(substring))) {
						continue;
					}else {
						break;
					}
				}
			}
			
			if(consequentCount / antecedentCount >= minConfidence) {
				//These statements aggregate the good rule, its confidence, its consequent count, and its antecedent count
				//and outputs them to stdout.  Can be modified for different outputs.				
				//Suggest pushing each good rule to a vector/ArrayList and returning it to PopulationRuleGenerator
				//or alternatively dumping them to an output file
				goodRules.put(rule, consequentCount / antecedentCount);
			}
    	}
    	System.out.println("Done.");
    	return goodRules;
    }
    
    private boolean contains(Integer[] transaction, Integer integer) {
    	boolean contains = false;
    	for(int i = 0; i < transaction.length; i++) {
    		if(transaction[i].equals(integer)) {
    			contains = true;
    		}
    	}
    	return contains;
    }
    
    public static void main(String[] args) {
    	
    }
}

