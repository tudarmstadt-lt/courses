package ibm.graph.schema;

import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
public class EdgeIndex extends Index {

	private EdgeLabel indexOnly = null;
	private Schema schema = null;

	public EdgeIndex(String name) {
		super("edge", name);
	}

	void setSchema(Schema s) {
		this.schema = s;
	}

	@Override
	public EdgeLabel getIndexOnly() {
		return indexOnly;
	}

	@Override
	public void setIndexOnly(Object indexOnly) {
		this.indexOnly = (EdgeLabel) indexOnly;
	}

	@Override
	JSONObject getIndexOnlyJSON() {
		if (indexOnly == null) {
			return null;
		}
		JSONObject result = new JSONObject();
		List<String> edgeLabels = schema.getEdgeLabels().stream().map(l -> l.getLabel()).collect(Collectors.toList());
		if (!edgeLabels.contains(indexOnly.getLabel())) {
			result.put("multiplicity", indexOnly.getMultiplicity());
		}
		result.put("name", indexOnly.getLabel());
		return result;
	}

	@Override
	public EdgeIndex Instance(JSONObject json) {
		EdgeIndex result = (EdgeIndex) super.createInstance(json);

		if (json.has("indexOnly")) {
			JSONObject indexOnlyJsonObj = json.getJSONObject("indexOnly");
			this.indexOnly = EdgeLabel.Instance(indexOnlyJsonObj);
		}

		return result;
	}

}