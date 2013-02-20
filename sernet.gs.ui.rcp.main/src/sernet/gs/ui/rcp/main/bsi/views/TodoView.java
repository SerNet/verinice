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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenSiegelFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenUmsetzungFilter;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.bsi.views.actions.TodoViewFilterAction;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;

/**
 * Shows controls that still have to be implemented.
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */
public class TodoView extends GenericMassnahmenView {

    public static final String ID = "sernet.gs.ui.rcp.main.bsi.views." + "todoview"; //$NON-NLS-1$

    private TableSorter tableSorter = new TableSorter();

    @Override
    protected void createPartControlImpl(Composite parent) {
        Table table = getViewer().getTable();

        final int iconColumnWidth = 25;
        final int dateColumnWidth = 200;
        final int bearbeiterColumnWidth = 100;
        final int siegelColumnWidth = 20;
        final int zielColumnWidth = 150;
        final int titleColumnWidth = 250;
        
        iconColumn = new TableColumn(table, SWT.LEFT);
        iconColumn.setText(" "); //$NON-NLS-1$
        iconColumn.setWidth(iconColumnWidth);
        iconColumn.addSelectionListener(new SortSelectionAdapter(this, iconColumn, 0));

        dateColumn = new TableColumn(table, SWT.LEFT);
        dateColumn.setText(Messages.TodoView_8);
        dateColumn.setWidth(dateColumnWidth);
        dateColumn.addSelectionListener(new SortSelectionAdapter(this, dateColumn, 1));

        bearbeiterColumn = new TableColumn(table, SWT.LEFT);
        bearbeiterColumn.setText(Messages.TodoView_9);
        bearbeiterColumn.setWidth(bearbeiterColumnWidth);
        bearbeiterColumn.addSelectionListener(new SortSelectionAdapter(this, bearbeiterColumn, 2));

        siegelColumn = new TableColumn(table, SWT.LEFT);
        siegelColumn.setText(Messages.TodoView_10);
        siegelColumn.setWidth(siegelColumnWidth);
        siegelColumn.addSelectionListener(new SortSelectionAdapter(this, siegelColumn, 3));

        zielColumn = new TableColumn(table, SWT.LEFT);
        zielColumn.setText(Messages.TodoView_11);
        zielColumn.setWidth(zielColumnWidth);
        zielColumn.addSelectionListener(new SortSelectionAdapter(this, zielColumn, 4));

        titleColumn = new TableColumn(table, SWT.LEFT);
        titleColumn.setText(Messages.TodoView_12);
        titleColumn.setWidth(titleColumnWidth);
        titleColumn.addSelectionListener(new SortSelectionAdapter(this, titleColumn, 5));

        getViewer().setColumnProperties(new String[] { "_icon", //$NON-NLS-1$
                "_date", //$NON-NLS-1$
                "_bearbeiter", //$NON-NLS-1$
                "_siegel", //$NON-NLS-1$
                "_ziel", //$NON-NLS-1$
                "_title" //$NON-NLS-1$
        });

        table.setHeaderVisible(true);
        table.setLinesVisible(true);
    }

    protected Action createFilterAction(MassnahmenUmsetzungFilter umsetzungFilter, MassnahmenSiegelFilter siegelFilter) {
        return new TodoViewFilterAction(this,getViewer(), Messages.TodoView_2, umsetzungFilter, siegelFilter);

    }

    protected String[] getUmsetzungPattern() {
        return new String[] { MassnahmenUmsetzung.P_UMSETZUNG_NEIN, MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE, MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET };
    }

    @Override
    protected String getMeasureLoadJobLabel() {
        return Messages.TodoView_13;
    }

    @Override
    protected ILabelProvider createLabelProvider() {
        return new TodoLabelProvider();
    }

    @Override
    protected String getMeasureLoadPlaceholderLabel() {
        return Messages.TodoView_14;
    }

    @Override
    protected TableSorter createSorter() {
        return tableSorter;
    }

    @Override
    protected String getTaskErrorLabel() {
        return Messages.TodoView_15;
    }

    @Override
    protected String getMeasureLoadTaskLabel() {
        return Messages.TodoView_14;
    }
    
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.bsi.views.GenericMassnahmenView#getSortByProperty()
     */
    @Override
    protected String getSortByProperty() {
        return MassnahmenUmsetzung.P_UMSETZUNGBIS;
    }
    
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.bsi.views.GenericMassnahmenView#getSortByProperty()
     */
    @Override
    protected String getDateProperty() {
        return MassnahmenUmsetzung.P_UMSETZUNGBIS;
    }

    private static class TodoLabelProvider extends LabelProvider implements ITableLabelProvider {

        private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy, EE"); //$NON-NLS-1$

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
            final int fullTextColumnIndex = 5;
            if (element instanceof PlaceHolder) {
                PlaceHolder ph = (PlaceHolder) element;
                if (columnIndex == fullTextColumnIndex) {       
                    return ph.getTitle();
                } else if(columnIndex>0) {
                    return ph.getTitleShort();
                }
                return "";
            }

            TodoViewItem mn = (TodoViewItem) element;
            switch (columnIndex) {
            case 0: // icon
                return ""; //$NON-NLS-1$
            case 1: // date
                Date date = mn.getUmsetzungBis();
                if (date == null){
                    return Messages.TodoView_3;
                }
                return dateFormat.format(date);
            case 2: // bearbeiter
                return mn.getUmsetzungDurch();
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
}
