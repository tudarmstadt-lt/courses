package ibm.graph.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import ibm.graph.Property;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
public class Schema {

	private List<Property> properties = new ArrayList<>();
	private List<String> vertexLabels = new ArrayList<>();
	private List<EdgeLabel> edgeLabels = new ArrayList<EdgeLabel>();
	private List<VertexIndex> vertexIndicies = new ArrayList<VertexIndex>();
	private List<EdgeIndex> edgeIndicies = new ArrayList<EdgeIndex>();

	public static Schema Instance(JSONObject response) {
		Schema s = new Schema();

		JSONArray jsonEdgeLabels = response.getJSONArray("edgeLabels");
		for (int i = 0; i < jsonEdgeLabels.length(); i++) {
			s.addEdgeLabel(EdgeLabel.Instance(jsonEdgeLabels.getJSONObject(i)));
		}

		JSONArray jsonVertexLabels = response.getJSONArray("vertexLabels");
		for (int i = 0; i < jsonVertexLabels.length(); i++) {
			s.addVertexLabel(jsonVertexLabels.getJSONObject(i).getString("name"));
		}

		JSONArray jsonEdgeIndexes = response.getJSONArray("edgeIndexes");
		EdgeIndex e = new EdgeIndex("");
		for (int i = 0; i < jsonEdgeIndexes.length(); i++) {
			s.addEdgeIndex(e.Instance(jsonEdgeIndexes.getJSONObject(i)));
		}

		JSONArray jsonVertexIndexes = response.getJSONArray("vertexIndexes");
		VertexIndex v = new VertexIndex("");
		for (int i = 0; i < jsonVertexIndexes.length(); i++) {
			s.addVertexIndex(v.Instance(jsonVertexIndexes.getJSONObject(i)));
		}

		JSONArray jsonPropertyKeys = response.getJSONArray("propertyKeys");
		for (int i = 0; i < jsonPropertyKeys.length(); i++) {
			s.addPropertyKey(Property.Instance(jsonPropertyKeys.getJSONObject(i)));
		}

		return s;
	}

	public List<Property> getPropertyKeys() {
		return properties;
	}

	public void setPropertyKeys(List<Property> propertyKeys) {
		this.properties = new ArrayList<Property>(propertyKeys);
	}

	public void addPropertyKey(Property propertyKey) {
		properties.add(propertyKey);
	}

	public List<String> getVertexLabels() {
		return vertexLabels;
	}

	public void setVertexLabels(List<String> vertexLabels) {
		this.vertexLabels = vertexLabels;
	}

	public void addVertexLabel(String vertexLabel) {
		this.vertexLabels.add(vertexLabel);
	}

	public List<EdgeLabel> getEdgeLabels() {
		return edgeLabels;
	}

	public void setEdgeLabels(List<EdgeLabel> labels) {
		this.edgeLabels = labels;
	}

	public void addEdgeLabel(EdgeLabel edgeLabel) {
		this.edgeLabels.add(edgeLabel);
	}

	public List<VertexIndex> getVertexIndicies() {
		return vertexIndicies;
	}

	public void setVertexIndicies(List<VertexIndex> vertexIndicies) {
		this.vertexIndicies = vertexIndicies;
	}

	public void addVertexIndex(VertexIndex vertexIndex) {
		this.vertexIndicies.add(vertexIndex);
	}

	public List<EdgeIndex> getEdgeIndicies() {
		return edgeIndicies;
	}

	public void setEdgeIndicies(List<EdgeIndex> edgeIndicies) {
		this.edgeIndicies = edgeIndicies;
	}

	public void addEdgeIndex(EdgeIndex edgeIndex) {
		this.edgeIndicies.add(edgeIndex);
	}

	public EdgeIndex createEdgeIndex(String name) {
		EdgeIndex result = new EdgeIndex(name);
		result.setSchema(this);
		return result;
	}

	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		JSONArray jsonProps = new JSONArray();
		JSONArray jsonVertexIndicies = new JSONArray();
		JSONArray jsonEdgeIndicies = new JSONArray();
		JSONArray jsonVertexLabels = new JSONArray();
		JSONArray jsonEdgeLabels = new JSONArray();

		properties.forEach(p -> jsonProps.put(p.toJSON()));
		vertexLabels.forEach(vLabel -> {
			JSONObject labelObj = new JSONObject();
			labelObj.put("name", vLabel);
			jsonVertexLabels.put(labelObj);
		});
		edgeLabels.forEach(eLabel -> jsonEdgeLabels.put(eLabel.toJSON()));
		vertexIndicies.forEach(vIndex -> jsonVertexIndicies.put(vIndex.toJSON()));
		edgeIndicies.forEach(eIndex -> jsonEdgeIndicies.put(eIndex.toJSON()));

		result.put("propertyKeys", jsonProps);
		result.put("vertexLabels", jsonVertexLabels);
		result.put("edgeLabels", jsonEdgeLabels);
		result.put("vertexIndexes", jsonVertexIndicies);
		result.put("edgeIndexes", jsonEdgeIndicies);

