package ibm.tools;

import java.util.Map;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * 
 */
public class MapUtil {

	public static <T> void incrementMapForKey(Map<T, Integer> map, T key) {
		if (!map.containsKey(key)) {
			map.put(key, 1);
		} else {
			map.put(key, map.get(key) + 1);
		}
	}

}
