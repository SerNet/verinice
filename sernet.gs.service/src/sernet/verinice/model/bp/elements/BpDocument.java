/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade <jk[at]sernet[dot]de>.
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
 ******************************************************************************/

package sernet.verinice.model.bp.elements;

import java.util.Collection;

import sernet.hui.common.connect.ITaggableElement;
import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.CnATreeElement;

@SuppressWarnings("serial")
public class BpDocument extends CnATreeElement implements IBpElement, ITaggableElement {

    public static final String TYPE_ID = "bp_document"; //$NON-NLS-1$
    public static final String PROP_ABBR = "bp_document_abbr"; //$NON-NLS-1$
    public static final String PROP_NAME = "bp_document_name"; //$NON-NLS-1$
    public static final String PROP_TAG = "bp_document_tag"; //$NON-NLS-1$

    public BpDocument() {
        super();
    }

    public BpDocument(CnATreeElement parent) {
        super(parent);
        init();
    }

    /*
     * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTypeId()
     */
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    /*
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

}
