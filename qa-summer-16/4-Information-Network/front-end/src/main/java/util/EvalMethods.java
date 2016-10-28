package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon D.
 */
public class EvalMethods {

	public static Map<String, List<Integer>> readResults(File resultsEvalFile) {

		Map<String, List<Integer>> resultsMap = new HashMap<String, List<Integer>>();
		if (!resultsEvalFile.exists() || !resultsEvalFile.isFile())
			return resultsMap;
		try {
			String line;
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream(resultsEvalFile), "UTF-8"));

			// BufferedReader br = new BufferedReader(new
			// FileReader(resultsEvalFile));
			while ((line = br.readLine()) != null) {
				String[] lineSplit = line.split("\t");
				if (lineSplit.length > 1) {
					String elementId = lineSplit[0];
					List<Integer> resultsElement = new ArrayList<Integer>();
					for (int i = 1; i < lineSplit.length; i++) {
						String res = lineSplit[i];
						System.out.println(res);
						resultsElement.add(Integer.valueOf(res));
					}
					resultsMap.put(elementId, resultsElement);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resultsMap;
	}

}
