package webfootprint.engine;

import java.util.*;

import webfootprint.engine.data.WebUser;
import webfootprint.engine.util.db.Database;
import webfootprint.engine.data.Profile;

public abstract class DefaultEngine implements Engine {
	
	protected ArrayList<WebUser> webUsers; //inferring about these webUsers;
	protected Database database;
	protected ArrayList<String> valuedAttributes; //attributes used for inferring target attribute(s). Used in the inferring phase. Specified by engine user.
	protected ArrayList<String> trainingAttributes; // attributes used for training inference engine. Used in the training phase. Specified by engine user.	
	protected ArrayList<Profile> trainingProfiles; //profiles used for training model.
	
	public DefaultEngine(Database database, ArrayList<WebUser> webUsers, ArrayList<String> valuedAttributes,
			ArrayList<String> trainingAttributes, ArrayList<Profile> trainingProfiles) {
		this.database = database;
		this.webUsers = webUsers;
		this.valuedAttributes = valuedAttributes;
		this.trainingAttributes = trainingAttributes;
		this.trainingProfiles = trainingProfiles;
	}
	
	public ArrayList<WebUser> getWebUsers() {
		return this.webUsers;
	}
	
	public Database getDatabase() {
		return this.database;
	}
	
	public ArrayList<String> getValuedAttributes() {
		return this.valuedAttributes;
	}
	
	public ArrayList<String> getTrainingAttributes() {
		return this.trainingAttributes;
	}
	
	public ArrayList<Profile> getTrainingProfiles() {
		return this.trainingProfiles;
	}
	
	public void genericInfer(ArrayList<WebUser> webUsers, ArrayList<String> attributes) {
		this.webUsers = webUsers;
		this.valuedAttributes = attributes;
	}
	
	public void genericTrain(ArrayList<Profile> profiles, ArrayList<String> attributes) {
		this.trainingProfiles = profiles;
		this.trainingAttributes = attributes;
	}	

}
