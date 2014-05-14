package webfootprint.engine.util.db;

import java.util.Vector;
import java.sql.*;


public class DatabaseUtils
{
	public static Vector getTableNames(Connection conn) throws SQLException
	{
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery("SELECT table_name FROM user_tables");
		Vector colData = getColumnData("table_name", rset);
		rset.close();
		stmt.close();
		
		return colData;
	}
	
	public static Vector getTableAttributeNames(String table, Connection conn) throws SQLException
	{
		String query = "SELECT column_name, nullable FROM user_tab_columns WHERE table_name = '" + table + "'";
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(query);
		Vector colData = getColumnData("column_name", rset);
		rset.close();
		stmt.close();
		
		return colData;
	}
	
	public static Vector getQueryAttributeNames(String query, Connection conn) throws SQLException
	{
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(query);
		ResultSetMetaData rsmd = rset.getMetaData();

		Vector v = new Vector();
		for(int i = 1; i <= rsmd.getColumnCount(); i++)
			v.addElement(rsmd.getColumnName(i));
		
		rset.close();
		stmt.close();
		
		return v;
	}
	
	public static Vector getColumnData(String column, ResultSet rset) throws SQLException {
		
		Vector data = new Vector();
		
		rset.next();
		
		while(!rset.isAfterLast()) {
			data.add(rset.getObject(column));
			rset.next();
		}
		
		return data;
	}
	
	public static int getTableSize(String table, Connection conn) throws SQLException
	{
		String query = "SELECT COUNT(1) FROM " + table;		
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(query);

		rset.next();
		int size = rset.getInt(1);

		rset.close();
		stmt.close();
		
		return size;
	}
	
	public static int getQuerySize(String query, Connection conn) throws SQLException
	{
		String fullQuery = "SELECT COUNT(1) FROM (" + query + ")";		
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(fullQuery);

		rset.next();
		int size = rset.getInt(1);

		rset.close();
		stmt.close();
		
		return size;
	}
	
	public static Vector getColumnNames(ResultSetMetaData rsmd) throws SQLException {
		Vector names = new Vector();
		for (int i=1; i <= rsmd.getColumnCount(); i++)
			names.addElement(new String(rsmd.getColumnName(i)));
		
		return names;
	}
	
	public static Vector getColumnTypes(ResultSetMetaData rsmd) throws SQLException {
		Vector types = new Vector();
		for (int i=1; i <= rsmd.getColumnCount(); i++)
			types.addElement(new Integer(rsmd.getColumnType(i)));
		
		return types;
	}
	
	public static String[] vectorToStrings(Vector v) {
		String[] s = new String[v.size()];
		
		for(int i = 0; i < s.length; i++)
			s[i] = (String)v.get(i);
		
		return s;
	}
}
