package webfootprint.engine;

import java.io.*;
import java.sql.*;
import java.util.*;

import webfootprint.engine.util.db.Database;
import webfootprint.engine.util.db.Schema;
import webfootprint.engine.data.Constants;
import webfootprint.engine.data.Inference;
import webfootprint.engine.data.Predict;
import webfootprint.engine.data.Profile;
import webfootprint.engine.data.Truth;
import webfootprint.engine.data.Tuple;
import webfootprint.engine.data.WebUser;
import webfootprint.engine.data.DefaultRecord;
import webfootprint.engine.apriori.AprioriEngine;
import webfootprint.engine.apriori.AprioriEngnSetting;
import webfootprint.engine.exception.AprioriException;
import webfootprint.engine.nb.NBEngine;
import webfootprint.engine.lda.LDAEngine;
import webfootprint.engine.nb.NBEngnSetting;
import webfootprint.engine.lda.LDAEngnSetting;


public class Console {
	
	protected Database m_database;
	protected String trainingSite;
	protected ArrayList<WebUser> webUsers; //for inferring
	protected ArrayList<Profile> trainingProfiles; //for training
	protected ArrayList<String> trainingAttributes;//for training
	protected ArrayList<String> relyingAttributes;
	//column names
	protected String userId;
	protected String attrName;
	protected String attrGroup;
	protected String attrValue;
	
	protected String profileRelation;
	protected Schema profileSchema;
	
	public Console(String machine, String user, String passwd, int dbType, String site, String userId, String attrName
			, String attrGroup, String attrValue) throws SQLException {
		m_database = new Database(machine, user, passwd, dbType);
		m_database.connect();
		this.trainingSite = site;
		this.attrName = attrName;
		this.userId = userId;
		this.attrValue = attrValue;
		this.attrGroup = attrGroup;
	}
		
	public Database getDatabase() {
		return this.m_database;
	}
	
