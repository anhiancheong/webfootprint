package newCore;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import webfootprint.WebFootPrint;
import webfootprint.engine.exception.AprioriException;





/** Each coreExperiment represents a new experiment on the data. Each experiment has unique parameters concerning
 * the websites used, the initial set of information and the various confidence and probability thresholds
 * an experiment is capable of loading the relevant information from a config file 
 * */
public class CoreExperiment {

	
	public double confidenceThreshold = 0.0;
	
	public double aggregateThreshold = 0.0;
	
	public double websiteThreshold = 0.0;
	
	public int experimentID = -1;
	
	/**websitesUsed is a hash map of the possible websites that an experiment could use
	 * each index in the website will look like <googleplus, true>,....
	 * */
	//HashMap<String, Boolean> websitesUsed = new HashMap<String, Boolean>();
	
	/**Arraylist stores the websites being used in this experiment*/
	ArrayList<String> websites = new ArrayList<String>();
	
	/**This is a data strucutre to hold the various cores involved with the experiment.
	 * There is a core of the initial values, an individual core for each website and a final aggregated core
	 * */
	//ArrayList? HashMap?
	//Should each inference procedure be done in another core, or through a method in this class...
	//Each end up being pretty different and it may be better to have a specialized method instead of having
	//a core method with tons of cases and statements
	
	/**These variable hold the first name and last name of the person being examined. Other attribute may be added,
	 * but the names are a minimum for the program to even function therefore are hardcoded */
	public String first_name = "";
	public String last_name = "";
	public int gtID = -1;
	
	/**These two booleans handle cases for looking at expected values or not*/
	public boolean considerTweets = false;
	public boolean linkedinExtracted = false;
	
	/** This array holds a list of the additional initial attributes that this experiment expects
	 * */
	ArrayList<String> initialAttributes = new ArrayList<String>();
	
	
	/******* PERSON SPECIFIC OBJECTS *******/
	//Each of these will be regenerated for each person that is analyzed
	HashMap<String, CoreAttribute> attributes;
	
	//To pull down the initial values, this array will provide all the possible sources of that information
	String[] allWebsites = {"googleplus", "facebook", "linkedin"};
	
	
	/** This method will read a json configured file and load the various run parameters
	 *  I just used Tavish's code for this part
	 * */
	public void loadConfig(String filename) {
		try {
			Reader read = null;
			read = new FileReader(new File(filename));
			JSONTokener jsonReader = new JSONTokener(read);
			JSONObject params = new JSONObject(jsonReader);
			/*
			 * Reading in configuration information from the file, things like confidence, merging thresholds etc
			 * */
			System.out.println(params.toString(3));

			confidenceThreshold = (float) params.getDouble("confidenceThreshold_forYifang");
			
			aggregateThreshold = (float) params.getDouble("aggregateThreshold");
			
			websiteThreshold = (float) params.getDouble("individualWebsiteThreshold");
			
			experimentID = params.getInt("experimentId");
			
			linkedinExtracted = params.getBoolean("considerLinkedinExtractedAttrs");
			
			considerTweets = params.getBoolean("considerTweets");
			
			//Getting which websites this experiment will seek to use
			JSONArray temp = params.getJSONArray("websites");

			for (int i = 0; i < temp.length(); i++) {
				websites.add(temp.getString(i));
			}

			

			//Gets the attirbutes for the initial core
			ArrayList<String> initialCoreAttrs = new ArrayList<String>();
			JSONArray tempInit = params.getJSONArray("initialCoreAttributes");

			for (int i = 0; i < tempInit.length(); i++) {
				if (tempInit.getString(i).equals("last_name") || tempInit.getString(i).equals("first_name"))
					continue;
				
				initialAttributes.add(tempInit.getString(i));
			}

			DebugOutput.print("Finished reading config file");
			
		}//end of try block
		catch(IOException ie){
			ie.printStackTrace();
		}
		catch(JSONException je){
			je.printStackTrace();
		}
	
	
	}

