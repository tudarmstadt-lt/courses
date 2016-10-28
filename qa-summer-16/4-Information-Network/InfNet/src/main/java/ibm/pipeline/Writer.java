package ibm.pipeline;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import ibm.graph.BluemixGraphDbClient;
import ibm.graph.Edge;
import ibm.graph.Property;
import ibm.graph.Vertex;
import ibm.graph.schema.Schema;
import ibm.graph.schema.SchemaHandling;
import ibm.pipeline.Pipeline.TransitionType;
import ibm.tools.Collectiontool;
import ibm.tools.IOTool;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
@Deprecated
public class Writer extends AbstractTransition {

	public Writer(String sourceFolder) {
		super(sourceFolder, "dontNeedThisHERE. Its just for dummy reasons.");
	}

	@Override
	public void run() {
		int vectors = 0;
		int edges = 0;
		int cntFiles = 0;
		BluemixGraphDbClient.pushedVertices = 0;
		BluemixGraphDbClient.puttedVertices = 0;
		// 1) Read files -> JSON Objects
		// 2) JSON Objects -> Instances of Vertex and Edge
		// 3) Instances -> DB.
		BluemixGraphDbClient client = new BluemixGraphDbClient("82f218fe-7fe5-4714-a6af-8f5fff39bd08");
		SchemaHandling schemaHandling = new SchemaHandling(client);
		Schema schema = schemaHandling.getSchema();

		File[] files = new File(sourceFolder).listFiles();
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
			System.out.println("File " + (++cntFiles) + " '" + f.getName() + "' read and pushed.");
		}

		System.out.println("Added " + vectors + " vectors and " + edges + " edges.");
		System.out.println("pushed " + BluemixGraphDbClient.pushedVertices + " vectors and putted "
				+ BluemixGraphDbClient.puttedVertices + " vectors.");
	}

	@Override
	public TransitionType getTransitionType() {
		return TransitionType.WRITER;
	}

}
