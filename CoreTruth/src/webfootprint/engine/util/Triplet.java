package webfootprint.engine.util;

public class Triplet<V, E, L> {
	
	V first;
	E second;
	L third;
	
	public Triplet(V first, E second, L third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}
	
	public V getFirst() {
		return this.first;
	}
	
	public E getSecond() {
		return this.second;
	}
	
	public L getThird() {
		return this.third;
	}
}
