package webfootprint.engine.apriori;

import webfootprint.engine.data.Constants;
import webfootprint.engine.util.db.Schema;

public class AprioriEngnSetting {
	
	private int maxFreItemsetLength;
	private double support;
	private double confidence;
	private int maxValNumPerAttr;
	private String associationRuleRelation;
	private Schema associationRuleSchema;
	private int strategy;
	
	private static int DEFAULT_MAX_FRE_ITEMSET_LENGTH = 3;
	private static double DEFAULT_SUPPORT = 0.0025;
	private static double DEFAULT_CONFIDENCE = 0.5;
	private static int DEFAULT_MAX_VAL_NUM_PER_ATTR = 5;
	private static String DEFAULT_ASSOCIATION_RULE_RELATION = "association_rule";
	private static Schema DEFAUL_ASSOCIATION_RULE_SCHEMA = Schema.getAssociationRule();
	private static String DEFAULT_INFER_STRATEGY = Constants.APRIORI_CONFIDENCE;
	
	public AprioriEngnSetting() {
		this.maxFreItemsetLength = this.DEFAULT_MAX_FRE_ITEMSET_LENGTH;
		this.support = this.DEFAULT_SUPPORT;
		this.confidence = this.DEFAULT_CONFIDENCE;
		this.maxValNumPerAttr = this.DEFAULT_MAX_VAL_NUM_PER_ATTR;
		this.associationRuleRelation = this.DEFAULT_ASSOCIATION_RULE_RELATION;
		this.associationRuleSchema = this.DEFAUL_ASSOCIATION_RULE_SCHEMA;
	}
	
	public AprioriEngnSetting(String site) {
		this();
		setAssociationRuleRelation(site + "_" + associationRuleRelation);
	}
	
	public void setmaxFreItemsetLength(int maxFreItemsetLength) {
		this.maxFreItemsetLength = maxFreItemsetLength;
	}
	
	public void setSupport(double support) {
		this.support = support;
	}
	
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	
	public void setMaxValNumPerAttr(int maxValNumPerAttr) {
		this.maxValNumPerAttr = maxValNumPerAttr;
	}
	
	public void setAssociationRuleRelation(String associationRuleRelation) {
		this.associationRuleRelation = associationRuleRelation;
	}
	
	public void setAssociationRuleSchema(Schema associationRuleSchema) {
		this.associationRuleSchema = associationRuleSchema;
	}
	
	public void setStrategy(int strategy) {
		this.strategy = strategy;
	}
	
	public void setDefaultMaxFreItemsetLength(int maxFreItemsetLength) {
		this.DEFAULT_MAX_FRE_ITEMSET_LENGTH = maxFreItemsetLength;
	}
	
	public void setDefaultSupport(double support) {
		this.DEFAULT_SUPPORT = support;
	}
	
	public void setDefaultConfidence(double confidence) {
		this.DEFAULT_CONFIDENCE = confidence;
	}
	
	public void setDefaultMaxValNumPerAttr(int maxValNumPerAttr) {
		this.DEFAULT_MAX_VAL_NUM_PER_ATTR = maxValNumPerAttr;
	}
	
	public void setDefaultAssociationRuleRelation(String associationRuleRelation) {
		this.DEFAULT_ASSOCIATION_RULE_RELATION = associationRuleRelation;
	}
	
	public void setDefaultAssociationRuleSchema(Schema associationRuleSchema) {
		this.DEFAUL_ASSOCIATION_RULE_SCHEMA = associationRuleSchema;
	}
	
	public void setDefaultInferStrategy(String strategy) {
		this.DEFAULT_INFER_STRATEGY = strategy;
	}
	
	public int getMaxFreItemsetLength() {
		return this.maxFreItemsetLength;
	}
	
	public double getSupport() {
		return this.support;
	}
	
	public double getConfidence() {
		return this.confidence;
	}
	
	public int getMaxValNumPerAttr() {
		return this.maxValNumPerAttr;
	}
	
	public String getAssociationRuleRelation() {
		return this.associationRuleRelation;
	}
	
	public Schema getAssociatinRuleSchema() {
		return this.associationRuleSchema;
	}
	
	public int getStrategy() {
		return this.strategy;
	}


}
