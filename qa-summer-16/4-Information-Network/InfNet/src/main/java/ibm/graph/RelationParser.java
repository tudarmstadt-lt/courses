package ibm.graph;

import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

import ibm.alchemy.relationshipextraction.Relation;
import ibm.graph.schema.Schema;
import ibm.tools.Collectiontool;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S. This class has the task to parse json object retrieved from
 *         alchemy. The result should be Vertex and Edge instances, or just put
 *         these object in the graph db
 *
 */
public class RelationParser {

	private BluemixGraphDbClient client;
	private Schema schema;
	private int parsedVectors = 0;

	public RelationParser(Schema schema, BluemixGraphDbClient client) {
		this.schema = schema;
		this.client = client;
	}

	public void parseAndPush(List<Relation> list) {
		parseAndPush(list, false);
	}

	/**
	 * Parses the obj (from alchemy) and pushes it to GraphDB
	 * 
	 * @param softVertexEquals
	 *            if true, the DB checks only for given vertices by the name. if
	 *            false, the DB checks existing vertices by name and type
	 * @param list
	 */
	public void parseAndPush(List<Relation> list, boolean softVertexEquals) {
		Relation parsedRelation;
		// System.out.println("number of relations: " + list.size());

		for (int i = 0; i < list.size(); i++) {
			// if(i < 6085) continue;
			// System.out.println("Pushing relation " + (i+1) + " of " +
			// list.size());
			parsedRelation = list.get(i);

			String label1 = parsedRelation.getEntityOneType();
			String label2 = parsedRelation.getEntityTwoType();
			String name1 = parsedRelation.getEntityOne();
			String name2 = parsedRelation.getEntityTwo();
			float score = new Float(parsedRelation.getScore());
			String link = parsedRelation.getUrl();
			// String link = "";
			// String title = "";
			String title = parsedRelation.getTitle();
			String relation = parsedRelation.getRelation();// -->label

			Vertex v1, v2;
			if (Collectiontool.containsCaseless(schema.getVertexLabels(), label1)) {
				v1 = new Vertex(label1);
			} else {
				v1 = new Vertex("otherV");
				v1.addProperty(new Property("otherVertexLabel", Property.DATATYPE_STRING, label1));
			}
			if (Collectiontool.containsCaseless(schema.getVertexLabels(), label2)) {
				v2 = new Vertex(label2);
			} else {
				v2 = new Vertex("otherV");
				v2.addProperty(new Property("otherVertexLabel", Property.DATATYPE_STRING, label2));
			}
			v1.addProperty(new Property("name", Property.DATATYPE_STRING, name1));
			v2.addProperty(new Property("name", Property.DATATYPE_STRING, name2));
			v1.addProperty(new Property("dummy", Property.DATATYPE_STRING, "dummy"));
			v2.addProperty(new Property("dummy", Property.DATATYPE_STRING, "dummy"));
			v1.addProperty(new Property("count", Property.DATATYPE_INTEGER, parsedRelation.getEntityOneCount()));
			v2.addProperty(new Property("count", Property.DATATYPE_INTEGER, parsedRelation.getEntityTwoCount()));
			v1.addProperty(new Property("sourceTitle", Property.DATATYPE_STRING, title));
			v2.addProperty(new Property("sourceTitle", Property.DATATYPE_STRING, title));
			v1.addProperty(new Property("sourceURL", Property.DATATYPE_STRING, link));
			v2.addProperty(new Property("sourceURL", Property.DATATYPE_STRING, link));
			v1.addProperty(new Property("relevance", Property.DATATYPE_FLOAT, parsedRelation.getEntityOneRelevance()));
			v2.addProperty(new Property("relevance", Property.DATATYPE_FLOAT, parsedRelation.getEntityTwoRelevance()));

			parsedVectors += 2;

			int v1Id = client.addVertex(v1, softVertexEquals);
			int v2Id = client.addVertex(v2, softVertexEquals);

			List<String> edgeLabels = schema.getEdgeLabels().stream().map(el -> el.getLabel())
					.collect(Collectors.toList());
			Edge e1;
			if (Collectiontool.containsCaseless(edgeLabels, relation)) {
				e1 = new Edge(v1Id, v2Id, relation);
			} else {
				e1 = new Edge(v1Id, v2Id, "otherE");
				e1.addProperty(new Property("otherEdgeLabel", Property.DATATYPE_STRING, relation));
			}
			e1.addProperty(new Property("link", Property.DATATYPE_STRING, link));
			e1.addProperty(new Property("title", Property.DATATYPE_STRING, title));
			e1.addProperty(new Property("relationShipScore", Property.DATATYPE_FLOAT, score));
			e1.addProperty(new Property("dummyE", Property.DATATYPE_STRING, "dummyE"));
			e1.addProperty(new Property("sentence", Property.DATATYPE_STRING, parsedRelation.getSentence()));
			e1.addProperty(new Property("relationsMode", Property.DATATYPE_INTEGER, parsedRelation.getRelationsMode()));
			e1.addProperty(
					new Property("alchemyEdgeCount", Property.DATATYPE_INTEGER, parsedRelation.getAlchemyTupleCount()));

			client.addEdge(e1);
		}

	}
}
