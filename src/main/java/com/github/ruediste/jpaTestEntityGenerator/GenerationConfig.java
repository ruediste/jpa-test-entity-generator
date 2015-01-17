package com.github.ruediste.jpaTestEntityGenerator;

import java.util.Map;

public interface GenerationConfig {
	Map<RelationType, Double> relationWeights();

	/**
	 * Usually, relations of a new entity point back to existing entities.
	 * This is the probability of creating relations in the reverse direction.
	 */
	 double reverseDirectionProbability();

	 /**
	  * Maximal number of generated relations per entity. The actual value is choosen randomly.
	  */
	int maxRelationCount();
}
