package webfootprint.engine;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import webfootprint.engine.apriori.AprioriEngine;
import webfootprint.engine.apriori.AprioriEngnSetting;
import webfootprint.engine.data.Constants;
import webfootprint.engine.data.DefaultRecord;
import webfootprint.engine.data.Inference;
import webfootprint.engine.data.Predict;
import webfootprint.engine.data.Profile;
import webfootprint.engine.data.Record;
import webfootprint.engine.data.Truth;
import webfootprint.engine.data.Tuple;
import webfootprint.engine.data.WebUser;
import webfootprint.engine.nb.NBEngine;
import webfootprint.engine.nb.NBEngnSetting;
import webfootprint.engine.util.db.Database;
import webfootprint.engine.util.db.Schema;

public class Evaluation {
	
	public Database database;
	protected String userId;
	protected String attrName;
	protected String attrGroup;
	protected String attrValue;
	protected String relation;
	protected Schema schema;
	protected String trainingSite;
	protected String targetSite;
	
	public Evaluation(Database database, String trainingSite,String targetSite, String relation, Schema schema, String userId, String attrName, String attrGroup, String attrValue) {
		this.database = database;
		this.userId = userId;
		this.attrName = attrName;
		this.attrGroup = attrGroup;
		this.attrValue = attrValue;
		this.relation = relation;
		this.schema = schema;
		this.trainingSite = trainingSite;
		this.targetSite = targetSite;
	}
	
	public ArrayList<String> intersection(ArrayList<String> set1, ArrayList<String> set2) throws SQLException {
		ArrayList<String> intersection = new ArrayList<String>();
		for(int i = 0; i < set1.size(); i++) {
			String element = set1.get(i);
			if(set2.contains(element)) {
				intersection.add(element);
			}
		}
		return intersection;
	}
	
	public ArrayList<String> complement(ArrayList<String> universal, ArrayList<String> original) {
		ArrayList<String> complement = new ArrayList<String>();
		for(int i = 0; i < universal.size(); i++) {
			String element = universal.get(i);
			if(!original.contains(element)) {
				complement.add(element);
			}
		}
		return complement;
	}
	
	public ArrayList<String> getTrainngAttributes() throws SQLException {
		String attributeRelation = trainingSite + "_" + Constants.TRAINING_ATTRIBUTES_RELATION;
		database.setActiveRelation(attributeRelation);
		Schema targetSchema = new Schema();
		targetSchema.addColumn("attribute", java.sql.Types.VARCHAR);
		ArrayList<ArrayList> values = database.select(targetSchema);
		ArrayList<String> trainingAttributes = new ArrayList<String>();
		for(int i = 0; i < values.size(); i++) {
			ArrayList array = values.get(i);
			String value = (String)array.get(0);
			trainingAttributes.add(value);
		}
		return trainingAttributes;
	}
	
	public ArrayList<WebUser> getWebUsers(ArrayList<String> trainingAttributes, ArrayList<String> relyingAttributes, int userNumber) 
			throws SQLException {		
		ArrayList<String> intersection = intersection(trainingAttributes, relyingAttributes);
		ArrayList<WebUser> webUsers = new ArrayList<WebUser>();
		database.setActiveRelation(relation);
		
		String query = "select ";
		for(int i = 0; i < schema.size(); i++) {
			query += schema.getColumn(i) + ", ";
		}
		query = query.substring(0, query.length() - 2);
		query += " from "  + database.getActiveRelation() + " where (";
		
		for(int i = 0; i < intersection.size(); i++) {
			query += attrName + "=\'" + intersection.get(i) + "\' or ";
		}
		query = query.substring(0, query.length() - 3);
		query += " );";
		ArrayList<ArrayList> values = database.constraintSelect(query, schema);
	
		HashMap<String, WebUser> userMap = new HashMap<String, WebUser>();	
		for(int j = 0; j < values.size(); j++) {
			ArrayList array = values.get(j);
			String userId = array.get(schema.getIndex(this.userId)).toString().trim();
			if(!userMap.containsKey(userId)) {
				Profile profile = new Profile(targetSite, userId);
				Profile groundTruth = new Profile(targetSite, userId);
				WebUser user = new WebUser(groundTruth, profile);
				userMap.put(userId, user);
			}
			
			WebUser currentUser = userMap.get(userId);
			
			String value = ((String)array.get(schema.getIndex(this.attrValue))).trim();
			if(!valueValidate(value)) {
				continue;
			}
			String attributeName = array.get(schema.getIndex(this.attrName)).toString().trim();
			if(intersection.contains(attributeName)) {
				
				currentUser.getProfile().addProfile(array.get(schema.getIndex(this.attrName)).toString().trim(), array.get(schema.getIndex(this.attrValue)).toString().trim(), 
						array.get(schema.getIndex(this.attrGroup)).toString().trim());	
			}
		}
		
		for(Iterator iterator = userMap.keySet().iterator(); iterator.hasNext() && userNumber > 0; ) {
			String key = (String)iterator.next();
			userNumber--;
			WebUser user = userMap.get(key);
			ArrayList<String> profileAttributes = user.getProfile().getAttributes();
			ArrayList<String> complement = complement(trainingAttributes, profileAttributes);
			for(int i = 0; i < complement.size(); i++) {
				String attribute = complement.get(i);
				Record record = user.getGroundTruth();  
				record.addAttribute(attribute);
			}
			webUsers.add(userMap.get(key));
		}
		return webUsers;
	}

