package webfootprint.engine.lda;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;
import java.util.regex.Pattern;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;
@Deprecated
public class OutLink {
	
	private final static int kNearestNeighbor = 10;
	private final static int kQuery = 3;
	private final static boolean targetFieldValueFile = false;
	private final static boolean confusionMatrix = true;
	
	public int[][] getPredict(HashMap<Integer, String> indexToValue, ArrayList<ArrayList<Integer>> indices, int neighborNumber) throws Exception {
		WikiOutLink wikiOutLink = new WikiOutLink();
		ArrayList<HashSet<String>> allOutLinks = new ArrayList<HashSet<String>>();
		for(int i = 0; i < indexToValue.size(); i++) {
			System.out.println(i + "th query running...");
			HashSet<String> outLinks = wikiOutLink.query(indexToValue.get(i));			
			allOutLinks.add(outLinks);
		}
		System.out.println("Computing outlink overlap...");		
		
		ArrayList<HashSet<String>> profileOutLinks = new ArrayList<HashSet<String>>();
		for(int i = 0; i < indices.size(); i++) {
			HashSet<String> profile = new HashSet<String>();
			for(int j = 0; j < indices.get(i).size(); j++) {
				profile.addAll(allOutLinks.get(indices.get(i).get(j)));
			}
			profileOutLinks.add(profile);
		}
		
		int[][] neighbors = getOutLinkModel(profileOutLinks, indices, neighborNumber);		
		return neighbors;		
	}
	

	private int[][] getOutLinkModel(ArrayList<HashSet<String>> profileOutLinks, ArrayList<ArrayList<Integer>> indices, int neighborNumber) {
		
		int[][] neighbor = new int[profileOutLinks.size()][neighborNumber];
		for(int i = 0; i < profileOutLinks.size(); i++) {
			double[] similarity = new double[profileOutLinks.size()]; 
			ArrayList<Integer> target = indices.get(i);
			for(int j = 0; j < profileOutLinks.size(); j++) {
				similarity[j] = overlap(profileOutLinks.get(i), profileOutLinks.get(j));
			}
			int[] order = sort(similarity);
			ArrayList<ArrayList<Integer>> included = new ArrayList<ArrayList<Integer>>();
			for(int j = 0, count = 0; j < order.length && count < neighborNumber; j++) {
				int predict = order[j];				
				if(contains(target, indices.get(predict)) && (contains(indices.get(predict), target))) {
					continue;
				}
				boolean include = false;
				for(int k = 0; k < included.size(); k++) {
					if(contains(included.get(k), indices.get(predict)) && contains(indices.get(predict), included.get(k))) {
						include = true;;
					}
				}
				if(include) {
					continue;
				}
				included.add(indices.get(predict));
				neighbor[i][count] = predict;
				count++;				
			}
		}		
		return neighbor;
	}	
	
	private boolean contains(ArrayList<Integer> index1, ArrayList<Integer>index2) {
		boolean predict = true;
		for(int i = 0; i < index2.size(); i++) {
			if(!index1.contains(index2.get(i))) {
				predict = false;
			}				
		}
		return predict;
	}
	
	private int[] sort(double[] value) {
		ArrayList<Item> list = new ArrayList<Item>();
		for(int i = 0; i < value.length; i++) {
			list.add(new Item(value[i], i));
		}
		Collections.sort(list, new comparator());
		int[] arrayInOrder = new int[value.length];
		for(int i = 0; i < value.length; i++) {
			arrayInOrder[i] = list.get(i).index;
		}
		return arrayInOrder;
	}
	
	private double overlap (HashSet<String> outLink1, HashSet<String> outLink2) {
		double overlap = 0.0;
		for(Iterator<String> stringIterator = outLink1.iterator(); stringIterator.hasNext(); ) {
			String link = stringIterator.next();
			if(outLink2.contains(link)) {
				overlap++;
			}
		}
		return overlap / (double)outLink1.size();
	}
	
