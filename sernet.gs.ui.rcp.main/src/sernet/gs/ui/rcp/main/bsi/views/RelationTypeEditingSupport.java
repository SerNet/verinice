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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CreateLink;
import sernet.verinice.service.commands.crud.ChangeLinkType;

/**
 * This class provides the editing support for the relation type column in the relations table used
 * in the LinkMaker and in the RelationTableViewer.
 * 
 * Note that some concepts used in this context are somewhat confusing. The terms "Dependant" and
 * "Dependency" stemming from CnALink are incorrect. Instead the terms "Source" and "Target"
 * should be used.
 *
 * @author koderman[at]sernet[dot]de
 */
public class RelationTypeEditingSupport extends EditingSupport {

    private Logger log = Logger.getLogger(RelationTypeEditingSupport.class);

    private IRelationTable view;
    private TableViewer viewer;
 
    private static HUITypeFactory huiTypeFactory = HitroUtil.getInstance().getTypeFactory();
    
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
        CnALink cnaLink = (CnALink) element;
        String sourceEntityTypeId = cnaLink.getDependant().getEntityType().getId();
        String targetEntityTypeId = cnaLink.getDependency().getEntityType().getId();
        Set<HuiRelation> possibleRelations = huiTypeFactory.getPossibleRelations(
                sourceEntityTypeId, targetEntityTypeId);

        return (possibleRelations != null && !possibleRelations.isEmpty());
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
        if (!(element instanceof CnALink)) {
            return null;
        }
        CnALink cnaLink = (CnALink) element;

        String[] linkTypeNames = getPossibleLinkTypeNames(cnaLink);
        ComboBoxCellEditor cellEditor = new ComboBoxCellEditor(
                viewer.getTable(), linkTypeNames, SWT.READ_ONLY);
        cellEditor.setActivationStyle(ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
        return cellEditor;
    }

    private String[] getPossibleLinkTypeNames(CnALink cnaLink) {
        CnATreeElement relationSource = cnaLink.getDependant();
        CnATreeElement relationTarget = cnaLink.getDependency();
        String relationSourceEntityTypeId = relationSource.getEntityType().getId();
        String relationTargetEntityTypeId = relationTarget.getEntityType().getId();
        
        Set<HuiRelation> possibleDownRelations = huiTypeFactory
                .getPossibleRelations(relationSourceEntityTypeId, relationTargetEntityTypeId);
        Set<HuiRelation> possibleUpRelations = huiTypeFactory
                .getPossibleRelations(relationTargetEntityTypeId, relationSourceEntityTypeId);

        Set<String> relationNames = new HashSet<>();
        String relationName;

        for (HuiRelation huiRelation : possibleDownRelations) {
            if (CnALink.isDownwardLink(view.getInputElmt(), cnaLink)) {
                relationName = huiRelation.getName();
            } else {
                relationName = huiRelation.getReversename();
            }
            relationNames.add(relationName);
        }
        for (HuiRelation huiRelation : possibleUpRelations) {
            if (CnALink.isDownwardLink(view.getInputElmt(), cnaLink)) {
                relationName = huiRelation.getReversename();
            } else {
                relationName = huiRelation.getName();
            }
            relationNames.add(relationName);
        }
        
        String[] currentLinkTypeNames = relationNames.toArray(new String[relationNames.size()]);
        Arrays.sort(currentLinkTypeNames, 0, currentLinkTypeNames.length, new NumericStringComparator());
        return currentLinkTypeNames;
    }

