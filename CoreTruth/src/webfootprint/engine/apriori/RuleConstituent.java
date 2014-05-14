package webfootprint.engine.apriori;

import java.util.*;

public class RuleConstituent {
	
	HashMap<String, String> map;
	ArrayList<String> keys;
	
	public RuleConstituent(int type) {
		map = new HashMap<String, String>();
		keys = new ArrayList<String>();
	}
	
	public boolean addItem(String key, String value) {
		if(!map.containsKey(key)) {
			map.put(key, value);
			keys.add(key);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean removeItem(String key) {
		int index = -1;
		for(int i = 0; i < map.size(); i++) {
			if (map.get(i).equals(key)) {
				index = i;
				break;
			}
		}
		if(index >= 0) {
			map.remove(key);
			keys.remove(index);
			return true;
		} else {
			return false;
		}
	}
	
	public ArrayList<String> getKeys() {
		return this.keys;
	}
	
	public String getValue(String key) {
		return map.get(key);
	}
	
	public String getKey(int index) {
		return keys.get(index);
	}
	
	public String getValue(int index) {
		return map.get(getKey(index));
	}
	
	public int size() {
		return keys.size();
	}
}
