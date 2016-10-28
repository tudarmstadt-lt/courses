package ibm.pipeline;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;

import ibm.pipeline.Pipeline.TransitionType;
import ibm.tools.IOTool;
import marvel.html.ExtractImportantSections;
import marvel.html.HTMLParser;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
@Deprecated
public class Extractor extends AbstractTransition {

	public Extractor(String sourceFolder, String destinationFolder) {
		super(sourceFolder, destinationFolder);
	}

	@Override
	public void run() {
		// Extract the information from all html pages in the directory

		File dir = new File(this.sourceFolder);
		File[] directoryListing = dir.listFiles();
		ExtractImportantSections eIS = new ExtractImportantSections();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				eIS.extract(HTMLParser.parseHTMLFile(child.toString()), null);
			}
		} else {
			System.err.println("No Files in Directory: " + sourceFolder);
		}

		// write JsonObjects with the extracted data to
		IOTool.writeToDirectory(destinationFolder);

		for (JSONObject json : eIS.getJSONObjects()) {
			FileWriter file;
			try {
				file = new FileWriter(destinationFolder + "/"
						+ FilenameUtils.normalize(json.getString("title").replace("/", "_")) + ".json");
				file.write(json.toString());
				file.flush();
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public TransitionType getTransitionType() {
		return TransitionType.EXTRACTOR;
	}

}
