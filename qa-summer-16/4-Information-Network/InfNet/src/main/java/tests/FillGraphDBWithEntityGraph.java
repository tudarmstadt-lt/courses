package tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import ibm.alchemy.entity.AlchemyEntitiesHandler;
import ibm.alchemy.entity.Tupel;
import ibm.alchemy.relationshipextraction.Relation;
import ibm.alchemy.relationshipextraction.RelationsHandler;
import ibm.graph.BluemixGraphDbClient;
import ibm.graph.RelationParser;
import ibm.graph.schema.Schema;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
public class FillGraphDBWithEntityGraph {

	private final String baseFolder = "M:\\Uni\\semester12\\Question Answering Systems with IBM Watson\\files\\";
	private final String graphFile = "M:\\Uni\\semester12\\Question Answering Systems with IBM Watson\\files\\entityOnlyGraphThreshold.6.json";
	private final String logFile = baseFolder + "EntitiyGraph__log.txt";
	private List<String> logLines;

	public static void main(String[] args) {
		new FillGraphDBWithEntityGraph().run();
	}

	public FillGraphDBWithEntityGraph() {
		try {
			logLines = Files.readAllLines(Paths.get(logFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private void run() {

		/**
		 * Whatsup with count.. may need an extra field in the schema
		 */
		List<Tupel> edges = AlchemyEntitiesHandler.readAndParseTupelFile(graphFile);
		List<Relation> relations = new ArrayList<Relation>();
		for (Tupel t : edges) {
			relations.add(new Relation(t.getNodeOne(), "", t.getNodeTwo(), "", t.getEdge(), "0.0", t.getPageTitles(),
					t.getUrls(), 0, 0, 0.0f, 0.0f, "", 8, t.getCount()));
		}

		System.out.println("relations parsed.");
		BluemixGraphDbClient client = new BluemixGraphDbClient("35e5b152-4189-4c2d-9486-a5cc6066f12b");// TODO:
																										// use
																										// other
																										// id

		// set the schema
		Schema schema = Schema.getSchema();

		// push to db
		RelationParser relationParser = new RelationParser(schema, client);
		BluemixGraphDbClient.pushedVertices = 0;
		BluemixGraphDbClient.puttedVertices = 0;
		BluemixGraphDbClient.pushedEdges = 0;
		BluemixGraphDbClient.puttedEdges = 0;

		relationParser.parseAndPush(relations, true);
		addLogMsg("AllDataInOneFile" + " Vertices(pushed, putted), Edges(pushed, putted): ["
				+ BluemixGraphDbClient.pushedVertices + ", " + BluemixGraphDbClient.puttedVertices + "], ["
				+ BluemixGraphDbClient.pushedEdges + ", " + BluemixGraphDbClient.puttedEdges + "]");
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

}
