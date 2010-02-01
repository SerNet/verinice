/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HitroUtil;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HuiRelation;

/**
 * A SWT composite that allow the user to create links to other objects, display the existing links, 
 * change or delete them etc.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
// TODO akoderman this view should also allow the user to create new elements and link them to the current element with one button
// i.e. for server select "Add&Link" to create a new person and link it as "owner" immediately.
public class LinkMaker extends ScrolledComposite {

	private CnATreeElement cnaElement;
	private boolean writeable;
	private Set<HuiRelation> relationsFromHere;

	/**
	 * @param parent
	 * @param style
	 */
	public LinkMaker(Composite parent) {
		super(parent, SWT.V_SCROLL
				| SWT.BORDER);
		this.setExpandHorizontal(true);
		this.setExpandVertical(true);
		FormLayout formLayout = new FormLayout();
		this.setLayout(formLayout);
		
	}

	/**
	 * 
	 */
	public void close() {
		// TODO Auto-generated method stub
		// remove 
	}

	/**
	 * @param cnAElement
	 * @param isWriteAllowed
	 */
	public void createView(CnATreeElement cnAElement, Boolean isWriteAllowed) {
		this.cnaElement = cnAElement;
		this.writeable = isWriteAllowed;

		Label label1 = new Label(this, SWT.NULL	);
		label1.setText("Relations for: ");

		FormData formData = new FormData();
		formData.top = new FormAttachment(0, 5);
		formData.left = new FormAttachment(0, 5);
		label1.setLayoutData(formData);
		label1.pack();
		
		Combo combo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setItems(getNames(relationsFromHere));
		FormData formData2 = new FormData();
		formData2.top = new FormAttachment(0, 5);
		formData2.left = new FormAttachment(label1, 5);
		combo.setLayoutData(formData2);
		combo.pack();
		
		
		
		
	}

	private String[] getNames(Set<HuiRelation> relationsFromHere2) {
		String[] names = new String[relationsFromHere2.size()];
        int i=0;
		for (HuiRelation huiRelation : relationsFromHere) {
			String targetEntityTypeID = huiRelation.getTo();
			names[i] = HitroUtil.getInstance().getTypeFactory().getEntityType(targetEntityTypeID).getName();
			i++;
		}
		return names;
	}

	/**
	 * @return
	 */
	private void fillPossibleLinkLists() {
		relationsFromHere = new HashSet<HuiRelation>();
		EntityType entityType = HitroUtil.getInstance().getTypeFactory().getEntityType(cnaElement.getEntity().getEntityType());
		
		relationsFromHere = entityType.getPossibleRelations();
		
		
		// relations are always modelled from one side of the relation. We will allow the user to create links both FROM the current element
		// as well as TO the current element (because he doesn't know, from which side a relation is modelled):
		// FIXME akoderman still to do
		
	}

	
	

}