		return result;
	}

	/**
	 * relationsMode == 1 relationships filtered by entities relationsMode == 2
	 * infoboxRelationships 4 soa relations 8 entity graph will be added if one
	 * edge is contained by multiple graphs
	 * 
	 * @return The schema used for our graphDB
	 */
	public static Schema getSchema() {
		Schema s = new Schema();
		s.addVertexLabel("Person");
		s.addVertexLabel("Facility");
		s.addVertexLabel("Organization");
		s.addVertexLabel("City");
		s.addVertexLabel("Location");
		s.addVertexLabel("GPE");
		s.addVertexLabel("otherV");

		// Edge labels
		s.addEdgeLabel(new EdgeLabel("employedBy", "MULTI"));
		s.addEdgeLabel(new EdgeLabel("affectedBy", "MULTI"));
		s.addEdgeLabel(new EdgeLabel("partOfMany", "MULTI"));
		s.addEdgeLabel(new EdgeLabel("locatedAt", "MULTI"));
		s.addEdgeLabel(new EdgeLabel("agentOf", "MULTI"));
		s.addEdgeLabel(new EdgeLabel("otherE", "MULTI"));

		// vertexProperties
		Property p1 = new Property("relevance", Property.DATATYPE_FLOAT, null);
		Property p2 = new Property("sourceURL", Property.DATATYPE_STRING, null);
		Property p3 = new Property("sourceTitle", Property.DATATYPE_STRING, null);
		Property p4 = new Property("name", Property.DATATYPE_STRING, null);
		Property p5 = new Property("count", Property.DATATYPE_INTEGER, null);
		Property p6 = new Property("dummy", Property.DATATYPE_STRING, null);
		Property p7 = new Property("otherVertexLabel", Property.DATATYPE_STRING, null);
		Property p8 = new Property("sentences", Property.DATATYPE_STRING, null);
		// EdgeProperties
		Property pe1 = new Property("relationShipScore", Property.DATATYPE_FLOAT, null);
		Property pe2 = new Property("title", Property.DATATYPE_STRING, null);
		Property pe3 = new Property("link", Property.DATATYPE_STRING, null);
		Property pe4 = new Property("otherEdgeLabel", Property.DATATYPE_STRING, null);
		Property pe5 = new Property("dummyE", Property.DATATYPE_STRING, null);
		Property pe6 = new Property("sentence", Property.DATATYPE_STRING, null);
		Property pe7 = new Property("relationsMode", Property.DATATYPE_INTEGER, null);
		Property pe8 = new Property("alchemyEdgeCount", Property.DATATYPE_INTEGER, null);

		s.setPropertyKeys(Arrays.asList(p1, p2, p3, p4, p5, p6, p7, p8));
		s.addPropertyKey(pe1);
		s.addPropertyKey(pe2);
		s.addPropertyKey(pe3);
		s.addPropertyKey(pe4);
		s.addPropertyKey(pe5);
		s.addPropertyKey(pe6);
		s.addPropertyKey(pe7);
		s.addPropertyKey(pe8);

		// vertex indicies
		VertexIndex v1 = new VertexIndex("vByRelevance");
		VertexIndex v2 = new VertexIndex("vBysourceURL");
		VertexIndex v3 = new VertexIndex("vBysourceTitle");
		VertexIndex v4 = new VertexIndex("vByName");
		VertexIndex v5 = new VertexIndex("vByCount");
		VertexIndex v6 = new VertexIndex("vByDummy");
		VertexIndex v7 = new VertexIndex("vByOtherVLabel");
		VertexIndex v8 = new VertexIndex("vBySentence");
		v1.addPropertyKey(new Property("relevance", -1, null));
		v1.setComposite(false);
		v2.addPropertyKey(new Property("sourceURL", -1, null));
		v3.addPropertyKey(new Property("sourceTitle", -1, null));
		v4.addPropertyKey(new Property("name", -1, null));
		v4.setComposite(false);
		v5.addPropertyKey(new Property("count", -1, null));
		v5.setComposite(false);
		v6.addPropertyKey(new Property("dummy", -1, null));
		v6.setComposite(true);
		v7.addPropertyKey(new Property("otherVertexLabel", -1, null));
		v7.setComposite(true);
		v8.addPropertyKey(new Property("sentences", -1, null));
		v8.setComposite(true);
		s.setVertexIndicies(Arrays.asList(v1, v2, v3, v4, v5, v6, v7, v8));

		// edge indicies
		EdgeIndex e1 = new EdgeIndex("eByrelScore");
		EdgeIndex e2 = new EdgeIndex("eByTitle");
		EdgeIndex e3 = new EdgeIndex("eByLink");
		EdgeIndex e4 = new EdgeIndex("eOtherEdgeLabel");
		EdgeIndex e5 = new EdgeIndex("eByDummy");
		EdgeIndex e6 = new EdgeIndex("eBySentence");
		EdgeIndex e7 = new EdgeIndex("eByRelationsMode");
		EdgeIndex e8 = new EdgeIndex("eByAlchemyEdgeCount");
		e1.addPropertyKey(new Property("relationShipScore", -1, null));
		e1.setComposite(false);
		e2.addPropertyKey(new Property("title", -1, null));
		e2.setComposite(false);
		e3.addPropertyKey(new Property("link", -1, null));
		e3.setComposite(true);
		e4.addPropertyKey(new Property("otherEdgeLabel", -1, null));
		e4.setComposite(true);
		e5.addPropertyKey(new Property("dummyE", -1, null));
		e5.setComposite(true);
		e6.addPropertyKey(new Property("sentence", -1, null));
		e6.setComposite(true);
		e7.addPropertyKey(new Property("relationsMode", -1, null));
		e7.setComposite(true);
		e8.addPropertyKey(new Property("alchemyEdgeCount", -1, null));
		e8.setComposite(false);

		s.setEdgeIndicies(Arrays.asList(e1, e2, e3, e4, e5, e6, e7, e8));
		return s;
	}

}
