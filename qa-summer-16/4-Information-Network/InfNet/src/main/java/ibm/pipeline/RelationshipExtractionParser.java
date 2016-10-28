package ibm.pipeline;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import ibm.relationship.BluemixRelationshipExtractionClient;
import ibm.tools.IOTool;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph S. and Simon S.
 */
@Deprecated
public class RelationshipExtractionParser extends AbstractParser {

	public RelationshipExtractionParser(String sourceFolder, String destinationFolder) {
		super(sourceFolder, destinationFolder);
	}

	@Override
	public void run() {
		BluemixRelationshipExtractionClient rE = new BluemixRelationshipExtractionClient();

		File dir = new File(sourceFolder);
		File[] directoryListing = dir.listFiles();
		JSONObject json;
		JSONArray jsonArray;
		if (directoryListing != null) {
			for (File child : directoryListing) {
				json = IOTool.readJsonObject(child.toString());
				jsonArray = rE.createRelationsData(rE.analyze(json));
				if (jsonArray != null) {
					// write JsonObjects with the extracted data to
					IOTool.writeToDirectory(destinationFolder);

					FileWriter file;
					try {
						file = new FileWriter(destinationFolder + File.separator
								+ FilenameUtils.normalize(json.getString("title").replace('/', '_')) + ".json");
						file.write(jsonArray.toString());
						file.flush();
						file.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			System.err.println("No Files in Directory: " + sourceFolder);
		}
	}

}
