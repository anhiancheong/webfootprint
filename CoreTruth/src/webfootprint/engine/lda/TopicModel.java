package webfootprint.engine.lda;

import java.util.*;
import java.util.regex.Pattern;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

import webfootprint.engine.data.Constants;
import webfootprint.engine.data.Profile;
import webfootprint.engine.data.Truth;
import webfootprint.engine.util.db.Database;
import webfootprint.engine.util.db.Schema;

public class TopicModel {
	
	private String name;
	private ArrayList<String> valueList;
	private HashSet<String> valueSet;
	private HashMap<String, double[]> valueMap;
	private ArrayList<String> categoryList;
	private Database database;
	private String site;
	private String relation;
	private Schema schema;
	private int topicNum;
	private String file;
	private TopicInferencer restoredInference;
	private ArrayList<Pipe> pipeList;
	
	public TopicModel(String name, Database database, String site, int defaultTopicNum) {
		this.name = name;
		this.database = database;
		this.site = site;
		valueList = new ArrayList<String>();
		valueSet = new HashSet<String>();
		valueMap = new HashMap<String, double[]>();
		categoryList = new ArrayList<String>();
		relation = Constants.LDA_RELATION_PREFIX + site + "_" + name;
		file = Constants.LDA_RELATION_PREFIX + site + "_" + name;
		schema = new Schema();
		schema.addColumn("value", java.sql.Types.VARCHAR);
		schema.addColumn("vector", java.sql.Types.VARCHAR);
		topicNum = defaultTopicNum;
	}
	
	public void train(ArrayList<Profile> trainingProfiles) throws InterruptedException, IOException, SQLException {
		for(int i = 0; i < trainingProfiles.size(); i++) {
			Profile profile = trainingProfiles.get(i);
			ArrayList<String> attributes = profile.getAttributes();
			for(int j = 0; j < attributes.size(); j++) {
				String attribute = attributes.get(j);
				if(attribute.equals(this.name)) {
					ArrayList values = profile.getAttributeValue(name);
					for(int k = 0; k < values.size(); k++) {
						String answer = ((Truth)values.get(k)).getAnswer();
						addValue(answer);
					}
				}
			}
		}
		Wikipedia wiki = new Wikipedia(new WikipediaSetting());
		for(int i = 0; i < valueList.size(); i++) {
			String value = valueList.get(i);
			System.out.println(i + "th query running...");
			ArrayList<String> categories = wiki.query(value);
			if (categories.size() == 0) {
				removeValue(value);
				i--;
				continue;
			}
			StringBuffer buffer = new StringBuffer();
			for(int j = 0; j < categories.size(); j++) {
				buffer.append(categories.get(j) + " ");
			}
			String categoriesStr = buffer.toString();
			categoriesStr = categoriesStr.substring(0, categoriesStr.length() - 1);
			categoryList.add(categoriesStr);
		}
		trainTopicModel(categoryList);
	}
	
	private void trainTopicModel(ArrayList<String> contents) throws IOException, SQLException {
		constructPipes();
		InstanceList instances = new InstanceList (new SerialPipes(pipeList));
		String content = "";
		for(int i = 0; i < contents.size(); i++) {
			content = content + "instance" + i + " X " + contents.get(i) + "\n";
		}		 
		
		InputStream is = new ByteArrayInputStream(content.getBytes());
		instances.addThruPipe(new CsvIterator (new InputStreamReader(is) , Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
											   3, 2, 1)); // data, label, name fields
		
		if(contents.size() < topicNum) {
			topicNum = contents.size();
		}
		ParallelTopicModel model = new ParallelTopicModel(topicNum);
		model.addInstances(instances);

		model.setNumThreads(2);
		model.setNumIterations(1000);
		model.estimate();
		File objectFile = new File(file);
		if(objectFile.exists()) {
			objectFile.delete();
		}
		model.writeInferencer(new ObjectOutputStream(new FileOutputStream(new File(file))));
		database.insertBinary(new File(file));
		objectFile = new File(file);
		if(objectFile.exists()) {
			objectFile.delete();
		}
		
		for(int i = 0; i < contents.size(); i++) {
			double[] vector = model.getTopicProbabilities(i);
			valueMap.put(valueList.get(i), vector);		
		}	
	}	
	
