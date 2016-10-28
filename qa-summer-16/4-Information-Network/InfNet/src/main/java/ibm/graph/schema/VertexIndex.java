package ibm.graph.schema;

import org.json.JSONObject;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
public class VertexIndex extends Index {

	private String indexOnly = null;

	public VertexIndex(String name) {
		super("vertex", name);
	}

	@Override
	public String getIndexOnly() {
		return indexOnly;
	}

	@Override
	public void setIndexOnly(Object indexOnly) {
		this.indexOnly = (String) indexOnly;
	}

	@Override
	JSONObject getIndexOnlyJSON() {
		if (indexOnly == null) {
			return null;
		}
		JSONObject result = new JSONObject();
		result.put("name", indexOnly);
		return result;
	}

	@Override
	public VertexIndex Instance(JSONObject json) {
		VertexIndex result = (VertexIndex) super.createInstance(json);

		if (json.has("indexOnly")) {
			JSONObject indexOnlyJsonObj = json.getJSONObject("indexOnly");
			this.indexOnly = indexOnlyJsonObj.getString("name");
		}

		return result;
	}

}
