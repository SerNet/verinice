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
package sernet.verinice.bpm.qm;

import java.util.Map;

import sernet.verinice.bpm.GenericEmailHandler;
import sernet.verinice.bpm.IEmailHandler;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IssueNotFixedEmailHandler extends GenericEmailHandler implements IEmailHandler {

    /* (non-Javadoc)
     * @see sernet.verinice.bpm.IEmailHandler#addParameter(java.lang.String, java.util.Map)
     */
    @Override
    public void addParameter(String uuidElement, Map<String, String> parameter) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.bpm.IEmailHandler#getTemplate()
     */
    @Override
    public String getTemplate() {
        // TODO Auto-generated method stub
        return null;
    }

}
