package com.coretruth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.*;

import webfootprint.*;
import webfootprint.engine.exception.AprioriException;

public class GenerateCore {

	public String line = null;

	public String firstName = "";
	public String lastName = "";
	public int count = 0;

	public static void main(String[] args) throws ClassNotFoundException, SQLException, AprioriException, IOException, InterruptedException{
		
		//Setup a directory for experiment outputs
		File folder = new File("./inputExperiments");
		File[] listOfFiles = folder.listFiles();

		String inputFile = null;
		String outputFile = null;

		//New core, this will hold the initial values we know about a person
		GenerateCore obj = new GenerateCore();
		
		//Iterating through the files in the directory, for each one...
		for (File file : listOfFiles) {
			
			if (file.getName().contains(".txt")) {

				inputFile = "./inputExperiments/"+file.getName();
				
				
				//Bulk of work is being called from here
				obj.oldmain(inputFile);
				
				
				//copy finalcore into finalcore_Constexpid
				DBAdaptor control = new DBAdaptor();
				
				// ****** DEFINES A TABLE BASED ON RESULTS FROM A QUERY
				// Just creating a copy of the final core table to a NEW finalcore table
				//String copytable = "select * into finalcore_"+ Constants.experimentId + " from finalcore";
				//control.st.execute(copytable);
				
				//clears out the core table
				//String clearCore = "Delete from core";
				//control.st.executeUpdate(clearCore);
				
				//clears out the finalcore table... horrible idea
				//String clearFinalCore = "Delete from finalcore";
				//control.st.executeUpdate(clearFinalCore);
				
				//closes DB
				control.closeDBhandle();
				
				//why make a new db connection????
				DBAdaptor predictclose = new DBAdaptor("jdbc:postgresql://localhost/webfootprint",Constants.username, Constants.password);
				
				//clearing the predict table
			    String clearPredict = "Delete from predict";
				predictclose.st2.executeUpdate(clearPredict);
				//predictclose.st2.executeUpdate(clearCore);
				
				//predictclose.closeDBhandle2();
				

			}
		}
		
	}
	
	public void oldmain(String filename) throws SQLException,
			AprioriException, IOException, ClassNotFoundException,
			InterruptedException {

		// Read json file for configuration.
		try {
			Reader read = null;
			read = new FileReader(new File(filename));
			JSONTokener jsonReader = new JSONTokener(read);
			JSONObject params = new JSONObject(jsonReader);
			/*
			 * Reading in configuration information from the file, things like confidence, merging thresholds etc
			 * */
			System.out.println(params.toString(3));

			Constants.confidenceThreshold = (float) params
					.getDouble("confidenceThreshold_forYifang");
			
			Constants.aggregateThreshold = (float) params
					.getDouble("aggregateThreshold");
			
			Constants.individualWebsiteThreshold = (float) params
					.getDouble("individualWebsiteThreshold");
			
			Constants.experimentId = params.getInt("experimentId");
			
			Constants.considerLinkedinExtractedAttrs = params.getBoolean("considerLinkedinExtractedAttrs");
			
			//Getting which websites this experiment will seek to use
			JSONArray temp = params.getJSONArray("websites");
			ArrayList<String> websites = new ArrayList<String>();

			for (int i = 0; i < temp.length(); i++) {
				websites.add(temp.getString(i));
			}

			Constants.considerTweets = params.getBoolean("considerTweets");

			//Gets the attirbutes for the initial core
			ArrayList<String> initialCoreAttrs = new ArrayList<String>();
			JSONArray tempInit = params.getJSONArray("initialCoreAttributes");

			for (int i = 0; i < tempInit.length(); i++) {
				initialCoreAttrs.add(tempInit.getString(i));
			}

			
			
			/******* MAIN CALLS TO PROGRAM ********/
			
			GenerateCore obj = new GenerateCore();
			
			//Pass in the websites list and core attributes to form the initial core
			obj.fillCoreAndPredict(websites, initialCoreAttrs);

			//Create the prediction of new attributes
			// How do they connect????
			//Inference engine
			WebFootPrint.mainFunction(); 
			System.out.println("Predict table populated...!");

			//Uses the prediction table to populat the rest?
			obj.fillFinalCore(websites, initialCoreAttrs);

			/**************************************/
			
			
		} catch (IOException e) {
			e.printStackTrace();

		} catch (JSONException e) {
			System.err.println("Something is wrong with params.json file!");
			System.out
					.println("\nPlease check you added params in correct json format.");
		}
	}

