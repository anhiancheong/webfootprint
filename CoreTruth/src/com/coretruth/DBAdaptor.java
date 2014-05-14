package com.coretruth;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;


public class DBAdaptor {

	Connection con = null;
    Statement st = null;
    ResultSet rs = null;
    
	Connection con2 = null;
    Statement st2 = null;
    ResultSet rs2 = null;
    
    public DBAdaptor(){
    	
	    String url = "jdbc:postgresql://localhost/webfootprint_result"; //
	    String user = Constants.username;
	    String password = Constants.password;
	    
	    
	    try{
	    	
	    	con = DriverManager.getConnection(url, user, password);
	        st = con.createStatement();
	        
	    }catch(SQLException ex){
	    	System.out.println("ERROR : "+ex.getMessage());
	    }
	    
    }
    
    public DBAdaptor(String url, String user, String password){
		try {

			con2 = DriverManager.getConnection(url, user, password);
			st2 = con2.createStatement();

		} catch (SQLException ex) {
			System.out.println("ERROR : " + ex.getMessage());
		}
	}
    
    public ResultSet getUserIds(String tablename, Core core){
    	
    	try{
    		  
    		LinkedHashMap<String,String> abc = core.core;
			Set<String> keys2 = abc.keySet();
				
			String attribute_name;
			String attribute_value;
			
			String initial = "SELECT "+ tablename+"_id FROM "+tablename+" where ";
			String add ="";
			String part =initial;
			
			
			int intersects = keys2.size() - 1;
			
			for (String k : keys2) {
					// each iteration is for individual attribute
			
					attribute_name = k;
					attribute_value = abc.get(k).replace("'", "");
					
					add = "attribute_name = '" + attribute_name.toLowerCase()  + "' and attribute_value = '" + attribute_value.toLowerCase() + "' ";
					
					part = part + add;
					if(intersects > 0){
						part = part + " INTERSECT " + initial;
						intersects--;
					}
			
			}
			
			String sql = part;
		
			
//			System.out.println("Get user IDs _____: "+sql);	
    	
    		
    		rs = st.executeQuery(sql);
    		
    	}catch(SQLException ex){
	    	System.out.println("ERROR : "+ex.getMessage());
	    }
    	//This will get the result set from database
    	
    	return rs;
    }
    
    public void createDataView(String tablename, ArrayList<String> userids){
    	
    	try{
    		String idsin = "(";
    		for(int i = 0 ; i < userids.size(); i++){
    			idsin += "'" + userids.get(i)+ "',";
    		}
    		
    		idsin = idsin.substring(0, idsin.length()-1) + ")";
    		
    		String sql = "SELECT * FROM "+tablename+" where "+tablename +"_id IN "+idsin; //.replace("[", "(").replace("]", ")"); 
    		
    		/****************************/
    		 //For linkedin_structure attribute
    		if(Constants.considerLinkedinExtractedAttrs && tablename.equalsIgnoreCase("linkedin")){
    			String union = "UNION SELECT attribute_id,structured_attribute,structured_attribute_value,linkedin_id FROM linkedin_structured_attribute where linkedin_id IN " + idsin;
    			sql += union;
    		}
   	



    		 /***************************/
    		
    		
    		
    		
    		
    		
    		String createView = "CREATE OR REPLACE view "+tablename+"_view AS ";
    		
//    		System.out.println("DHFDHFD  " + createView+sql);
    		String ql = createView + sql;
    		boolean i = st.execute(ql);

    		
    	}catch(SQLException ex){
	    	System.out.println("ERROR in createDataView : "+ex.getMessage());
	    }
    	//This will get the result set from database
    	
    	return;
    }
    
    public ResultSet getDataFromView(String tablename){
		
		try {
			rs = st.executeQuery("Select * from "+tablename+"_view");
			
			int count = 0;
			while (rs.next()) {
				rs.getString(1);
				count++;
			}
//			System.out.println(count);
			
		} catch (SQLException ex) {

		}
	
		return rs;
    }
    
    public ArrayList<String> getAttributeNames(String tablename){
    	
    	ArrayList<String> attributes = new ArrayList<String>();
    	try {
    		rs = st.executeQuery("Select distinct attribute_name from "+tablename+"_view");
    		
    		while(rs.next()){
    			attributes.add(rs.getString(1));
    		}
    		
    	}
    	catch(SQLException ex){
	    	System.out.println("ERROR  : "+ex.getMessage());
    	}
    	return attributes;
    }
    
    public ArrayList<String> getValuesForAttribute(String tablename,String attribute_name){
    	
    	ArrayList<String> attribute_values = new ArrayList<String>();
    	String sql = "Select attribute_value from "+tablename+"_view where attribute_name = '"+attribute_name+"'";
    	
    	try {
    		rs = st.executeQuery(sql);
    		
    		while(rs.next()){
    			attribute_values.add(rs.getString(1));
    		}
    		
    	}
    	catch(SQLException ex){
	    	System.out.println("ERROR  : "+ex.getMessage());
    	}
    	
    	return attribute_values;
	}
    
	public void closeDBhandle() {

		try {
			st.close();
			//rs.close();
			con.close();
		} catch (SQLException ex) {
			System.out.println("ERROR : " + ex.getMessage());
		}

	}
	
	public void closeDBhandle2() {

		try {
			st2.close();
			//rs.close();
			con2.close();
		} catch (SQLException ex) {
			System.out.println("ERROR : " + ex.getMessage());
		}

	}
}
