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

import static sernet.verinice.model.bp.DeductionImplementationUtil.setImplementationStausToRequirement;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import sernet.verinice.interfaces.IReevaluator;
import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.bp.Reevaluator;
import sernet.verinice.model.common.AbstractLinkChangeListener;
import sernet.verinice.model.common.CascadingTransaction;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ILinkChangeListener;
import sernet.verinice.model.common.TransactionAbortedException;

/**
 * @author Daniel Murygin dm[at]sernet.de
 *
 */
public class Safeguard extends CnATreeElement implements IBpElement {

    private static final long serialVersionUID = -2117441377311538326L;

    public static final String TYPE_ID = "bp_safeguard"; //$NON-NLS-1$
    private static final String PROP_ABBR = "bp_safeguard_abbr"; //$NON-NLS-1$
    private static final String PROP_OBJECTBROWSER_DESC = "bp_safeguard_objectbrowser_content"; //$NON-NLS-1$
    private static final String PROP_NAME = "bp_safeguard_name"; //$NON-NLS-1$
    private static final String PROP_ID = "bp_safeguard_id"; //$NON-NLS-1$
    public static final String PROP_QUALIFIER = "bp_safeguard_qualifier"; //$NON-NLS-1$
    private static final String PROP_LAST_CHANGE = "bp_safeguard_last_change"; //$NON-NLS-1$
    private static final String PROP_RESP_ROLES = "bp_safeguard_responsibleroles";//$NON-NLS-1$
    public static final String PROP_CONFIDENTIALITY = "bp_safeguard_value_method_confidentiality";//$NON-NLS-1$
    public static final String PROP_INTEGRITY = "bp_safeguard_value_method_integrity";//$NON-NLS-1$
    public static final String PROP_AVAILABILITY = "bp_safeguard_value_method_availability";//$NON-NLS-1$
    public static final String PROP_QUALIFIER_BASIC = "bp_safeguard_qualifier_basic";//$NON-NLS-1$
    public static final String PROP_QUALIFIER_STANDARD = "bp_safeguard_qualifier_standard";//$NON-NLS-1$
    public static final String PROP_QUALIFIER_HIGH = "bp_safeguard_qualifier_high";//$NON-NLS-1$
    public static final String REL_BP_SAFEGUARD_BP_THREAT = "rel_bp_safeguard_bp_threat"; //$NON-NLS-1$

    private final IReevaluator protectionRequirementsProvider = new Reevaluator(this);
    private final ILinkChangeListener linkChangeListener = new AbstractLinkChangeListener() {

        private static final long serialVersionUID = 9205866080876674150L;

        @Override
        public void determineValue(CascadingTransaction ta) throws TransactionAbortedException {
            if (ta.hasBeenVisited(Safeguard.this)) {
                return;
            }

            for (CnALink cnALink : Safeguard.this.getLinksUp()) {
                CnATreeElement dependant = cnALink.getDependant();
                if ((BpRequirement.TYPE_ID.equals(dependant.getTypeId()))) {
                    setImplementationStausToRequirement(Safeguard.this, dependant);
                }
            }
        }
    };

    protected Safeguard() {}

    @Override
    public ILinkChangeListener getLinkChangeListener() {
        return linkChangeListener;
    }

    @Override
    public IReevaluator getProtectionRequirementsProvider() {
        return protectionRequirementsProvider;
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
    
    public void setTitle(String title) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), title);
    }

    public String getQualifier() {
        return getEntity().getPropertyValue(PROP_QUALIFIER);
    }

    public void setQualifier(String qualifier) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_QUALIFIER), qualifier);
    }

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
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_LAST_CHANGE), String.valueOf(date.getTime()));
    }

    public Set<String> getResponsibleRoles(){
        String property = getEntity().getPropertyValue(PROP_RESP_ROLES);
        Set<String> roles;
        if (property != null && property.length() > 0) {
            StringTokenizer tokenizer = new StringTokenizer(property, "/");
            roles = new HashSet<>(tokenizer.countTokens() + 1);
            while (tokenizer.hasMoreTokens()) {
                roles.add(tokenizer.nextToken());
            }
        } else {
            roles = new HashSet<>();
        }
        return roles;
    }

    public void setResponisbleRoles(String roles) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_RESP_ROLES), roles);
    }

    public void addResponsibleRole(String role) {
        Set<String> roles = getResponsibleRoles();
        roles.add(role);
        StringBuilder property = new StringBuilder();
        Iterator<String> iter = roles.iterator();
        while (iter.hasNext()) {
            property.append(iter.next());
            if (iter.hasNext()) {
                property.append(" / ");
            }
        }
        setResponisbleRoles(property.toString());
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

    public static String getIdentifierOfSafeguard(CnATreeElement requirement) {
        return requirement.getEntity().getPropertyValue(PROP_ID);
    }
}
