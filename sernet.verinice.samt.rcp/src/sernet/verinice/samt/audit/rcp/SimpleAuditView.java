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
package sernet.verinice.samt.audit.rcp;

import sernet.verinice.iso27k.rcp.ISMView;
import sernet.verinice.iso27k.rcp.action.TypeFilter;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.rcp.IAttachedToPerspective;

/**
 * Simple view for Audits
 * 
 * This is a extended version of the {@link ISMView} which reduces 
 * the functionality of it's base class. Only Audits are visible by 
 * adding a type filter.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de> 
 */
public class SimpleAuditView extends ISMView implements IAttachedToPerspective  {

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "sernet.verinice.samt.rcp.views.SimpleAuditView"; //$NON-NLS-1$

    /**
     * The constructor.
     */
    public SimpleAuditView() {
        super();
    }
    
   
    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.ISMView#createTypeFilter()
     */
    @Override
    protected TypeFilter createTypeFilter() {
        TypeFilter filter = new TypeFilter(viewer);
        filter.addType(new String[]{Audit.TYPE_ID,AuditGroup.TYPE_ID});
        return filter;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.rcp.IAttachedToPerspective#getPerspectiveId()
     */
    public String getPerspectiveId() {
        return AuditPerspective.ID;
    }

}