package com.coretruth;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

public class Core {

	LinkedHashMap<String, String> attribute_cleaner = new LinkedHashMap<String, String>();
	LinkedHashMap<String, String> yifangToCorwin = new LinkedHashMap<String, String>();
	// It will store the attribute_name, attribute_value which is above
	// probability
	LinkedHashMap<String, String> core = new LinkedHashMap<String, String>();
	LinkedHashMap<String, String> probability = new LinkedHashMap<String, String>();
	LinkedHashMap<String, String> confidence = new LinkedHashMap<String, String>();
	LinkedHashMap<String, String> entropy = new LinkedHashMap<String, String>();
	LinkedHashMap<String, String> notCore = new LinkedHashMap<String, String>();
	LinkedHashMap<String, String> notCoreProbability = new LinkedHashMap<String, String>();
	LinkedHashMap<String, String> count = new LinkedHashMap<String, String>();

	String user_id = "";
	ArrayList<String> originals = new ArrayList<String>();

	String source_website = "";
	int exp_id;
	DBAdaptor dbadaptor = null;
	
/*	location_name
	name_givenname
	name_familyname
	organizations_type
	skill_name
	industry
	company_industry
	num_connections
	relationshipStatus
	gender
	*/	
	
	/*
	 * url
	image_url
	email
	first_name
	middle_name
	last_name
	gender
	relationship_status
	location
	language
	country
	state
	city
	street
	birthday
	age_min
	age_max
	occupation
	company
	education
	industry
	linkedin_headline
	twitter_id
	google_plus_id
	linkedin_id
	foursquare_id
	twitter_account
	*/

	public Core() {
		attribute_cleaner.put("relationship_status", "relationshipstatus");
		attribute_cleaner.put("location", "location_name");
		attribute_cleaner.put("address", "location_name");
		attribute_cleaner.put("first_name", "name_givenname");
		attribute_cleaner.put("last_name", "name_familyname");
		attribute_cleaner.put("gender", "gender");
		attribute_cleaner.put("company", "company_industry");
		attribute_cleaner.put("occupation", "industry");

		yifangToCorwin.put("relationshipstatus", "relationship_status");
		yifangToCorwin.put("location_name", "location");
		yifangToCorwin.put("name_givenname", "first_name");
		yifangToCorwin.put("name_familyname", "last_name");
		yifangToCorwin.put("gender", "gender");
		yifangToCorwin.put("company_industry", "company");
		yifangToCorwin.put("industry", "occupation");
	}

	public String getCoreValuesAsString() {

		Set<String> keys = core.keySet();
		String coreValues = "";

		for (String k : keys) {
			coreValues = coreValues + k + ": " + core.get(k) + ": "
					+ probability.get(k) + ", ";
			// System.out.println(k+" -- "+lhm2.get(k));
		}

		return coreValues;
	}

	public void addToCore(String attribute_name, String attribute_value,
			String prob) {

		String temp = "";
		if (yifangToCorwin.containsKey(attribute_name)) {
			temp = yifangToCorwin.get(attribute_name);

//			System.out.println("******CLEANED ATTR*******" + temp);
		} else
			temp = attribute_name;

		core.put(temp, attribute_value);
		probability.put(temp, prob.toString());

		float probfloat = Float.parseFloat(prob);
		double doubleProbfloat = Double.parseDouble(prob);

		// float ent = (float)(probfloat * Math.log(probfloat));
		float ent = (float) (doubleProbfloat * Math.log(doubleProbfloat));

		// System.out.println("probfloat value : -------------------------------------------  "+probfloat);

		String tempval = "" + String.valueOf(ent);
		// System.out.println("Entropy value for " +temp +
		// ": -------------------------------------------    "+tempval);
		entropy.put(temp, tempval);

		// System.out.println("DSFKLJDSKFLJDSLKF: "+entropy.get(temp));
	}

