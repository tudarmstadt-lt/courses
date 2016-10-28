package ibm.alchemy.entity;

import com.ibm.watson.developer_cloud.alchemy.v1.model.Entities;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph s.
 */
public class AlchemyEntitiesWithFurtherInformation {

	Entities[] entities;
	String link;
	String title;

	public AlchemyEntitiesWithFurtherInformation(Entities[] entities, String link, String title) {
		super();
		this.entities = entities;
		this.link = link;
		this.title = title;
	}

	public Entities[] getEntities() {
		return entities;
	}

	public void setEntities(Entities[] entities) {
		this.entities = entities;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
