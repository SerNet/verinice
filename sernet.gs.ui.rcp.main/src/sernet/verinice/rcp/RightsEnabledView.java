/*******************************************************************************
 * Copyright (c) 2014 Daniel Murygin.
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
package sernet.verinice.rcp;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;

/**
 * Abstract base class for rights enabled views.
 * RightsEnabledView checks rights whenever this view is getting visible. 
 * View is closed if user is not allowed to open this view.
 *
 * If you extend this class and overwrite createPartControl 
 * call super.createPartControl(parent).
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class RightsEnabledView extends ViewPart implements IPartListener2 {

    private static final Logger LOG = Logger.getLogger(RightsEnabledView.class);
    
    protected void openingDeclined() { 
        LOG.error("Opening of view is now allowed, view-id: " + getViewId() + ", action-id: " + getRightID());      
        if (LOG.isDebugEnabled()) {
            LOG.debug("Stacktrace: ", new RuntimeException());
        }
        closeAllViews(this.getClass());
        dispose();        
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        if (!Activator.getDefault().isStandalone() && !checkRights()) {
            final IWorkbenchWindow workbenchWindow = getSite().getWorkbenchWindow();
            workbenchWindow.getPartService().addPartListener(this);
            return;
        }
    }
    
    private boolean checkRights() {
        Activator.inheritVeriniceContextState();
        RightsServiceClient service = (RightsServiceClient) VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(getRightID());
    }

    /**
     * @return The action-right-id of this view
     */
    public abstract String getRightID();
    
    /**
     * @return The id of this view
     */
    public abstract String getViewId();
    
    /**
     * Closes all instances of a given view type.
     * 
     * <p>Searches for all views of a given type in all workbench windows and
     * closes them.</p>
     * 
     * @param viewType The view class to close
     * @see IWorkbenchPage#hideView(org.eclipse.ui.IViewReference)
     */
    @SuppressWarnings("rawtypes")
    private static void closeAllViews(final Class viewType) {
        final IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();

        // for all workbench windows
        for (int w = 0; w < windows.length; w++) {
            final IWorkbenchPage[] pages = windows[w].getPages();

            // for all workbench pages
            // of a given workbench window
            for (int p = 0; p < pages.length; p++) {
                final IWorkbenchPage page = pages[p];
                final IViewReference[] viewRefs = page.getViewReferences();

                // for all view references
                // of a given workbench page
                // of a given workbench window
                for (int v = 0; v < viewRefs.length; v++) {
                    final IViewReference viewRef = viewRefs[v];
                    final IWorkbenchPart viewPart = viewRef.getPart(false);
                    final Class partType = (viewPart != null) ? viewPart.getClass() : null;

                    if (viewType == null || viewType.equals(partType)) {
                        try {
                            page.hideView(viewRef);
                        } catch(Exception e) {
                            LOG.warn("Exception while closing view."); //$NON-NLS-1$
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Stacktrace: ", e); //$NON-NLS-1$
                            }
                        } 
                    }
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
     */
    @Override
    public void partVisible(IWorkbenchPartReference partRef) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("partVisible: " + partRef.getId()); //$NON-NLS-1$
        }
        if(getViewId().equals(partRef.getId())) {
            if(!isServerRunning()){
                IInternalServerStartListener listener = new IInternalServerStartListener(){
                    @Override
                    public void statusChanged(InternalServerEvent e) {
                        if(e.isStarted()){
                            checkAndDecline();
                        }
                    }

                };
                Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
            } else {
                checkAndDecline();
            }         
        }
    }

    private void checkAndDecline() {
        if (!checkRights()) {         
            openingDeclined();
        }
    }
       
    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
     */
    @Override
    public void partActivated(IWorkbenchPartReference partRef) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("partActivated: " + partRef.getId()); //$NON-NLS-1$
        }
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFocus..."); //$NON-NLS-1$
        }
    }

    

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
     */
    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("partBroughtToTop: " + partRef.getId()); //$NON-NLS-1$
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
     */
    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("partClosed: " + partRef.getId()); //$NON-NLS-1$
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
     */
    @Override
    public void partDeactivated(IWorkbenchPartReference partRef) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("partDeactivated: " + partRef.getId()); //$NON-NLS-1$
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
     */
    @Override
    public void partOpened(IWorkbenchPartReference partRef) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("partOpened: " + partRef.getId()); //$NON-NLS-1$
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
     */
    @Override
    public void partHidden(IWorkbenchPartReference partRef) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("partHidden: " + partRef.getId()); //$NON-NLS-1$
        }
    }



    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
     */
    @Override
    public void partInputChanged(IWorkbenchPartReference partRef) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("partInputChanged: " + partRef.getId()); //$NON-NLS-1$
        }
    }
    
    /**
     * @return false if operation mode is standalone and internal server is not running
     */
    protected boolean isServerRunning() {
        return !(Activator.getDefault().isStandalone()  && !Activator.getDefault().getInternalServer().isRunning());
    }

}
