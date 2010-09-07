/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.iso27k;

import java.util.Collection;
import java.util.Collections;

import sernet.verinice.model.common.CnATreeElement;

@SuppressWarnings("serial")
public class ImportIsoGroup extends CnATreeElement implements IISO27kGroup {
	
	public static final String TYPE_ID = ImportIsoGroup.class.getSimpleName();

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
	
	public ImportIsoGroup(CnATreeElement model) {
		super(model);
	}

	protected ImportIsoGroup() {
	}

	@Override
	public String getTitle() {
		return Messages.ImportIsoGroup_0;
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
    /* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IISO27kGroup#getChildTypes()
     */
    @Override
    public String[] getChildTypes() {
        return CHILD_TYPES;
    }

	@Override
	public boolean canContain(Object obj) {
		return true;
	}

    /* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IISO27kElement#getAbbreviation()
     */
    @Override
    public String getAbbreviation() {
        return getTypeId();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IISO27kElement#getTags()
     */
    @Override
    public Collection<? extends String> getTags() {
        return Collections.emptyList();
    }


	
}