	/**This method will populate all of the object needed to do the probability and inference task for a given person*/
	public void initialize(String fname, String lname) {
		// TODO Auto-generated method stub
		DebugOutput.print("Initialization Started");
		
		first_name = fname;
		last_name = lname;
		
		/*
		 * add first and last to a CoreAttribute object
		 * for each other initial attribute, find and populate the CoreAttribute
		 * 
		 */
		attributes = new HashMap<String, CoreAttribute>();
		
		
		attributes.put("first_name", new CoreAttribute("first_name"));
		attributes.get("first_name").addValue(first_name, "ground", 1.0);
		//Sets the isInitialValue of the Attribute to trues
		attributes.get("first_name").isGround();
		
		attributes.put("last_name", new CoreAttribute("last_name"));
		attributes.get("last_name").addValue(last_name, "ground", 1.0);
		attributes.get("last_name").isGround();
		
		
		//For each of the other initial attribute like education or location
		//Add an object to hold their instances, then find their values and 
		//populate the attributes
		for(int i = 0; i < initialAttributes.size(); i++) {
			String groundAttr = initialAttributes.get(i);
			attributes.put(groundAttr, new CoreAttribute(groundAttr));
			//query database for initial given value
			populateInitialAttribute(groundAttr);
		}
		
		
	}

	/**Method will query each of our ground truth tables and try to find the correct initial attribute for the given person
	 * */
	private void populateInitialAttribute(String groundAttr) {
		// TODO Auto-generated method stub
		
		/*DO I KNOW GT ID??? LOOK IT UP OR HAVE IT IN FILE*/
		if(gtID == -1){
			setGTID();
		}
		
		PsqlQuery webAttrQuery = new PsqlQuery();
		for(String curWeb : allWebsites){
			//SELECT * from website where gtperson_id = gtID;
			
			DebugOutput.print("Now searching: " + curWeb + " for attributes");
			
			//Make an array of key value pairs for the <attr_name, attr_value> for the attributes from each website
			ArrayList<SimpleEntry<String, String>> initialAttrPairs = webAttrQuery.makeAttrQueryByGTID(curWeb, gtID);
			
			for(SimpleEntry<String, String> curPair : initialAttrPairs){
				String curKey = curPair.getKey();
				String curVal = curPair.getValue();
				
				//If the current key is one of the initial values, add the value to the set of attributes
				if(initialAttributes.contains(curKey)){
					attributes.get(curKey).addValue(curVal, "Initial", 1.0);
				}
			}
			
			//iterate through the attribute names, add the value to the core, mark it off on the hashmap
		}
		
		// IT WOULD BE BETTER TO LOAD THIS FROM THE FILE AS WELL
		/*
		 * 
		 * EXIT IF NOT ALL INITIAL ATTRIBUTE ARE FOUND
		 * 
		 * 
		 * */
		
		//If any of the attributes are empty, return false and exit this execution, print error message
	}

	
	
	/**Queries the ground truth table and finds the relevant gt_person_id for this person*/
	private void setGTID() {
		// TODO Auto-generated method stub
		
		PsqlQuery gtIDQuery = new PsqlQuery();
		gtID = gtIDQuery.getGTID(first_name, last_name);	
		
	}

	
	
	
	/**Run will do the inference and lookup tasks for the person from each table as well as the population engine
	 * It will continue until the attributes become stable
	 * */
	public void run() {
		// TODO Auto-generated method stub
		
		DebugOutput.print("Starting Inference process");
		
		/*
		 * ALGORITHM...
		 * Look at the getDataIntoView subroutine and the getP for a lot of the work of the actual inference
		 * */
		for(int webIndex = 0; webIndex < websites.size(); webIndex++){
			
			String curWebsite = websites.get(webIndex);
			String curWebsiteIDColName = curWebsite + "_id";
			//Run query for the website ids of people with the first and last names equal
			//Run query for all attributes for those ids
			//iterate over attributes and values
			PsqlQuery idQuery = new PsqlQuery();
			ResultSet idQueryResults = idQuery.makeIDQuery(last_name, first_name, curWebsite, attributes, true);
			//idQuery.execute();
			
			//ResultSet idQueryResults = idQuery.getResultSet();
			
			HashMap<String, ArrayList<String>> websiteAttr = idQuery.makeWebsiteAttrQuery(curWebsite, idQueryResults);
			//idQuery.execute();
			
			//ResultSet websiteAttr = idQuery.getResultSet();
			//Will go through all returned attributes and iterate over them and add the significant ones to the core
			//populateCoreFromWebsiteAttributes(websiteAttr);
			
			DebugOutput.print("" + websiteAttr.size());
			
			//Will iterate through attributes, count all attributes of same type on the given website to find probabilty
			addAttributesToCore(websiteAttr, curWebsite);
			
		}
		
		
		getPredictedPopulationAttributes();
		
		
		
		/*Do population inference engine run*/
		
		/*Continue doing website inference until no changes have been made*/
		
		
	}

