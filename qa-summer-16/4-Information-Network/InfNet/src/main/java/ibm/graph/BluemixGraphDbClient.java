package ibm.graph;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ibm.connection.BluemixHttpServices;
import ibm.connection.CredentialParser;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph S. and Simon S.
 */
public class BluemixGraphDbClient {

	private CloseableHttpClient client = null;
	// private String credentialsPath = "nbproject/private/graphDBCredentials";
	private String credentialsPath = "nbproject/private/smalltestGraphDBCredentials";
	BluemixHttpServices httpservices;
	private String graphId = null;

	BiMap<Integer, Vertex> vertices = null;
	BiMap<String, Edge> edges = null;

	private String sessionToken = "";

	public BluemixGraphDbClient(String graphId) {
		this.graphId = graphId;
		vertices = HashBiMap.create();
		edges = HashBiMap.create();

		if (graphId != null) {
			initialize();
		} else {
			client = HttpClients.createDefault();
			CredentialParser parser = new CredentialParser(credentialsPath);
			httpservices = new BluemixHttpServices(parser.getApiURL(), parser.getUser(), parser.getPass());
		}
	}

	private void initialize() {
		client = HttpClients.createDefault();
		CredentialParser parser = new CredentialParser(credentialsPath);
		httpservices = new BluemixHttpServices(parser.getApiURL(), parser.getUser(), parser.getPass());

		JSONObject response = httpservices.httpGET("/_session", null);
		sessionToken = "gds-token " + (String) response.get("gds-token");
		httpservices.setSessionToken(sessionToken);

		fillBiMaps();
	}

	public BluemixHttpServices getHttpServices() {
		return httpservices;
	}

	public String getGraphId() {
		return graphId;
	}

