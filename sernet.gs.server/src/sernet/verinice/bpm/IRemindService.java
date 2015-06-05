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

import java.util.Map;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.model.bpm.MissingParameterException;
import sernet.verinice.model.common.CnATreeElement;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IRemindService {

    String TEMPLATE_NUMBER = "n";    //$NON-NLS-1$
    String TEMPLATE_URL = "url";    //$NON-NLS-1$
    String TEMPLATE_EMAIL = "email"; //$NON-NLS-1$
    String TEMPLATE_EMAIL_FROM = "emailFrom"; //$NON-NLS-1$
    String TEMPLATE_REPLY_TO = "replyTo"; //$NON-NLS-1$
    String TEMPLATE_NAME = "name"; //$NON-NLS-1$
    String TEMPLATE_ADDRESS = "address"; //$NON-NLS-1$
    String TEMPLATE_SUBJECT = "subject"; //$NON-NLS-1$
    String TEMPLATE_PATH = "path"; //$NON-NLS-1$
    
    Map<String,String> loadUserData(String name) throws MissingParameterException;
    
    void sendEmail(final Map<String,String> model, boolean html);
    
    CnATreeElement retrieveElement(String uuid, RetrieveInfo ri);
}
