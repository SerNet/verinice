/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.samt.audit.rcp;

import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Daniel Murygin <dm@sernet.de>
 *
 */
public class AuditView extends GenericElementView {

    public static final String ID = "sernet.verinice.samt.audit.rcp.AuditView"; //$NON-NLS-1$
    
    public AuditView() {
        super(new ElementViewCommandFactory(Audit.TYPE_ID,AuditGroup.TYPE_ID));
    }
    
    public CnATreeElement getGroupToAdd() {
        CnATreeElement group = getSelectedGroup();
        final String typeId = this.commandFactory.getElementTypeId();
        if (group == null) {
            Organization org = getSelectedOrganization();
            if (org != null) {
                group = org.getGroup(typeId);
            }    
        }
        return group;
    }
    
}
