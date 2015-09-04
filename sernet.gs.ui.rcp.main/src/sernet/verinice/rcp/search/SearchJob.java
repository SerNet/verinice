/*******************************************************************************
 * Copyright (c) 2015 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.search;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.model.search.VeriniceQuery;
import sernet.verinice.model.search.VeriniceSearchResult;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class SearchJob extends WorkspaceJob {

    private VeriniceQuery query;

    private SearchView searchView;

    public SearchJob(VeriniceQuery query, SearchView searchView) {
        super(Messages.SearchView_5);
        this.query = query;

        this.searchView = searchView;
    }

    @Override
    public IStatus runInWorkspace(IProgressMonitor mon) throws CoreException {

        try {

            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    searchView.disableSearch();
                }
            });

            Activator.inheritVeriniceContextState();

            final VeriniceSearchResult result = ServiceFactory.lookupSearchService().query(query);
            result.setVeriniceQuery(query);

            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    searchView.updateResultCombobox(result);
                }
            });

        } finally {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    searchView.enableSearch();
                }
            });
        }

        return Status.OK_STATUS;
    }

}
