package beans;

import java.util.List;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon D.
 */
public class Edge {

	private String id;
	private int node1;
	private int node2;
	private double weight;
	private String title;
	private List<String> sentences;
	private List<String> urls;
	private List<String> pageTitles;
	private double relationshipScore;
	private int relationsMode;

	public Edge() {
		super();
	}

	public Edge(String id, int node1, int node2, double weight, String title, List<String> sentences, List<String> urls,
			List<String> pageTitles, double relationshipScore, int relationsMode) {
		super();
		this.id = id;
		this.node1 = node1;
		this.node2 = node2;
		this.weight = weight;
		this.title = title;
		this.sentences = sentences;
		this.urls = urls;
		this.pageTitles = pageTitles;
		this.relationshipScore = relationshipScore;
		this.relationsMode = relationsMode;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getNode1() {
		return node1;
	}

	public void setNode1(int node1) {
		this.node1 = node1;
	}

	public int getNode2() {
		return node2;
	}

	public void setNode2(int node2) {
		this.node2 = node2;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + node1;
		result = prime * result + node2;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		long temp;
		temp = Double.doubleToLongBits(weight);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		Edge other = (Edge) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (node1 != other.node1)
			return false;
		if (node2 != other.node2)
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (Double.doubleToLongBits(weight) != Double.doubleToLongBits(other.weight))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Edge [id=" + id + ", node1=" + node1 + ", node2=" + node2 + ", weight=" + weight + "]";
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

		List<String> titles = this.getPageTitles();
		StringBuilder sbtitlesJson = new StringBuilder();
		sbtitlesJson.append("[");
		for (String title : titles) {
			sbtitlesJson.append("\"" + title + "\",");
		}
		if (titles.size() > 0)
			sbtitlesJson.setLength(sbtitlesJson.length() - 1);
		sbtitlesJson.append("]");
		String titlesJson = sbtitlesJson.toString();

		return "{\"id\":\"" + id + "\", \"title\":\"" + title + "\", \"node1\":\"" + node1 + "\", \"node2\":\"" + node2
				+ "\", \"weight\":\"" + weight + "\", \"sentences\":" + sentencesJson + ",\"urls\":" + urlsJson
				+ ",\"pageTitles\":" + titlesJson + "}";
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public double getRelationshipScore() {
		return relationshipScore;
	}

	public void setRelationshipScore(double relationshipScore) {
		this.relationshipScore = relationshipScore;
	}

	public int getRelationsMode() {
		return relationsMode;
	}

	public void setRelationsMode(int relationsMode) {
		this.relationsMode = relationsMode;
	}

	public List<String> getPageTitles() {
		return pageTitles;
	}

	public void setPageTitles(List<String> pageTitles) {
		this.pageTitles = pageTitles;
	}

}