	/**
	 * 
	 * @param e
	 * @return the edgeID, or null if the edge already exists
	 */
	public String addEdge(Edge e) {

		String edgeId = edgeExists(e);
		if (edgeId == null) {
			String resultID = null;
			IBMGraphDBResponse result = httpservices.ibmPOST("/" + graphId + "/edges", e.toJSONObject());
			JSONArray data = result.getData();
			if (data.length() > 0) {
				JSONObject response = data.getJSONObject(0);
				resultID = response.getString("id");
				e.setId(resultID);
				edges.put(resultID, e);
			}
			pushedEdges++;
			return resultID;
		} else {
			// check title and url --> put
			Edge oldEdge = edges.get(edgeId);
			Edge mergedEdge = new Edge(oldEdge.getFromVertexId(), oldEdge.getToVertexId(), oldEdge.getLabel());
			for (Property p : oldEdge.getProperties()) {
				if (p.getName().equals("alchemyEdgeCount")) {
					mergedEdge.addProperty(new Property(p.getName(), Property.DATATYPE_INTEGER,
							(int) p.getValue() + (int) e.getProperty("alchemyEdgeCount").getValue()));
				} else if (p.getName().equals("title") || p.getName().equals("link")
						|| p.getName().equals("sentence")) {
					String newElement = (String) p.getValue();
					List<String> oldElements_old = new ArrayList<>(Arrays.asList(((String) p.getValue()).split(";")));
					// oldElements =
					List<String> oldElements = new ArrayList<>();
					oldElements_old.forEach(
							element -> Arrays.asList(element.split(";")).forEach(item -> oldElements.add(item)));
					if (!oldElements.contains(newElement)) {
						oldElements.add(newElement);
					}
					mergedEdge.addProperty(new Property(p.getName(), p.getDataType(), String.join(";", oldElements)));
				} else if (p.getName().equals("relationShipScore")) {
					float oldScore;
					float newScore;
					if (p.getValue() instanceof Double) {
						oldScore = ((Double) p.getValue()).floatValue();
					} else if (p.getValue() instanceof Integer) {
						oldScore = ((Integer) p.getValue()).floatValue();
					} else {
						oldScore = (float) p.getValue();
					}
					if (e.getProperty("relationShipScore").getValue() instanceof Double) {
						newScore = ((Double) e.getProperty("relationShipScore").getValue()).floatValue();
					} else if (e.getProperty("relationShipScore").getValue() instanceof Integer) {
						newScore = ((Integer) e.getProperty("relationShipScore").getValue()).floatValue();
					} else {
						newScore = (float) e.getProperty("relationShipScore").getValue();
					}
					mergedEdge.addProperty(
							new Property(p.getName(), Property.DATATYPE_FLOAT, Math.max(oldScore, newScore)));
				} else if (p.getName().equals("relationsMode")) {
					int oldVal = (int) p.getValue();
					int newVal = (int) e.getProperty("relationsMode").getValue();
					int mergedModeValue = -1;
					if (oldVal == newVal || oldVal == 15) {
						mergedModeValue = oldVal;
					} else if (oldVal < newVal) {
						mergedModeValue = oldVal + newVal;
					} else if (oldVal == 2 || oldVal == 4 || oldVal == 1 || oldVal == 8) {
						mergedModeValue = oldVal + newVal;
					} else if (newVal == 1 && oldVal % 2 == 0) {
						mergedModeValue = oldVal;
					} else if (newVal == 1 && oldVal % 2 != 0) {
						mergedModeValue = oldVal + newVal;
					} else if (newVal == 2 && Arrays.asList(new int[] { 5, 9, 12, 13 }).contains(oldVal)) {
						mergedModeValue = oldVal + newVal;
					} else if (newVal == 2 && !Arrays.asList(new int[] { 5, 9, 12, 13 }).contains(oldVal)) {
						mergedModeValue = oldVal;
					} else if (newVal == 4 && Arrays.asList(new int[] { 3, 9, 10, 11 }).contains(oldVal)) {
						mergedModeValue = oldVal + newVal;
					} else if (newVal == 4 && !Arrays.asList(new int[] { 3, 9, 10, 11 }).contains(oldVal)) {
						mergedModeValue = oldVal;
					} else if (newVal == 8 && Arrays.asList(new int[] { 3, 5, 6, 7 }).contains(oldVal)) {
						mergedModeValue = oldVal + newVal;
					} else if (newVal == 8 && !Arrays.asList(new int[] { 3, 5, 6, 7 }).contains(oldVal)) {
						mergedModeValue = oldVal;
					}

					mergedEdge.addProperty(new Property(p.getName(), Property.DATATYPE_INTEGER, mergedModeValue));
				} else {
					mergedEdge.addProperty(p);// dummyE, otherEdgeLabel
				}
			}

			JSONObject json = new JSONObject();
			JSONObject jsonProps = new JSONObject();
			for (Property p : mergedEdge.getProperties()) {
				jsonProps.put(p.getName(), p.getValue());
			}
			json.put("properties", jsonProps);
			httpservices.ibmPUT("/" + graphId + "/edges/" + edgeId, json);
			edges.put(edgeId, mergedEdge);
			puttedEdges++;
			return edgeId;
		}

	}

	public static int pushedVertices = 0;
	public static int puttedVertices = 0;
	public static int pushedEdges = 0;
	public static int puttedEdges = 0;

	public int addVertex(Vertex v) {
		return addVertex(v, false);
	}

