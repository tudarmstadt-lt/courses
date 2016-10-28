package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import beans.Edge;
import beans.Node;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon D.
 */
public class GraphDBToBeanParser {

	private static final int MAX_LIST_ITEMS = 20;

	public static List<Node> parseNodes(List<JSONObject> nodes) {
		List<Node> result = new ArrayList<Node>();
		for (JSONObject nodeJson : nodes) {
			result.add(parseNode(nodeJson));
		}
		return result;
	}

	public static Node parseNode(JSONObject nodeJson) {
		int id = nodeJson.getInt("id");
		String type = nodeJson.get("label").toString();
		String value = null;
		JSONObject properties = nodeJson.getJSONObject("properties");
		JSONArray name = properties.getJSONArray("name");
		JSONObject obj = name.getJSONObject(0);
		value = obj.get("value").toString();
		if (properties.has("otherVertexLabel")) {
			JSONArray otherVArr = properties.getJSONArray("otherVertexLabel");
			type = otherVArr.getJSONObject(0).get("value").toString();
		}
		int count = 0;
		double relevance = 0.0;
		int group = nodeJson.getInt("relationsMode");
		if (properties.has("relevance")) {
			JSONArray otherVArr = properties.getJSONArray("relevance");
			relevance = otherVArr.getJSONObject(0).getDouble("value");
		}
		if (properties.has("count")) {
			JSONArray otherVArr = properties.getJSONArray("count");
			count = otherVArr.getJSONObject(0).getInt("value");
		}
		if (properties.has("count")) {
			JSONArray otherVArr = properties.getJSONArray("count");
			count = otherVArr.getJSONObject(0).getInt("value");
		}

		List<String> urls = parseListFromNodeJSON(properties, "sourceURL");
		List<String> sentences = parseListFromNodeJSON(properties, "sentences");
		List<String> titles = parseListFromNodeJSON(properties, "sourceTitle");

		Node node = new Node(id, value, type, group, sentences, urls, titles, count, relevance);
		return node;
	}

	private static List<String> parseListFromNodeJSON(JSONObject properties, String key) {
		List<String> result = new ArrayList<>();
		if (properties.has(key)) {
			JSONArray otherVArr = properties.getJSONArray(key);
			String str = otherVArr.getJSONObject(0).getString("value");
			str = str.replaceAll("\"", " ");
			result = splitStr(str);
		}
		return result.size() > MAX_LIST_ITEMS ? result.subList(0, MAX_LIST_ITEMS - 1) : result;
	}

	private static List<String> parseListFromEdgesJSON(JSONObject properties, String key) {
		List<String> result = new ArrayList<>();
		if (properties.has(key)) {
			String str = properties.getString(key);
			str = str.replaceAll("\"", " ");
			result = splitStr(str);
		}
		return result;
	}

	private static List<String> splitStr(String str) {
		List<String> result = new ArrayList<>();
		if (str.length() == 0) {
			return result;
		}
		List<String> elements = Arrays.asList(str.split(";"));
		String s2 = "__SPLIT_CHARACTER__";
		for (String item : elements) {
			List<String> nestedElements = Arrays.asList(item.split(s2));
			for (String element : nestedElements) {
				if (!result.contains(element)) {
					result.add(element);
				}
			}
		}
		return result;
	}

	public static List<Edge> parseEdges(List<JSONObject> edges) {
		List<Edge> result = new ArrayList<Edge>();
		for (JSONObject json : edges) {
			result.add(parseEdge(json));
		}
		return result;
	}

	public static Edge parseEdge(JSONObject json) {
		int node1 = json.getInt("inV");
		int node2 = json.getInt("outV");
		String title = json.getString("label");
		JSONObject properties = json.getJSONObject("properties");

		int relationsMode = 0;
		double relationshipScore = 0.0;
		if (properties.has("otherEdgeLabel")) {
			title = properties.get("otherEdgeLabel").toString();
		}
		if (properties.has("relationShipScore")) {
			relationshipScore = properties.getDouble("relationShipScore");
		}
		if (properties.has("relationsMode")) {
			relationsMode = properties.getInt("relationsMode");
		}

		List<String> urls = parseListFromEdgesJSON(properties, "link");
		List<String> sentences = parseListFromEdgesJSON(properties, "sentence");
		List<String> pageTitles = parseListFromEdgesJSON(properties, "title");
		Edge edge = new Edge(json.getString("id"), node1, node2, 0., title, sentences, urls, pageTitles,
				relationshipScore, relationsMode);
		return edge;
	}
}
