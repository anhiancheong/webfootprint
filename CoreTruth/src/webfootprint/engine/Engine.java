package webfootprint.engine;

import java.util.*;

import webfootprint.engine.data.WebUser;
import webfootprint.engine.util.db.Database;
import webfootprint.engine.data.Profile;

public interface Engine {
	
	public ArrayList<WebUser> getWebUsers();
	
	public Database getDatabase();
	
	public ArrayList<String> getValuedAttributes();
	
	public ArrayList<String> getTrainingAttributes();
	
	public ArrayList<Profile> getTrainingProfiles();
	
	public void genericInfer(ArrayList<WebUser> webUsers, ArrayList<String> attributes);
	
	public void genericTrain(ArrayList<Profile> profiles, ArrayList<String> attributes);

}
