/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphdb;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Christoph S. and Simon S.
 * 
 *         Property keys are objects with the following properties: - name: A
 *         unique string. This value is required. - datatype: Supported data
 *         types are Integer, Float, Boolean, and String. This value is
 *         required. - cardinality: One of SINGLE, LIST, or SET. The default is
 *         SINGLE. See Property Key Cardinality in the Titan documentation.
 */
public class Property {

	public static final int CARDINALITY_SINGLE = 0;
	public static final int CARDINALITY_LIST = 1;
	public static final int CARDINALITY_SET = 2;

	public static final int DATATYPE_INTEGER = 0;
	public static final int DATATYPE_FLOAT = 1;
	public static final int DATATYPE_BOOLEAN = 2;
	public static final int DATATYPE_STRING = 3;

	private List<String> datatypeStrings = Arrays.asList("Integer", "Float", "Boolean", "String");
	private List<String> cardinalityStrings = Arrays.asList("SINGLE", "LIST", "SET");

	private String name = null;

	private int datatype = -1;

	// default: single
	private int cardinality = -1;

	// the value of the property
	private Object value = null;

	/**
	 * 
	 * @param name
	 * @param datatype
	 * @param value
	 */
	public Property(String name, int datatype, Object value) {

		this.name = name;
		this.datatype = datatype;

		if (value != null) {
			this.value = value;

			if (getDataType(value) != datatype) {
				System.err.println("Couldn't create property: datatype missmatch: " + datatype + " - " + value);
				return;
			}
		}

	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public int getCardinality() {
		return cardinality;
	}

	public void setCardinality(int cardinality) {
		this.cardinality = cardinality;
	}

	public int getDataType() {
		return datatype;
	}

	public void setDataType(int datatype) {
		this.datatype = datatype;
	}

	public static int getDataType(Object test) {
		if (test instanceof Integer) {
			return DATATYPE_INTEGER;
		} else if (test instanceof Float) {
			return DATATYPE_FLOAT;
		} else if (test instanceof Boolean) {
			return DATATYPE_BOOLEAN;
		} else {
			return DATATYPE_STRING;
		}
	}

	public int getDataType(String type) {
		return this.datatypeStrings.indexOf(type);
	}

	public int getCardinality(String cardinality) {
		return this.cardinalityStrings.indexOf(cardinality);
	}

	/**
	 * Contains no value atm. should be optional
	 * 
	 * @return
	 */
	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		result.put("name", name);

		if (this.datatype >= 0) {
			result.put("dataType", datatypeStrings.get(datatype));
		}

		if (this.cardinality > 0) {
			result.put("cardinality", cardinalityStrings.get(cardinality));// (default
																			// is
																			// single,
																			// don't
																			// have
																			// to
																			// specify
																			// it.
		}
		return result;
	}

	/**
	 * can't parse value atm. should be optional
	 * 
	 * @param json
	 * @return
	 */
	public static Property Instance(JSONObject json) {

		Property p = new Property(json.getString("name"), -1, null);
		if (json.has("dataType")) {
			p.setDataType(p.getDataType(json.getString("dataType")));
		}

		if (json.has("cardinality")) {
			p.setCardinality(p.getCardinality(json.getString("cardinality")));
		} else {
			p.setCardinality(CARDINALITY_SINGLE);// default from API
		}

		return p;
	}

	@Override
	public String toString() {
		String result = "[Name=" + getName() + "; Value=" + getValue().toString() + "]";
		return result;
	}

}
