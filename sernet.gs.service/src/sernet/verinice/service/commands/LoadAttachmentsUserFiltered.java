/*******************************************************************************
 * Copyright (c) 2013 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.Attachment;

/**
 * @deprecated use {@link LoadAttachments}
 */
@Deprecated
public class LoadAttachmentsUserFiltered extends GenericCommand {

    private static final long serialVersionUID = 20140530;

    private static final Logger LOG = Logger.getLogger(LoadAttachmentsUserFiltered.class);

    private Integer id;

    private List<Attachment> result;

    public LoadAttachmentsUserFiltered(Integer id) {
        this.id = id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try {
            LoadAttachments command = null;

            command = new LoadAttachments(id);

            command = getCommandService().executeCommand(command);
            result = command.getAttachmentList();
        } catch (Exception e) {
            LOG.error("Error loading attachments filtered by user", e);
        }
    }

    public List<Attachment> getResult() {
        if (result == null) {
            return new ArrayList<Attachment>(0);
        }
        return this.result;
    }
}