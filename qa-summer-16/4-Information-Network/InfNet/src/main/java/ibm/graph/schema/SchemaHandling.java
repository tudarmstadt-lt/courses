package ibm.graph.schema;

import org.json.JSONArray;

import ibm.graph.BluemixGraphDbClient;
import ibm.graph.IBMGraphDBResponse;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
public class SchemaHandling {

	private BluemixGraphDbClient client;

	public SchemaHandling(BluemixGraphDbClient dbClient) {
		client = dbClient;
	}

	public Schema getSchema() {
		IBMGraphDBResponse response = client.getHttpServices().ibmGET("/" + client.getGraphId() + "/schema", null);
		JSONArray data = response.getData();
		if (data.length() >= 1) {
			return Schema.Instance(data.getJSONObject(0));
		}
		return null;
	}

	public void setSchema(Schema s) {
		// System.out.println(s.toJSON().toString(3));
		client.getHttpServices().ibmPOST("/" + client.getGraphId() + "/schema", s.toJSON());
	}

}
