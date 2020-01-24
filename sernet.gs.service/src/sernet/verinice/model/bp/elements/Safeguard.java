/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin.
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
 *     Daniel Murygin dm[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.bp.elements;

import java.util.Collection;
import java.util.Date;

import sernet.hui.common.connect.IIdentifiableElement;
import sernet.hui.common.connect.ITaggableElement;
import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.bp.IImplementableSecurityLevelProvider;
import sernet.verinice.model.bp.ImplementationStatus;
import sernet.verinice.model.bp.SecurityLevel;
import sernet.verinice.model.bp.SecurityLevelUtil;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin dm[at]sernet.de
 *
 */
public class Safeguard extends CnATreeElement implements IBpElement, IIdentifiableElement,
        ITaggableElement, IImplementableSecurityLevelProvider {

    private static final long serialVersionUID = -3597661958061483411L;

    public static final String TYPE_ID = "bp_safeguard"; //$NON-NLS-1$
    public static final String PROP_OBJECTBROWSER_DESC = "bp_safeguard_objectbrowser_content"; //$NON-NLS-1$
    private static final String PROP_NAME = "bp_safeguard_name"; //$NON-NLS-1$
    private static final String PROP_ID = "bp_safeguard_id"; //$NON-NLS-1$
    public static final String PROP_TAG = "bp_safeguard_tag"; //$NON-NLS-1$
    private static final String PROP_LAST_CHANGE = "bp_safeguard_last_change"; //$NON-NLS-1$
    public static final String PROP_CONFIDENTIALITY = "bp_safeguard_value_method_confidentiality";//$NON-NLS-1$
    public static final String PROP_INTEGRITY = "bp_safeguard_value_method_integrity";//$NON-NLS-1$
    public static final String PROP_AVAILABILITY = "bp_safeguard_value_method_availability";//$NON-NLS-1$
    public static final String PROP_QUALIFIER = "bp_safeguard_qualifier"; //$NON-NLS-1$
    // These keys shall not be used for localization but only to identify which
    // ENUM value shall be used. Use the ENUMs getLabel() instead.
    private static final String PROP_QUALIFIER_BASIC = "bp_safeguard_qualifier_basic";//$NON-NLS-1$
    private static final String PROP_QUALIFIER_STANDARD = "bp_safeguard_qualifier_standard";//$NON-NLS-1$
    private static final String PROP_QUALIFIER_HIGH = "bp_safeguard_qualifier_high";//$NON-NLS-1$
    public static final String PROP_IMPLEMENTATION_STATUS = "bp_safeguard_implementation_status"; //$NON-NLS-1$
    public static final String PROP_IMPLEMENTATION_STATUS_NO = "bp_safeguard_implementation_status_no"; //$NON-NLS-1$
    public static final String PROP_IMPLEMENTATION_STATUS_YES = "bp_safeguard_implementation_status_yes"; //$NON-NLS-1$
    public static final String PROP_IMPLEMENTATION_STATUS_PARTIALLY = "bp_safeguard_implementation_status_partially"; //$NON-NLS-1$
    public static final String PROP_IMPLEMENTATION_STATUS_NOT_APPLICABLE = "bp_safeguard_implementation_status_na"; //$NON-NLS-1$
    public static final String PROP_RELEASE = "bp_safeguard_release"; //$NON-NLS-1$

    protected Safeguard() {
    }

    public Safeguard(CnATreeElement parent) {
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
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_OBJECTBROWSER_DESC),
                description);
    }

    @Override
    public String getTitle() {
        return getEntity().getPropertyValue(PROP_NAME);
    }

    @Override
    public void setTitel(String name) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
    }

    public void setTitle(String title) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), title);
    }

    /**
     * @return The Security level level represented by property PROP_QUALIFIER
     */
    @Override
    public SecurityLevel getSecurityLevel() {
        // Parsing the string as SecurityLevel should actually be done
        // in Proceeding. But every class has different
        // localization keys. If unique keys, e.g. "QUALIFIER_BASIC"
        // would be used everywhere this code can and should be moved to
        // SecurityLevel.ofLocalizationKey.
        String qualifier = getEntity().getRawPropertyValue(PROP_QUALIFIER);
        if (qualifier == null) {
            return null;
        }
        switch (qualifier) {
        case PROP_QUALIFIER_BASIC:
            return SecurityLevel.BASIC;
        case PROP_QUALIFIER_STANDARD:
            return SecurityLevel.STANDARD;
        case PROP_QUALIFIER_HIGH:
            return SecurityLevel.HIGH;
        case "":
            return null;
        default:
            throw new IllegalStateException("Unknown security level '" + qualifier + "'");
        }
    }

    /**
     * Stores the appropriate property value id to PROP_QUALIFIER.
     */
    public void setSecurityLevel(SecurityLevel level) {
        String qualifier = null;
        switch (level) {
        case BASIC:
            qualifier = PROP_QUALIFIER_BASIC;
            break;
        case STANDARD:
            qualifier = PROP_QUALIFIER_STANDARD;
            break;
        case HIGH:
            qualifier = PROP_QUALIFIER_HIGH;
            break;
        }
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_QUALIFIER), qualifier);
    }

    @Override
    public String getIdentifier() {
        return getEntity().getPropertyValue(PROP_ID);
    }

    public void setIdentifier(String id) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ID), id);
    }

    public Date getLastChange() {
        return getEntity().getDate(PROP_LAST_CHANGE);
    }

    public void setLastChange(Date date) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_LAST_CHANGE),
                String.valueOf(date.getTime()));
    }

    public void setIsAffectsConfidentiality(boolean affectsConfidentiality) {
        this.setNumericProperty(PROP_CONFIDENTIALITY, (affectsConfidentiality) ? 1 : 0);
    }

    public boolean isAffectsConfidentiality() {
        return ((this.getNumericProperty(PROP_CONFIDENTIALITY) == 1) ? true : false);
    }

    public void setIsAffectsIntegrity(boolean affectsIntegrity) {
        this.setNumericProperty(PROP_INTEGRITY, (affectsIntegrity) ? 1 : 0);
    }

    public boolean isAffectsIntegrity() {
        return ((this.getNumericProperty(PROP_INTEGRITY) == 1) ? true : false);
    }

    public void setIsAffectsAvailability(boolean affectsAvailability) {
        this.setNumericProperty(PROP_AVAILABILITY, (affectsAvailability) ? 1 : 0);
    }

    public boolean isAffectsAvailability() {
        return ((this.getNumericProperty(PROP_AVAILABILITY) == 1) ? true : false);
    }

    public ImplementationStatus getImplementationStatus() {
        String rawValue = getEntity().getRawPropertyValue(PROP_IMPLEMENTATION_STATUS);
        return getImplementationStatus(rawValue);
    }

    public static ImplementationStatus getImplementationStatus(String rawValue) {
        if (rawValue == null || rawValue.isEmpty()) {
            return null;
        }
        switch (rawValue) {
        case PROP_IMPLEMENTATION_STATUS_NO:
            return ImplementationStatus.NO;
        case PROP_IMPLEMENTATION_STATUS_NOT_APPLICABLE:
            return ImplementationStatus.NOT_APPLICABLE;
        case PROP_IMPLEMENTATION_STATUS_PARTIALLY:
            return ImplementationStatus.PARTIALLY;
        case PROP_IMPLEMENTATION_STATUS_YES:
            return ImplementationStatus.YES;
        default:
            throw new IllegalStateException("Unknown implementation status '" + rawValue + "'");
        }
    }

    public void setImplementationStatus(ImplementationStatus status) {
        String rawValue;
        if (status == null) {
            rawValue = null;
        } else {
            switch (status) {
            case NO:
                rawValue = PROP_IMPLEMENTATION_STATUS_NO;
                break;
            case NOT_APPLICABLE:
                rawValue = PROP_IMPLEMENTATION_STATUS_NOT_APPLICABLE;
                break;
            case PARTIALLY:
                rawValue = PROP_IMPLEMENTATION_STATUS_PARTIALLY;
                break;
            case YES:
                rawValue = PROP_IMPLEMENTATION_STATUS_YES;
                break;
            default:
                throw new IllegalStateException("Unknown implementation status '" + status + "'");
            }
        }
        setSimpleProperty(PROP_IMPLEMENTATION_STATUS, rawValue);
    }

    public static String getIdentifierOfSafeguard(CnATreeElement requirement) {
        return requirement.getEntity().getPropertyValue(PROP_ID);
    }

    @Override
    public String getFullTitle() {
        return joinPrefixAndTitle(getIdentifier(), getTitle());
    }

    public static boolean isSafeguard(CnATreeElement element) {
        if (element == null) {
            return false;
        }
        return TYPE_ID.equals(element.getTypeId());
    }

    @Override
    public Collection<String> getTags() {
        return TagHelper.getTags(getEntity().getPropertyValue(PROP_TAG));
    }

    @Override
    public boolean getImplementationPending() {
        return SecurityLevelUtil.getImplementationPending(getImplementationStatus());
    }
}
