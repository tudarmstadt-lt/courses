package graphdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph S. and Simon S.
 */
public class BluemixGraphDbClient {

	private CloseableHttpClient client = null;
	private String credentialsPath = "/graphDBCredentials";
	BluemixHttpServices httpservices;
	private String graphId = null;

	// BiMap<Integer, Vertex> vertices = null;
	// BiMap<String, Edge> edges = null;

	private String sessionToken = "";

	public BluemixGraphDbClient(String credentialsFileContent, String graphId) {
		this.graphId = graphId;
		// vertices = HashBiMap.create();
		// edges = HashBiMap.create();

		if (graphId != null) {
			initialize(credentialsFileContent);
		} else {
			client = HttpClients.createDefault();
			CredentialParser parser = new CredentialParser(null);
			parser.parse(credentialsFileContent);
			httpservices = new BluemixHttpServices(parser.getApiURL(), parser.getUser(), parser.getPass());
		}
	}

	private void initialize(String credentialsFileContent) {
		client = HttpClients.createDefault();
		CredentialParser parser = new CredentialParser(null);
		parser.parse(credentialsFileContent);
		httpservices = new BluemixHttpServices(parser.getApiURL(), parser.getUser(), parser.getPass());

		JSONObject response = httpservices.httpGET("/_session", null);
		sessionToken = "gds-token " + (String) response.get("gds-token");
		httpservices.setSessionToken(sessionToken);

		// fillBiMaps();
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

		if (edgeExists(e) != null) {
			return null;
		}

		String resultID = null;
		IBMGraphDBResponse result = httpservices.ibmPOST("/" + graphId + "/edges", e.toJSONObject());
		JSONArray data = result.getData();
		if (data.length() > 0) {
			JSONObject response = data.getJSONObject(0);
			resultID = response.getString("id");
			// edges.put(resultID, e);
		}
		return resultID;
	}

	public static int pushedVertices = 0;
	public static int puttedVertices = 0;

