package ibm.alchemy.relationshipextraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import ibm.tools.IOTool;

/**
 * Class to handle the request and response of the Alchemy Relationship
 * extraction.
 * 
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph S.
 */
public class RelationsHandler {

	public static void main(String[] args) {
		RelationsHandler rH = new RelationsHandler();
		// rH.analyzeDir("extractedInformationNEW",
		// "alchemyRelationshipExtraction", 2500, 3000);
		// rH.analyzeDir("testc", "relationshipExtraction", 0, 1);
		rH.printRelations(rH.createRelationsData(read("./alchemyRelationshipExtraction/Joseph Rogers-9859.json")));

	}

	private static JSONObject[] read(String file) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			JSONArray jsonArray = new JSONArray(line);
			JSONObject[] jsonObjectArray = new JSONObject[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); i++) {
				jsonObjectArray[i] = jsonArray.getJSONObject(i);
			}
			return jsonObjectArray;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public void printRelations(List<Relation> relationList) {
		for (Relation relation : relationList) {
			relation.printRelation();
		}
	}

	public List<Relation> createRelationsData(JSONObject[] JsonObjectArray) {
		List<Relation> relations = new ArrayList<Relation>();
		for (int i = 0; i < JsonObjectArray.length; i++) {
			relations.addAll(createRelationsData(JsonObjectArray[i]));
		}
		return relations;
	}

	/**
	 * Create a List of @link {@link Relation} out of a json Relationship object
	 * 
	 * @param jsonObject
	 *            containing the relationship extraction response
	 * @return {@link List} with {@link Relation}
	 */
	public List<Relation> createRelationsData(JSONObject jsonObject) {
		String entityOne = "";
		String entityOneType = "";
		String entityTwo = "";
		String entityTwoType = "";
		String relation = "";
		String score = "";
		String title = "";
		String url = "";
		String sentence = "";
		List<Relation> relations;
		relations = new ArrayList<Relation>();
		if (jsonObject.has("title"))
			title = jsonObject.getString("title");
		if (jsonObject.has("link"))
			url = jsonObject.getString("link");

		if (!jsonObject.has("typedRelations")) {
			return null;
		}
		JSONArray typedRelationsArray = (JSONArray) jsonObject.get("typedRelations");
		for (int i = 0; i < typedRelationsArray.length(); i++) {
			if (typedRelationsArray.getJSONObject(i).has("score") && typedRelationsArray.getJSONObject(i).has("type")) {
				score = typedRelationsArray.getJSONObject(i).getString("score");
				relation = typedRelationsArray.getJSONObject(i).getString("type");
				sentence = typedRelationsArray.getJSONObject(i).getString("sentence");
				JSONArray arguments = typedRelationsArray.getJSONObject(i).getJSONArray("arguments");
				for (int j = 0; j < arguments.length(); j++) {
					if (j == 0) {
						// entityOne =
						// arguments.getJSONObject(j).getString("text");
						entityOne = ((JSONObject) arguments.getJSONObject(j).getJSONArray("entities").get(0))
								.getString("text");
						entityOneType = ((JSONObject) arguments.getJSONObject(j).getJSONArray("entities").get(0))
								.getString("type");
					} else {
						// entityTwo =
						// arguments.getJSONObject(j).getString("text");
						entityTwo = ((JSONObject) arguments.getJSONObject(j).getJSONArray("entities").get(0))
								.getString("text");
						entityTwoType = ((JSONObject) arguments.getJSONObject(j).getJSONArray("entities").get(0))
								.getString("type");
					}
				}
				relations.add(new Relation(entityOne, entityOneType, entityTwo, entityTwoType, relation, score, title,
						url, 1, 1, 0.0f, 0.0f, sentence, 0, 0));
			}
		}
		return relations;
	}

	/**
	 * Relationship analyzation for a hole dir.
	 * 
	 * @param fromDir
	 *            the directory, where to find the files
	 * @param toDir
	 *            the directory, where the results of the Alchemy Entity
	 *            Extraction should be saved
	 * @param startRange
	 *            which file should be number of star
	 * @param endRange
	 *            which file should be number of last
	 */
	public void analyzeDir(String fromDir, String toDir, int startRange, int endRange) {
		File dir = new File(fromDir);
		File[] directoryListing = dir.listFiles();
		File current;
		JSONObject json;
		JSONObject[] jsonReturn;
		String link;
		String title;
		String filename;
		String text;
		for (int i = startRange; i < endRange; i++) {
			current = directoryListing[i];
			System.out.println("current file number: " + i + " and filename: " + current.getName());

			if (current.toString().contains(".DS_Store"))
				continue;

			json = IOTool.readJsonObject(current.toString());
			filename = json.getString("filename");

			title = current.getName();
			link = json.getString("link");
			text = json.getString("text");
			try {
				// 3 works, test 2 now
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			AlchemyRelationshipExtraction aRe = new AlchemyRelationshipExtraction();
			jsonReturn = aRe.analyzeText(text);

			JSONArray jsonArray = new JSONArray();
			for (JSONObject jsonObject : jsonReturn) {
				jsonObject.put("link", link);
				jsonArray.put(jsonObject);
			}

			writeToDirectory(toDir);

			FileWriter file;
			try {
				file = new FileWriter(toDir + "/" + filename + ".json");
				file.write(jsonArray.toString());
				file.flush();
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public static boolean writeToDirectory(String newFolder) {
		File file = new File(newFolder);
		boolean returnValue = false;
		try {
			FileUtils.forceMkdir(file);
			returnValue = true;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return returnValue;

	}
}
