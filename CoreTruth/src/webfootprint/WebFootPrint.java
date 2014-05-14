package webfootprint;

import java.util.ArrayList;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

import webfootprint.engine.data.Constants;
import webfootprint.engine.data.Inference;
import webfootprint.engine.data.Predict;
import webfootprint.engine.data.Profile;
import webfootprint.engine.data.WebUser;
import webfootprint.engine.apriori.AprioriEngine;
import webfootprint.engine.util.db.Database;
import webfootprint.engine.Console;
import webfootprint.engine.util.db.Schema;
import webfootprint.engine.exception.AprioriException;

public class WebFootPrint {
	
	public void trainTest() throws SQLException, AprioriException, IOException, 
	InterruptedException, ClassNotFoundException  {
		String machine = "jdbc:postgresql://127.0.0.1/webfootprint";
	    String userid = "kevin";
	    //String passwd = "19600524";
	    String passwd = "kevin";
	    int dbType = 2;
			
		Console console = new Console(machine, userid, passwd, dbType, "gplus", "user_id", "attribute_name", "attribute_group",
				"attribute_value");
		ArrayList<String> attributes = new ArrayList<String>();
		//attributes.add("company_industry");
		//attributes.add("industry");
		//attributes.add("start_date_year");
		//attributes.add("start_date_month");
		//attributes.add("num_connections");
		//attributes.add("location_name");
		//attributes.add("skill_name");
		//attributes.add("title");
		attributes.add("relationshipstatus");
		attributes.add("gender");
		attributes.add("organizations_type");
		attributes.add("name_givenname");
		attributes.add("name_familyname");
		attributes.add("displayname");
		attributes.add("location_name");
		int[] engines = new int[1];
		engines[0] = Constants.ASSOCIATION_RULE_MINING;
		//engines[1] = Constants.NAIVE_BAYES;
		//engines[0] = Constants.LDA;
		console.train("gplus_data", Schema.getGplus(), attributes, engines);
	}
	
	public void LinkedInInferTest() throws AprioriException, SQLException, IOException, InterruptedException,
	ClassNotFoundException {
		String machine = "jdbc:postgresql://localhost/webfootprint"; 
	    String userid = "kevin";
	    //String passwd = "19600524";
	    String passwd = "kevin";
	    int dbType = 2;

		Console console = new Console(machine, userid, passwd, dbType, "linkedin", "user_id", "attribute_name", "attribute_group",
				"attribute_value");
		ArrayList<String> relyingAttributes = new ArrayList<String>();
		//relyingAttributes.add("name_givenname");
		//relyingAttributes.add("name_familyname");
		//relyingAttributes.add("relationshipstatus");
		//relyingAttributes.add("gender");
	
		relyingAttributes.add("industry");
		//relyingAttributes.add("start_date_year");
		
		int[] engines = new int[3];
		engines[0] = Constants.ASSOCIATION_RULE_MINING;
		engines[1] = Constants.NAIVE_BAYES;
		engines[2] = Constants.LDA;
		console.infer(Schema.getLinkedin(), null, engines, "core", "core", 19);

		System.out.println();
	}
	
	public void GplusInferTest() throws AprioriException, SQLException, IOException, InterruptedException,
	ClassNotFoundException {
		String machine = "jdbc:postgresql://localhost/webfootprint"; 
	    String userid = "kevin";
	    //String passwd = "19600524";
	    String passwd = "kevin";
	    int dbType = 2;

		Console console = new Console(machine, userid, passwd, dbType, "gplus", "user_id", "attribute_name", "attribute_group",
				"attribute_value");
		ArrayList<String> relyingAttributes = new ArrayList<String>();
		//relyingAttributes.add("name_givenname");
		//relyingAttributes.add("name_familyname");
		//relyingAttributes.add("relationshipstatus");
		//relyingAttributes.add("gender");
	
		relyingAttributes.add("industry");
		//relyingAttributes.add("start_date_year");
		int[] engines = new int[3];
		engines[0] = Constants.ASSOCIATION_RULE_MINING;
		engines[1] = Constants.NAIVE_BAYES;
		engines[2] = Constants.LDA;

		console.infer(Schema.getGplus(), null, engines, "core", "core", 19);

		System.out.println();
	}
	
//	public static void main(String[] args) throws SQLException, AprioriException, IOException,
//		ClassNotFoundException, InterruptedException {
//		long start = System.currentTimeMillis();
//		WebFootPrint test = new WebFootPrint();
//		test.LinkedInInferTest();
//		test.GplusInferTest();
//		long end = System.currentTimeMillis();
//		System.out.println("elapsed time " + (end - start));
//	}
	
	public static void mainFunction() throws SQLException, AprioriException, IOException,
	ClassNotFoundException, InterruptedException {
	
		System.out.println("In Yifang's code!");
		
		long start = System.currentTimeMillis();
		WebFootPrint test = new WebFootPrint();
		test.LinkedInInferTest();
		test.GplusInferTest();
		long end = System.currentTimeMillis();
		System.out.println("elapsed time " + (end - start));
	}
}
