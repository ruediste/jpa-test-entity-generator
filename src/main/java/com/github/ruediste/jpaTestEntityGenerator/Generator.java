package com.github.ruediste.jpaTestEntityGenerator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;


import com.google.common.base.CaseFormat;

/**
 * Hello world!
 *
 */
public class Generator {
	public static void main(String[] args) {
		new Generator().mainImpl(args);
	}

	Random random = new Random(1);
	int totalRelationCount;

	private void mainImpl(String[] args) {
		List<Entity> graph = createGraph(30, DefaultGenerationConfigs.BALANCED);
		Path target = Paths.get("generated/test");
		try {
			if (Files.exists(target))
				removeRecursive(target);
			Files.createDirectories(target);
			generateJavaFiles(target, graph);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void generateJavaFiles(Path target, List<Entity> graph)
			throws IOException {
		for (Entity e : graph) {
			generateJavaFile(target, e);
		}
	}

	private void generateJavaFile(Path target, Entity e) throws IOException {
		BufferedWriter writer = Files.newBufferedWriter(
				target.resolve(e.name + ".java"), Charset.forName("UTF-8"),
				StandardOpenOption.CREATE);

		writer.append("package test;\n");
		writer.append("import java.util.Set;\nimport javax.persistence.*;\n");
		writer.append("@Entity\n");
		writer.append("public class " + e.name + "{\n");
		writer.append("@Id @GeneratedValue int id;\n");
		writer.append("// outRelations\n");
		for (Relation rel : e.outRelations) {
			String upper = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL,
					rel.name);
			switch (rel.type) {
			case MANY_TO_MANY: {
				writer.append("@ManyToMany\n");
				writer.append("@JoinTable(joinColumns = @JoinColumn(name = \""
						+ rel.source.name
						+ "_id\", referencedColumnName = \"id\"), inverseJoinColumns = @JoinColumn(name = \""
						+ rel.target.name
						+ "_id\", referencedColumnName = \"id\"))");
				writer.append("private Set<" + rel.target.name + "> "
						+ rel.name + ";\n");
			}
				break;
			case MANY_TO_ONE: {
				writer.append("@ManyToOne\n");
				writer.append("private " + rel.target.name + " " + rel.name
						+ ";\n");
				writer.append("public " + rel.target.name + " get" + upper
						+ "(){return " + rel.name + ";}\n");
				writer.append("public void set" + upper + "(" + rel.target.name
						+ " value){" + rel.name + "=value;}\n");
			}
				break;
			default:
				throw new RuntimeException("should not happen");
			}
		}

		writer.append("// inRelations\n");
		for (Relation rel : e.inRelations) {
			String upper = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL,
					rel.name);
			switch (rel.type) {
			case MANY_TO_MANY: {
				writer.append("@ManyToMany(mappedBy=\"" + rel.name + "\")\n");
				writer.append("private Set<" + rel.source.name + "> "
						+ rel.name + ";\n");
			}
				break;
			case MANY_TO_ONE: {
				writer.append("@OneToMany(mappedBy=\"" + rel.name + "\")\n");
				writer.append("private Set<" + rel.source.name + "> "
						+ rel.name + ";\n");
			}
				break;
			default:
				throw new RuntimeException("should not happen");

			}
		}
		writer.append("}\n");
		writer.flush();
		writer.close();
	}

	private List<Entity> createGraph(int count, DefaultGenerationConfigs config) {
		ArrayList<Entity> result = new ArrayList<Entity>();
		for (int i = 0; i < count; i++) {
			Entity e = new Entity("Entity" + i);
			addRelations(result, e, config);
			result.add(e);
		}

		return result;
	}

	private void addRelations(ArrayList<Entity> existingEntities, Entity e,
			GenerationConfig config) {
		if (existingEntities.isEmpty())
			return;
		for (int i = 0; i < config.maxRelationCount(); i++) {
			RelationType type = chooseRandom(config.relationWeights());
			boolean isReverse = random.nextDouble() < config
					.reverseDirectionProbability();
			Entity other = existingEntities.get((int) Math
					.floor(existingEntities.size() * random.nextDouble()));
			Relation relation;
			String name = CaseFormat.UPPER_UNDERSCORE.to(
					CaseFormat.LOWER_CAMEL, type.toString());
			if (isReverse) {
				relation = new Relation(name + totalRelationCount++, other, e,
						type);
			} else
				relation = new Relation(name + totalRelationCount++, e, other,
						type);

			e.addRelation(relation);
			other.addRelation(relation);
		}
	}

	private <T> T chooseRandom(Map<T, Double> valueMap) {
		double sum = valueMap.values().stream().reduce(0.0, (a, b) -> a + b);
		double target = random.nextDouble() * sum;

		double current = 0.0;
		for (Entry<T, Double> e : valueMap.entrySet()) {
			current += e.getValue();
			if (current > target)
				return e.getKey();
		}

		throw new RuntimeException("shouldNotHappen");
	}

	public static void removeRecursive(Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc)
					throws IOException {
				// try to delete the file anyway, even if its attributes
				// could not be read, since delete-only access is
				// theoretically possible
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					throws IOException {
				if (exc == null) {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				} else {
					// directory iteration failed; propagate exception
					throw exc;
				}
			}
		});
	}
}