	public void addToNonCore(String attribute_name, String attribute_value,
			String prob) {

		String temp = "";
		if (yifangToCorwin.containsKey(attribute_name)) {
			temp = yifangToCorwin.get(attribute_name);

//			System.out.println("******CLEANED ATTR*******" + temp);
		} else
			temp = attribute_name;

		notCore.put(temp, attribute_value);
		notCoreProbability.put(temp, prob.toString());
	}

	public void addCountVal(String website, String count) {
		this.count.put(website, count);
	}

	public void dumpCore() {
//		System.out.println("\n\nDumping core\n\n");
		// Get database handle

		try {
			dbadaptor = new DBAdaptor(); 
			DBAdaptor db2old = new DBAdaptor("jdbc:postgresql://localhost/webfootprint",Constants.username,Constants.password); 
			// check if user already in core
			String getID = "select user_id from core where attribute_name = 'first_name' and attribute_value = '"
					+ core.get("first_name")
					+ "' INTERSECT (select user_id from core where attribute_name = 'last_name' and attribute_value = '"
					+ core.get("last_name") + "')";
			dbadaptor.rs = dbadaptor.st.executeQuery(getID);
			if (dbadaptor.rs.next()) {
				user_id = dbadaptor.rs.getString(1);
//				System.out.println(getID + "\nFound user id in core:" + user_id);

				return;
			}

			dbadaptor.rs = dbadaptor.st
					.executeQuery("Select nextval('core_user_id_seq')");
			while (dbadaptor.rs.next()) {
				user_id = dbadaptor.rs.getString(1);
			}
			Set<String> keys = core.keySet();
			String initial = "Insert into core(exp_id, user_id, attribute_name, attribute_value) values ";
			String sql = initial;
			for (String k : keys) {
				// Change the key k

				sql = sql + "(" + this.exp_id
						+ ", '"+ user_id +"','" + k + "','"
						+ core.get(k) + "'),";

				// probability.get(k);
			}
			sql = sql.substring(0, sql.length() - 1);
//			 System.out.println("CORE STRING" + sql);
			dbadaptor.st.executeUpdate(sql);
			
			//Before this, change the attribute names to Yifangs format
			String sql_yifang = initial;
			String te = "";
			for (String k : keys) {
				// Change the key k
				if (attribute_cleaner.containsKey(k)) {
					te = attribute_cleaner.get(k);

//					System.out.println("******CLEANED ATTR*******" + temp);
				} else
					te= k;

				sql_yifang = sql_yifang + "(" + this.exp_id
						+ ", '"+ user_id +"','" + te + "','"
						+ core.get(k) + "'),";

				// probability.get(k);
			}
			sql_yifang = sql_yifang.substring(0, sql_yifang.length() - 1);
			
			db2old.st2.executeUpdate(sql_yifang);
			dbadaptor.closeDBhandle();
			db2old.closeDBhandle2();

		} catch (SQLException ex) {
			System.out.println("ERROR : " + ex.getMessage());
		}
	}

