package tests;

import ibm.graph.SimpleEdge;
import ibm.graph.SimpleNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @Deprecated now use the {@link ibm.alchemy.entity.AlchemyEntitiesHandler} -
 *             createTupelForHoleDirAndSave
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon D.
 */
public class AlchemyEntitiesGraph {

	public final static int THRESHOLD_COUNT = 0;
	public final static Integer THRESHOLD_WEIGHT_EDGE = 15;

	/*
	 * Uses entities from Alchemy API, builds a graph out of them and stores
	 * this graph in json files for the front-end visualisation
	 */
	public static void main(String[] args) {

		// File dir = new
		// File("/Users/simondif/Documents/QA_Project/data/alchemyEntitiesWithoutText");
		File dir = new File(
				"M:\\Uni\\semester12\\Question Answering Systems with IBM Watson\\files\\alchemyEntityExtraction");
		Set<Integer> nodeIds = new HashSet<Integer>();
		Map<SimpleEdge, Integer> edgesWithWeigth = new HashMap<SimpleEdge, Integer>();
		Map<Integer, SimpleNode> mapItoS = new HashMap<Integer, SimpleNode>();
		Map<SimpleNode, Integer> mapStoI = new HashMap<SimpleNode, Integer>();
		if (dir.isDirectory()) {
			for (File f : dir.listFiles()) {
				if (!f.getName().endsWith("json"))
					continue;
				System.out.println("new file " + f.getName());
				BufferedReader br;
				try {
					br = new BufferedReader(new FileReader(f));
					String line = br.readLine();
					if (line != null) {
						List<SimpleNode> entities = readEntities(line);
						updateGraph(nodeIds, edgesWithWeigth, mapItoS, mapStoI, entities);
					}
					br.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			edgesWithWeigth = filterOutLowWeightEdges(edgesWithWeigth);
			nodeIds = getConnectedNodes(edgesWithWeigth);
			String nodesFileName = "entityGraph__Nodes.json";
			String edgesFileName = "entityGraph__Edges.json";
			try {
				writeGraphIntoFiles(nodesFileName, edgesFileName, nodeIds, mapItoS, edgesWithWeigth);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(nodeIds.size() + " nodes and " + edgesWithWeigth.size() + " edges.");
		} else {
			System.out.println("Cannot find dir " + dir.getAbsolutePath());
		}

	}

	private static Set<Integer> getConnectedNodes(Map<SimpleEdge, Integer> edgesWithWeigth) {

		Set<Integer> connectedNodes = new HashSet<Integer>();
		for (SimpleEdge edge : edgesWithWeigth.keySet()) {
			connectedNodes.add(edge.getNode1());
			connectedNodes.add(edge.getNode2());
		}
		return connectedNodes;
	}

	private static Map<SimpleEdge, Integer> filterOutLowWeightEdges(Map<SimpleEdge, Integer> edgesWithWeigth) {

		Map<SimpleEdge, Integer> tmp = new HashMap<SimpleEdge, Integer>();
		for (Entry<SimpleEdge, Integer> entry : edgesWithWeigth.entrySet()) {
			if (entry.getValue() > THRESHOLD_WEIGHT_EDGE) {
				tmp.put(entry.getKey(), entry.getValue());
			}
		}
		return tmp;
	}

	private static void writeGraphIntoFiles(String nodesFileName, String edgesFileName, Set<Integer> nodeIds,
			Map<Integer, SimpleNode> mapItoS, Map<SimpleEdge, Integer> edgesWithWeigth) throws IOException {
		// {"id":12504,"label":"GPE","type":"vertex","properties":{"dummy":[{"id":"55n-9nc-4qt","value":"dummy"}],"name":[{"id":"5jv-9nc-35x","value":"Hong
		// Kong.While"}]}}
		StringWriter nodesSW = new StringWriter();
		nodesSW.append("[");
		for (Integer nodeId : nodeIds) {
			SimpleNode node = mapItoS.get(nodeId);
			nodesSW.append("{");
			nodesSW.append("\"id\":" + nodeId + ",\"label\":\"" + node.getType()
					+ "\",\"properties\":{\"name\":[{\"value\":\"" + node.getText() + "\"}]}");
			nodesSW.append("},");
		}
		nodesSW.getBuffer().setLength(nodesSW.getBuffer().length() - 1);
		nodesSW.append("]");

		File foNodes = new File(nodesFileName);
		FileWriter fwNodes = new FileWriter(foNodes);
		fwNodes.write(nodesSW.toString());
		fwNodes.close();

		// {"inV":41016,"inVLabel":"GPE","outVLabel":"LOCATION","id":"cng-9k0-hed-vnc","label":"partOfMany","type":"edge","outV":12384}
		StringWriter edgesSW = new StringWriter();
		edgesSW.append("[");
		for (SimpleEdge edge : edgesWithWeigth.keySet()) {
			edgesSW.append("{");
			edgesSW.append("\"inV\":" + edge.getNode1() + ",\"outV\":" + edge.getNode2());
			edgesSW.append("},");
		}
		edgesSW.getBuffer().setLength(edgesSW.getBuffer().length() - 1);
		edgesSW.append("]");

		File foEdges = new File(edgesFileName);
		FileWriter fwEdges = new FileWriter(foEdges);
		fwEdges.write(edgesSW.toString());
		fwEdges.close();
	}

	private static void updateGraph(Set<Integer> nodeIds, Map<SimpleEdge, Integer> edgesWithWeigth,
			Map<Integer, SimpleNode> mapItoS, Map<SimpleNode, Integer> mapStoI, List<SimpleNode> entities) {
		if (entities.size() < 2)
			return;

		for (SimpleNode entity : entities) {
			if (!mapStoI.containsKey(entity)) {
				mapStoI.put(entity, mapStoI.size());
				mapItoS.put(mapItoS.size(), entity);
			}
			nodeIds.add(mapStoI.get(entity));
		}
		for (SimpleNode entity1 : entities) {
			for (SimpleNode entity2 : entities) {
				if (!entity1.equals(entity2) && mapStoI.get(entity1) < mapStoI.get(entity2)) {
					ibm.tools.MapUtil.incrementMapForKey(edgesWithWeigth,
							new SimpleEdge(mapStoI.get(entity1), mapStoI.get(entity2)));
				}
			}
		}

	}

	private static List<SimpleNode> readEntities(String line) {

		List<SimpleNode> entitiesList = new ArrayList<SimpleNode>();
		// System.out.println(line);
		JSONArray largerArr = new JSONArray(line);
		for (int j = 0; j < largerArr.length(); j++) {
			JSONObject json = largerArr.getJSONObject(j);

			JSONArray entities = json.getJSONArray("entities");
			for (int i = 0; i < entities.length(); i++) {
				JSONObject entity = entities.getJSONObject(i);
				int count = entity.getInt("count");
				if (count > THRESHOLD_COUNT) {
					entitiesList.add(new SimpleNode(entity.getString("text"), entity.getString("type")));
				}
			}
		}

		return entitiesList;
	}

}
