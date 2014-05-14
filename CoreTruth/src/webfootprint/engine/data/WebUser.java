package webfootprint.engine.data;

public class WebUser {

	Profile groundTruth ;
	Profile profile;
	Inference inference;
		
	public WebUser(Profile groundTruth, Profile profile, Inference inference) {
		this.groundTruth = groundTruth;
		this.profile = profile;
		this.inference = inference;
	}
	
	public WebUser(Profile groundTruth, Profile profile) {
		this(groundTruth, profile, new Inference(groundTruth.getSite(), groundTruth.getUserId()));
	}
	
	public Profile getGroundTruth() {
		return this.groundTruth;
	}
	
	public Profile getProfile() {
		return this.profile;
	}
	
	public Inference getInference() {
		return this.inference;
	}
	
}
