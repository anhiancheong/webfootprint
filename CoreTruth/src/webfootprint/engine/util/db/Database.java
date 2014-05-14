package webfootprint.engine.util.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

import webfootprint.engine.util.Pair;
import webfootprint.engine.data.Constants;

import org.postgresql.util.PSQLException;


//@author Professor Lisa Singh, Ph.D. & Mitchell Beard

public class Database {
	
	Connection m_connection;
	
	String machine = "jdbc:postgresql://127.0.0.1:5432/webfootprint";
    String userid = "postgres";
    String passwd = "6O1zkV52";
    int dbType = 2;
    String activeRelation;
	
	public Database(String machine, String userid, String passwd, int dbType) {
		this.machine = machine;
		this.userid = userid;
		this.passwd = passwd;
		this.dbType = dbType;
	}
		
	/*************************************************************
	Connect to the database using machine name, userid, and password.
	**************************************************************/
	public Connection connect() throws SQLException {
		if(dbType == Constants.ORACLE_DB) {
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
		}
		else if(dbType == Constants.MYSQL_DB) {
			DriverManager.registerDriver(new com.mysql.jdbc.Driver());
		}
		else if(dbType == Constants.POSTGRESQL_DB) {
			DriverManager.registerDriver(new org.postgresql.Driver());
		}
		
		Connection conn = DriverManager.getConnection(machine, userid, passwd);
                             // @machineName:port:SID,   userid,  password
		m_connection = conn;
        return m_connection;
	}
	
	public void closeConnection() throws SQLException{
		m_connection.close();
	}
	
	public String getMachine() {
		return this.machine;
	}
	
	public String getUser() {
		return this.userid;
	}
	
	public String getPasswd() {
		return this.passwd;
	}
	
	public Connection getConnection() {
		return this.m_connection;
	}
	
	public String getActiveRelation() {
		return this.activeRelation;
	}
	
	public void setMachine(String machine) {
		this.machine = machine;
	}
	
	public void setUser(String user) {
		this.userid = user;
	}
	
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	
	public void setActiveRelation(String activeRelation) {
		this.activeRelation = activeRelation;
	}
	
	public void insert(String query) throws SQLException{
		
		Statement statement = m_connection.createStatement();
		statement.executeUpdate(query);
		statement.close();
	}
	
	public void insert(String relation, ArrayList<String> values) throws SQLException {
		String query = "insert into " + relation + " values( ";
		for(int i = 0; i < values.size(); i++) {
			query += "\'" + values.get(i) + "\', ";
		}
		query = query.substring(0, query.length() -2);
		query += " );";
		insert(query);
	}
	
	public ArrayList<ArrayList> constraintSelect(String query, Schema schema) throws SQLException {
		ArrayList<ArrayList> values = new ArrayList<ArrayList>();
		Statement stmt = m_connection.createStatement();
		ResultSet queryResult = stmt.executeQuery(query);
		queryResult.next();	
				
		while(!queryResult.isAfterLast()) {
			ArrayList<Object> value = new ArrayList<Object>();
			for(int i = 0; i < schema.size(); i++ ) {
				String name = schema.getColumn(i);				
				int type = schema.getDataType(i);
				value.add(getUserData(queryResult, type, name));
			}
			values.add(value);
			queryResult.next();
		}
		
		queryResult.close();
		stmt.close();	
		return values;		
	}
	
	public ArrayList<ArrayList> select(Schema targetColumn) throws SQLException {
		ArrayList<ArrayList> values = new ArrayList<ArrayList>();
		Statement stmt = m_connection.createStatement();
		String query = "select ";
		for(int i = 0; i < targetColumn.size(); i++) {
			query += targetColumn.getColumn(i) + ", ";
		}

		query = query.substring(0, query.length() - 2);
		query += " from " + this.activeRelation + ";";
		
		ResultSet queryResult = stmt.executeQuery(query);
		queryResult.next();		
				
		while(!queryResult.isAfterLast()) {
			ArrayList<Object> value = new ArrayList<Object>();
			for(int i = 0; i < targetColumn.size(); i++ ) {
				String name = targetColumn.getColumn(i);				
				int type = targetColumn.getDataType(i);
				value.add(getUserData(queryResult, type, name));
			}
			values.add(value);
			queryResult.next();
		}
		
		queryResult.close();
		stmt.close();	
		return values;		
	}
	
