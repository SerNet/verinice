/*******************************************************************************  
 * Copyright (c) 2017 Viktor Schmidt.  
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
 *     Viktor Schmidt <vschmidt[at]ckc[dot]de> - initial API and implementation  
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.SaveElement;

/**
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de>
 */
public class CopyTemplateElements extends CopyTreeElements {

    private static final Logger LOG = Logger.getLogger(CopyTemplateElements.class);

    private Set<String> templateCandidateUuids = new HashSet<String>();

    /**
     * @param selectedGroup
     * @param elements
     * @param copyLinks
     */
    public CopyTemplateElements(CnATreeElement selectedGroup, List<CnATreeElement> elements, Set<String> templateCandidateUuids) {
        super(selectedGroup, elements, false);
        this.templateCandidateUuids = templateCandidateUuids;
    }

    @Override
    public void run(final IProgressMonitor monitor) {
        if (getElements().size() > 0) {
            super.run(monitor);
        }

        if (this.getSelectedGroup().isImplementation()) {
            try {
                this.getSelectedGroup().getImplementedTemplateUuids().addAll(templateCandidateUuids);
                SaveElement<CnATreeElement> saveCommand = new SaveElement<CnATreeElement>(this.getSelectedGroup(), true);
                saveCommand = ServiceFactory.lookupCommandService().executeCommand(saveCommand);

            } catch (CommandException e) {
                LOG.error("Error while copying template elements.", e);
            }
        }
    }
}
