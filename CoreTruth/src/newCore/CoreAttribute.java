package newCore;
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
	boolean isInitialValue = false;
	
	
	
	/**Hold the set of all values that have ever occured for this attribute */
	LinkedHashMap<String, AttributeCollection> values = new LinkedHashMap<String, AttributeCollection>();

	
	
	public CoreAttribute(String attrName) {
		// TODO Auto-generated constructor stub
		attributeName = attrName;
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
		}
		
		
		//add the value passed in to the overall attribute
		//values.get(attrValue).addInstance(new AttributeInstance());
		values.get(attrValue).addInstance(source, prob);
		
		updateMax();
		
		
	}

	/**This method is for debug purposes*/
	public String getPrintStr() {
		// TODO Auto-generated method stub
		String retStr = attributeName;
		retStr += "   isInitialValue: " + isInitialValue;
		
		retStr += "   Values:  " + values.keySet().toString();
		
		return retStr;
	}
	
	public void updateMax(){
		
		double tempMaxP = 0.0;
		for(String value: values.keySet()){
			if(values.get(value).getHighestP() > tempMaxP) {
				currentMaxValue = value;
				currentMaxSource = values.get(value).maxInstance.source;
				tempMaxP = values.get(value).getHighestP();
			}
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
