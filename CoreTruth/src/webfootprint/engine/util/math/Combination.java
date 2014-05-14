package webfootprint.engine.util.math;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

public class Combination {
	
	public static HashSet<HashSet<String>> combination(Set<String> elements, int length) {
		HashSet<HashSet<String>> combinations = new HashSet<HashSet<String>>();
		HashSet<HashSet<String>> temp = new HashSet<HashSet<String>>();
		for(Iterator<String> elementIterator = elements.iterator(); elementIterator.hasNext(); ) {
			HashSet<String> set = new HashSet<String>();
			set.add(elementIterator.next());
			temp.add(set);
		}
		
		recursive(combinations, temp, length);		
		return combinations;		
	}
	
	private static void recursive(HashSet<HashSet<String>>combinations, HashSet<HashSet<String>> temp, int featureScale) {
		if(featureScale == 1) {
			combinations.addAll(temp);
		}else {
			featureScale--;
			HashSet<HashSet<String>> newTemp = new HashSet<HashSet<String>>();
			for(Iterator<HashSet<String>> outerIterator = temp.iterator(); outerIterator.hasNext(); ) {
				
	            HashSet<String> x = outerIterator.next();
				for(Iterator<HashSet<String>> innerIterator = temp.iterator(); innerIterator.hasNext(); ) {
	                
	                HashSet<String> y = innerIterator.next();	               
	                HashSet<String> candidate = new HashSet<String>();
	                for(Iterator<String> stringIterator = x.iterator(); stringIterator.hasNext(); ) {
	                	candidate.add(stringIterator.next());
	                }
	                    
	                int numDifferent = 0;	
	                for(Iterator<String> stringIterator = y.iterator(); stringIterator.hasNext(); ) {
	                	String element = stringIterator.next();
	                	boolean found = false;
	                	if (candidate.contains(element)) { 
	                    		found = true;
	                    		break;
	                    }
	                	if (!found){
	                		numDifferent++;	                		
	                		candidate.add(element);
	                	}	            	
	            	}
	                  
	                if (numDifferent == 1) {
	                	newTemp.add(candidate);
	                }
	            }
	        }
			recursive(combinations, newTemp, featureScale);
		}
	}
	
	public static void main(String [] args) {
		
		HashSet<String> set = new HashSet<String>();
		set.add("1");
		set.add("2");
		set.add("3");
		set.add("4");
		set.add("5");
		Combination.combination(set, 2);
		
	}

}
