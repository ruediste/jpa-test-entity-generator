package com.github.ruediste.jpaTestEntityGenerator;

import java.util.HashMap;
import java.util.Map;

public enum DefaultGenerationConfigs implements GenerationConfig {

	BALANCED{

		@Override
		public Map<RelationType, Double> relationWeights() {
			Map<RelationType, Double> result=new HashMap<RelationType, Double>();
			result.put(RelationType.MANY_TO_ONE, 1.0);
			result.put(RelationType.MANY_TO_MANY, 1.0);
			return result;
		};

		@Override
		public double reverseDirectionProbability() {
			return 0.2;
		}

		@Override
		public int maxRelationCount() {
			return 5;
		}}
	
}
