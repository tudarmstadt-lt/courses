/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphdb;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author simon
 */
public class CredentialParser {

	private String apiURL;
	private String user;
	private String pass;

	public CredentialParser(String path) {
		if (path == null) {
			return;
		}
		try {

			String fileContent = new String(Files.readAllBytes(Paths.get(path)));
			parse(fileContent);
		} catch (IOException ex) {
			ex.printStackTrace();
			Logger.getLogger(CredentialParser.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void parse(String fileContent) {
		JSONObject jsonCredentials = new JSONObject(fileContent).getJSONObject("credentials");
		if (jsonCredentials.isNull("apiURL"))
			this.apiURL = jsonCredentials.getString("url");
		else
			this.apiURL = jsonCredentials.getString("apiURL");
		if (!(jsonCredentials.isNull("username")))
			this.user = jsonCredentials.getString("username");
		if (jsonCredentials.isNull("apikey"))
			this.pass = jsonCredentials.getString("password");
		else
			this.pass = jsonCredentials.getString("apikey");
	}

	public String getApiURL() {
		return apiURL;
	}

	public String getUser() {
		return user;
	}

	public String getPass() {
		return pass;
	}

}
