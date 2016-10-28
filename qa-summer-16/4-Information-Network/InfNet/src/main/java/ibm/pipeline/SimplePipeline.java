package ibm.pipeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ibm.watson.developer_cloud.alchemy.v1.model.Entities;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Entity;
import com.ibm.watson.developer_cloud.alchemy.v1.model.SAORelations;

import ibm.alchemy.entity.AlchemyAPIClient;
import ibm.alchemy.entity.AlchemyEntitiesHandler;
import ibm.alchemy.entity.AlchemyEntitiesWithFurtherInformation;
import ibm.alchemy.entity.Tupel;
import ibm.alchemy.relations.AlchemyRelationsClient;
import ibm.alchemy.relations.RelationHandler;
import ibm.alchemy.relationshipextraction.AlchemyRelationshipExtraction;
import ibm.alchemy.relationshipextraction.InfoBoxRelationsHandler;
import ibm.alchemy.relationshipextraction.Relation;
import ibm.alchemy.relationshipextraction.RelationsHandler;
import ibm.graph.BluemixGraphDbClient;
import ibm.graph.RelationParser;
import ibm.graph.schema.Schema;
import ibm.graph.schema.SchemaHandling;
import marvel.html.ExtractImportantSections;
import marvel.html.HTMLParser;

/**
 * 
 * running a simple as possible pipeline with this
 * 
 * The result should be like
 * 
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
public class SimplePipeline {

	private static final String FILENAME = "testSet/New York City-4215.html";

	private List<JSONObject> nodes;
	private List<JSONObject> edges;

	/**
	 * searches for an Entity which represents the given 'test'-String as good
	 * as possible. if no entity is found, this method returns null and the
	 * given test-string belonging to a relationship entity is not good for
	 * further use.
	 * 
	 * @param test
	 * @param entities
	 * @return
	 */
	private Entity isRelationEntityAGoodEntity(String test, List<Entity> entities) {
		for (Entity e : entities) {
			if (testGoodEntity(test, e.getText())) {
				return e;
			}
		}
		return null;
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

	public List<JSONObject> getNodes() {
		return nodes;
	}

	public List<JSONObject> getEdges() {
		return edges;
	}

	public void deleteOldGraph(BluemixGraphDbClient client) {
		List<String> allGraphIds = client.getAllUsedGraphIDs();
		for (String id : allGraphIds) {
			if (id.equals("g")) {
				continue;
			}
			client = new BluemixGraphDbClient(id);
			client.deleteGraph();
		}
	}

	public String createNewGraph(BluemixGraphDbClient client) {
		String graphid = client.createGraph();
		return graphid;
	}

	public void setSchema(String graphId, BluemixGraphDbClient client) {
		// set the schema
		Schema schema = Schema.getSchema();
		SchemaHandling schemaHandling = new SchemaHandling(client);
		schemaHandling.setSchema(schema);
	}

	public static void main(String[] args) {
		SimplePipeline p = new SimplePipeline();
		BluemixGraphDbClient client = new BluemixGraphDbClient(null);
		p.deleteOldGraph(client);
		String graphId = p.createNewGraph(client);
		client = new BluemixGraphDbClient(graphId);
		p.setSchema(graphId, client);

		p.run(FILENAME, graphId);
	}

	public void run(String filename, String graphId) {
		BluemixGraphDbClient client = new BluemixGraphDbClient(graphId);
		BluemixGraphDbClient.pushedVertices = 0;
		BluemixGraphDbClient.puttedVertices = 0;
		BluemixGraphDbClient.pushedEdges = 0;
		BluemixGraphDbClient.puttedEdges = 0;

		// set the schema
		Schema schema = Schema.getSchema();

		// push to db
		RelationParser relationParser = new RelationParser(schema, client);

		// 1. Step: parse the html file
		ExtractImportantSections eIS = new ExtractImportantSections();
		eIS.extract(HTMLParser.parseHTMLFile(filename), null);
		List<JSONObject> extractedInfoInJSONObjs = eIS.getJSONObjects();

		// 2. Step: get the entities
		AlchemyAPIClient alchemy = new AlchemyAPIClient();
		List<Entity> extractedEntities = new ArrayList<>();
		for (JSONObject json : extractedInfoInJSONObjs) {
			Entities[] entities = alchemy.analyzeText(json.getString("text"));
			for (Entities en : entities) {
				List<Entity> l_en = en.getEntities();
				for (Entity entity : l_en) {
					if (!extractedEntities.contains(entity)) {
						extractedEntities.add(entity);
					}
				}
			}
		}

		// Step 3: get the relationships
		AlchemyRelationshipExtraction aRE = new AlchemyRelationshipExtraction();
		List<JSONObject> alchemyRelationships = new ArrayList<>();
		for (JSONObject json : extractedInfoInJSONObjs) {
			JSONObject[] alchemiesRels = aRE.analyzeText(json.getString("text"));
			for (JSONObject rel : alchemiesRels) {
				alchemyRelationships.add(rel.put("title", json.getString("title")).put("link", json.getString("link")));
			}
		}

		// 4.1 filter relations after the entities
		List<Relation> filteredRelations = new ArrayList<>();
		RelationsHandler handler = new RelationsHandler();
		for (JSONObject jsonRelation : alchemyRelationships) {
			List<Relation> relations = handler.createRelationsData(jsonRelation);
			if (relations == null) {
				continue;
			}
			for (Relation r : relations) {
				Entity one = isRelationEntityAGoodEntity(r.getEntityOne(), extractedEntities);
				if (one != null) {
					Entity two = isRelationEntityAGoodEntity(r.getEntityTwo(), extractedEntities);
					if (two != null) {
						Relation filteredR = new Relation(one.getText(), r.getEntityOneType(), two.getText(),
								r.getEntityTwoType(), r.getRelation(), r.getScore(), r.getTitle(), r.getUrl(),
								one.getCount(), two.getCount(), one.getRelevance().floatValue(),
								two.getRelevance().floatValue(), r.getSentence(), 1, 0);
						filteredRelations.add(filteredR);
					}
				}
			}
		}
		relationParser.parseAndPush(filteredRelations);
		printCounts("Relationships: ");
		resetCounter();

		// 4.2. infoboxes
		InfoBoxRelationsHandler relHandler = new InfoBoxRelationsHandler();
		for (JSONObject extractedInfo : extractedInfoInJSONObjs) {
			String link_EE = extractedInfo.getString("link");
			String title_EE = extractedInfo.getString("title");

			JSONArray arr = eIS.extractInfoboxes(HTMLParser.parseHTMLFile(filename));
			;
			List<Relation> relations = relHandler.createRelationsData(arr, title_EE, link_EE);
			if (relations != null) {
				relationParser.parseAndPush(relations);
				printCounts("Infoboxes: ");
				resetCounter();
			}
		}

		// 4.3. SOA
		ibm.alchemy.relations.RelationHandler soaHandler = new RelationHandler();
		AlchemyRelationsClient aRC = new AlchemyRelationsClient();
		List<ibm.alchemy.relations.Relation> filteredSAORelations = new ArrayList<>();
		AlchemyEntitiesHandler aEH = new AlchemyEntitiesHandler();
		Entities[] soaExtractedEntities = aEH.createNewEntitiesByOldEntities(extractedEntities);
		for (JSONObject extractedInfo : extractedInfoInJSONObjs) {
			SAORelations[] saoUnfiltererd = aRC.analyzeText(extractedInfo.getString("text"), "1", "1");
			JSONArray jsonArray = new JSONArray();
			for (int j = 0; j < saoUnfiltererd.length; j++) {
				jsonArray.put(new JSONObject(saoUnfiltererd[j]).put("url", extractedInfo.getString("link")));
			}
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject currentSoaJson = jsonArray.getJSONObject(i);
				filteredSAORelations.addAll(soaHandler.createRelations(currentSoaJson, soaExtractedEntities));
			}
		}

		List<Relation> relations = new ArrayList<>();
		for (ibm.alchemy.relations.Relation r : filteredSAORelations) {
			relations.add(new Relation(r.getSubject(), "SOA_ORIG_SUBJ: " + r.getOriginSubject(), r.getObject(),
					"SOA_ORIG_OBJ: " + r.getOriginObject(), r.getAction(), "0.0", "", r.getLink(), 0, 0, 0.0f, 0.0f,
					r.getOriginSentence(), 4, 0));
		}
		relationParser.parseAndPush(relations);
		printCounts("SAo: ");
		resetCounter();

		// 4.4. entity graph
		for (JSONObject extractedIno : extractedInfoInJSONObjs) {
			AlchemyEntitiesWithFurtherInformation aEWFI = new AlchemyEntitiesWithFurtherInformation(
					soaExtractedEntities, extractedIno.getString("url"), extractedIno.getString("title"));
			List<Tupel> edges = aEH.createJsonEdgesForFile(aEWFI);
			List<Relation> entityRelations = new ArrayList<Relation>();
			for (Tupel t : edges) {
				entityRelations.add(new Relation(t.getNodeOne(), "", t.getNodeTwo(), "", t.getEdge(), "0.0",
						t.getPageTitles(), t.getUrls(), 0, 0, 0.0f, 0.0f, "", 8, t.getCount()));
			}
			relationParser.parseAndPush(entityRelations);
			printCounts("Entity Graph: ");
		}
	}

	private void printCounts(String msg) {
		System.out.println(msg + "Vertices(pushed, putted), Edges(pushed, putted): ["
				+ BluemixGraphDbClient.pushedVertices + ", " + BluemixGraphDbClient.puttedVertices + "], ["
				+ BluemixGraphDbClient.pushedEdges + ", " + BluemixGraphDbClient.puttedEdges + "]");
	}

	private void resetCounter() {
		BluemixGraphDbClient.pushedVertices = 0;
		BluemixGraphDbClient.puttedVertices = 0;
		BluemixGraphDbClient.pushedEdges = 0;
		BluemixGraphDbClient.puttedEdges = 0;
	}

}
