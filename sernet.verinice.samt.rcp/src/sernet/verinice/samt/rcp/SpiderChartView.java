/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.samt.rcp;

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot.ISelectionListener;
import sernet.gs.ui.rcp.main.bsi.views.chart.ChartView;
import sernet.gs.ui.rcp.main.bsi.views.chart.IChartGenerator;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.iso27k.model.ControlGroup;
import sernet.verinice.rcp.IAttachedToPerspective;
import sernet.verinice.samt.service.FindSamtGroup;

/**
 * @author Daniel Murygin <dm@sernet.de>
 * 
 */
@SuppressWarnings("restriction")
public class SpiderChartView extends ChartView implements IAttachedToPerspective {

    private static final Logger LOG = Logger.getLogger(SpiderChartView.class);

    public static final String ID = "sernet.verinice.samt.rcp.SpiderChartView"; //$NON-NLS-1$

    private ICommandService commandService;

    public SpiderChartView() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.bsi.views.chart.ChartView#getDefaultChartGenerator
     * ()
     */
    @Override
    protected IChartGenerator getDefaultChartGenerator() {
        return maturitySpiderChart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.bsi.views.chart.ChartView#createMenus()
     */
    @Override
    protected void createMenus() {
        // Toolbar and Viewmenu of SpiderChartView must be empty
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.bsi.views.chart.ChartView#getDefaultElement()
     */
    @Override
    protected CnATreeElement getDefaultElement() {
        FindSamtGroup command = new FindSamtGroup();
        try {
            command = getCommandService().executeCommand(command);
        } catch (RuntimeException e) {
            LOG.error("Error while executing FindSamtGroup command", e); //$NON-NLS-1$
            throw e;
        } catch (Exception e) {
            final String message = "Error while executing FindSamtGroup command"; //$NON-NLS-1$
            LOG.error(message, e);
            throw new RuntimeException(message, e);
        }
        return command.getSelfAssessmentGroup();
    }

    /**
     * Method is called if the selection in the GUI is changed.
     * 
     * @param part
     * @param selection the newly selected GUI element
     * @see sernet.gs.ui.rcp.main.bsi.views.chart.ChartView#pageSelectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    protected synchronized void pageSelectionChanged(IWorkbenchPart part, ISelection selection) {
        if (!(selection instanceof IStructuredSelection)) {
            return;
        }
        Object firstSelection = ((IStructuredSelection) selection).getFirstElement();
        CnATreeElement selectedElement = (CnATreeElement) firstSelection;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Selection changed, selected element: " + selectedElement); //$NON-NLS-1$
        }

        if(showChartForSelection(firstSelection)) {
            if (this.element != null && selectedElement == this.element) {
                return;
            }
            this.element = selectedElement;
            drawChart();
        }
    }

    /**
     * Returns true if selection is a ControlGroup
     * and if at least one of its children is a ControlGroup too.
     * 
     * @param selection a RCP-GUI selection from {@link ISelectionListener}
     * @return true if a chart should be displayed for this selection
     */
    private boolean showChartForSelection(Object selection) {
        boolean showIt = false;
        if(selection!=null && selection instanceof ControlGroup) {
            ControlGroup group = (ControlGroup) selection;
            Set<CnATreeElement> children = group.getChildren();
            if(children!=null) {
                for (CnATreeElement child : children) {
                    if(child!=null && ControlGroup.TYPE_ID.equals(child.getTypeId())) {
                        showIt = true;
                        break;
                    }
                }
            }
        }
        return showIt;
    }

    public ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandServive();
        }
        return commandService;
    }

    private ICommandService createCommandServive() {
        return ServiceFactory.lookupCommandService();
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.IAttachedToPerspective#getPerspectiveId()
     */
    public String getPerspectiveId() {
        return SamtPerspective.ID;
    }
}
