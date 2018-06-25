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

import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;
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

/**
 * Abstract base class for rights enabled views. RightsEnabledView checks rights
 * whenever this view is getting visible. View is closed if user is not allowed
 * to open this view.
 *
 * If you extend this class and overwrite createPartControl call
 * super.createPartControl(parent).
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class RightsEnabledView extends ViewPart {

    private static final Logger LOG = Logger.getLogger(RightsEnabledView.class);

    protected void openingDeclined() {
        LOG.error("Opening of view is not allowed, view-id: " + getViewId() + ", action-id: "
                + getRightID());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Stacktrace: ", new RuntimeException());
        }
        closeAllViews(this.getClass());
        dispose();
    }

    /*
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.
     * widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        if (!Activator.getDefault().isStandalone() && !checkRights()) {
            final IWorkbenchWindow workbenchWindow = getSite().getWorkbenchWindow();
            workbenchWindow.getPartService().addPartListener(new CheckPermissonListener());
        }
    }

    private boolean checkRights() {
        Activator.inheritVeriniceContextState();
        RightsServiceClient service = (RightsServiceClient) VeriniceContext
                .get(VeriniceContext.RIGHTS_SERVICE);
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
     * <p>
     * Searches for all views of a given type in all workbench windows and
     * closes them.
     * </p>
     * 
     * @param viewType
     *            The view class to close
     * @see IWorkbenchPage#hideView(org.eclipse.ui.IViewReference)
     */
    private static void closeAllViews(final Class<? extends RightsEnabledView> viewType) {
        Stream.of(PlatformUI.getWorkbench().getWorkbenchWindows())
                .forEach(window -> Stream.of(window.getPages())
                        .forEach(page -> Stream.of(page.getViewReferences()).forEach(viewRef -> {

                            // for all view references
                            // of a given workbench page
                            // of a given workbench window
                            final IWorkbenchPart viewPart = viewRef.getPart(false);
                            final Class<?> partType = (viewPart != null) ? viewPart.getClass()
                                    : null;

                            if (viewType == null || viewType.equals(partType)) {
                                safeHideView(page, viewRef);
                            }
                        })));

    }

    private static void safeHideView(IWorkbenchPage page, IViewReference viewRef) {
        try {
            page.hideView(viewRef);
        } catch (Exception e) {
            LOG.warn("Exception while closing view."); //$NON-NLS-1$
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stacktrace: ", e); //$NON-NLS-1$
            }
        }
    }

    /*
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFocus..."); //$NON-NLS-1$
        }
    }

    /**
     * @return false if operation mode is standalone and internal server is not
     *         running
     */
    protected boolean isServerRunning() {
        return !(Activator.getDefault().isStandalone()
                && !Activator.getDefault().getInternalServer().isRunning());
    }

    /**
     * A listener that ensures that the required permissions are checked when
     * the view is shown
     */
    private final class CheckPermissonListener extends PartListenerAdapter {
        @Override
        public void partVisible(IWorkbenchPartReference partRef) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("partVisible: " + partRef.getId()); //$NON-NLS-1$
            }
            if (getViewId().equals(partRef.getId())) {
                if (!isServerRunning()) {
                    Activator.getDefault().getInternalServer()
                            .addInternalServerStatusListener(e -> {
                                if (e.isStarted()) {
                                    checkAndDecline();
                                }
                            });
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

    }

}
