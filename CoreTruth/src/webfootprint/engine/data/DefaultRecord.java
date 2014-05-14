package webfootprint.engine.data;

import java.util.*;

public abstract class DefaultRecord implements Record {
	
	String site;
	String userId;
	HashMap<String, ArrayList> attributeMap;
	ArrayList<String> attributeList;
	
	protected DefaultRecord(String site, String userId, HashMap<String, ArrayList> attributeMap, ArrayList<String> attributeList) {
		this.site = site;
		this.userId = userId;
		this.attributeMap = attributeMap;
		this.attributeList = attributeList;
	}
	
	protected DefaultRecord(String site, String userId) {
		this(site, userId, new HashMap<String, ArrayList>(), new ArrayList<String>());
	}
	
	protected DefaultRecord(String userId) {
		this(null, userId);
	}
	
	public String getUserId() {
		return userId;
	}
	
	public String getSite() {
		return site;
	}
	
	protected HashMap<String, ArrayList> getMap() {
		return attributeMap;
	}
	
	public Tuple addAttribute(String attribute, Tuple tuple) {
		//attribute = nameSpecialCharSubstitute(attribute);
		if(!attributeMap.containsKey(attribute)) {
			attributeMap.put(attribute, new ArrayList<Tuple>());
			attributeList.add(attribute);
		}
		if(tuple != null) {
			attributeMap.get(attribute).add(tuple);		
		}
		return tuple;
	}
	
	public void addAttribute(String attribute) {
		if(!attributeMap.containsKey(attribute)) {
			attributeMap.put(attribute, new ArrayList<Tuple>());
			attributeList.add(attribute);
		}
	}
	
	public ArrayList<Tuple> addAttribute(String attribute, ArrayList<Tuple> array) {
		//attribute = nameSpecialCharSubstitute(attribute);
		if(!attributeMap.containsKey(attribute)) {
			attributeMap.put(attribute, new ArrayList<Tuple>());
			attributeList.add(attribute);
		}
		attributeMap.get(attribute).addAll(array);
		return array;
	}
	
	public boolean removeAttribute(String attribute) {
		int index = -1;
		for(int i = 0; i < attributeList.size(); i++) {
			if (attributeList.get(i).equals(attribute)) {
				index = i;
				break;
			}
		}
		if(index >= 0) {
			attributeMap.remove(attribute);
			attributeList.remove(index);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean containsAttribute(String attribute) {
		if(attributeMap.containsKey(attribute)) {
			return true;
		} else {
			return false;
		}
	}
	
	public ArrayList<String> getAttributes() {
		return attributeList;
	}
	
	public ArrayList getAttributeValue(String attribute) {
		return attributeMap.get(attribute);
	}
	
	public int size() {
		return attributeList.size();
	}
	
	public String getAttribute(int index) {
		if(index < attributeList.size()) {
			return attributeList.get(index);
		} else {
			return null;
		}
	}
	
	public boolean containsValue(String attribute, String value) {
		if(!attributeMap.containsKey(attribute)) {
			return false;
		} else {
			ArrayList array = attributeMap.get(attribute);
			for(int i = 0; i < array.size(); i++) {
				Tuple tuple = (Tuple)array.get(i);
				String answer = (String)tuple.getObject();
				if(answer.equals(value)) {
					return true;
				}				
			}
			return false;
		}
	}
	
	public static String nameSpecialCharSubstitute(String string) {
		//string =  string.replaceAll("[\\W\\s\\.:; \\|\\/\\-\\\\]+", Constants.ATTRIBUTE_NAME_SPECIAL_CHAR_SUBSTITUTION);
		string =  string.replaceAll("[\\W\\s]+", Constants.ATTRIBUTE_NAME_SPECIAL_CHAR_SUBSTITUTION);
		string = string.replaceAll("[" + Constants.ATTRIBUTE_NAME_SPECIAL_CHAR_SUBSTITUTION+ "]+", Constants.ATTRIBUTE_NAME_SPECIAL_CHAR_SUBSTITUTION);
		return string.toLowerCase().trim();
	}
	
	public static String valueSpecialCharSubstitute(String string) {
		string =  string.replaceAll("[\\W\\s]+", Constants.ATTRIBUTE_VALUE_SPECIAL_CHAR_SUBSTITUTION);
		string = string.replaceAll("[" + Constants.ATTRIBUTE_VALUE_SPECIAL_CHAR_SUBSTITUTION+ "]+", Constants.ATTRIBUTE_VALUE_SPECIAL_CHAR_SUBSTITUTION);
		return string.toLowerCase().trim();
	}

}