	public void getPrediction() {
		// get all the attributes from prediction table with confidence greater
		// than threshold

		try {
			dbadaptor = new DBAdaptor();

			String getID = "select user_id from core where attribute_name = 'first_name' and attribute_value = '"
					+ core.get("first_name")
					+ "' INTERSECT (select user_id from core where attribute_name = 'last_name' and attribute_value = '"
					+ core.get("last_name") + "')";
			dbadaptor.rs = dbadaptor.st.executeQuery(getID);
			if (dbadaptor.rs.next()) {
				user_id = dbadaptor.rs.getString(1);
//				System.out.println(getID + "\nFound user id in core:" + user_id);

			}
			String getPredict = "Select * from predict where user_id = '"
					+ user_id + "' and confidence > "
					+ Constants.confidenceThreshold;
			// get result set and then for each attribute check if it is in the
			// core variable and then process
//			System.out.println(getPredict);
			String attribute_name, attribute_value, algorithm;
			Float local_confidence;
			
			DBAdaptor dbadp2 = new DBAdaptor(
					"jdbc:postgresql://127.0.0.1/webfootprint", 
					Constants.username, Constants.password);
			dbadp2.rs2 = dbadp2.st2.executeQuery(getPredict);

			while (dbadp2.rs2.next()) {
				attribute_name = dbadp2.rs2.getString("attribute_name");
				attribute_value = dbadp2.rs2.getString("attribute_value").toLowerCase();
				local_confidence = dbadp2.rs2.getFloat("confidence");
				algorithm = dbadp2.rs2.getString("algorithm").toLowerCase();

//				System.out.println("attribute_name: " + attribute_name);
				String temps = "";
				if (yifangToCorwin.containsKey(attribute_name)) {
					temps = yifangToCorwin.get(attribute_name);

//					System.out.println("******CLEANED ATTR*******" + temp);
				} else
					temps= attribute_name;
//				System.out.println("Cleaned attribute_name: " + temps);
				
//				System.out.println(core.keySet().toString());
				
				if (core.containsKey(temps)) { //comment this, because yifanys value can get added
//					System.out.println("##################################Has key!!!+ attribute_value =  "+ attribute_value);
					String value = probability.get(temps);

					if(local_confidence > Float.parseFloat(value)){
						probability.put(temps, local_confidence.toString());
					}
					
//						Float temp = (Float.parseFloat(value) + local_confidence) / 2;

//						System.out.print("old p value: " + value + "New p Value = " + temp + "\n");

					//	probability.put(attribute_name, temp.toString());
						confidence.put(temps,local_confidence.toString());
				}

				else if(local_confidence > Constants.confidenceThreshold){
					core.put(temps, attribute_value);
					probability.put(temps, local_confidence.toString());
					confidence.put(temps,local_confidence.toString());
				}
				else{
					//don't give a damn to prediction
				}
				// predicted attribute is same as core and if it has the same
				// value

			}
			dbadaptor.closeDBhandle();
			dbadp2.closeDBhandle2();

		} catch (SQLException ex) {
			System.out.println("ERROR : " + ex.getMessage());
		}
	}

