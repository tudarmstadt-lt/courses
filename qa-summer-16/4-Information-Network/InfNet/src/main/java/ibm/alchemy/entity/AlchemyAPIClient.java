package ibm.alchemy.entity;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyLanguage;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Entities;

import ibm.connection.CredentialParser;

/**
 * Class to call the Alchemy Language Entity Extraction service Restriction:
 * 1,000 API Events per Day
 * 
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph S.
 */
public class AlchemyAPIClient {
	private String credentialsPath = "./alchemyAPICredentials.json";
	private AlchemyLanguage service;

	public static void main(String[] args) {

		AlchemyAPIClient client = new AlchemyAPIClient();
		String a = null;
		// client.analyze("http://marvelcinematicuniverse.wikia.com/wiki/Natasha_Romanoff");
		client.analyzeText(a);
	}

	public AlchemyAPIClient() {
		initialize();
	}

	private void initialize() {
		CredentialParser parser = new CredentialParser(credentialsPath);
		service = new AlchemyLanguage();
		service.setApiKey(parser.getPass());
	}

	/**
	 * Extract the Entities of the text of a web page.
	 * 
	 * @param url
	 *            from the side to be analyzed e.g.
	 *            "http://marvelcinematicuniverse.wikia.com/wiki/
	 *            Natasha_Romanoff
	 * @return {@link Entities}
	 */
	public Entities analyze(String url) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(AlchemyLanguage.URL, url);
		Entities entities = service.getEntities(params);
		System.out.println("Entities: " + entities);
		return entities;
	}

	/**
	 * Extract the Entities of a text. If the text is longer than 50kb, it is
	 * splitted with equal frequency-
	 * 
	 * @param text
	 *            to be analyzed
	 * @return {@link Entities} Array
	 */
	public Entities[] analyzeText(String text) {
		int lengthOfTheText;
		final int maximumTextSize = 50000;
		JSONArray result = new JSONArray();
		try {
			byte[] textAsByte = text.getBytes("UTF-8");
			lengthOfTheText = textAsByte.length;
			// System.out.println(lengthOfTheText);
			// if Size of Text is greater than 50kb, we must split it --> equal
			// frequency ansatz
			Entities[] entitiesArray = null;
			if (lengthOfTheText > maximumTextSize) {
				long muchTimes = Math.round(lengthOfTheText / (double) maximumTextSize);
				entitiesArray = new Entities[(int) muchTimes];
				double splitlength = lengthOfTheText / muchTimes;

				String[] split_string = new String[(int) muchTimes];
				for (int k = 0; k < muchTimes; k++) {
					String rString = "";
					for (int i = (int) (k * splitlength); i < splitlength * (k + 1); i++) {
						rString += (char) textAsByte[i];
					}
					split_string[k] = rString;
				}

				for (int i = 0; i < muchTimes; i++) {
					Map<String, Object> params = new HashMap<String, Object>();
					params.put(AlchemyLanguage.TEXT, split_string[i]);
					Entities entities = service.getEntities(params);
					entitiesArray[i] = entities;
				}
				return entitiesArray;

			} else {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put(AlchemyLanguage.TEXT, text);
				Entities entities = service.getEntities(params);
				return new Entities[] { entities };
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;

	}

}
