package newCore;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import webfootprint.WebFootPrint;
import webfootprint.engine.exception.AprioriException;

import com.coretruth.Constants;


public class PsqlQuery {

	
	String currentQuery = "";
	ResultSet currentResultSet = null;
	Connection conn = null;
	Statement curStmt = null;
	String username = "kevin";
	String password = "kevin";
	
	public PsqlQuery(){
		
		Connection conn = null;
		try {
			Class.forName("org.postgresql.Driver"); //load the driver
            conn = DriverManager.getConnection("jdbc:postgresql://localhost/webfootprint_result?user="+username+"&password="+password+"");
            curStmt = conn.createStatement();
		}
		catch(ClassNotFoundException ce){
			ce.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**A one parameter constructor that will take in the name of another database to use*/
	public PsqlQuery(String db) {
		// TODO Auto-generated constructor stub
		Connection conn = null;
		try {
			Class.forName("org.postgresql.Driver"); //load the driver
            conn = DriverManager.getConnection("jdbc:postgresql://localhost/webfootprint?user="+username+"&password="+password+"");
            curStmt = conn.createStatement();
		}
		catch(ClassNotFoundException ce){
			ce.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**This method will return a HashMap of entries with the attributes of the person that have the highest probability
	 * ex it may return an array list with {<education, phd>,<location, new york>} where phd and new york have the highest probabilty
	 * of all the values for that attribute*/
	private HashMap<String, String> getHighestPKeyValueSet(HashMap<String,CoreAttribute> attributes) {
		// TODO Auto-generated method stub
		
		HashMap<String, String> retMap = new HashMap<String, String>();
		for(String key : attributes.keySet()){
			CoreAttribute currentAttrCollection = attributes.get(key);
			
			//Put the value of the maximum probabilty into the set
			retMap.put(currentAttrCollection.getAttributeName(), currentAttrCollection.getMaxValStr());
		}
		
		return retMap;
	}
	
	
	/**This method will query a specific table for the valid ids of the people in that table who
	 * match the last name, first name and other found attributes for that person
	 * @return 
	 * */
	public ResultSet makeIDQuery(String last_name, String first_name,
			String curWebsite, HashMap<String,CoreAttribute> attributes, boolean initialOnly) {
		// TODO Auto-generated method stub
		
		HashMap<String, String> attributeKeySet = getHighestPKeyValueSet(attributes);
		String curWebsiteID = curWebsite + "_id";
		String finalQuery = "";
		
		/*Look at the table, find*/
		String nameQuery = "(SELECT " + curWebsiteID + " from " + curWebsite + " WHERE attribute_name = 'last_name' " +
				           " and attribute_value = '" + last_name.toLowerCase() + "') INTERSECT (SELECT " + curWebsiteID + " from " + curWebsite + " WHERE attribute_name = 'first_name' " +
				           " and attribute_value = '" + first_name.toLowerCase() + "')";
		
		
		String attributesIntersectsQuery = "";
		
		for(String attr : attributeKeySet.keySet()){
			
			if(attr == "first_name" || attr == "last_name")
			{continue;}
			
			
			//If the core only uses initial values
			//Then if the value we're looking at is not an initial value
			//I dont want to include it in the queries
			if(initialOnly){
				if(!attributes.get(attr).isInitialValue){
					continue;
				}
			}
			attributesIntersectsQuery += " INTERSECT (SELECT " + curWebsiteID + " FROM " + curWebsite + " WHERE attribute_name LIKE '" +
			                             attr + "' AND attribute_value LIKE '" +attributes.get(attr)+ "')";
		}
		
		
		finalQuery = nameQuery + attributesIntersectsQuery;
		DebugOutput.print(finalQuery);
		
		currentQuery = finalQuery;
		execute();
		return currentResultSet;
		
	}

	public void execute() {
		// TODO Auto-generated method stub
		
		try {
			curStmt.executeQuery(currentQuery);
			currentResultSet = curStmt.getResultSet();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

	public HashMap<String, ArrayList<String>> makeWebsiteAttrQuery(String curWebsite, ResultSet idQueryResults) {
		// TODO Auto-generated method stub
		
		String curWebsiteIDName = curWebsite + "_id";
		HashMap<String, ArrayList<String>> websiteAttr;
		String anyArrayQuery = "SELECT * from " + curWebsite +" where " + curWebsiteIDName + "= ANY ('{";
		
		try{
			
			while (idQueryResults.next()) {
				if(idQueryResults.getString(curWebsiteIDName) != null) {
					anyArrayQuery += idQueryResults.getString(curWebsiteIDName) + ",";
				}
			}
			
			//trim last comma off the array portion of the query
			if(anyArrayQuery.lastIndexOf(",") != -1){
				anyArrayQuery = anyArrayQuery.substring(0, anyArrayQuery.lastIndexOf(","));
			}
			anyArrayQuery += "}' :: varchar[]);";
			/*SELECT * from googleplus where googleplus_id = ANY ('{100000074573021313575, 100000480922975211925}' :: varchar[]);*/
		}
		catch(SQLException sqle) {
			sqle.printStackTrace();
		}
		
		
		
		//EXCUTE!!!
		currentQuery = anyArrayQuery;
		execute();
		
		
		
		
		// Convert the result set into an array of simpleEntry
		websiteAttr = new HashMap<String, ArrayList<String>>();
		try {
			
			while(currentResultSet.next()){
				
				String attrName = currentResultSet.getString("attribute_name");
				String attrVal = currentResultSet.getString("attribute_value");
				
				if(!websiteAttr.containsKey(attrName)){
					
					websiteAttr.put(attrName, new ArrayList<String>());
					
				}
				
				websiteAttr.get(attrName).add(attrVal);
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return websiteAttr;
		
	}

	public ResultSet getResultSet() {
		// TODO Auto-generated method stub
		return currentResultSet;
	}
	
	public void close(){
		try {
			if(conn != null) {
				conn.close();
			}
			if(curStmt != null) {
				curStmt.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**Method will create a query to get the gt_person_id of a given person (first and last name)
	 * Will return -1 if not found*/
	public int getGTID(String first_name, String last_name) {
		// TODO Auto-generated method stub
		int gtID = -1;
		
		String gtIDSql = "SELECT person_id from gt_person WHERE first_name = '"+first_name+"' AND last_name = '"+last_name+"'";
		
		setQuery(gtIDSql);
		execute();
		
		try {
			currentResultSet.next();
			gtID = (int) currentResultSet.getInt("person_id");
		} 
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		
		
		return gtID;
	}

	private void setQuery(String SqlStr) {
		// TODO Auto-generated method stub
		currentQuery = SqlStr;
	}

	//Queries the given website table for entries with the associated gt_person_id
	//will make an array of key-value pairs of the attribute names and values and return
	//them
	public ArrayList<SimpleEntry<String, String>> makeAttrQueryByGTID(String curWeb, int gtID) {
		// TODO Auto-generated method stub
		String attrQuery = "SELECT * from "+curWeb+" where gtperson_id = '" + gtID + "';";
		
		DebugOutput.print(attrQuery);
		
		setQuery(attrQuery);
		execute();
		
		ArrayList<SimpleEntry<String, String>> websiteAttr = new ArrayList<SimpleEntry<String,String>>();
		
		try{
			
			while(currentResultSet.next()){
				
				String attrName = currentResultSet.getString("attribute_name");
				String attrVal = currentResultSet.getString("attribute_value");
				
				websiteAttr.add(new SimpleEntry<String, String>(attrName, attrVal));
			}
			
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		
		return websiteAttr;//(SimpleEntry<String, String>[]) websiteAttr.toArray();
	}

	
	/** This method will look at the core table in webfootprint_result and get the id of the person this core is looking at*/
	public String getPopEngineID(String first_name, String last_name) {
		// TODO Auto-generated method stub
		String returnID = "-1";
		String idQuery = "select user_id from core where attribute_name = 'name_givenname' and attribute_value = '"
			+ first_name
			+ "' INTERSECT (select user_id from core where attribute_name = 'name_familyname' and attribute_value = '"
			+ last_name + "')";
		
		try{
			currentQuery = idQuery;
			execute();
			
			currentResultSet.next();
			returnID = currentResultSet.getString("user_id");
		}
		catch(SQLException s){
			s.printStackTrace();
		}
		
		return returnID;
	}

	/**
	 * This method will access the 'webfootprint' database and query the 'predict' table for 
	 * new attributes for that person
	 * */
	public ArrayList<AttributeInstance> getPopAttr(String userIDForYifangDB, double confidenceThreshold) {
		// TODO Auto-generated method stub
		
		DebugOutput.print("Getting population inference engine results");
		ArrayList<AttributeInstance> returnInstances = new ArrayList<AttributeInstance>();
		
		try {
			WebFootPrint.mainFunction();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		String getPredict = "Select * from predict where user_id = '"
			+ userIDForYifangDB + "' and confidence > "
			+ confidenceThreshold;
		
		currentQuery = getPredict;
		
		DebugOutput.print("Predict Table query is: " + currentQuery);
		
		execute();
		
		try {
			
			while(currentResultSet.next()){
			
				String attrName = currentResultSet.getString("attribute_name");
				String attrValue = currentResultSet.getString("attribute_value");
				String attrGroup = currentResultSet.getString("attribute_group");
				double confidence = currentResultSet.getDouble("confidence");
				String algorithm = currentResultSet.getString("algorithm");
				
				returnInstances.add(new AttributeInstance(attrName, attrValue, attrGroup, confidence, algorithm));
				
				//STORE THE ATTRIBUTES!!!!!!
				DebugOutput.print("Iterating over prediction attributes - " + attrName);
				
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return returnInstances;
	}


	
	
}
