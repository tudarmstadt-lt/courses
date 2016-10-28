package servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
public class SmallestTestServlet extends HttpServlet {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -5460344045754108469L;

	String nodeId1 = null;
	String nodeId2 = null;
	String edgeId = null;

	String evalNodesFile = "resultsEvalNodes.txt";
	String evalEdgesFile = "resultsEvalEdges.txt";

	private static final String GRAPH_ID = "217f900e-b876-4e4b-bed3-7b56b25c3a67";

	Boolean node1Eval = false;
	Boolean node2Eval = false;

	GremlinQueries gq = null;

	@SuppressWarnings("unused")
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// String credentialsContents = readFile("/graphDBCredentials");
		String credentialsContents = ExpandableGraphServlet.readFile("/smalltestGraphDBCredentials");

		// BluemixGraphDbClient bgdbclient = new
		// BluemixGraphDbClient(credentialsContents, "g");
		BluemixGraphDbClient bgdbclient = new BluemixGraphDbClient(credentialsContents, GRAPH_ID);

		gq = new GremlinQueries(bgdbclient);
		gq.setFilter(RelationTypes.NO_FILTER);
		List<List<JSONObject>> nodesAndEdges = gq.getGraph();
		List<Node> nodes = GraphDBToBeanParser.parseNodes(nodesAndEdges.get(0));
		List<Edge> edges = GraphDBToBeanParser.parseEdges(nodesAndEdges.get(1));
		System.out.println(nodes.size() + " nodes and " + edges.size() + " edges retrieved from GraphDB.");
		// Map<String, Object> attributes = new HashMap<String, Object>();
		request.setAttribute("nodes", nodes);
		request.setAttribute("edges", edges);

		// request.setAttribute( ATT_NODES, nodesJSON );
		// request.setAttribute( ATT_EDGES, edgesJSON );

		this.getServletContext().getRequestDispatcher("/WEB-INF/smalltest.jsp").forward(request, response);
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
				Map<String, List<Integer>> resultsEvalNodes = getResultsEvalNodes(evalNodesFile);

				Map<String, List<Integer>> resultsEvalEdges = getResultsEvalEdges(evalEdgesFile);

				Set<String> evaluatedEdgeIds = resultsEvalEdges.keySet();
				List<String> randomEdgeIdsFromFiles = readEdgeIdFiles();

				String idNewEdgeToEvaluate = gq.getRandomEdge(evaluatedEdgeIds, randomEdgeIdsFromFiles);
				List<JSONObject> relation = gq.getRelationById(idNewEdgeToEvaluate);
				Edge e1 = GraphDBToBeanParser.parseEdge(relation.get(0));
				Node from = GraphDBToBeanParser.parseNode(relation.get(1));
				Node to = GraphDBToBeanParser.parseNode(relation.get(2));

				// String idNewEdgeToEvaluate = "oe591-m94-kk5-j2g";
				String json = "{\"edge\":" + e1.toJson() + ",\"node1\":" + from.toJson() + ",\"node2\":" + to.toJson()
						+ "}";
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
				Map<String, List<Integer>> resultsEvalNodes = getResultsEvalNodes(evalNodesFile);
				Set<String> evaluatedNodeIds = resultsEvalNodes.keySet();

				// Read file with edges eval results
				Map<String, List<Integer>> resultsEvalEdges = getResultsEvalEdges(evalEdgesFile);
				Set<String> evaluatedEdgeIds = resultsEvalEdges.keySet();

				processNodesEvalResults(idNode1, idNode2, node1Good, node2Good, resultsEvalNodes, evalNodesFile);
				processEdgeEvalResults(edgeGood, resultsEvalEdges, idEdge, evalEdgesFile);

				/*
				 * for (String el : resultsEvalNodes.keySet()){
				 * System.out.print("node = "+el); for (Integer res :
				 * resultsEvalNodes.get(el)){ System.out.print("\t"+res); }
				 * System.out.println(); }
				 */

