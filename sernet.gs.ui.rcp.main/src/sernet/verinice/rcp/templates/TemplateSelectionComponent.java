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
package sernet.verinice.rcp.templates;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import sernet.gs.ui.rcp.main.bsi.dialogs.Messages;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.ElementSelectionComponent;
import sernet.verinice.service.commands.LoadTemplateCandidates;

/**
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de>
 */
public class TemplateSelectionComponent extends ElementSelectionComponent {

    private static final Logger LOG = Logger.getLogger(TemplateSelectionComponent.class);

    private CnATreeElement inputElement;

    /**
     * @param container
     * @param type
     * @param scopeId
     */
    public TemplateSelectionComponent(Composite container, CnATreeElement inputElement) {
        super(container, inputElement.getTypeId(), inputElement.getScopeId());
        this.inputElement = inputElement;
    }

    @Override
    protected void createColumns() {
        final int column4Width = 200;
        // parent object column
        TableViewerColumn column2 = new TableViewerColumn(getViewer(), SWT.LEFT);
        column2.getColumn().setText(Messages.CnATreeElementSelectionDialog_11);
        column2.getColumn().setResizable(true);
        column2.getColumn().setWidth(column4Width);
        column2.setLabelProvider(new CellLabelProvider() {
            @Override
            public void update(ViewerCell cell) {
                if (cell.getElement() instanceof PlaceHolder) {
                    cell.setText(((PlaceHolder) cell.getElement()).getTitle());
                    return;
                }
                CnATreeElement elmt = (CnATreeElement) cell.getElement();
                cell.setText(elmt.getParent().getTitle());
            }
        });
        super.createColumns();
    }

    @Override
    protected void loadElementsFromDb() throws CommandException {
        LoadTemplateCandidates command;
        if (isScopeOnly()) {
            command = new LoadTemplateCandidates(this.inputElement.getUuid(), getTypeId(), getScopeId(), getGroupId());
        } else {
            command = new LoadTemplateCandidates(this.inputElement.getUuid(), getTypeId());
        }
        command = ServiceFactory.lookupCommandService().executeCommand(command);
        showElementsInTable(new ArrayList<CnATreeElement>(command.getTemplateCandidates()));
    }
}
