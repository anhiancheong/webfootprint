package webfootprint.engine.data;

public class Constants {
	
	/**************Data Pre-processing***********/
	public final static String DEFAULT_ATTRIBUTE_GROUP = "DEFAULT_GROUP";
	public final static String ATTRIBUTE_NAME_SPECIAL_CHAR_SUBSTITUTION = "_";
	public final static String ATTRIBUTE_VALUE_SPECIAL_CHAR_SUBSTITUTION = " ";
	public final static String TRAINING_ATTRIBUTES_RELATION = "training_attributes";
	public final static int VALUE_LENGTH_UP_BOUND = 50;
	
	/**************Prediction Post-processing***********/
	public final static int ASCENDING = 0;
	public final static int DESCENDING = 1;
	
	public final static String MAJORITY_VOTE_CONFIDENCE = "majority_vote_confidence";
	public final static String RECIPROCAL_RANK_CONFIDENCE = "reciprocal_rank_confidence";
	
	/**************Engine***********/
	public final static int ASSOCIATION_RULE_MINING = 0;
	public final static int NAIVE_BAYES = 1;
	public final static int LDA = 2;
	public final static int ENSEMBLE = 3;
	
	/**************Naive Bayes***********/
	public final static String CONDITIONAL_PROBABILITY_SEPERATOR = "SEPERATOR";
	public final static String NAIVE_BAYES_RELATION_PREFIX = "NB_";
	
	public final static int NAIVE_BAYES_EXIST = 0;
	public final static int NAIVE_BAYES_SMOOTHING = 1;
	
	public final static String NAIVE_BAYES_PROBABILITY = "naive_bayes_probability";
	public final static String NAIVE_BAYES_SELECTIVITY = "naive_bayes_selectivity";
	public final static String NAIVE_BAYES_MAJORITY_CONFIDENCE = "naive_bayes_majority_confidence";

	/**************Association Rule***********/
	public final static String APRIORI_CONFIDENCE = "apriori_confidence";
	public final static String APRIORI_SELECTIVITY = "apriori_selectivity";
	
	public final static int ANTECEDENT = 0;
	public final static int CONSEQUENT = 1;
	
	/**************LDA***********/
	public final static String LDA_RELATION_PREFIX = "LDA_";
	public final static String CHI_SQUARE_DISTANCE = "LDA_CHI_SQUARE_DISTANCE";
	public final static String LDA_MODEL_RELATION = "LDA_MODEL";
	public final static String LDA_MAJORITY_VOTE_CONFIDENCE = "lda_majority_vote_confidence";
	
	/**************ENSEMBLE***********/
	public final static String ENSEMBLE_MAJORITY_VOTE_CONFIDENCE = "ensemble_majoirty_vote_confidence";
	public final static String DEFAULT_CONFIDENCE = "default_confidence";
	
	/**************Database***********/
	public final static int ORACLE_DB = 0;
	public final static int MYSQL_DB = 1;
	public final static int POSTGRESQL_DB = 2;	

}
