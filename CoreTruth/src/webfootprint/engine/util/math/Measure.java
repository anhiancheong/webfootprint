package webfootprint.engine.util.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

import java.io.BufferedWriter;
import java.io.IOException;

public class Measure {
	
	public static double chiSquareDistance(double[] vector1, double[] vector2) {
		double nominator = 0.0;
		double denominator = 0.0;
		for(int i = 0; i < vector1.length; i++) {
			nominator = nominator + (vector1[i] - vector2[i]) * (vector1[i] - vector2[i]);
			denominator = denominator + (vector1[i] + vector2[i]);
		}
		return nominator / denominator;
	}
	
	public static double chiSquareDistance(Double[] vector1, Double[] vector2) {
		try{
		double nominator = 0.0;
		double denominator = 0.0;
		for(int i = 0; i < vector1.length; i++) {
			nominator = nominator + (vector1[i] - vector2[i]) * (vector1[i] - vector2[i]);
			denominator = denominator + (vector1[i] + vector2[i]);
		}
		return nominator / denominator;
		}catch(Exception e) {
			return 0.0;
		}
	}
	
	public static double cosin(double[] vector1, double[] vector2) {
		double dotProduct12 = 0.0;
		double dotProduct1 = 0.0;
		double dotProduct2 = 0.0;
		for(int i = 0; i < vector1.length; i++) {
			dotProduct12 = dotProduct12 + vector1[i] * vector2[i];
			dotProduct1 = dotProduct1 + vector1[i] * vector1[i];
			dotProduct2 = dotProduct2 + vector2[i] * vector2[i];
		}
		if(dotProduct1 == 0.0 || dotProduct2 == 0.0) {
			return 0.0;
		} else {
			return dotProduct12 / (Math.sqrt(dotProduct1 * dotProduct2));
		}
	}
	
	public static double dStat(double[] vector1, double[] vector2) {
		double[] residual = linearRegression(vector1, vector2);
		double squareSum = 0.0;
		double differenceSquareSum = 0.0;
		for(int i = 0; i < residual.length - 1; i++) {
			differenceSquareSum = differenceSquareSum + (residual[i] - residual[i + 1]) * (residual[i] - residual[i + 1]);
			squareSum = squareSum + residual[i] * residual[i];
		}
		squareSum = squareSum + residual[residual.length - 1] * residual[residual.length - 1];
		return differenceSquareSum / squareSum;
		
	}
	
	public static double[] linearRegression(double[] x, double[] y) {
		double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
        for(int i = 0; i < x.length; i++) {
            sumx = x[i] + sumx;
            sumx2 = x[i] * x[i] + sumx2;
            sumy = y[i] + sumy;
        }
        double xbar = sumx / x.length;
        double ybar = sumy / y.length;

        // second pass: compute summary statistics
        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (int i = 0; i < x.length; i++) {
            xxbar += (x[i] - xbar) * (x[i] - xbar);
            yybar += (y[i] - ybar) * (y[i] - ybar);
            xybar += (x[i] - xbar) * (y[i] - ybar);
        }
        double beta1 = xybar / xxbar;
        double beta0 = ybar - beta1 * xbar;

        // print results
        //System.out.println("y   = " + beta1 + " * x + " + beta0);

        // analyze results
        double[] residual = new double[x.length];
        for (int i = 0; i < residual.length; i++) {
            double fit = beta1 * x[i] + beta0;
            residual[i] = fit - y[i];            
        }
        return residual;
	}
	
	public static boolean zeroCheck(double[] array) {
		for(int i = 0; i < array.length; i++) {
			if(array[i] != 0.0) {
				return false;
			}
		}
		return true;
	}
	
	
	public static ArrayList<Item> ascendingSort(double[] value) {
		ArrayList<Item> list = new ArrayList<Item>();
		for(int i = 0; i < value.length; i++) {
			list.add(new Item(value[i], i));
		}
		Collections.sort(list, new ascendingComparator());
		return list;
	}
	
	public static ArrayList<Item> descendingSort(double[] value) {
		ArrayList<Item> list = new ArrayList<Item>();
		for(int i = 0; i < value.length; i++) {
			list.add(new Item(value[i], i));
		}
		Collections.sort(list, new descendingComparator());
		return list;
	}	
	
	public static class Item {
		public double value;
		public int index;
		
		public Item(double similarity, int index) {
			this.value = similarity;
			this.index = index;			
		}
		
		public int getIndex() {
			return index;
		}
		
		public double getValue() {
			return value;
		}		
	}
	
	public static class ItemIndex {
		public double value;
		public String index;
		
		public ItemIndex(double value, String index) {
			this.value = value;
			this.index = index;			
		}
		
		public String getIndex() {
			return index;
		}
		
		public double getValue() {
			return value;
		}		
	}
	
	private static class ascendingComparator implements Comparator<Item> {
		public int compare(Item t1, Item t2){
			return Double.compare(t1.getValue(), t2.getValue());			
		}
	}
	
	private static class descendingComparator implements Comparator<Item> {
		public int compare(Item t1, Item t2){
			return Double.compare(t2.getValue(), t1.getValue());			
		}
	}
	
	public static void main(String[] args) {
		double[] vector1 = new double[4];
		vector1[0] = 0.264;
		vector1[1] = 0.735;
		vector1[2] = 0.925;
		vector1[3] = 0.074;
		double[] vector2 = new double[4];
		vector2[0] = 0.264;
		vector2[1] = 0.735;
		vector2[2] = 0.466;
		vector2[3] = 0.533;
		
		System.out.println(Measure.chiSquareDistance(vector1, vector2));
	}	
}
