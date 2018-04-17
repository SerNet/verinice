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
 * A control from the ISO/IEC 27000 standard.
 * See https://en.wikipedia.org/wiki/ISO/IEC_27000-series for details
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
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
    public static final String PROP_CONTROL_EFFECT_P = "control_eff_probability";
    public static final String PROP_EUGDPR_PSEUDONYMIZATION = "control_data_protection_objectives_eugdpr_pseudonymization";
    public static final String PROP_EUGDPR_ENCRYPTION = "control_data_protection_objectives_eugdpr_encryption";
    public static final String PROP_EUGDPR_CONFIDENTIALITY = "control_data_protection_objectives_eugdpr_confidentiality";
    public static final String PROP_EUGDPR_INTEGRITY = "control_data_protection_objectives_eugdpr_integrity";
    public static final String PROP_EUGDPR_AVAILABILITY = "control_data_protection_objectives_eugdpr_availability";
    public static final String PROP_EUGDPR_RESILIENCE = "control_data_protection_objectives_eugdpr_resilience";
    public static final String PROP_EUGDPR_RECOVERABILITY = "control_data_protection_objectives_eugdpr_recoverability";
    public static final String PROP_EUGDPR_EFFECTIVENESS = "control_data_protection_objectives_eugdpr_effectiveness";

    public static final String REL_CONTROL_PERSON_ISO = "rel_control_person-iso";
    public static final String REL_CONTROL_INCSCEN = "rel_control_incscen";

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
		return getEntity().getPropertyValue(PROP_NAME);
	}

	@Override
    public void setTitel(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
	}

	@Override
    public String getAbbreviation() {
		return getEntity().getPropertyValue(PROP_ABBR);
	}

	public void setAbbreviation(String abbreviation) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ABBR), abbreviation);
	}

	@Override
    public Collection<String> getTags() {
		return TagHelper.getTags(getEntity().getPropertyValue(PROP_TAG));
	}

	@Override
    public String getDescription() {
		return getEntity().getPropertyValue(PROP_DESC);
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
        return getEntity().getPropertyValue(PROP_MATURITY_COMMENT);
    }

	public String getImplementationExplanation() {
        return getEntity().getPropertyValue(PROP_IMPL_EXPLANATION);
    }

	public int getMaturityValueByTag(){
        int maturity = -1;
        for(String tag : this.getTags()){
            if(tag.equals(IControl.TAG_MATURITY_LVL_1)){
                maturity = 1;
            } else if (tag.equals(IControl.TAG_MATURITY_LVL_2)){
                maturity = 2;
            } else if (tag.equals(IControl.TAG_MATURITY_LVL_3)){
                maturity = 3;
            }
        }
        return maturity;
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

	public void setImplementation(String state) {
	    getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_IMPL), state);
    }


	public static boolean isImplemented(Entity entity) {
	    return getImplementation(entity).equals(IMPLEMENTED_YES);
	}

	@Override
    public boolean isImplemented() {
	    return getImplementation().equals(IMPLEMENTED_YES);
	}

	 public boolean isImplementationNotEdited() {
	        return getImplementation().equals(IMPLEMENTED_NOTEDITED);
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
	    return getEntity().getPropertyValue(PROP_FEEDBACK_NOTE);
    }

    public String getGsmDescription() {
        return getEntity().getPropertyValue(PROP_GSM_ISM_CONTROL_DESCRIPTION);
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
        return getEntity().getPropertyValue(PROP_ISR_MATURITY_QUANTITY);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IISRControl#getISRPropertyId()
     */
    @Override
    public String getISRPropertyId() {
        return PROP_ISR_MATURITY;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IControl#setVersion(java.lang.String)
     */
    @Override
    public void setVersion(String version) {
        // at the moment there it is not necessary to save the version Controls
        // see VN-1007 for details
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IControl#getVersion()
     */
    @Override
    public String getVersion() {
        // at the moment there it is not necessary to save the version in Controls
        // see VN-1007 for details
        return null;
    }

    /**
     * Returns true if control implementation status is everything but not
     * {@link IControl#IMPLEMENTED_NA}.
     */
    public static boolean isPlanned(Entity entity) {
       return !getImplementation(entity).equals(IMPLEMENTED_NA);
    }

}
