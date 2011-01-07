/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.hui.common.connect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.multiselectionlist.IMLPropertyType;
import sernet.hui.common.rules.IFillRule;
import sernet.hui.common.rules.IValidationRule;
import sernet.hui.common.rules.NullRule;

/**
 * @author prack
 * @version $Id: PropertyType.java,v 1.6 2006/10/20 14:55:16 aprack Exp $
 */
public class PropertyType implements IMLPropertyType, IEntityElement, Comparable<PropertyType> {

	private final Logger log = Logger.getLogger(PropertyType.class);

	public int getMinValue() {
		return minValue;
	}

	public int getMaxValue() {
		return maxValue;
	}

	private String id;

	private String name;

	public boolean isCrudButtons() {
		return crudButtons;
	}

	private HashSet dependencies = new HashSet();

	private String inputName;
	
	private byte inputtype = INPUT_LINE;

	public static final byte INPUT_LINE = 0;

	public static final byte INPUT_TEXT = 1;

	public static final byte INPUT_SINGLEOPTION = 2;

	public static final byte INPUT_MULTIOPTION = 3;

	//public static final byte INPUT_CHECKBOX = 4; no longer used, see type 8

	public static final byte INPUT_REFERENCE = 5;

	public static final byte INPUT_DATE = 6;

	private static final byte INPUT_NUMERICOPTION = 7;

	private static final byte INPUT_BOOLEANOPTION = 8;

	private List<IValidationRule> validators = new ArrayList<IValidationRule>();

	private IFillRule defaultRule = new NullRule();

	private boolean required = false;

	private ArrayList options = new ArrayList();

	private String tooltiptext = "";

	private boolean initialfocus;
	private boolean editable;

	private boolean visible;

	private boolean isURL;

	private String referencedEntityTypeId;

	private IReferenceResolver referenceResolver;

	private IUrlResolver urlResolver;

	private boolean crudButtons;

	private int minValue = 0;

	private int maxValue = 0;

    private boolean reportable;

    private int numericDefault;

    private String tags;

    private int textrows;

	/**
     * @param numericDefault the numericDefault to set
     */
    public void setNumericDefault(String numericDefault) {
        try {
            this.numericDefault = Integer.parseInt(numericDefault);
        } catch (NumberFormatException e) {
            log.warn("Not a valid number: " + numericDefault);
        }
    }

    /**
     * @return the reportable
     */
    public boolean isReportable() {
        return reportable;
    }