	/**This method will be the launching point for the population inference engine part
	 * It will find the user id for the person in question, and if they exist, access
	 * another database and pull down new possible attributes*/
	private void getPredictedPopulationAttributes() {
		// TODO Auto-generated method stub
		
		PsqlQuery inferQuery = new PsqlQuery("webfootprint");
		String userIDForYifangDB = inferQuery.getPopEngineID(first_name, last_name);
		if(userIDForYifangDB.equals("-1")){
			DebugOutput.print("NO ID FOUND FOR POPULATION INFERENCE. ENDING THIS STEP");
			return;
		}
		
		inferQuery.close();
		inferQuery = new PsqlQuery("webfootprint");
		

		
		ArrayList<AttributeInstance> popEngineAttr = inferQuery.getPopAttr(userIDForYifangDB, confidenceThreshold);
		
		//loop over all the attributes, add the ones over the threshold, check for different names
		
		
	}

	/** This method will take the key value pairs, count them to find probability, then add the high probability ones to the core
	 * 
	 * 
	 * HashMap dimensions are <attribute name, attribute values[]>
	 * */
	private void addAttributesToCore(
			HashMap<String, ArrayList<String>> websiteAttr, String source) {
		// TODO Auto-generated method stub
		
		Set<String> attributeNames = websiteAttr.keySet();
		
		
		//For each distinct attribute name (education, location), I will do an individual count
		for(String attrName: attributeNames) {
			HashMap<String, Integer> countMapping = new HashMap<String, Integer>();
			
			ArrayList<String> currentValuesList =  websiteAttr.get(attrName);
			
			for(int i = 0; i < currentValuesList.size(); i++) {
				
				//Get the current item in the arraylist
				String currentValue = currentValuesList.get(i);
				
				if(!countMapping.containsKey(currentValue)){
					countMapping.put(currentValue, 0);
				}
				
				countMapping.put(currentValue, countMapping.get(currentValue) + 1);
			}
			
			//Calculate the probabilities for each distinct value
			int totalNumberOfInstances = currentValuesList.size();
			
			//go through each distinct attribute value that occured for this attribute and calculate the probability
			for(String attrValue: countMapping.keySet()){
				
				double probForAttrVal = (double)countMapping.get(attrValue) / (double)totalNumberOfInstances;
				
				//If the value is greater than the website threshold, we want to add it to the core
				if(probForAttrVal > websiteThreshold){
					
					if(!attributes.containsKey(attrValue)){
						attributes.put(attrName, new CoreAttribute(attrName));
					}
					
					attributes.get(attrName).addValue(attrValue, source, probForAttrVal);
					DebugOutput.print("Value added to the core! " + attrName + " -- " + attrValue);
				}//end if website threshold
				
			}//end iteration of distinct value set
			
			
		}//end iteration of all attribute names
		
		
		
		
		
	}//end method



	public void postToDB() {
		// TODO Auto-generated method stub
		
		/*
		 * Call sub routines to generate all the necessary queries, 
		 * store all the posting queries into an array list, then execute them one by one
		 * 
		 * */
		
	}

	/**outputDebugFIle will output a text file with relevant information about the program execution*/
	public void outputDebugFile() {
		// TODO Auto-generated method stub
		DebugOutput.print("Starting debug file output");
		
		try{
			FileWriter fWriter = new FileWriter("debug.txt");
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			
			bWriter.write("Experiment ID: " + experimentID + "  ");
			bWriter.write(first_name + " , " + last_name + " : " + gtID + "\n");
			
			for(String key : attributes.keySet()){
				bWriter.write(attributes.get(key).getPrintStr() + "\n");
			}
			
			
			bWriter.close();
			fWriter.close();
		}
		catch(IOException ioe){
			ioe.printStackTrace();
			
		}
		
	}
	
	
}//end of class
