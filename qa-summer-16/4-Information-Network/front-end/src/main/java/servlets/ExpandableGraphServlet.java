package servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import beans.Edge;
import beans.Node;
import graphdb.BluemixGraphDbClient;
import graphdb.GremlinQueries;
import graphdb.GremlinQueries.RelationTypes;
import util.GraphDBToBeanParser;

/**
 *
 * We don't want to parse files/directories here. Just get the data from the
 * pipeline.
 * 
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon D.
 */

public class ExpandableGraphServlet extends HttpServlet {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -5460344045754108469L;

	String nodeId1 = null;
	String nodeId2 = null;
	String edgeId = null;

	String evalNodesFile = "resultsEvalNodes.txt";
	String evalEdgesFile = "resultsEvalEdges.txt";

	Boolean node1Eval = false;
	Boolean node2Eval = false;

	private static final int MIN_VERTEX_COUNT = 0;
	private static final double MIN_RELATIONSHIP_SCORE = 0;
	private static final int MAX_NEIGHBORS = 1000000;

	GremlinQueries gq = null;

	@SuppressWarnings("unused")
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String credentialsContents = readFile("/graphDBCredentials");
		// String credentialsContents =
		// readFile("/smalltestGraphDBCredentials");

		// BluemixGraphDbClient bgdbclient = new
		// BluemixGraphDbClient(credentialsContents, "g");
		BluemixGraphDbClient bgdbclient = new BluemixGraphDbClient(credentialsContents,
				"35e5b152-4189-4c2d-9486-a5cc6066f12b");

		gq = new GremlinQueries(bgdbclient);
		gq.setFilter(RelationTypes.NO_FILTER);
		List<List<JSONObject>> searchedGraph = gq.searchForNodeAndGetNeighborhood("Howard", 1, MIN_VERTEX_COUNT,
				MIN_RELATIONSHIP_SCORE, MAX_NEIGHBORS);
		// List<List<JSONObject>> nodesAndEdges = gq.getGraph();
		List<Node> nodes = GraphDBToBeanParser.parseNodes(searchedGraph.get(0));
		List<Edge> edges = GraphDBToBeanParser.parseEdges(searchedGraph.get(1));
		System.out.println(nodes.size() + " nodes and " + edges.size() + " edges retrieved from GraphDB.");
		// Map<String, Object> attributes = new HashMap<String, Object>();
		request.setAttribute("nodes", nodes);
		request.setAttribute("edges", edges);

		// request.setAttribute( ATT_NODES, nodesJSON );
		// request.setAttribute( ATT_EDGES, edgesJSON );

