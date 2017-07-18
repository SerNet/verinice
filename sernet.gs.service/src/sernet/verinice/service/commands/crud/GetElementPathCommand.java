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
package sernet.verinice.service.commands.crud;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.LoadAncestors;

/**
 * Command to retrieve to the complete path from a given {@link CnATreeElement} (by uuid and typid)
 * to its rootElement ( {@link ITVerbund} or {@link Organization} ) 
 * 
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
public class GetElementPathCommand extends GenericCommand {
    
    private String result, uuid, typeId;
    
    public GetElementPathCommand(String uuid, String typeId){
        this.uuid = uuid;
        this.typeId = typeId;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {

        result = getElementPath(uuid, typeId);

    }
    
    private String getElementPath(String uuid, String typeId){

        RetrieveInfo ri = RetrieveInfo.getPropertyInstance();

        LoadAncestors command = new LoadAncestors(typeId, uuid, ri);
        try {
            command = getCommandService().executeCommand(command);
        } catch (CommandException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        CnATreeElement current = command.getElement();

        // build object path
        StringBuilder sb = new StringBuilder();
        sb.insert(0, current.getTitle());

        while (current.getParent() != null) {
            current = current.getParent();
            sb.insert(0, "/");
            sb.insert(0, current.getTitle());
        }

        // crop the root element, which is always ISO .. or BSI ...
        String[] p = sb.toString().split("/");
        sb = new StringBuilder();
        for (int i = 1; i < p.length; i++) {
            sb.append("/").append(p[i]);
        }
        return sb.toString();
    }
    
    public String getResult(){
        return result;
    }

}
