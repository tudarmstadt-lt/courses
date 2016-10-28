package ibm.relationship;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonObject;

import ibm.connection.BluemixHttpServices;
import ibm.connection.CredentialParser;
import ibm.tools.IOTool;
import marvel.html.ExtractImportantSections;
import marvel.html.HTMLParser;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph S.
 */
public class BluemixRelationshipExtractionClient {

	private String credentialsPath = "nbproject/private/relationShipCredentials";
	private BluemixHttpServices httpservices;
	private JSONObject json;

	public BluemixRelationshipExtractionClient() {
		initialize();
	}

	private void initialize() {
		HttpClients.createDefault();
		CredentialParser parser = new CredentialParser(credentialsPath);
		httpservices = new BluemixHttpServices(parser.getApiURL(), parser.getUser(), parser.getPass());

	}

	private static void relationshipEx(String extractedInformationJsonDir, String saveRelationDir) {
		// hier wurde der Relation Ship Extraction service auf den
		// MarvelHTMLFilesParsed.json getestet
		BluemixRelationshipExtractionClient rE = new BluemixRelationshipExtractionClient();

		File dir = new File("./" + extractedInformationJsonDir);
		File[] directoryListing = dir.listFiles();
		ExtractImportantSections eIS = new ExtractImportantSections();
		List<JSONObject> tupelList = new ArrayList<JSONObject>();
		JSONObject json;
		JSONArray jsonArray;
		if (directoryListing != null) {
			for (File child : directoryListing) {
				json = IOTool.readJsonObject(child.toString());
				jsonArray = rE.createRelationsData(rE.analyze(json));
				if (jsonArray != null) {
					// write JsonObjects with the extracted data to
					writeToDirectory("./" + saveRelationDir);

					FileWriter file;
					try {
						file = new FileWriter("./" + saveRelationDir + "/"
								+ FilenameUtils.normalize(json.getString("title")) + ".json");
						file.write(jsonArray.toString());
						file.flush();
						file.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			System.err.println("No Files in Directory: " + extractedInformationJsonDir);
		}

	}

	public JSONObject analyze(JSONObject json) {
		this.json = json;
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("txt", json.getString("text")));
		qparams.add(new BasicNameValuePair("sid", "ie-en-news"));
		qparams.add(new BasicNameValuePair("rt", "json"));
		return httpservices.httpPOSTWithParameters("/v1/sire/0", qparams);
	}

	public JSONArray createRelationsData(JSONObject jsonObject) {
		JSONArray tuples = new JSONArray();
		String entityOne;
		String entityTwo;
		String relation;
		Map<String, String> keyEntityTypMapping = new HashMap<String, String>();
		try {

			// entities
			if (jsonObject.getJSONObject("doc") != null
					&& jsonObject.getJSONObject("doc").getJSONObject("entities") != null
					&& jsonObject.getJSONObject("doc").getJSONObject("entities").getJSONArray("entity") != null) {
				JSONArray entities = jsonObject.getJSONObject("doc").getJSONObject("entities").getJSONArray("entity");
				for (int i = 0; i < entities.length(); i++) {
					String eid = ((JSONObject) entities.get(i)).getString("eid");
					String type = ((JSONObject) entities.get(i)).getString("type");
					keyEntityTypMapping.put(eid, type);
				}
			}

			// relations
			if (jsonObject.getJSONObject("doc") != null
					&& jsonObject.getJSONObject("doc").getJSONObject("relations") != null
					&& jsonObject.getJSONObject("doc").getJSONObject("relations").getJSONArray("relation") != null) {
				JSONArray bez = jsonObject.getJSONObject("doc").getJSONObject("relations").getJSONArray("relation");
				String entityOneType = ((JSONObject) ((JSONObject) bez.get(0)).getJSONArray("rel_entity_arg").get(0))
						.getString("eid");
				String entityTwoType = ((JSONObject) ((JSONObject) bez.get(0)).getJSONArray("rel_entity_arg").get(1))
						.getString("eid");

				for (int i = 0; i < bez.length(); i++) {
					relation = (String) ((JSONObject) bez.get(i)).get("type");

					JSONArray relmention = ((JSONObject) bez.get(i)).getJSONObject("relmentions")
							.getJSONArray("relmention");
					JSONObject score = ((JSONObject) relmention.get(0));
					JSONArray rel_metion_arg = ((JSONObject) relmention.get(0)).getJSONArray("rel_mention_arg");
					entityOne = (String) ((JSONObject) rel_metion_arg.get(0)).get("text");
					entityTwo = (String) ((JSONObject) rel_metion_arg.get(1)).get("text");
					tuples.put(new JSONObject().put("entityOne", entityOne).put("entityTwo", entityTwo)
							.put("relation", relation).put("score", score.getDouble("score"))
							.put("title", json.getString("title")).put("link", json.getString("link"))
							.put("entityOneType", keyEntityTypMapping.get(entityOneType))
							.put("entityTwoType", keyEntityTypMapping.get(entityTwoType)));
				}

				return tuples;
			}
		} catch (Exception e) {
			System.err.println("catched Exception" + e);
		}

		return null;
	}

	public static boolean writeToDirectory(String newFolder) {
		File file = new File(newFolder);
		boolean returnValue = false;
		try {
			FileUtils.forceMkdir(file);
			returnValue = true;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return returnValue;

	}

}
