package sernet.hui.common.rules;

public class SimpleValue implements IFillRule {

	private String value;

	public String getValue() {
		return value;
	}

	public void init(String[] params) {
		value = params != null ? params[0] : "";
	}

}
