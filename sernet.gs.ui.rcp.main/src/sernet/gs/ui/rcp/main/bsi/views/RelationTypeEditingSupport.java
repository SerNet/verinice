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
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.ChangeLinkType;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnALink;

/**
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class RelationTypeEditingSupport extends EditingSupport {

	private CellEditor dropDownEditor;
	private IRelationTable view;
	private TableViewer viewer;

	public RelationTypeEditingSupport(IRelationTable view, TableViewer viewer) {
		super(viewer);
		this.viewer = viewer;
		this.view = view;
	}

	protected boolean canEdit(Object element) {
		if (!(element instanceof CnALink))
			return false;

		CnALink link = (CnALink) element;
		String currentName = CnALink.getRelationName(view.getInputElmt(),
				link);
		Set<HuiRelation> possibleRelations = HitroUtil.getInstance()
				.getTypeFactory().getPossibleRelations(
						link.getDependant().getEntityType().getId(),
						link.getDependency().getEntityType().getId());

		return (possibleRelations != null && possibleRelations.size() > 0);
	}

	protected CellEditor getCellEditor(Object element) {
		if (!(element instanceof CnALink))
			return null;
		CnALink link = (CnALink) element;

		String[] currentLinkTypeNames = getPossibleLinkTypeNames(link);
		ComboBoxCellEditor choiceEditor = new ComboBoxCellEditor(viewer.getTable(), 
				currentLinkTypeNames, SWT.READ_ONLY);
		choiceEditor
				.setActivationStyle(ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
		return choiceEditor;
	}

	private String[] getPossibleLinkTypeNames(CnALink link) {
		Set<HuiRelation> possibleRelations = HitroUtil.getInstance()
				.getTypeFactory().getPossibleRelations(
						link.getDependant().getEntityType().getId(),
						link.getDependency().getEntityType().getId());
		Set<String> names = new HashSet<String>();
		Set<String> IDs = new HashSet<String>();

		for (HuiRelation huiRelation : possibleRelations) {
			String id = huiRelation.getId();
			String name = (CnALink.isDownwardLink(view.getInputElmt(), link)) ? huiRelation
					.getName()
					: huiRelation.getReversename();
			names.add(name);
			IDs.add(id);
		}

		String[] currentLinkTypeNames = (String[]) names
				.toArray(new String[names.size()]);
		return currentLinkTypeNames;
	}

	protected Object getValue(Object element) {
		if (!(element instanceof CnALink))
			return null;
		CnALink link = (CnALink) element;
		String currentName = CnALink.getRelationName(view.getInputElmt(),
				link);
		Logger.getLogger(this.getClass()).debug("current name " + currentName);

		int idx = getIndex(currentName, getPossibleLinkTypeNames(link));
		Logger.getLogger(this.getClass()).debug("getvalue index: " + idx);
		return idx;
	}

	private int getIndex(String currentName, String[] currentLinkTypeNames) {
		int i = 0;
		for (String name : currentLinkTypeNames) {
			if (name.equals(currentName))
				return i;
			++i;
		}
		return -1;
	}

	protected void setValue(Object element, Object value) {
		if (!(element instanceof CnALink))
			return;

		CnALink link = (CnALink) element;
		int index = (Integer) value;

		String linkTypeName = getPossibleLinkTypeNames(link)[index];
		String linkTypeID = getLinkIdForName(link, linkTypeName);
		Logger.getLogger(this.getClass()).debug("Setting value " + linkTypeID);

		ChangeLinkType command = new ChangeLinkType(link, linkTypeID, link.getComment());
		
		CnALink newLink = null;
		try {
			command = ServiceFactory.lookupCommandService().executeCommand(
					command);
			newLink = command.getLink();
		} catch (CommandException e) {
			ExceptionUtil.log(e, "Fehler beim Ändern der Relation.");
		}
		
		CnAElementFactory.getModel(link.getDependant()).linkChanged(link, newLink, view);
	}

	/**
	 * @param linkTypeName
	 */
	private String getLinkIdForName(CnALink link, String linkTypeName) {
		Set<HuiRelation> possibleRelations = HitroUtil.getInstance()
				.getTypeFactory().getPossibleRelations(
						link.getDependant().getEntityType().getId(),
						link.getDependency().getEntityType().getId());

		for (HuiRelation huiRelation : possibleRelations) {
			String id = huiRelation.getId();
			String name = (CnALink.isDownwardLink(view.getInputElmt(), link)) ? huiRelation
					.getName()
					: huiRelation.getReversename();
			if (name.equals(linkTypeName))
				return id;
		}
		return "";

	}
}