	/**
	 * 
	 * @param v
	 *            The vertex to be added
	 * @param softVertexEquals
	 *            if true, the DB checks only for given vertices by the name. if
	 *            false, the DB checks existing vertices by name and type
	 * @return the id.
	 */
	public int addVertex(Vertex v, boolean softVertexEquals) {
		int resultID = -1;

		// first check if a vertex exists, that has the same label & name
		int vId;
		if (softVertexEquals) {
			vId = vertexExistsSoft(v);
		} else {
			vId = vertexExists(v);
		}

		if (vId >= 0) {// update vertex
			// merge Properties, yeah this looks a little ugly-.-
			Vertex oldV = vertices.get(vId);
			List<Property> mergedProps = new ArrayList<Property>();
			HashMap<String, Property> oldProps = oldV.getPropertyMap();
			HashMap<String, Property> vProps = v.getPropertyMap();
			for (String oldPropName : oldProps.keySet()) {
				if (vProps.containsKey(oldPropName)) {
					if (oldPropName.equals("relevance")) {
						float oldValue;
						if (oldProps.get(oldPropName).getValue() instanceof Double) {
							oldValue = ((Double) oldProps.get(oldPropName).getValue()).floatValue();
						} else if (oldProps.get(oldPropName).getValue() instanceof Integer) {
							oldValue = (Integer) oldProps.get(oldPropName).getValue();
						} else {
							oldValue = (float) oldProps.get(oldPropName).getValue();
						}
						float newValue;
						if (vProps.get(oldPropName).getValue() instanceof Double) {
							newValue = ((Double) vProps.get(oldPropName).getValue()).floatValue();
						} else if (vProps.get(oldPropName).getValue() instanceof Integer) {
							newValue = (Integer) vProps.get(oldPropName).getValue();
						} else {
							newValue = (float) vProps.get(oldPropName).getValue();
						}
						mergedProps
								.add(new Property(oldPropName, Property.DATATYPE_FLOAT, Math.max(oldValue, newValue)));
					} else if (oldPropName.equals("sourceURL") || oldPropName.equals("sourceTitle")
							|| oldPropName.equals("sentences")) {
						String newElement = (String) vProps.get(oldPropName).getValue();
						List<String> oldElements_old = new ArrayList<>(Arrays
								.asList(((String) oldProps.get(oldPropName).getValue()).split("__SPLIT_CHARACTER__")));
						// oldElements =
						List<String> oldElements = new ArrayList<>();
						oldElements_old
								.forEach(e -> Arrays.asList(e.split(";")).forEach(item -> oldElements.add(item)));
						if (!oldElements.contains(newElement)) {
							oldElements.add(newElement);
						}
						mergedProps.add(new Property(oldPropName, oldProps.get(oldPropName).getDataType(),
								String.join(";", oldElements)));
					} else if (oldPropName.equals("count")) {
						mergedProps.add(new Property(oldPropName, oldProps.get(oldPropName).getDataType(),
								(int) oldProps.get(oldPropName).getValue() + (int) vProps.get(oldPropName).getValue()));
					} else if (!oldPropName.equals("name") && !oldPropName.equals("dummy")
							&& !oldPropName.equals("otherVertexLabel")) {
						Property mergedP;
						if (oldProps.get(oldPropName).getDataType() == Property.DATATYPE_STRING) {
							String value = oldProps.get(oldPropName).getValue() + ","
									+ vProps.get(oldPropName).getValue();
							mergedP = new Property(oldPropName, Property.DATATYPE_STRING, value);
						} else {// put the new one in
							mergedP = new Property(oldPropName, vProps.get(oldPropName).getDataType(),
									vProps.get(oldPropName).getValue());
						}
						mergedProps.add(mergedP);
					} else {
						mergedProps.add(vProps.get(oldPropName));
					}
				} else {
					mergedProps.add(oldProps.get(oldPropName));
				}
			}
			for (String vPropName : vProps.keySet()) {
				if (!oldProps.containsKey(vPropName)) {
					mergedProps.add(vProps.get(vPropName));
				}
			}

			// push to DB
			Vertex mergedV = new Vertex(v.getLabel(), mergedProps);
			JSONObject json = new JSONObject();
			JSONObject jsonProps = new JSONObject();
			for (Property p : mergedV.getProperties()) {
				jsonProps.put(p.getName(), p.getValue());
			}
			json.put("properties", jsonProps);
			httpservices.ibmPUT("/" + graphId + "/vertices/" + vId, json);
			vertices.put(vId, mergedV);

			puttedVertices++;

			return vId;
		} else {
			IBMGraphDBResponse result = httpservices.ibmPOST("/" + graphId + "/vertices", v.toJSONObject());
			JSONArray data = result.getData();
			if (data.length() > 0) {
				JSONObject response = data.getJSONObject(0);
				v.setId(resultID);
				resultID = response.getInt("id");
				vertices.put(resultID, v);
			}
			pushedVertices++;
			return resultID;
		}
	}

	public void fillBiMaps() {
		vertices = HashBiMap.create();
		edges = HashBiMap.create();

		IBMGraphDBResponse vertexResult = httpservices.ibmGET("/" + graphId + "/vertices?dummy=dummy", null);
		IBMGraphDBResponse edgeResult = httpservices.ibmGET("/" + graphId + "/edges?dummyE=dummyE", null);

		for (int i = 0; i < vertexResult.getData().length(); i++) {
			JSONObject obj = vertexResult.getData().getJSONObject(i);
			Vertex v = Vertex.parseJSONObject(obj);
			vertices.put(v.getId(), v);
		}

		for (int i = 0; i < edgeResult.getData().length(); i++) {
			JSONObject obj = edgeResult.getData().getJSONObject(i);
			Edge e = Edge.parseJSONObject(obj);
			edges.put(e.getId(), e);
		}

		System.out.println(edges.size() + " Edges and " + vertices.size() + " read when filling the BiMaps.");
	}

