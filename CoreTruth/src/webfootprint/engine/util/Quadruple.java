package webfootprint.engine.util;

public class Quadruple<V, E, L, S> {
	
	V first;
	E second;
	L third;
	S fourth;
	
	public Quadruple(V first, E second, L third, S fourth) {
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
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
	
	public S getFourth() {
		return this.fourth;
	}
}
