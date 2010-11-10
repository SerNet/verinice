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
 *     Robert Schuster <r.schuster@tarent.de> - reworked to use common base class
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenSiegelFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenUmsetzungFilter;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.bsi.views.GenericMassnahmenView.SortSelectionAdapter;
import sernet.gs.ui.rcp.main.bsi.views.actions.AuditViewFilterAction;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;

/**
 * Shows implemented controls to be reviewed by the auditor.
 * 
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class AuditView extends GenericMassnahmenView {
	public static final String ID = "sernet.gs.ui.rcp.main.bsi.views.auditview"; //$NON-NLS-1$

	AuditTableSorter tableSorter = new AuditTableSorter();
	
	@Override
	protected void createPartControlImpl(Composite parent) {
		Table table = viewer.getTable();
		
		iconColumn = new TableColumn(table, SWT.LEFT);
		iconColumn.setText(" "); //$NON-NLS-1$
		iconColumn.setWidth(25);
		iconColumn.addSelectionListener(new SortSelectionAdapter(this,iconColumn,0));
		
		dateColumn = new TableColumn(table, SWT.LEFT);
		dateColumn.setText(Messages.AuditView_8);
		dateColumn.setWidth(200);
		dateColumn.addSelectionListener(new SortSelectionAdapter(this,dateColumn,1));
		
		bearbeiterColumn = new TableColumn(table, SWT.LEFT);
		bearbeiterColumn.setText(Messages.AuditView_9);
		bearbeiterColumn.setWidth(100);
		bearbeiterColumn.addSelectionListener(new SortSelectionAdapter(this,bearbeiterColumn,2));
		
		siegelColumn = new TableColumn(table, SWT.LEFT);
		siegelColumn.setText(Messages.AuditView_10);
		siegelColumn.setWidth(20);
		siegelColumn.addSelectionListener(new SortSelectionAdapter(this,siegelColumn,3));
		
		zielColumn = new TableColumn(table, SWT.LEFT);
		zielColumn.setText(Messages.AuditView_11);
		zielColumn.setWidth(150);
		zielColumn.addSelectionListener(new SortSelectionAdapter(this,zielColumn,4));
		
		titleColumn = new TableColumn(table, SWT.LEFT);
		titleColumn.setText(Messages.AuditView_12);
		titleColumn.setWidth(250);
		titleColumn.addSelectionListener(new SortSelectionAdapter(this,titleColumn,5));
		
		viewer.setColumnProperties(new String[] {
				"_icon", //$NON-NLS-1$
				"_date", //$NON-NLS-1$
				"_bearbeiter", //$NON-NLS-1$
				"_siegel", //$NON-NLS-1$
				"_ziel", //$NON-NLS-1$
				"_title" //$NON-NLS-1$
		});
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}
	
	protected String[] getUmsetzungPattern() {
		return new String[] {
				MassnahmenUmsetzung.P_UMSETZUNG_JA,
				MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH
		};
	}

	@Override
	protected Action createFilterAction(
			MassnahmenUmsetzungFilter umsetzungFilter,
			MassnahmenSiegelFilter siegelFilter) {
		return new AuditViewFilterAction(this,viewer, Messages.AuditView_19,
				umsetzungFilter, siegelFilter);
	}

	@Override
	protected ILabelProvider createLabelProvider() {
		return new AuditLabelProvider();
	}

	@Override
	protected TableSorter createSorter() {
		return tableSorter;
	}

	@Override
	protected String getMeasureLoadJobLabel() {
		return Messages.AuditView_4;
	}

	@Override
	protected String getMeasureLoadPlaceholderLabel() {
		return Messages.AuditView_2;
	}

	@Override
	protected String getTaskErrorLabel() {
		return Messages.AuditView_5;
	}

	@Override
	protected String getMeasureLoadTaskLabel() {
		return "Lade Massnahmen";
	}
	
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.bsi.views.GenericMassnahmenView#getSortByProperty()
     */
    @Override
    protected String getSortByProperty() {
        return MassnahmenUmsetzung.P_NAECHSTEREVISIONAM;
    }
    
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.bsi.views.GenericMassnahmenView#getSortByProperty()
     */
    @Override
    protected String getDateProperty() {
        return MassnahmenUmsetzung.P_NAECHSTEREVISIONAM;
    }
	
	private static class AuditLabelProvider extends LabelProvider implements ITableLabelProvider {

		private SimpleDateFormat dateFormat =  new SimpleDateFormat("dd.MM.yy, EE"); //$NON-NLS-1$
		
		public Image getColumnImage(Object element, int columnIndex) {
			if (element instanceof PlaceHolder) {
				return null;
			}
			
			TodoViewItem mn = (TodoViewItem) element;
			if (columnIndex == 0) {
				return CnAImageProvider.getImage(mn);
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {

			if (element instanceof PlaceHolder) {
				if (columnIndex == 1) {
					PlaceHolder ph = (PlaceHolder) element;
					return ph.getTitle();
				}
				return ""; //$NON-NLS-1$
			}
			
			TodoViewItem mn = (TodoViewItem) element;
			switch(columnIndex) {
			case 0: // icon
				return ""; //$NON-NLS-1$
			case 1: // date
				Date date = mn.getNaechsteRevision();
				if (date == null)
					return Messages.TodoView_3;
				return dateFormat.format(date);
			case 2: // bearbeiter
				return mn.getRevisionDurch();
			case 3: // siegelstufe
				return "" + mn.getStufe(); //$NON-NLS-1$
			case 4: // zielobjekt
				return mn.getParentTitle();
			case 5: // title
				return mn.getTitle();
			}
			return ""; //$NON-NLS-1$
		}
	}
	
	protected class AuditTableSorter extends TableSorter {
	   
	    private final Logger log = Logger.getLogger(AuditView.AuditTableSorter.class);
	    
	     /* (non-Javadoc)
	     * @see sernet.gs.ui.rcp.main.bsi.views.GenericMassnahmenView.TableSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	     */
	    @Override
	    public int compare(Viewer viewer, Object o1, Object o2) {
	        int rc = 0;
            try {
                TodoViewItem mn1 = (TodoViewItem) o1;
                TodoViewItem mn2 = (TodoViewItem) o2;           
                if(o1==null) {
                    if(o2!=null) {
                        rc = 1;
                    }
                } else if(o2==null) {
                    if(o1!=null) {
                        rc = -1;
                    }
                } else {
                    // e1 and e2 != null    
                    switch (propertyIndex) {
                    case 0:
                        rc = sortByString(mn1.getUmsetzung(),mn2.getUmsetzung());
                        break;
                    case 1:
                        rc = sortByDate(mn1.getNaechsteRevision(), mn2.getNaechsteRevision());
                        break;
                    case 2:
                        rc = sortByString(mn1.getRevisionDurch(),mn2.getRevisionDurch());
                        break;
                    case 3:
                        rc = sortByString(String.valueOf(mn1.getStufe()), String.valueOf(mn2.getStufe()));
                        break;
                    case 4:
                        rc = sortByString(mn1.getParentTitle(), mn2.getParentTitle());
                        break;
                    case 5:
                        rc = sortByString(mn1.getTitle(), mn2.getTitle());
                        break;
                    default:
                        rc = 0;
                    }
                }
                // If descending order, flip the direction
                if (direction == DESCENDING) {
                    rc = -rc;
                }
            } catch(Exception e) {
                log.error("Error while sorting elements", e);
            }
            return rc;
	    }
	}

}
