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

import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot.ISelectionListener;
import sernet.gs.ui.rcp.main.bsi.views.chart.ChartView;
import sernet.gs.ui.rcp.main.bsi.views.chart.IChartGenerator;
import sernet.gs.ui.rcp.main.bsi.views.chart.Messages;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IISO27KModelListener;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.samt.SamtTopic;
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
        return samtProgressChart;
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

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.bsi.views.chart.ChartView#setDescription()
     */
    protected void setDescription() {
        // no description
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
        
        //if(showChartForSelection(selectedElement)) {
        ControlGroup group = getChartControlGroup(selectedElement);
        if(group!=null) {
            if (this.element != null && selectedElement == this.element) {
                return;
            }
            this.element = group;
            drawChart();
        }
    }
    
    private ControlGroup getChartControlGroup(CnATreeElement selection) {
        ControlGroup group = null;
        if(isControlType(selection)) {
           if(Audit.TYPE_ID.equals(selection.getParent().getTypeId())
              && ControlGroup.TYPE_ID.equals(selection.getTypeId())) {
               group = (ControlGroup) selection; 
           } else {
               group = getChartControlGroup(selection.getParent()); 
           }
        } else if(selection!=null && Audit.TYPE_ID.equals(selection.getTypeId())) {
            group = getControlGroup(selection);
        }
        return group;
    }
    
    private ControlGroup getControlGroup(CnATreeElement selfAssessment) {
        ControlGroup controlGroup = null;
        Set<CnATreeElement> elementSet = selfAssessment.getChildren();
        for (Iterator<CnATreeElement> iterator = elementSet.iterator(); iterator.hasNext();) {
            CnATreeElement element = iterator.next();
            if (element instanceof ControlGroup) {
                controlGroup = (ControlGroup) element;
                break;
            }
        }
        return controlGroup;
    }

    /**
     * @param selection
     * @return
     */
    private boolean isControlType(CnATreeElement selection) {
        return( selection!=null &&
                (ControlGroup.TYPE_ID.equals(selection.getTypeId()) ||
                 Control.TYPE_ID.equals(selection.getTypeId()) ||
                 SamtTopic.TYPE_ID.equals(selection.getTypeId()))
        );
    }

    /**
     * Returns true if selection is a ControlGroup
     * and if at least one of its children is a ControlGroup too.
     * 
     * @param selection a RCP-GUI selection from {@link ISelectionListener}
     * @return true if a chart should be displayed for this selection
     */
    private boolean showChartForSelection(CnATreeElement selection) {
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
    
    protected ChartView.ChangeListener createChangeListener() {
        return new SamtChartListener();
    }

    class SamtChartListener extends ChartView.ChangeListener implements IBSIModelListener,IISO27KModelListener {
       
        /* (non-Javadoc)
         * @see sernet.verinice.model.iso27k.IISO27KModelListener#childAdded(sernet.verinice.model.common.CnATreeElement, sernet.verinice.model.common.CnATreeElement)
         */
        @Override
        public void childAdded(CnATreeElement category, CnATreeElement child) { 
            // call drawChart() if user can change the ISA somedays
        }
    
        /* (non-Javadoc)
         * @see sernet.verinice.model.iso27k.IISO27KModelListener#childChanged(sernet.verinice.model.common.CnATreeElement, sernet.verinice.model.common.CnATreeElement)
         */
        @Override
        public void childChanged(CnATreeElement category, CnATreeElement child) {
            drawChart();     
        }
    
        /* (non-Javadoc)
         * @see sernet.verinice.model.iso27k.IISO27KModelListener#childRemoved(sernet.verinice.model.common.CnATreeElement, sernet.verinice.model.common.CnATreeElement)
         */
        @Override
        public void childRemoved(CnATreeElement category, CnATreeElement child) {  
            // call drawChart() if user can change the ISA somedays
        }
    
        /* (non-Javadoc)
         * @see sernet.verinice.model.iso27k.IISO27KModelListener#databaseChildAdded(sernet.verinice.model.common.CnATreeElement)
         */
        @Override
        public void databaseChildAdded(CnATreeElement child) { 
            // call drawChart() if user can change the ISA somedays
        }
    
        /* (non-Javadoc)
         * @see sernet.verinice.model.iso27k.IISO27KModelListener#databaseChildChanged(sernet.verinice.model.common.CnATreeElement)
         */
        @Override
        public void databaseChildChanged(CnATreeElement child) {
            drawChart();   
        }
    
        /* (non-Javadoc)
         * @see sernet.verinice.model.iso27k.IISO27KModelListener#databaseChildRemoved(sernet.verinice.model.common.CnATreeElement)
         */
        @Override
        public void databaseChildRemoved(CnATreeElement child) { 
            // call drawChart() if user can change the ISA somedays
        }
    
        /* (non-Javadoc)
         * @see sernet.verinice.model.iso27k.IISO27KModelListener#databaseChildRemoved(sernet.verinice.model.common.ChangeLogEntry)
         */
        @Override
        public void databaseChildRemoved(ChangeLogEntry entry) {
            // call drawChart() if user can change the ISA somedays
        }
    
        /* (non-Javadoc)
         * @see sernet.verinice.model.iso27k.IISO27KModelListener#linkAdded(sernet.verinice.model.common.CnALink)
         */
        @Override
        public void linkAdded(CnALink link) {
    
        }
    
        /* (non-Javadoc)
         * @see sernet.verinice.model.iso27k.IISO27KModelListener#linkChanged(sernet.verinice.model.common.CnALink, sernet.verinice.model.common.CnALink, java.lang.Object)
         */
        @Override
        public void linkChanged(CnALink old, CnALink link, Object source) {       
        }
    
        /* (non-Javadoc)
         * @see sernet.verinice.model.iso27k.IISO27KModelListener#linkRemoved(sernet.verinice.model.common.CnALink)
         */
        @Override
        public void linkRemoved(CnALink link) {   
        }
    
        /* (non-Javadoc)
         * @see sernet.verinice.model.iso27k.IISO27KModelListener#modelRefresh(java.lang.Object)
         */
        @Override
        public void modelRefresh(Object object) {
            drawChart();
        }
    
        /* (non-Javadoc)
         * @see sernet.verinice.model.iso27k.IISO27KModelListener#modelReload(sernet.verinice.model.iso27k.ISO27KModel)
         */
        @Override
        public void modelReload(ISO27KModel newModel) {
            drawChart();
        }

        /* (non-Javadoc)
         * @see sernet.verinice.model.bsi.IBSIModelListener#modelRefresh()
         */
        @Override
        public void modelRefresh() {
            drawChart();
        }

        /* (non-Javadoc)
         * @see sernet.verinice.model.bsi.IBSIModelListener#modelReload(sernet.verinice.model.bsi.BSIModel)
         */
        @Override
        public void modelReload(BSIModel newModel) {
            drawChart();
        }
    }
}
    
