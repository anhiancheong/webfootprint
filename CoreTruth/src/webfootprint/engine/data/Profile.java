package webfootprint.engine.data;

import java.util.*;

import webfootprint.engine.util.math.Counter;
import webfootprint.engine.util.text.Stemmer;

public class Profile extends DefaultRecord {
	
	
	public Profile(String site, String userId, HashMap<String, ArrayList> profileMap, ArrayList<String> profileList) {
		super(site, userId, profileMap, profileList);
	}
	
	public Profile(String site, String userId) {
		this(site, userId, new HashMap<String, ArrayList>(), new ArrayList<String>());
	}
	
	public Profile(String userId) {
		this(null, userId);
	}
	
	public Truth addProfile(String profile, String value, String group) {
		value = DefaultRecord.valueSpecialCharSubstitute(value).trim();
		if(value.equals("")) {
			return null;
		}
		Truth truth = new Truth(value, group);
		return (Truth)super.addAttribute(profile, truth);
	}
	
	public Truth addProfile(String profile, String value) {
		value = DefaultRecord.valueSpecialCharSubstitute(value).trim();
		if(value.equals("")) {
			return null;
		}
		return addProfile(profile, value, Constants.DEFAULT_ATTRIBUTE_GROUP);
	}
	
	public boolean removeProfile(String profile) {
		return super.removeAttribute(profile);
	}
	
	public boolean containsProfile(String profile) {
		return super.containsAttribute(profile);
	}
	
	public ArrayList<String> getProfiles() {
		return super.getAttributes();
	}
	
	public ArrayList getProfileValue(String profile) {
		return super.getAttributeValue(profile);
	}
	
	public Truth addProfile(String attribute, Truth truth) {
		return (Truth)addAttribute(attribute, truth);
	}
	
	public ArrayList<Profile> singleValWOGroupConstraint(int upperBound) {
		Profile generic = this.clone();
		ArrayList<String> attributes = getProfiles();
		for(int i = 0; i < attributes.size(); i++) {
			ArrayList array = getProfileValue(attributes.get(i));
			if(array.size() > upperBound) {
				generic.removeProfile(attributes.get(i));
			}			
		}
		
		int[] width = new int[generic.size()];
		attributes = generic.getAttributes();
		for(int i = 0; i < attributes.size(); i++) {
			String attribute = attributes.get(i);
			width[i] = generic.getAttributeValue(attribute).size();
		}
		
		Counter counter = new Counter(width);
		ArrayList<Profile> profiles = new ArrayList<Profile>();
		while(counter.hasNext()) {
			int[] digits = counter.next();
			Profile copy = new Profile(generic.getSite(), generic.getUserId());
			for(int i = 0; i < attributes.size(); i++) {
				String attribute = attributes.get(i);
				Truth truth = (Truth)generic.getAttributeValue(attribute).get(digits[i]);
				copy.addProfile(attribute, truth);				
			}
			profiles.add(copy);
		}	
		return profiles;
	}
	
	public ArrayList<Profile> singleValWGroupConstraint(int upperBound) {
		ArrayList<Profile> profiles = singleValWOGroupConstraint(upperBound);
		ArrayList<int[]> groupings = largestClustering(profiles);
		
		Profile generic = this.clone();
		ArrayList<String> attributes = getProfiles();
		for(int i = 0; i < attributes.size(); i++) {
			ArrayList array = getProfileValue(attributes.get(i));
			if(array.size() > upperBound) {
				generic.removeProfile(attributes.get(i));
			}			
		}
		
		int[] width = new int[groupings.size()];
		for(int i = 0; i < groupings.size(); i++) {
			int[] cluster = groupings.get(i);		
			width[i] = generic.smallestValueNum(cluster);
		}
		
		Counter counter = new Counter(width);
		profiles = new ArrayList<Profile>();
		while(counter.hasNext()) {
			int[] digits = counter.next();
			Profile copy = new Profile(generic.getSite(), generic.getUserId());
			for(int i = 0; i < digits.length; i++) {
				int[] cluster = groupings.get(i);
				for(int j = 0; j < cluster.length; j++) {
					String attribute = generic.getAttributes().get(cluster[j]);
					Truth truth = (Truth)generic.getAttributeValue(attribute).get(digits[i]);
					copy.addProfile(attribute, truth);
				}
			}
			profiles.add(copy);
		}	
		return profiles;
	}
	
	private int smallestValueNum(int[] cluster) {
		int temp = Integer.MAX_VALUE;
		for(int i = 0; i < cluster.length; i++) {
			String attribute = this.getAttributes().get(cluster[i]);
			int size = this.getAttributeValue(attribute).size();
			if(size < temp) {
				temp = size;
			}
		}
		return temp;
	}
	
	private ArrayList<int[]> largestClustering(ArrayList<Profile> profiles) {
		int size = Integer.MAX_VALUE;
		HashMap<String, ArrayList> temp = new HashMap<String, ArrayList>();
		for(int i = 0; i < profiles.size(); i++) {
			HashMap<String, ArrayList> clusters = groupClustering(profiles.get(i));
			if(clusters.size() < size) {
				temp = clusters;
			}
		}
		
		ArrayList<int[]> largest = new ArrayList<int[]>();
		for(Iterator iterator = temp.keySet().iterator(); iterator.hasNext(); ) {
			ArrayList array = temp.get(iterator.next());
			int[] intArray = new int[array.size()];
			for(int i = 0; i < array.size(); i++) {
				Integer value = (Integer)array.get(i);
				intArray[i] = value.intValue();
			}
			largest.add(intArray);
		}
		return largest;
	}
	
	private HashMap<String, ArrayList> groupClustering(Profile profile) {
		HashMap<String, ArrayList> clusters = new HashMap<String, ArrayList>();
		ArrayList<String> attributes = profile.getAttributes();
		for(int i = 0; i < attributes.size(); i++) {
			String attribute = attributes.get(i);
			Truth truth = (Truth)profile.getAttributeValue(attribute).get(0);
			String group = truth.getGroup();
			if(!clusters.containsKey(group)) {
				clusters.put(group, new ArrayList<Integer>());
			}
			clusters.get(group).add(i);			
		}
		return clusters;
	}
	
	public Profile clone()  {
		Profile profile = new Profile(site, userId);
		ArrayList<String> list = this.getAttributes();
		ArrayList<String> copyList = profile.getAttributes();
		for(int i = 0; i < list.size(); i++) {
			copyList.add(new String(list.get(i)));
		}
		
		HashMap<String, ArrayList> copyMap = profile.getMap();
		for(int i = 0; i < list.size(); i++) {
			ArrayList array = this.getAttributeValue(list.get(i));
			ArrayList<Truth> copyArray = new ArrayList<Truth>();
			for(int j = 0; j < array.size(); j++) {
				copyArray.add(((Truth)array.get(j)).clone());
			}
			copyMap.put(list.get(i), copyArray);
		}
		return profile;
	}
	
	public static void main(String[] args) {
		
		Profile profile = new Profile("linkedin", "1234");
		profile.addProfile("skill", "java", "group 1");
		profile.addProfile("skill", "marketing", "group 2");
		profile.addProfile("industry", "software", "group 1");
		profile.addProfile("industry", "retail", "group 2");
		profile.addProfile("gender", "male", "default");
		profile.singleValWGroupConstraint(5);

	}
	
}
