package ibm.tools;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 */
public class Collectiontool {

	/**
	 * Source:
	 * http://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-
	 * java
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		Map<K, V> result = new LinkedHashMap<>();
		Stream<Map.Entry<K, V>> st = map.entrySet().stream();

		st.sorted(Map.Entry.comparingByValue()).forEachOrdered(e -> result.put(e.getKey(), e.getValue()));

		return result;
	}

	/**
	 * checks if element is in list, NOT casesensitive
	 * 
	 * @param list
	 * @param element
	 * @return
	 */
	public static boolean containsCaseless(List<String> list, String element) {
		String el = element.toUpperCase();
		for (String i : list) {
			if (i.toUpperCase().equals(el)) {
				return true;
			}
		}
		return false;

	}
}
