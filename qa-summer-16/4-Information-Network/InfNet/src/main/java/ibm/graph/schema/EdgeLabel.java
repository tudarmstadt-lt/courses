package ibm.graph.schema;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S. edge: label, multiplicity MULTI, SIMPLE, MANY2ONE, ONE2MANY,
 *         ONE2ONE
 * 
 */
public class EdgeLabel {

	private String label;
	private String multiplicity;

	private List<String> allowedMultis = Arrays.asList("MULTI", "SIMPLE", "MANY2ONE", "ONE2MANY", "ONE2ONE");

	/**
	 * 
	 * @param label
	 * @param multiplicity
	 *            must be one of MULTI, SIMPLE, MANY2ONE, ONE2MANY, ONE2ONE
	 */
	public EdgeLabel(String label, String multiplicity) {

		this.label = label;
		if (!allowedMultis.contains(multiplicity)) {
			// System.err.println("multiplicity not supported.");
			return;
		}

		this.multiplicity = multiplicity;
	}

	public static EdgeLabel Instance(JSONObject obj) {
		EdgeLabel result = new EdgeLabel(obj.getString("name"), null);
		if (obj.has("multiplicity")) {
			result.setMultiplicity(obj.getString("multiplicity"));
		}

		return result;
	}

	public String getLabel() {
		return label;
	}

	/**
	 * 
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	public String getMultiplicity() {
		return multiplicity;
	}

	/**
	 * 
	 * @param multiplicity
	 *            must be one of MULTI, SIMPLE, MANY2ONE, ONE2MANY, ONE2ONE
	 */
	public void setMultiplicity(String multiplicity) {
		this.multiplicity = multiplicity;
	}

	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		result.put("name", label);
		result.put("multiplicity", multiplicity);
		return result;
	}
}
