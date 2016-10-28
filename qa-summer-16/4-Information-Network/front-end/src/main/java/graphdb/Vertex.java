/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph S. and Simon S.
 */
public class Vertex {

	public static Vertex parseJSONObject(JSONObject jsonVertex) {
		String label = jsonVertex.getString("label");
		Integer id = jsonVertex.getInt("id");
		List<Property> properties = new ArrayList<Property>();

		JSONObject propsObj = jsonVertex.getJSONObject("properties");
		Iterator<String> it = propsObj.keys();
		while (it.hasNext()) {
			String name = it.next();
			JSONObject iterProp = propsObj.getJSONArray(name).getJSONObject(0);

			Object value = iterProp.get("value");

			Property p = new Property(name, Property.getDataType(value), value);
			properties.add(p);
		}

		Vertex v = new Vertex(label, properties);
		v.setId(id);
		return v;
	}

	/**
	 * When creating a vertex, the label must be defined in the schema under
	 * "VertexLabels" *
	 */
	private String label = null;

	/**
	 * The property must be defined in the schema.
	 */
	// private List<Property> properties = null;

	/**
	 * The property must be defined in the schema. The key is the name, which is
	 * also contained in the property.
	 */
	private HashMap<String, Property> properties;

	private int id = -1;

	public Vertex(String label, List<Property> properties) {
		this.label = label;
		this.properties = new HashMap<>();
		setProperties(properties);
	}

	public Vertex(String label) {
		this.label = label;
		this.properties = new HashMap<String, Property>();
	}

	public void addProperty(Property prop) {
		this.properties.put(prop.getName(), prop);
	}

	public void setProperties(List<Property> properties) {
		this.properties = new HashMap<>();
		for (Property p : properties) {
			this.properties.put(p.getName(), p);
		}
	}

	public String getName() {
		if (hasProperty("name")) {
			return (String) getProperty("name").getValue();
		} else {
			return null;
		}
	}

	public Property getProperty(String name) {
		return properties.get(name);
	}

	public boolean hasProperty(String name) {
		return properties.containsKey(name);
	}

	public HashMap<String, Property> getPropertyMap() {
		HashMap<String, Property> result = new HashMap<>();
		for (Property p : getProperties()) {
			result.put(p.getName(), p);
		}
		return result;
	}

	public String getLabel() {
		return label;
	}

	public List<Property> getProperties() {
		return properties.values().stream().collect(Collectors.toList());
	}

	public JSONObject toJSONObject() {
		JSONObject json = new JSONObject();
		JSONObject jsonProps = new JSONObject();
		for (String pName : properties.keySet()) {
			jsonProps.put(pName, properties.get(pName).getValue().toString());// TODO::::
																				// this
																				// might
																				// cause
																				// errors
																				// when
																				// not
																				// using
																				// strings?????
		}
		json.put("properties", jsonProps);
		json.put("label", this.label);
		return json;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
