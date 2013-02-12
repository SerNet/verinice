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

import sernet.verinice.model.bpm.MissingParameterException;

/**
 * Sends template based emails.
 * Templates uses a customizable parameter map.
 * 
 * Don't use this interface directly use {@link GenericEmailHandler}.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IEmailHandler {

    /**
     * Send an email to the assignee.
     * 
     * @param assignee A username
     * @param uuid The UUID of an element
     */
    void send(String assignee, String type, Map<String, Object> processVariables, String uuidElement);
    
    /**
     * Use this method to add parameter used in the template for this handler.
     * 
     * @param uuidElement The UUID of an element
     * @param parameter Parameter for the email template
     * @throws MissingParameterException 
     */
    void addParameter(String type, Map<String, Object> processVariables, String uuidElement, Map<String, String> emailParameter) throws MissingParameterException;
    
    /**
     * This method must return the name of the template file without path, locale and file extensions.
     * 
     * If template files are IssueReminder.vm and IssueReminder_de.vm
     * this method must return "IssueReminder".
     * 
     * Templates directory is "sernet/verinice/bpm" in bundle "sernet.gs.server".
     * 
     * @return Name of the template file without path, locale and file extensions
     */
    String getTemplate();
    
    boolean isHtml();

}
