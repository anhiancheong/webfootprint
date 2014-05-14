package com.coretruth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultiset;

public class GenerateCoreActual {

	//probability threshold for each website's attribute guess
	public float pThreshold = Constants.individualWebsiteThreshold;

	//The set oof websites being used for this core
	public String websites[];
	
	public int webcount[];
	DBAdaptor dbadaptor = null;
	
	ResultSet userids[];
	String tableName;
	//All the cores after multiple runs/feedbacks
	Core cores[];
	Core aggregateCore = new Core();
	
	//boolean for whether core has changed since the last check to the db
	boolean hasCoreChanged[];
	boolean countFlag[];
	
	//the minimum information about the person
	public String firstName = null;
	public String lastName = null;

	// public void doWork(String[] args, String fname, String lname, boolean
	// callDumpCore) throws IOException {
	public void doWork(ArrayList<String> listWebsites,
			HashMap<String, String> initialCoreKeyValues, boolean callDumpCore,
			String tweetHandle) throws IOException {
		// if (args.length < 1) {
		// System.out
		// .println("Specify websites\nUsage: -w twitter googleplus facebook pipl whitepages foursquare");
		// return;
		// }
		// if (Arrays.asList(args).contains("-w") && args.length < 2) {
		// System.out
		// .println("Specify websites\n twitter googleplus facebook pipl whitepages foursquare");
		// return;
		// }

		//takes the list of websites (facebook, linkedin...)
		this.websites = new String[listWebsites.size()];
		this.webcount = new int[websites.length];
		countFlag = new boolean[websites.length];

		//format website names to lower case
		for (int i = 0; i < listWebsites.size(); i++) {
			websites[i] = listWebsites.get(i).toLowerCase();
			countFlag[i] = true;
		}

		//have an array of userids - one for each website
		userids = new ResultSet[websites.length];

		//tracks if each website changed the core
		this.hasCoreChanged = new boolean[websites.length];
		
		//build one core per table
		this.cores = new Core[websites.length];
		
		//this is just a little sad
		int i = 0;

		for (i = 0; i < cores.length; i++) {

			cores[i] = new Core();

			// iterate through all initial core key value pairs and add them to
			// core

			for (String key : initialCoreKeyValues.keySet()) {
				String attribute_name = key;
				String attribute_value = initialCoreKeyValues.get(key);
				cores[i].addToCore(attribute_name, attribute_value, "1.0");
			}

			//set the exact experiment id
			cores[i].exp_id = Constants.experimentId;
			hasCoreChanged[i] = false;

			do {
				//This will add information to the core (one core per website)
				this.getDataIntoViews(cores[i], false, i);
				
				//calculate likelyhood of attributes within each core
				this.calculateP(cores[i], false, i);

				//Keep doing this until no more attributes are added to the set
			} while (hasCoreChanged[i] == true);
			// cores[i].printAll();
		}

		//Combine all of the cores, consider the twitter handles
		this.aggregateCores(tweetHandle);

		//DO all the logic for eahc core now
		for (i = 0; i < cores.length; i++) {
			this.getDataIntoViews(aggregateCore, true, i);
			this.calculateP(aggregateCore, true, i);

		}

		// System.out.println("\nAggregate core: ");

		// aggregateCore.printAll();

		if (!(aggregateCore.core.size() < 3)) { // only if more than name
			//What is callDumpCore?????????? -ahc
			if (callDumpCore) {
				aggregateCore.dumpCore();

			} else {
				// Get Yifangs values and then do thing and then dump
				// Read from Predict Table and change the aggregate core
				// accordingly
				// System.out.println("IN ELSE PART");

				//Get the prediction from the population inference engine
				aggregateCore.getPrediction();
				aggregateCore.dumpFinalCore();
			}

		}
		dbadaptor.closeDBhandle();

		return;
	}

