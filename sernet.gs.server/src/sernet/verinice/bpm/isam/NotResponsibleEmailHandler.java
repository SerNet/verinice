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
package sernet.verinice.bpm.isam;

import java.util.Map;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.bpm.GenericEmailHandler;
import sernet.verinice.bpm.IEmailHandler;
import sernet.verinice.bpm.IRemindService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class NotResponsibleEmailHandler extends GenericEmailHandler implements IEmailHandler {
    
    private static final String TEMPLATE = "NotResponsibleReminder";
    
    private static final String TEMPLATE_ELEMENT_TITLE = "elementTitle";
    
    private static final String TEMPLATE_COMMENT = "comment";
    
    /* (non-Javadoc)
     * @see sernet.verinice.bpm.IEmailHandler#addParameter(java.lang.String, java.util.Map)
     */
    @Override
    public void addParameter(String type, Map<String, Object> processVariables, String uuidElement, Map<String, String> emailParameter) {
        CnATreeElement element = getRemindService().retrieveElement(uuidElement, RetrieveInfo.getPropertyInstance());
        String title = element.getTitle();
        emailParameter.put(TEMPLATE_ELEMENT_TITLE, title);
        emailParameter.put(IRemindService.TEMPLATE_SUBJECT, "Not responsible for control: " + title); 
        if(element instanceof Control) {
            emailParameter.put(TEMPLATE_COMMENT, ((Control)element).getImplementationExplanation());
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.bpm.IEmailHandler#getTemplate()
     */
    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

}
