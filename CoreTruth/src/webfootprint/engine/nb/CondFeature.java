package webfootprint.engine.nb;

import java.util.*;
import java.sql.SQLException;

import webfootprint.engine.data.Constants;
import webfootprint.engine.data.Profile;
import webfootprint.engine.data.Truth;
import webfootprint.engine.util.Pair;
import webfootprint.engine.util.db.Schema;
import webfootprint.engine.util.db.Database;

public class CondFeature {
	
	protected String priorFeature;
	protected String postFeature;
	protected ArrayList<String> priorList;
	protected HashSet<String> priorSet;
	protected HashMap<String, Double> priorCount;
	protected HashMap<String, HashMap> condCount;
	protected HashMap<String, Double> priorProb;
	protected HashMap<String, HashMap> condProb;
	protected ArrayList<String> postList;
	protected HashSet<String> postSet;
	private Schema schema;
	String relation;
	Database database;
	String site;
	Double smoothing;
	
	public CondFeature(String priorFeature, String postFeature, Database database, String site, Double smoothing) {
		this.priorFeature = priorFeature;
		this.postFeature = postFeature;
		this.priorList = new ArrayList<String>();
		this.priorCount = new HashMap<String, Double>();
		this.condCount = new HashMap<String, HashMap>();
		this.condProb = new HashMap<String, HashMap>();
		this.priorProb = new HashMap<String, Double>();
		this.postSet = new HashSet<String>();
		this.priorSet = new HashSet<String>();
		this.postList = new ArrayList<String>();
		schema = new Schema();
		schema.addColumn("name", java.sql.Types.VARCHAR);
		schema.addColumn("prob", java.sql.Types.REAL);
		relation = Constants.NAIVE_BAYES_RELATION_PREFIX + site + "_" +  priorFeature + "SEPERATOR" + postFeature;
		this.database = database;
		this.site = site;
		this.smoothing = smoothing;
	}
	
	public CondFeature(String priorFeature, String postFeature, Database database, String site) {
		this(priorFeature, postFeature, database, site, null);
	}
	
	public String getPriorFeature() {
		return this.priorFeature;
	}
	
	public String getPostFeature() {
		return this.postFeature;
	}
	
	public ArrayList<String> getPriors() {
		return this.priorList;
	}
	
	public ArrayList<String> getPosts() {
		return this.postList;
	}
	
	public Double getPriorCount(String prior) {
		return priorCount.get(prior);
	}
	
	public HashMap getCondCount(String prior) {
		return condCount.get(prior);
	}
	
	public HashMap getCondProb(String prior) {
		return condProb.get(prior);
	}
	
	public Double getCondProb(String prior, String post) {
		HashMap postMap = getCondProb(prior);
		return ((ProbPair)postMap.get(post)).getProb();
	}
	
	public boolean containPost(String prior, String post) {
		HashMap postMap = getCondProb(prior);
		return postMap.containsKey(post);
	}
	
	public boolean addPrior(String prior) {
		if(!priorSet.contains(prior)) {
			priorList.add(prior);
			priorSet.add(prior);
			priorCount.put(prior, 0.0);
			condCount.put(prior, new HashMap<String, Double>());
			condProb.put(prior, new HashMap<String, Double>());
			return true;
		} else {
			return false;
		}
	}
	
	public void addPriorCount(String prior, Double count) {
		addPrior(prior);
		Double prevCount = priorCount.get(prior);
		priorCount.put(prior, prevCount + count);
	}
	
	public void addCondCount(String prior, String post, Double count) {
		addPrior(prior);
		HashMap priorMap = condCount.get(prior);
		if(!priorMap.containsKey(post)) {
			priorMap.put(post, 0.0);
			addPost(post);			
		}
		Double prevCount = (Double)priorMap.get(post);
		priorMap.put(post, prevCount + count);
	}
	
	private boolean addPost(String post) {
		if(!postSet.contains(post)) {
			postList.add(post);
			postSet.add(post);
			return true;
		} else {
			return false;
		}
	}
	
	public void calculateProb() {
		Double totalPriorCount = 0.0;
		for(int i = 0; i < priorList.size(); i++) {
			String prior = priorList.get(i);
			totalPriorCount += priorCount.get(prior);
		}
		
		for(int i = 0; i < priorList.size(); i++) {
			String prior = priorList.get(i);
			priorProb.put(prior, priorCount.get(prior) / totalPriorCount);
			HashMap postCount = condCount.get(prior);
			HashMap postProb = condProb.get(prior);
			for(Iterator iterator = postCount.keySet().iterator(); iterator.hasNext(); ) {
				String post = (String)iterator.next();
				Double prob = (Double)postCount.get(post) / priorCount.get(prior) * (1.0 - smoothing);
				ProbPair pair = new ProbPair(prob, true);
				postProb.put(post, pair);
			}
		}
	}
	
