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

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class RelationTableViewer extends TableViewer {


    private TableViewerColumn col6;
    private TableViewerColumn col7;
    private TableViewerColumn col8;
   
	/**
	 * @param parent
	 * @param i
	 */
	public RelationTableViewer(IRelationTable relationView, Composite parent, int style, boolean showRisk) {
		super(parent, style);
		
		final int defaultColumnWidth = 25;
		final int viewerCol2Width = 150;
		final int col4Width = 200;
		final int viewerCol5Width = 250;
		
		TableColumn col1;
		TableViewerColumn viewerCol2;
		TableColumn col4;
		IRelationTable view;
		TableColumn col3;
		TableViewerColumn viewerCol5;
		
		view = relationView;
		
		Table table = getTable();
		
		// relation icon:
		col1 = new TableColumn(table, SWT.LEFT);
		col1.setText(""); //$NON-NLS-1$
		col1.setWidth(defaultColumnWidth);
		col1.setResizable(false);
		
		// name of relation: (i.e. "author of")
		viewerCol2 = new TableViewerColumn(this, SWT.LEFT);
		viewerCol2.getColumn().setText(Messages.RelationTableViewer_1);
		viewerCol2.getColumn().setWidth(viewerCol2Width);
		
		viewerCol2.setEditingSupport(new RelationTypeEditingSupport(view, this));

		// element type icon:
		col3 = new TableColumn(table, SWT.CENTER);
		col3.setText(""); //$NON-NLS-1$
		col3.setWidth(defaultColumnWidth);
		
		// element title:
		col4 = new TableColumn(table, SWT.LEFT);
		col4.setText(Messages.RelationTableViewer_6);
		col4.setWidth(col4Width);
		
		viewerCol5 = new TableViewerColumn(this, SWT.LEFT);
        viewerCol5.getColumn().setText(Messages.RelationTableViewer_7);
        viewerCol5.getColumn().setWidth(viewerCol5Width);  
        viewerCol5.setEditingSupport(new RelationDescriptionEditingSupport(view, this));
         
		// risk avalues if requested:
		if (showRisk) {
		    col6 = new TableViewerColumn(this, SWT.LEFT);
		    col6.getColumn().setText("C"); //$NON-NLS-1$
		    col6.getColumn().setWidth(defaultColumnWidth);
		    
		    col7 = new TableViewerColumn(this, SWT.LEFT);
		    col7.getColumn().setText("I"); //$NON-NLS-1$
		    col7.getColumn().setWidth(defaultColumnWidth);
		    
		    col8 = new TableViewerColumn(this, SWT.LEFT);
		    col8.getColumn().setText("A"); //$NON-NLS-1$
		    col8.getColumn().setWidth(defaultColumnWidth);
		}
		
		
		setColumnProperties(new String[] {
				IRelationTable.COLUMN_IMG, //$NON-NLS-1$
				IRelationTable.COLUMN_TYPE, //$NON-NLS-1$
				IRelationTable.COLUMN_TYPE_IMG, //$NON-NLS-1$
				IRelationTable.COLUMN_TITLE, //$NON-NLS-1$
				IRelationTable.COLUMN_COMMENT, //$NON-NLS-1$
				IRelationTable.COLUMN_RISK_C, //$NON-NLS-1$
				IRelationTable.COLUMN_RISK_I, //$NON-NLS-1$
				IRelationTable.COLUMN_RISK_A //$NON-NLS-1$
		});
		
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		

	}
}