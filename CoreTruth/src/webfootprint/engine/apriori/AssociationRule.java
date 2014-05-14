package webfootprint.engine.apriori;

import webfootprint.engine.util.Quadruple;

public class AssociationRule extends Quadruple {
	
	public AssociationRule(RuleConstituent antecedent, RuleConstituent consequent, Double support, Double confidence) {
		super(antecedent, consequent, support, confidence);
	}
	
	public AssociationRule(RuleConstituent antecedent, RuleConstituent consequent, Double confidence) {
		super(antecedent, consequent, null, confidence);
	}
	
	public RuleConstituent getAntecedent() {
		return (RuleConstituent)super.getFirst();
	}
	
	public RuleConstituent getConsequent() {
		return (RuleConstituent)super.getSecond();
	}
	
	public Double getSupport() {
		return (Double)super.getThird();
	}
	
	public Double getConfidence() {
		return (Double)super.getFourth();
	}
}
