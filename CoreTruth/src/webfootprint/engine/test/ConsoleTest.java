package webfootprint.engine.test;

import java.util.ArrayList;
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

public class ConsoleTest {
	
	public void trainTest() throws AprioriException, SQLException {
		String machine = "jdbc:postgresql://127.0.0.1:5432/postgres";
	    String userid = "postgres";
	    String passwd = "19600524";
	    int dbType = 2;
			
		Console console = new Console(machine, userid, passwd, dbType, "linkedin", "user_id", "attribute_name", "attribute_group",
				"attribute_value");
		ArrayList<String> attributes = new ArrayList<String>();
		attributes.add("company/industry");
		attributes.add("industry");
		int[] engines = new int[1];
		engines[0] = Constants.ASSOCIATION_RULE_MINING;
		//console.train("linkedin_data", Schema.getLinkedin(), attributes, engines);
	}
	
	public void inferTest() throws AprioriException, SQLException {
		String machine = "jdbc:postgresql://127.0.0.1:5432/postgres";
	    String userid = "postgres";
	    String passwd = "19600524";
	    int dbType = 2;
		
		Console console = new Console(machine, userid, passwd, dbType, "linkedin", "user_id", "attribute_name", "attribute_group",
				"attribute_value");
		ArrayList<String> attributes = new ArrayList<String>();
		attributes.add("industry");
		int[] engines = new int[1];
		engines[0] = Constants.ASSOCIATION_RULE_MINING;
		//console.infer("linkedin_data", Schema.getLinkedin(), attributes, engines, targets);
		System.out.println();
	}
	
	public static void main(String[] args) throws SQLException, AprioriException {
		ConsoleTest test = new ConsoleTest();
		test.inferTest();
		
	}
}
