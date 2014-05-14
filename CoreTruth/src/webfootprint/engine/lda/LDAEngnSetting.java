package webfootprint.engine.lda;

public class LDAEngnSetting {
	
	private int neighborNum;
	private int queryNum;
	private int topicNum;
	private int featureNumUpBound;
	private int featureNumLoBound;
	private Double distanceThresh;
	private String site;
	private boolean train;
	
	private int DEFAULT_NEIGHBOR_NUM = 5;
	private int DEFAULT_QUERY_NUM = 1;
	private int DEFAULT_TOPIC_NUM = 50;
	private int DEFAULT_FEATURE_NUM_UP_BOUND = 2;
	private int DEFAULT_FEATURE_NUM_LO_BOUND = 1;
	private Double DEFAULT_DISTANCE_THRESH = 0.001;
	private String DEFAULT_SITE = "linkedin";
	private boolean DEFAULT_TRAIN = false;
	
	public LDAEngnSetting() {
		this.neighborNum = this.DEFAULT_NEIGHBOR_NUM;
		this.queryNum = this.DEFAULT_QUERY_NUM;
		this.topicNum = this.DEFAULT_TOPIC_NUM;
		this.featureNumLoBound = this.DEFAULT_FEATURE_NUM_LO_BOUND;
		this.featureNumUpBound = this.DEFAULT_FEATURE_NUM_UP_BOUND;
		this.train = this.DEFAULT_TRAIN;
	}
	
	public LDAEngnSetting(String site) {
		this();
		setSite(site);
	}
	
	
	public void setNeighborNum(int neighborNum) {
		this.neighborNum = neighborNum;
	}
	
	public void setQueryNum(int queryNum) {
		this.queryNum = queryNum;
	}
	
	public void setTopicNum(int topicNum) {
		this.topicNum = topicNum;
	}
	
	public void setFeatureNumUpBound(int featureNumUpBound) {
		this.featureNumUpBound = featureNumUpBound;
	}
	
	public void setFeatureNumLoBound(int featureNumLoBound) {
		this.featureNumLoBound = featureNumLoBound;
	}
	
	public void setDistanceThresh(Double distanceThresh) {
		this.distanceThresh = distanceThresh;
	}
	
	public void setSite(String site) {
		this.site = site;
	}
	
	public void setTrain(boolean train) {
		this.train = train;
	}
 	
	public void setDefaultNeighborNum(int neighborNum) {
		this.DEFAULT_NEIGHBOR_NUM = neighborNum;
	}
	
	public void setDefaultQueryNum(int queryNum) {
		this.DEFAULT_QUERY_NUM = queryNum;
	}
	
	public void setDefaultTopicNum(int topicNum) {
		this.DEFAULT_TOPIC_NUM = topicNum;
	}
	
	public void setDefaultFeatureNumLoBound(int featureNumLoBound) {
		this.DEFAULT_FEATURE_NUM_LO_BOUND = featureNumLoBound;
	}
	
	public void setDefaultFeatureNumUpBound(int featureNumUpBound) {
		this.DEFAULT_FEATURE_NUM_UP_BOUND = featureNumUpBound;
	}
	
	public void setDefaultDistanceThresh(Double distanceThresh) {
		this.DEFAULT_DISTANCE_THRESH = distanceThresh;
	}
	
	public void setDefaultSite(String site) {
		this.DEFAULT_SITE = site;
	}
	
	public void setDefaultTrain(boolean train) {
		this.DEFAULT_TRAIN = train;
	}
	
	public int getNeighborNum() {
		return this.neighborNum;
	}
	
	public int getQueryNum() {
		return this.queryNum;
	}
	
	public int getTopicNum() {
		return this.topicNum;
	}
	
	public int getFeatureNumUpBound() {
		return this.featureNumUpBound;
	}
	
	public int getFeatureNumLoBound() {
		return this.featureNumLoBound;
	}
	
	public Double getDistanceThresh() {
		return this.distanceThresh;
	}
	
	public String getSite() {
		return this.site;
	}
	
	public boolean getTrain() {
		return this.train;
	}
	
}