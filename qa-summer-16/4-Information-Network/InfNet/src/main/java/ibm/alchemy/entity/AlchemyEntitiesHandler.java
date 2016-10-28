package ibm.alchemy.entity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ibm.watson.developer_cloud.alchemy.v1.model.Entities;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Entity;

import ibm.tools.IOTool;

/**
 * Class to handle the request and response of the Alchemy Entity extraction.
 * 
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph S.
 */
public class AlchemyEntitiesHandler {

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
	private static void alchemyEntityExtractionForMultipleFiles(int start, int end, String fromDir, String toDir) {
		AlchemyAPIClient client = new AlchemyAPIClient();
		File dir = new File("./" + fromDir);
		File[] directoryListing = dir.listFiles();
		File current;
		JSONObject json;
		Entities[] entities;
		String text;
		String filename;
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
			entities = client.analyzeText(text);

			writeToDirectory("./" + toDir);
			JSONArray jsonArray = new JSONArray();
			for (int j = 0; j < entities.length; j++) {
				jsonArray.put(new JSONObject(entities[j]));
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

	/**
	 * Can read an Alchemy Entity file, which was further generated and saved by
	 * {@link #alchemyEntityExtractionForMultipleFiles(int, int, String,
	 * String)h}
	 * 
	 * @param file
	 *            which contains the Alchemy Entities
	 */
	@SuppressWarnings("unused")
	public static AlchemyEntitiesWithFurtherInformation readAndParseEntityFile(String file) {
		BufferedReader br;
		try {
			// read file
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			// parse file to json array
			JSONArray jsonArray = new JSONArray(line);
			// System.err.println(jsonArray);
			// create the Entities[] to return
			Entities[] entitiesArray = new Entities[jsonArray.length()];
			// if the file is splittet x times, because of the size limit of
			// 50KB per request, we have have an array with 11 entities entries
			String title = "";
			String url = "";
			for (int i = 0; i < jsonArray.length(); i++) {
				// get the entities entry
				JSONObject currentJsonEntities = jsonArray.getJSONObject(i);
				// System.out.println("aa: " + currentJsonEntities);
				title = currentJsonEntities.getString("title");
				url = currentJsonEntities.getString("url");
				// System.out.println("Title: " + title + " url: " + url);
				JSONArray currentJsonEntitiesEntries = currentJsonEntities.getJSONArray("entities");

				// create a Alchemy Entities Type
				Entities alchemyEntities = new Entities();
				// an Entity Type consists of a list of Alchemy Entity's
				List<Entity> alchemyEntityList = new ArrayList<Entity>();
				// create for each entry in the entities entry an Alchemy Entry
				// and store it into the Entry List
				for (int j = 0; j < currentJsonEntitiesEntries.length(); j++) {
					// System.out.println(currentJsonEntitiesEntries.get(j));
					JSONObject enitity = currentJsonEntitiesEntries.getJSONObject(j);
					Entity alchemyEntity = new Entity();
					alchemyEntity.setCount(enitity.getInt("count"));
					alchemyEntity.setText(enitity.getString("text"));
					alchemyEntity.setType(enitity.getString("type"));
					alchemyEntity.setRelevance(enitity.getDouble("relevance"));
					alchemyEntityList.add(alchemyEntity);
				}
				// set the List with the Alchemy Entity Entries
				alchemyEntities.setEntities(alchemyEntityList);
				// set the entries at the response array
				entitiesArray[i] = alchemyEntities;

			}
			AlchemyEntitiesWithFurtherInformation aEWFI = new AlchemyEntitiesWithFurtherInformation(entitiesArray, url,
					title);
			return aEWFI;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Entities[] createNewEntitiesByOldEntities(List<Entity> extractedEntities) {
		List<Entities> result = new ArrayList<>();
		Entities alchemyEntities = new Entities();
		alchemyEntities.setEntities(extractedEntities);
		result.add(alchemyEntities);

		return result.toArray(new Entities[] {});
	}

	/**
	 * Generates a Distribution for directory including Alchemy Entry files.
	 * 
	 * @param sourcefolder
	 *            the directory which includes the entry files e.g.
	 *            alchemyEntityExtraction
	 * @param outputfile
	 *            the file e.g.alchemyEntitiesCountDistribution.txt
	 */
	private static void createAndPrintAlchemyEntitiesDistribution(String sourcefolder, String outputfile) {
		HashMap<String, Integer> distribution = new HashMap<String, Integer>();
		// Entitie Distribution
		File dir = new File(sourcefolder);
		if (dir.isDirectory()) {
			for (File f : dir.listFiles()) {

				Entities[] entitiesArray = readAndParseEntityFile(f.toString()).getEntities();
				for (Entities entities : entitiesArray) {
					for (Entity entity : entities.getEntities()) {
						if (distribution.containsKey(entity.getText()))
							distribution.put(entity.getText(), distribution.get(entity.getText()) + entity.getCount());
						else
							distribution.put(entity.getText(), entity.getCount());
					}
				}
			}
		}
		FileWriter file;
		try {
			distribution = (HashMap<String, Integer>) sortMapByValues(distribution);

			// print
			file = new FileWriter(outputfile);
			file.write("Entity Distribution" + "\n");
			file.write("Count : entity" + "\n");
			for (Entry<String, Integer> set : distribution.entrySet()) {
				file.write(set.getValue() + ":" + set.getKey() + "\n");

			}
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generates a count Distribution for directory including Alchemy Entry
	 * files. count each entity per file once.
	 * 
	 * @param sourcefolder
	 *            the directory which includes the entry files e.g.
	 *            alchemyEntityExtraction
	 * @param outputfile
	 *            the file e.g.alchemyEntitiesCountDistribution.txt
	 */
	private static void createAndPrintAlchemyEntitiesCountDistribution(String sourcefolder, String outputfile) {
		HashMap<String, Integer> distribution = new HashMap<String, Integer>();
		// Entitie Distribution
		File dir = new File(sourcefolder);
		if (dir.isDirectory()) {
			for (File f : dir.listFiles()) {
				Entities[] entitiesArray = readAndParseEntityFile(f.toString()).getEntities();
				for (Entities entities : entitiesArray) {
					for (Entity entity : entities.getEntities()) {
						if (distribution.containsKey(entity.getText()))
							distribution.put(entity.getText(), distribution.get(entity.getText()) + 1);
						else
							distribution.put(entity.getText(), 1);
					}
				}
			}
		}
		FileWriter file;
		try {
			distribution = (HashMap<String, Integer>) sortMapByValues(distribution);

			// print
			file = new FileWriter(outputfile);
			file.write("Entity Count Distribution" + "\n");
			file.write("Count (max once per File) : entity" + "\n");
			for (Entry<String, Integer> set : distribution.entrySet()) {
				file.write(set.getValue() + ":" + set.getKey() + "\n");

			}
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Map<String, Integer> sortMapByValues(Map<String, Integer> aMap) {

		Set<Entry<String, Integer>> mapEntries = aMap.entrySet();

		System.out.println("Values and Keys before sorting ");
		for (Entry<String, Integer> entry : mapEntries) {
			System.out.println(entry.getValue() + " - " + entry.getKey());
		}

		// used linked list to sort, because insertion of elements in linked
		// list is faster than an array list.
		List<Entry<String, Integer>> aList = new LinkedList<Entry<String, Integer>>(mapEntries);

		// sorting the List
		Collections.sort(aList, new Comparator<Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> ele1, Entry<String, Integer> ele2) {

				return ele1.getValue().compareTo(ele2.getValue());
			}
		});

		// Storing the list into Linked HashMap to preserve the order of
		// insertion.
		Map<String, Integer> aMap2 = new LinkedHashMap<String, Integer>();
		for (Entry<String, Integer> entry : aList) {
			aMap2.put(entry.getKey(), entry.getValue());
		}

		// printing values after soring of map
		System.out.println("Value " + " - " + "Key");
		for (Entry<String, Integer> entry : aMap2.entrySet()) {
			System.out.println(entry.getValue() + " - " + entry.getKey());
		}
		return aMap2;

	}

	public static void main(String[] args) {
		AlchemyEntitiesHandler aEH = new AlchemyEntitiesHandler();
		// ******* enrich the alchemy entities
		// // aEH.addFurtherInformation("./10cc-30305.json",
		// // "./extractedInformationNEW/10cc-30305.json");
		// // aEH.addFurtherInformation("./Phil Coulson-2053.json",
		// // "./extractedInformationNEW/Phil Coulson-2053.json");
		//
		// aEH.furtherInformationForCompleteDir("./alchemyEntityExtraction",
		// "./extractedInformationNEW");
		// *************************
		// //
		// createAndPrintAlchemyEntitiesCountDistribution("alchemyEntityExtraction","alchemyEntitiesCountDistribution.txt");
		// //
		createAndPrintAlchemyEntitiesDistribution("alchemyEntityExtraction", "alchemyEntitiesDistribution.txt");
		// AlchemyEntitiesHandler aEH = new AlchemyEntitiesHandler();
		// //

		// readAndParseEntityFile("alchemyEntityExtraction/Phil
		// Coulson-2053.json");
		// aEH.createJsonEdgesForFile(readAndParseEntityFile("alchemyEntityExtraction/Phil
		// Coulson-2053.json"));
		// for (Tupel t :
		// aEH.createJsonEdgesForFile(readAndParseEntityFile("alchemyEntityExtraction/Phil
		// Coulson-2053.json"))) {
		// System.err.println(t.tupelAsString() );
		// }

		// aEH.createTupelForHoleDirAndSave("alchemyEntityExtraction",
		// "entityOnlyGraph");
		aEH.readAndParseTupelFile("entityOnlyGraph.json");
	}

	private void furtherInformationForCompleteDir(String dirString, String dirWithTheInformation) {
		File dir = new File(dirString);
		File[] directoryListing = dir.listFiles();
		File current;
		String fileToEnrich;
		for (int i = 0; i < directoryListing.length; i++) {
			fileToEnrich = directoryListing[i].toString().substring(directoryListing[i].toString().lastIndexOf('/'));
			addFurtherInformation(directoryListing[i].toString(), dirWithTheInformation + fileToEnrich);
		}
	}

	private void addFurtherInformation(String fileToEnrich, String fileWithTheInformation) {
		File fWTI = new File(fileWithTheInformation);
		System.out.println("File with the Information: " + fileWithTheInformation);
		JSONObject json;
		String link;
		String title;

		json = IOTool.readJsonObject(fWTI.toString());
		link = json.getString("link");
		title = json.getString("title");
		JSONArray jsonArrayToEnrich = IOTool.readJsonArray(fileToEnrich);
		for (int i = 0; i < jsonArrayToEnrich.length(); i++) {
			jsonArrayToEnrich.getJSONObject(i).put("url", link);
			jsonArrayToEnrich.getJSONObject(i).put("title", title);
		}
		FileWriter file;
		try {
			file = new FileWriter(fileToEnrich);
			file.write(jsonArrayToEnrich.toString());
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Writes an {@link JSONObject} to a file in the specified folder
	 *
	 * @param newFolder
	 *            the Folder name
	 * @param filename
	 * @param jsonObject
	 */
	private static void writeJsonObjectToDirectory(String newFolder, String filename, JSONObject jsonObject) {
		writeToDirectory(newFolder);
		if (jsonObject != null) {
			FileWriter file;
			try {
				file = new FileWriter("./" + newFolder + "/" + filename);
				file.write(jsonObject.toString());
				file.flush();
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public List<Tupel> createJsonEdgesForFile(
			AlchemyEntitiesWithFurtherInformation alchemyEntitiesWithFurtherInformation) {
		String nodeOne;
		String nodeTwo;
		List<Tupel> resultList = new ArrayList<Tupel>();
		Tupel result = null;
		for (Entities entities : alchemyEntitiesWithFurtherInformation.getEntities()) {
			for (Entity entityOne : entities.getEntities()) {
				nodeOne = entityOne.getText();
				for (Entity entityTwo : entities.getEntities()) {
					// keine Eigenkanten, keine Gerichteten kanten
					nodeTwo = entityTwo.getText();
					if (nodeOne.equals(nodeTwo) || interimResultContainsAlready(resultList, nodeOne, nodeTwo))
						continue;
					else {
						result = new Tupel(nodeOne, nodeTwo, alchemyEntitiesWithFurtherInformation.getTitle(),
								alchemyEntitiesWithFurtherInformation.getLink());
						resultList.add(result);
					}
				}
			}
		}
		return resultList;

	}

	private boolean interimResultContainsAlready(List<Tupel> resultList, String nodeOne, String nodeTwo) {
		for (Tupel tupel : resultList) {
			if ((tupel.getNodeOne().equals(nodeTwo) && tupel.getNodeTwo().equals(nodeOne))) {
				// || (tupel.getNodeOne().equals(nodeOne) &&
				// tupel.getNodeTwo().equals(nodeTwo)))
				return true;
			}
		}
		return false;
	}

	public void createTupelForHoleDirAndSave(String fromDir, String filename) {
		File dir = new File("./" + fromDir);
		File[] directoryListing = dir.listFiles();
		File current;
		List<Tupel> interimResults;
		List<Tupel> endResults = new ArrayList<Tupel>();
		boolean contains = false;

		// System.err.println("count files: " + directoryListing.length);
		for (int i = 0; i < directoryListing.length; i++) {
			current = directoryListing[i];
			// System.out.println("i=" + i + " : " + current.toString());
			if (current.toString().contains(".DS_Store"))
				continue;

			interimResults = createJsonEdgesForFile(readAndParseEntityFile(current.toString()));
			for (Tupel tupel : interimResults) {
				for (int j = 0; j < endResults.size(); j++) {
					contains = false;
					Tupel endRsultTupel = endResults.get(j);
					if ((endRsultTupel.getNodeOne().equals(tupel.getNodeOne())
							&& endRsultTupel.getNodeTwo().equals(tupel.getNodeTwo()))
							|| (endRsultTupel.getNodeOne().equals(tupel.getNodeTwo())
									&& endRsultTupel.getNodeTwo().equals(tupel.getNodeOne()))) {
						endRsultTupel.increaseCountByOne();
						endRsultTupel.setPageTitles(tupel.getPageTitles());
						endRsultTupel.setUrl(tupel.getUrls());
						contains = true;
						break;
					}
				}
				if (!contains)
					endResults.add(tupel);
			}

		}

		try {
			JSONObject jsonObject;
			JSONArray jsonArray = new JSONArray();
			int maxcount = 0;
			for (Tupel tupel : endResults) {
				jsonObject = new JSONObject();
				jsonObject.put("nodeOne", tupel.getNodeOne());
				jsonObject.put("nodeTwo", tupel.getNodeTwo());
				jsonObject.put("edge", tupel.getEdge());
				jsonObject.put("count", tupel.getCount());
				jsonObject.put("title", tupel.getPageTitles());
				jsonObject.put("url", tupel.getUrls());
				if (maxcount < tupel.getCount())
					maxcount = tupel.getCount();
				jsonArray.put(jsonObject);
			}
			// System.out.println("Maxcount: " + maxcount);
			FileWriter file;
			file = new FileWriter(filename + ".json");
			file.write(jsonArray.toString());
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<Tupel> readAndParseTupelFile(String file) {
		BufferedReader br;

		List<Tupel> resultList = new ArrayList<Tupel>();
		Tupel currentTupel;
		// read file
		try {
			br = new BufferedReader(new FileReader(file));

			String line = br.readLine();
			// parse file to json array
			JSONArray jsonArray = new JSONArray(line);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject currentJsonObject = jsonArray.getJSONObject(i);
				currentTupel = new Tupel(currentJsonObject.getString("nodeOne"), currentJsonObject.getString("nodeTwo"),
						currentJsonObject.getString("title"), currentJsonObject.getString("url"));
				currentTupel.setCount(currentJsonObject.getInt("count"));
				// System.out.println("add: " + currentTupel.tupelAsString());
				resultList.add(currentTupel);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println("#Elements parsed into List: " +
		// resultList.size());
		return resultList;

	}

}
