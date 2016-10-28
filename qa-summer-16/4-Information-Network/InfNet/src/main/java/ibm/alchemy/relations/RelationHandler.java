package ibm.alchemy.relations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ibm.watson.developer_cloud.alchemy.v1.model.Entities;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Entity;
import com.ibm.watson.developer_cloud.alchemy.v1.model.SAORelation;
import com.ibm.watson.developer_cloud.alchemy.v1.model.SAORelation.Action;
import com.ibm.watson.developer_cloud.alchemy.v1.model.SAORelation.Action.Verb;
import com.ibm.watson.developer_cloud.alchemy.v1.model.SAORelation.RelationObject;
import com.ibm.watson.developer_cloud.alchemy.v1.model.SAORelation.Subject;
import com.ibm.watson.developer_cloud.alchemy.v1.model.SAORelations;

import ibm.alchemy.entity.AlchemyAPIClient;
import ibm.alchemy.entity.AlchemyEntitiesHandler;
import ibm.alchemy.entity.AlchemyEntitiesWithFurtherInformation;
import ibm.tools.IOTool;

/**
 * Class to handle the request and response of the Alchemy SAO Relation
 * extraction.
 * 
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph S.
 */
public class RelationHandler {

	/**
	 * Uses {@link AlchemyAPIClient#analyzeText(String) analyzeText}
	 * 
	 * @param start
	 *            which file should be number of start
	 * @param end
	 *            which file should be number of last
	 * @param fromDir
	 *            the directory, where to find the files
	 * @param toDir
	 *            the directory, where the results of the Alchemy Entity
	 *            Extraction should be saved
	 */
	@SuppressWarnings("unused")
	private static void alchemyRelationExtractionForMultipleFiles(int start, int end, String fromDir, String toDir,
			String namedEntitiesDetection, String restrictResults) {
		AlchemyRelationsClient aRC = new AlchemyRelationsClient();
		File dir = new File("./" + fromDir);
		File[] directoryListing = dir.listFiles();
		File current;
		JSONObject json;
		SAORelations[] soaRelations;
		String text;
		String filename;
		String link;
		// start und ende festlegen, da nur 1000 Anfragen pro Tag
		// diectoryListening 7368 files
		for (int i = start; i < end; i++) {

			current = directoryListing[i];
			System.out.println("i=" + i + " : " + current.toString());
			if (current.toString().contains(".DS_Store"))
				continue;
			json = IOTool.readJsonObject(current.toString());
			filename = json.getString("filename");
			text = json.getString("text");
			link = json.getString("link");

			soaRelations = aRC.analyzeText(text, namedEntitiesDetection, restrictResults);

			writeToDirectory("./" + toDir);
			JSONArray jsonArray = new JSONArray();
			for (int j = 0; j < soaRelations.length; j++) {
				jsonArray.put(new JSONObject(soaRelations[j]).put("url", link));
			}
			FileWriter file;
			try {
				file = new FileWriter("./" + toDir + "/" + filename + ".json");
				file.write(jsonArray.toString());
				file.flush();
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@SuppressWarnings("unused")
	private static SAORelations[] readAndParseRelationFile(String file) {
		BufferedReader br;
		try {
			// read file
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			// parse file to json array
			JSONArray jsonArray = new JSONArray(line);
			// System.err.println(jsonArray);
			// create the SAORelations[] to return
			SAORelations[] soaRelationsArray = new SAORelations[jsonArray.length()];
			System.out.println("eingelesenen jsonarray length: " + jsonArray.length());
			// if the file is splittet x times, because of the size limit of
			// 50KB per request, we have have an array with 11 entities entries
			for (int i = 0; i < jsonArray.length(); i++) {

				JSONObject soaRelationsJson = jsonArray.getJSONObject(i);
				JSONArray currentSoaRelations = soaRelationsJson.getJSONArray("relations");
				System.out.println("lenght: " + currentSoaRelations.length());
				System.out.println(i + "=i: " + currentSoaRelations.toString());
				SAORelations alchemySoaRelations = new SAORelations();
				List<SAORelation> soaRelationList = new ArrayList<SAORelation>();
				for (int j = 0; j < currentSoaRelations.length(); j++) {
					System.out.println(j + " = j");
					System.out.println(currentSoaRelations.get(j));
					JSONObject soaRelation = currentSoaRelations.getJSONObject(j);
					SAORelation alchemySaoRelation = new SAORelation();
					alchemySaoRelation.setSentence(soaRelation.getString("sentence"));
					Subject subject = new Subject();
					subject.setText(soaRelation.getJSONObject("subject").getString("text"));
					alchemySaoRelation.setSubject(subject);
					Action action = new Action();
					action.setLemmatized(soaRelation.getJSONObject("action").getString("lemmatized"));
					Verb verb = new Verb();
					verb.setText(soaRelation.getJSONObject("action").getJSONObject("verb").getString("text"));
					action.setVerb(verb);
					action.setText(soaRelation.getJSONObject("action").getString("text"));
					alchemySaoRelation.setAction(action);
					RelationObject relationObject = new RelationObject();
					if (!soaRelation.has("object")) {
						relationObject.setText("");
					} else
						relationObject.setText(soaRelation.getJSONObject("object").getString("text"));
					alchemySaoRelation.setObject(relationObject);
					soaRelationList.add(alchemySaoRelation);
				}
				alchemySoaRelations.setRelations(soaRelationList);
				soaRelationsArray[i] = alchemySoaRelations;
			}
			// System.out.println(soaRelationsArray[0].getRelations().get(48).getSentence());
			return soaRelationsArray;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Create a new folder
	 * 
	 * @param newFolder
	 *            the folder Location e.g. ./testfolder
	 */
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

	public static void main(String[] args) {
		RelationHandler rH = new RelationHandler();
		// rH.alchemyRelationExtractionForMultipleFiles(3945, 4050,
		// "extractedInformationNEW", "soaRelationExtr", "1", "1");
		// rH.alchemyRelationExtractionForMultipleFiles(0, 2,
		// "testc","relationExtr", "0","1");
		// rH.analyzeDir("testc", "relationshipExtraction", 0, 2);
		// rH.printRelations(rH.createRelationsData(read("./relationshipExtraction/Playground-24981.json")));
		// rH.readAndParseRelationFile("./relationExtr/107th Infantry
		// Regiment-137513.json");
		// rH.printRelations(rH.readAndParseRelationFile("./relationExtr/Playground-24981.json"));
		// rH.readAndParseRelationFile("./relationExtr/Playground-24981.json");
		// rH.printRelations(rH.readAndParseRelationFile("./relationExtr/Playground-24981.json"));
		// rH.getRelationFromFile("./soaRelationExtr/Captain America-2056.json",
		// "./alchemyEntityExtraction/");

		// rH.saveResultList(rH.getRelationsForHoleDir("./soaRelationExtr/",
		// "./alchemyEntityExtraction/"));
		rH.readAndParse("./resultList.json");
	}

	private List<Relation> getRelationsForHoleDir(String fromDir, String compareDir) {
		File dir = new File(fromDir);
		File[] directoryListing = dir.listFiles();
		List<Relation> resultList = new ArrayList<Relation>();
		for (File file : directoryListing) {
			System.out.println(file.toString());
			resultList.addAll(getRelationFromFile(file.toString(), compareDir));
		}
		return resultList;

	}

	public void printRelations(SAORelations[] relationList) {
		System.out.println("soaRelationsArray-length: " + relationList.length);
		for (int i = 0; i < relationList.length; i++) {
			System.out.println(relationList[i].toString());
		}
	}

	public List<Relation> readAndParse(String file) {
		List<Relation> resultList = new ArrayList<Relation>();
		try {

			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			JSONArray jsonArray = new JSONArray(line);

			for (int i = 0; i < jsonArray.length(); i++) {
				resultList.add(new Relation(jsonArray.getJSONObject(i)));

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		// for (Relation relation : resultList) {
		// relation.print();
		// }
		return resultList;
	}

	public void saveResultList(List<Relation> resultList) {
		JSONArray result = new JSONArray();
		for (Relation relation : resultList) {
			result.put(relation.getAsJsonObject());

		}
		FileWriter file;
		try {
			file = new FileWriter("resultList.json");

			file.write(result.toString());

			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<Relation> getRelationFromFile(String fromSoaRelationFile, String compareWithEntityDir) {
		AlchemyEntitiesHandler aEH = new AlchemyEntitiesHandler();
		// System.out.println("File from Soa Relation: " + fromSoaRelationFile);
		String file = fromSoaRelationFile.substring(fromSoaRelationFile.lastIndexOf('/') + 1);
		Entities[] entitiesComparedFile;
		// System.out.println("file exist: " + new File(compareDir,
		// file).exists());
		List<Relation> relations = new ArrayList<Relation>();

		if (new File(compareWithEntityDir, file).exists()) {
			// System.out.println("file from Entity Dir: " +
			// compareWithEntityDir + file);
			entitiesComparedFile = aEH.readAndParseEntityFile(compareWithEntityDir + file).getEntities();
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(fromSoaRelationFile));

				String line = br.readLine();
				JSONArray jsonArray = new JSONArray(line);
				// System.out.println("SoaJSONArray: " + jsonArray.length());
				for (int i = 0; i < jsonArray.length(); i++) {
					// get the entities entry
					JSONObject currentSoaJson = jsonArray.getJSONObject(i);
					// System.out.println("i: " + i + currentSoaJson);
					relations.addAll(createRelations(currentSoaJson, entitiesComparedFile));

				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return relations;

	}

	public List<Relation> createRelations(JSONObject currentSoaRelation, Entities[] entitiesComparedFile) {
		// System.err.println(currentSoaRelation.toString());
		List<Relation> relationList = new ArrayList<Relation>();
		String originSentence = "";
		String action = "";
		String object = "";
		String originObject = "";
		String subject = "";
		String originSubject = "";
		String link = currentSoaRelation.getString("url");

		JSONArray relationArray = currentSoaRelation.getJSONArray("relations");
		// System.out.println("j-lenght:" + relationArray.length());
		for (int j = 0; j < relationArray.length(); j++) {
			JSONObject current = relationArray.getJSONObject(j);
			// System.err.println("j:" + j + current.toString());
			originSentence = current.getString("sentence");
			if (!current.has("action") || !current.has("object") || !current.has("subject"))
				continue;
			action = current.getJSONObject("action").getString("text");

			originObject = current.getJSONObject("object").getString("text");

			originSubject = current.getJSONObject("subject").getString("text");

			Entity subjectEntity = isRelationEntityAGoodEntity(current.getJSONObject("subject").getString("text"),
					entitiesComparedFile);
			Entity objectEntity = isRelationEntityAGoodEntity(current.getJSONObject("object").getString("text"),
					entitiesComparedFile);
			if (subjectEntity == null || objectEntity == null)
				continue;

			object = objectEntity.getText();
			subject = subjectEntity.getText();

			// new Relation(originSentence, action, object, originObject,
			// subject, originSubject, link).print();
			relationList.add(new Relation(originSentence, action, object, originObject, subject, originSubject, link));
		}

		return relationList;
	}

	private Entity isRelationEntityAGoodEntity(String test, Entities[] entitiesComparedFile) {
		for (Entities entities : entitiesComparedFile) {
			for (Entity entity : entities.getEntities()) {
				if (testGoodEntity(test, entity.getText())) {
					// System.out.println("entity found");
					return entity;
				}
			}

		}
		return null;
	}

	private static boolean testGoodEntity(String is, String entityText) {
		String test = is.toLowerCase();
		String shouldBe = entityText.toLowerCase();
		// zweiter Teil des OR hinzugefügt || test.contains(shouldBe)
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
