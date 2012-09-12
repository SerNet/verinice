/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.bpm;

import java.util.Collections;
import java.util.Map;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Dummy implementation for standalone version.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class RemindServiceDummy implements IRemindService {

    /* (non-Javadoc)
     * @see sernet.verinice.bpm.IRemindService#loadUserData(java.lang.String)
     */
    @Override
    public Map<String, String> loadUserData(String name) {
        return Collections.emptyMap();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.bpm.IRemindService#sendEmail(java.util.Map)
     */
    @Override
    public void sendEmail(Map<String, String> model, boolean html) {
    }

    /* (non-Javadoc)
     * @see sernet.verinice.bpm.IRemindService#retrieveElement(java.lang.String, sernet.gs.service.RetrieveInfo)
     */
    @Override
    public CnATreeElement retrieveElement(String uuid, RetrieveInfo ri) {
        return null;
    }

}
