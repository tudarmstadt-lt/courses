package tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import ibm.alchemy.relationshipextraction.InfoBoxRelationsHandler;
import ibm.alchemy.relationshipextraction.Relation;
import ibm.graph.BluemixGraphDbClient;
import ibm.graph.RelationParser;
import ibm.graph.schema.Schema;
import ibm.tools.IOTool;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
public class FillDBWithInfoboxRelations {

	private final String baseFolder = "M:\\Uni\\semester12\\Question Answering Systems with IBM Watson\\files\\";
	private final String infoboxRelFolder = baseFolder + "infoboxRelations";
	private final String extractedInfosFolder = baseFolder + "extractedInformationNEW";
	private final String errorFile = baseFolder + "infoboxes_errorFiles.txt";
	private final String finishedFile = baseFolder + "infoboxes_finishedFiles.txt";
	private final String logFile = baseFolder + "infoboxes_log.txt";
	private List<String> finishedFiles;
	private List<String> errorFiles;
	private List<String> logLines;

	public static void main(String[] args) {
		new FillDBWithInfoboxRelations().run();
	}

	public FillDBWithInfoboxRelations() {
		try {
			finishedFiles = Files.readAllLines(Paths.get(finishedFile));
			errorFiles = Files.readAllLines(Paths.get(errorFile));
			logLines = Files.readAllLines(Paths.get(logFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void run() {
		List<String> files_extractedInfo = getAllFilesFromFolder(extractedInfosFolder);

		// initiate GraphDB client
		BluemixGraphDbClient client = new BluemixGraphDbClient("35e5b152-4189-4c2d-9486-a5cc6066f12b");

		// set the schema
		Schema schema = Schema.getSchema();

		// push to db
		RelationParser relationParser = new RelationParser(schema, client);
		BluemixGraphDbClient.pushedVertices = 0;
		BluemixGraphDbClient.puttedVertices = 0;
		BluemixGraphDbClient.pushedEdges = 0;
		BluemixGraphDbClient.puttedEdges = 0;

		InfoBoxRelationsHandler relHandler = new InfoBoxRelationsHandler();
		for (String filename_eI : files_extractedInfo) {
			JSONObject extractedInfo = IOTool.readJsonObject(extractedInfosFolder + "\\" + filename_eI);
			String filename_EE = extractedInfo.getString("filename");
			String link_EE = extractedInfo.getString("link");
			String title_EE = extractedInfo.getString("title");
			if (finishedFiles.contains(filename_EE)) {
				continue;
			}
			if (fileExists(infoboxRelFolder + "\\" + filename_EE + ".json")) {
				resetCounter();
				JSONArray arr = IOTool.readJsonArray(infoboxRelFolder + "\\" + filename_EE + ".json");
				List<Relation> relations = relHandler.createRelationsData(arr, title_EE, link_EE);
				if (relations == null) {
					addErrorFile(filename_EE + ": couldn't parse relations. jsonRelation = '" + arr.toString() + "' ");
					continue;
				}
				relationParser.parseAndPush(relations);
				addLogMsg(filename_EE + " Vertices(pushed, putted), Edges(pushed, putted): ["
						+ BluemixGraphDbClient.pushedVertices + ", " + BluemixGraphDbClient.puttedVertices + "], ["
						+ BluemixGraphDbClient.pushedEdges + ", " + BluemixGraphDbClient.puttedEdges + "]");
				addFinishedFile(filename_EE);
			} else {
				addErrorFile(filename_eI + " couldn't find file in entities folder");
			}
		}
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

	private void resetCounter() {
		BluemixGraphDbClient.pushedEdges = 0;
		BluemixGraphDbClient.pushedVertices = 0;
		BluemixGraphDbClient.puttedEdges = 0;
		BluemixGraphDbClient.puttedVertices = 0;
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
}