	public boolean addValue(String value) {
		if(!valueSet.contains(value)) {
			valueList.add(value);
			valueSet.add(value);
			return true;
		}
		return false;
	}
	
	public boolean containsValue(String value) {
		return valueSet.contains(value);
	}
	
	public boolean removeValue(String value) {
		int index = -1;
		for(int i = 0; i < valueList.size(); i++) {
			if(valueList.get(i).equals(value)) {
				index = i;
				break;
			}
		}
		if(index >= 0) {
			valueSet.remove(value);
			valueList.remove(index);
			return true;
		}
		return false;
	}
	
	public boolean addVector(String value, double[] vector) {
		addValue(value);
		if(!valueMap.containsKey(value)) {
			valueMap.put(value, vector);
			return true;
		}
		return false;
	}
	
	public double[] getVector(String value) {
		if(valueMap.containsKey(value)) {
			return valueMap.get(value);
		}
		return null;
	}
	
	public void writeIntoDb() throws SQLException {
		database.setActiveRelation(relation);
		database.createTable(relation, schema, true);
		for(int i = 0; i < valueList.size(); i++) {
			String value = valueList.get(i);
			double[] vector = valueMap.get(value);
			StringBuffer buffer = new StringBuffer();
			for(int j = 0; j < vector.length; j++) {
				buffer.append(String.valueOf(vector[j]) + "|");
			}
			String vectorStr = buffer.toString();
			vectorStr = vectorStr.substring(0, vectorStr.length() - 1);
			ArrayList<String> array = new ArrayList<String>();
			array.add(value);
			array.add(vectorStr);
			database.insert(relation, array);
		}
	}
		
	public void readFromDb() throws SQLException {
		database.setActiveRelation(relation);
		ArrayList<ArrayList> arrays = database.select(schema);
		for(int i = 0; i < arrays.size(); i++) {
			ArrayList row = arrays.get(i);
			String value = (String)row.get(0);
			String[] vectorStr = ((String)row.get(1)).split("\\|");
			double[] vector = new double[vectorStr.length];
			for(int j = 0; j < vector.length; j++) {
				vector[j] = Double.valueOf(vectorStr[j]);
			}
			addVector(value, vector);
		}
	}
	
	public void readTopicModel() throws SQLException, IOException, ClassNotFoundException {
		ParallelTopicModel lda = new ParallelTopicModel(topicNum);
		InputStream in = database.selectBinary(file);
		restoredInference = lda.readInferencer(new ObjectInputStream(in));	
		readFromDb();
	}
	
	public double[] trainComingProfile(String value) throws IOException, InterruptedException, ClassNotFoundException {
		Wikipedia wiki = new Wikipedia(new WikipediaSetting());
		ArrayList<String> categories = wiki.query(value);
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < categories.size(); i++) {
			buffer.append(categories.get(i) + " ");
		}
		String categoriesStr = buffer.toString();
		String text = "AP881218-0001   X       " + categoriesStr;
		constructPipes();
		InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(text.getBytes()));
		InstanceList instances = new InstanceList(new SerialPipes(pipeList));
		instances.addThruPipe(new CsvIterator (reader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
											   3, 2, 1)); 
		Instance instance = instances.iterator().next();
		return restoredInference.getSampledDistribution(instance, 200, 100, 100);
	}
	
	private void constructPipes() {
		if(pipeList == null) {
			pipeList = new ArrayList<Pipe>();
			// Pipes: lowercase, tokenize, remove stopwords, map to features
			pipeList.add( new CharSequenceLowercase());
			pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
			pipeList.add( new TokenSequenceRemoveStopwords(new File("stoplists/en.txt"), "UTF-8", false, false, false));
			pipeList.add( new TokenSequence2FeatureSequence());
		}
	}
	
	public int getVectorLength() {
		return valueMap.entrySet().iterator().next().getValue().length;
	}
	
}
