package webfootprint.engine.util.db;

import java.sql.*;

public class SQLTypeConverter {
	
	public static Object castObjectFromString(String str, Class type) {
		Class[] paramTypes = {String.class};
		Object[] params = {str};
		
		try {
			return type.getConstructor(paramTypes).newInstance(params);
		}
		catch(Exception e) {
			return str;
		}
	}
	
	public static Object getJavaType(ResultSet rset, int sqlType, String columnName) throws SQLException {
		switch (sqlType)
		{
			// String cases
			//rset.getBinaryStream(columnLabel);
			case java.sql.Types.BINARY:
				return rset.getBinaryStream(columnName);
			case java.sql.Types.CHAR:
			case java.sql.Types.VARCHAR:
			case java.sql.Types.LONGVARCHAR:
			
			case java.sql.Types.VARBINARY:
			case java.sql.Types.LONGVARBINARY:
			case java.sql.Types.DATE:
			case java.sql.Types.TIME:
			case java.sql.Types.TIMESTAMP:
				return rset.getString(columnName);

			// Integer cases
			case java.sql.Types.TINYINT:
			case java.sql.Types.SMALLINT:
			case java.sql.Types.INTEGER:
			case java.sql.Types.BIT:
				return new Integer(rset.getInt(columnName));

			// Long Integer
			case java.sql.Types.BIGINT: 
				return new Long(rset.getLong(columnName));

			// Decimal numbers
			case java.sql.Types.REAL:
			case java.sql.Types.FLOAT:
			case java.sql.Types.DOUBLE:
			case java.sql.Types.NUMERIC:
			case java.sql.Types.DECIMAL:
				return new Double(rset.getDouble(columnName));
				
			default:
				return rset.getObject(columnName);
		}
	}
	
	public static String getPsqlTypeName(int sqlType) {
		switch (sqlType)
		{
			// String cases
			case java.sql.Types.CHAR:
				return "char";
			case java.sql.Types.VARCHAR:
				return "varchar";
			case java.sql.Types.LONGVARCHAR:
				return "longvarchar";
			case java.sql.Types.BINARY:
				return "bytea";
			case java.sql.Types.VARBINARY:
				return "varbinary";
			case java.sql.Types.LONGVARBINARY:
				return "longvarbinary";
			case java.sql.Types.DATE:
				return "date";
			case java.sql.Types.TIME:
				return "time";
			case java.sql.Types.TIMESTAMP:
				return "timestamp";

			// Integer cases
			case java.sql.Types.TINYINT:
				return "tinyint";
			case java.sql.Types.SMALLINT:
				return "smallint";
			case java.sql.Types.INTEGER:
				return "integer";
			case java.sql.Types.BIT:
				return "bit";

			// Long Integer
			case java.sql.Types.BIGINT: 
				return "bigint";

			// Decimal numbers
			case java.sql.Types.REAL:
				return "real";
			case java.sql.Types.FLOAT:
				return "float";
			case java.sql.Types.DOUBLE:
				return "real";
			case java.sql.Types.NUMERIC:
				return "real";
			case java.sql.Types.DECIMAL:
				return "real";
				
			default:
				return "varchar";
		}
	}
}
