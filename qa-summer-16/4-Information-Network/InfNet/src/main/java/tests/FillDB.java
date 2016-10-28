package tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import ibm.graph.BluemixGraphDbClient;
import ibm.graph.Edge;
import ibm.graph.Property;
import ibm.graph.Vertex;
import ibm.graph.schema.EdgeIndex;
import ibm.graph.schema.EdgeLabel;
import ibm.graph.schema.Schema;
import ibm.graph.schema.SchemaHandling;
import ibm.graph.schema.VertexIndex;
import ibm.tools.IOTool;
import ibm.tools.Collectiontool;

/**
 * So here are some statistics: we have 33 different labels for vertices and 41
 * different labels for edges
 * 
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
@Deprecated
public class FillDB {

	private static String GRAPHDB_ID = "82f218fe-7fe5-4714-a6af-8f5fff39bd08";

	/**
	 * to use this, uncomment the necessary methods/lines
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// 1) some infos
		// printTypeInfo(getAllEntityTypes("./nodesAndEdges"));
		// printTypeInfo(getAllRelationNames("./nodesAndEdges"));

		// 2) set schema and/or read files
		setSchemaForDemo();
		// readFilesAndPutThemInDB("./nodesAndEdges");

		// 3) get all vertices and edges and save them
		// BluemixGraphDbClient client = new BluemixGraphDbClient(GRAPHDB_ID);
		// client.saveGraphAsJsonFiles();
	}

	private static void printTypeInfo(Map<String, Integer> entityTypes) {
		entityTypes = Collectiontool.sortByValue(entityTypes);
		for (String type : entityTypes.keySet()) {
			System.out.println(type + ": " + entityTypes.get(type));
		}
		System.out.println("Found " + entityTypes.size() + " different labels.");
	}

	/**
	 * Gets all the types of the entities. With that we update the schema.
	 * 
	 * @param string
	 * @return
	 */
	private static HashMap<String, Integer> getAllEntityTypes(String directory) {
		HashMap<String, Integer> labels = new HashMap<>();
		File[] files = new File(directory).listFiles();
		for (File f : files) {
			JSONArray relationShips = IOTool.readJsonArray(f.getAbsolutePath());
			for (int i = 0; i < relationShips.length(); i++) {
				JSONObject obj = relationShips.getJSONObject(i);
				String t1 = obj.getString("entityOneType");
				String t2 = obj.getString("entityTwoType");

				if (!labels.containsKey(t1)) {
					labels.put(t1, 0);
				}
				if (!labels.containsKey(t2)) {
					labels.put(t2, 0);
				}
				labels.replace(t1, labels.get(t1) + 1);
				labels.replace(t2, labels.get(t2) + 1);
			}

		}
		return labels;
	}

	private static HashMap<String, Integer> getAllRelationNames(String directory) {
		HashMap<String, Integer> labels = new HashMap<>();
		File[] files = new File(directory).listFiles();
		for (File f : files) {
			JSONArray relationShips = IOTool.readJsonArray(f.getAbsolutePath());
			for (int i = 0; i < relationShips.length(); i++) {
				JSONObject obj = relationShips.getJSONObject(i);
				String rel = obj.getString("relation");
				if (!labels.containsKey(rel)) {
					labels.put(rel, 0);
				}
				labels.replace(rel, labels.get(rel) + 1);
			}

		}
		return labels;
	}

	private static void setSchemaForDemo() {

		BluemixGraphDbClient client = new BluemixGraphDbClient(GRAPHDB_ID);

		/*
		 * Uncomment the following lines to delete the old graph (with data and
		 * scheme. Create a new EMPTY graph. NECESSARY For demo.(if the scheme
		 * is not already set correctly!
		 */
		client.deleteGraph();
		String graphId = client.createGraph();
		System.out.println("Created graph with id '" + graphId + "'");
		client = new BluemixGraphDbClient(graphId);

		SchemaHandling schemaHandling = new SchemaHandling(client);

		// vertex labels
		Schema s = new Schema();
		s.addVertexLabel("Person");
		s.addVertexLabel("Facility");
		s.addVertexLabel("Organization");
		s.addVertexLabel("City");
		s.addVertexLabel("Location");
		s.addVertexLabel("GPE");
		s.addVertexLabel("otherV");

		// Edge labels
		s.addEdgeLabel(new EdgeLabel("employedBy", "MULTI"));
		s.addEdgeLabel(new EdgeLabel("affectedBy", "MULTI"));
		s.addEdgeLabel(new EdgeLabel("partOfMany", "MULTI"));
		s.addEdgeLabel(new EdgeLabel("locatedAt", "MULTI"));
		s.addEdgeLabel(new EdgeLabel("agentOf", "MULTI"));
		s.addEdgeLabel(new EdgeLabel("otherE", "MULTI"));

		// vertexProperties
		Property p1 = new Property("relevance", Property.DATATYPE_FLOAT, null);
		Property p2 = new Property("sourceURL", Property.DATATYPE_STRING, null);
		Property p3 = new Property("sourceTitle", Property.DATATYPE_STRING, null);
		Property p4 = new Property("name", Property.DATATYPE_STRING, null);
		Property p5 = new Property("count", Property.DATATYPE_INTEGER, null);
		Property p6 = new Property("dummy", Property.DATATYPE_STRING, null);
		Property p7 = new Property("otherVertexLabel", Property.DATATYPE_STRING, null);
		// EdgeProperties
		Property pe1 = new Property("relationShipScore", Property.DATATYPE_FLOAT, null);
		Property pe2 = new Property("title", Property.DATATYPE_STRING, null);
		Property pe3 = new Property("link", Property.DATATYPE_STRING, null);
		Property pe4 = new Property("otherEdgeLabel", Property.DATATYPE_STRING, null);
		Property pe5 = new Property("dummyE", Property.DATATYPE_STRING, null);

		s.setPropertyKeys(Arrays.asList(p1, p2, p3, p4, p5, p6, p7));
		s.addPropertyKey(pe1);
		s.addPropertyKey(pe2);
		s.addPropertyKey(pe3);
		s.addPropertyKey(pe4);
		s.addPropertyKey(pe5);

		// vertex indicies
		VertexIndex v1 = new VertexIndex("vByRelevance");
		VertexIndex v2 = new VertexIndex("vBysourceURL");
		VertexIndex v3 = new VertexIndex("vBysourceTitle");
		VertexIndex v4 = new VertexIndex("vByName");
		VertexIndex v5 = new VertexIndex("vByCount");
		VertexIndex v6 = new VertexIndex("vByDummy");
		VertexIndex v7 = new VertexIndex("vByOtherVLabel");
		v1.addPropertyKey(new Property("relevance", -1, null));
		v1.setComposite(true);
		v2.addPropertyKey(new Property("sourceURL", -1, null));
		v3.addPropertyKey(new Property("sourceTitle", -1, null));
		v4.addPropertyKey(new Property("name", -1, null));
		v4.setComposite(true);
		v5.addPropertyKey(new Property("count", -1, null));
		v5.setComposite(true);
		v6.addPropertyKey(new Property("dummy", -1, null));
		v6.setComposite(true);
		v7.addPropertyKey(new Property("otherVertexLabel", -1, null));
		v7.setComposite(true);
		s.setVertexIndicies(Arrays.asList(v1, v2, v3, v4, v5, v6, v7));

		// edge indicies
		EdgeIndex e1 = new EdgeIndex("eByrelScore");
		EdgeIndex e2 = new EdgeIndex("eByTitle");
		EdgeIndex e3 = new EdgeIndex("eByLink");
		EdgeIndex e4 = new EdgeIndex("eOtherEdgeLabel");
		EdgeIndex e5 = new EdgeIndex("eByDummy");
		e1.addPropertyKey(new Property("relationShipScore", -1, null));
		e1.setComposite(true);
		e2.addPropertyKey(new Property("title", -1, null));
		e2.setComposite(true);
		e3.addPropertyKey(new Property("link", -1, null));
		e3.setComposite(true);
		e4.addPropertyKey(new Property("otherEdgeLabel", -1, null));
		e4.setComposite(true);
		e5.addPropertyKey(new Property("dummyE", -1, null));
		e5.setComposite(true);
		s.setEdgeIndicies(Arrays.asList(e1, e2, e3, e4, e5));

		schemaHandling.setSchema(s);

	}

	/**
	 * TODO:very important: need to check for existing vertices/edges now this
	 * creates only a bunch of 2-pair vertices, connected with an edge.
	 * 
	 * @param directory
	 */
	private static void readFilesAndPutThemInDB(String directory) {

		int vectors = 0;
		int edges = 0;
		int cntFiles = 0;
		// 1) Read files -> JSON Objects
		// 2) JSON Objects -> Instances of Vertex and Edge
		// 3) Instances -> DB.
		BluemixGraphDbClient client = new BluemixGraphDbClient(GRAPHDB_ID);
		SchemaHandling schemaHandling = new SchemaHandling(client);
		Schema schema = schemaHandling.getSchema();

		File[] files = new File(directory).listFiles();
		for (File f : files) {
			JSONArray relationShips = IOTool.readJsonArray(f.getAbsolutePath());
			for (int i = 0; i < relationShips.length(); i++) {
				JSONObject obj = relationShips.getJSONObject(i);
				String label1 = obj.getString("entityOneType");
				String label2 = obj.getString("entityTwoType");
				String name1 = obj.getString("entityOne");
				String name2 = obj.getString("entityTwo");
				float score = (float) obj.getDouble("score");
				String link = obj.getString("link");
				String title = obj.getString("title");
				String relation = obj.getString("relation");// -->label

				Vertex v1, v2;
				if (Collectiontool.containsCaseless(schema.getVertexLabels(), label1)) {
					v1 = new Vertex(label1);
				} else {
					v1 = new Vertex("otherV");
					v1.addProperty(new Property("otherVertexLabel", Property.DATATYPE_STRING, label1));
				}
				if (Collectiontool.containsCaseless(schema.getVertexLabels(), label2)) {
					v2 = new Vertex(label2);
				} else {
					v2 = new Vertex("otherV");
					v2.addProperty(new Property("otherVertexLabel", Property.DATATYPE_STRING, label2));
				}
				v1.addProperty(new Property("name", Property.DATATYPE_STRING, name1));
				v2.addProperty(new Property("name", Property.DATATYPE_STRING, name2));
				v1.addProperty(new Property("dummy", Property.DATATYPE_STRING, "dummy"));
				v2.addProperty(new Property("dummy", Property.DATATYPE_STRING, "dummy"));
				vectors += 2;

				int v1Id = client.addVertex(v1);
				int v2Id = client.addVertex(v2);

				List<String> edgeLabels = schema.getEdgeLabels().stream().map(el -> el.getLabel())
						.collect(Collectors.toList());
				Edge e1;
				if (Collectiontool.containsCaseless(edgeLabels, relation)) {
					e1 = new Edge(v1Id, v2Id, relation);
				} else {
					e1 = new Edge(v1Id, v2Id, "otherE");
					e1.addProperty(new Property("otherEdgeLabel", Property.DATATYPE_STRING, relation));
				}
				e1.addProperty(new Property("link", Property.DATATYPE_STRING, link));
				e1.addProperty(new Property("title", Property.DATATYPE_STRING, title));
				e1.addProperty(new Property("relationShipScore", Property.DATATYPE_FLOAT, score));
				e1.addProperty(new Property("dummyE", Property.DATATYPE_STRING, "dummyE"));

				client.addEdge(e1);
				edges++;
			}
			System.out.println("File " + (cntFiles++) + " read and pushed.");
		}

		System.out.println("Added " + vectors + " vectors and " + edges + " edges.");
	}

	/**
	 * these methods are for testing if a vertex/edge already exist. Need to
	 * capsulate them in the next sprint.
	 */

}
