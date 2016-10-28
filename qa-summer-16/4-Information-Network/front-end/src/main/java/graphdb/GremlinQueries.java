package graphdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
public class GremlinQueries {

	public enum RelationTypes {
		USER_GENERATED(0), ALCHEMY_RELATIONSHIP(1), INFOBOX_RELATIONS(2), SOA_RELATIONS(4), ENTITY_GRAPH(
				8), ALCHEMY_AND_INFOBOXES(3), ALCHEMY_RELATIONS_AND_ENTITY_GRAPH(9),

		NO_FILTER(42);

		private final int value;

		RelationTypes(int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}
	}

	/**
	 * Here is a sample usage
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// if you don't know the graph id, we have to retrieve it first.
		/*
		 * BluemixGraphDbClient bgdbclient = new BluemixGraphDbClient(null);
		 * String graphId = bgdbclient.getAllUsedGraphIDs().get(0);
		 * 
		 * //this is the graphDB client we are using. bgdbclient = new
		 * BluemixGraphDbClient(graphId);
		 * 
		 * GremlinQueries gq = new GremlinQueries(bgdbclient);
		 * 
		 * //uncomment one of the following lines / blocks to archive an example
		 * usage.
		 * 
		 * //get all nodes: //List<JSONObject> allNodes = gq.getAllNodes();
		 * 
		 * //get all edges: //List<JSONObject> allEdges = gq.getAllEdges();
		 * 
		 * //get all nodes and edges where the relations have a score larger
		 * than 0.996 /*List<List<JSONObject>> nodesAndEdgesLarger =
		 * gq.getEdgesWithScoreLargerThan(0.996); List<JSONObject>
		 * nodesLargerThanX = nodesAndEdgesLarger.get(0); List<JSONObject>
		 * edgesLargerThanX = nodesAndEdgesLarger.get(1);
		 */

