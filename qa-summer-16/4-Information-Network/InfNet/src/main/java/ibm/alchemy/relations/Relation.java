package ibm.alchemy.relations;

import org.json.JSONObject;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph S.
 */
public class Relation {
	String originSentence;
	String action;

	String object;

	String originObject;

	String subject;

	String originSubject;
	String link;

	public Relation(String originSentence, String action, String object, String originObject, String subject,
			String originSubject, String link) {
		super();
		this.originSentence = originSentence;
		this.action = action;
		this.object = object;
		this.originObject = originObject;
		this.subject = subject;
		this.originSubject = originSubject;
		this.link = link;
	}

	public Relation(JSONObject json) {
		this(json.getString("originSentence"), json.getString("action"), json.getString("object"),
				json.getString("originObject"), json.getString("subject"), json.getString("originSubject"),
				json.getString("link"));
	}

	public JSONObject getAsJsonObject() {
		JSONObject rel = new JSONObject();
		rel.put("originSentence", originSentence);
		rel.put("originSubject", originSubject);
		rel.put("subject", subject);
		rel.put("originObject", originObject);
		rel.put("object", object);
		rel.put("action", action);
		rel.put("link", link);
		return rel;
	}

	public String getAction() {
		return action;
	}

	public String getObject() {
		return object;
	}

	public String getOriginObject() {
		return originObject;
	}

	public String getOriginSentence() {
		return originSentence;
	}

	public String getOriginSubject() {
		return originSubject;
	}

	public String getSubject() {
		return subject;
	}

	public String getLink() {
		return link;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public void setOriginObject(String originObject) {
		this.originObject = originObject;
	}

	public void setOriginSentence(String originSentence) {
		this.originSentence = originSentence;
	}

	public void setOriginSubject(String originSubject) {
		this.originSubject = originSubject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void print() {
		System.out.println("originSentence: " + originSentence + " originSubject: " + originSubject + " subject: "
				+ subject + " originObject: " + originObject + " object: " + object + " action: " + action + " link: "
				+ link);
	}
}
