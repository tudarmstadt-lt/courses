package servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import beans.Edge;
import beans.Node;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon D.
 */
public class GraphVizAlchemyEntitiesServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -130861909289206227L;
	private static final String ATT_NODES = "nodesJSON";
	private static final String ATT_EDGES = "edgesJSON";

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String nodesJSON = readNodesJson("/nodesAlchemyEntitiesWithoutText.json");
		String edgesJSON = readEdgesJson("/edgesAlchemyEntitiesWithoutText.json");
		List<Node> nodes = getNodes(nodesJSON);
		analyzeAndAddGroups(nodes);
		List<Edge> edges = getEdges(edgesJSON);
		// Map<String, Object> attributes = new HashMap<String, Object>();
		request.setAttribute("nodes", nodes);
		request.setAttribute("edges", edges);

		// request.setAttribute( ATT_NODES, nodesJSON );
		// request.setAttribute( ATT_EDGES, edgesJSON );

		this.getServletContext().getRequestDispatcher("/WEB-INF/graphViz.jsp").forward(request, response);
	}

	private void analyzeAndAddGroups(List<Node> nodes) {
		Map<String, Integer> entityTypeToGroupId = new HashMap<String, Integer>();
		for (Node node : nodes) {
			if (!entityTypeToGroupId.containsKey(node.getType()))
				entityTypeToGroupId.put(node.getType(), entityTypeToGroupId.size());
			node.setGroup(entityTypeToGroupId.get(node.getType()));
		}
	}

	private List<Edge> getEdges(String edgesJSON) {

		List<Edge> edges = new ArrayList<Edge>();
		JSONArray array = new JSONArray(edgesJSON);
		for (int i = 0; i < array.length(); i++) {
			JSONObject edgeJson = array.getJSONObject(i);
			int node1 = edgeJson.getInt("inV");
			int node2 = edgeJson.getInt("outV");
			String title = edgeJson.getString("label");
			// Edge edge = new Edge(""+edges.size(), node1, node2, 0., title);
			// edges.add(edge);
		}
		return edges;
	}

	private List<Node> getNodes(String nodesJSON) {

		List<Node> nodes = new ArrayList<Node>();
		JSONArray array = new JSONArray(nodesJSON);
		for (int i = 0; i < array.length(); i++) {
			JSONObject nodeJson = array.getJSONObject(i);
			int id = nodeJson.getInt("id");
			String type = nodeJson.get("label").toString();
			String value = null;
			JSONObject properties = nodeJson.getJSONObject("properties");
			JSONArray name = properties.getJSONArray("name");
			JSONObject obj = name.getJSONObject(0);
			value = obj.get("value").toString();
			// Node node = new Node(id, value, type);
			// nodes.add(node);
		}
		return nodes;
	}

	private String readEdgesJson(String fileName) {

		String edgesJson = null;
		try {
			edgesJson = readJsonFromFile(fileName);
			System.out.println(edgesJson);
			// JSONArray array = new JSONArray(edgesJson);
			// edgesJson = array.toString();
			/*
			 * JSONObject obj = new JSONObject(edgesJson); if
			 * (obj.has("result")){ JSONObject result =
			 * obj.getJSONObject("result"); if (result.has("data")){ JSONArray
			 * data = result.getJSONArray("data"); edgesJson = data.toString();
			 * System.out.println(edgesJson); } }
			 */
		} catch (IOException e) {
			e.printStackTrace();
		}
		return edgesJson;
	}

	private String readNodesJson(String fileName) {

		String nodesJson = null;
		try {
			nodesJson = readJsonFromFile(fileName);
			System.out.println(nodesJson);
			// JSONArray array = new JSONArray(nodesJson);
			// nodesJson = array.toString();
			/*
			 * JSONObject obj = new JSONObject(nodesJson); if
			 * (obj.has("result")){ JSONObject result =
			 * obj.getJSONObject("result"); if (result.has("data")){ JSONArray
			 * data = result.getJSONArray("data"); nodesJson = data.toString();
			 * System.out.println(nodesJson); } }
			 */

		} catch (IOException e) {
			e.printStackTrace();
		}
		return nodesJson;
	}

	private String readJsonFromFile(String fileName) throws IOException {

		InputStream is = GraphVizAlchemyEntitiesServlet.class.getResourceAsStream(fileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = br.readLine();
		return line;
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}

}