    public void addValidator(IValidationRule rule) {
		if (!validators.contains(rule)) {
			validators.add(rule);
		}
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
	public ArrayList<IMLPropertyOption> getOptions() {
		return options;
	}

	public PropertyOption getOption(String id) {
		for (Iterator iter = options.iterator(); iter.hasNext();) {
			PropertyOption option = (PropertyOption) iter.next();
			if (option.getId().equals(id)) {
				return option;
			}
		}
		return null;
	}
	
	public PropertyOption getOption(Integer id) {
        for (Iterator iter = options.iterator(); iter.hasNext();) {
            PropertyOption option = (PropertyOption) iter.next();
            if (option.getValue().equals(id)) {
                return option;
            }
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
		if (dependencies.size() < 1) {
			return true;
		}

		// if deps defined, at least one of them must be there:
		for (Iterator iter = dependencies.iterator(); iter.hasNext();) {
			String dep = (String) iter.next();
			if (entity.isSelected(dep)) {
				return true;
			}
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
	 * Set the option that this property depends on. It is only valid when one
	 * of the options given as dependencies is selected in another property.
	 * 
	 * @param set
	 */
	public void setDependencies(HashSet set) {
		this.dependencies = set;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * sernet.snkdb.guiswt.multiselectionlist.MLPropertyType#isMultiselect()
	 */
	public boolean isMultiselect() {
		return inputtype == INPUT_MULTIOPTION;
	}

	public boolean isSingleSelect() {
		return inputtype == INPUT_SINGLEOPTION;
	}

	public boolean isNumericSelect() {
		return inputtype == INPUT_NUMERICOPTION;
	}
	
	public boolean isBooleanSelect() {
	    return inputtype == INPUT_BOOLEANOPTION;
	}

	public boolean isEnum() {
		return inputtype == INPUT_SINGLEOPTION || inputtype == INPUT_MULTIOPTION;
	}

	public boolean isLine() {
		return inputtype == INPUT_LINE;
	}

	public boolean isReference() {
		return inputtype == INPUT_REFERENCE;
	}

	public boolean isText() {
		return inputtype == INPUT_TEXT;
	}

	public boolean isDate() {
		return inputtype == INPUT_DATE;
	}

	/**
	 * @param attribute
	 */
	public void setInputType(String attribute) {
	    inputName = attribute;
		if (attribute.equals("line")) {
			inputtype = INPUT_LINE;
		} else if (attribute.equals("singleoption")) {
			inputtype = INPUT_SINGLEOPTION;
		} else if (attribute.equals("multioption")) {
			inputtype = INPUT_MULTIOPTION;
		} else if (attribute.equals("text")) {
			inputtype = INPUT_TEXT;
		} else if (attribute.equals("reference")) {
			inputtype = INPUT_REFERENCE;
		} else if (attribute.equals("date")) {
			inputtype = INPUT_DATE;
		} else if (attribute.equals("numericoption")) {
			inputtype = INPUT_NUMERICOPTION;
		} else if (attribute.equals("booleanoption")) {
            inputtype = INPUT_BOOLEANOPTION;
        }
	}
	
	public String getInputName() {
	    return inputName;
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
			if (!validator.validate(text, params)) {
				return false;
			}
		}
		return true;
	}

	public void setVisible(boolean b) {
		this.visible = b;
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

	public void setReferencedEntityType(String attribute) {
		this.referencedEntityTypeId = attribute;
	}

	public String getReferencedEntityTypeId() {
		return referencedEntityTypeId;
	}

	public List<IMLPropertyOption> getReferencedEntities() {
		if (referenceResolver != null) {
			return referenceResolver.getAllEntitesForType(referencedEntityTypeId);
		}
		return new ArrayList<IMLPropertyOption>();
	}

	public IReferenceResolver getReferenceResolver() {
		return referenceResolver;
	}

	public void setReferenceResolver(IReferenceResolver referenceResolver) {
		this.referenceResolver = referenceResolver;
	}

	public List<HuiUrl> getResolvedUrls() {
		if (this.urlResolver != null) {
			return urlResolver.resolve();
		}
		return new ArrayList<HuiUrl>();
	}

	public IUrlResolver getUrlResolver() {
		return urlResolver;
	}

	public void setUrlResolver(IUrlResolver urlResolver) {
		this.urlResolver = urlResolver;
	}

	/**
	 * @param b
	 */
	public void setCrudButtons(boolean b) {
		this.crudButtons = b;
	}

	/**
	 * @param properties
	 * @return
	 */
	public List<IMLPropertyOption> getReferencedEntities(List<Property> references) {
		if (referenceResolver != null) {
			return referenceResolver.getReferencedEntitesForType(referencedEntityTypeId, references);
		}
		return new ArrayList<IMLPropertyOption>();
	}

	/**
	 * @param attribute
	 */
	public void setNumericMin(String minString) {
		try {
			this.minValue = Integer.parseInt(minString);
		} catch (NumberFormatException e) {
			log.warn("Not a valid number: " + minString);
		}
	}

	public void setNumericMax(String maxString) {
		try {
			this.maxValue = Integer.parseInt(maxString);
		} catch (NumberFormatException e) {
			log.warn("Not a valid number: " + maxString);
		}
	}
	 /**
     * Get a name to show instead of a numeric value if one is defined.
     * 
     * @param i
     * @return
     */
    public String getNameForValue(int i) {
        for (Iterator iterator = options.iterator(); iterator.hasNext();) {
            PropertyOption option = (PropertyOption) iterator.next();
            if (option.getValue() != null && option.getValue() == i) {
                return option.getName();
            }
        }
        return Integer.toString(i);
    }

    /**
     * @param equals
     */
    public void setReportable(boolean value) {
        this.reportable = value;
    }

    /**
     * @return
     */
    public int getNumericDefault() {
        return this.numericDefault;
    }

    /**
     * @param attribute
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * @return the tags
     */
    public String getTags() {
        return tags;
    }

    /**
     * @param attribute
     */
    public void setTextRows(String rows) {
        this.textrows = Integer.parseInt(rows);
    }

    /**
     * @return the textrows
     */
    public int getTextrows() {
        return textrows;
    }

	/**
	 * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(PropertyType o) {
		final int LESS = -1;
		final int EQUAL = 0;
		final int GREATER = 1;
		int result = LESS;
		if(o!=null) {
			if(getName()==null) {
				result = (o.getName()==null) ? EQUAL : GREATER;
			} else if(o.getName()!=null) {
				result = this.getName().compareTo(o.getName());
			}
		}
		return result;
	}   

}