	public void getDataIntoViews(Core core, boolean isaggregateCore, int i) {

		//use a different p-value threshold for aggregating terms
		if (isaggregateCore) {
			pThreshold = Constants.aggregateThreshold;
		}

		//just using parallel arrays of website..... why not store these into the core themselves?
		tableName = websites[i];

		dbadaptor = new DBAdaptor();
		
		//this returns the table ids?
		//Looks at the given website (google+, fb, etc..)
		//gets all the user ids in that table of the people who names 
		// and other attributes match those we have in the iniital core
		
		userids[i] = dbadaptor.getUserIds(tableName, core);

		ArrayList<String> ids = new ArrayList<String>();
		int count = 0;
		try {
			while (userids[i].next()) {
				ids.add(userids[i].getString(1));
				count++;
			}
		} catch (SQLException ex) {
			System.out.println("ERROR : " + ex.getMessage());
		}
		// System.out.println("User found on " + websites[i] + ": " + count);
		// Store all the data in views, one for each website
		webcount[i] = count;
		System.out.println("Users on " + websites[i] + " : " + count);

		//add the count of users for this website to the core
		if (!isaggregateCore && countFlag[i]) {
			core.addCount(websites[i], count);
			countFlag[i] = false;
		}

		if (count != 0) {
			dbadaptor.createDataView(tableName, ids);
		}

		// Till here, we have all the data on a single name from different
		// website stored in views.
		dbadaptor.closeDBhandle();
	}

	public void calculateP(Core core, boolean isaggregateCore, int i) {
		// Go through each view and calculate attribute probabilities.
		HelperStorage[] websites_data = new HelperStorage[websites.length];

		if (webcount[i] == 0) {
			hasCoreChanged[i] = false;
			return;
		}

		core.source_website = websites[i];

		if (isaggregateCore) {
			pThreshold = Constants.aggregateThreshold;
		}

		websites_data[i] = new HelperStorage();
		// Get the list of all attributes we have in that table, so that then we
		// can find the probability for those attributes
		dbadaptor = new DBAdaptor();
		websites_data[i].attributes = dbadaptor.getAttributeNames(websites[i]);

		ArrayList<String> attribute_values = new ArrayList<String>();

		for (String attribute_name : websites_data[i].attributes) {

			// System.out.println("||||| Attribute_name:" + attribute_name);

			if (attribute_name.equalsIgnoreCase("blob")
					|| attribute_name.equalsIgnoreCase("first_name")
					|| attribute_name.equalsIgnoreCase("last_name")
					|| attribute_name.equalsIgnoreCase("name")
					|| attribute_name.equalsIgnoreCase("full_name")) {
				hasCoreChanged[i] = false;
				continue;

			}
			// System.out.println("///////" + core.getCoreValuesAsString()+
			// "    " + attribute_name);
			if (core.getCoreValuesAsString().contains(attribute_name)) {
				// System.out.println("Already in core " + attribute_name);
				hasCoreChanged[i] = false;
				continue;
			}

			// for each particular attribute, get all the possible values from
			// db
			attribute_values = dbadaptor.getValuesForAttribute(websites[i],
					attribute_name);
			int total = attribute_values.size();
			HashMultiset<String> attr_values = HashMultiset.create();

			// put all the values in the multiset
			attr_values.addAll(attribute_values);

			// get the distinct values
			Set<String> distinct_values_for_attr = attr_values.elementSet();

			// Iterate over distinct_values_for_attr and find attribute
			// probability and store it
			Iterator<String> iter = distinct_values_for_attr.iterator();

			float probability = 0.0f;

			// Now we have the values for the attribute, calculate probability
			// of each value
			// iterate through the attribute values and count each recurring
			// value
			// divide it by total i.e. attribute_values.size()
			// store its
			float previousMaxCoreP = 0.0f;
			String max_core_attribute_val = "";
			float previousNonCoreP = 0.0f;
			String max_non_core_attribute_val = "";

			while (iter.hasNext()) {

				String attribute_some_value = iter.next().toString();

				// Decide how to add things to core, for now its only
				// probability value,
				// but can be count and percentage value

				int count = attr_values.count(attribute_some_value);
				probability = (float) count / (float) total;

				// System.out.println(attribute_some_value+" : "+count+"  Total: "+total+" P = "+probability);

				if (probability >= pThreshold && probability > previousMaxCoreP) {
					// add this attribute,value to core
					previousMaxCoreP = probability;
					max_core_attribute_val = attribute_some_value;

				} else if (probability > previousNonCoreP) {

					previousNonCoreP = probability;
					max_non_core_attribute_val = attribute_some_value;

				}
			} // end of while for each attribute's values
				// Found the max of core and first non core for each attribute
				// till here

			if (previousMaxCoreP > 0.0) {

				core.addToCore(attribute_name, max_core_attribute_val,
						Float.toString(previousMaxCoreP));

				// System.out.println("++++++ Added to core : " +
				// attribute_name+ " : " + max_core_attribute_val);

				// core.notCore.remove(attribute_name);

				hasCoreChanged[i] = true;
			} else {

				if (!core.notCore.containsKey(attribute_name)) {

					core.addToNonCore(attribute_name,
							max_non_core_attribute_val,
							Float.toString(previousNonCoreP));

					// System.out.println("------ Added to non-core : "+
					// attribute_name + " : " + max_non_core_attribute_val);
				}

				hasCoreChanged[i] = false;
			}

			// save the probability in HelperStorage object
			websites_data[i].addData(attribute_name, max_core_attribute_val,
					probability);

			// System.out.println(attribute_name +" " +attribute_some_value
			// +": "+websites_data[i].getProbabilityValue(attribute_name,
			// attribute_some_value) + "--" +total);

			// till here all probabilities for different values of an attribute
			// are done!

		} // end for each attribute
			// till here all the attributes for a website are processed

//		System.out.println("Core For website : " + core.source_website + "\n"
//				+ core.getCoreValuesAsString() + "\n\n");

		// probablities for all websites calculated

		// Now we refine the core...this we can do above will calculating
		// probabilities
		// If the P is greater than certain min threshold, we can add it to core
		// and then do the process again
		// That is we issue all the queries again but this time we get the data
		// from the views
		// The result set will be reduced as we add more data to the core

		dbadaptor.closeDBhandle();
	}

