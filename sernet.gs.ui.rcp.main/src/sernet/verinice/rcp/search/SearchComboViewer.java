/*******************************************************************************
 * Copyright (c) 2015 benjamin.
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
 *     benjamin <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.search;

import java.util.Set;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import sernet.verinice.model.search.VeriniceSearchResult;
import sernet.verinice.model.search.VeriniceSearchResultObject;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class SearchComboViewer extends ComboViewer implements IStructuredContentProvider, ISelectionChangedListener {

    private SearchView searchView;

    public SearchComboViewer(Composite searchComboComposite, SearchView searchView) {

        super(searchComboComposite, SWT.DROP_DOWN | SWT.READ_ONLY);

        this.searchView = searchView;
        this.setContentProvider(this);

        this.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                VeriniceSearchResultObject veriniceSearchResult = (VeriniceSearchResultObject) element;
                return new StringBuilder().append(veriniceSearchResult.getEntityTypeId()).append(" - ").append(veriniceSearchResult.getHits()).toString();
            }
        });

        this.addSelectionChangedListener(this);;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
     * .viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof VeriniceSearchResult) {
            Set<VeriniceSearchResultObject> input = ((VeriniceSearchResult) newInput).getAllVeriniceSearchObjects();
            if (!input.isEmpty())
                viewer.setSelection(new StructuredSelection(input), true);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java
     * .lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof VeriniceSearchResult) {
            VeriniceSearchResult veriniceSearchResult = (VeriniceSearchResult) inputElement;
            VeriniceSearchResultObject[] input = new VeriniceSearchResultObject[veriniceSearchResult.getAllVeriniceSearchObjects().size()];
            return veriniceSearchResult.getAllVeriniceSearchObjects().toArray(input);
        } else {
            return new VeriniceSearchResultObject[0];
        }
    }


    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        if(!event.getSelection().isEmpty()){
            VeriniceSearchResultObject veriniceSearchResultObject = (VeriniceSearchResultObject) ((StructuredSelection) event.getSelection()).getFirstElement();
            searchView.setTableViewer(veriniceSearchResultObject);
        }
    }
}
