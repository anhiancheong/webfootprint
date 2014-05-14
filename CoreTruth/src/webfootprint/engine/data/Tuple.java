package webfootprint.engine.data;

public class Tuple<O> {
	
	DefaultUserData userData;
	O object;
	
	public Tuple(O object) {
		this.userData = new DefaultUserData();
		this.object = object;
	}
	
	public DefaultUserData getUserData() {
		return this.userData;
	}
	
	public void setObject(O object) {
		this.object = object;
	}
	
	public O getObject() {
		return this.object;
	}
	
}
