package ibm.alchemy.relationshipextraction;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph S.
 */
public class InfoBoxRelationsHandler {

	/**
	 * The given files contains one json array. each jsonObj in the array
	 * contains one relation.
	 * 
	 * @param fileRelations
	 * @return
	 */
	@SuppressWarnings("unused")
	public List<Relation> createRelationsData(JSONArray fileRelations, String title, String url) {
		List<Relation> result = new ArrayList<>();
		for (int i = 0; i < fileRelations.length(); i++) {
			String jsonStr = fileRelations.getJSONObject(i).toString(3);

			String nodeOne = fileRelations.getJSONObject(i).getString("nodeOne");
			String nodeTwo = fileRelations.getJSONObject(i).getString("nodeTwo");
			String edgeLabel = fileRelations.getJSONObject(i).getString("edge");
			String edgeType = fileRelations.getJSONObject(i).getString("type");// should
																				// be
																				// "infobox"
			result.add(new Relation(nodeOne, "INFOBOX_NONE", nodeTwo, "INFOBOX_NONE", edgeLabel, "0.0", title, url, 1,
					1, 0.0f, 0.0f, "INFOBOX_NO_SENTENCE", 2, 0));
		}

		return result;
	}
}
