/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
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
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.iso27k;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import sernet.hui.common.connect.Entity;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
@SuppressWarnings("serial")
public class Audit extends CnATreeElement implements IISO27kElement, IISO27kGroup,IISO27Scope {

	public static final String TYPE_ID = "audit"; //$NON-NLS-1$
	public static final String PROP_ABBR = "audit_abbr"; //$NON-NLS-1$
	public static final String PROP_NAME = "audit_name"; //$NON-NLS-1$
	public static final String PROP_TAG = "audit_tag"; //$NON-NLS-1$
	
	public static final String PROP_CREAT = "audit_isa_creat";
	public static final String PROP_CREATPHONE = "audit_isa_creatphone";
    public static final String PROP_CREATMAIL = "audit_isa_creatmail";
    public static final String PROP_STARTDATE = "audit_startdate";
    public static final String PROP_ENDDATE = "audit_enddate";
	
	
	public static final String[] CHILD_TYPES = new String[] {
        AssetGroup.TYPE_ID,
        PersonGroup.TYPE_ID,
        ControlGroup.TYPE_ID,
        FindingGroup.TYPE_ID,
        EvidenceGroup.TYPE_ID,
        InterviewGroup.TYPE_ID
    };
	
	/**
	 * Creates an empty audit
	 */
	public Audit() {
		super();
	}
	
	public Audit(CnATreeElement parent) {
	    this(parent,false);
	}
	
	public Audit(CnATreeElement parent, boolean createChildren) {
		super(parent);
		setEntity(new Entity(TYPE_ID));
        getEntity().initDefaultValues(getTypeFactory());
        // sets the localized title via HUITypeFactory from message bundle
        setTitel(getTypeFactory().getMessage(TYPE_ID));
        if(createChildren) {
            addChild(new AssetGroup(this));
            addChild(new ControlGroup(this));
            addChild(new PersonGroup(this));
            addChild(new FindingGroup(this));
            addChild(new EvidenceGroup(this));
            addChild(new InterviewGroup(this));
        }
    }
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTypeId()
	 */
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	/* (non-Javadoc)
	 * @see sernet.verinice.model.common.CnATreeElement#canContain(java.lang.Object)
	 */
	@Override
	public boolean canContain(Object obj) {
	    boolean canContain = false;
        if(obj instanceof CnATreeElement) {
            CnATreeElement element = (CnATreeElement)obj;
            canContain = Arrays.asList(getChildTypes()).contains(element.getTypeId()) 
                         || this.getTypeId().equals(element.getTypeId());
        }
        return canContain;
	}
	
	/* (non-Javadoc)
     * @see sernet.verinice.iso27k.model.Group#getChildTypes()
     */
    @Override
    public String[] getChildTypes() {
        return CHILD_TYPES;
    }
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTitel()
	 */
	@Override
	public String getTitle() {
		return getEntity().getSimpleValue(PROP_NAME);
	}
	
	public void setTitel(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
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
	
	public String getCreator(){
		return getEntity().getSimpleValue(PROP_CREAT);
	}
	
	public String getCreatorPhone(){
		return getEntity().getSimpleValue(PROP_CREATPHONE);
	}
	
	public String getCreatorEmail(){
		return getEntity().getSimpleValue(PROP_CREATMAIL);
	}
	
	public Date getStartDate(){
        return getEntity().getDate(PROP_STARTDATE);
    }
    
    public Date getEndDate(){
        return getEntity().getDate(PROP_ENDDATE);
    }
	
	public ArrayList<CnATreeElement> toList(){
		ArrayList<CnATreeElement> list = new ArrayList<CnATreeElement>();
		list.add(this);
		return list;
	}

    /**
     * @return
     */
    public ControlGroup getControlGroup() {
        ControlGroup controlGroup = null;
        for (CnATreeElement child : getChildren()) {
            if(ControlGroup.TYPE_ID.equals(child.getTypeId())) {
                controlGroup = (ControlGroup) child;
                break;
            }
        }
        return controlGroup;
    }

}
