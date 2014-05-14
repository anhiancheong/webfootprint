package webfootprint.engine.data;

import java.util.ArrayList;
import java.util.HashMap;

public interface Record {
	
	public Tuple addAttribute(String attribute, Tuple tuple);
	
	public void addAttribute(String attribute);
	
	public ArrayList<Tuple> addAttribute(String attribute, ArrayList<Tuple> tuples);
	
	public boolean removeAttribute(String attribute);
	
	public ArrayList<String> getAttributes();
	
	public ArrayList getAttributeValue(String attribute);
	
	public String getSite();
	
	public String getUserId();
	
	public boolean containsAttribute(String attribute);
	
	public int size();
	
	public boolean containsValue(String attribute, String value);
	
}