	public void aggregateCores(String twitter_handle) {
		// make aggregate core from cores[i]
		aggregateCore.source_website = "Aggregate";

		for (int i = 0; i < cores.length; i++) {

			Set<String> keys = this.cores[i].core.keySet();

			// System.out.println("Keys for " + websites[i] + " :" +
			// keys.toString());

			if (keys.isEmpty()) {
				continue;
			}
			for (String k : keys) { // keys are all the attribute names

				if (!(aggregateCore.core.containsKey(k))) {
					// add to aggregateCore
					aggregateCore.addToCore(k, this.cores[i].core.get(k),
							cores[i].probability.get(k));
					// aggregateCore.source_website = websites[i];
				} else {
					if (Float.parseFloat(aggregateCore.probability.get(k)) < Float
							.parseFloat(cores[i].probability.get(k))) {
						// replace the value for the attribute k in
						// aggregateCore..storing the highest Probability
						aggregateCore.addToCore(k, this.cores[i].core.get(k),
								cores[i].probability.get(k));
					}
				}
			}

			//special handling for adding twitter data
			if (Constants.considerTweets) {
				HashMap<String, String> tweet_attrs = this
						.getTwitterAttributes(twitter_handle);
				for (Map.Entry<String, String> entry : tweet_attrs.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					String prob = getProbabilityOfTweetAttr(twitter_handle,
							key, value);
					aggregateCore.addToCore(key, value, prob);
					// System.out.println(key + " " + value +"  " + prob);
				}

			}

			aggregateCore.addCountVal(websites[i],
					cores[i].count.get(websites[i])); // website profiles count

			/*
			 * What kind of attributes are 'non-core'? The ones that fell beneathe the confidence threshold?
			 * */
			Set<String> nonkeys = this.cores[i].notCore.keySet();

			for (String k : nonkeys) {

				if (!(aggregateCore.notCore.containsKey(k))) {
					// add to aggregateCore
					aggregateCore.addToNonCore(k, this.cores[i].notCore.get(k),
							cores[i].notCoreProbability.get(k));
				} else {
					if (Float.parseFloat(aggregateCore.notCoreProbability
							.get(k)) < Float
							.parseFloat(cores[i].notCoreProbability.get(k))) {
						aggregateCore.addToNonCore(k,
								this.cores[i].notCore.get(k),
								cores[i].notCoreProbability.get(k));
					}
				}
			}
		}

		// System.out.println("\nAggregate: " + aggregateCore.source_website +
		// "\n" + aggregateCore.getCoreValuesAsString());
	}

