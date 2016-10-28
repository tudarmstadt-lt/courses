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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

@Deprecated
/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon D.
 */
public class RelationshipExtractionGraph {

	public static void main(String[] args) {

		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("pipelineSubsetData/nodesAndEdges/1960s.json"));
			String jsonString = br.readLine();
			System.out.println(jsonString);
			br.close();
			JSONArray relationships = new JSONArray(jsonString);
			Map<SimpleEdge, Integer> edgesWithWeigth = new HashMap<SimpleEdge, Integer>();
			Map<Integer, SimpleNode> mapItoS = new HashMap<Integer, SimpleNode>();
			Map<SimpleNode, Integer> mapStoI = new HashMap<SimpleNode, Integer>();
			Set<Integer> nodesIds = new HashSet<Integer>();
			Set<Integer> edgesIds = new HashSet<Integer>();
			for (int i = 0; i < relationships.length(); i++) {
				JSONObject relationship = relationships.getJSONObject(i);
				// String score = relationship.getString("score");
				String entityTwo = relationship.getString("entityTwo");
				String entityTwoType = relationship.getString("entityTwoType");
				String entityOne = relationship.getString("entityOne");
				String entityOneType = relationship.getString("entityOneType");
				String title = relationship.getString("title");
				String relation = relationship.getString("relation");
				SimpleNode node1 = new SimpleNode(entityOne, entityOneType);
				SimpleNode node2 = new SimpleNode(entityTwo, entityTwoType);
				int idNode1 = -1;
				if (!mapStoI.keySet().contains(node1)) {
					idNode1 = mapItoS.size();
					nodesIds.add(mapItoS.size());
					mapItoS.put(mapItoS.size(), node1);
					mapStoI.put(node1, idNode1);
				} else {
					idNode1 = mapStoI.get(node1);
				}
				int idNode2 = -1;
				if (!mapStoI.keySet().contains(node2)) {
					idNode2 = mapItoS.size();
					nodesIds.add(mapItoS.size());
					mapItoS.put(mapItoS.size(), node2);
					mapStoI.put(node2, idNode2);
				} else {
					idNode2 = mapStoI.get(node2);
				}
				edgesWithWeigth.put(new SimpleEdge(idNode1, idNode2), 1);
			}

			String nodesFileName = "nodesRelExt.json";
			String edgesFileName = "edgesRelExt.json";
			try {
				writeGraphIntoFiles(nodesFileName, edgesFileName, nodesIds, mapItoS, edgesWithWeigth);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

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
}
