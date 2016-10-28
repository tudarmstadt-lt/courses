package servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon D.
 */
public class GraphVizAlchemyAndRelExtractionServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -130861909289206227L;
	private static final String ATT_NODES = "nodesJSON";
	private static final String ATT_EDGES = "edgesJSON";

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String nodesJSON = readNodesJson("/nodesRelExt.json");
		String edgesJSON = readEdgesJson("/edgesRelExt.json");

		request.setAttribute(ATT_NODES, nodesJSON);
		request.setAttribute(ATT_EDGES, edgesJSON);

		this.getServletContext().getRequestDispatcher("/WEB-INF/graphVizAlchemyRelExtraction.jsp").forward(request,
				response);
	}

	private String readEdgesJson(String fileName) {

		String edgesJson = null;
		try {
			edgesJson = readJsonFromFile(fileName);
			System.out.println(edgesJson);
			// JSONArray array = new JSONArray(edgesJson);
			// edgesJson = array.toString();
			/*
			 * JSONObject obj = new JSONObject(edgesJson); if
			 * (obj.has("result")){ JSONObject result =
			 * obj.getJSONObject("result"); if (result.has("data")){ JSONArray
			 * data = result.getJSONArray("data"); edgesJson = data.toString();
			 * System.out.println(edgesJson); } }
			 */
		} catch (IOException e) {
			e.printStackTrace();
		}
		return edgesJson;
	}

	private String readNodesJson(String fileName) {

		String nodesJson = null;
		try {
			nodesJson = readJsonFromFile(fileName);
			System.out.println(nodesJson);
			// JSONArray array = new JSONArray(nodesJson);
			// nodesJson = array.toString();
			/*
			 * JSONObject obj = new JSONObject(nodesJson); if
			 * (obj.has("result")){ JSONObject result =
			 * obj.getJSONObject("result"); if (result.has("data")){ JSONArray
			 * data = result.getJSONArray("data"); nodesJson = data.toString();
			 * System.out.println(nodesJson); } }
			 */

		} catch (IOException e) {
			e.printStackTrace();
		}
		return nodesJson;
	}

	private String readJsonFromFile(String fileName) throws IOException {

		InputStream is = GraphVizAlchemyEntitiesServlet.class.getResourceAsStream(fileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = br.readLine();
		return line;
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}
}