	public HashMap<String, String> getTwitterAttributes(String twitter_handle) {

		DBAdaptor dbForTweets = new DBAdaptor();

		String sql = "Select * from tweet_structured_attribute where twitter_handle = '"
				+ twitter_handle + "'";
		HashMap<String, ArrayList<String>> tweet_attribute_values = new HashMap<String, ArrayList<String>>();
		HashMap<String, String> tweets_result_attrs = new HashMap<String, String>();

		try {
			dbForTweets.rs = dbForTweets.st.executeQuery(sql);

			while (dbForTweets.rs.next()) {

				String attribute_key = dbForTweets.rs.getString("structured_attribute");
				String attribute_value = dbForTweets.rs.getString(
						"structured_attribute_value").toLowerCase();

				if (tweet_attribute_values.containsKey(attribute_key)) {
					ArrayList<String> temp = new ArrayList<String>();
					temp = tweet_attribute_values.get(attribute_key);
					temp.add(attribute_value);
					tweet_attribute_values.put(attribute_key, temp);
				} else {
					ArrayList<String> temp = new ArrayList<String>();
					temp.add(attribute_value);
					tweet_attribute_values.put(attribute_key, temp);
				}
			}

			// System.out.println(twitter_handle + "\n" +
			// tweet_attribute_values.toString());
			// ok so now what to do with these values and which one to
			// consider????

			// I am going to take the value with highest frequency for each
			// attribute
			for (Map.Entry<String, ArrayList<String>> entry : tweet_attribute_values
					.entrySet()) {
				String key = entry.getKey();
				ArrayList<String> values = entry.getValue();

				HashSet<String> values_set = new HashSet<String>();
				for (String s : values) {
					values_set.add(s);
				}

				int max_occurrences = 0;
				String max_attr_value = "";

				for (String v : values_set) {

					int occurrences = Collections.frequency(values, v);

					if (occurrences > max_occurrences) {
						max_attr_value = v;
						max_occurrences = occurrences;
					}
				}

				tweets_result_attrs.put(key, max_attr_value);
			}

			// System.out.println( tweets_result_attrs.toString());

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dbForTweets.closeDBhandle();
		return tweets_result_attrs;
	}

	public String getProbabilityOfTweetAttr(String handle, String attr,
			String val) {

		DBAdaptor dbForTweets = new DBAdaptor();
		float prob = 0.0f;
		String sql_total_count = "Select count(*) from tweet_structured_attribute where twitter_handle = '"
				+ handle + "' and structured_attribute = '" + attr + "'";
		String sql_for_val = "Select count(*) from tweet_structured_attribute where twitter_handle = '"
				+ handle
				+ "' and structured_attribute = '"
				+ attr
				+ "' and structured_attribute_value = '" + val + "'";
		try {
			dbForTweets.rs = dbForTweets.st.executeQuery(sql_total_count);
			int total = 0;
			int count = 0;
			if (dbForTweets.rs.next()) {
				total = dbForTweets.rs.getInt(1);
			}

			dbForTweets.rs = dbForTweets.st.executeQuery(sql_for_val);
			if (dbForTweets.rs.next()) {
				count = dbForTweets.rs.getInt(1);
			}

			prob = (float) count / (float) total;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		dbForTweets.closeDBhandle();
		return Float.toString(prob);

	}
}// end of class
