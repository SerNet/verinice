/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.samt;

import java.util.Collection;
import java.util.Date;

import sernet.hui.common.connect.Entity;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IControl;
import sernet.verinice.model.iso27k.IISO27kElement;

/**
 * @author Daniel Murygin <dm@sernet.de>
 *
 */
public class SamtTopic extends CnATreeElement implements IISO27kElement, IControl {
    
    public static final String TYPE_ID = "samt_topic"; //$NON-NLS-1$
    public static final String PROP_ABBR = "samt_topic_abbr"; //$NON-NLS-1$
    public static final String PROP_NAME = "samt_topic_name"; //$NON-NLS-1$
    public static final String PROP_TAG = "samt_topic_tag"; //$NON-NLS-1$
    
    // properties to implement IControl and execute risk analysis
    public static final String PROP_DESC = "samt_topic_desc"; //$NON-NLS-1$
    public static final String PROP_MATURITY = "samt_topic_maturity"; //$NON-NLS-1$
    public static final String PROP_WEIGHT1 = "samt_topic_weight"; //$NON-NLS-1$
    public static final String PROP_WEIGHT2 = "samt_topic_ownweight"; //$NON-NLS-1$
    public static final String PROP_THRESHOLD1 = "samt_topic_min1"; //$NON-NLS-1$
    public static final String PROP_THRESHOLD2 = "samt_topic_min2"; //$NON-NLS-1$
    public static final String PROP_IMPLEMENTED = "samt_topic_implemented"; //$NON-NLS-1$
    public static final String PROP_COMPLETE_UNTIL = "samt_topic_audit_compluntil"; //$NON-NLS-1$
    
    public static final String REL_SAMTTOPIC_PERSON_ISO = "rel_samttopic_person-iso_resp"; //$NON-NLS-1$
    
    
    
    
    public SamtTopic() {
        super();
        setEntity(new Entity(TYPE_ID));
        getEntity().initDefaultValues(getTypeFactory());
    }
    
    public SamtTopic(CnATreeElement parent) {
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
    
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#setTitel(java.lang.String)
     */
    public void setTitel(String name) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.model.IISO27kElement#getAbbreviation()
     */
    public String getAbbreviation() {
        return getEntity().getSimpleValue(PROP_ABBR);
    }
    
    public void setAbbreviation(String abbreviation) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ABBR), abbreviation);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.model.IISO27kElement#getTags()
     */
    public Collection<? extends String> getTags() {
        return TagHelper.getTags(getEntity().getSimpleValue(PROP_TAG));
    }

    public String getDescription() {
        return getEntity().getSimpleValue(SamtTopic.PROP_DESC);
    }
    
    public void setDescription(String description) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(SamtTopic.PROP_DESC), description);
    }
    
    public void setMaturity(String value) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(SamtTopic.PROP_MATURITY), value);
    }
    
    public int getMaturity() {
        return getEntity().getInt(SamtTopic.PROP_MATURITY);
    }
    
    public int getThreshold1() {
        return getEntity().getInt(SamtTopic.PROP_THRESHOLD1);
    }
    
    public void setThreshold1(String value) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(SamtTopic.PROP_THRESHOLD1), value);
        
    }
    
    public int getThreshold2() {
        return getEntity().getInt(SamtTopic.PROP_THRESHOLD2);
    }

    public void setThreshold2(String value) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(SamtTopic.PROP_THRESHOLD2), value);
        
    }
    
    /**
     * Returns the used weight.
     * @return
     */
    public int getWeight1() {
        return getEntity().getInt(SamtTopic.PROP_WEIGHT1);
    }
    
    /**
     * Sets the suggested weight for maturity calculation.
     * @param value
     */
    public void setWeight1(String value) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(SamtTopic.PROP_WEIGHT1), value);
        
    }
    
    /**
     * Returns the used weight.
     * @return
     */
    public int getWeight2() {
        return getEntity().getInt(SamtTopic.PROP_WEIGHT2);
    }
    
    /**
     * Sets the actually used weight for maturity calculation.
     * @param value
     */
    public void setWeight2(String value) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(SamtTopic.PROP_WEIGHT2), value);
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.model.IControl#getMaturityPropertyId()
     */
    @Override
    public String getMaturityPropertyId() {
        return PROP_MATURITY;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IControl#isImplemented()
     */
    @Override
    public boolean isImplemented() {
        // never implemented, use maturity service
        throw new RuntimeException("Use maturity service to determine implementation.");
    }
    
    public Date getCompleteUntil() {
        return getEntity().getDate(PROP_COMPLETE_UNTIL);
    }

}
