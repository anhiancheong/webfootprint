package webfootprint.engine.util.math;

public class Counter {
	
	int upBound;
	int[] digitUpBound;
	int[] digits;
	int value;
	
	public Counter(int[] digitUpBound) {
		this.digitUpBound = digitUpBound;
		this.digits = new int[digitUpBound.length];
		for(int i = 0; i < digitUpBound.length; i++) {
			digits[i] = 0;
		}
		
		this.upBound = 1;
		for(int i = 0; i < digitUpBound.length; i++) {
			this.upBound *= digitUpBound[i];
		}		
		value = -1;
	}
	
	public int[] next() {
		value++;
		int temp = value;
		for(int i = 0; i < digits.length; i++) {
			int denominator = denominator(i + 1);
			int position = digits.length - (i + 1);
			digits[position] = temp / denominator;
			temp -= digits[position] * denominator;  
		}
		return this.digits;		
	}
	
	private int denominator(int length) {
		int denominator = 1;
		for(int i = 0; i < this.digits.length - length; i++) {
			denominator *= digitUpBound[i];
		}
		return denominator;
	}
	
	public boolean hasNext() {
		if(value < upBound - 1) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void main(String[] args) {
		int[] digits = new int[4];
		digits[0] = 4;
		digits[1] = 3;
		digits[2] = 3;
		digits[3] = 2;
		Counter counter = new Counter(digits);
		while(counter.hasNext()) {
			int[] value = counter.next();
			for(int i = 0; i < value.length; i++) {
				System.out.print(value[i] + " ");
			}
			System.out.print("\n");
		}		
	}
}