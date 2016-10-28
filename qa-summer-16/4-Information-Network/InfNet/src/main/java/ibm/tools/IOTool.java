package ibm.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Helper class that includes methods commonly used
 * 
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 */
public class IOTool {

	public static JSONObject readJsonObject(String file) {
		InputStream is;
		JSONObject json;
		try {
			is = new FileInputStream(file);
			String jsonTxt = IOUtils.toString(is);
			json = new JSONObject(jsonTxt);
			return json;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static JSONArray readJsonArray(String file) {
		InputStream is;
		JSONArray json;
		try {
			is = new FileInputStream(file);
			String jsonTxt = IOUtils.toString(is);
			json = new JSONArray(jsonTxt);
			return json;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Forces to create a folder
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
