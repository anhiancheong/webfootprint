package webfootprint.engine.util.db;

import webfootprint.engine.util.Pair;

public interface GenericSchema {
	
	public boolean addColumn(String column, int dataType);
	
	public boolean removeColumn(String column);
	
	public String getColumn(int index);
	
	public int getDataType(int index);
	
	public String getPsqlDataType(int index);
	
	public int size();
	
}
