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

import static sernet.verinice.model.bp.DeductionImplementationUtil.isDeductiveImplementationEnabled;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.bsi.ISchutzbedarfProvider;
import sernet.verinice.model.common.CascadingTransaction;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ILinkChangeListener;
import sernet.verinice.model.common.TransactionAbortedException;
import sernet.verinice.model.iso27k.AssetValueAdapter;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class BpRequirement extends CnATreeElement implements IBpElement {
    
    private static final long serialVersionUID = 436541703079680979L;
    
    public static final String TYPE_ID = "bp_requirement"; //$NON-NLS-1$
    
    public static final String PROP_ABBR = "bp_requirement_abbr"; //$NON-NLS-1$
    public static final String PROP_OBJECTBROWSER = "bp_requirement_objectbrowser_content"; //$NON-NLS-1$
    public static final String PROP_NAME = "bp_requirement_name"; //$NON-NLS-1$
    public static final String PROP_ID = "bp_requirement_id"; //$NON-NLS-1$
    public static final String PROP_QUALIFIER = "bp_requirement_qualifier"; //$NON-NLS-1$
    public static final String PROP_LAST_CHANGE = "bp_requirement_last_change"; //$NON-NLS-1$
    public static final String PROP_RESPONSIBLE_ROLES = "bp_requirement_responsibleroles"; //$NON-NLS-1$
    public static final String PROP_CONFIDENTIALITY = "bp_requirement_value_method_confidentiality";//$NON-NLS-1$
    public static final String PROP_INTEGRITY = "bp_requirement_value_method_integrity";//$NON-NLS-1$
    public static final String PROP_AVAILABILITY = "bp_requirement_value_method_availability";//$NON-NLS-1$  
    public static final String PROP_QUALIFIER_BASIC = "bp_requirement_qualifier_basic"; //$NON-NLS-1$
    public static final String PROP_QUALIFIER_STANDARD = "bp_requirement_qualifier_standard"; //$NON-NLS-1$
    public static final String PROP_QUALIFIER_HIGH = "bp_requirement_qualifier_high"; //$NON-NLS-1$

    
    public static final String REL_BP_REQUIREMENT_BP_THREAT = "rel_bp_requirement_bp_threat"; //$NON-NLS-1$
    public static final String REL_BP_REQUIREMENT_BP_SAFEGUARD = "rel_bp_requirement_bp_safeguard"; //$NON-NLS-1$

    private final ISchutzbedarfProvider schutzbedarfProvider = new AssetValueAdapter(this);

    @Override
    public ILinkChangeListener getLinkChangeListener() {
        return linkChangeListener;
    }
    @Override
    public ISchutzbedarfProvider getSchutzbedarfProvider() {
        return schutzbedarfProvider;
    }
    
    private final IReevaluator protectionRequirementsProvider = new Reevaluator(this);
    private final ILinkChangeListener linkChangeListener = new AbstractLinkChangeListener() {

        private static final long serialVersionUID = -3220319074711927103L;

        @Override
        public void determineValue(CascadingTransaction ta) throws TransactionAbortedException {
            if (!isDeductiveImplementationEnabled(BpRequirement.this)
                    || ta.hasBeenVisited(BpRequirement.this)) {
                return;
            }

            for (CnALink cnALink : BpRequirement.this.getLinksUp()) {
                CnATreeElement dependant = cnALink.getDependant();
                if (dependant instanceof Safeguard) {
                    setImplementationStausToRequirement((Safeguard) dependant, BpRequirement.this);
                }
            }
        }
    };
    
    protected BpRequirement() {}

    public BpRequirement(CnATreeElement parent) {
        super(parent);
        init();
    }
    
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }
    
    @Override
    public boolean canContain(Object object) {
        return object instanceof BpThreat;
    }
    
    public String getObjectBrowserDescription() {
        return getEntity().getPropertyValue(PROP_OBJECTBROWSER);
    }
    
    public void setObjectBrowserDescription(String description) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_OBJECTBROWSER), description);
    }
    
    public String getAbbreviation() {
        return getEntity().getPropertyValue(PROP_ABBR);
    }
    
    public void setAbbreviation(String abbreviation) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ABBR), abbreviation);
    }
    
    @Override
    public String getTitle() {
        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(getIdentifier()).append(" ");
        titleBuilder.append("[").append(getQualifier());
        titleBuilder.append("]").append(" ");
        titleBuilder.append(getEntity().getPropertyValue(PROP_NAME));
        return titleBuilder.toString();
    }
    
    public void setTitle(String title) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), title);
    }
    
    public String getIdentifier() {
        return getEntity().getPropertyValue(PROP_ID);
    }
    
    public void setIdentifier(String id) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ID), id);
    }
    
    public String getQualifier() {
        return getEntity().getPropertyValue(PROP_QUALIFIER);
    }
    
    public void setQualifier(String qualifier) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_QUALIFIER), qualifier);
    }
    
    public Date getLastChange() {
        return getEntity().getDate(PROP_LAST_CHANGE);
    }
    
    public void setLastChange(Date date) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_LAST_CHANGE), String.valueOf(date.getTime()));
    }
    
    public Set<String> getResponsibleRoles(){
        String property = getEntity().getPropertyValue(PROP_RESPONSIBLE_ROLES);
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
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_RESPONSIBLE_ROLES), roles);
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
    
    public boolean IsAffectsConfidentiality() {
        return ((this.getNumericProperty(PROP_CONFIDENTIALITY) == 1) ? true : false); 
    }
    
    public void setIsAffectsIntegrity(boolean affectsIntegrity) {
        this.setNumericProperty(PROP_INTEGRITY, (affectsIntegrity) ? 1 : 0);
    }
    
    public boolean IsAffectsIntegrity() {
        return ((this.getNumericProperty(PROP_INTEGRITY) == 1) ? true : false); 
    }  
    
    public void setIsAffectsAvailability(boolean affectsAvailability) {
        this.setNumericProperty(PROP_AVAILABILITY, (affectsAvailability) ? 1 : 0);
    }
    
    public boolean IsAffectsAvailability() {
        return ((this.getNumericProperty(PROP_AVAILABILITY) == 1) ? true : false); 
    }

}
