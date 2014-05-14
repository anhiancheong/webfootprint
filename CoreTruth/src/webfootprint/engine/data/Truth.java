package webfootprint.engine.data;

public class Truth extends Tuple {
	
	String group;
	
	public Truth(String answer, String group) {
		super(answer);
		this.group = group;
	}
	
	public String getAnswer() {
		return (String)super.getObject();
	}
	
	public void setAnswer(String answer) {
		super.setObject(answer);
	}
	
	public String getGroup() {
		return this.group;
	}
	
	public Truth clone() {
		String answer = new String(getAnswer());
		String group = new String(getGroup());
		return new Truth(answer, group);
	}
		
}