	/**
	 * A vertex exists if another vertex with the same label and name is in the
	 * sotred bimap
	 * 
	 * @param v
	 * @return the vertex id if it already exists. Else: -1
	 */
	private int vertexExists(Vertex v) {
		String vName = v.getName();

		List<Entry<Integer, Vertex>> sameLabel = vertices.entrySet().stream()
				.filter(entry -> entry.getValue().getLabel().equals(v.getLabel())).collect(Collectors.toList());
		List<Entry<Integer, Vertex>> sameName = sameLabel.stream()
				.filter(entry -> entry.getValue().getName().equals(vName)).collect(Collectors.toList());

		if (sameName.size() > 0) {
			return sameName.get(0).getKey();
		} else {
			return -1;
		}
	}

	private int vertexExistsSoft(Vertex v) {
		String vName = v.getName();

		List<Entry<Integer, Vertex>> sameName = vertices.entrySet().stream()
				.filter(entry -> entry.getValue().getName().equals(vName)).collect(Collectors.toList());

		if (sameName.size() > 0) {
			return sameName.get(0).getKey();
		} else {
			return -1;
		}
	}

	/**
	 * An edge exists if there is an other edge with the same relationship,
	 * vertexFrom and vertexTo
	 * 
	 * @param e
	 * @return
	 */
	private String edgeExists(Edge e) {
		List<Edge> edgesWithSameLabel = null;
		if (e.getLabel().equals("otherE")) {
			edgesWithSameLabel = edges.entrySet().stream().map(en -> en.getValue())
					.filter(en -> en.hasProperty("otherEdgeLabel") && en.getProperty("otherEdgeLabel").getValue()
							.equals(e.getProperty("otherEdgeLabel").getValue()))
					.collect(Collectors.toList());
		} else {
			edgesWithSameLabel = edges.entrySet().stream()
					.filter(entry -> entry.getValue().getLabel().equals(e.getLabel())).map(entry -> entry.getValue())
					.collect(Collectors.toList());
		}
		Vertex from = vertices.get(e.getFromVertexId());
		Vertex to = vertices.get(e.getToVertexId());
		List<Edge> sameVertices = edgesWithSameLabel.stream().filter(en -> {
			Vertex from_it = vertices.get(en.getFromVertexId());
			Vertex to_it = vertices.get(en.getToVertexId());
			return from.getLabel().equals(from_it.getLabel()) && from.getName().equals(from_it.getName())
					&& to.getLabel().equals(to_it.getLabel()) && to.getName().equals(to_it.getName());
		}).collect(Collectors.toList());
		if (sameVertices.size() > 0) {
			return sameVertices.get(0).getId();
		} else {
			return null;
		}
	}

	/**
	 * Creates a graph with graphID. Choose an unused graphID
	 * 
	 * @param graphID
	 *            the graph ID e.g. g2
	 */
	private void createGraphById() {
		String postURLGraph = "/_graphs/" + graphId;
		httpservices.httpPOST(postURLGraph, null);
	}

	public String createGraph() {
		String postURLGraph = "/_graphs";
		return httpservices.httpPOST(postURLGraph, null).getString("graphId");
	}

