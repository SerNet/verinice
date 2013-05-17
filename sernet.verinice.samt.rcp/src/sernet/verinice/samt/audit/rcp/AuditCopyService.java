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

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IPostProcessor;
import sernet.verinice.iso27k.service.CopyService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.samt.audit.service.CopyLinks;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * 
 */
public class AuditCopyService extends CopyService {

    private static final Logger LOG = Logger.getLogger(AuditCopyService.class);
    
    /**
     * @author Daniel Murygin <dm[at]sernet[dot]de>
     * 
     */
    public class LinkTask implements IPostProcessor {

        
        private CnATreeElement linkTo;
        
        /**
         * @param linkElement
         */
        public LinkTask(CnATreeElement linkTo) {
            this.linkTo = linkTo;
        }

        /* (non-Javadoc)
         * @see sernet.verinice.iso27k.service.PasteService.IPostProcessor#process(java.util.Map)
         */
        @Override
        public void process(List<String> copyUuidList, Map<String, String> sourceDestMap) {
            try {
                CopyLinks copyLinksCommand = new CopyLinks(copyUuidList,sourceDestMap,linkTo);          
                getCommandService().executeCommand(copyLinksCommand);
            } catch (CommandException e) {
                LOG.error("Error while creating links on server.", e);
            }
        }
       
    }

    /**
     * @param group
     * @param elementList
     */
    public AuditCopyService(CnATreeElement group, CnATreeElement linkTo, List<CnATreeElement> elementList) {
        super(group, elementList);
        addPostProcessor(new LinkTask(linkTo));
    }

}