	public void execute() throws Exception {

		BufferedReader in = new BufferedReader(new FileReader("gplus_given_name_gender_balance.txt"));
		String line = "";
		ArrayList<String> id = new ArrayList<String>();
		ArrayList<String> source = new ArrayList<String>();
		ArrayList<String> target = new ArrayList<String>();
		ArrayList<ArrayList<Integer>> indices = new ArrayList<ArrayList<Integer>>();
		HashMap<String, Integer> valueToIndex = new HashMap<String, Integer>();
		HashMap<Integer, String> indexToValue = new HashMap<Integer, String>();
		HashMap<String, Integer> labelToIndex = new HashMap<String, Integer>();
		HashMap<Integer, String> indexToLabel = new HashMap<Integer, String>();
		while((line = in.readLine()) != null) {
			
			line = line.replaceAll("[!@#$%^&*(),:.\\\\?,<>\"{};/\']+", "");
			line = line.replaceAll("\\]\\[", "");
			line = line.toLowerCase();
			String[] tokens = line.split("\\|");			
			String[] phrases = tokens[1].split(", ");
			int indicator = 0;
			ArrayList<Integer> user = new ArrayList<Integer>();
			for(int i = 0; i < phrases.length && indicator < kQuery ; i++, indicator++) {
				if(!valueToIndex.containsKey(phrases[i])) {
					valueToIndex.put(phrases[i], valueToIndex.size());
					indexToValue.put(valueToIndex.size() - 1, phrases[i]);
				}
				user.add(valueToIndex.get(phrases[i]));
			}
			indices.add(user);
			id.add(phrases[0]);
			source.add(tokens[1]);
			target.add(tokens[2]);
			if(!targetFieldValueFile) {
				if(!labelToIndex.containsKey(tokens[2]))
				labelToIndex.put(tokens[2], labelToIndex.size());
				indexToLabel.put(labelToIndex.size() - 1, tokens[2]);
			}
		}
		in.close();
		
		if(targetFieldValueFile) {
			in = new BufferedReader(new FileReader("state_list.txt"));			
			while((line = in.readLine()) != null) {
				indexToLabel.put(indexToLabel.size(), line.toLowerCase());
				labelToIndex.put(line.toLowerCase(), labelToIndex.size());
			}
			in.close();
		}
		
		int[][] neighbor = getPredict(indexToValue, indices, kNearestNeighbor);		
		
		BufferedWriter out = new BufferedWriter(new FileWriter("gplus_given_name_gender_1000_predict.txt"));
		for(int i = 0; i < neighbor.length; i++) {
			String guess = "";
			ArrayList<String> guesses = new ArrayList<String>();
			for(int j = 1; j < kNearestNeighbor; j++) {
				guess = guess + j + ": " + target.get(neighbor[i][j]) + " ";
				guesses.add(target.get(neighbor[i][j]));
			}
			out.write(i + "|CONTENT: " + source.get(i) + "|TRUE LABEL: " + target.get(i) + "|PREDICT: " + guess + "\n");
			out.flush();			
		}		
		out.close();
		if(confusionMatrix) {
			confusionMatrix(labelToIndex, neighbor, target);
		}
	}
	
	private void confusionMatrix(HashMap<String, Integer> labelToIndex, int[][] predict, ArrayList<String> target) throws Exception{
		int[][] confusionMatrix = new int[labelToIndex.size()][labelToIndex.size()];
		for(int i = 0; i < confusionMatrix.length; i++) {
			for(int j = 0; j < confusionMatrix[i].length; j++) {
				confusionMatrix[i][j] = 0;
			}
		}		
		
		BufferedWriter out = new BufferedWriter(new FileWriter("gplus_given_name_gender_1000_predict.txt", true));
		for(int i = 0; i < predict.length; i++) {
			ArrayList<String> guesses = new ArrayList<String>();
			for(int j = 1; j < kNearestNeighbor; j++) {
				guesses.add(target.get(predict[i][j]));
			}
			
			String vote = majorityVote(guesses);			
			String trueLabel = target.get(i);			
			int row = labelToIndex.get(trueLabel);
			int column = labelToIndex.get(vote);
			try{
				confusionMatrix[row][column] = confusionMatrix[row][column] + 1;
			}
	        catch(Exception e) {
	        	System.out.println();
	        }
		}
		
		Double correctLabel = 0.0;
		for(int i = 0; i < confusionMatrix.length; i++) {
			correctLabel = correctLabel + confusionMatrix[i][i];
		}
		correctLabel = correctLabel / target.size();
		System.out.println("accuracy: " + correctLabel);
		String[] tableHead = new String[labelToIndex.size()];
		for(Iterator<String> stringIterator = labelToIndex.keySet().iterator(); stringIterator.hasNext(); ) {
			String key = stringIterator.next();
			tableHead[labelToIndex.get(key)] = key;
		}
		for(int i = 0; i < confusionMatrix.length; i++) {
			if(i == 0) {
				for(int j = 0; j < confusionMatrix[i].length; j++) {
					out.write("\t" + tableHead[j]);
					out.flush();
				}
				out.write("\n");				
			}
			for(int j = 0; j < confusionMatrix[i].length; j++) {
				if(j == 0 ) {
					out.write(tableHead[i]);
				}
				out.write("\t" + confusionMatrix[i][j]);
			}
			out.write("\n");
			out.flush();
		}
		out.close();		
	}
	
	private String majorityVote(ArrayList<String> guesses) {
		HashMap<String, Integer> accumulate = new HashMap<String, Integer>();
		for(int i = 0; i < guesses.size(); i++) {
			if (accumulate.containsKey(guesses.get(i))) {
				accumulate.put(guesses.get(i), accumulate.get(guesses.get(i)) + 1);
			} else {
				accumulate.put(guesses.get(i), 1);
			}
		}		
		
		Integer temp = 0;
		String result = "";
		for(Iterator<String> stringIterator = accumulate.keySet().iterator(); stringIterator.hasNext();) {
			String key = stringIterator.next();
			if (accumulate.get(key) >= temp) {
				temp = accumulate.get(key);				
				result = key;
			}
		}
		return result;		
	}	
		
	public class Item {
		public double similarity;
		public int index;
		public Item(double similarity, int index) {
			this.similarity = similarity;
			this.index = index;			
		}		
	}
	
	private class comparator implements Comparator<Item> {
		public int compare(Item t1, Item t2){
			return Double.compare(t2.similarity,t1.similarity);			
		}
	}	

	public static void main(String[] args) throws Exception {
		
		OutLink outLink = new OutLink();
		long time1 = System.currentTimeMillis();
	    outLink.execute();
	    long time2 = System.currentTimeMillis();
	    System.out.println("time: " + (time2 - time1));

	}
}