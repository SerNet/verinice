/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.ChangeLinkType;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CreateLink;

/**
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class RelationTypeEditingSupport extends EditingSupport {

    private IRelationTable view;
    private TableViewer viewer;
    
    private Logger log = Logger.getLogger(RelationTypeEditingSupport.class);

    public RelationTypeEditingSupport(IRelationTable view, TableViewer viewer) {
        super(viewer);
        this.viewer = viewer;
        this.view = view;
    }

    @Override
    protected boolean canEdit(Object element) {
        if (!(element instanceof CnALink)) {
            return false;
        }
        CnALink link = (CnALink) element;
        Set<HuiRelation> possibleRelations = HitroUtil.getInstance().getTypeFactory().getPossibleRelations(link.getDependant().getEntityType().getId(), link.getDependency().getEntityType().getId());

        return (possibleRelations != null && possibleRelations.size() > 0);
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
        if (!(element instanceof CnALink)) {
            return null;
        }
        CnALink link = (CnALink) element;

        String[] currentLinkTypeNames = getPossibleLinkTypeNames(link);
        ComboBoxCellEditor choiceEditor = new ComboBoxCellEditor(viewer.getTable(), currentLinkTypeNames, SWT.READ_ONLY);
        choiceEditor.setActivationStyle(ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
        return choiceEditor;
    }

    private String[] getPossibleLinkTypeNames(CnALink link) {
        Set<HuiRelation> possibleDownRelations = HitroUtil.getInstance().getTypeFactory().getPossibleRelations(link.getDependant().getEntityType().getId(), link.getDependency().getEntityType().getId());
        Set<HuiRelation> possibleUpRelations = HitroUtil.getInstance().getTypeFactory().getPossibleRelations(link.getDependency().getEntityType().getId(), link.getDependant().getEntityType().getId());
        Set<String> names = new HashSet<String>();
        Set<String> ids = new HashSet<String>();
           
        for (HuiRelation huiRelation : possibleDownRelations) {
            String id = huiRelation.getId();
            String name = (CnALink.isDownwardLink(view.getInputElmt(), link)) ? huiRelation.getName() : huiRelation.getReversename();
            names.add(name);
            ids.add(id);
        }
        for (HuiRelation huiRelation : possibleUpRelations) {
            String id = huiRelation.getId();
            String name = (CnALink.isDownwardLink(view.getInputElmt(), link)) ? huiRelation.getReversename() : huiRelation.getName();
            names.add(name);
            ids.add(id);
        }
        

        String[] currentLinkTypeNames = names.toArray(new String[names.size()]);
        return currentLinkTypeNames;
    }

    @Override
    protected Object getValue(Object element) {
        if (!(element instanceof CnALink)) {
            return null;
        }
        CnALink link = (CnALink) element;
        String currentName = CnALink.getRelationName(view.getInputElmt(), link);
        if(log.isDebugEnabled()){
            log.debug("current name " + currentName);
        }

        int idx = getIndex(currentName, getPossibleLinkTypeNames(link));
        if(log.isDebugEnabled()){
            log.debug("getvalue index: " + idx);
        }
        return idx;
    }

    private int getIndex(String currentName, String[] currentLinkTypeNames) {
        int i = 0;
        for (String name : currentLinkTypeNames) {
            if (name.equals(currentName)) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    @Override
    protected void setValue(Object element, Object value) {
        if (!(element instanceof CnALink)) {
            return;
        }
        CnALink link = (CnALink) element;
        int index = (Integer) value;

        String linkTypeName = getPossibleLinkTypeNames(link)[index];
        String linkTypeID = getLinkIdForName(link, linkTypeName);
        if(log.isDebugEnabled()){
            log.debug("Setting value " + linkTypeID);
        }
        
        CnALink newLink = null;
        if(changeLinkDirection(link, linkTypeID)){
            CreateLink<CnALink, CnATreeElement, CnATreeElement> createLinkCommand = new CreateLink<CnALink, CnATreeElement, CnATreeElement>(link.getDependency(), link.getDependant(), linkTypeID);
            try {
                createLinkCommand = ServiceFactory.lookupCommandService().executeCommand(createLinkCommand);
                newLink = createLinkCommand.getLink();
                if(CnAElementHome.getInstance().isDeleteAllowed(link)){
                    CnAElementHome.getInstance().remove(link);
                    if (CnAElementFactory.isModelLoaded()) {
                        CnAElementFactory.getLoadedModel().linkRemoved(link);
                        CnAElementFactory.getLoadedModel().linkAdded(newLink);
                    }
                    CnAElementFactory.getInstance().getISO27kModel().linkRemoved(link);
                    CnAElementFactory.getInstance().getISO27kModel().linkAdded(newLink);
                }
            } catch (CommandException e) {
                ExceptionUtil.log(e, Messages.RelationTypeEditingSupport_1);
            }
        } else {

            ChangeLinkType command = new ChangeLinkType(link, linkTypeID, link.getComment());

            try {
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                newLink = command.getLink();
            } catch (CommandException e) {
                ExceptionUtil.log(e, Messages.RelationTypeEditingSupport_2);
            }
            CnAElementFactory.getModel(link.getDependant()).linkChanged(link, newLink, view);
            if (CnAElementFactory.isModelLoaded()) {
                CnAElementFactory.getLoadedModel().linkChanged(link, newLink, view);
            }
            CnAElementFactory.getInstance().getISO27kModel().linkChanged(link, newLink, view);
        }
    }
    
    private boolean changeLinkDirection(CnALink link, String linkTypeId){
        Set<HuiRelation> toFromRelations = HitroUtil.getInstance().getTypeFactory().getPossibleRelations(link.getDependency().getEntityType().getId(), link.getDependant().getEntityType().getId());
        for(HuiRelation hr : toFromRelations){
            if(hr.getId().equals(linkTypeId)){
                return true;
            }
        }
        return false;
    }

    /**
     * @param linkTypeName
     */
    private String getLinkIdForName(CnALink link, String linkTypeName) {
        Set<HuiRelation> possibleDownRelations = HitroUtil.getInstance().getTypeFactory().getPossibleRelations(link.getDependant().getEntityType().getId(), link.getDependency().getEntityType().getId());
        Set<HuiRelation> possibleUpRelations = HitroUtil.getInstance().getTypeFactory().getPossibleRelations(link.getDependency().getEntityType().getId(), link.getDependant().getEntityType().getId());
        for (HuiRelation huiRelation : possibleDownRelations) {
            String id = huiRelation.getId();
            String name = (CnALink.isDownwardLink(view.getInputElmt(), link)) ? huiRelation.getName() : huiRelation.getReversename();
            if (name.equals(linkTypeName)) {
                return id;
            }
        }

        for (HuiRelation huiRelation : possibleUpRelations) {
            String id = huiRelation.getId();
            String name = (view.getInputElmt().getLinksUp().contains(link)) ? huiRelation.getName() : huiRelation.getReversename();
            if (name.equals(linkTypeName)) {
                return id;
            }
        }

        
        return "";

    }
}