	/**
	 * @param schemaFileName
	 *            the FileName or optional null
	 * @param graphID
	 *            the graph ID
	 */
	private void createSchema(String schemaFileName) {

		final String defaultSchemaFileName = "./schema-file.json";
		String chosenSchemaFileName = "";
		if (schemaFileName != null)
			chosenSchemaFileName = schemaFileName;
		else
			chosenSchemaFileName = defaultSchemaFileName;
		System.out.println("file: " + chosenSchemaFileName);

		InputStream is;
		JSONObject json;
		try {
			is = new FileInputStream(chosenSchemaFileName);
			String jsonTxt = IOUtils.toString(is);
			json = new JSONObject(jsonTxt);
			httpservices.httpPOST("/" + graphId + "/schema", json);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param graphID
	 *            the graph to delete
	 */
	public void deleteGraph() {
		// httpservices.httpDELETE("/_graphs/" + graphId);
	}

	private void changeGraphID(String graphId) {
		this.graphId = graphId;
	}

	private Edge getEdgeById(String id) {
		Edge e = null;
		IBMGraphDBResponse result = httpservices.ibmGET("/" + graphId + "/edges/" + id, null);
		JSONArray data = result.getData();
		if (data.length() > 0) {
			JSONObject jsonEdge = data.getJSONObject(0);
			e = Edge.parseJSONObject(jsonEdge);
		}
		return e;
	}

	private Vertex getVertexById(int id) {
		Vertex v = null;
		IBMGraphDBResponse result = httpservices.ibmGET("/" + graphId + "/vertices/" + id, null);
		JSONArray data = result.getData();
		if (data.length() > 0) {
			JSONObject jsonVertex = data.getJSONObject(0);
			v = Vertex.parseJSONObject(jsonVertex);
		}
		return v;
	}

	/**
	 * 
	 * @return a list of graph ids
	 */
	public List<String> getAllUsedGraphIDs() {
		List<String> graphIds = new ArrayList<String>();

		JSONObject result = httpservices.httpGET("/_graphs", null);
		JSONArray data = result.getJSONArray("graphs");
		if (data.length() > 0) {
			for (int i = 0; i < data.length(); i++) {
				graphIds.add(data.getString(i));
			}
		}

		return graphIds;
	}

	/**
	 * TODO: return something
	 * 
	 * @param graphID
	 */
	private void printSchemaForGraph() {
		httpservices.httpGET("/" + graphId + "/schema", null);
	}

	public void saveGraphAsJsonFiles() {
		saveEdgesAsJsonFile();
		saveNodesAsJsonFile();
	}

	public List<JSONObject> getAllNodesFromGraph() {
		List<JSONObject> nodes = new ArrayList<>();
		IBMGraphDBResponse result = httpservices.ibmGET("/" + graphId + "/vertices?dummy=dummy", null);
		JSONArray json = result.getData();
		for (int i = 0; i < json.length(); i++) {
			nodes.add(json.getJSONObject(i));
		}

		return nodes;
	}

	public List<JSONObject> getAllEdgesFromGraph() {
		List<JSONObject> edges = new ArrayList<>();
		IBMGraphDBResponse result = httpservices.ibmGET("/" + graphId + "/edges?dummyE=dummyE", null);
		JSONArray json = result.getData();
		for (int i = 0; i < json.length(); i++) {
			edges.add(json.getJSONObject(i));
		}
		return edges;
	}

	private void saveNodesAsJsonFile() {
		IBMGraphDBResponse result = httpservices.ibmGET("/" + graphId + "/vertices?dummy=dummy", null);
		JSONArray json = result.getData();
		FileWriter file;
		try {
			file = new FileWriter("./nodesSubsetDemo.json");
			file.write(json.toString());
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveEdgesAsJsonFile() {
		IBMGraphDBResponse result = httpservices.ibmGET("/" + graphId + "/edges?dummyE=dummyE", null);
		JSONArray json = result.getData();
		FileWriter file;
		try {
			file = new FileWriter("./edgesSubsetDemo.json");
			file.write(json.toString());
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<String> getRandomEdgeIds(int count, int relationsMode) {
		List<String> result = new ArrayList<>();
		List<Entry<String, Edge>> edgesList = new ArrayList<>(edges.entrySet());
		Random random = new Random();
		while (result.size() != count) {
			int index = random.nextInt(edges.size());
			if (!result.contains(edgesList.get(index).getKey())) {
				if (relationsMode > 0) {
					if (edgesList.get(index).getValue().hasProperty("relationsMode") && (int) edgesList.get(index)
							.getValue().getProperty("relationsMode").getValue() == relationsMode) {
						result.add(edgesList.get(index).getKey());
					}
				} else {
					result.add(edgesList.get(index).getKey());
				}
			}
		}
		return result;
	}

	public List<String> getRandomEdgeIds(int count) {
		return getRandomEdgeIds(count, -1);
	}

}
