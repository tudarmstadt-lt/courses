package graphdb;

import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph S. and Simon S.
 * 
 *         Should we return JSONObject (response) for each of the methods?
 *         Better Idea: parse the status of the message: if it is 200, then
 *         return the result-data. Else -> some error handling
 */
public class BluemixHttpServices {
	private CloseableHttpClient client = null;
	private String apiURL = null;
	private String userId = null;
	private String password = null;
	private String sessionToken = null;

	public BluemixHttpServices(String apiURL, String userId, String password) {
		client = HttpClients.createDefault();
		this.apiURL = apiURL;
		this.userId = userId;
		this.password = password;
	}

	public IBMGraphDBResponse ibmPUT(String uri, JSONObject jsonContent) {
		return new IBMGraphDBResponse(httpPUT(uri, jsonContent));
	}

	public IBMGraphDBResponse ibmGET(String uri, JSONObject jsonContent) {
		return new IBMGraphDBResponse(httpGET(uri, jsonContent));
	}

	public IBMGraphDBResponse ibmPOST(String uri, JSONObject jsonContent) {
		return new IBMGraphDBResponse(httpPOST(uri, jsonContent));
	}

	public IBMGraphDBResponse ibmDELETE(String uri) {
		return new IBMGraphDBResponse(httpDELETE(uri));
	}

