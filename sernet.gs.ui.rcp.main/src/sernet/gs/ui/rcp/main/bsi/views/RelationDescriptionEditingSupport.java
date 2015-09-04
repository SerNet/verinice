/*******************************************************************************
 * Copyright (c) 2012 Julia Haas.
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
 *     Julia Haas <jh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;

/**
 * @author Julia Haas <jh[at]sernet[dot]de>
 *
 */

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.service.commands.UpdateElement;

public class RelationDescriptionEditingSupport extends EditingSupport {

    static final Logger LOG = Logger.getLogger(RelationDescriptionEditingSupport.class);
    private IRelationTable view;
    private TableViewer viewer;

    public RelationDescriptionEditingSupport(IRelationTable view, TableViewer viewer) {
        super(viewer);
        this.viewer = viewer;
        this.view = view;
    }

    protected boolean canEdit(Object element) {
        if ((element instanceof CnALink)) {
            return true;
        }
        return false;
    }

    protected CellEditor getCellEditor(Object element) {
        if (!(element instanceof CnALink)){
            return null;
        }
        return new TextCellEditor(viewer.getTable());
    }

    protected Object getValue(Object element) {
        if (!(element instanceof CnALink)){
            return null;
        }
        CnALink link = (CnALink) element;
        String comment = link.getComment();
        Logger.getLogger(this.getClass()).debug("description " + comment);
        return comment;
    }

    protected void setValue(Object element, Object value) {
        CnALink link = (CnALink) element;
        String description = (String) value;
        link.setComment(description);

        CnALink newLink = null;

        try {
            UpdateElement<CnALink> command = new UpdateElement<CnALink>(link, true, ChangeLogEntry.STATION_ID);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            newLink = command.getElement();
        } catch (Exception e) {
            ExceptionUtil.log(e, "Fehler beim Erstellen von Kommentaren.");
        }CnAElementFactory.getModel(link.getDependant()).linkChanged(link, newLink, view);
    }

}
