/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.iso27k;

import java.util.Collection;
import java.util.Date;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
@SuppressWarnings("serial")
public class Control extends CnATreeElement implements IISO27kElement, IControl, IISRControl {

	public static final String TYPE_ID = "control"; //$NON-NLS-1$
	public static final String PROP_ABBR = "control_abbr"; //$NON-NLS-1$
	public static final String PROP_NAME = "control_name"; //$NON-NLS-1$
	public static final String PROP_TAG = "control_tag"; //$NON-NLS-1$
	public static final String PROP_DESC = "control_desc"; //$NON-NLS-1$
	
	// control implementation state:
	// see IControl.java
	
	// this is another way to measure control implementation:
	public static final String PROP_MATURITY = "control_maturity"; //$NON-NLS-1$
	public static final String PROP_WEIGHT1 = "control_weight"; //$NON-NLS-1$
	public static final String PROP_WEIGHT2 = "control_ownweight"; //$NON-NLS-1$
	public static final String PROP_THRESHOLD1 = "control_min1"; //$NON-NLS-1$
	public static final String PROP_THRESHOLD2 = "control_min2"; //$NON-NLS-1$
	public static final String PROP_MATURITY_DUEDATE = "control_maturity_duedate"; //$NON-NLS-1$
	public static final String PROP_MATURITY_COMMENT = "control_maturity_comment"; //$NON-NLS-1$
	public static final String PROP_CONTROL_IMPL_DATE = "control_implby"; //$NON-NLS-1$
	public static final String PROP_IMPL_EXPLANATION = "control_implemented_explanation"; //$NON-NLS-1$
    public static final String PROP_FEEDBACK_NOTE = "control_feedback_note"; //$NON-NLS-1$

	// ISR properties:
	public static final String PROP_ISR_MATURITY = "control_isr_maturity"; //$NON-NLS-1$
	public static final String PROP_ISR_MATURITY_QUANTITY = "control_isr_quantity_of_maturity"; //$NON-NLS-1$
	
	public static final String PROP_EFFECTIVENESS_CONFIDENTIALITY ="control_effectiveness_confidentiality"; 
    public static final String PROP_EFFECTIVENESS_INTEGRITY="control_effectiveness_integrity" ;
    public static final String PROP_EFFECTIVENESS_AVAILABILITY="control_effectiveness_availability"; 
    public static final String PROP_EFFECTIVENESS_PROBABILITY="control_eff_probability";
    public static final String PROP_GSM_ISM_CONTROL_DESCRIPTION = "gsm_ism_control_description";
    public static final String REL_CONTROL_PERSON_ISO = "rel_control_person-iso"; 
    public static final String REL_CONTROL_INCSCEN = "rel_control_incscen"; 
	
   
	/**
	 * Creates an empty asset
	 */
	public Control() {
		super();
		setEntity(new Entity(TYPE_ID));
		getEntity().initDefaultValues(getTypeFactory());
	}
	
	public Control(CnATreeElement parent) {
		super(parent);
		setEntity(new Entity(TYPE_ID));
		getEntity().initDefaultValues(getTypeFactory());
        // sets the localized title via HUITypeFactory from message bundle
        setTitel(getTypeFactory().getMessage(TYPE_ID));
    }
	
   
	
   
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTypeId()
	 */
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTitel()
	 */
	@Override
	public String getTitle() {
		return getEntity().getSimpleValue(PROP_NAME);
	}
	
	@Override
    public void setTitel(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
	}
	
	@Override
    public String getAbbreviation() {
		return getEntity().getSimpleValue(PROP_ABBR);
	}
	
	public void setAbbreviation(String abbreviation) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ABBR), abbreviation);
	}
	
	@Override
    public Collection<? extends String> getTags() {
		return TagHelper.getTags(getEntity().getSimpleValue(PROP_TAG));
	}
	
	@Override
    public String getDescription() {
		return getEntity().getSimpleValue(PROP_DESC);
	}
	
	@Override
    public void setDescription(String description) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_DESC), description);
	}

	
	@Override
    public void setMaturity(String value) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_MATURITY), value);
	}
	
	@Override
    public int getMaturity() {
	    return getEntity().getInt(PROP_MATURITY);
	}
	
	public Date getDueDate() {
        return getEntity().getDate(PROP_CONTROL_IMPL_DATE);
    }
	
	public String getMaturityComment() {
        return getEntity().getSimpleValue(PROP_MATURITY_COMMENT);
    }
	
	public String getImplementationExplanation() {
        return getEntity().getSimpleValue(PROP_IMPL_EXPLANATION);
    }
	
	public static String getImplementation(Entity entity) {
	    PropertyList properties = entity.getProperties(PROP_IMPL);
	    if (properties == null || properties.getProperties() == null
	            || properties.getProperties().size() < 1){
	        return IMPLEMENTED_NOTEDITED;
	    }
	    Property property = properties.getProperty(0);
	    if (property != null && property.getPropertyValue()!=null && !property.getPropertyValue().equals("")){ //$NON-NLS-1$
	        return property.getPropertyValue();
	    }
	    return IMPLEMENTED_NOTEDITED;
	}
	
	public String getImplementation() {
	    return getImplementation(getEntity());
    }
	
	
	public static boolean isImplemented(Entity entity) {
	    return getImplementation(entity).equals(IMPLEMENTED_YES);
	}
	
	@Override
    public boolean isImplemented() {
	    return getImplementation().equals(IMPLEMENTED_YES);
	}

	/**
	 * Returns the used weight.
	 * @return
	 */
	@Override
    public int getWeight2() {
	    return getEntity().getInt(PROP_WEIGHT2);
	}
	
	@Override
    public int getThreshold1() {
	    return getEntity().getInt(PROP_THRESHOLD1);
    }

	@Override
    public int getThreshold2() {
	    return getEntity().getInt(PROP_THRESHOLD2);
	}

	
    /**
     * Returns the used weight.
     * @return
     */
    @Override
    public int getWeight1() {
        return getEntity().getInt(PROP_WEIGHT1);
    }
	
	/**
	 * Sets the suggested weight for maturity calculation.
	 * @param value
	 */
	@Override
    public void setWeight1(String value) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_WEIGHT1), value);
		
	}
	
	/**
	 * Sets the actually used weight for maturity calculation.
	 * @param value
	 */
	@Override
    public void setWeight2(String value) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_WEIGHT2), value);
		
	}

	@Override
    public void setThreshold1(String value) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_THRESHOLD1), value);
		
	}

	@Override
    public void setThreshold2(String value) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_THRESHOLD2), value);
		
	}
	
	public String getFeedbackNote() {
	    return getEntity().getSimpleValue(PROP_FEEDBACK_NOTE);
    }
	
    public String getGsmDescription() {
        return getEntity().getSimpleValue(PROP_GSM_ISM_CONTROL_DESCRIPTION);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.model.IControl#getMaturityPropertyId()
     */
    @Override
    public String getMaturityPropertyId() {
        return PROP_MATURITY;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IISRControl#getISRMaturity()
     */
    @Override
    public int getISRMaturity() {
        return getEntity().getInt(PROP_ISR_MATURITY);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IISRControl#getISRMaturityQuantity()
     */
    @Override
    public String getISRMaturityQuantity() {
        return getEntity().getSimpleValue(PROP_ISR_MATURITY_QUANTITY);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IISRControl#getISRPropertyId()
     */
    @Override
    public String getISRPropertyId() {
        return PROP_ISR_MATURITY;
    }

}