	private boolean valueValidate(String value) {
		if(value.length() > Constants.VALUE_LENGTH_UP_BOUND) {
			return false;
		} else if(value.matches("[\\s\\W]*")) {
			return false;
		} else {
			return true;
		}
	}
	
	public void predictRecommending(int[] engine, HashMap<Integer, VoteSetting> settings, ArrayList<WebUser> webUsers) {
		for(int i = 0; i < webUsers.size(); i++) {
			WebUser user = webUsers.get(i);
			Inference inference = user.getInference();
			ArrayList<String> attributes = inference.getAttributes();
			for(int j = 0; j < attributes.size(); j++) {
				String attribute = attributes.get(j);
				//System.out.println(user.getProfile().getAttributes().get(0).);
				for(int k = 0; k < engine.length; k++) {
					if(settings.containsKey(engine[k])) {
						VoteSetting setting = settings.get(engine[k]);
						inference.vote(attribute, setting.getAlgorithm(), setting);
					}
				}
				VoteSetting setting = settings.get(Constants.ENSEMBLE);
				inference.vote(attribute, setting.getAlgorithm(), setting);
			}		
		}
	}
	
	public void accuracy(ArrayList<WebUser> users) {
		
		Double predict = 0.0;
		
		for(int i = 0; i < users.size(); i++) {
			Inference inference = users.get(i).getInference();
			ArrayList<String> inferences = inference.getAttributes();
			for(int j = 0; j < inferences.size(); j++) {
				String attribute = inferences.get(j);
				predict += inference.getInferenceValue(attribute).size();
			}
		}
		System.out.println("predictions: " + predict);

	}
	
	private static boolean contains(ArrayList array1, ArrayList array2) {
		for(int i = 0; i < array1.size(); i++) {
			Tuple tuple1 = (Tuple)array1.get(i);
			String answer1 = (String) tuple1.getObject();
			for(int j = 0; j < array2.size(); j++) {
				Tuple tuple2 = (Tuple)array2.get(j);
				String answer2 = (String) tuple2.getObject();
				if(answer1.equals(answer2)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void writePredictIntoDb(ArrayList<WebUser> users, Schema schema, String profile) throws SQLException {
		database.createTable(profile, schema, false);
		for(int i = 0; i < users.size(); i++) {
			
			Inference inference = users.get(i).getInference();
			ArrayList<String> attributes = inference.getAttributes();
			for(int j = 0; j < attributes.size(); j++) {
				String attribute = attributes.get(j);
				ArrayList predicts = inference.getAttributeValue(attribute);
				HashMap<String, Integer> counts = new HashMap<String, Integer>();
				for(int k = 0; k < predicts.size(); k++) {
					Predict predict = (Predict)predicts.get(k);
					int engine = predict.getAlgorithm();
					String algorithm;
					Double confidence;
					switch(engine) {
					case Constants.ASSOCIATION_RULE_MINING:
						algorithm = "association_rule";
						confidence = (Double)predict.getUserData().getUserDatum("apriori_confidence");
						break;
					case Constants.NAIVE_BAYES:
						algorithm = "naive_bayes";
						confidence = (Double)predict.getUserData().getUserDatum("naive_bayes_majority_confidence");
						break;
					case Constants.LDA:
					default:
						algorithm = "lda";
						confidence = (Double)predict.getUserData().getUserDatum("lda_majority_vote_confidence");
					}
					String answer = predict.getAnswer();
					if(!counts.containsKey(algorithm)) {
						counts.put(algorithm, 1);
					} else {
						Integer count = counts.get(algorithm);
						counts.put(algorithm, count + 1);
					}
					ArrayList<String> insert = new ArrayList<String>();
					insert.add(inference.getUserId());
					insert.add(attribute);
					insert.add(String.valueOf(counts.get(algorithm)));
					insert.add(answer);
					insert.add(String.valueOf(confidence));
					insert.add(algorithm);
					database.insert(profile, insert);
					
				}
			}			
		}
	}
	
	public void printPredict(ArrayList<WebUser> users) {
		for(int i = 0; i < users.size(); i++) {
			WebUser user = users.get(i);
			System.out.println("USER: " + user.getProfile().getUserId());
			Inference inference = user.getInference();
			for(int j = 0; j < inference.size(); j++) {
				String attribute = inference.getAttribute(j);
				System.out.println("----ATTRIBUTE: " + attribute);
				ArrayList answers = inference.getAttributeValue(attribute);
				printAttributePredcits(answers);
			}
		}
	}
	
	private void printAttributePredcits(ArrayList array) {
		for(int i = 0; i < array.size(); i++) {
			Predict predict = (Predict)array.get(i);
			String algorithm;
			switch(predict.getAlgorithm()) {
			case(Constants.ASSOCIATION_RULE_MINING):
				algorithm = "APRIORI";
				break;
			case(Constants.NAIVE_BAYES):
				algorithm = "NAIVE_BAYES";
				break;
			case(Constants.LDA):
			default:
				algorithm = "LDA";					
			}
			System.out.print("--------" + algorithm + " ANSWER: " + predict.getAnswer());
			for(Iterator iterator = predict.getUserData().getUserDatumKeyIterator(); iterator.hasNext(); ) {
				String key = (String) iterator.next();
				Double value = (Double)predict.getUserData().getUserDatum(key);
				System.out.print(", " + key.toUpperCase() + ": " + value);
			}
			System.out.println();
		}
	}

}