    @Override
    protected Object getValue(Object element) {
        if (!(element instanceof CnALink)) {
            return null;
        }
        CnALink cnaLink = (CnALink) element;
        
        String currentName = CnALink.getRelationName(view.getInputElmt(), cnaLink);
        if(log.isDebugEnabled()){
            log.debug("Current name: " + currentName);
        }

        int index = getIndex(currentName, getPossibleLinkTypeNames(cnaLink));
        if(log.isDebugEnabled()){
            log.debug("Index in getValue(): " + index);
        }
                
        return index;
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
        CnALink cnaLink = (CnALink) element;
        int index = (Integer) value;
        
        String[] possibleLinkTypeNames = getPossibleLinkTypeNames(cnaLink);
        String linkTypeName = possibleLinkTypeNames[index];
        String linkTypeId = getLinkTypeIdForName(cnaLink, linkTypeName);
        if(log.isDebugEnabled()) {
            log.debug("Setting value " + linkTypeId);
        }
        
        CnALink newCnaLink = null;
        if (changeLinkDirection(cnaLink, linkTypeId)) {
            CreateLink<CnATreeElement, CnATreeElement> createLinkCommand =
                    new CreateLink<>(
                            cnaLink.getDependency(), cnaLink.getDependant(), linkTypeId);
            try {
                createLinkCommand = ServiceFactory.lookupCommandService()
                        .executeCommand(createLinkCommand);
                newCnaLink = createLinkCommand.getLink();
                if(CnAElementHome.getInstance().isDeleteAllowed(cnaLink)){
                    CnAElementHome.getInstance().remove(cnaLink);
                    if (CnAElementFactory.isModelLoaded()) {
                        CnAElementFactory.getLoadedModel().linkRemoved(cnaLink);
                        CnAElementFactory.getLoadedModel().linkAdded(newCnaLink);
                    }
                    CnAElementFactory.getInstance().getISO27kModel().linkRemoved(cnaLink);
                    CnAElementFactory.getInstance().getISO27kModel().linkAdded(newCnaLink);
                }
            } catch (CommandException e) {
                ExceptionUtil.log(e, Messages.RelationTypeEditingSupport_1);
            }
        } else {
            ChangeLinkType command = new ChangeLinkType(cnaLink, linkTypeId, cnaLink.getComment());

            try {
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                newCnaLink = command.getLink();
            } catch (CommandException e) {
                ExceptionUtil.log(e, Messages.RelationTypeEditingSupport_2);
            }
            
            CnATreeElement source = cnaLink.getDependant();
            CnATreeElement sourceModel = CnAElementFactory.getModel(source);
            sourceModel.linkChanged(cnaLink, newCnaLink, view);
            if (CnAElementFactory.isModelLoaded()) {
                CnAElementFactory.getLoadedModel().linkChanged(cnaLink, newCnaLink, view);
            }
            
            CnAElementFactory.getInstance().getISO27kModel().linkChanged(cnaLink, newCnaLink, view);
        }
    }
    
    private boolean changeLinkDirection(CnALink link, String linkTypeId) {
        String sourceTypeId = link.getDependency().getEntityType().getId();
        String targetTypeId = link.getDependant().getEntityType().getId();
        Set<HuiRelation> huiRelations = huiTypeFactory.getPossibleRelations(
                sourceTypeId, targetTypeId);
        for(HuiRelation huiRelation : huiRelations){
            if(huiRelation.getId().equals(linkTypeId)){
                return true;
            }
        }
        return false;
    }

    private String getLinkTypeIdForName(CnALink link, String linkTypeName) {
        String sourceTypeId = link.getDependant().getEntityType().getId();
        String targetTypeId = link.getDependency().getEntityType().getId();
        Set<HuiRelation> possibleDownRelations = huiTypeFactory.getPossibleRelations(
                sourceTypeId, targetTypeId);
        Set<HuiRelation> possibleUpRelations = huiTypeFactory.getPossibleRelations(
                targetTypeId, sourceTypeId);
        
        for (HuiRelation huiRelation : possibleDownRelations) {
            String id = huiRelation.getId();
            String name;
            if (CnALink.isDownwardLink(view.getInputElmt(), link)) {
                name = huiRelation.getName();
            } else {
                name = huiRelation.getReversename();
            }
            if (name.equals(linkTypeName)) {
                return id;
            }
        }

        for (HuiRelation huiRelation : possibleUpRelations) {
            String id = huiRelation.getId();
            String name;
            if (view.getInputElmt().getLinksUp().contains(link)) {
                name = huiRelation.getName();
            } else {
                name = huiRelation.getReversename();
            }
            if (name.equals(linkTypeName)) {
                return id;
            }
        }
     
        return "";
    }
}