		this.getServletContext().getRequestDispatcher("/WEB-INF/expandableGraph.jsp").forward(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		System.out.println("POST SmallestTestServlet");
		Map<String, String[]> params = request.getParameterMap();
		System.out.println(params.toString());
		String action = null;
		if (params.containsKey("action")) {
			action = params.get("action")[0];
			System.out.println("action=" + action);
			if (action.equals("deleteNode")) {
				String nodeId = params.get("nodeId")[0];
				System.out.println("delete node " + nodeId);
				gq.removeNode(Integer.valueOf(nodeId));
			} else if (action.equals("addEdge")) {
				nodeId1 = params.get("nodeId1")[0];
				nodeId2 = params.get("nodeId2")[0];
				System.out.println("add edge between " + nodeId1 + " and " + nodeId2);
				if (gq == null)
					System.out.println("gq==null");
				gq.addEdge(Integer.valueOf(nodeId1), Integer.valueOf(nodeId2), "NO_EDGE_LABEL_DEFINED"); // TODO
																											// deal
																											// with
																											// edge
																											// label
			} else if (action.equals("deleteEdge")) {
				String edgeId = params.get("edgeId")[0];
				System.out.println("delete edge " + edgeId);
				gq.removeEdge(edgeId);
			} else if (action.equals("evaluate")) {
				System.out.println("action evaluate");
				Map<String, List<Integer>> resultsEvalNodes = null;
				File resultsEvalFile = new File(evalNodesFile);
				if (resultsEvalFile.exists()) {
					resultsEvalNodes = util.EvalMethods.readResults(resultsEvalFile);
				} else {
					resultsEvalNodes = new HashMap<String, List<Integer>>();
				}

				Map<String, List<Integer>> resultsEvalEdges = null;
				File resultsEvalEdgesFile = new File(evalEdgesFile);
				if (resultsEvalEdgesFile.exists()) {
					resultsEvalEdges = util.EvalMethods.readResults(resultsEvalEdgesFile);
				} else {
					resultsEvalEdges = new HashMap<String, List<Integer>>();
				}
				Set<String> evaluatedEdgeIds = resultsEvalEdges.keySet();
				List<String> randomEdgeIdsFromFiles = readEdgeIdFiles();

				String idNewEdgeToEvaluate = gq.getRandomEdge(evaluatedEdgeIds, randomEdgeIdsFromFiles);

				// String idNewEdgeToEvaluate = "oe591-m94-kk5-j2g";
				String json = "{\"edgeId\":\"" + idNewEdgeToEvaluate + "\"}";
				System.out.println(json);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write(json);
				// this.getServletContext().getRequestDispatcher(
				// "/WEB-INF/smalltest.jsp" ).forward( request, response);

			} else if (action.equals("evalResults")) {

				System.out.println("Evaluation results");

				// Node 1
				Boolean node1Good = null;
				String idNode1 = null;
				if (!params.containsKey("idNode1")) {
					System.out.println("problem: no id of node1 in eval results on servlet side");
				} else {

					idNode1 = params.get("idNode1")[0];
					String node1ResString = params.get("node1Res")[0];

					if (node1ResString.equals("true"))
						node1Good = true;
					else if (node1ResString.equals("false"))
						node1Good = false;
					else
						System.out.println("PROBLEM: node1ResString = " + node1ResString);
				}
				// Node 2
				Boolean node2Good = null;
				String idNode2 = null;
				if (params.containsKey("idNode2")) {
					idNode2 = params.get("idNode2")[0];
					String node2ResString = params.get("node2Res")[0];
					if (node2ResString.equals("true"))
						node2Good = true;
					else if (node2ResString.equals("false"))
						node2Good = false;
					else
						System.out.println("PROBLEM: node2ResString = " + node2ResString);
				}

				// Edge
				Boolean edgeGood = null;
				String idEdge = null;
				if (params.containsKey("idEdge")) {
					idEdge = params.get("idEdge")[0];
					String edgeResString = params.get("edgeRes")[0];

					if (edgeResString.equals("true"))
						edgeGood = true;
					else if (edgeResString.equals("false"))
						edgeGood = false;
					else
						System.out.println("PROBLEM: edgeResString = " + edgeResString);
				}

				// Read file with nodes eval results
				Map<String, List<Integer>> resultsEvalNodes = null;
				File resultsEvalNodesFile = new File(evalNodesFile);
				if (resultsEvalNodesFile.exists()) {
					resultsEvalNodes = util.EvalMethods.readResults(resultsEvalNodesFile);
				} else {
					resultsEvalNodes = new HashMap<String, List<Integer>>();
				}
				Set<String> evaluatedNodeIds = resultsEvalNodes.keySet();

				// Read file with edges eval results
				Map<String, List<Integer>> resultsEvalEdges = null;
				File resultsEvalEdgesFile = new File(evalEdgesFile);
				if (resultsEvalEdgesFile.exists()) {
					resultsEvalEdges = util.EvalMethods.readResults(resultsEvalEdgesFile);
				} else {
					resultsEvalEdges = new HashMap<String, List<Integer>>();
				}
				Set<String> evaluatedEdgeIds = resultsEvalEdges.keySet();

				if (node1Good != null) {
					Integer resEval = 0;
					if (node1Good)
						resEval = 1;
					if (resultsEvalNodes.containsKey(idNode1)) {
						resultsEvalNodes.get(idNode1).add(resEval);
					} else {
						resultsEvalNodes.put(idNode1, new ArrayList<Integer>(resEval));
					}
				}
				if (node2Good != null) {
					Integer resEval = 0;
					if (node2Good)
						resEval = 1;
					if (resultsEvalNodes.containsKey(idNode2)) {
						resultsEvalNodes.get(idNode2).add(resEval);
					} else {
						resultsEvalNodes.put(idNode2, new ArrayList<Integer>(resEval));
					}
				}

				// Write results nodes
				writeResultsIntoFile(getServletContext().getRealPath("/") + evalNodesFile, resultsEvalNodes);

				if (edgeGood != null) {
					Integer resEval = 0;
					if (edgeGood)
						resEval = 1;
					if (resultsEvalEdges.containsKey(idEdge)) {
						resultsEvalEdges.get(idEdge).add(resEval);
					} else {
						resultsEvalEdges.put(idEdge, new ArrayList<Integer>(resEval));
					}

					// Write results edges
					writeResultsIntoFile(getServletContext().getRealPath("/") + evalEdgesFile, resultsEvalEdges);

				}

				// String idNewEdgeToEvaluate =
				// gq.getRandomEdge(evaluatedEdgeIds);
				String idNewEdgeToEvaluate = "oe591-m94-kk5-j2g";

				String json = "{\"edgeId\":\"" + idNewEdgeToEvaluate + "\"}";
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write(json);
				System.out.println(json);

			} else if (action.equals("addNode")) {

				String name = params.get("name")[0];
				String type = params.get("type")[0];
				System.out.println("add node with name= " + name + " and type= " + type);
				gq.addNode(type, name);

			} else if (action.equals("editNode")) {

				String id = params.get("id")[0];
				String name = params.get("name")[0];
				String type = params.get("type")[0];
				System.out.println("modify node: id= " + id + ", name= " + name + " and type= " + type);
				gq.editNode(Integer.valueOf(id), name, type);

			} else if (action.equals("editEdge")) {

				String id = params.get("id")[0];
				String label = params.get("label")[0];
				System.out.println("modify edge: id= " + id + ", label= " + label);
				gq.editEdge(id, label);

			} else if (action.equals("expand")) {

				String nodeId = params.get("nodeId")[0];
				System.out.println("Expanding graph with neighbours of node " + nodeId);
				List<List<JSONObject>> neighbours = gq.getOnehopNeighborhood(Integer.valueOf(nodeId), MIN_VERTEX_COUNT,
						MIN_RELATIONSHIP_SCORE, MAX_NEIGHBORS);
				List<Node> nodes = GraphDBToBeanParser.parseNodes(neighbours.get(0));
				List<Edge> edges = GraphDBToBeanParser.parseEdges(neighbours.get(1));
				Iterator<Node> itNodes = nodes.iterator();
				StringWriter swJsonNodes = new StringWriter();
				swJsonNodes.append("[");
				while (itNodes.hasNext()) {
					swJsonNodes.append(itNodes.next().toJson());
					swJsonNodes.append(",");
				}
				swJsonNodes.getBuffer().setLength(swJsonNodes.getBuffer().length() - 1);
				swJsonNodes.append("]");
				String nodesJson = swJsonNodes.toString();

				Iterator<Edge> itEdges = edges.iterator();
				StringWriter swJsonEdges = new StringWriter();
				swJsonEdges.append("[");
				while (itEdges.hasNext()) {
					swJsonEdges.append(itEdges.next().toJson());
					swJsonEdges.append(",");
				}
				swJsonEdges.getBuffer().setLength(swJsonEdges.getBuffer().length() - 1);
				swJsonEdges.append("]");
				String edgesJson = swJsonEdges.toString();
				System.out.println("nodes = " + nodesJson);
				System.out.println("edges = " + edgesJson);

				String json = "{\"nodes\":" + nodesJson + ",\"edges\":" + edgesJson + "}";
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write(json);

			} else if (action.equals("search")) {

				String nodeLabel = params.get("nodeLabel")[0];
				System.out.println("nodeLabel = " + nodeLabel);
				/*
				 * List<JSONObject> potentialNodes =
				 * gq.searchForNode(nodeLabel); for (JSONObject potentialNode :
				 * potentialNodes){
				 * System.out.println(potentialNode.toString()); }
				 */
				List<List<JSONObject>> neighbours = gq.searchForNodeAndGetNeighborhood(nodeLabel, 1, MIN_VERTEX_COUNT,
						MIN_RELATIONSHIP_SCORE, MAX_NEIGHBORS);
				List<Node> nodes = GraphDBToBeanParser.parseNodes(neighbours.get(0));
				List<Edge> edges = GraphDBToBeanParser.parseEdges(neighbours.get(1));
				Iterator<Node> itNodes = nodes.iterator();
				StringWriter swJsonNodes = new StringWriter();
				swJsonNodes.append("[");
				while (itNodes.hasNext()) {
					swJsonNodes.append(itNodes.next().toJson());
					swJsonNodes.append(",");
				}
				swJsonNodes.getBuffer().setLength(swJsonNodes.getBuffer().length() - 1);
				swJsonNodes.append("]");
				String nodesJson = swJsonNodes.toString();

				Iterator<Edge> itEdges = edges.iterator();
				StringWriter swJsonEdges = new StringWriter();
				swJsonEdges.append("[");
				while (itEdges.hasNext()) {
					swJsonEdges.append(itEdges.next().toJson());
					swJsonEdges.append(",");
				}
				swJsonEdges.getBuffer().setLength(swJsonEdges.getBuffer().length() - 1);
				swJsonEdges.append("]");
				String edgesJson = swJsonEdges.toString();
				System.out.println("nodes = " + nodesJson);
				System.out.println("edges = " + edgesJson);

				String json = "{\"nodes\":" + nodesJson + ",\"edges\":" + edgesJson + "}";
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write(json);

			} else {
				System.out.println("action else");
			}

		}
	}

