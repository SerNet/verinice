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
package sernet.verinice.model.moditbp.elements;

import sernet.hui.common.connect.Entity;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class ModITBPRequirement extends CnATreeElement {
    
    private static final long serialVersionUID = -2100983132718990376L;

    public static final String TYPE_ID = "moditbp_requirement"; //$NON-NLS-1$
    
    public static final String PROP_ABBR = "moditbp_requirement_abbr";
    public static final String PROP_TITLE = "moditbp_requirement_title";
    public static final String PROP_ID = "moditbp_requirement_id";
    public static final String PROP_DESCRIPTION = "moditbp_requirement_desc";
    public static final String PROP_QUALIFIER = "moditbp_requirement_qualifier";
    
    public ModITBPRequirement(CnATreeElement parent) {
        super(parent);
        setEntity(new Entity(TYPE_ID));
        getEntity().initDefaultValues(getTypeFactory());
        // sets the localized title via HUITypeFactory from message bundle
        setTitel(getTypeFactory().getMessage(TYPE_ID));
    }
    
    protected ModITBPRequirement() {}


    @Override
    public String getTitle() {
        StringBuilder sb = new StringBuilder();
        String title =  getEntity().getSimpleValue(PROP_TITLE);
        sb.append("[").append(getQualifier()).append("] ");
        sb.append(title);
        return sb.toString();
    }
    
    @Override
    public void setTitel(String title) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_TITLE), title);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.common.CnATreeElement#getTypeId()
     */
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }
    
    public void setAbbreviation(String abbr) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ABBR), abbr);
    }
    
    public String getAbbreviation() {
        return getEntity().getSimpleValue(PROP_ABBR);
    }
    
    public void setIdentifier(String identifier) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ID), identifier);
    }
    
    public String getIdentifier() {
        return getEntity().getSimpleValue(PROP_ID);
    }
    
    public void setDescription(String description) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_DESCRIPTION), description);
    }
    
    public String getDescription() {
        return getEntity().getSimpleValue(PROP_DESCRIPTION);
    }
    
    public void setQualifier(String qualifier) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_QUALIFIER), qualifier);
    }
    
    public String getQualifier() {
        return getEntity().getSimpleValue(PROP_QUALIFIER);
    }

}
