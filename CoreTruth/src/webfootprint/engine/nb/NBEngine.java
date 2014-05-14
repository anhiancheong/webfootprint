package webfootprint.engine.nb;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Set;

import webfootprint.engine.DefaultEngine;
import webfootprint.engine.data.Profile;
import webfootprint.engine.data.WebUser;
import webfootprint.engine.data.Truth;
import webfootprint.engine.util.Pair;
import webfootprint.engine.util.db.Database;
import webfootprint.engine.util.db.Schema;
import webfootprint.engine.util.math.Combination;

public class NBEngine extends DefaultEngine {
	
	public int featureNumUpBound;
	public int featureNumLoBound;
	public Double confidence;
	public String site;
	public Double smoothing;
	private HashMap<String, CondFeature> featureMap;
	private Double pairSizeUpBound;
	private double priorNumToPostNum;
	
	public NBEngine(Database database, ArrayList<WebUser> webUsers, ArrayList<String> valuedAttributes,
			ArrayList<String> trainingAttributes, ArrayList<Profile> trainingProfiles) throws SQLException {
		super(database, webUsers, valuedAttributes, trainingAttributes, trainingProfiles);
	}
	
	public void trainConfig(NBEngnSetting setting) {
		this.site = setting.getSite();
		this.smoothing = setting.getSmoothing();
		this.pairSizeUpBound = setting.getPairSizeUpBound();
		this.priorNumToPostNum = setting.getPriorNumToPostNum();
	}
	
	public void train() throws SQLException {
		
		HashSet<HashSet<String>> combinations =  Combination.combination(new HashSet<String>(trainingAttributes), 2);
		for(Iterator iterator = combinations.iterator(); iterator.hasNext(); ) {
			HashSet set = (HashSet)iterator.next();
			for(Iterator attrIter = set.iterator(); attrIter.hasNext(); ) {
				String first = (String)attrIter.next();
				String second = (String)attrIter.next();
				CondFeature firstFeature = new CondFeature(first, second, database, site, smoothing);
				System.out.println("training " + firstFeature.relation);
				firstFeature.train(trainingProfiles);
				
				CondFeature secondFeature = new CondFeature(second, first, database, site, smoothing);
				System.out.println("training " + secondFeature.relation);
				secondFeature.train(trainingProfiles);
				if(firstFeature.getPriors().size() * secondFeature.getPriors().size() > pairSizeUpBound) {
					continue;
				}
				firstFeature.writeIntoDb(this.priorNumToPostNum);
				secondFeature.writeIntoDb(this.priorNumToPostNum);				
			}
		}		
	}
	
	public void inferConfig(NBEngnSetting setting) {
		this.site = setting.getSite();
		this.featureNumLoBound = setting.getFeatureNumLoBound();
		this.featureNumUpBound = setting.getFeatureNumUpBound();
		this.confidence = setting.getConfidence();
		this.featureMap = new HashMap<String, CondFeature>();
		this.pairSizeUpBound = setting.getPairSizeUpBound();
	}
	
	public void infer() throws SQLException {
		for(int num = featureNumLoBound; num <= featureNumUpBound; num++) {
			HashSet<String> set = new HashSet<String>();
			set.addAll(valuedAttributes);
			if(num > set.size()) {
				continue;
			}
			HashSet<HashSet<String>> combinations = Combination.combination(set, num);
			for(Iterator iterator = combinations.iterator(); iterator.hasNext(); ) {
				HashSet combination = (HashSet)iterator.next();				
				outerInfer(combination);				
			}
		}
	}
	
	private void outerInfer(HashSet<String> combination) throws SQLException {
		NaiveBayes nb = new NaiveBayes(database, site, smoothing, confidence);
		for(int i = 0; i < webUsers.size(); i++) {
			WebUser user = webUsers.get(i);
			if(i % 20 == 0) {
				System.out.println("naive bayes " + i + "users");
			}
			Profile groundTruth = user.getGroundTruth();
			for(int j = 0; j < groundTruth.size(); j++) {
				innerInfer(user, j, combination);				
			}
		}
	}
	
	private void innerInfer(WebUser user, int priorAttr, HashSet<String> combination) throws SQLException {
		Profile profile = user.getProfile();
		Profile groundTruth = user.getGroundTruth();
		ArrayList<String> posts = new ArrayList<String>();
		for(Iterator attrIter = combination.iterator(); attrIter.hasNext(); ) {
			String postAttr = (String)attrIter.next();
			if(!profile.containsAttribute(postAttr)) {
				return;
			}
			posts.add(postAttr);
		}
		String prior = groundTruth.getAttribute(priorAttr);
		NaiveBayes nb = new NaiveBayes(database, site, smoothing, confidence);		
		nb.classify(posts, user, prior, featureMap);
	}
	
}
