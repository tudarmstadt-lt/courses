package marvel.html;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Parsed an html file
 * 
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph S.
 */
public class HTMLParser {

	public static void main(String args[]) {
		HTMLParser.parseHTMLFile("./marvel_html_files/1900s-23252.html");
	}

	public static Document parseHTMLFile(String filename) {
		System.out.println("Now parsing: " + filename);
		Document htmlFile = null;
		try {
			htmlFile = Jsoup.parse(new File(filename), "ISO-8859-1");
			System.out.println("html_file: " + filename + " is successfully parsed!");
			return htmlFile;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

}
