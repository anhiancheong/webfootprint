package webfootprint.engine.apriori;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;

import webfootprint.engine.data.Predict;
import webfootprint.engine.data.Constants;
import webfootprint.engine.data.Profile;
import webfootprint.engine.data.Inference;
import webfootprint.engine.data.WebUser;
import webfootprint.engine.DefaultEngine;
import webfootprint.engine.util.db.Database;
import webfootprint.engine.util.db.Schema;
import webfootprint.engine.exception.AprioriException;


public class AprioriEngine extends DefaultEngine {
	
	ArrayList<AssociationRule> inferenceRules;
	int inferringStrategy;
	int maxFreItemsetLength;
	double support;
	double confidence;
	int maxValNumPerAttr;
	String associationRuleRelation;
	Schema associationRuleSchema;
	int strategy;
	
	public AprioriEngine(Database database, ArrayList<WebUser> webUsers, ArrayList<String> valuedAttributes,
			ArrayList<String> trainingAttributes, ArrayList<Profile> trainingProfiles) throws SQLException {
		super(database, webUsers, valuedAttributes, trainingAttributes, trainingProfiles);
	}
	
	public void inferConfig(AprioriEngnSetting setting) {
		this.associationRuleRelation = setting.getAssociationRuleRelation();
		this.associationRuleSchema = setting.getAssociatinRuleSchema();
		this.strategy = setting.getStrategy();
	}
	
	public void infer() 
	throws SQLException {
		getDatabase().setActiveRelation(associationRuleRelation);
		inferenceRules = readRules();	
		this.inferringStrategy = strategy;
		super.genericInfer(webUsers, valuedAttributes);
		innerInfer();
	}
	
	public void writeRules() throws SQLException {
    	getDatabase().setActiveRelation(associationRuleRelation);
    	
    	getDatabase().createTable(associationRuleRelation, associationRuleSchema, true);
    	for(int i = 0; i < inferenceRules.size(); i++) {
    		AssociationRule rule = inferenceRules.get(i);
    		RuleConstituent antecedent = (RuleConstituent)rule.getAntecedent();
    		RuleConstituent consequent = (RuleConstituent)rule.getConsequent();
    		Double confidence = (Double)rule.getConfidence();
    		String anteString = "";
    		for(int j = 0; j < antecedent.getKeys().size(); j++) {
    			ArrayList<String> keys = antecedent.getKeys();
    			String key = keys.get(j);
    			String value = antecedent.getValue(key);
    			anteString += key + "=" + value + "|";
    		}
    		anteString = anteString.substring(0, anteString.length() - 1);
    		
    		String conseString = "";
    		for(int j = 0; j < consequent.getKeys().size(); j++) {
    			ArrayList<String> keys = consequent.getKeys();
    			String key = keys.get(j);
    			String value = consequent.getValue(key);
    			conseString += key + "=" + value + "|";
    		}
    		conseString = conseString.substring(0, conseString.length() - 1);
    		
    		String query = "insert into " + associationRuleRelation + " values('"+ anteString + "','" + conseString + "','" + String.valueOf(confidence) + "');";
    		getDatabase().insert(query);
    	}
    }
	
	protected ArrayList<AssociationRule> readRules() throws SQLException {
		String query = "select * from " + getDatabase().getActiveRelation() + ";";
		ArrayList<ArrayList> ruleRelation = getDatabase().constraintSelect(query, associationRuleSchema);
		ArrayList<AssociationRule> inferenceRules = new ArrayList<AssociationRule>();
		
		for(int i = 0; i < ruleRelation.size(); i++) {
			Boolean incorrectRule = false;
			ArrayList rule = ruleRelation.get(i);
		
			String antecedentString = (String)rule.get(0);
			antecedentString = antecedentString.trim();
			String[] antecedents = antecedentString.split("\\|");
			RuleConstituent antecedent = new RuleConstituent(Constants.ANTECEDENT);
			
			for(int j = 0; j < antecedents.length; j++) {
				if(antecedents[j].split("=").length != 2) {
					incorrectRule = true;
					break;
				}
				antecedent.addItem(antecedents[j].split("=")[0], antecedents[j].split("=")[1]);
			}
			
			RuleConstituent consequent = new RuleConstituent(Constants.CONSEQUENT);
			String consequentString = (String)rule.get(1);
			
			consequentString = consequentString.trim();
			String[] consequents = consequentString.split("\\|");
			for(int j = 0; j < consequents.length; j++) {
				if(consequents[j].split("=").length != 2) {
					incorrectRule = true;
					break;
				}
				consequent.addItem(consequents[j].split("=")[0], consequents[j].split("=")[1]);
			}
			
			if(incorrectRule) { 
				continue;
			}

			Double confidence = (Double)rule.get(2);
			inferenceRules.add(new AssociationRule(antecedent, consequent, confidence));			
		}
		return inferenceRules;
	}
	
	private void innerInfer() {		
		for(int i = 0; i < getWebUsers().size(); i++) {
			if(i % 20 == 0) {
				System.out.println("apriori " + i + "users");
			}
			for(int j = 0; j < inferenceRules.size(); j++) {
				innerInfer(inferenceRules.get(j), getWebUsers().get(i));
			}
		}							
	}
	
	private void innerInfer(AssociationRule rule, WebUser user) {
		
		RuleConstituent antecedent = rule.getAntecedent();
		RuleConstituent consequent = rule.getConsequent();
		Double confidence = rule.getConfidence();
		Profile profile = user.getProfile();
		Profile groundTruth = user.getGroundTruth();
		Inference inference = user.getInference();
		
		for(int i = 0; i < antecedent.size(); i++) {
			String attribute = antecedent.getKey(i);
			if(!profile.containsAttribute(attribute)) {
				return;
			}else {
				if(!profile.containsValue(attribute, antecedent.getValue(attribute))) {
					return;
				}
			}
		}
		
		for(int i = 0; i < consequent.size(); i++) {
			String attribute = consequent.getKey(i);
			for(int j = 0; j < groundTruth.size(); j++) {
				String targetAttribute = groundTruth.getAttribute(j);
				if(attribute.equals(targetAttribute)) {
					Predict predict = inference.addInference(targetAttribute, consequent.getValue(attribute), Constants.ASSOCIATION_RULE_MINING);
					predict.getUserData().addUserDatum(Constants.APRIORI_CONFIDENCE, confidence);
					predict.getUserData().addUserDatum(Constants.APRIORI_SELECTIVITY, new Double(antecedent.size()));
				}
			}
		}
	}
	
	public void trainingConfig(AprioriEngnSetting setting) {
		this.maxFreItemsetLength = setting.getMaxFreItemsetLength();
		this.support = setting.getSupport();
		this.confidence = setting.getConfidence();
		this.maxValNumPerAttr = setting.getMaxValNumPerAttr();
		this.associationRuleRelation = setting.getAssociationRuleRelation();
		this.associationRuleSchema = setting.getAssociatinRuleSchema();
	}
	
	public void train() throws SQLException, AprioriException {
		super.genericTrain(trainingProfiles, trainingAttributes);
		Apriori apriori = new Apriori(maxFreItemsetLength, support, confidence, maxValNumPerAttr);
		inferenceRules = apriori.train(trainingProfiles, trainingAttributes);
		writeRules();
	}
	
	public static void main(String[] args) throws SQLException {
		
		
	}
}