		// get all nodes from a one hop neighborhood from node searching 'Tony
		// Stark'.
		// List<List<JSONObject>> neighborhood =
		// gq.searchForNodeAndGetNeighborhood("Tony Stark", 1);

	}

	private BluemixGraphDbClient client;
	private List<Integer> filterRelationModes;

	public GremlinQueries(BluemixGraphDbClient client) {
		this.client = client;
		this.setFilter(RelationTypes.ALCHEMY_RELATIONSHIP);
	}

	/**
	 * Sets a filter, all queries after setting the filter will give results
	 * only relations as specified in the filter. if filterHard == false,
	 * relations that belong to multiple relationtypes can be returned.
	 * 
	 * @param relationType
	 * @param filterHard
	 */
	public void setFilter(RelationTypes relationType, boolean filterHard) {
		if (relationType == RelationTypes.NO_FILTER) {
			filterRelationModes = null;
			return;
		}
		filterRelationModes = new ArrayList<>();
		if (relationType == RelationTypes.USER_GENERATED) {
			filterRelationModes.add(0);
			return;
		}
		filterRelationModes.add(relationType.value());
		if (!filterHard) {
			switch (relationType) {
			case ALCHEMY_RELATIONSHIP:
				filterRelationModes.addAll(Arrays.asList(new Integer[] { 3, 5, 7, 9, 11, 13, 15 }));
				break;
			case INFOBOX_RELATIONS:
				filterRelationModes.addAll(Arrays.asList(new Integer[] { 3, 6, 7, 10, 11, 14, 15 }));
				break;
			case SOA_RELATIONS:
				filterRelationModes.addAll(Arrays.asList(new Integer[] { 5, 6, 7, 12, 13, 14, 15 }));
				break;
			case ENTITY_GRAPH:
				filterRelationModes.addAll(Arrays.asList(new Integer[] { 9, 10, 11, 12, 13, 14, 15 }));
				break;
			default:
				throw new IllegalArgumentException("relationType is wrong.");
			}
		}
	}

	public void setFilter(RelationTypes relationType) {
		setFilter(relationType, true);
	}

	private String getFilterQueryPart() {
		if (filterRelationModes == null) {
			return null;
		}
		String relationModes = String.join(", ",
				filterRelationModes.stream().map(i -> i.toString()).collect(Collectors.toList()));
		String result = ".has('relationsMode', within(0, " + relationModes + "))";
		return result;
	}

	/**
	 * 
	 * @return a list: 1. element the nodes 2. element: the edges
	 */
	public List<List<JSONObject>> getGraph() {
		List<List<JSONObject>> nodesAndEdges = new ArrayList<>();
		String edgeQuery = "def g = graph.traversal(); g.E().has('dummyE', 'dummyE')";
		if (filterRelationModes != null) {
			edgeQuery += getFilterQueryPart();
		}
		List<JSONObject> edges = gremlin(edgeQuery);
		List<JSONObject> nodes = new ArrayList<>();
		List<Integer> vertexIds = new ArrayList<>();
		List<JSONObject> neighboringNodes = gremlin(edgeQuery + ".bothV()");
		for (JSONObject jsonVertex : neighboringNodes) {
			if (!vertexIds.contains(jsonVertex.getInt("id"))) {
				vertexIds.add(jsonVertex.getInt("id"));
				nodes.add(jsonVertex);
			}
		}

		List<JSONObject> nodesWithRelationsMode = new ArrayList<>();
		for (JSONObject node : nodes) {
			JSONObject n = addRelationsModeToNode(node, edges);
			nodesWithRelationsMode.add(n);
		}

		List<JSONObject> edgelessNodes = getEdgelessNodes();
		for (int i = 0; i < edgelessNodes.size(); i++) {
			if (!vertexIds.contains(edgelessNodes.get(i).getInt("id"))) {
				vertexIds.add(edgelessNodes.get(i).getInt("id"));
				nodesWithRelationsMode.add(edgelessNodes.get(i));
			}
		}

		nodesAndEdges.add(nodesWithRelationsMode);
		nodesAndEdges.add(edges);
		return nodesAndEdges;
	}

	/**
	 * gets only the nodes, which contain 'namePart' in its name. Example:
	 * Searching for Stark gets nodes with the names 'Stark', 'Stark
	 * Industries', 'Tony Stark', etc.
	 * 
	 * @param namePart
	 * @return ONLY NODES
	 */
	private List<JSONObject> searchForNode(String namePart) {
		List<JSONObject> result = new ArrayList<>();
		List<JSONObject> qResult = gremlin(
				"def g = graph.traversal(); g.V().has('name', textContains('" + namePart + "'))");

		for (JSONObject n : qResult) {
			result.add(addRelationsModeToNode(n, 0));
		}

		return result;
	}

	/**
	 * gets all edges which have a relationship score > x
	 * 
	 * @param x
	 * @return a list: 1. element the nodes 2. element: the edges
	 */
	public List<List<JSONObject>> getEdgesWithScoreLargerThan(double x) {
		List<List<JSONObject>> nodesAndEdges = new ArrayList<>();
		List<Integer> vertexIds = new ArrayList<>();
		String edgeQuery = "def g = graph.traversal(); g.E().has('dummyE', 'dummyE').has('relationShipScore', gt(" + x
				+ "))";
		if (filterRelationModes != null) {
			edgeQuery += getFilterQueryPart();
		}
		List<JSONObject> edges = gremlin(edgeQuery);
		List<JSONObject> vertices = new ArrayList<>();
		List<JSONObject> tmpVertices = gremlin(edgeQuery + ".bothV()");
		for (JSONObject tmpVertex : tmpVertices) {
			if (!vertexIds.contains(tmpVertex.getInt("id"))) {
				vertexIds.add(tmpVertex.getInt("id"));
				vertices.add(tmpVertex);
			}
		}

		List<JSONObject> nodesWithRelationsMode = new ArrayList<>();
		for (JSONObject node : vertices) {
			JSONObject n = addRelationsModeToNode(node, edges);
			nodesWithRelationsMode.add(n);
		}

		nodesAndEdges.add(nodesWithRelationsMode);
		nodesAndEdges.add(edges);
		return nodesAndEdges;
	}

	/**
	 * gets the neighboring edges and the vertices connected to the edges of the
	 * specified vertex.
	 * 
	 * @param vertexId
	 * @return a list: 1. element the nodes 2. element: the edges
	 */
	public List<List<JSONObject>> getOnehopNeighborhood(int vertexId, int minCount, double minRelationshipScore,
			int maxNeighbors) {
		List<List<JSONObject>> nodesAndEdges = new ArrayList<>();

		List<Integer> vertexIds = new ArrayList<>();
		vertexIds.add(vertexId);
		String edgeQuery = "def g = graph.traversal(); g.V(" + vertexId + ").bothE()";
		if (filterRelationModes != null) {
			edgeQuery += getFilterQueryPart();
		}
		List<JSONObject> qEdges = gremlin(edgeQuery);
		List<JSONObject> vertices = new ArrayList<>();
		List<JSONObject> edges = new ArrayList<>();

		List<JSONObject> preEdges = new ArrayList<>();
		List<JSONObject> preNodes = new ArrayList<>();
		List<JSONObject> tmpVertices = gremlin(edgeQuery + ".bothV()");
		for (JSONObject tmpVertex : tmpVertices) {
			if (!vertexIds.contains(tmpVertex.getInt("id"))) {
				JSONObject properties = tmpVertex.getJSONObject("properties");
				int vCount = 0;
				if (properties.has("count")) {
					JSONArray otherVArr = properties.getJSONArray("count");
					vCount = otherVArr.getJSONObject(0).getInt("value");
				}
				if (vCount == 0 || vCount >= minCount) {
					preNodes.add(tmpVertex);
					vertexIds.add(tmpVertex.getInt("id"));
				}
				;
			}
		}

		// List<JSONObject> filteredNodes = filterNodes(preNodes, maxNeighbors);
		/*
		 * vertexIds.clear(); for(JSONObject jsonNode : filteredNodes) {
		 * vertexIds.add(jsonNode.getInt("id")); }
		 */

		for (JSONObject edge : qEdges) {
			Double relScore = 0.0;
			JSONObject properties = edge.getJSONObject("properties");
			if (properties.has("relationShipScore")) {
				relScore = properties.getDouble("relationShipScore");
			}
			if (relScore.equals(0.0) || relScore >= minRelationshipScore) {
				preEdges.add(edge);
			}
		}

		// now remove loose edges and nodes
		for (JSONObject preEdge : preEdges) {
			int node1 = preEdge.getInt("inV");
			int node2 = preEdge.getInt("outV");
			if (vertexIds.contains(node1) && vertexIds.contains(node2)) {
				edges.add(preEdge);
			}
		}

		// remove loose nodes
		Set<Integer> allNodeIdsFromEdges = new HashSet<Integer>();
		for (JSONObject edge : edges) {
			int node1 = edge.getInt("inV");
			int node2 = edge.getInt("outV");
			allNodeIdsFromEdges.add(node1);
			allNodeIdsFromEdges.add(node2);
		}
		for (JSONObject preNode : preNodes) {
			int nodeId = preNode.getInt("id");
			if (allNodeIdsFromEdges.contains(nodeId)) {
				vertices.add(preNode);
			}
		}

		List<JSONObject> nodesWithRelationsMode = new ArrayList<>();
		for (JSONObject node : vertices) {
			JSONObject n = addRelationsModeToNode(node, edges);
			nodesWithRelationsMode.add(n);
		}
		nodesAndEdges.add(nodesWithRelationsMode);
		nodesAndEdges.add(edges);
		return nodesAndEdges;
	}

	/**
	 * 
	 * @param namePart
	 * @param hops:
	 *            the number of hops, the neighborhood is large
	 * @return a list: 1. element the nodes 2. element: the edges
	 */
	public List<List<JSONObject>> searchForNodeAndGetNeighborhood(String namePart, int hops, int minCount,
			double minRelationshipScore, int maxNeighbors) {
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

		// List<Integer> filteredIds = new ArrayList<>();
		// relationShipScore
		// edges: relationShipScore(float)
		// nodes: count(int), relevance(float)
		List<JSONObject> preEdges = new ArrayList<>();
		List<JSONObject> preNodes = new ArrayList<>();
		// List<JSONObject> filteredNodes = new ArrayList<>();
		String query = "def g = graph.traversal(); g.V().has('name', textContains('" + namePart + "'))";
		for (int i = 0; i < hops; i++) {
			query += ".bothE()";
			if (filterRelationModes != null) {
				query += getFilterQueryPart();
			}
			List<JSONObject> additionalEdges = gremlin(query);
			for (JSONObject edge : additionalEdges) {
				if (!edgeIds.contains(edge.getString("id"))) {
					Double relScore = 0.0;
					JSONObject properties = edge.getJSONObject("properties");
					if (properties.has("relationShipScore")) {
						relScore = properties.getDouble("relationShipScore");
					}
					if (relScore.equals(0.0) || relScore >= minRelationshipScore) {
						preEdges.add(edge);
						edgeIds.add(edge.getString("id"));
					}
				}
			}

			query += ".bothV()";
			List<JSONObject> additionalNodes = gremlin(query);
			for (JSONObject vertex : additionalNodes) {
				if (!vertexIds.contains(vertex.getInt("id"))) {
					JSONObject properties = vertex.getJSONObject("properties");
					int vCount = 0;
					if (properties.has("count")) {
						JSONArray otherVArr = properties.getJSONArray("count");
						vCount = otherVArr.getJSONObject(0).getInt("value");
					}
					if (vCount == 0 || vCount >= minCount) {
						preNodes.add(vertex);
						// filteredIds.add(vertex.getInt("id"));
						vertexIds.add(vertex.getInt("id"));
					}
				}
			}
			/*
			 * filteredNodes = filterNodes(preNodes, maxNeighbors);
			 * 
			 * filteredIds.clear(); for(JSONObject jsonNode : filteredNodes) {
			 * filteredIds.add(jsonNode.getInt("id")); }
			 */
		}

		// have to remove edges where i removed the vertices
		edgeIds.clear();

		for (JSONObject preEdge : preEdges) {
			int node1 = preEdge.getInt("inV");
			int node2 = preEdge.getInt("outV");
			if (vertexIds.contains(node1) && vertexIds.contains(node2)) {
				edges.add(preEdge);
				edgeIds.add(preEdge.getString("id"));
			}
		}

		// remove loose nodes
		Set<Integer> allNodeIdsFromEdges = new HashSet<Integer>();
		for (JSONObject edge : edges) {
			int node1 = edge.getInt("inV");
			int node2 = edge.getInt("outV");
			allNodeIdsFromEdges.add(node1);
			allNodeIdsFromEdges.add(node2);
		}
		for (JSONObject preNode : preNodes) {
			int nodeId = preNode.getInt("id");
			if (allNodeIdsFromEdges.contains(nodeId)) {
				nodes.add(preNode);
			}
		}

		List<JSONObject> nodesWithRelationsMode = new ArrayList<>();
		for (JSONObject node : nodes) {
			JSONObject n = addRelationsModeToNode(node, edges);
			nodesWithRelationsMode.add(n);
		}

		nodesAndEdges.add(nodesWithRelationsMode);
		nodesAndEdges.add(edges);
		return nodesAndEdges;
	}

	private List<JSONObject> filterNodes(List<JSONObject> preNodes, int limit) {
		List<JSONObject> result = new ArrayList<>();
		Map<JSONObject, Double> mappedNodes = new HashMap<>();
		for (JSONObject n : preNodes) {
			double relevance = 0.0;
			JSONObject properties = n.getJSONObject("properties");
			if (properties.has("relevance")) {
				JSONArray otherVArr = properties.getJSONArray("relevance");
				relevance = otherVArr.getJSONObject(0).getDouble("value");
			}
			mappedNodes.put(n, relevance);
		}
		List<Entry<JSONObject, Double>> listMappedNodes = mappedNodes.entrySet().stream().collect(Collectors.toList());
		listMappedNodes.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
		result = listMappedNodes.stream().map(item -> item.getKey()).collect(Collectors.toList());
		return result.size() > limit ? result.subList(0, limit - 1) : result;
	}

	private JSONObject addRelationsModeToNode(JSONObject node, List<JSONObject> givenEdges) {
		JSONObject result = node;
		int id = node.getInt("id");
		for (JSONObject edge : givenEdges) {
			int node1 = edge.getInt("inV");
			int node2 = edge.getInt("outV");
			if (node1 == id || node2 == id) {
				result = addRelationsModeToNode(node, getRelationsMode(edge));
			}
		}
		return result;
	}

	public List<JSONObject> getEdgelessNodes() {
		List<JSONObject> result = gremlin(
				"def g = graph.traversal(); g.V().has('dummy', 'dummy').group().by(bothE().count()).limit(local, 1)");
		if (result.size() != 1) {
			return new ArrayList<JSONObject>();
		}
		JSONObject groupedNodes = result.get(0);
		if (groupedNodes.has("0")) {
			JSONArray edgelessNodesArr = groupedNodes.getJSONArray("0");
			List<JSONObject> edgelessNodes = new ArrayList<>();
			for (int i = 0; i < edgelessNodesArr.length(); i++) {
				edgelessNodes.add(addRelationsModeToNode(edgelessNodesArr.getJSONObject(i), 0));
			}
			return edgelessNodes;
		} else {
			return new ArrayList<JSONObject>();
		}
	}

	private JSONObject addRelationsModeToNode(JSONObject node, int relationsMode) {
		JSONObject result = node;
		result.put("relationsMode", relationsMode);
		return result;
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

	/*
	 * =========================================================== Add / Edit /
	 * Remove functionality down here
	 * ===========================================================
	 */

	/**
	 * checks if a vertex with the given type(label) or name exists.
	 * 
	 * @param type
	 * @param name
	 * @return the id of the existing node. -1 if the node doesn't exist
	 */
	public int nodeExists(String type, String name) {
		Vertex v = buildVertex(type, name);
		return client.vertexExists(v);
	}

	/**
	 * @param name
	 * @param type
	 * @return the vertex id. if the node already exists, the id of the existing
	 *         node will be returned. Check with nodeExists(String, String)
	 *         before.
	 */
	public int addNode(String type, String name) {
		Vertex v = buildVertex(type, name);
		return client.addVertex(v);
	}

	/**
	 * Removes the vertex with the given id AND all connected edges.
	 * 
	 * @param id
	 *            the id of the vertex to be removed.
	 */
	public void removeNode(int id) {
		gremlin("def g = graph.traversal(); g.V(" + id + ").drop()");
	}

	/**
	 * Can only edit the name. Labels are immutable
	 * 
	 * @param nodeId
	 *            the id of the node to be edited. TODO: change label of
	 *            otherVertexLabel
	 * @param name
	 */
	public void editNode(int nodeId, String name, String otherLabel) {
		client.editNode(nodeId, name, otherLabel);
	}

	/**
	 * 
	 * @param fromId
	 * @param toId
	 * @param edgeLabel
	 * @return the edgeID
	 */
	public String addEdge(int fromId, int toId, String edgeLabel) {
		Edge e = buildEdge(fromId, toId, edgeLabel);
		return client.addEdge(e);
	}

	/**
	 * Should only delete the edge, not the vertices
	 * 
	 * @param id
	 */
	public void removeEdge(String id) {
		gremlin("def g = graph.traversal(); g.E('" + id + "').drop()");
	}

	public void editEdge(String id, String otherEdgeLabel) {
		client.editEdge(id, otherEdgeLabel);
	}

	/**
	 * 
	 * @param fromId
	 * @param toId
	 * @param edgeLabel
	 * @return the Id of the edge, if the edge exists. If edge doesn't exist,
	 *         this method returns null
	 */
	public String edgeExists(int fromId, int toId, String edgeLabel) {
		Edge e = buildEdge(fromId, toId, edgeLabel);
		return client.edgeExists(e);
	}

	private Edge buildEdge(int fromId, int toId, String edgeLabel) {
		List<String> givenLabelsBySchema = Arrays
				.asList(new String[] { "employedby", "affectedby", "partofmany", "locatedat", "agentof" });
		Edge e;
		if (givenLabelsBySchema.contains(edgeLabel.toLowerCase())) {
			e = new Edge(fromId, toId, edgeLabel);
		} else {
			e = new Edge(fromId, toId, "otherE");
			e.addProperty(new Property("otherEdgeLabel", Property.DATATYPE_STRING, edgeLabel));
		}
		e.addProperty(new Property("dummyE", Property.DATATYPE_STRING, "dummyE"));
		e.addProperty(new Property("relationsMode", Property.DATATYPE_INTEGER, 0));
		return e;
	}

	private Vertex buildVertex(String type, String name) {
		List<String> givenLabelsBySchema = Arrays
				.asList(new String[] { "person", "facility", "organization", "city", "location", "gpe" });
		Vertex v;
		if (givenLabelsBySchema.contains(type.toLowerCase())) {
			v = new Vertex(type);
		} else {
			v = new Vertex("otherV");
			v.addProperty(new Property("otherVertexLabel", Property.DATATYPE_STRING, type));
		}
		v.addProperty(new Property("name", Property.DATATYPE_STRING, name));
		v.addProperty(new Property("dummy", Property.DATATYPE_STRING, "dummy"));
		return v;
	}

	/**
	 * @param id
	 * @return a list of 3 elements: 1. the edge, 2. the from node 3. the to
	 *         node
	 */
	public List<JSONObject> getRelationById(String id) {
		List<JSONObject> result = new ArrayList<>();
		JSONObject edge = gremlin("def g = graph.traversal(); g.E('" + id + "')").get(0);
		JSONObject node1 = gremlin("def g = graph.traversal(); g.E('" + id + "').inV()").get(0);
		JSONObject node2 = gremlin("def g = graph.traversal(); g.E('" + id + "').outV()").get(0);

		int relMode = getRelationsMode(edge);
		node1 = addRelationsModeToNode(node1, relMode);
		node2 = addRelationsModeToNode(node2, relMode);

		result.add(edge);
		result.add(node1);
		result.add(node2);
		return result;
	}

	private int getRelationsMode(JSONObject edge) {
		int result = 0;
		if (edge.has("properties")) {
			JSONObject properties = edge.getJSONObject("properties");
			if (properties.has("relationsMode")) {
				result = properties.getInt("relationsMode");
			}
		}
		return result;
	}

	/**
	 * 
	 * @param idsEvaluatedEdges
	 * @param randomEdgeIdsFromFiles
	 * @return the Id of the edge, randomly selected among not evaluated edges
	 *         (allEdges - evaluatedEdges).
	 */
	public String getRandomEdge(Set<String> idsEvaluatedEdges, List<String> randomEdgeIdsFromFiles) {
		Random random = new Random();
		String element = randomEdgeIdsFromFiles.get(random.nextInt(randomEdgeIdsFromFiles.size()));
		while (idsEvaluatedEdges.contains(element)) {
			element = randomEdgeIdsFromFiles.get(random.nextInt(randomEdgeIdsFromFiles.size()));
		}
		return element;
	}

}
