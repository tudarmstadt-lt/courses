package ibm.alchemy.entity;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph S.
 */
public class Tupel {
	private String nodeOne = "";
	private String nodeTwo = "";
	private String edge = "";;
	private int count = 0;
	private String titles = "";
	private String urls = "";

	public String getUrls() {
		return urls;
	}

	public void setUrl(String url) {
		this.urls = this.urls + ";" + url;
	}

	public Tupel(String nodeOne, String nodeTwo, String title, String url) {
		super();
		this.nodeOne = nodeOne;
		this.nodeTwo = nodeTwo;
		this.edge = nodeOne + "-e-" + nodeTwo;
		this.count = 1;
		this.titles = title;
		this.urls = url;
	}

	public int getCount() {
		return count;
	}

	public String getNodeOne() {
		return nodeOne;
	}

	public String getNodeTwo() {
		return nodeTwo;
	}

	public String getEdge() {
		return edge;
	}

	public void setCount(int newCount) {
		this.count = newCount;
	}

	public void increaseCountByOne() {
		this.count = this.count + 1;
	}

	public String tupelAsString() {
		return this.nodeOne + "-:-" + this.nodeTwo + "-:-" + this.edge + "-:-" + this.count + "-:-" + this.titles
				+ "-:-" + this.urls;
	}

	public void setPageTitles(String title) {
		this.titles = this.titles + ";" + title;
	}

	public String getPageTitles() {
		return this.titles;
	}

}
