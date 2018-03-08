/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.bp.elements;

import java.util.Collection;

import sernet.hui.common.connect.IIdentifiableElement;
import sernet.hui.common.connect.ITaggableElement;
import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 */
public class BpThreat extends CnATreeElement implements IBpElement, IIdentifiableElement, ITaggableElement {
    
    private static final long serialVersionUID = -7182966153863832177L;
    
    private static final String PROP_ABBR = "bp_threat_abbr"; //$NON-NLS-1$
    private static final String PROP_OBJECTBROWSER_DESC = "bp_threat_objectbrowser_content"; //$NON-NLS-1$
    private static final String PROP_NAME = "bp_threat_name"; //$NON-NLS-1$
    private static final String PROP_ID = "bp_threat_id"; //$NON-NLS-1$
    public static final String PROP_TAG = "bp_threat_tag"; //$NON-NLS-1$

    private static final String PROP_CONFIDENIALITY = "bp_threat_value_method_confidentiality"; //$NON-NLS-1$
    private static final String PROP_INTEGRITY = "bp_threat_value_method_integrity"; //$NON-NLS-1$
    private static final String PROP_AVAILABILITY = "bp_threat_value_method_availability"; //$NON-NLS-1$
    
    public static final String TYPE_ID = "bp_threat"; //$NON-NLS-1$

    public static final String REL_BP_REQUIREMENT_BP_APPLICATION = "rel_bp_threat_bp_application"; //$NON-NLS-1$
    public static final String REL_BP_REQUIREMENT_BP_BUSINESSPROCESS = "rel_bp_threat_bp_businessprocess"; //$NON-NLS-1$
    public static final String REL_BP_REQUIREMENT_BP_DEVICE = "rel_bp_threat_bp_device"; //$NON-NLS-1$
    public static final String REL_BP_REQUIREMENT_BP_ICSSYSTEM = "rel_bp_threat_bp_icssystem"; //$NON-NLS-1$
    public static final String REL_BP_REQUIREMENT_BP_ITNETWORK = "rel_bp_threat_bp_itnetwork"; //$NON-NLS-1$
    public static final String REL_BP_REQUIREMENT_BP_ITSYSTEM = "rel_bp_threat_bp_itsystem"; //$NON-NLS-1$
    public static final String REL_BP_REQUIREMENT_BP_NETWORK = "rel_bp_threat_bp_network"; //$NON-NLS-1$
    public static final String REL_BP_REQUIREMENT_BP_ROOM = "rel_bp_threat_bp_room"; //$NON-NLS-1$
    
    protected BpThreat() {}
    
    public BpThreat(CnATreeElement parent) {
        super(parent);
        init();
    }

    @Override
    public String getTypeId() {
        return TYPE_ID;
    }
    
    public String getObjectBrowserDescription() {
        return getEntity().getPropertyValue(PROP_OBJECTBROWSER_DESC);
    }
    
    public void setObjectBrowserDescription(String description) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_OBJECTBROWSER_DESC), description);
    }
    
    public String getAbbreviation() {
        return getEntity().getPropertyValue(PROP_ABBR);
    }
    
    public void setAbbreviation(String abbreviation) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ABBR), abbreviation);
    }
    
    @Override
    public String getTitle() {
        return getEntity().getPropertyValue(PROP_NAME);
    }
    
    @Override
    public void setTitel(String name) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
    }
    
    @Override
    public String getIdentifier() {
        return getEntity().getPropertyValue(PROP_ID);
    }
    
    public void setIdentifier(String id) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ID), id);
    }
    
    public boolean isConfidentiality() {
        String value = getEntity().getPropertyValue(PROP_CONFIDENIALITY);
        return Boolean.getBoolean(value);
    }
    
    public void setConfidentiality(boolean isConfidentiality) {
        int value = (isConfidentiality) ? 1 : 0;
        setNumericProperty(PROP_CONFIDENIALITY, value);
    }
    
    public boolean isIntegrity() {
        String value = getEntity().getPropertyValue(PROP_INTEGRITY);
        return Boolean.getBoolean(value);
    }
    
    public void setIntegrity(boolean isIntegrity) {
        int value = (isIntegrity) ? 1 : 0;
        setNumericProperty(PROP_INTEGRITY, value);
    }
    
    public boolean isAvailability() {
        String value = getEntity().getPropertyValue(PROP_AVAILABILITY);
        return Boolean.getBoolean(value);
    }
    
    public void setAvailibility(boolean isAvailability) {
        int value = (isAvailability) ? 1 : 0;
        setNumericProperty(PROP_AVAILABILITY, value);
    }
    
    public static String getIdentifierOfThreat(CnATreeElement requirement) {
        return requirement.getEntity().getPropertyValue(PROP_ID);
    }

    @Override
    public String getFullTitle() {
        return joinPrefixAndTitle(getIdentifier(), getTitle());
    }

    @Override
    public Collection<String> getTags() {
        return TagHelper.getTags(getEntity().getPropertyValue(PROP_TAG));
    }

}
