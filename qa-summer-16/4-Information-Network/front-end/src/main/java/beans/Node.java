package beans;

import java.util.List;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon D.
 */
public class Node {

	private int id;
	private String text;
	private String type;
	private int group;
	private List<String> sentences;
	private List<String> urls;
	private List<String> titles;
	private int count;
	private double relevance;

	public Node(int id, String text, String type, int group, List<String> sentences, List<String> urls,
			List<String> titles, int count, double relevance) {
		super();
		this.id = id;
		this.text = text;
		this.type = type;
		this.group = group;
		this.sentences = sentences;
		this.urls = urls;
		this.titles = titles;
		this.count = count;
		this.relevance = relevance;
	}

	public Node() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (id != other.id)
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Node [id=" + id + ", text=" + text + ", type=" + type + "]";
	}

	public String toJson() {

		List<String> sentences = this.getSentences();
		StringBuilder sbSentencesJson = new StringBuilder();
		sbSentencesJson.append("[");
		for (String sentence : sentences) {
			sbSentencesJson.append("\"" + sentence + "\",");
		}
		if (sentences.size() > 0)
			sbSentencesJson.setLength(sbSentencesJson.length() - 1);
		sbSentencesJson.append("]");
		String sentencesJson = sbSentencesJson.toString();

		List<String> urls = this.getUrls();
		StringBuilder sbURLsJson = new StringBuilder();
		sbURLsJson.append("[");
		for (String url : urls) {
			sbURLsJson.append("\"" + url + "\",");
		}
		if (urls.size() > 0)
			sbURLsJson.setLength(sbURLsJson.length() - 1);
		sbURLsJson.append("]");
		String urlsJson = sbURLsJson.toString();

		List<String> titles = this.getTitles();
		StringBuilder sbtitlesJson = new StringBuilder();
		sbtitlesJson.append("[");
		for (String title : titles) {
			sbtitlesJson.append("\"" + title + "\",");
		}
		if (titles.size() > 0)
			sbtitlesJson.setLength(sbtitlesJson.length() - 1);
		sbtitlesJson.append("]");
		String titlesJson = sbtitlesJson.toString();

		return "{\"id\":\"" + id + "\", \"group\":\"" + group + "\", \"text\":\"" + text + "\", \"type\":\"" + type
				+ "\", \"sentences\":" + sentencesJson + ",\"urls\":" + urlsJson + ",\"titles\":" + titlesJson + "}";
	}

	public List<String> getSentences() {
		return sentences;
	}

	public void setSentences(List<String> sentences) {
		this.sentences = sentences;
	}

	public List<String> getUrls() {
		return urls;
	}

	public void setUrls(List<String> urls) {
		this.urls = urls;
	}

	public List<String> getTitles() {
		return titles;
	}

	public void setTitles(List<String> titles) {
		this.titles = titles;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public double getRelevance() {
		return relevance;
	}

	public void setRelevance(double relevance) {
		this.relevance = relevance;
	}

}
