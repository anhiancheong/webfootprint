package newCore;


/** attributeInstance is a unique key-value pair with augmented information about the particular value
 * In context, a CoreAttribute will hold information about a particular attribute and it will contain a hashmap of
 * <value, attributeInstance>. This allows attributeInstance to hold information about frequency, source and probability of the value
 * @author Andrew
 *
 */
public class AttributeInstance {

	/** value is the value part of the attribute-value pair
	 * 
	 * */
	String value = "";
	
	/**When the attributeInstance is used for the population inference engine, I use this
	 * additional field to store the name of the attribute. Since the names between the two
	 * databases are different, I dont want to use the attributecollection or coreattribute objects*/
	String name = "";
	
	
	/**This variable is used for the population inference step; it tracks which algorithm generated this attribute*/
	String algorithmSource = "";
	
	
	/*Probability of this value occuring*/
	double probability = 0.0;
	
	
	/**Holds the source of where this value came from; examples include Twitter, Linkedin, Googleplus, etc..*/
	String source = "";
	
	/**Instance type indicates whether the value is a belief or a core truth
	 * */
	String instanceType = "";
	
	double entropy = 0.0;
	
	public AttributeInstance(String attribute_value, String instanceSource, double prob) {
		// TODO Auto-generated constructor stub
		value = attribute_value;
		source = instanceSource;
		probability = prob;
	}
	
	/**This constructor is used to store information for attributes returned from the population inference engine
	 * */
	public AttributeInstance(String attribute_name, String attribute_value, String instanceSource, double conf, String alg) {
		// TODO Auto-generated constructor stub
		name = attribute_name;
		value = attribute_value;
		source = instanceSource;
		probability = conf;
		algorithmSource = alg;
	}

	@Override
	public String toString(){
		return value;
	}
	
	
}
