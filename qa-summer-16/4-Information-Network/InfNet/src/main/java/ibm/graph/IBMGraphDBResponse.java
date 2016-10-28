/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ibm.graph;

import org.json.JSONArray;
import org.json.JSONObject;

import ibm.connection.IBMStatusMessage;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S
 */
public class IBMGraphDBResponse {

	private IBMStatusMessage status = null;
	private JSONArray data = null;

	public IBMGraphDBResponse(JSONObject responseObj) {

		if (!responseObj.has("status")) {
			System.err.println("No status available. Error: " + responseObj.toString());
			throw new IllegalAccessError("Stop. No Status");
			// return;
		}
		JSONObject statusObj = responseObj.getJSONObject("status");
		status = new IBMStatusMessage(statusObj.getInt("code"), statusObj.getString("message"));

		if (status.getCode() == 200 || status.getCode() == 202) {
			JSONObject resultSubObj = responseObj.getJSONObject("result");
			data = resultSubObj.getJSONArray("data");
		} else {
			System.err.println("Request error: " + status.getCode() + " - " + status.getMessage());
		}
	}

	public IBMStatusMessage getStatus() {
		return status;
	}

	public JSONArray getData() {
		return data;
	}

}
