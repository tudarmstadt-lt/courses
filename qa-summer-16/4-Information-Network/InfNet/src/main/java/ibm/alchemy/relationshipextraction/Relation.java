package ibm.alchemy.relationshipextraction;

/**
 * Represents the important facts of an Alchemy Relationship Extraction
 * Relation, that we need for our Application.
 * 
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph S.
 */
public class Relation {

	public Relation(String entityOne, String entityOneType, String entityTwo, String entityTwoType, String relation,
			String score, String title, String url, int entityOneCount, int entityTwoCount, float entityOneRelevance,
			float entityTwoRelevance, String sentence, int relationsMode, int alchemyTupleCount) {
		super();
		this.entityOne = entityOne;
		this.entityOneType = entityOneType;
		this.entityTwo = entityTwo;
		this.entityTwoType = entityTwoType;
		this.relation = relation;
		this.score = score;
		this.title = title;
		this.url = url;
		this.entityOneCount = entityOneCount;
		this.entityTwoCount = entityTwoCount;
		this.entityOneRelevance = entityOneRelevance;
		this.entityTwoRelevance = entityTwoRelevance;
		this.sentence = sentence;
		this.relationsMode = relationsMode;
		this.alchemyTupleCount = alchemyTupleCount;
	}

	private int entityOneCount;
	private int entityTwoCount;
	private String entityOne;
	private String entityOneType;
	private String entityTwo;
	private String entityTwoType;
	private String relation;
	private String score;
	private String title;
	private String url;
	private float entityOneRelevance;
	private float entityTwoRelevance;
	private String sentence;
	private int relationsMode;
	private int alchemyTupleCount;

	public String getEntityOne() {
		return entityOne;
	}

	public String getEntityOneType() {
		return entityOneType;
	}

	public String getEntityTwo() {
		return entityTwo;
	}

	public String getEntityTwoType() {
		return entityTwoType;
	}

	public String getRelation() {
		return relation;
	}

	public String getScore() {
		return score;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public void printRelation() {
		System.out.println("entityOne: " + entityOne + " type: " + entityOneType + " entityTwo: " + entityTwo
				+ " type: " + entityTwoType + " relation: " + relation + " score: " + score + " title: " + title
				+ " url:" + url);
		// System.out.println(entityOne + " : " + entityTwo);
	}

	public int getEntityOneCount() {
		return entityOneCount;
	}

	public int getEntityTwoCount() {
		return entityTwoCount;
	}

	public float getEntityOneRelevance() {
		return entityOneRelevance;
	}

	public float getEntityTwoRelevance() {
		return entityTwoRelevance;
	}

	public String getSentence() {
		return sentence;
	}

	public int getRelationsMode() {
		return relationsMode;
	}

	public int getAlchemyTupleCount() {
		return alchemyTupleCount;
	}
}
