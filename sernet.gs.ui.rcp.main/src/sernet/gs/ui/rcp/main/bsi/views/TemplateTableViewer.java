/*******************************************************************************  
 * Copyright (c) 2016 Viktor Schmidt.  
 *  
 * This program is free software: you can redistribute it and/or   
 * modify it under the terms of the GNU Lesser General Public License   
 * as published by the Free Software Foundation, either version 3   
 * of the License, or (at your option) any later version.  
 * This program is distributed in the hope that it will be useful,      
 * but WITHOUT ANY WARRANTY; without even the implied warranty   
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.    
 * See the GNU Lesser General Public License for more details.  
 *  
 * You should have received a copy of the GNU Lesser General Public License  
 * along with this program.   
 * If not, see <http://www.gnu.org/licenses/>.  
 *   
 * Contributors:  
 *     Viktor Schmidt <vschmidt[at]ckc[dot]de> - initial API and implementation  
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
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.LoadAncestors;

/**
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de> 
 */
public class TemplateTableViewer extends TableViewer {

    private static final Logger LOG = Logger.getLogger(TemplateTableViewer.class);

    private final TableViewerColumn col0;
    private final TableViewerColumn col1;
    private final TableViewerColumn col2;
    private final TableViewerColumn col3;
    private final TableViewerColumn col4;


    public TemplateTableViewer(Composite parent, int style) {
        super(parent, style);

        final int defaultColumnWidth = 30;
        final int col1Width = 250;
        final int col2Width = 250;
        final int col3Width = 250;
        final int col4Width = 250;

        ColumnViewerToolTipSupport.enableFor(this, ToolTip.NO_RECREATE);

        Table table = getTable();

        // element type icon:
        col0 = new TableViewerColumn(this, SWT.CENTER);
        col0.getColumn().setText(""); //$NON-NLS-1$
        col0.getColumn().setWidth(defaultColumnWidth);
        col0.getColumn().setResizable(false);

        // element title
        col1 = new TableViewerColumn(this, SWT.LEFT);
        col1.getColumn().setText(Messages.TemplateTableViewer_1);
        col1.getColumn().setWidth(col1Width);

        // element parent
        col2 = new TableViewerColumn(this, SWT.LEFT);
        col2.getColumn().setText(Messages.TemplateTableViewer_2);
        col2.getColumn().setWidth(col2Width);

        // element scope id:
        col3 = new TableViewerColumn(this, SWT.LEFT);
        col3.getColumn().setText(Messages.TemplateTableViewer_3);
        col3.getColumn().setWidth(col3Width);

        col4 = new TableViewerColumn(this, SWT.LEFT);
        col4.getColumn().setText(Messages.TemplateTableViewer_4);
        col4.getColumn().setWidth(col4Width);

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

    }

    /**
     * Provides an object path of the object in the title column in the template
     * view.
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

        TemplateViewLabelProvider templateViewLabelProvider;

        public PathCellLabelProvider(TemplateViewLabelProvider templateViewLabelProvider, Composite parent, int column) {
            this.templateViewLabelProvider = templateViewLabelProvider;
            this.parent = parent;
            this.column = column;
            cache = new HashMap<>();

            // calc text width
            this.gc = new GC(parent);
            this.fmt = gc.getFontMetrics();
            this.charWidth = fmt.getAverageCharWidth();
        }

        @Override
        public String getToolTipText(final Object obj) {

            if (!(obj instanceof CnATreeElement)) {
                return ""; //$NON-NLS-1$
            }

            CnATreeElement element = (CnATreeElement) obj;

            // the mouse location must be tracked, before we make "slow" remote
            // calls"
            int mouseX = MouseInfo.getPointerInfo().getLocation().x;
            LOG.debug("mouse location: " + mouseX); //$NON-NLS-1$

            if (cache.containsKey(element.getId())) {
                return cropToolTip(cache.get(element.getId().toString()), mouseX);
            }

            try {

                RetrieveInfo ri = RetrieveInfo.getPropertyInstance();

                LoadAncestors command = new LoadAncestors(element.getTypeId(), element.getUuid(), ri);
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

                cache.put(element.getId().toString(), sb.toString());

            } catch (CommandException e) {
                LOG.debug("loading ancestors failed", e); //$NON-NLS-1$
            }

            return cropToolTip(cache.get(element.getId().toString()), mouseX);
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
                cell.setImage(templateViewLabelProvider.getColumnImage(cell.getElement(), column));
                break;
            case 1:
                cell.setText(templateViewLabelProvider.getColumnText(cell.getElement(), column));
                break;
            }

        }

        public void updateShellWidthAndX(int shellWidth, int shellX) {
            this.shellWidth = shellWidth;
            this.shellX = shellX;
        }

    }

    /**
     * Provide tool tips for column 1 and 2 for the template table view.
     * 
     * @param templateViewLabelProvider
     *            The ordinary column provider
     * @param parent
     *            The {@link Composite} which contains the table.
     * @return A List of all initiated cell label provider.
     */
    public List<PathCellLabelProvider> initToolTips(TemplateViewLabelProvider templateViewLabelProvider, Composite parent) {

        List<PathCellLabelProvider> labelProviders = new ArrayList<>();

        PathCellLabelProvider labelProviderCol0 = new PathCellLabelProvider(templateViewLabelProvider, parent, 0);
        labelProviders.add(labelProviderCol0);
        col0.setLabelProvider(labelProviderCol0);

        PathCellLabelProvider labelProviderCol1 = new PathCellLabelProvider(templateViewLabelProvider, parent, 1);
        labelProviders.add(labelProviderCol1);
        col1.setLabelProvider(labelProviderCol1);

        return labelProviders;
    }
}
