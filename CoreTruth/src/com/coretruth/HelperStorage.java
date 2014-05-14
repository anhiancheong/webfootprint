package com.coretruth;

import java.util.ArrayList;
import java.util.LinkedHashMap;


/*
This class is used to just store the variable - value - probability setup
It is used to access the probability value of the specific variable value pair

*/

public class HelperStorage {

	ArrayList<String> attributes = new ArrayList<String>();
	
	LinkedHashMap<String,LinkedHashMap<String,Float>> data = new LinkedHashMap<String,LinkedHashMap<String,Float>>();
				 //<Attribute <Value, probability>>	
	

	public void addData(String attribute_name, String attribute_value, Float probability){
		
		LinkedHashMap<String,Float> value_probability = new LinkedHashMap<String,Float>();
		value_probability.put(attribute_value, probability);
		data.put(attribute_name,value_probability);
		
	}
	
	public void addAttribute(String attribute){
		attributes.add(attribute);
	}
	
	public Float getProbabilityValue(String attribute_name, String attribute_value){
		
		LinkedHashMap<String,Float> value_probability = new LinkedHashMap<String,Float>();
		
		value_probability = data.get(attribute_name);
		Float p = value_probability.get(attribute_value);

		return p;
	}
}