	public void train(ArrayList<Profile> trainingProfiles) { 
		for(int i = 0; i < trainingProfiles.size(); i++) {
			Profile profile = trainingProfiles.get(i);
			ArrayList array = profile.getAttributeValue(priorFeature);
			if(array == null) {
				continue;
			}
			for(int j = 0; j < array.size(); j++) {
				Truth truth = (Truth)array.get(j);
				String answer = truth.getAnswer();
				addPriorCount(answer, 1.0 / array.size());
			}
			
			ArrayList firstArray = profile.getAttributeValue(priorFeature);
			for(int j = 0; j < firstArray.size(); j++) {
				Truth firstTruth = (Truth)firstArray.get(j);
				String firstAnswer = firstTruth.getAnswer();
				ArrayList secondArray = profile.getAttributeValue(postFeature);
				if(secondArray == null) {
					continue;
				}
				for(int k = 0; k < secondArray.size(); k++) {
					Truth secondTruth = (Truth)secondArray.get(k);
					String secondAnswer = secondTruth.getAnswer();
					addCondCount(firstAnswer, secondAnswer, 1.0 / (firstArray.size() * secondArray.size()));
				}
			}
		}
		calculateProb();
		test();
	}
	
	private void test() {
		for(Iterator iterator = condCount.keySet().iterator(); iterator.hasNext(); ) {
			String key = (String)iterator.next();
			HashMap map = condCount.get(key);
			for(Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
				String feature = (String)iter.next();
				Double count = (Double)map.get(feature);
				double value = count.doubleValue();
				if(!(count < Double.MAX_VALUE)) {
					System.out.println();
				}
			}
			
		}
	}
	
	public void writeIntoDb(double priorNumToPostNum) throws SQLException {
		double priorSize = (double)this.priorList.size();
		double postSize = (double)this.postList.size();
		if(priorSize / postSize > priorNumToPostNum) {
			return;
		}
		database.setActiveRelation(relation);
		database.createTable(relation, schema, true);
		for(Iterator iterator = priorProb.keySet().iterator(); iterator.hasNext(); ) {
			String prior = (String)iterator.next();
			Double prob = (Double)priorProb.get(prior);
			ArrayList<String> values = new ArrayList<String>();
			values.add(prior);
			values.add(String.valueOf(prob));
			database.insert(relation, values);			
		}
		
		for(Iterator priorIter = condProb.keySet().iterator(); priorIter.hasNext(); ) {
			String prior = (String)priorIter.next();
			HashMap postMap = condProb.get(prior);
			for(Iterator postIter = postMap.keySet().iterator(); postIter.hasNext(); ) {
				String post = (String)postIter.next();
				Double prob = ((ProbPair)postMap.get(post)).getProb();
				ArrayList<String> values = new ArrayList<String>();
				values.add(prior + Constants.CONDITIONAL_PROBABILITY_SEPERATOR + post);
				values.add(String.valueOf(prob));
				database.insert(relation, values);
			}
			
		}
	}
	
	public boolean readFromDb() throws SQLException {
		database.setActiveRelation(relation);
		ArrayList<ArrayList> values;
		try{
			values = database.select(schema);
		} catch(SQLException e) {
			return false;
		}
		for(int i = 0; i < values.size(); i++) {
			ArrayList row = values.get(i);
			String name = (String)row.get(0);
			Double prob = (Double)row.get(1);
			if(name.indexOf(Constants.CONDITIONAL_PROBABILITY_SEPERATOR) < 0) {
				addPrior(name);
				priorProb.put(name, prob);
			} else {
				String[] names = name.split(Constants.CONDITIONAL_PROBABILITY_SEPERATOR);
				String prior = names[0];
				String post = names[1];
				addPost(post);
				if(!condProb.containsKey(prior)) {
					condProb.put(prior, new HashMap<String, ProbPair>());
				}
				HashMap<String, ProbPair> postMap = condProb.get(prior);
				postMap.put(post, new ProbPair(prob, true));
			}
		}
		
		for(int i = 0; i < priorList.size(); i++) {
			String prior = priorList.get(i);
			HashMap<String, ProbPair> postMap = condProb.get(prior);
			Double sum = accumulate(postMap);
			Double smoothing;
			if(postMap.size() != 0) {
				smoothing = (1.0 - sum) / postMap.size();
			} else {
				smoothing = 1.0 / postList.size();
			}
			for(int j = 0; j < postList.size(); j++) {
				String post = postList.get(j);
				if(!postMap.containsKey(post)) {
					postMap.put(post, new ProbPair(smoothing, false));
				}
			}
		}
		testDB();
		return true;
	}
	
	private void testDB() {
		for(Iterator iterator = condProb.keySet().iterator(); iterator.hasNext(); ) {
			String key = (String)iterator.next();
			HashMap map = condProb.get(key);
			for(Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
				String feature = (String)iter.next();
				ProbPair pair = (ProbPair)map.get(feature);
				Double value = pair.getProb();
				if(Double.isInfinite(value.doubleValue())) {
					System.out.println(key + " : " + feature);
					System.exit(1);
				}
			}
			
		}
	}
	
	private static Double accumulate(HashMap map) {
		Double sum = 0.0;
		for(Iterator iterator = map.keySet().iterator(); iterator.hasNext(); ) {
			String key = (String)iterator.next();
			sum += ((ProbPair)map.get(key)).getProb();
		}
		return sum;
	}

}

