package ibm.graph.schema;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import ibm.graph.Property;

/**
 * if unique == true. composite must be set to true, too
 * 
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
public abstract class Index {
	private List<Property> indexProperties = new ArrayList<Property>();
	private String type = null;
	private String name = null;
	private boolean composite = false;
	private boolean unique = false;
	private boolean requiresReindex = false;

	public Index(String type, String name) {
		this.type = type;
		this.name = name;
	}

	abstract Object getIndexOnly();

	abstract void setIndexOnly(Object indexOnly);

	abstract JSONObject getIndexOnlyJSON();

	public List<Property> getPropertyKeys() {
		return indexProperties;
	}

	public void setPropertyKeys(List<Property> propertyKeys) {
		this.indexProperties = propertyKeys;
	}

	public void addPropertyKey(Property propertyKey) {
		this.indexProperties.add(propertyKey);
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isComposite() {
		return composite;
	}

	public void setComposite(boolean composite) {
		this.composite = composite;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public boolean isRequiresReindex() {
		return requiresReindex;
	}

	public void setRequiresReindex(boolean requiresReindex) {
		this.requiresReindex = requiresReindex;
	}

	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		// result.put("type", type);->when adding to schema, we don't need this.
		result.put("name", name);

		JSONArray jsonProps = new JSONArray();
		for (Property p : indexProperties) {
			jsonProps.put(p.getName());
		}
		result.put("propertyKeys", jsonProps);
		result.put("unique", unique);
		result.put("composite", composite);
		// result.put("requiresReindex", requiresReindex);--> not yes.. don't
		// think we need this

		JSONObject jsonIndexOnly = getIndexOnlyJSON();
		if (jsonIndexOnly != null) {
			result.put("indexOnly", getIndexOnlyJSON());
		}

		return result;
	}

	public abstract Index Instance(JSONObject json);

	protected Index createInstance(JSONObject json) {
		String type = json.optString("type");// this might cause problems, if
												// the response opject has no
												// type.. test it.
		String name = json.getString("name");
		Index result;
		if (type.equals("vertex")) {
			result = new VertexIndex(name);
		} else if (type.equals("edge")) {
			result = new EdgeIndex(name);
		} else {
			System.err.println("Error parsing index json to instance.wrong type specified as Index.");
			return null;
		}

		JSONArray props = json.getJSONArray("propertyKeys");
		for (int i = 0; i < props.length(); i++) {
			// result.addPropertyKey(Property.Instance(props.getJSONObject(i)));
			// <--- this was an old impl. but we only have strings here
			result.addPropertyKey(new Property(props.getString(i), Property.DATATYPE_STRING, null));// <---
																									// the
																									// property
																									// should
																									// already
																									// exist
																									// somewhere
																									// in
																									// the
																									// schema,
																									// we
																									// just
																									// get
																									// the
																									// name
																									// here
		}

		result.setUnique(json.optBoolean("unique", false));
		result.setComposite(json.optBoolean("composite", false));
		result.setRequiresReindex(json.optBoolean("requiresReindex", false));
		return result;
	}

}
