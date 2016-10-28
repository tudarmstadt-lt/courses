package tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ibm.alchemy.relations.RelationHandler;
import ibm.alchemy.relationshipextraction.Relation;
import ibm.graph.BluemixGraphDbClient;
import ibm.graph.RelationParser;
import ibm.graph.schema.Schema;

/**
 * TODO: write down where subject, object, etc. is saved
 * 
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
public class FillGraphDBWithSOARelations {

	private final String baseFolder = "M:\\Uni\\semester12\\Question Answering Systems with IBM Watson\\files\\";
	private final String htmlFolder = baseFolder + "marvel_html_files";
	private final String extractedInfosFolder = baseFolder + "extractedInformationNEW";
	private final String relationShipsFolder = baseFolder + "soaRelationExtr";
	private final String entitiesFolder = baseFolder + "alchemyEntityExtraction";
	private final String soaFilteredFile = baseFolder + "resultSoaRelationFilteredWithEntities.json";

	private final String errorFile = baseFolder + "SOA__errorFiles.txt";
	private final String finishedFile = baseFolder + "SOA__finishedFiles.txt";
	private final String logFile = baseFolder + "SOA__log.txt";
	private List<String> finishedFiles;
	private List<String> errorFiles;
	private List<String> logLines;

	public static void main(String[] args) {
		new FillGraphDBWithSOARelations().run();

	}

	public FillGraphDBWithSOARelations() {
		try {
			finishedFiles = Files.readAllLines(Paths.get(finishedFile));
			errorFiles = Files.readAllLines(Paths.get(errorFile));
			logLines = Files.readAllLines(Paths.get(logFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private void run() {
		List<String> files = getAllFilesFromFolder(relationShipsFolder);
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

		RelationHandler handler = new RelationHandler();
		List<ibm.alchemy.relations.Relation> filteredRelations = handler.readAndParse(this.soaFilteredFile);
		List<Relation> relations = new ArrayList<>();
		for (ibm.alchemy.relations.Relation r : filteredRelations) {
			relations.add(new Relation(r.getSubject(), "SOA_ORIG_SUBJ: " + r.getOriginSubject(), r.getObject(),
					"SOA_ORIG_OBJ: " + r.getOriginObject(), r.getAction(), "0.0", "", r.getLink(), 0, 0, 0.0f, 0.0f,
					r.getOriginSentence(), 4, 0));
		}
		relationParser.parseAndPush(relations);
		addLogMsg("AllDataInOneFile" + " Vertices(pushed, putted), Edges(pushed, putted): ["
				+ BluemixGraphDbClient.pushedVertices + ", " + BluemixGraphDbClient.puttedVertices + "], ["
				+ BluemixGraphDbClient.pushedEdges + ", " + BluemixGraphDbClient.puttedEdges + "]");
		addFinishedFile("AllDataInOneFile");
	}

	private void resetCounter() {
		BluemixGraphDbClient.pushedEdges = 0;
		BluemixGraphDbClient.pushedVertices = 0;
		BluemixGraphDbClient.puttedEdges = 0;
		BluemixGraphDbClient.puttedVertices = 0;
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

}
