package webfootprint.engine.util.db;

import java.util.*;

import webfootprint.engine.util.Pair;

public class Schema implements GenericSchema {
	
	ArrayList<Pair> columnTypes;
	final static Schema LINKEDIN = new Schema();
	final static Schema GPLUS = new Schema();
	final static Schema LDA_MODEL = new Schema();
	final static Schema ASSOCIATION_RULE = new Schema();
	final static Schema PREDICTS = new Schema();
	HashMap<String, Integer> index;
	
	public Schema(ArrayList<Pair> columnTypes) {
		this.columnTypes = columnTypes;
		this.index = new HashMap<String, Integer>();
	}
	
	public Schema() {
		this(new ArrayList<Pair>());
	}
	
	public static Schema getLinkedin() {
		LINKEDIN.addColumn("user_id", java.sql.Types.VARCHAR);
		LINKEDIN.addColumn("attribute_name", java.sql.Types.VARCHAR);
		LINKEDIN.addColumn("attribute_group", java.sql.Types.VARCHAR);
		LINKEDIN.addColumn("attribute_value", java.sql.Types.VARCHAR);
		return LINKEDIN;
	}
	
	public static Schema getGplus() {
		GPLUS.addColumn("user_id", java.sql.Types.VARCHAR);
		GPLUS.addColumn("attribute_name", java.sql.Types.VARCHAR);
		GPLUS.addColumn("attribute_group", java.sql.Types.VARCHAR);
		GPLUS.addColumn("attribute_value", java.sql.Types.VARCHAR);
		return GPLUS;
	}
	
	public static Schema getLDA() {
		LDA_MODEL.addColumn("name", java.sql.Types.VARCHAR);
		LDA_MODEL.addColumn("model", java.sql.Types.BINARY);
		return LDA_MODEL;
	}
	
	public static Schema getAssociationRule() {
		ASSOCIATION_RULE.addColumn("antecedent", java.sql.Types.VARCHAR);
		ASSOCIATION_RULE.addColumn("consequent", java.sql.Types.VARCHAR);
		ASSOCIATION_RULE.addColumn("confidence", java.sql.Types.REAL);
		return ASSOCIATION_RULE;
	}
	
	public static Schema getPredict() {		
		PREDICTS.addColumn("user_id", java.sql.Types.VARCHAR);
		PREDICTS.addColumn("attribute_name", java.sql.Types.VARCHAR);
		PREDICTS.addColumn("attribute_group", java.sql.Types.VARCHAR);
		PREDICTS.addColumn("attribute_value", java.sql.Types.VARCHAR);
		PREDICTS.addColumn("confidence", java.sql.Types.REAL);
		PREDICTS.addColumn("algorithm", java.sql.Types.VARCHAR);
		return PREDICTS;
		
	}
	
	public boolean addColumn(String column, int dataType) {
		int index = -1;
		for(int i = 0; i < columnTypes.size(); i++) {
			if(((String)columnTypes.get(i).getFirst()).equals(column)) {
				index = i;
				break;
			}
		}
		
		if(index < 0) {
			columnTypes.add(new Pair(column, dataType));
			this.index.put(column, new Integer(this.index.size()));
			return true;
		} else {
			return false;
		}
	}
	
	public int getIndex(String column) {
		return this.index.get(column).intValue();
	}
	
	public boolean removeColumn(String column) {
		int index = -1;
		for(int i = 0; i < columnTypes.size(); i++) {
			if(((String)columnTypes.get(i).getFirst()).equals(column)) {
				index = i;
				break;
			}
		}
		if(index >= 0) {
			columnTypes.remove(index);
			return true;
		} else {
			return false;
		}
	}
	
	public int size() {
		return columnTypes.size();
	}
	
	public String getColumn(int index) {
		return (String)columnTypes.get(index).getFirst();
	}
	
	public int getDataType(int index) {
		return ((Integer)columnTypes.get(index).getSecond()).intValue();
	}
	
	public String getPsqlDataType(int index) {
		int dataType = ((Integer)columnTypes.get(index).getSecond()).intValue();
		return SQLTypeConverter.getPsqlTypeName(dataType);
	}
	
	public static void main(String[] args) {
		Schema schema1 =Schema.getLinkedin();
		Schema schema2 = Schema.getGplus();
		System.out.println();
	}
	
}