	public JSONObject httpPUT(String uri, JSONObject jsonContent) {
		try {
			// uri e.g. "/vertices"
			String postURL = apiURL + uri;

			// PUT to the bluemix service
			HttpPut httpPut = new HttpPut(postURL);
			if (sessionToken == null) {
				byte[] userpass = (userId + ":" + password).getBytes();
				byte[] encoding = Base64.encodeBase64(userpass);
				httpPut.setHeader("Authorization", "Basic " + new String(encoding));
			} else {
				httpPut.setHeader("Authorization", sessionToken);
			}
			if (jsonContent != null) {
				StringEntity strEnt = new StringEntity(jsonContent.toString(), ContentType.APPLICATION_JSON);
				httpPut.setEntity(strEnt);
			}
			// send the put and retrieve the response from the Bluemix service
			HttpResponse httpResponse = client.execute(httpPut);
			HttpEntity httpEntity = httpResponse.getEntity();
			String content = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			// the JSON response body from the Bluemix GraphDB service
			JSONObject jsonResponseContent = new JSONObject(content);
			// System.out.println("JSON response body: " + content);

			// TODO: check for a 401 and the other http status messages
			int code = httpResponse.getStatusLine().getStatusCode();
			if (code != 200) {
				System.out.println("statuscode: " + code + " - response='" + content + "' ");
			}

			return jsonResponseContent;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public JSONObject httpPOST(String uri, JSONObject jsonContent) {
		try {
			// uri e.g. "/vertices"
			String postURL = apiURL + uri;
			// System.out.println("POST:" + postURL);

			// POST to the bluemix service
			HttpPost httpPost = new HttpPost(postURL);
			if (sessionToken == null) {
				byte[] userpass = (userId + ":" + password).getBytes();
				byte[] encoding = Base64.encodeBase64(userpass);
				httpPost.setHeader("Authorization", "Basic " + new String(encoding));
			} else {
				httpPost.setHeader("Authorization", sessionToken);
			}
			httpPost.setHeader("Content-Type", "application/json");
			httpPost.setHeader("Accept", "application/json");

			if (jsonContent != null) {
				StringEntity strEnt = new StringEntity(jsonContent.toString(), ContentType.APPLICATION_JSON);
				httpPost.setEntity(strEnt);
			}
			// send the post and retrieve the response from the Bluemix service
			HttpResponse httpResponse = client.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			String content = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			// the JSON response body from the Bluemix GraphDB service
			JSONObject jsonResponseContent = new JSONObject(content);
			// System.out.println("JSON response body: " + content);

			// TODO: check for a 401 and the other http status messages
			int code = httpResponse.getStatusLine().getStatusCode();

			if (code != 200) {
				System.out.println("statuscode: " + code + " - response='" + content + "' ");
			}

			return jsonResponseContent;

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println("Finish!");
		return null;
	}

	public JSONObject httpGET(String uri, JSONObject jsonContent) {
		try {
			// uri e.g. "/vertices"
			String postURL = apiURL + uri;
			// System.out.println("GET:" + postURL);

			// GET to the bluemix service
			HttpGet httpGet = new HttpGet(postURL);
			if (sessionToken == null) {
				byte[] userpass = (userId + ":" + password).getBytes();
				byte[] encoding = Base64.encodeBase64(userpass);
				httpGet.setHeader("Authorization", "Basic " + new String(encoding));
			} else {
				httpGet.addHeader("Authorization", sessionToken);
			}
			// send the get and retrieve the response from the Bluemix service
			HttpResponse httpResponse = client.execute(httpGet);
			HttpEntity httpEntity = httpResponse.getEntity();
			String content = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			// the JSON response body from the Bluemix GraphDB service
			JSONObject jsonResponseContent = new JSONObject(content);
			// System.out.println("JSON response body: " + content);

			// check for a 401 and the other http status messages
			int code = httpResponse.getStatusLine().getStatusCode();
			if (code != 200) {
				System.out.println("statuscode: " + code + " - response='" + content + "' ");
			}

			return jsonResponseContent;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public JSONObject httpDELETE(String uri) {
		try {
			// uri e.g. "/vertices"
			String postURL = apiURL + uri;

			// POST to the bluemix service
			HttpDelete httpDelete = new HttpDelete(postURL);
			if (sessionToken == null) {
				byte[] userpass = (userId + ":" + password).getBytes();
				byte[] encoding = Base64.encodeBase64(userpass);
				httpDelete.setHeader("Authorization", "Basic " + new String(encoding));
			} else {
				httpDelete.setHeader("Authorization", sessionToken);
			}
			// send the delete and retrieve the response from the Bluemix
			// service
			HttpResponse httpResponse = client.execute(httpDelete);
			HttpEntity httpEntity = httpResponse.getEntity();
			String content = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			// the JSON response body from the Bluemix GraphDB service
			JSONObject jsonResponseContent = new JSONObject(content);
			// System.out.println("JSON response body: " + content);

			// tODO: check for a 401 and the other http status messages
			// we check the ibm status messages, not the http
			int code = httpResponse.getStatusLine().getStatusCode();
			if (code != 200) {
				System.out.println("statuscode: " + code + " - response='" + content + "' ");
			}

			return jsonResponseContent;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * After the session token is set, it will be used instead of Basic
	 * autentication After the session set tokens will this used instead of
	 * Basic authentication. You are not longer limited limited to an average
	 * rate of 1 request per second, with a maximum of 3 requests per second.
	 * 
	 * @param sessionToken
	 */
	public void setSessionToken(String sessionToken) {
		this.sessionToken = sessionToken;

	}

	public JSONObject httpPOSTWithParameters(String uri, List<NameValuePair> qparams) {
		try {
			HttpPost httpPost;

			if (!(uri.contains("gateway-a.watsonplatform"))) {
				// uri e.g. "/vertices"
				String postURL = apiURL + uri;

				// POST to the bluemix service
				httpPost = new HttpPost(postURL);
				if (sessionToken == null) {
					byte[] userpass = (userId + ":" + password).getBytes();
					byte[] encoding = Base64.encodeBase64(userpass);
					httpPost.setHeader("Authorization", "Basic " + new String(encoding));
				} else {
					httpPost.setHeader("Authorization", sessionToken);
				}
			} else {
				// POST to the bluemix service
				httpPost = new HttpPost(uri);

				qparams.add(new BasicNameValuePair("apikey", password));

				httpPost.setEntity(new UrlEncodedFormEntity(qparams));
			}

			httpPost.setEntity(new UrlEncodedFormEntity(qparams));
			// send the post and retrieve the response from the Bluemix service
			HttpResponse httpResponse = client.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			String content = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);
			// the JSON response body from the Bluemix GraphDB service
			// System.out.println(content);
			JSONObject jsonResponseContent = new JSONObject(content);
			// System.out.println("JSON response body: " + content);

			// TODO: check for a 401 and the other http status messages
			int code = httpResponse.getStatusLine().getStatusCode();
			if (code != 200) {
				System.out.println("statuscode: " + code + " - response='" + content + "' ");
			}

			return jsonResponseContent;

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println("Finish!");
		return null;
	}

}
