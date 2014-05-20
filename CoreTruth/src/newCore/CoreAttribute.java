package newCore;
import java.util.HashMap;
import java.util.LinkedHashMap;




/** CoreAttribute is an object used to hold information about a particular attribute that could occur in the core
 * And example would be 'location' which could have multiple different values and instances of those values
 * CoreAttribute will help track this information
 * CoreAttribute
 * */
public class CoreAttribute {



	
	
	/**This label is for the type of attribute that this object is tracking
	 * Examples include location, first_name, last_name*/
	String attributeName  = "";
	
	/**This will hold the string value of the attribute value with the highest probabilty in the core
	 * ex: New York may be here instead of Paris if P(NYC) > P(Paris)
	 * */
	String currentMaxValue = "";
	
	/**currentMaxSource is the origin of the given maximum probability value*/
	String currentMaxSource = "";
	
	/**This boolean tracks whether this attribute was given by the user for the experiment
	 * For example, first and last name is ALWAYS groundTruth */
	boolean isInitialValue;
	
	
	
	/**Hold the set of all values that have ever occured for this attribute */
	LinkedHashMap<String, AttributeCollection> values = new LinkedHashMap<String, AttributeCollection>();
	
	/***/
	HashMap<String, Double> localProb;
	
	
	public CoreAttribute(String attrName) {
		// TODO Auto-generated constructor stub
		attributeName = attrName;
		isInitialValue = false;
	}
	
	/**Sets the isInitialValue flag to true*/
	public void isGround() {
		// TODO Auto-generated method stub
		DebugOutput.print("Initial Value of attribute " + attributeName + " is set to true");
		isInitialValue = true;
		
	}

	/** Will add an instance of a value for this given attribute
	 * ex: will add "Chicago" "linkedin" .75 and pass it down into the attribute instance objects
	 * 
	 * */
	public void addValue(String attrValue, String source, double prob) {
		// TODO Auto-generated method stub
		
		attrValue = attrValue.replace("'", "");
		
		//If the new value is not yet in the hashmap, we will add it
		if(values.containsKey(attrValue) == false){
			values.put(attrValue, new AttributeCollection(attrValue));
			DebugOutput.print("A new value for the attribute: " + attributeName + " has been found");
		}
		
		
		//add the value passed in to the overall attribute
		//values.get(attrValue).addInstance(new AttributeInstance());
		values.get(attrValue).addInstance(source, prob);
		
		DebugOutput.print("Value: " + attrValue + " has been added! ");
		
		updateMax();
		
		
	}

	/**This method is for debug purposes*/
	public String getPrintStr() {
		// TODO Auto-generated method stub
		updateMax();
		
		DebugOutput.print("Number of values for attribute: " + attributeName + " is: " + values.keySet().size());
		
		String retStr = attributeName;
		retStr += "   isInitialValue: " + isInitialValue;
		
		retStr += "   Values:  ";
		for(String key: values.keySet()){
			retStr += "  " + values.get(key).attribute_value + "(" + values.get(key).maxInstance.source + " -- P = " + localProb.get(key) + ")";
			//retStr += " \n Other Values: " + values.get(key).getAllInstancesPrintString();
		}
		
		
		return retStr;
	}
	
	public void updateMax(){
		
		double tempMaxP = 0.0;
		updateValuesProb();
		for(String value: values.keySet()){
			if(values.get(value).getHighestP() > tempMaxP) {
				currentMaxValue = value;
				currentMaxSource = values.get(value).maxInstance.source;
				tempMaxP = localProb.get(currentMaxValue);
			}
		}
		
	}
	
	/**Each value will individually have a probability relative to other values
	 * ie if Paris occurs 3 times and london occurs once, I will want a probability of .75 for paris and .25 for london*/
	public void updateValuesProb(){
		
		double totalCount = 0.0;
		localProb = new HashMap<String, Double>();
		//Calculate the total number of counts of value occurences for this value
		for(String key: values.keySet()){
			totalCount += (double)values.get(key).getCount();
		}
		
		//Calculate the probability for each value relative to other values
		for(String key: values.keySet()){
			localProb.put(key, (double)values.get(key).getCount()/totalCount);
		}
		
		
	}

	public String getAttributeName() {
		// TODO Auto-generated method stub
		return attributeName;
	}

	
	public String getMaxValStr() {
		// TODO Auto-generated method stub
		return currentMaxValue;
	}
	
}
