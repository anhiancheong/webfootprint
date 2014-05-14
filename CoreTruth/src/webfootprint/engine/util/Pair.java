package webfootprint.engine.util;

public class Pair<V, E> {
	
	V first;
	E second;
	
	public Pair(V first, E second) {
		this.first = first;
		this.second = second;		
	}
	
	public V getFirst() {
		return this.first;
	}
	
	public E getSecond() {
		return this.second;
	}
}
