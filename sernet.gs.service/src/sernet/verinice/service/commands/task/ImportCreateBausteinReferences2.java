/*******************************************************************************
 * Copyright (c) 2015 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn <sh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.task;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CreateLink;

/**
 *
 * creates references from itgs modules to itgs objects for all kind of modules (user defined also)
 * replaces {@link ImportCreateBausteinReferences} which is deprecated with the existant of this class
 *
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class ImportCreateBausteinReferences2 extends GenericCommand {
    
    private transient Logger log = Logger.getLogger(ImportCreateBausteinReferences2.class);
    private static final String NO_COMMENT = "";
    
    private CnATreeElement source = null;
    private Set<CnATreeElement> destinations = null;
    
    private Set<String> createdLinksIdentifier = null;
    
    public ImportCreateBausteinReferences2(CnATreeElement source, Set<CnATreeElement> destinations) {
        this.source = source;
        this.destinations = destinations;
        this.createdLinksIdentifier = new HashSet<>(destinations.size());
    }

    @Override
    public void execute() {
        try {
        for(CnATreeElement target : destinations) {
            Set<HuiRelation> possibleRelations = HitroUtil.getInstance().getTypeFactory().getPossibleRelations(source.getEntityType().getId(), target.getEntityType().getId());
            if (!possibleRelations.isEmpty()) {
                if(getLog().isDebugEnabled()) {
                    getLog().debug("Creating BausteinReference between " + source.getTitle() + " and " + target.getTitle());
                }
                CreateLink command = new CreateLink(source, target, possibleRelations.iterator().next().getId(), NO_COMMENT);
                command = getCommandService().executeCommand(command);
                createdLinksIdentifier.add(source.hashCode() + "#" + target.hashCode());
            }
        }
        } catch (Exception e) {
            getLog().error("Error while creating link between elements imported by gstool import", e);
            throw new RuntimeException(e);
        }

    }
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(ImportCreateBausteinReferences.class);
        }
        return log;
    }
    
    public Set<String> getCreatedLinksIdentifier(){
        return this.createdLinksIdentifier;
    }
    

}