	public void insertBinary(File file) throws IOException, SQLException {
		Schema schema = Schema.getLDA();
		Statement stmt = m_connection.createStatement();
		String delete = "delete from " + Constants.LDA_MODEL_RELATION + " where " + schema.getColumn(0) + "=\'";
		delete += file + "\';";
		stmt.executeUpdate(delete);
		stmt.close();
		
		FileInputStream fis = new FileInputStream(file);
		PreparedStatement ps = m_connection.prepareStatement("insert into " + Constants.LDA_MODEL_RELATION + " values (?, ?)");
		ps.setString(1, file.getName());
		ps.setBinaryStream(2, fis, file.length());
		ps.executeUpdate();
	}
	
	public InputStream selectBinary(String file) throws SQLException, IOException {
		Statement stmt = m_connection.createStatement();
		Schema schema = Schema.getLDA();
		String query = "select " + schema.getColumn(1) + " from " + Constants.LDA_MODEL_RELATION + " where " + schema.getColumn(0) + "=\'";
		query += file + "\' ;";
		ResultSet queryResult = stmt.executeQuery(query);
		queryResult.next();		
		InputStream in = queryResult.getBinaryStream(schema.getColumn(1));
		
		queryResult.close();
		stmt.close();	
		return in;
	}
	
	public void createTable(String name, Schema schema, boolean replace) throws SQLException {
		String query = "create table " + name + " (";
		for(int i = 0; i < schema.size(); i++) {
			String column = schema.getColumn(i);				
			String psqlDatatype = schema.getPsqlDataType(i);
			query += column + " " + psqlDatatype + ", ";
		}
		
		query = query.substring(0, query.length() - 2);
		query += ");";
		
		Statement statement = m_connection.createStatement();
		try{
			statement.executeUpdate(query);
		}catch(PSQLException e) {
			if(replace) {
				String dropRelation = "drop table " + name + ";";
				statement.executeUpdate(dropRelation);
				statement.executeUpdate(query);
			}
		}
		statement.close();	
	}
	
	protected Object getUserData(ResultSet queryResult, int type, String colName) throws SQLException {
		
		Object datum = SQLTypeConverter.getJavaType(queryResult, type, colName);
		if(datum == null) {
			datum = new String("null");			
		}		
		return datum;
	}	
	
	protected String getUserData(ResultSet queryResult, Vector colTypes, Vector colNames) throws SQLException {
		
		String datum = "";		
		for (int i = 0; i < colNames.size(); i++) {
			datum = datum + "\t" + SQLTypeConverter.getJavaType(queryResult, ((Integer)colTypes.get(i)).intValue(), (String)colNames.get(i));
			if(datum == null) datum = new String("null");			
		}		
		return datum;
	}
	
	
	public static void main(String[] argv) throws Exception {
		
		String machine = "jdbc:postgresql://127.0.0.1:5432/postgres";
	    String userid = "postgres";
	    String passwd = "19600524";
	    int dbType = 2;
		Database database = new Database(machine, userid, passwd, dbType);
		database.connect();
		Schema schema = new Schema();
		schema.addColumn("rule", java.sql.Types.VARCHAR);
		schema.addColumn("confidence", java.sql.Types.REAL);
		database.createTable("linkedin_association_rule", schema, true);
		database.insert("insert into gplus values(\'first\', \'43.05\');");
		database.insert("insert into gplus values(\'second\', \'45.05\');");
		database.constraintSelect("select rule, confidence from gplus;", schema);
		
		database.closeConnection();
		System.out.println("success");		
	}	
}
