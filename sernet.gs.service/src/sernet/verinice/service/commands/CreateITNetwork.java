/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.moditbp.elements;

<<<<<<< HEAD
import sernet.hui.common.connect.Entity;
||||||| merged common ancestors
=======
import sernet.verinice.model.bp.elements.ItNetwork;
>>>>>>> Merge branch feature/renewed-ITBP
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class ImportModITBPGroup extends CnATreeElement {
    
    private static final long serialVersionUID = -7286059698308443978L;
    
    public static final String TYPE_ID = "moditbpimportgroup";

    public ImportModITBPGroup(CnATreeElement model) {
        super(model);
        setEntity(new Entity(TYPE_ID));
    }
    
<<<<<<< HEAD
    protected ImportModITBPGroup() {
||||||| merged common ancestors
    @Override
    public void execute() {
        super.execute();
        if (super.element instanceof ITNetwork) {
            ITNetwork network = (ITNetwork) element;
            if(createChildren) {
                network.createNewCategories();
            }
            Set<CnATreeElement> children = network.getChildren();
            for (CnATreeElement child : children) {
                addPermissionsForScope(child);
            }
            element.setScopeId(element.getDbId());
            for (CnATreeElement group : element.getChildren()) {
                group.setScopeId(element.getDbId());
            }
        }
=======
    @Override
    public void execute() {
        super.execute();
        if (super.element instanceof ItNetwork) {
            ItNetwork network = (ItNetwork) element;
            if(createChildren) {
                network.createNewCategories();
            }
            Set<CnATreeElement> children = network.getChildren();
            for (CnATreeElement child : children) {
                addPermissionsForScope(child);
            }
            element.setScopeId(element.getDbId());
            for (CnATreeElement group : element.getChildren()) {
                group.setScopeId(element.getDbId());
            }
        }
>>>>>>> Merge branch feature/renewed-ITBP
        
    }
    
    
    /* (non-Javadoc)
     * @see sernet.verinice.model.common.CnATreeElement#getTitle()
     */
    @Override
    public String getTitle() {
        return "imported Objects"; // TODO internationalize
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.common.CnATreeElement#getTypeId()
     */
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }
    
    @Override
<<<<<<< HEAD
    public boolean canContain(Object obj) {
        if (obj instanceof ITNetwork ){
            return true;
        }
        return false;
||||||| merged common ancestors
    public ITNetwork getNewElement() {
        return (ITNetwork) super.getNewElement();
=======
    public ItNetwork getNewElement() {
        return (ItNetwork) super.getNewElement();
>>>>>>> Merge branch feature/renewed-ITBP
    }

}