	public void fillCoreAndPredict(ArrayList<String> websites,
			ArrayList<String> initialCoreAttrs) throws SQLException,
			AprioriException, IOException, ClassNotFoundException,
			InterruptedException {

		// Read first_name, last_name pair from database table gt_person
		DBAdaptor dbadap = new DBAdaptor();
		DBAdaptor dbadap2 = new DBAdaptor();
		HashMap<String, String> initialCoreKeyValues = new HashMap<String, String>();
		ArrayList<Integer> PIDs = new ArrayList<Integer> ();
		PIDs = this.readIDsFromFile();
//		System.out.println(PIDs.size());
		
		for (int k = 0; k < PIDs.size(); k++) {
			try {
				//try to find the specified person from the ground truth table
				dbadap.rs = dbadap.st
						.executeQuery("select person_id, first_name, last_name from gt_person where person_id = " + PIDs.get(k) ); // TODO REMOVE where clause Raise the limit later on and remove where

				//There should only be one person...if the query was person_id = X
				while (dbadap.rs.next()) {

					int person_id = dbadap.rs.getInt("person_id");
					String fname = dbadap.rs.getString("first_name").toLowerCase();
					
					String lname = dbadap.rs.getString("last_name").toLowerCase();
					//				fname = "dennis"; //  REMOVE
					//				lname = "yu"; //  REMOVE
					// initialCoreKeyValues.put("person_id",String.valueOf(person_id));
					
					initialCoreKeyValues.put("first_name", fname);
					initialCoreKeyValues.put("last_name", lname);
					System.out.println("\n/*****" + fname + " " + lname
							+ "*****/");

					/************************************************************************/

					// Go through initialcore attributes and get their values for
					// each person from gtperson_finalcore_assoc table

					String getProfileIds = "Select * from gtperson_finalcore_assoc where gt_person_id = "
							+ person_id;
					//				System.out.println("Get profile id: "+ getProfileIds);

					/*
					 * Getting the linkedin, Google+... social media profiles for the specified ground truth person
					 * There should really be a person class here...
					 * */
					dbadap2.rs = dbadap2.st.executeQuery(getProfileIds);
					HashMap<String, String> website_profile_ids = new HashMap<String, String>();
					String twitterHandle = "";
					
					if (dbadap2.rs.next()) {

						//Twitter case is different
						if (!dbadap2.rs.wasNull()) {
							twitterHandle = dbadap2.rs.getString("twitter_handle");
						}
						
						//get the associated id for each website
						for (int i = 0; i < websites.size(); i++) {
							String id = websites.get(i) + "_id";
							String tempId = dbadap2.rs.getString(id);

							if (!dbadap2.rs.wasNull()) {
								website_profile_ids.put(id, tempId);

							}
						}
					}

					//skip ids that don't have linkedin and googleplus profile handles yet
					if (website_profile_ids.size() < 2 && twitterHandle != "") {
						//					System.out.println("Website IDs: \n" + website_profile_ids.toString());
						continue;
					}

					//create a map entry for each key value pair in the website profile id list
					for (Map.Entry<String, String> entry : website_profile_ids
							.entrySet()) {
						//name of website
						String website_id = entry.getKey();
						//profile id/username
						String id = entry.getValue();
						//getting rid of the _id part of the website
						// google+_id -> google+
						String website_name = website_id.substring(0,
								website_id.length() - 3);
						
						//for all of the values in initial core (first_name, last_name, others)
						for (int j = 0; j < initialCoreAttrs.size(); j++) {
							String attr = initialCoreAttrs.get(j);
							
							//ignore the name attributes
							if (attr.equalsIgnoreCase("first_name")
									|| attr.equalsIgnoreCase("last_name")) {
								continue;
							} else {
								//query the table for the specific website and get each attribute that is in the table for it
								//ex: if location is in the set of core attributes that we have
								//we want to get that information from the website table (such as the location in g+ or linkedin)
								//
								String sql = "Select attribute_value from "
										+ website_name
										+ " where attribute_name = '" + attr
										+ "' and " + website_id + " = '" + id
										+ "' ";
								dbadap2.rs = dbadap2.st.executeQuery(sql);

								//add the results to the initial values list
								if (dbadap2.rs.next()) {
									String value = dbadap2.rs
											.getString("attribute_value");
									initialCoreKeyValues.put(attr, value);
								}

							}//end each value loop
						}//end each website loop
					}

					// Now call do work from here with initial core attributes key
					// value pairs
					// initialCoreKeyValues contains the key value pairs to be
					// passed onto next

					//				System.out.println(initialCoreKeyValues.toString());
					GenerateCoreActual genCore = new GenerateCoreActual();
					//				genCore.getTwitterAttributes("labusque");

					genCore.doWork(websites, initialCoreKeyValues, true,
							twitterHandle); // (websites,
					// initial
					// core
					// attributes,
					// dumpcore)

				}

				
				

			} catch (SQLException e) {
				e.printStackTrace();
			} //end the try for database errors block
		
		
		}//end users loop
		System.out.println("Core generation complete...!");
		dbadap.closeDBhandle();
		dbadap2.closeDBhandle();
		
		//initial core is now filled for a given person
		return;
	}

