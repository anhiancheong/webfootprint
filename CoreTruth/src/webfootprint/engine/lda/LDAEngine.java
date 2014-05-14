package webfootprint.engine.lda;

import java.sql.SQLException;
import java.util.*;
import java.io.IOException;

import webfootprint.engine.data.Constants;
import webfootprint.engine.data.Profile;
import webfootprint.engine.data.WebUser;
import webfootprint.engine.data.Truth;
import webfootprint.engine.data.Predict;
import webfootprint.engine.util.db.Database;
import webfootprint.engine.util.db.Schema;
import webfootprint.engine.util.math.Combination;
import webfootprint.engine.util.math.Measure;
import webfootprint.engine.DefaultEngine;
import webfootprint.engine.util.Pair;

public class LDAEngine extends DefaultEngine {

	private int neighborNum;
	private int queryNum;
	private int topicNum;
	private int featureNumLoBound;
	private int featureNumUpBound;
	private Double distanceThresh;
	private String site;
	HashMap<String, TopicModel> models;
	private boolean train;
	
	public LDAEngine(Database database, ArrayList<WebUser> webUsers, ArrayList<String> valuedAttributes,
			ArrayList<String> trainingAttributes, ArrayList<Profile> trainingProfiles) throws SQLException {
		super(database, webUsers, valuedAttributes, trainingAttributes, trainingProfiles);
	}
	
	public void trainConfig(LDAEngnSetting setting) {
		this.neighborNum = setting.getNeighborNum();
		this.queryNum = setting.getQueryNum();
		this.topicNum = setting.getTopicNum();
		this.distanceThresh = setting.getDistanceThresh();
		this.site = setting.getSite();
		this.train = setting.getTrain();
	}
	
	public void train() throws InterruptedException, SQLException, IOException {
		database.createTable(Constants.LDA_MODEL_RELATION, Schema.getLDA(), false);
		for(int i = 0; i < trainingAttributes.size(); i++) {
			String attribute = trainingAttributes.get(i);
			TopicModel model = new TopicModel(attribute, database, site, topicNum);
			model.train(trainingProfiles);
			model.writeIntoDb();
		}
	}
	
	public void inferConfig(LDAEngnSetting setting) {
		this.featureNumLoBound = setting.getFeatureNumLoBound();
		this.featureNumUpBound = setting.getFeatureNumUpBound();
		this.neighborNum = setting.getNeighborNum();
		this.queryNum = setting.getQueryNum();
		this.topicNum = setting.getTopicNum();
		this.distanceThresh = setting.getDistanceThresh();
		this.site = setting.getSite();
		models = new HashMap<String, TopicModel>();
	}
	
	public void infer() throws SQLException, ClassNotFoundException, InterruptedException, IOException {
		for(int num = featureNumLoBound; num <= featureNumUpBound; num++) {
			HashSet<String> attributes = new HashSet<String>(valuedAttributes);
				//if(we)
			HashSet<HashSet<String>> combinations = Combination.combination(attributes, num);
			for(Iterator iterator = combinations.iterator(); iterator.hasNext(); ) {
				HashSet<String> set = (HashSet<String>)iterator.next();
				System.out.print("lda using ");
				for(Iterator iter = set.iterator(); iter.hasNext(); ) {
					System.out.print((String)iter.next() + ", ");
				}
				System.out.println();
				
				innerInfer(set, webUsers);
			}
		}		
	}
	
	public void innerInfer(HashSet<String> attributes, ArrayList<WebUser> users) throws SQLException, ClassNotFoundException, InterruptedException, IOException {
		
		HashMap<String, double[]> trainingVectorMap= new HashMap<String, double[]>();
		HashMap<String, Profile> profileMap = new HashMap<String, Profile>();
		for(int i = 0; i < trainingProfiles.size(); i++) {
			Profile profile = trainingProfiles.get(i);
			String name = profile.getUserId();
			double[] vector = concatenate(attributes, profile, false);
			if(vector != null) {
				trainingVectorMap.put(profile.getUserId(), vector);
				profileMap.put(profile.getUserId(), profile);
			}
		}
		
		PriorityQueue<Pair> queue = new PriorityQueue<Pair>(100, 
				new Comparator<Pair>(){
					public int compare(Pair item1, Pair item2){
						return Double.compare((Double)item1.getSecond(), (Double)item2.getSecond());				
			}
		});
		for(int i = 0; i < users.size(); i++) {
			WebUser user = users.get(i);
			Profile targetProfile = user.getProfile();
			double[] targetVector = concatenate(attributes, targetProfile, this.train);
			if(targetVector == null) {
				continue;
			}
			
			for(Iterator iterator = trainingVectorMap.keySet().iterator(); iterator.hasNext(); ) {
				String name = (String)iterator.next();
				double[] reference = trainingVectorMap.get(name);
				Double distance = Measure.chiSquareDistance(targetVector, reference);
				queue.add(new Pair(name, distance));
			}
			
			Profile groundTruth = user.getGroundTruth();
			int count = neighborNum;
			while(!queue.isEmpty() && count > 0) {
				count--;
				Pair pair = queue.poll();
				String name = (String)pair.getFirst();
				Double distance = (Double)pair.getSecond();
				Profile profile = profileMap.get(name);
				for(int j = 0; j < groundTruth.size(); j++) {
					String targetAttr = groundTruth.getAttribute(j);
					if(profile.containsAttribute(targetAttr)) {
						ArrayList answers = profile.getAttributeValue(targetAttr);
						for(int k = 0; k < answers.size(); k++) {
							Truth truth = (Truth)answers.get(k);		
							Predict predict = user.getInference().addInference(targetAttr, truth.getAnswer(), Constants.LDA);
							predict.getUserData().addUserDatum(Constants.CHI_SQUARE_DISTANCE, distance);
						}
					}
				}
			}
			queue.clear();			
		}
	}
	
	private double[] concatenate(HashSet<String> attributes, Profile profile, boolean train) throws SQLException, ClassNotFoundException, InterruptedException, IOException {
		ArrayList<double[]> vectors = new ArrayList<double[]>();
		int length = 0;
		for(Iterator iterator = attributes.iterator(); iterator.hasNext(); ) {
			String attribute = (String)iterator.next();
			if(!profile.containsAttribute(attribute)) {
				return null;
			}
			if(!models.containsKey(attribute)) {
				TopicModel topicModel = new TopicModel(attribute, database, site, topicNum);
				topicModel.readTopicModel();
				models.put(attribute, topicModel);				
			}
			TopicModel model = models.get(attribute);
			double[] temp = new double[model.getVectorLength()];
			ArrayList array = profile.getAttributeValue(attribute);
			for(int i = 0; i < array.size(); i++) {
				double[] tempVector;
				String answer = ((Truth)array.get(i)).getAnswer();
				if(!train) {				
					if(!model.containsValue(answer)) {
						continue;
					}
					tempVector = model.getVector(answer);
				} else {
					tempVector = model.trainComingProfile(answer);
				}
				for(int j = 0; j < tempVector.length; j++) {
					temp[j] += tempVector[j] / array.size();
				}				
			}
			if(Measure.zeroCheck(temp)) {
				return null;
			}
			
			length += temp.length;
			vectors.add(temp);
		}
		double[] concatenated = new double[length];
		int count = 0;
		for(int i = 0; i < vectors.size(); i++) {
			double[] vector = vectors.get(i);
			for(int j = 0; j < vector.length; j++) {
				concatenated[count] = vector[j];
				count++;
			}
		}
		return concatenated;		
	}
	
}