	public ArrayList<Profile> readTrainingProfiles(String relation, Schema schema, ArrayList<String> trainingAttributes) throws SQLException {
		this.m_database.setActiveRelation(relation);
		ArrayList<Profile> profiles = new ArrayList<Profile>();
		
		String query = "select ";
		for(int i = 0; i < schema.size(); i++) {
			query += schema.getColumn(i) + ", ";
		}
		query = query.substring(0, query.length() - 2);
		query += " from " + getDatabase().getActiveRelation() + " where ";		 
		query += "(";
		for(int i = 0; i < trainingAttributes.size(); i++) {
			query += this.attrName + "=\'" + trainingAttributes.get(i) + "\' or ";
		}
		query = query.substring(0, query.length() - 3);
		query += " );";
		ArrayList<ArrayList> values = getDatabase().constraintSelect(query, schema);
		HashMap<String, Profile> users = new HashMap<String, Profile>();
		
		for(int i = 0; i < values.size(); i++) {
			ArrayList array = values.get(i);
			String value = ((String)array.get(schema.getIndex(this.attrValue))).trim();
			if(!valueValidate(value)) {
				continue;
			}
			
			String user = ((String)array.get(schema.getIndex(this.userId))).trim();
			if(!users.containsKey(user)) {
				users.put(user, new Profile(this.trainingSite, user));
				profiles.add(users.get(user));			
			}
			users.get(user).addProfile(((String)array.get(schema.getIndex(this.attrName))).trim(), 
					((String)array.get(schema.getIndex(this.attrValue))).trim(), ((String)array.get(schema.getIndex(this.attrGroup))).trim());
		
		}
		return profiles;		
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
	
	public void train(String profileRelation, Schema schema, ArrayList<String> interestingAttributes, int[] engines) throws SQLException, AprioriException, IOException, 
	InterruptedException, ClassNotFoundException {
		createTrainingAttributeRelation(interestingAttributes);
		this.trainingAttributes = interestingAttributes;
		this.trainingProfiles = readTrainingProfiles(profileRelation, schema, trainingAttributes);		
		for(int i = 0; i < engines.length; i++) {
			train(engines[i]);
		}
		m_database.closeConnection();
	}
	
	private void createTrainingAttributeRelation(ArrayList<String> interestingAttributes) throws SQLException {
		String trainingRelation = trainingSite + "_" + Constants.TRAINING_ATTRIBUTES_RELATION;
		Schema relationAttributes = new Schema();
		relationAttributes.addColumn("attribute", java.sql.Types.VARCHAR);
		m_database.createTable(trainingRelation, relationAttributes, true);
		m_database.setActiveRelation(trainingRelation);
		for(int i = 0; i < interestingAttributes.size(); i++) {
			String attribute = interestingAttributes.get(i);
			ArrayList<String> values = new ArrayList<String>();
			values.add(attribute);
			m_database.insert(trainingRelation, values);
		}				
	}
	
	public void train(int engineSelect) throws SQLException, AprioriException, InterruptedException, IOException {
		
		Engine engine;
		switch(engineSelect) {
		case Constants.ASSOCIATION_RULE_MINING:
			engine = new AprioriEngine(m_database, null, null, this.trainingAttributes, this.trainingProfiles);
			((AprioriEngine)engine).trainingConfig(new AprioriEngnSetting(trainingSite));
			((AprioriEngine)engine).train();
			break;
		case Constants.NAIVE_BAYES:
			engine = new NBEngine(m_database, null, null, this.trainingAttributes, this.trainingProfiles);
			((NBEngine)engine).trainConfig(new NBEngnSetting(trainingSite));
			((NBEngine)engine).train();
			break;
		case Constants.LDA:
			engine = new LDAEngine(m_database, null, null, this.trainingAttributes, this.trainingProfiles);
			((LDAEngine)engine).trainConfig(new LDAEngnSetting(trainingSite));
			((LDAEngine)engine).train();
			break;
		default:
		}
	}
	
	/**
	 * @param profileRelation The relation in which the training data are stored 
	 * @param schema The relation schema according to which both training data and inferring data in database are organized
	 * @param relyingAttributes attributes employed to infer user attributes needing to be inferred
	 * @param engines engine array used in the inferring
	 * @param inferrningAttributes attributes with groundTruth need to be inferred.
	 * @param guessingAttributes attributes without groundTruth need to be inferred.
	 * @param targetSite the website from which the users needing to be inferred are downloaded
	 * @param targetRelation the relation in which the user to be inferred are stored
	 * @param number the number of users needing to be inferred
	 */

	public void infer(Schema schema, ArrayList<String> relyingAttributes, int[] engines, String targetSite, String targetRelation, int limit) throws SQLException, AprioriException, 
	IOException, InterruptedException, ClassNotFoundException {
		this.profileRelation = this.trainingSite + "_data";
		this.profileSchema = schema;
		this.relyingAttributes = relyingAttributes;
		
		Schema targetSchema = profileSchema;
		Evaluation evaluation = new Evaluation(m_database, trainingSite, targetSite, targetRelation, targetSchema, this.userId, this.attrName, this.attrGroup, this.attrValue);
		this.trainingAttributes = evaluation.getTrainngAttributes(); 
		
		if(this.relyingAttributes == null) {
			this.relyingAttributes = this.trainingAttributes;
		}

		this.webUsers = evaluation.getWebUsers(this.trainingAttributes, this.relyingAttributes, limit);
		for(int i = 0; i < engines.length; i++) {
			infer(engines[i]);
		}
		evaluation.predictRecommending(engines, VoteSetting.getDefaultSettings(), this.webUsers);
		
		evaluation.printPredict(webUsers);
		evaluation.accuracy(webUsers);
		evaluation.writePredictIntoDb(webUsers, Schema.getPredict(), "predict");
		m_database.closeConnection();
	}
	
	public void infer(int engineSelect) throws SQLException, AprioriException, IOException, InterruptedException, ClassNotFoundException {
		
		Engine engine;
		switch(engineSelect) {
		case Constants.ASSOCIATION_RULE_MINING:
			engine = new AprioriEngine(m_database, webUsers, relyingAttributes, null, null);
			((AprioriEngine)engine).inferConfig(new AprioriEngnSetting(trainingSite));
			((AprioriEngine)engine).infer();
			break;
		case Constants.NAIVE_BAYES:
			engine = new NBEngine(m_database, webUsers, relyingAttributes, null, null);
			((NBEngine)engine).inferConfig(new NBEngnSetting(trainingSite));
			((NBEngine)engine).infer();
			break;
		case Constants.LDA:
			this.trainingProfiles = readTrainingProfiles(profileRelation, profileSchema, trainingAttributes);	
			engine = new LDAEngine(m_database, webUsers, relyingAttributes, null, trainingProfiles);
			((LDAEngine)engine).inferConfig(new LDAEngnSetting(trainingSite));
			((LDAEngine)engine).infer();
			break;
		default:
		}
	}
		
	public static void main(String args[]) throws Exception {
		
	}
}
