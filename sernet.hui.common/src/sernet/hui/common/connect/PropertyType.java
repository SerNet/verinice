
package sernet.hui.common.connect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import sernet.hui.common.multiselectionlist.IMLPropertyType;
import sernet.hui.common.rules.IFillRule;
import sernet.hui.common.rules.IValidationRule;
import sernet.hui.common.rules.NullRule;

/**
 * @author prack
 * @version $Id: PropertyType.java,v 1.6 2006/10/20 14:55:16 aprack Exp $
 */
public class PropertyType implements IMLPropertyType, IEntityElement {
	private String id;

	private String name;

	private HashSet dependencies = new HashSet();

	private byte inputtype = INPUT_LINE;

	public static final byte INPUT_LINE = 0;

	public static final byte INPUT_TEXT = 1;

	public static final byte INPUT_SINGLEOPTION = 2;

	public static final byte INPUT_MULTIOPTION = 3;

	public static final byte INPUT_CHECKBOX = 4;

	public static final byte INPUT_REFERENCE = 5;

	public static final byte INPUT_DATE = 6;

	private List<IValidationRule> validators = new ArrayList<IValidationRule>();
	
	private IFillRule defaultRule = new NullRule();

	private boolean required = false;

	private ArrayList options = new ArrayList();

	private String tooltiptext = "";

	private boolean initialfocus;
	private boolean editable;

	private boolean visible;

	private boolean isURL;

	public void addValidator(IValidationRule rule) {
		if (!validators.contains(rule))
			validators.add(rule);
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public IFillRule getDefaultRule() {
		return defaultRule;
	}

	public void setDefaultRule(IFillRule defaultRule) {
		this.defaultRule = defaultRule;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns possible <code>DocValue</code> objects for this property.
	 * 
	 * @return Returns the predefinedValues.
	 */
	public ArrayList getOptions() {
		return options;
	}

	public PropertyOption getOption(String id) {
		for (Iterator iter = options.iterator(); iter.hasNext();) {
			PropertyOption option = (PropertyOption) iter.next();
			if (option.getId().equals(id))
				return option;
		}
		return null;
	}

	/**
	 * List of <code>DocValue</code> objects.
	 * 
	 * @param predefinedValues
	 *            The predefinedValues to set.
	 */
	public void setPredefinedValues(ArrayList predefinedValues) {
		this.options = predefinedValues;
	}

	/**
	 * @return Returns the required.
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * @param required
	 *            The required to set.
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getTooltiptext() {
		return tooltiptext;
	}

	public void setTooltiptext(String tooltiptext) {
		this.tooltiptext = tooltiptext;
	}

	/**
	 * Check if necessary dependencies are fulfilled to display this type List
	 * of dependencies is OR, one of them is sufficient to display the property
	 * type.
	 * 
	 * @return
	 */
	public boolean dependenciesFulfilled(Entity entity) {
		// no deps defined:
		if (dependencies.size() < 1)
			return true;

		// if deps defined, at least one of them must be there:
		for (Iterator iter = dependencies.iterator(); iter.hasNext();) {
			String dep = (String) iter.next();
			if (entity.isSelected(dep))
				return true;
		}
		return false;
	}

	/**
	 * Checks if this property depends on the given option.
	 * 
	 * @param optionId
	 * @return
	 */
	public boolean isDependency(String optionId) {
		return dependencies.contains(optionId);
	}

	/**
	 * Set the option that this property depends on.
	 * It is only valid when one of the options given as
	 * dependencies is selected in another property.
	 * @param set
	 */
	public void setDependencies(HashSet set) {
		this.dependencies = set;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sernet.snkdb.guiswt.multiselectionlist.MLPropertyType#isMultiselect()
	 */
	public boolean isMultiselect() {
		return inputtype == INPUT_MULTIOPTION;
	}
	
	public boolean isSingleSelect() {
		return inputtype == INPUT_SINGLEOPTION;
	}
	
	public boolean isBool() {
		return inputtype == INPUT_CHECKBOX;
	}
	
	public boolean isEnum() {
		return inputtype == INPUT_SINGLEOPTION 
			|| inputtype == INPUT_MULTIOPTION;
	}
	
	public boolean isLine() {
		return inputtype == INPUT_LINE;
	}
	
	public boolean isText( ) {
		return inputtype == INPUT_TEXT;
	}
	
	public boolean isDate() {
		return inputtype == INPUT_DATE;
	}

	

	/**
	 * @param attribute
	 */
	public void setInputType(String attribute) {
		if (attribute.equals("line"))
			inputtype = INPUT_LINE;
		else if (attribute.equals("singleoption"))
			inputtype = INPUT_SINGLEOPTION;
		else if (attribute.equals("multioption"))
			inputtype = INPUT_MULTIOPTION;
		else if (attribute.equals("text"))
			inputtype = INPUT_TEXT;
		else if (attribute.equals("reference"))
			inputtype = INPUT_REFERENCE;
		else if (attribute.equals("date"))
			inputtype = INPUT_DATE;
		
	}

	public void setInitialFocus(boolean b) {
		this.initialfocus = b;
	}
	
	public boolean isFocus() {
		return this.initialfocus;
	}

	public List<IValidationRule> getValidators() {
		return validators;
	}

	public void setValidators(List<IValidationRule> validators) {
		this.validators = validators;
	}

	public boolean validate(String text, String[] params) {
		for (Iterator iter = validators.iterator(); iter.hasNext();) {
			IValidationRule validator = (IValidationRule) iter.next();
			if (!validator.validate(text, params))
				return false;
		}
		return true;
	}

	public void setVisible(boolean b) {
		this.visible=b;
	}
	
	public boolean isVisible() {
		return visible;
	}

	public void setURL(boolean url) {
		this.isURL = url;
	}

	public boolean isURL() {
		return isURL;
	}
}
