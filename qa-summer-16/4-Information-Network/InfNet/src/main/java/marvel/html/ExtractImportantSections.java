package marvel.html;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * This class is used to extract the, for us, important sections of a crawled
 * marvel file
 * 
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph S.
 */
public class ExtractImportantSections {
	private List<JSONObject> jsonObjectList;

	public static void main(String[] args) {
		// File file = new File("./marvel_html_files/New York City-4215.html");
		// ExtractImportantSections eIS = new ExtractImportantSections();
		// eIS.extract(HTMLParser.parseHTMLFile(file.toString()),null);
		// System.out.println(eIS.getJSONObjects().toString());

		// infobox parsing test
		File file = new File("./Phil Coulson-2053.html");
		ExtractImportantSections eIS = new ExtractImportantSections();
		// eIS.write(eIS.extractInfoboxes(HTMLParser.parseHTMLFile(file.toString())),"./infobox","Phil
		// Coulson-2053");
		// ((eIS.extractInfoboxInformationForHoleDir("./marvel_html_files_with_correct_infoboxes",
		// "./infoboxRelations");
		eIS.read("./infoboxRelations/Iron Man-2026.json");
	}

	public ExtractImportantSections() {
		jsonObjectList = new ArrayList<JSONObject>();
	}

	public List<JSONObject> getJSONObjects() {
		return jsonObjectList;
	}

	/**
	 * Extract the important Information of a marvel html file, that are used
	 * for the data processing and as input for the ibm services. We extract the
	 * filename, title, link to the wikia article and the text ( all paragraphs
	 * combined)
	 */
	public void extract(Document htmlFile, String filename) {
		JSONObject obj = new JSONObject();
		obj.put("filename", filename);
		obj.put("title", htmlFile.title());
		obj.put("link", htmlFile.select("a[href]").first().text());
		String documentText = "";
		for (Element element : htmlFile.getElementsByTag("p")) {
			// System.err.println(element.text());
			if (!documentText.isEmpty() && Character.isWhitespace(documentText.charAt(documentText.length() - 1)))
				documentText = documentText + element.text();
			else {
				documentText = documentText + " ";
				documentText = documentText + element.text();
			}

		}
		documentText = StringUtils.remove(documentText, "Â");
		documentText = documentText.replaceAll("Ã¼", "ü");
		System.out.println(documentText);
		obj.put("text", documentText);

		if (!(((String) obj.get("text")).isEmpty()))
			jsonObjectList.add(obj);
	}

	/**
	 * Extract all Relations which are included in the information box. These
	 * Information is static, this means that it is fixed and not a result of
	 * services
	 * 
	 */
	public JSONArray extractInfoboxes(Document htmlFile) {
		JSONArray infobox = new JSONArray();
		for (Element element : htmlFile.getElementsByTag("h2")) {
			if (element.text().contains("Infobox")) {
				// System.out.println("Infobox text: " + element.text());
				String nodeOne = (element.text().replace("Infobox ", "").replaceAll(":", "")
						.replaceAll("\\[\\d+\\]", "").replaceAll("\\*", "").replaceAll("©", "é").replaceAll("Ã", "")
						.replaceAll("Ã¼", "ü").replaceAll("¼", "ü"));
				nodeOne = StringUtils.remove(nodeOne, "\u0080");
				// System.err.println("nodeOne: " + nodeOne);
				Element siblingElement = element.siblingElements().get(0);
				for (Element trElement : siblingElement.getElementsByTag("tr")) {
					String edge = (trElement.getElementsByTag("td").get(0).text().replaceAll("\\[\\d+\\]", "")
							.replaceAll("\\*", "").replaceAll("©", "é").replaceAll("Ã", "").replaceAll("Ã¼", "ü"));
					edge = StringUtils.remove(edge, "\u0080");
					String[] nodes;
					if (!trElement.getElementsByTag("td").get(1).text().contains("Date"))
						nodes = trElement.getElementsByTag("td").get(1).text().split(", ");
					else
						nodes = trElement.getElementsByTag("td").get(1).text().split("asdfasfsd");
					for (int i = 0; i < nodes.length; i++) {
						String nodeTwo = (nodes[i].replaceAll("\\[\\d+\\]", "").replaceAll("\\*", "")
								.replaceAll("©", "é").replaceAll("Ã", "").replaceAll("Ã¼", "ü"));
						nodeTwo = StringUtils.remove(nodeTwo, "\u0080");
						System.out.println(nodeOne + "-" + edge + "-" + nodeTwo);
						JSONObject current = new JSONObject();
						current.put("nodeOne", nodeOne);
						current.put("nodeTwo", nodeTwo);
						current.put("edge", edge);
						current.put("type", "infobox");
						infobox.put(current);
					}

				}

			}
		}
		System.out.println(infobox.toString());
		return infobox;
	}

	// z.b. directory = "./marvel_html_files"; extractedInformationJson =
	// "extractedInformationNEW";
	private static void extractInformationForHoleDir(String directory, String extractedInformationJson) {
		// Extract the information from all html pages in the directory

		File dir = new File(directory);
		File[] directoryListing = dir.listFiles();
		ExtractImportantSections eIS = new ExtractImportantSections();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				String filename = child.toString().substring(child.toString().lastIndexOf("/") + 1,
						child.toString().length());
				int lastPeriodPos = filename.lastIndexOf('.');

				// Remove the last period and everything after it
				filename = filename.substring(0, lastPeriodPos);

				System.out.println("nnn " + filename);

				if (!child.toString().contains("DS_Store"))
					eIS.extract(HTMLParser.parseHTMLFile(child.toString()), filename);
			}
		} else {
			System.err.println("No Files in Directory: " + directory);
		}

		// write JsonObjects with the extracted data to
		writeToDirectory("./" + extractedInformationJson);

		for (JSONObject json : eIS.getJSONObjects()) {
			FileWriter file;
			try {
				file = new FileWriter("./" + extractedInformationJson + "/" + json.getString("filename") + ".json");
				file.write(json.toString());
				file.flush();
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Write the Infobox Relation JSONArray to file
	 */
	public void write(JSONArray data, String toDir, String filename) {
		writeToDirectory("./" + toDir);
		FileWriter file;
		try {
			file = new FileWriter("./" + toDir + "/" + filename + ".json");
			file.write(data.toString());
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create Infobox Relations for all files included in the folder
	 */
	private void extractInfoboxInformationForHoleDir(String fromDir, String toDir) {
		File dir = new File(fromDir);
		File[] directoryListing = dir.listFiles();
		JSONArray data;
		for (File file : directoryListing) {
			if (file.toString().contains(".DS_Store"))
				continue;
			data = extractInfoboxes(HTMLParser.parseHTMLFile(file.toString()));
			String filename = file.toString().substring(file.toString().lastIndexOf('/') + 1).replace(".html", "");
			write(data, toDir, filename);

		}

	}

	/**
	 * reads a file and tries to parse it to an JSONArray
	 * 
	 * @param file
	 * @return JSONArray
	 */
	public static JSONArray read(String file) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			JSONArray jsonArray = new JSONArray(line);
			// System.out.println(jsonArray);
			return jsonArray;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * Create a new folder
	 * 
	 * @param newFolder
	 *            the folder Location e.g. ./testfolder
	 */
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
