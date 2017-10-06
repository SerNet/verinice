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

import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.common.CnATreeElement;

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
    private static final String PROP_QUALIFIER = "bp_safeguard_qualifier"; //$NON-NLS-1$
    
    public static final String REL_BP_SAFEGUARD_BP_THREAT = "rel_bp_safeguard_bp_threat"; //$NON-NLS-1$

    protected Safeguard() {}
    
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

}
