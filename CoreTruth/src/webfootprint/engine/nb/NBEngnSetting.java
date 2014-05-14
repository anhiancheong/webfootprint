package webfootprint.engine.nb;

public class NBEngnSetting {
	
	private int featureNumUpBound;
	private int featureNumLoBound;
	private Double confidence;
	private String site;
	private Double smoothing;
	private Double pairSizeUpBound;
	private double priorNumToPostNum;
	
	private int DEFAULT_FEATURE_NUM_UP_BOUND = 2;
	private int DEFAULT_FEATURE_NUM_LO_BOUND = 1;
	private Double DEFAULT_CONFIDENCE = 1.0;
	private String DEFAULT_SITE = "linkedin";
	private Double DEFAULT_SMOOTHING = 0.1;
	private Double DEFAULT_PAIR_SIZE_UP_BOUND = 60000.0;
	private static double DEFAULT_PRIOR_NUM_TO_POST_POST_NUM = 10.0;
	
	public NBEngnSetting() {
		this.featureNumUpBound = this.DEFAULT_FEATURE_NUM_UP_BOUND;
		this.featureNumLoBound = this.DEFAULT_FEATURE_NUM_LO_BOUND;
		this.confidence = this.DEFAULT_CONFIDENCE;
		this.site = this.DEFAULT_SITE;
		this.smoothing = this.DEFAULT_SMOOTHING;
		this.pairSizeUpBound = this.DEFAULT_PAIR_SIZE_UP_BOUND;
		this.priorNumToPostNum = this.DEFAULT_PRIOR_NUM_TO_POST_POST_NUM;
	}
	
	public NBEngnSetting(String site) {
		this();
		setSite(site);
	}
	
	public void setFeatureNumUpBound(int featureNumUpBound) {
		this.featureNumUpBound = featureNumUpBound;
	}
	
	public void setFeatureNumLoBound(int featureNumLoBound) {
		this.featureNumLoBound = featureNumLoBound;
	}
	
	public void setConfidence(Double confidence) {
		this.confidence = confidence;
	}
	
	public void setSite(String site) {
		this.site = site;
	}
	
	public void setSmoothing(Double smoothing) {
		this.smoothing = smoothing;
	}
	
	public void setPairSizeUpBound(Double pairSizeUpBound) {
		this.pairSizeUpBound = pairSizeUpBound;
	}
	
	public void setDefaultFeatureNumUpBound(int featureNumUpBound) {
		this.DEFAULT_FEATURE_NUM_UP_BOUND = featureNumUpBound;
	}
	
	public void setDefaultFeatureNumLoBound(int featureNumLoBound) {
		this.DEFAULT_FEATURE_NUM_LO_BOUND = featureNumLoBound;
	}
	
	public void setDefaultConfidence(Double confidence) {
		this.DEFAULT_CONFIDENCE = confidence;
	}
	
	public void setDefaultSite(String site) {
		this.DEFAULT_SITE = site;
	}
	
	public void setDefaultSmoothing(Double smoothing) {
		this.DEFAULT_SMOOTHING = smoothing;
	}
	
	public void setDefaultPairSizeUpBound(Double pairSizeUpBound) {
		this.DEFAULT_PAIR_SIZE_UP_BOUND = pairSizeUpBound;
	}
	
	public int getFeatureNumUpBound() {
		return this.featureNumUpBound;
	}
	
	public int getFeatureNumLoBound() {
		return this.featureNumLoBound;
	}
	
	public Double getConfidence() {
		return this.confidence;
	}
	
	public String getSite() {
		return this.site;
	}
	
	public Double getSmoothing() {
		return this.smoothing;
	}
	
	public Double getPairSizeUpBound() {
		return this.pairSizeUpBound;
	}
	
	public double getPriorNumToPostNum() {
		return this.priorNumToPostNum;
	}

}
