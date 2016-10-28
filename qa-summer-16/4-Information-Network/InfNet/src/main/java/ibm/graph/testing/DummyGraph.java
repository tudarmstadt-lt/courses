/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ibm.graph.testing;

import ibm.graph.BluemixGraphDbClient;
import ibm.graph.Edge;
import ibm.graph.Property;
import ibm.graph.Vertex;
import ibm.graph.schema.Schema;
import ibm.graph.schema.SchemaHandling;
import ibm.graph.schema.VertexIndex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph S. und Simon S.
 */
public class DummyGraph {

	public static void main(String[] args) {
		// sampleUsagePutSchema();

		/**
		 * Creates a Graph with 3 Nodes an 3 Edges. Nodes a,b,c Edges e1,e2,e3
		 * Graph: a-e1-b, b-e2-c, c-e3-a
		 */
		/*
		 * List<Property> properties = new ArrayList<Property>(); Property
		 * property = new Property("dummy", 4, "dummy");
		 * properties.add(property);
		 * 
		 * Vertex a = new Vertex("a", properties); client.addVertex(a); Vertex b
		 * = new Vertex("b", properties); client.addVertex(b); Vertex c = new
		 * Vertex("c", properties); client.addVertex(c); System.out.println(
		 * "Vertices added!");
		 * 
		 * Edge e1 = new Edge(vertexBiMap.get("a").intValue(),
		 * vertexBiMap.get("b").intValue(), "e1", properties);
		 * client.addEdge(e1); Edge e2 = new Edge(vertexBiMap.get("b"),
		 * vertexBiMap.get("c"), "e2", properties); client.addEdge(e2); Edge e3
		 * = new Edge(vertexBiMap.get("b"), vertexBiMap.get("c"), "e3",
		 * properties); client.addEdge(e3); System.out.println("Edges added!");
		 */
	}

}
