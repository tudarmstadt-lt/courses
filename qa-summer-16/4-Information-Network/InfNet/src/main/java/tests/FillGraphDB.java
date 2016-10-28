package tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Entity;

import ibm.alchemy.relationshipextraction.Relation;
import ibm.alchemy.relationshipextraction.RelationsHandler;
import ibm.graph.BluemixGraphDbClient;
import ibm.graph.RelationParser;
import ibm.graph.schema.Schema;
import ibm.tools.IOTool;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
public class FillGraphDB {

	private final String baseFolder = "M:\\Uni\\semester12\\Question Answering Systems with IBM Watson\\files\\";
	private final String htmlFolder = baseFolder + "marvel_html_files";
	private final String extractedInfosFolder = baseFolder + "extractedInformationNEW";
	private final String relationShipsFolder = baseFolder + "alchemyRelationshipExtraction";
	private final String entitiesFolder = baseFolder + "alchemyEntityExtraction";

	private final String errorFile = baseFolder + "errorFiles.txt";
	private final String finishedFile = baseFolder + "finishedFiles.txt";
	private final String logFile = baseFolder + "log.txt";
	private List<String> finishedFiles;
	private List<String> errorFiles;
	private List<String> logLines;

	public static void main(String[] args) {
		try {
			new FillGraphDB().run();
			// new FillGraphDB().initiate();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	public FillGraphDB() {
		try {
			finishedFiles = Files.readAllLines(Paths.get(finishedFile));
			errorFiles = Files.readAllLines(Paths.get(errorFile));
			logLines = Files.readAllLines(Paths.get(logFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * only use when deleting the graph
	 *
	 * private void initiate() { BluemixGraphDbClient client = new
	 * BluemixGraphDbClient(null); List<String> allGraphIds =
	 * client.getAllUsedGraphIDs(); client = new
	 * BluemixGraphDbClient(allGraphIds.get(0)); client.deleteGraph(); String
	 * graphid = client.createGraph(); System.out.println("Created graph id: " +
	 * graphid); client = new BluemixGraphDbClient(graphid);
	 * 
	 * // set the schema Schema schema = Schema.getSchema(); SchemaHandling
	 * schemaHandling = new SchemaHandling(client);
	 * schemaHandling.setSchema(schema); }/
	 **/

	@SuppressWarnings("unused")
	private void run() {
		List<String> files_extractedInfo = getAllFilesFromFolder(extractedInfosFolder);

		// initiate GraphDB client
		BluemixGraphDbClient client = new BluemixGraphDbClient("35e5b152-4189-4c2d-9486-a5cc6066f12b");
		// BluemixGraphDbClient client = new BluemixGraphDbClient("g");

		// set the schema
		Schema schema = Schema.getSchema();

		// push to db
		RelationParser relationParser = new RelationParser(schema, client);
		BluemixGraphDbClient.pushedVertices = 0;
		BluemixGraphDbClient.puttedVertices = 0;
		BluemixGraphDbClient.pushedEdges = 0;
		BluemixGraphDbClient.puttedEdges = 0;

		for (String filename_eI : files_extractedInfo) {
			JSONObject extractedInfo = IOTool.readJsonObject(extractedInfosFolder + "\\" + filename_eI);
			String filename_EE = extractedInfo.getString("filename");
			if (finishedFiles.contains(filename_EE)) {
				continue;
			}
			if (fileExists(entitiesFolder + "\\" + filename_EE + ".json")) {
				List<Entity> extractedEntities = getEntities(filename_EE);
				if (fileExists(relationShipsFolder + "\\" + filename_EE + ".json")) {
					resetCounter();
					List<JSONObject> alchemyRelationships = getRelationships(filename_EE);

					// Now we have entities and relationships.. start the
					// filtering
					List<Relation> filteredRelations = new ArrayList<>();
					RelationsHandler handler = new RelationsHandler();
					for (JSONObject jsonRelation : alchemyRelationships) {
						List<Relation> relations = handler.createRelationsData(jsonRelation);
						if (relations == null) {
							addErrorFile(filename_EE + ": couldn't parse relations. jsonRelation = '"
									+ jsonRelation.toString() + "' ");
							continue;
						}
						for (Relation r : relations) {
							Entity one = isRelationEntityAGoodEntity(r.getEntityOne(), extractedEntities);
							if (one != null) {
								Entity two = isRelationEntityAGoodEntity(r.getEntityTwo(), extractedEntities);
								if (two != null) {
									Relation filteredR = new Relation(one.getText(), r.getEntityOneType(),
											two.getText(), r.getEntityTwoType(), r.getRelation(), r.getScore(),
											r.getTitle(), r.getUrl(), one.getCount(), two.getCount(),
											one.getRelevance().floatValue(), two.getRelevance().floatValue(),
											r.getSentence(), 1, 0);
									filteredRelations.add(filteredR);
								}
							}
						}
					}

					relationParser.parseAndPush(filteredRelations);
					addLogMsg(filename_EE + " Vertices(pushed, putted), Edges(pushed, putted): ["
							+ BluemixGraphDbClient.pushedVertices + ", " + BluemixGraphDbClient.puttedVertices + "], ["
							+ BluemixGraphDbClient.pushedEdges + ", " + BluemixGraphDbClient.puttedEdges + "]");
					addFinishedFile(filename_EE);
				} else {
					addErrorFile(filename_eI + " couldn't find file in relationships folder");
				}
			} else {
				addErrorFile(filename_eI + " couldn't find file in entities folder");
			}
		}
	}

	private void resetCounter() {
		BluemixGraphDbClient.pushedEdges = 0;
		BluemixGraphDbClient.pushedVertices = 0;
		BluemixGraphDbClient.puttedEdges = 0;
		BluemixGraphDbClient.puttedVertices = 0;
	}

	private List<JSONObject> getRelationships(String filename) {
		List<JSONObject> result = new ArrayList<>();
		JSONArray fileContents = IOTool.readJsonArray(relationShipsFolder + "\\" + filename + ".json");
		for (int i = 0; i < fileContents.length(); i++) {
			result.add(fileContents.getJSONObject(i));
		}
		return result;
	}

	private List<Entity> getEntities(String filename) {
		List<Entity> result = new ArrayList<>();
		JSONArray fileContents = IOTool.readJsonArray(entitiesFolder + "\\" + filename + ".json");
		for (int i = 0; i < fileContents.length(); i++) {
			JSONArray json_ent_arr = fileContents.getJSONObject(i).getJSONArray("entities");
			for (int j = 0; j < json_ent_arr.length(); j++) {
				Entity entity = new Gson().fromJson(json_ent_arr.getJSONObject(j).toString(), Entity.class);
				if (!result.contains(entity)) {
					result.add(entity);
				}
			}
		}
		return result;
	}

	private void addFinishedFile(String filename) {
		finishedFiles.add(filename);
		try {
			Files.write(Paths.get(finishedFile), finishedFiles);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Finished '" + filename + "' - total of " + finishedFiles.size() + " files");
	}

	private void addErrorFile(String msg) {
		errorFiles.add(msg);
		try {
			Files.write(Paths.get(errorFile), errorFiles);
			System.err.println("Saving the Error msg: " + msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addLogMsg(String msg) {
		logLines.add(msg);
		try {
			Files.write(Paths.get(logFile), logLines);
			System.out.println("Saving Log:" + msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<String> getAllFilesFromFolder(String folder) {

		File f = new File(folder);
		if (!fileExists(folder)) {
			return null;
		}
		if (!f.isDirectory()) {
			return null;
		}
		return Arrays.asList(f.list());
	}

	private boolean fileExists(String path) {
		return new File(path).exists();
	}

	/**
	 * searches for an Entity which represents the given 'test'-String as good
	 * as possible. if no entity is found, this method returns null and the
	 * given test-string belonging to a relationship entity is not good for
	 * further use.
	 * 
	 * @param test
	 * @param entities
	 * @return
	 */
	private Entity isRelationEntityAGoodEntity(String test, List<Entity> entities) {
		for (Entity e : entities) {
			if (testGoodEntity(test, e.getText())) {
				return e;
			}
		}
		return null;
	}

	private static boolean testGoodEntity(String is, String entityText) {
		String test = is.toLowerCase();
		String shouldBe = entityText.toLowerCase();
		if (test.equals(shouldBe)) {
			return true;
		}

		String[] testArr = test.split(" ");

		List<String> shouldBeArr = Arrays.asList(shouldBe.split(" "));
		for (String test_part : testArr) {
			if (!shouldBeArr.contains(test_part)) {
				return false;
			}
		}

		return true;
	}

}