	/**
	 * 
	 * @param v
	 *            The vertex to be added
	 * @return the id.
	 */
	public int addVertex(Vertex v) {
		int resultID = -1;

		// first check if a vertex exists, that has the same label & name
		int vId = vertexExists(v);
		if (vId >= 0) {// update vertex
			// merge Properties, yeah this looks a little ugly-.-
			// Vertex oldV = vertices.get(vId);
			Vertex oldV = getVertexById(vId);
			List<Property> mergedProps = new ArrayList<Property>();
			HashMap<String, Property> oldProps = oldV.getPropertyMap();
			HashMap<String, Property> vProps = v.getPropertyMap();
			for (String oldPropName : oldProps.keySet()) {
				if (vProps.containsKey(oldPropName)) {
					if (oldPropName.equals("count")) {
						mergedProps.add(new Property(oldPropName, oldProps.get(oldPropName).getDataType(),
								(int) oldProps.get(oldPropName).getValue() + 1));
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
			// vertices.put(vId, mergedV);

			puttedVertices++;

			return vId;
		} else {
			IBMGraphDBResponse result = httpservices.ibmPOST("/" + graphId + "/vertices", v.toJSONObject());
			JSONArray data = result.getData();
			if (data.length() > 0) {
				JSONObject response = data.getJSONObject(0);
				resultID = response.getInt("id");
				// vertices.put(resultID, v);
			}
			pushedVertices++;
			return resultID;
		}
	}

	/*
	 * public void fillBiMaps() { vertices = HashBiMap.create(); edges =
	 * HashBiMap.create();
	 * 
	 * 
	 * IBMGraphDBResponse vertexResult = httpservices.ibmGET("/" + graphId +
	 * "/vertices?dummy=dummy", null); IBMGraphDBResponse edgeResult =
	 * httpservices.ibmGET("/" + graphId + "/edges?dummyE=dummyE", null);
	 * 
	 * for(int i = 0; i < vertexResult.getData().length(); i++) { JSONObject obj
	 * = vertexResult.getData().getJSONObject(i); Vertex v =
	 * Vertex.parseJSONObject(obj); vertices.put(v.getId(), v); }
	 * 
	 * for(int i = 0; i < edgeResult.getData().length(); i++) { JSONObject obj =
	 * edgeResult.getData().getJSONObject(i); Edge e =
	 * Edge.parseJSONObject(obj); edges.put(e.getId(), e); }
	 * 
	 * System.out.println(edges.size() + " Edges and " + vertices.size() +
	 * " read."); }
	 */

	/**
	 * A vertex exists if another vertex with the same label and name is in the
	 * sotred bimap
	 * 
	 * @param v
	 * @return the vertex id if it already exists. Else: -1
	 */
	public int vertexExists(Vertex v) {
		String vName = v.getName();

		List<JSONObject> sameLabelAndName = gremlin("def g = graph.traversal(); g.V().has('dummy', 'dummy').hasLabel('"
				+ v.getLabel() + "').has('name', '" + v.getName() + "')");

		// List<Entry<Integer, Vertex>> sameLabel =
		// vertices.entrySet().stream().filter(entry ->
		// entry.getValue().getLabel().equals(v.getLabel())).collect(Collectors.toList());
		// List<Entry<Integer, Vertex>> sameName =
		// sameLabel.stream().filter(entry ->
		// entry.getValue().getName().equals(vName)).collect(Collectors.toList());

		if (sameLabelAndName.size() > 0) {
			return sameLabelAndName.get(0).getInt("id");
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
	public String edgeExists(Edge e) {
		List<Edge> edgesWithSameLabel = new ArrayList<>();
		if (e.getLabel().equals("otherE")) {
			List<JSONObject> jsonEdges = gremlin(
					"def g = graph.traversal(); g.E().has('dummyE', 'dummyE').hasLabel('otherE').has('otherEdgeLabel', '"
							+ e.getProperty("otherEdgeLabel").getValue().toString() + "')");
			for (JSONObject j : jsonEdges)
				edgesWithSameLabel.add(Edge.parseJSONObject(j));
		} else {
			List<JSONObject> jsonEdges = gremlin(
					"def g = graph.traversal(); g.E().has('dummyE', 'dummyE').hasLabel('" + e.getLabel() + "')");
			for (JSONObject j : jsonEdges)
				edgesWithSameLabel.add(Edge.parseJSONObject(j));
		}
		Vertex from = getVertexById(e.getFromVertexId());
		Vertex to = getVertexById(e.getToVertexId());
		List<Edge> sameVertices = edgesWithSameLabel.stream().filter(en -> {
			Vertex from_it = getVertexById(en.getFromVertexId());
			Vertex to_it = getVertexById(en.getToVertexId());
			return from.getLabel().equals(from_it.getLabel()) && from.getName().equals(from_it.getName())
					&& to.getLabel().equals(to_it.getLabel()) && to.getName().equals(to_it.getName());
		}).collect(Collectors.toList());
		return sameVertices.size() > 0 ? sameVertices.get(0).getId() : null;
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

	public void editNode(int nodeId, String name, String otherLabel) {
		Vertex oldV = getVertexById(nodeId);
		List<Property> putProperties = new ArrayList<>();
		for (Property p : oldV.getProperties()) {
			if (p.getName().equals("otherVertexLabel")) {
				putProperties.add(new Property("otherVertexLabel", Property.DATATYPE_STRING, otherLabel));
			} else if (p.getName().equals("name")) {
				putProperties.add(new Property("name", Property.DATATYPE_STRING, name));
			} else {
				putProperties.add(p);
			}
		}
		if (!oldV.hasProperty("otherVertexLabel")) {
			putProperties.add(new Property("otherVertexLabel", Property.DATATYPE_STRING, otherLabel));
		}

		oldV.setProperties(putProperties);
		// vertices.put(nodeId, oldV);

		JSONObject json = new JSONObject();
		JSONObject jsonProps = new JSONObject();
		for (Property p : putProperties) {
			jsonProps.put(p.getName(), p.getValue());
		}
		json.put("properties", jsonProps);
		httpservices.ibmPUT("/" + graphId + "/vertices/" + nodeId, json);

	}

	public void editEdge(String id, String otherEdgeLabel) {

		Edge oldEdge = getEdgeById(id);
		Edge putEdge = new Edge(oldEdge.getFromVertexId(), oldEdge.getToVertexId(), oldEdge.getLabel());
		putEdge.setId(oldEdge.getId());
		for (Property p : oldEdge.getProperties()) {
			if (p.getName().equals("otherEdgeLabel")) {
				putEdge.addProperty(new Property("otherEdgeLabel", Property.DATATYPE_STRING, otherEdgeLabel));
			} else {
				putEdge.addProperty(p);
			}
		}
		if (!putEdge.hasProperty("otherEdgeLabel")) {
			putEdge.addProperty(new Property("otherEdgeLabel", Property.DATATYPE_STRING, otherEdgeLabel));
		}

		// edges.put(id, putEdge);
		JSONObject json = new JSONObject();
		JSONObject jsonProps = new JSONObject();
		for (Property p : putEdge.getProperties()) {
			jsonProps.put(p.getName(), p.getValue());
		}
		json.put("properties", jsonProps);
		httpservices.ibmPUT("/" + graphId + "/edges/" + id, json);
	}

	private List<JSONObject> gremlin(String query) {
		List<JSONObject> elements = new ArrayList<>();

		JSONObject queryObj = new JSONObject();
		queryObj.put("gremlin", query);
		IBMGraphDBResponse result = httpservices.ibmPOST("/" + getGraphId() + "/gremlin", queryObj);
		JSONArray jsonData = result.getData();
		for (int i = 0; i < jsonData.length(); i++) {
			elements.add(jsonData.getJSONObject(i));
		}
		return elements;
	}

}
