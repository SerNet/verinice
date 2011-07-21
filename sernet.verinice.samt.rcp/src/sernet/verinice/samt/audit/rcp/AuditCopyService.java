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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.service.CopyService;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.common.CnALink;
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

        
        RetrieveInfo ri = new RetrieveInfo().setLinksDown(true).setLinksUp(true);
        
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
        public void process(Map<String, String> sourceDestMap) {
            try {
                Set<String> copyUuidSet = new HashSet<String>();
                for (CnATreeElement element : getCopyElements()) {
                    copyUuidSet.add(element.getUuid());
                } 
                CopyLinks copyLinksCommand = new CopyLinks(copyUuidSet,sourceDestMap,linkTo);          
                copyLinksCommand = getCommandService().executeCommand(copyLinksCommand);
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
