/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.iso27k;

import java.util.Collection;

import sernet.hui.common.connect.Entity;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class Organization extends CnATreeElement implements IISO27kGroup, IISO27Scope {

	public static final String TYPE_ID = "org"; //$NON-NLS-1$
	public static final String PROP_ABBR = "org_abbr"; //$NON-NLS-1$
	public static final String PROP_NAME = "org_name"; //$NON-NLS-1$
	public static final String PROP_TAG = "org_tag"; //$NON-NLS-1$
	
	public static final String[] CHILD_TYPES = new String[] {
	    AssetGroup.TYPE_ID,
        ControlGroup.TYPE_ID,
        AuditGroup.TYPE_ID,
        ExceptionGroup.TYPE_ID,
        PersonGroup.TYPE_ID,
        RequirementGroup.TYPE_ID,
        IncidentGroup.TYPE_ID,
        IncidentScenarioGroup.TYPE_ID,
        ResponseGroup.TYPE_ID,
        ThreatGroup.TYPE_ID,
        VulnerabilityGroup.TYPE_ID,
        DocumentGroup.TYPE_ID,
        RecordGroup.TYPE_ID,
        ProcessGroup.TYPE_ID,
    };
	
	/**
	 * Creates an empty Organization
	 */
	public Organization() {
		super();
		setEntity(new Entity(TYPE_ID));
        getEntity().initDefaultValues(getTypeFactory());
	}
	
	public Organization(CnATreeElement parent) {
	    this(parent,false);
	}
	
	public Organization(CnATreeElement parent, boolean createChildren) {
		super(parent);
		setEntity(new Entity(TYPE_ID));
		getEntity().initDefaultValues(getTypeFactory());
        // sets the localized title via HUITypeFactory from message bundle
        setTitel(getTypeFactory().getMessage(TYPE_ID));
        if(createChildren) {
            addChild(new AssetGroup(this));
    		addChild(new ControlGroup(this));
    		addChild(new AuditGroup(this));
    		addChild(new ExceptionGroup(this));
    		addChild(new PersonGroup(this));
    		addChild(new RequirementGroup(this));
    		addChild(new IncidentGroup(this));
    		addChild(new IncidentScenarioGroup(this));
    		addChild(new ResponseGroup(this));
    		addChild(new ThreatGroup(this));
    		addChild(new VulnerabilityGroup(this));
    		addChild(new DocumentGroup(this));
    		addChild(new RecordGroup(this));
    		addChild(new ProcessGroup(this));
        }
	}
	
	@Override
	public boolean canContain(Object child) {
		return (child instanceof Group);
	}
	
	/* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IISO27kGroup#getChildTypes()
     */
    @Override
    public String[] getChildTypes() {
        return CHILD_TYPES;
    }
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTypeId()
	 */
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTitle()
	 */
	public String getTitle() {
		return getEntity().getSimpleValue(PROP_NAME);
	}
	
	
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#setTitel(java.lang.String)
     */
    public void setTitel(String name) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
    }
	
	/**
	 * @param name
	 */
	public void setTitle(String name) {
	    setTitel(name);
	}
	
	public String getAbbreviation() {
		return getEntity().getSimpleValue(PROP_ABBR);
	}
	
	public void setAbbreviation(String abbreviation) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ABBR), abbreviation);
	}
	
	public Collection<? extends String> getTags() {
		return TagHelper.getTags(getEntity().getSimpleValue(PROP_TAG));
	}

	
}
