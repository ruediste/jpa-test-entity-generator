package com.github.ruediste.jpaTestEntityGenerator;

public class Relation {
	
	String name;
	Entity source;
	Entity target;
	RelationType type;

	public Relation(String name, Entity source, Entity target, RelationType type) {
		super();
		this.name = name;
		this.source = source;
		this.target = target;
		this.type = type;
	}

}
