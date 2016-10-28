package tests;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import ibm.graph.BluemixGraphDbClient;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
public class GetRandomRelations {

	public static void main(String[] args) {
		BluemixGraphDbClient client = new BluemixGraphDbClient("35e5b152-4189-4c2d-9486-a5cc6066f12b");
		//List<String> allKinds = client.getRandomEdgeIds(100);
		List<String> alchemyelations = client.getRandomEdgeIds(400, 1);
		List<String> infobox = client.getRandomEdgeIds(400, 2);
		List<String> soa = client.getRandomEdgeIds(400, 4);
		List<String> entitygraph = client.getRandomEdgeIds(400, 8);

		//saveListToFile(allKinds, "100EdgeIds__all_kinds.txt");
		saveListToFile(alchemyelations, "400EdgeIds__alchemy.txt");
		saveListToFile(infobox, "400EdgeIds__infobox.txt");
		saveListToFile(soa, "400EdgeIds__soa.txt");
		saveListToFile(entitygraph, "400EdgeIds__entity_graph.txt");
		System.out.println("Finished.");
	}

	public static void saveListToFile(List<String> elements, String path) {
		try {
			Files.write(Paths.get(path), elements, Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
