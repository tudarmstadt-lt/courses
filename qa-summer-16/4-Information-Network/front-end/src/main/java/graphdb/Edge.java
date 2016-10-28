/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphdb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONObject;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph S. and Simon S.
 */
public class Edge {

	public static Edge parseJSONObject(JSONObject jsonEdge) {
		Edge e = null;
		int fromId = jsonEdge.getInt("outV");
		int toId = jsonEdge.getInt("inV");
		String id = jsonEdge.getString("id");
		String label = jsonEdge.getString("label");
		List<Property> properties = new ArrayList<Property>();

		JSONObject jsonProps = jsonEdge.getJSONObject("properties");
		Iterator<String> it = jsonProps.keys();
		while (it.hasNext()) {
			String name = it.next();
			Object value = jsonProps.get(name);

			Property p = new Property(name, Property.getDataType(value), value);
			properties.add(p);
		}
		e = new Edge(fromId, toId, label, properties);
		e.setId(id);
		return e;
	}

	private int fromVertexId = -1;
	private int toVertexId = -1;
	private String label = null;

	private String id = null;
	private List<Property> properties = null;

	public Edge(int fromId, int toId, String label) {
		this(fromId, toId, label, new ArrayList<Property>());
	}

	public Edge(int fromId, int toId, String label, List<Property> props) {
		this.fromVertexId = fromId;
		this.toVertexId = toId;
		this.label = label;
		properties = props;
	}

	public void addProperty(Property prop) {
		this.properties.add(prop);
	}

	public JSONObject toJSONObject() {
		JSONObject json = new JSONObject();
		JSONObject jsonProps = new JSONObject();
		if (properties != null && properties.size() > 0) {
			for (Property p : properties) {
				jsonProps.put(p.getName(), p.getValue());
			}
			json.put("properties", jsonProps);
		}
		json.put("outV", fromVertexId);
		json.put("inV", toVertexId);
		json.put("label", this.label);
		return json;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getFromVertexId() {
		return fromVertexId;
	}

	public int getToVertexId() {
		return toVertexId;
	}

	public String getLabel() {
		return label;
	}

	public List<Property> getProperties() {
		return properties;
	}

	public Property getProperty(String name) {
		for (Property p : properties) {
			if (p.getName().equals(name)) {
				return p;
			}
		}

		return null;
	}

	public boolean hasProperty(String name) {
		for (Property p : properties) {
			if (p.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

}
