/*******************************************************************************
 * Copyright (c) 2016 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn <sh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.search.ISearchService;
import sernet.verinice.rcp.search.SearchView;

/**
 * Special implementation of [{@link OpenMultipleViewAction}
 * 
 * This is necessary because of full-text-search could be disabled during
 * runtime (tier2-only), which requires to disable this action (implemented via
 * listener)
 * 
 * In tier3 full-text-search could only be en-/disabled by setting a property in
 * the web.xml of the server and restarting it, so no need to react during
 * (client-)runtime in that case
 * 
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
public class OpenSearchViewAction extends OpenMultipleViewAction {

    private final IWorkbenchWindow window;
    private final String viewId;
    private int instance = 0;

    public OpenSearchViewAction(IWorkbenchWindow window, String label) {
        super(window, label, SearchView.ID, ImageCache.SEARCH, ActionRightIDs.SEARCHVIEW);
        this.window = window;
        this.viewId = SearchView.ID;
        syncEnabledWithPreference();
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.actions.RightsEnabledAction#doRun()
     */
    @Override
    public void doRun() {
        syncEnabledWithPreference();
        if (window != null) {
            try {
                window.getActivePage().showView(viewId, "" + String.valueOf(instance++), IWorkbenchPage.VIEW_ACTIVATE); //$NON-NLS-1$
            } catch (PartInitException e) {
                ExceptionUtil.log(e, Messages.OpenMultipleViewAction_2);
            }
        }
    }

    private void syncEnabledWithPreference() {
        if (Activator.getDefault().isStandalone()) {
            syncEnabledStandalone();
        } else {
            syncEnabledServer();
        }
    }

    private void syncEnabledServer() {
        try {
            int searchServiceImplementation = ServiceFactory.lookupSearchService().getImplementationtype();
            if (ISearchService.ES_IMPLEMENTATION_TYPE_DUMMY == searchServiceImplementation) {
                this.setEnabled(false);
            } else if (checkRights() && ISearchService.ES_IMPLEMENTATION_TYPE_REAL == searchServiceImplementation) {
                this.setEnabled(true);
            }
        } catch (Exception e) {
            Logger.getLogger(OpenSearchViewAction.class).error("Can't connect to searchService, disabling searchView", e);
            this.setEnabled(false);
        }
    }

    private void syncEnabledStandalone() {
        DisableSearchActionListener listener = new DisableSearchActionListener(this);
        Activator.getDefault().getPreferenceStore().addPropertyChangeListener(listener);
        this.addPropertyChangeListener(listener);
        this.setEnabled(!Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SEARCH_DISABLE));
    }

    private final class DisableSearchActionListener implements IPropertyChangeListener {

        final OpenSearchViewAction action;

        public DisableSearchActionListener(OpenSearchViewAction action) {
            this.action = action;
        }

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            boolean disabled = Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SEARCH_DISABLE);
            if (PreferenceConstants.SEARCH_DISABLE.equals(event.getProperty())) {
                if (checkRights() && disabled) {
                    action.setEnabled(false);
                } else if (checkRights() && !disabled) {
                    action.setEnabled(true);
                }
            }
        }

    }

}
