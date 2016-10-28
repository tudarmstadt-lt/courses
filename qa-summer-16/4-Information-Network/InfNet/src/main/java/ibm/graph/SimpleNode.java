package ibm.graph;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
public class SimpleNode {

	private String text;
	private String type;

	public SimpleNode() {
		super();
	}

	public SimpleNode(String text, String type) {
		super();
		this.text = text;
		this.type = type;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		SimpleNode other = (SimpleNode) obj;
		if (text.equals(other.text) && type.equals(other.type)) {
			return true;
		}
		return false;
	}

}