	private List<String> readEdgeIdFiles() {
		List<String> filenames = Arrays.asList(new String[] { "/smallGraphEdgeIds.txt" });
		// List<String> filenames = Arrays.asList(new String[] {
		// "100EdgeIds__all_kinds.txt", "50EdgeIds__alchemy.txt",
		// "50EdgeIds__entity_graph.txt", "50EdgeIds__infobox.txt",
		// "50EdgeIds__soa.txt" });
		List<String> edgeIds = new ArrayList<>();
		for (String filename : filenames) {
			List<String> edgeIdsInFile = readFileLineWise(filename);
			edgeIdsInFile.forEach(item -> {
				if (!edgeIds.contains(item))
					edgeIds.add(item);
			});
		}
		return edgeIds;
	}

	public static void writeResultsIntoFile(String resultsEvalFile, Map<String, List<Integer>> resultsEvalMap)
			throws IOException {

		ArrayList<String> output = new ArrayList<String>();
		for (String elementId : resultsEvalMap.keySet()) {
			String line = elementId + "\t";
			for (Integer res : resultsEvalMap.get(elementId)) {
				line += res + "\t";
			}
			output.add(line);
		}
		Path path = Paths.get(resultsEvalFile);
		Files.write(path, output, StandardCharsets.UTF_8);
	}

	public static String readFile(String fileName) {

		InputStream is = ExpandableGraphServlet.class.getResourceAsStream(fileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String ls = System.getProperty("line.separator");
		String line = null;
		try {
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append(ls);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	private List<String> readFileLineWise(String filename) {
		InputStream is = ExpandableGraphServlet.class.getResourceAsStream(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		List<String> result = new ArrayList<>();
		String line = null;
		try {
			while ((line = br.readLine()) != null) {
				result.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

}
