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

import java.awt.MouseInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.service.commands.LoadAncestors;

/**
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class RelationTableViewer extends TableViewer {

    private static final Logger LOG = Logger.getLogger(RelationTableViewer.class);

    private TableViewerColumn col6;
    private TableViewerColumn col7;
    private TableViewerColumn col8;

    private final TableViewerColumn col1;
    private final TableViewerColumn viewerCol2;
    private final TableViewerColumn col4;
    private final IRelationTable view;
    private final TableViewerColumn col3;
    private final TableViewerColumn viewerCol5;
    private final TableViewerColumn col5;
    
    private TableViewerColumn colRiskTreatment;
    private TableViewerColumn colCWithControls;
    private TableViewerColumn colIWithControls;
    private TableViewerColumn colAWithControls;

    /**
     * @param parent
     * @param i
     */
    public RelationTableViewer(IRelationTable relationView, Composite parent, int style, boolean showRisk) {
        super(parent, style);

        final int defaultColumnWidth = 30;
        final int viewerCol2Width = 100;
        final int col4Width = 150;
        final int col5Width = 100;
        final int viewerCol5Width = 250;
        final int colWithRiskTreatment = 130;

        ColumnViewerToolTipSupport.enableFor(this, ToolTip.NO_RECREATE);

        view = relationView;

        Table table = getTable();

        // relation icon:
        col1 = new TableViewerColumn(this, SWT.LEFT);
        col1.getColumn().setText(""); //$NON-NLS-1$
        col1.getColumn().setWidth(defaultColumnWidth);
        col1.getColumn().setResizable(false);

        // name of relation: (i.e. "author of")
        viewerCol2 = new TableViewerColumn(this, SWT.LEFT);
        viewerCol2.getColumn().setText(Messages.RelationTableViewer_1);
        viewerCol2.getColumn().setWidth(viewerCol2Width);

        viewerCol2.setEditingSupport(new RelationTypeEditingSupport(view, this));

        // element type icon:
        col3 = new TableViewerColumn(this, SWT.CENTER);
        col3.getColumn().setText(""); //$NON-NLS-1$
        col3.getColumn().setWidth(defaultColumnWidth);

        // element title
        col4 = new TableViewerColumn(this, SWT.LEFT);
        col4.getColumn().setText(Messages.RelationTableViewer_6);
        col4.getColumn().setWidth(col4Width);

        // element scope id:
        col5 = new TableViewerColumn(this, SWT.LEFT);
        col5.getColumn().setText(Messages.RelationTableViewer_5);
        col5.getColumn().setWidth(col5Width);

        viewerCol5 = new TableViewerColumn(this, SWT.LEFT);
        viewerCol5.getColumn().setText(Messages.RelationTableViewer_7);
        viewerCol5.getColumn().setWidth(viewerCol5Width);
        viewerCol5.setEditingSupport(new RelationDescriptionEditingSupport(view, this));

        // risk avalues if requested:
        if (showRisk) {
            colRiskTreatment = new TableViewerColumn(this, SWT.LEFT);
            colRiskTreatment.getColumn().setText(Messages.RelationTableViewer_0);
            colRiskTreatment.getColumn().setWidth(colWithRiskTreatment);
            colRiskTreatment.setEditingSupport(new RiskTreatmentEditingSupport(view, this));
                      
            col6 = new TableViewerColumn(this, SWT.LEFT);
            col6.getColumn().setText("C"); //$NON-NLS-1$
            col6.getColumn().setWidth(defaultColumnWidth);

            col7 = new TableViewerColumn(this, SWT.LEFT);
            col7.getColumn().setText("I"); //$NON-NLS-1$
            col7.getColumn().setWidth(defaultColumnWidth);

            col8 = new TableViewerColumn(this, SWT.LEFT);
            col8.getColumn().setText("A"); //$NON-NLS-1$
            col8.getColumn().setWidth(defaultColumnWidth);
            
            colCWithControls = new TableViewerColumn(this, SWT.LEFT);
            colCWithControls.getColumn().setText(Messages.RelationTableViewer_2);
            colCWithControls.getColumn().setWidth(defaultColumnWidth);

            colIWithControls = new TableViewerColumn(this, SWT.LEFT);
            colIWithControls.getColumn().setText(Messages.RelationTableViewer_8); 
            colIWithControls.getColumn().setWidth(defaultColumnWidth);

            colAWithControls = new TableViewerColumn(this, SWT.LEFT);
            colAWithControls.getColumn().setText(Messages.RelationTableViewer_9); 
            colAWithControls.getColumn().setWidth(defaultColumnWidth);
        }

        setColumnProperties(new String[] { IRelationTable.COLUMN_IMG, 
                IRelationTable.COLUMN_TYPE, 
                IRelationTable.COLUMN_TYPE_IMG, 
                IRelationTable.COLUMN_SCOPE_ID, 
                IRelationTable.COLUMN_TITLE, 
                IRelationTable.COLUMN_COMMENT, 
                IRelationTable.COLUMN_RISK_TREATMENT,
                IRelationTable.COLUMN_RISK_C, 
                IRelationTable.COLUMN_RISK_I, 
                IRelationTable.COLUMN_RISK_A,
                IRelationTable.COLUMN_RISK_C_CONTROLS, 
                IRelationTable.COLUMN_RISK_I_CONTROLS, 
                IRelationTable.COLUMN_RISK_A_CONTROLS 
        });

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

    }
    
    /**
     * Provides an object path of the linked object in the title column in the
     * relation view.
     */
    public static class InfoCellLabelProvider extends CellLabelProvider {

        static final Map<Integer, String> TOOLTIPS;
        static {
            TOOLTIPS = new HashMap<>();
            TOOLTIPS.put(7, Messages.RelationTableViewer_C);
            TOOLTIPS.put(8, Messages.RelationTableViewer_I);
            TOOLTIPS.put(9, Messages.RelationTableViewer_A);
            TOOLTIPS.put(10, Messages.RelationTableViewer_C_with_controls);
            TOOLTIPS.put(11, Messages.RelationTableViewer_I_with_controls);
            TOOLTIPS.put(12, Messages.RelationTableViewer_A_with_Controls);
        }
        
        RelationViewLabelProvider relationViewLabelProvider;
        
        
        /** current column index */
        private int column;
        
        public InfoCellLabelProvider(RelationViewLabelProvider relationViewLabelProvider, int column) {
            this.relationViewLabelProvider = relationViewLabelProvider;
            this.column = column;
           
        }
        
        @Override
        public void update(ViewerCell cell) {
            cell.setText(relationViewLabelProvider.getColumnText(cell.getElement(), column));      
        }
        
        @Override
        public String getToolTipText(final Object element) {
            String tooltip = TOOLTIPS.get(column);
            if (tooltip == null) {
                tooltip = ""; //$NON-NLS-1$
            }     
            return tooltip;
        }
        
    }

    /**
     * Provides an object path of the linked object in the title column in the
     * relation view.
     */
    public static class PathCellLabelProvider extends CellLabelProvider {

        /**
         * Caches the object pathes. Key is the title of the target
         * CnaTreeElement which is listed in the title column.
         */
        private Map<String, String> cache;

        /** the current width of the verinice window */
        private int shellWidth;

        /** current x value of the verinice window */
        private int shellX;

        /** current parent element wich contained the table */
        Composite parent;

        /** current column index */
        private int column;

        private GC gc;

        private FontMetrics fmt;

        private int charWidth;

        RelationViewLabelProvider relationViewLabelProvider;

        public PathCellLabelProvider(RelationViewLabelProvider relationViewLabelProvider, Composite parent, int column) {
            this.relationViewLabelProvider = relationViewLabelProvider;
            this.parent = parent;
            this.column = column;
            cache = new HashMap<>();

            // calc text width
            this.gc = new GC(parent);
            this.fmt = gc.getFontMetrics();
            this.charWidth = fmt.getAverageCharWidth();
        }

        @Override
        public String getToolTipText(final Object element) {

            if (!(element instanceof CnALink)) {
                return ""; //$NON-NLS-1$
            }

            CnALink link = (CnALink) element;

            // the mouse location must be tracked, before we make "slow" remote
            // calls"
            int mouseX = MouseInfo.getPointerInfo().getLocation().x;
            LOG.debug("mouse location: " + mouseX); //$NON-NLS-1$

            CnATreeElement cnATreeElement;
            boolean isDownwardLink = CnALink.isDownwardLink(relationViewLabelProvider.getInputElemt(), link);
            if (isDownwardLink) {
                cnATreeElement = link.getDependency();

            } else {
                cnATreeElement = link.getDependant();
            }

            if (cache.containsKey(link.getId())) {
                return cropToolTip(cache.get(link.getId().toString() + isDownwardLink), mouseX);
            }

            try {

                RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
                relationViewLabelProvider.replaceLinkEntities(link);

                LoadAncestors command = new LoadAncestors(cnATreeElement.getTypeId(), cnATreeElement.getUuid(), ri);
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                CnATreeElement current = command.getElement();

                // build object path
                StringBuilder sb = new StringBuilder();
                sb.insert(0, current.getTitle());

                while (current.getParent() != null) {
                    current = current.getParent();
                    sb.insert(0, "/"); //$NON-NLS-1$
                    sb.insert(0, current.getTitle());
                }



                // crop the root element, which is always ISO .. or BSI ...
                String[] p = sb.toString().split("/"); //$NON-NLS-1$
                sb = new StringBuilder();
                for (int i = 1; i < p.length; i++) {
                    sb.append("/").append(p[i]); //$NON-NLS-1$
                }
                
                // delete root slash
                sb.deleteCharAt(0);

                cache.put(link.getId().toString() + isDownwardLink, sb.toString());

            } catch (CommandException e) {
                LOG.debug("loading ancestors failed", e); //$NON-NLS-1$
            }

            return cropToolTip(cache.get(link.getId().toString() + isDownwardLink), mouseX);
        }

        /**
         * Since rcp does not display strings, which are wider than the
         * {@link Shell} width, we have to calculate the font metric and maybe
         * to crop the string.
         * 
         * @param toolTipText
         *            the raw tooltiptext with the complete path.
         * 
         * @param mouseX
         *            current x value of the mouse position on the whole
         *            display, multi displays are taken into account
         * 
         * @return if the tooltip text is wider the {@link Shell} elements from
         *         the end are removed until the text is short enough.
         */
        public String cropToolTip(String toolTipText, int mouseX) {

            // FIXME should be done once in constructor

            // crop
            int spaceLeft = shellWidth - (Math.abs(shellX - mouseX));
            if (charWidth * toolTipText.length() >= spaceLeft - ("...".length() * charWidth)) { //$NON-NLS-1$

                String[] p = toolTipText.split("/"); //$NON-NLS-1$
                StringBuilder sb = new StringBuilder();

                // avoid infinite loop, since this path cannot be cropped
                if (p.length == 1) {
                    return toolTipText;
                }

                for (int i = 0; i < p.length - 1; i++) {
                    sb.append("/").append(p[i]); //$NON-NLS-1$
                }

                // delete root slash
                sb.deleteCharAt(0);

                // check again, if short enough
                return cropToolTip(sb.toString() + " ...", mouseX); //$NON-NLS-1$
            }

            return toolTipText;
        }

        @Override
        public void update(ViewerCell cell) {
            // delegate the cell text to the origin label provider
            switch (column) {
            case 0:
                cell.setImage(relationViewLabelProvider.getColumnImage(cell.getElement(), column));
                break;
            case 2:
                cell.setImage(relationViewLabelProvider.getColumnImage(cell.getElement(), column));
                break;
            case 3:
                cell.setText(relationViewLabelProvider.getColumnText(cell.getElement(), column));
            }

        }

        public void updateShellWidthAndX(int shellWidth, int shellX) {
            this.shellWidth = shellWidth;
            this.shellX = shellX;
        }

    }

    /**
     * Provide tool tips for column 1,3 and 4 for the relation table view.
     * 
     * @param relationViewLabelProvider
     *            The ordinary column provider
     * @param parent
     *            The {@link Composite} which contains the table.
     * @return A List of all initiated cell label provider.
     */
    public List<PathCellLabelProvider> initToolTips(RelationViewLabelProvider relationViewLabelProvider, Composite parent) {

        List<PathCellLabelProvider> relTblCellLabelProv = new ArrayList<>();

        PathCellLabelProvider relTableCellProviderCol1 = new PathCellLabelProvider(relationViewLabelProvider, parent, 0);
        relTblCellLabelProv.add(relTableCellProviderCol1);
        col1.setLabelProvider(relTableCellProviderCol1);

        PathCellLabelProvider relTableCellProviderCol3 = new PathCellLabelProvider(relationViewLabelProvider, parent, 2);
        relTblCellLabelProv.add(relTableCellProviderCol3);
        col3.setLabelProvider(relTableCellProviderCol3);

        PathCellLabelProvider relTableCellProviderCol4 = new PathCellLabelProvider(relationViewLabelProvider, parent, 3);
        relTblCellLabelProv.add(relTableCellProviderCol4);
        col4.setLabelProvider(relTableCellProviderCol4);
        
        if (this.getTable().getColumnCount() > 7) {
            col6.setLabelProvider(new InfoCellLabelProvider(relationViewLabelProvider, 7));
            col7.setLabelProvider(new InfoCellLabelProvider(relationViewLabelProvider, 8));
            col8.setLabelProvider(new InfoCellLabelProvider(relationViewLabelProvider, 9));
            colCWithControls.setLabelProvider(new InfoCellLabelProvider(relationViewLabelProvider, 10));
            colIWithControls.setLabelProvider(new InfoCellLabelProvider(relationViewLabelProvider, 11));
            colAWithControls.setLabelProvider(new InfoCellLabelProvider(relationViewLabelProvider, 12));
        }
        return relTblCellLabelProv;
    }
    
    public static boolean isAssetAndSzenario(CnALink link) {
        try {
            CnATreeElement dependant = link.getDependant();
            CnATreeElement dependency = link.getDependency();
            return (Asset.TYPE_ID.equals(dependant.getTypeId()) && IncidentScenario.TYPE_ID.equals(dependency.getTypeId()))
                   || (Asset.TYPE_ID.equals(dependency.getTypeId()) && IncidentScenario.TYPE_ID.equals(dependant.getTypeId()));
        } catch(Exception e) {
            LOG.error("Error while checking link.", e); //$NON-NLS-1$
            return false;
        }
    }
}