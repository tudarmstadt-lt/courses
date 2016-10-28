package tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ibm.graph.BluemixGraphDbClient;
import ibm.pipeline.Pipeline;
import ibm.pipeline.Writer;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
@Deprecated
public class PipelineTest {

	private static String GRAPHDB_ID = "82f218fe-7fe5-4714-a6af-8f5fff39bd08";

	@SuppressWarnings("unused")
	public static void main(String[] args) {

		String oldProp = "http://marvelcinematicuniverse.wikia.com/wiki/Maya_Hansen";
		List<String> oldElements = new ArrayList<>(Arrays.asList(oldProp.split("__SPLIT_CHARACTER__")));
		oldElements.add("wuhu");
		// oldElements.add("wurst");
		String mergedElements = String.join("__SPLIT_CHARACTER__", oldElements);

		boolean a = testGoodEntity("hallo", "hallo");// true
		boolean b = testGoodEntity("hallo", "hallo welt");// true
		boolean c = testGoodEntity("hallo", "welt");// false
		boolean d = testGoodEntity("lo", "hallo welt");// false
		boolean e = testGoodEntity("maja hansen", "doctor maja hansen");// true

		int x = 23;
		/*
		 * Pipeline p = new Pipeline("pipelineSubsetData");
		 * //p.registerTransition(new Extractor("html", "extractedInfo"));
		 * //p.registerTransition(new
		 * RelationshipExtractionParser("extractedInfo", "nodesAndEdges"));
		 * 
		 * p.preloadResults(Pipeline.TransitionType.PARSER, "nodesAndEdges");
		 * p.registerTransition(new Writer("nodesAndEdges"));
		 * 
		 * p.run();
		 * 
		 * BluemixGraphDbClient client = new BluemixGraphDbClient(GRAPHDB_ID);
		 * client.saveGraphAsJsonFiles();
		 */

	}

	private static boolean testGoodEntity(String is, String entityText) {
		String test = is.toLowerCase();
		String shouldBe = entityText.toLowerCase();
		if (test.equals(shouldBe)) {
			return true;
		}

		String[] testArr = test.split(" ");

		List<String> shouldBeArr = Arrays.asList(shouldBe.split(" "));
		for (String test_part : testArr) {
			if (!shouldBeArr.contains(test_part)) {
				return false;
			}
		}

		return true;
	}

}
