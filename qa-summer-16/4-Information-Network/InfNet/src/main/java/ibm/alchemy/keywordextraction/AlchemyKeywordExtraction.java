package ibm.alchemy.keywordextraction;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyLanguage;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Entities;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Keyword;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Keywords;

import ibm.connection.CredentialParser;

/**
 * Class to call the Alchemy Language Keyword Extraction service Restriction:
 * 1,000 API Events per Day
 * 
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph S.
 */
public class AlchemyKeywordExtraction {
	private String credentialsPath = "./alchemyAPICredentials.json";
	private AlchemyLanguage service;

	public AlchemyKeywordExtraction() {
		initialize();
	}

	public static void main(String[] args) {
		// example for Relationship Extraction of an element
		AlchemyKeywordExtraction aKe = new AlchemyKeywordExtraction();
		// aKe.analyze("http://marvelcinematicuniverse.wikia.com/wiki/Natasha_Romanoff");
		String a = "";
		// aKe.analyzeText(a);
		// System.out.println("k: " +
		// System.out.println(aKe.getTopKElements(100,
		// aKe.analyze("http://marvelcinematicuniverse.wikia.com/wiki/Natasha_Romanoff")));
		System.out.println("k: " + aKe.getTopKElements(99, aKe.analyzeText(a)));
	}

	/**
	 * Returns the top i Keywords of an Keywords-Array
	 * 
	 * @param i
	 *            Keywords to return
	 * 
	 * @param keywordsArray
	 *            containing all the {@link Keywords} extracted from multiple
	 *            files
	 * 
	 * @return {@link List} containing the top i {@link Keywords}
	 */
	public List<Entry<String, Double>> getTopKElements(int i, Keywords[] keywordsArray) {

		Map resultMap = new HashMap<String, Double>();
		for (int j = 0; j < keywordsArray.length; j++) {
			for (Entry<String, Double> keyword : getTopKElements(i, keywordsArray[j])) {
				if (!resultMap.containsKey(keyword.getKey())) {
					resultMap.put(keyword.getKey(), keyword.getValue());
				} else {// contains
					if (Double.parseDouble(resultMap.get(keyword.getKey()).toString()) < keyword.getValue()) {
						resultMap.put(keyword.getKey(), keyword.getValue());
					}

				}
			}
		}
		List<Entry<String, Double>> sorted = (List) entriesSortedByValues(resultMap);
		List<Entry<String, Double>> filteredList = new ArrayList<Entry<String, Double>>();

		for (int j = 0; j < i; j++) {
			// falls nicht genügend elemente in Liste, gäbe es null pointer
			if (sorted.size() > j)
				filteredList.add(sorted.get(j));

		}
		return filteredList;

	}

	/**
	 * Returns the top i Keywords
	 * 
	 * @param i
	 *            Keywords to return
	 * @param keywords
	 *            containing all the {@link Keywords}
	 * @return {@link List} containing the top i {@link Keywords}
	 */
	public List<Entry<String, Double>> getTopKElements(int i, Keywords keywords) {
		JSONArray jsonArray;
		JSONObject jsonResult = null;
		Map resultMap = new HashMap<String, Double>();
		List<Keyword> keywordList = keywords.getKeywords();
		for (int j = 0; j < keywordList.size(); j++) {
			resultMap.put(keywordList.get(j).getText(), keywordList.get(j).getRelevance());
		}

		List<Entry<String, Double>> sorted = (List) entriesSortedByValues(resultMap);
		List<Entry<String, Double>> filteredList = new ArrayList<Entry<String, Double>>();

		for (int j = 0; j < i; j++) {
			// falls nicht genügend elemente in Liste, gäbe es null pointer
			if (sorted.size() > j) {
				filteredList.add(sorted.get(j));
			}
		}
		return filteredList;

	}

	/**
	 * Sort all the elements of a map.
	 */
	static <K, V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(Map<K, V> map) {

		List<Entry<K, V>> sortedEntries = new ArrayList<Entry<K, V>>(map.entrySet());

		Collections.sort(sortedEntries, new Comparator<Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> e1, Entry<K, V> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		});

		return sortedEntries;
	}

	/**
	 * Extract the Keywords of the text of a web page.
	 * 
	 * @param url
	 *            from the side to be analyzed e.g.
	 *            "http://marvelcinematicuniverse.wikia.com/wiki/
	 *            Natasha_Romanoff
	 * @return {@link Keywords}
	 */
	private Keywords analyze(String url) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(AlchemyLanguage.URL, url);
		Keywords keywords = service.getKeywords(params);
		return keywords;

	}

	/**
	 * Extract the Keywords of a text. If the text is longer than 50kb, it is
	 * splitted with equal frequency-
	 * 
	 * @param text
	 *            to be analyzed
	 * @return {@link Keywords} Array
	 */
	public Keywords[] analyzeText(String text) {
		int lengthOfTheText;
		final int maximumTextSize = 50000;
		JSONArray result = new JSONArray();
		try {
			byte[] textAsByte = text.getBytes("UTF-8");
			lengthOfTheText = textAsByte.length;
			// System.out.println(lengthOfTheText);
			// if Size of Text is greater than 50kb, we must split it --> equal
			// frequency ansatz
			Keywords[] keywordsArray = null;
			if (lengthOfTheText > maximumTextSize) {
				long muchTimes = Math.round(lengthOfTheText / (double) maximumTextSize);
				keywordsArray = new Keywords[(int) muchTimes];
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
					Keywords keywords = service.getKeywords(params);
					keywordsArray[i] = keywords;
				}
				return keywordsArray;

			} else {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put(AlchemyLanguage.TEXT, text);
				Keywords keywords = service.getKeywords(params);
				return new Keywords[] { keywords };
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;

	}

	private void initialize() {
		CredentialParser parser = new CredentialParser(credentialsPath);
		service = new AlchemyLanguage();
		service.setApiKey(parser.getPass());
	}
}
