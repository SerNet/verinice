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
    public static final String PROP_WEIGHT = "samt_topic_weight"; //$NON-NLS-1$
    public static final String PROP_OWNWEIGHT = "samt_topic_ownweight"; //$NON-NLS-1$
    public static final String PROP_MIN1 = "samt_topic_min1"; //$NON-NLS-1$
    public static final String PROP_MIN2 = "samt_topic_min2"; //$NON-NLS-1$
    public static final String PROP_IMPLEMENTED = "samt_topic_implemented"; //$NON-NLS-1$
    public static final String PROP_COMPLETE_UNTIL = "samt_topic_audit_compluntil"; //$NON-NLS-1$
    public static final String PROP_AUDIT_FINDINGS = "samt_topic_audit_findings"; //$NON-NLS-1$
    public static final String PROP_EXTERNALNOTE = "samt_topic_externalnote"; //$NON-NLS-1$
    public static final String PROP_INTERNALNOTE = "samt_topic_internalnote"; //$NON-NLS-1$
    public static final String PROP_VERSION = "samt_topic_version"; //$NON-NLS-1$
    
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
    
    /*
     * @see sernet.hui.common.connect.ITaggableElement#getTags()
     */
    public Collection<String> getTags() {
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
    
    /**
     * This method is using the same name as the related property in
     * SNCA.xml. 
     * 
     * @return The values of property SamtTopic.PROP_MIN1
     */
    public int getMin1() {
        return getEntity().getInt(SamtTopic.PROP_MIN1);
    }
    /* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IControl#getThreshold1()
     */
    public int getThreshold1() {
        return getMin1();
    }
    
    /**
     * Sets the values of property SamtTopic.PROP_MIN1
     * 
     * This method is using the same name as the related property in
     * SNCA.xml.
     */
    public void setMin1(String value) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(SamtTopic.PROP_MIN1), value);
        
    }
    /* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IControl#setThreshold1(java.lang.String)
     */
    public void setThreshold1(String threshold1) {
        setMin1(threshold1);
    }
    
    /**
     * This method is using the same name as the related property in
     * SNCA.xml.
     *  
     * @return The values of property SamtTopic.PROP_MIN2
     */
    public int getMin2() {
        return getEntity().getInt(SamtTopic.PROP_MIN2);
    }
    /* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IControl#getThreshold2()
     */
    public int getThreshold2() {
        return getMin2();
    }

    /**
     * Sets the values of property SamtTopic.PROP_MIN2
     * 
     * This method is using the same name as the related property in
     * SNCA.xml.
     */
    public void setMin2(String value) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(SamtTopic.PROP_MIN2), value);
        
    }
    /* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IControl#setThreshold2(java.lang.String)
     */
    public void setThreshold2(String threshold2) {
        setMin2(threshold2);
    }
    
    public String getAuditFindings() {
        return getEntity().getSimpleValue(SamtTopic.PROP_AUDIT_FINDINGS);
    }
    
    public String getExternalNode() {
        return getEntity().getSimpleValue(SamtTopic.PROP_EXTERNALNOTE);
    }
       
    public String getInternalNote() {
        return getEntity().getSimpleValue(SamtTopic.PROP_INTERNALNOTE);
    }
    
    /**
     * This method is using the same name as the related property in
     * SNCA.xml.
     * 
     * @return Value of property SamtTopic.PROP_WEIGHT
     */
    public int getWeight() {
        return getEntity().getInt(SamtTopic.PROP_WEIGHT);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IControl#getWeight1()
     */
    public int getWeight1() {
        return getWeight();
    }
    
    
    /**
     * Sets the suggested weight for maturity calculation.
     * 
     * This method is using the same name as the related property in
     * SNCA.xml. 
     * 
     * @param weight
     */
    public void setWeight(String weight) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(SamtTopic.PROP_WEIGHT), weight);
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IControl#setWeight1(java.lang.String)
     */
    public void setWeight1(String value) {
        setWeight(value);
    }
    
    /**
     * This method is using the same name as the related property in
     * SNCA.xml. 
     * 
     * @return The used weight.
     */
    public int getOwnweight() {
        return getEntity().getInt(SamtTopic.PROP_OWNWEIGHT);
    }  
    
    /* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IControl#getWeight2()
     */
    public int getWeight2() {
        return getOwnweight();
    }
    
    /**
     * Sets the actually used weight for maturity calculation.
     * 
     * This method is using the same name as the related property in
     * SNCA.xml.
     * 
     * @param The actually used weight
     */
    public void setOwnweight(String value) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(SamtTopic.PROP_OWNWEIGHT), value);
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IControl#setWeight2(java.lang.String)
     */
    public void setWeight2(String value) {
        setOwnweight(value);
    }
    
    /**
     * Return the version of this topic. Can be null or empty.
     * If no version is set version is 1.x
     */ 
    public String getVersion() {
        return getEntity().getSimpleValue(SamtTopic.PROP_VERSION);
    }
    
    /**
     * Sets the version of this topic. If no version
     * is set version is 1.x
     * 
     * @param value The version of this topic
     */
    public void setVersion(String value) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(SamtTopic.PROP_VERSION), value);
        
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