				List<String> randomEdgeIdsFromFiles = readEdgeIdFiles();
				String idNewEdgeToEvaluate = gq.getRandomEdge(evaluatedEdgeIds, randomEdgeIdsFromFiles);
				List<JSONObject> relation = gq.getRelationById(idNewEdgeToEvaluate);
				Edge e1 = GraphDBToBeanParser.parseEdge(relation.get(0));
				Node from = GraphDBToBeanParser.parseNode(relation.get(1));
				Node to = GraphDBToBeanParser.parseNode(relation.get(2));
				// String idNewEdgeToEvaluate = "oe591-m94-kk5-j2g";

				String json = "{\"edge\":" + e1.toJson() + ",\"node1\":" + from.toJson() + ",\"node2\":" + to.toJson()
						+ "}";
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

			} else if (action.equals("selectGroup")) {

				System.out.println("action selectGroup");
				// TODO

			}

		}
	}

	private void processNodesEvalResults(String idNode1, String idNode2, Boolean node1Good, Boolean node2Good,
			Map<String, List<Integer>> resultsEvalNodes, String evalNodesFile2) throws IOException {

		if (node1Good != null) {
			Integer resEval = 0;
			if (node1Good)
				resEval = 1;
			if (!resultsEvalNodes.containsKey(idNode1)) {
				resultsEvalNodes.put(idNode1, new ArrayList<Integer>(resEval));
			}
			resultsEvalNodes.get(idNode1).add(resEval);
		}

		if (node2Good != null) {
			Integer resEval = 0;
			if (node2Good)
				resEval = 1;
			if (!resultsEvalNodes.containsKey(idNode2)) {
				resultsEvalNodes.put(idNode2, new ArrayList<Integer>(resEval));
			}
			resultsEvalNodes.get(idNode2).add(resEval);
		}

		// Write results nodes
		ExpandableGraphServlet.writeResultsIntoFile(getServletContext().getRealPath("/") + evalNodesFile,
				resultsEvalNodes);

	}

	private void processEdgeEvalResults(Boolean edgeGood, Map<String, List<Integer>> resultsEvalEdges, String idEdge,
			String evalEdgesFile2) throws IOException {

		if (edgeGood != null) {
			Integer resEval = 0;
			if (edgeGood)
				resEval = 1;
			if (!resultsEvalEdges.containsKey(idEdge)) {
				resultsEvalEdges.put(idEdge, new ArrayList<Integer>(resEval));
			}
			resultsEvalEdges.get(idEdge).add(resEval);

			// Write results edges
			ExpandableGraphServlet.writeResultsIntoFile(getServletContext().getRealPath("/") + evalEdgesFile,
					resultsEvalEdges);

		}

	}

	private Map<String, List<Integer>> getResultsEvalNodes(String evalNodesFile2) {

		Map<String, List<Integer>> resultsEvalNodes = new HashMap<String, List<Integer>>();
		File resultsEvalNodesFile = new File(getServletContext().getRealPath("/") + evalNodesFile);
		if (resultsEvalNodesFile.exists() && resultsEvalNodesFile.isFile()) {
			resultsEvalNodes = util.EvalMethods.readResults(resultsEvalNodesFile);
		} else {
			System.out.println("no file with results of eval");
			resultsEvalNodes = new HashMap<String, List<Integer>>();
		}
		return resultsEvalNodes;
	}

	private Map<String, List<Integer>> getResultsEvalEdges(String evalEdgesFile2) {

		Map<String, List<Integer>> resultsEvalEdges = new HashMap<String, List<Integer>>();
		File resultsEvalEdgesFile = new File(getServletContext().getRealPath("/") + evalEdgesFile);
		if (resultsEvalEdgesFile.exists() && resultsEvalEdgesFile.isFile()) {
			resultsEvalEdges = util.EvalMethods.readResults(resultsEvalEdgesFile);
		} else {
			resultsEvalEdges = new HashMap<String, List<Integer>>();
		}
		return resultsEvalEdges;
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

	private List<String> readFileLineWise(String filename) {
		InputStream is = SmallestTestServlet.class.getResourceAsStream(filename);
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
