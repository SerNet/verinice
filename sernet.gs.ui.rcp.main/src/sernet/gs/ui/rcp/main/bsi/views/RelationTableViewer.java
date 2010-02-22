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
package sernet.gs.ui.rcp.main.bsi.views;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.common.model.ChangeLogEntry;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HitroUtil;
import sernet.hui.common.connect.HuiRelation;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class RelationTableViewer extends TableViewer {

	private TableColumn col1;
	private TableViewerColumn viewerCol2;
	private TableColumn col4;
	private IRelationTable view;
	private TableColumn col3;
	
	/**
	 * @param parent
	 * @param i
	 */
	public RelationTableViewer(IRelationTable relationView, Composite parent, int style) {
		super(parent, style);
		this.view = relationView;
		
		Table table = getTable();
		
		col1 = new TableColumn(table, SWT.LEFT);
		col1.setText("");
		col1.setWidth(25);
		col1.setResizable(false);
		
		viewerCol2 = new TableViewerColumn(this, SWT.LEFT);
		viewerCol2.getColumn().setText("Relation");
		viewerCol2.getColumn().setWidth(100);
		
		viewerCol2.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object obj) {
				if (!(obj instanceof CnALink))
					return "";
				
				CnALink link = (CnALink) obj;
				HuiRelation relation = HitroUtil.getInstance().getTypeFactory().getRelation(link.getRelationId());

				// if we can't find a real name for the relation, we just display "depends on" or "necessary for":
					if (CnALink.isDownwardLink(view.getInputElmt(), link))
						return (relation != null) ? relation.getName() : "hängt ab von";
					else
						return (relation != null) ? relation.getReversename() : "ist nötig für";
			}
		});
		viewerCol2.setEditingSupport(new RelationTypeEditingSupport(view, this));

		col3 = new TableColumn(table, SWT.LEFT);
		col3.setText("");
		col3.setWidth(20);
		
		col4 = new TableColumn(table, SWT.LEFT);
		col4.setText("Titel");
		col4.setWidth(250);
		
		setColumnProperties(new String[] {
				IRelationTable.COLUMN_IMG, //$NON-NLS-1$
				IRelationTable.COLUMN_TYPE, //$NON-NLS-1$
				IRelationTable.COLUMN_TYPE_IMG, //$NON-NLS-1$
				IRelationTable.COLUMN_TITLE //$NON-NLS-1$
		});
		
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

	}
}