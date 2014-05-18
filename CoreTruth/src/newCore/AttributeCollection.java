package newCore;
import java.util.ArrayList;



/**
 * AttributeCollection will represent the collection of all instances when a given value for an attribute has occured
 * Ex: Paris may occur as a location multiple times. We still want to keep the instances which are above the threshold
 * and want to track those occurences anyway
 * This will have an arraylist of the AttributeInstances and will persisently track the highest probability instance
 * */
public class AttributeCollection {

	/*Information about the occurences of this value*/
	ArrayList<AttributeInstance> instances = new ArrayList<AttributeInstance>();
	
	/*The value that this collection tracks*/
	String attribute_value = "";
	
	/*MAXIMUMS*/
	AttributeInstance maxInstance;
	
	
	
	public AttributeCollection(String val){
		attribute_value = val.replaceAll("'", "");
	
	}

	public void addInstance(String source, double prob) {
		// TODO Auto-generated method stub
		instances.add(new AttributeInstance(attribute_value, source, prob));
		updateMaxInstance();
	
	}
	
	/**WIll iterate through all instances and find the instance with the max probability*/
	public void updateMaxInstance(){
		
		double tempMaxP = 0.0;
		for(AttributeInstance curInstance : instances) {
			if(curInstance.probability > tempMaxP){
				maxInstance = curInstance;
				tempMaxP = curInstance.probability;
			}
		}
		
	}
	
	public int getCount(){
		return instances.size();
	}

	public double getHighestP() {
		// TODO Auto-generated method stub
		return maxInstance.probability;
	}
	
	
}
