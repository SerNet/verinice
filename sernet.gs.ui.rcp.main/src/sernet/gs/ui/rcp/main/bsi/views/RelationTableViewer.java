/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak[at]sernet[dot]de>.
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

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author koderman[at]sernet[dot]de
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
    private TableViewerColumn col5;
    private TableViewerColumn col6;
    private TableViewerColumn col7;
    
	/**
	 * @param parent
	 * @param i
	 */
	public RelationTableViewer(IRelationTable relationView, Composite parent, int style, boolean showRisk) {
		super(parent, style);
		this.view = relationView;
		
		Table table = getTable();
		
		// relation icon:
		col1 = new TableColumn(table, SWT.LEFT);
		col1.setText(""); //$NON-NLS-1$
		col1.setWidth(25);
		col1.setResizable(false);
		
		// name of relation: (i.e. "author of")
		viewerCol2 = new TableViewerColumn(this, SWT.LEFT);
		viewerCol2.getColumn().setText(Messages.RelationTableViewer_1);
		viewerCol2.getColumn().setWidth(150);
		
//		viewerCol2.setLabelProvider(new ColumnLabelProvider() {
//			public String getText(Object obj) {
//				if (!(obj instanceof CnALink))
//					return ""; //$NON-NLS-1$
//				
//				CnALink link = (CnALink) obj;
//				HuiRelation relation = HitroUtil.getInstance().getTypeFactory().getRelation(link.getRelationId());
//
//				// if we can't find a real name for the relation, we just display "depends on" or "necessary for":
//					if (CnALink.isDownwardLink(view.getInputElmt(), link))
//						return (relation != null) ? relation.getName() : Messages.RelationTableViewer_3;
//					else
//						return (relation != null) ? relation.getReversename() : Messages.RelationTableViewer_4;
//			}
//		});
		viewerCol2.setEditingSupport(new RelationTypeEditingSupport(view, this));

		// element type icon:
		col3 = new TableColumn(table, SWT.CENTER);
		col3.setText(""); //$NON-NLS-1$
		col3.setWidth(25);
		
		// element title:
		col4 = new TableColumn(table, SWT.LEFT);
		col4.setText(Messages.RelationTableViewer_6);
		col4.setWidth(200);
		
		// risk avalues if requested:
		if (showRisk) {
		    col5 = new TableViewerColumn(this, SWT.LEFT);
		    col5.getColumn().setText("C"); //$NON-NLS-1$
		    col5.getColumn().setWidth(25);
//		    col5.setLabelProvider(new RiskLabelProver('C'));
		    
		    col6 = new TableViewerColumn(this, SWT.LEFT);
		    col6.getColumn().setText("I"); //$NON-NLS-1$
		    col6.getColumn().setWidth(25);
//		    col6.setLabelProvider(new RiskLabelProver('I'));
		    
		    col7 = new TableViewerColumn(this, SWT.LEFT);
		    col7.getColumn().setText("A"); //$NON-NLS-1$
		    col7.getColumn().setWidth(25);
//		    col7.setLabelProvider(new RiskLabelProver('A'));
		}
		
		
		setColumnProperties(new String[] {
				IRelationTable.COLUMN_IMG, //$NON-NLS-1$
				IRelationTable.COLUMN_TYPE, //$NON-NLS-1$
				IRelationTable.COLUMN_TYPE_IMG, //$NON-NLS-1$
				IRelationTable.COLUMN_TITLE, //$NON-NLS-1$
				IRelationTable.COLUMN_RISK_C, //$NON-NLS-1$
				IRelationTable.COLUMN_RISK_I, //$NON-NLS-1$
				IRelationTable.COLUMN_RISK_A //$NON-NLS-1$
		});
		
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

	}
}