	public void fillFinalCore(ArrayList<String> websites,
			ArrayList<String> initialCoreAttrs) throws SQLException,
			AprioriException, IOException, ClassNotFoundException,
			InterruptedException {

		// Read first_name, last_name pair from database table gt_person
		DBAdaptor dbadap = new DBAdaptor();
		DBAdaptor dbadap2 = new DBAdaptor();
		HashMap<String, String> initialCoreKeyValues = new HashMap<String, String>();
		ArrayList<Integer> PIDs = new ArrayList<Integer> ();
		PIDs = this.readIDsFromFile();
		
		for (int k = 0; k < PIDs.size(); k++) {
			try {
				dbadap.rs = dbadap.st
						.executeQuery("select person_id, first_name, last_name from gt_person where person_id = "
								+ PIDs.get(k)); // TODO REMOVE Rasie the limit later on

				while (dbadap.rs.next()) {

					int person_id = dbadap.rs.getInt("person_id");
					String fname = dbadap.rs.getString("first_name")
							.toLowerCase();
					String lname = dbadap.rs.getString("last_name")
							.toLowerCase();
					//				fname = "dennis"; //  REMOVE
					//				lname = "yu"; //  REMOVE
					// initialCoreKeyValues.put("person_id",String.valueOf(person_id));
					initialCoreKeyValues.put("first_name", fname);
					initialCoreKeyValues.put("last_name", lname);
					// initialCoreKeyValues.put("first_name", "stephen");
					// initialCoreKeyValues.put("last_name", "lang");
					// System.out.println(fname + " " + lname);

					// Go through initialcore attributes and get their values for
					// each person from gt_person_attributes table

					// gtperson_finalcore_assoc
					// Go through initialcore attributes and get their values for
					// each person from gtperson_finalcore_assoc table

					String getProfileIds = "Select * from gtperson_finalcore_assoc where gt_person_id = "
							+ person_id;

					dbadap2.rs = dbadap2.st.executeQuery(getProfileIds);
					HashMap<String, String> website_profile_ids = new HashMap<String, String>();
					String twitterHandle = "";

					if (dbadap2.rs.next()) {
						twitterHandle = dbadap2.rs.getString("twitter_handle");
						for (int i = 0; i < websites.size(); i++) {
							String id = websites.get(i) + "_id";
							String tempId = dbadap2.rs.getString(id);

							if (!dbadap2.rs.wasNull()) {
								website_profile_ids.put(id, tempId);

							}
						}
					}

					//skip ids that don't have linkedin and googleplus profile handles yet
					if (website_profile_ids.size() < 2
							&& twitterHandle.isEmpty()) {
						continue;
					}

					for (Map.Entry<String, String> entry : website_profile_ids
							.entrySet()) {
						String website_id = entry.getKey();
						String id = entry.getValue();
						String website_name = website_id.substring(0,
								website_id.length() - 3);

						for (int j = 0; j < initialCoreAttrs.size(); j++) {
							String attr = initialCoreAttrs.get(j);
							if (attr.equalsIgnoreCase("first_name")
									|| attr.equalsIgnoreCase("last_name")) {
								continue;
							} else {
								String sql = "Select attribute_value from "
										+ website_name
										+ " where attribute_name = '" + attr
										+ "' and " + website_id + " = '" + id
										+ "' ";
								dbadap2.rs = dbadap2.st.executeQuery(sql);

								if (dbadap2.rs.next()) {
									String value = dbadap2.rs
											.getString("attribute_value");
									initialCoreKeyValues.put(attr, value);
								}

							}
						}
					}

					GenerateCoreActual genCore = new GenerateCoreActual();
					System.out.println("Calling genWork with false...!");

					genCore.doWork(websites, initialCoreKeyValues, false,
							twitterHandle);
				}

			
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		

		}
		
		System.out.println("FinalCore generation complete...!");
		dbadap.closeDBhandle();
		dbadap2.closeDBhandle();
		return;

	}
	
	
	
	/*
	 * This method will just read from a file where the format is id | first_name | last_name
	 * */
	public ArrayList<Integer> readIDsFromFile() throws IOException{
		
		InputStream    fis;
		BufferedReader br;
		String         line;

		fis = new FileInputStream("startingSet.txt");
		br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
		ArrayList<Integer> personIDs = new ArrayList<Integer>();
		while ((line = br.readLine()) != null) {
		    // Deal with the line
			String pid[] = new String[3];
			pid = line.split("\\|");
			String tpid = pid[0];
			personIDs.add(Integer.parseInt(tpid));
		}
		
		// Done with the file
		br.close();
		br = null;
		fis = null;
		
		return personIDs;
	}
	
	
}
// end of class