	public void dumpFinalCore() {

		// dump core with attribute name, value and probability and semantic
		// score
		// dump notCore attribute name, value , if p and
		// exposure score = core.sum(probability)
		// count per site for that name
		// semantic score(entropy)

		try {
			// dbadaptor = new
			// DBAdaptor("jdbc:postgresql://127.0.0.1:2000/webfootprint","postgres","6O1zkV52");
			DBAdaptor localdbadaptor = new DBAdaptor();
			// Get the new confidence values
			String uid = user_id;

			// for core
			Set<String> keys = core.keySet();
			String initial = "Insert into finalcore(row_id, attribute_type, attribute_name, attribute_value, probability, entropy, experiment_id) values ";
			String sql = initial;

			for (String k : keys) {
				// Change the key k
				
				String newkey = k;

				sql = sql + "('" + uid + " ','core','" + newkey + "','"
						+ core.get(k) + "','" + probability.get(k).toString()
						+ "','" + entropy.get(k) + "'," + Constants.experimentId + "),";

				// probability.get(k);
			}
			sql = sql.substring(0, sql.length() - 1);
//			System.out.println("SQL for core :" + sql);
			localdbadaptor.st.executeUpdate(sql);

			// for confidences
			Set<String> keysConfidence = confidence.keySet();
			String initialConfidence = "Insert into finalcore(row_id, attribute_type, attribute_name, attribute_value, probability, entropy, experiment_id) values ";
			String sqlConfidence = initialConfidence;

			if (!keysConfidence.isEmpty()) {
				for (String newkey : keysConfidence) {
					// Change the key k			

					sqlConfidence = sqlConfidence + "('" + uid
							+ " ','notcore','" + newkey + "','"
							+ "confidence" + "','"
							+ confidence.get(newkey).toString() + "',NULL," +Constants.experimentId + "),";

					// probability.get(k);
				}
//				System.out.println("SQLConfidence :" + sqlConfidence);
				sqlConfidence = sqlConfidence.substring(0, sqlConfidence.length() - 1);
				localdbadaptor.st.executeUpdate(sqlConfidence);
			}
			// For exposure score
			String exposureScore = this.calculateExpoScore();
			String abc = "Insert into finalcore(row_id, attribute_type, attribute_name, attribute_value, probability, entropy, experiment_id) values ('"
					+ user_id
					+ "','notcore','exposure','"
					+ exposureScore
					+ "','','',"+ Constants.experimentId+")";
			// System.out.println(abc);
			localdbadaptor.st.executeUpdate(abc);

			// for nonCore
			keys = notCore.keySet();
//			System.out.println("NonCore attributes");
//			System.out.println(keys.toString());
			String initial2 = "Insert into finalcore(row_id, attribute_type, attribute_name, attribute_value, probability, entropy, experiment_id) values ";
			String sql2 = initial2;

			for (String newkey : keys) {
				// Change the key k
				

				sql2 = sql2 + "('" + user_id + "','notcore','" + newkey + "','"
						+ notCore.get(newkey).replace("'", "\\'") + "','" + notCoreProbability.get(newkey)
						+ "',''," + Constants.experimentId +"),";

			}
			sql2 = sql2.substring(0, sql2.length() - 1);

//			System.out.println("SQL2: " + sql2);
			if (!keys.isEmpty())
				localdbadaptor.st.executeUpdate(sql2);

			// For count
			Set<String> keyys = this.count.keySet();
			System.out.println("Count attributes");
			System.out.println(keyys.toString());
			String initial4 = "Insert into finalcore(row_id, attribute_type, attribute_name, attribute_value, probability, entropy, experiment_id) values ";
			String sql4 = initial4;

			for (String k : keyys) {
				// Change the key k
				sql4 = sql4 + "('" + user_id + "','websiteCount','" + k + "','"
						+ count.get(k) + "','','',"+ Constants.experimentId +"),";

			}
			sql4 = sql4.substring(0, sql4.length() - 1);

//			System.out.println("SQL4 : " + sql4);

			localdbadaptor.st.executeUpdate(sql4);

			// calculate entropy for each attribute in core and non core

			localdbadaptor.closeDBhandle();

		} catch (SQLException ex) {
			System.out.println("ERROR : " + ex.getMessage());
		}
	}

	public String calculateExpoScore() {

		Set<String> keys = notCoreProbability.keySet();

		float expof = 0.0f;
		for (String k : keys) {
			expof = expof + Float.parseFloat(notCoreProbability.get(k));
			// System.out.println(k+" -- "+lhm2.get(k));
		}
		String expo = Float.toString(expof);
		return expo;

	}

	public void addCount(String website, int count2) {
		String c = Integer.toString(count2);
		count.put(website, c);
		// System.out.println("----------Added count"+count.get(website));
		Set<String> ck = count.keySet();
		// System.out.println(ck.toString());
		for (String k : ck) {
			// System.out.println(k +"   " +count.get(k));
		}

	}

	public void printAll() {

		Set<String> keys = probability.keySet();
		System.out.println("Core attributes");
		for (String k : keys) {
			System.out.println(k + " " + core.get(k) + " " + probability.get(k)
					+ "  " + entropy.get(k)

			);
		}

		keys = count.keySet();
		// System.out.println("|NonCore attributes");
		for (String k : keys) {
			// System.out.println(k +"   " +count.get(k));
		}

		keys = notCore.keySet();
		System.out.println("NonCore attributes");
		for (String k : keys) {
			System.out.println(k + " " + " " + notCore.get(k) + " "

			);
		}

	}
}
