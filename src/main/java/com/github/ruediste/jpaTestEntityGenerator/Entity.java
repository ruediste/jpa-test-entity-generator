package com.github.ruediste.jpaTestEntityGenerator;

import java.util.ArrayList;
import java.util.List;

public class Entity {

	String name;
	List<Relation> outRelations=new ArrayList<>();
	List<Relation> inRelations=new ArrayList<>();

	public Entity(String name) {
		this.name = name;
	}

	
	public void addRelation(Relation relation){
		if (relation.source==this)
			outRelations.add(relation);
		if (relation.target==this)
			inRelations.add(relation);
	}
}
