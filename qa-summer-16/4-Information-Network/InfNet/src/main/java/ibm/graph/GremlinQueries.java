package ibm.graph;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
public class GremlinQueries {

	/**
	 * Here is a sample usage
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// if you don't know the graph id, we have to retrieve it first.
		BluemixGraphDbClient bgdbclient = new BluemixGraphDbClient(null);
		String graphId = bgdbclient.getAllUsedGraphIDs().get(0);

		// this is the graphDB client we are using.
		bgdbclient = new BluemixGraphDbClient(graphId);

		GremlinQueries gq = new GremlinQueries(bgdbclient);

		// uncomment one of the following lines / blocks to achive an example
		// usage.

		// get all nodes:
		// List<JSONObject> allNodes = gq.getAllNodes();

		// get all edges:
		// List<JSONObject> allEdges = gq.getAllEdges();

		// get all nodes and edges where the relations have a score larger than
		// 0.996
		/*
		 * List<List<JSONObject>> nodesAndEdgesLarger =
		 * gq.getEdgesWithScoreLargerThan(0.996); List<JSONObject>
		 * nodesLargerThanX = nodesAndEdgesLarger.get(0); List<JSONObject>
		 * edgesLargerThanX = nodesAndEdgesLarger.get(1);
		 */

		// get all nodes from a one hop neighboorhood from node searching 'Tony
		// Stark'.
		List<List<JSONObject>> neighborhood = gq.searchForNodeAndGetNeighborhood("Tony Stark", 1);

	}

	private BluemixGraphDbClient client;

	public GremlinQueries(BluemixGraphDbClient client) {
		this.client = client;
	}

	public List<JSONObject> getAllNodes() {
		return gremlin("def g = graph.traversal(); g.V().has('dummy', 'dummy')");
	}

	public List<JSONObject> getAllEdges() {
		return gremlin("def g = graph.traversal(); g.E().has('dummyE', 'dummyE')");
	}

	/**
	 * gets only the nodes, which contain 'namePart' in its name. Example:
	 * Searching for Stark gets nodes with the names 'Stark', 'Stark
	 * Industries', 'Tony Stark', etc.
	 * 
	 * @param namePart
	 * @return ONLY NODES
	 */
	public List<JSONObject> searchForNode(String namePart) {
		return gremlin("def g = graph.traversal(); g.V().has('name', textContains('" + namePart + "'))");
	}

	/**
	 * gets all edges which have a relationship score > x
	 * 
	 * @param x
	 * @return a list: 1. element the nodes 2. element: the edges
	 */
	public List<List<JSONObject>> getEdgesWithScoreLargerThan(double x) {
		List<List<JSONObject>> nodesAndEdges = new ArrayList<>();
		nodesAndEdges
				.add(gremlin("def g = graph.traversal(); g.E().has('dummyE', 'dummyE').has('relationShipScore', gt(" + x
						+ ")).bothV()"));
		nodesAndEdges.add(gremlin(
				"def g = graph.traversal(); g.E().has('dummyE', 'dummyE').has('relationShipScore', gt(" + x + "))"));
		return nodesAndEdges;
	}

	/**
	 * 
	 * @param namePart
	 * @param hops:
	 *            the number of hops, the neighborhood is large
	 * @return a list: 1. element the nodes 2. element: the edges
	 */
	public List<List<JSONObject>> searchForNodeAndGetNeighborhood(String namePart, int hops) {
		if (hops <= 0) {
			throw new IllegalArgumentException();
		}

		List<JSONObject> nodes = searchForNode(namePart);
		List<JSONObject> edges = new ArrayList<>();
		List<List<JSONObject>> nodesAndEdges = new ArrayList<>();
		List<String> edgeIds = new ArrayList<>();
		List<Integer> vertexIds = new ArrayList<>();
		for (JSONObject foundNode : nodes) {
			vertexIds.add(foundNode.getInt("id"));
		}

		String query = "def g = graph.traversal(); g.V().has('name', textContains('" + namePart + "'))";
		for (int i = 0; i < hops; i++) {
			query += ".bothE()";
			List<JSONObject> additionalEdges = gremlin(query);
			for (JSONObject edge : additionalEdges) {
				if (!edgeIds.contains(edge.getString("id"))) {
					edges.add(edge);
					edgeIds.add(edge.getString("id"));
				}
			}

			query += ".bothV()";
			List<JSONObject> additionalNodes = gremlin(query);
			for (JSONObject vertex : additionalNodes) {
				if (!vertexIds.contains(vertex.getInt("id"))) {
					nodes.add(vertex);
					vertexIds.add(vertex.getInt("id"));
				}
			}
		}

		nodesAndEdges.add(nodes);
		nodesAndEdges.add(edges);
		return nodesAndEdges;
	}

	/**
	 * IMPORTANT NOTICE: I don't know if this method works correctly. Use
	 * searchForNodeAndGetNeighborhood() instead. CAN be significantly faster
	 * for larger hops and datasets
	 * 
	 * TODO: - need to check for correctness: compare with the other method.
	 * Best on a larger set and visually. (hops ~= 3)
	 * 
	 * 
	 * 
	 * @param namePart
	 * @param hops
	 * @return
	 */
	public List<List<JSONObject>> searchForNodeAndGetNeighborhoodFAST(String namePart, int hops) {
		if (hops <= 0) {
			throw new IllegalArgumentException();
		}
		List<List<JSONObject>> nodesAndEdges = new ArrayList<>();
		String query = "def g = graph.traversal(); g.V().has('name', textContains('" + namePart + "'))";
		for (int i = 0; i < hops - 1; i++) {
			query += ".bothE().bothV()";
		}
		query += ".bothE()";
		List<JSONObject> edges = gremlin(query);
		List<JSONObject> nodes = gremlin(query + ".bothV()");
		List<JSONObject> checkedEdges = new ArrayList<>();
		List<JSONObject> checkedNodes = new ArrayList<>();

		List<String> edgeIds = new ArrayList<>();
		List<Integer> vertexIds = new ArrayList<>();
		for (JSONObject n : nodes) {
			if (!vertexIds.contains(n.getInt("id"))) {
				vertexIds.add(n.getInt("id"));
				checkedNodes.add(n);
			}
		}

		for (JSONObject e : edges) {
			if (!edgeIds.contains(e.getString("id"))) {
				edgeIds.add(e.getString("id"));
				checkedEdges.add(e);
			}
		}

		nodesAndEdges.add(checkedNodes);
		nodesAndEdges.add(checkedEdges);
		return nodesAndEdges;
	}

	/**
	 * executes a gremlin query
	 * 
	 * @param query
	 * @return the data which results from the query.
	 */
	private List<JSONObject> gremlin(String query) {
		List<JSONObject> elements = new ArrayList<>();

		JSONObject queryObj = new JSONObject();
		queryObj.put("gremlin", query);
		IBMGraphDBResponse result = client.httpservices.ibmPOST("/" + client.getGraphId() + "/gremlin", queryObj);
		JSONArray jsonData = result.getData();
		for (int i = 0; i < jsonData.length(); i++) {
			elements.add(jsonData.getJSONObject(i));
		}
		return elements;
	}
}
