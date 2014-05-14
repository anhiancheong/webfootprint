package webfootprint.engine.data;

public class Predict extends Tuple {
	
	int algorithm;
	
	public Predict(String answer) {
		this(answer, -1);
	}
	
	public Predict(String answer, int algorithm) {
		super(answer);
		this.algorithm = algorithm;
	}
	
	public String getAnswer() {
		return (String)super.getObject();
	}
	
	public void setAnswer(String answer) {
		super.setObject(answer);
	}
	
	public int getAlgorithm() {
		return this.algorithm;
	}
	
}
