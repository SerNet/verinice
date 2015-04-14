/*******************************************************************************
 * Copyright (c) 2015 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.search.VeriniceSearchResultRow;
import sernet.verinice.model.search.VeriniceSearchResultObject;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.service.commands.LoadElementByUuid;

/**
 *
 */
public class SearchView extends RightsEnabledView {
    
    private static final Logger LOG = Logger.getLogger(SearchView.class);
    
    public static final String ID = "sernet.verinice.rcp.search.SearchView";
    
    private Text queryText;
    
    private Button searchButton;
    
    private Combo resultsByTypeCombo;
    
    private SearchContentProvider contentProvider = new SearchContentProvider();
    
    private SearchLabelProvider labelProvider = new SearchLabelProvider();
    
    TableViewer viewer;
    
    SearchTableSorter tableSorter = new SearchTableSorter();
    
    private Action doubleClickAction;
    
    public SearchView(){
        super();
    }
    
    @Override
    public void createPartControl(Composite parent) {
        try {
            super.createPartControl(parent);
            initView(parent);
        } catch (Exception e){
            LOG.error("Error while creating control", e); //$NON-NLS-1$
            ExceptionUtil.log(e, "Something went wrong here");
        }
    }
    
    private void initView(Composite parent) {         
        parent.setLayout(new FillLayout());
        createComposite(parent);
        
        makeActions();
//        fillLocalToolBar();   
//        startInitDataJob();
    }
    
    private void makeActions(){
        createDoubleClickAction();
        hookDoubleClickAction();
    }
    
    private void createDoubleClickAction() {
        doubleClickAction = new Action() {
            @Override
            public void run() {
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection) selection).getFirstElement();
                VeriniceSearchResultRow result = (VeriniceSearchResultRow) obj;

                // open the object on the other side of the link:
                LoadElementByUuid<CnATreeElement> elementLoader = new LoadElementByUuid<CnATreeElement>(result.getIdentifier());
                try {
                    elementLoader = ServiceFactory.lookupCommandService().executeCommand(elementLoader);
                } catch (CommandException e) {
                    LOG.error("Error loading element from db",e);
                }
                if(elementLoader.getElement() != null){
                    EditorFactory.getInstance().updateAndOpenObject(elementLoader.getElement());
                }
            }
        };

    }

    private void hookDoubleClickAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
    }
    
    
    private void createComposite(Composite parent) {
        Composite composite = createContainerComposite(parent);
        Composite searchComposite = createSearchComposite(composite);
        createSearchForm(searchComposite);
        Composite tableComposite = createTableComposite(composite);
        createTable(tableComposite);      
        getSite().setSelectionProvider(viewer);
        viewer.setInput(new PlaceHolder("Platzhalter"));
    }
    
    private void createTypeCombo(Composite parent){
        
    }
    
    private void createTable(Composite tableComposite) {
        viewer = new TableViewer(tableComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true,true);
        viewer.getControl().setLayoutData(gd);
        
        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(labelProvider);
        Table table = viewer.getTable();
        
        createTableColumn("typeID", 50, 0);
        createTableColumn("Title", 50, 1);
        createTableColumn("Fundstelle", 50, 2);
        createTableColumn("UUID", 50, 3);
        
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        viewer.setSorter(tableSorter);
        // ensure initial table sorting (by filename)
        ((SearchTableSorter) viewer.getSorter()).setColumn(2);
    }
    
    private void createTableColumn(String title, int width, int index) {
        TableColumn scopeColumn = new TableColumn(viewer.getTable(), SWT.LEFT);
        scopeColumn.setText(title);
        scopeColumn.setWidth(width);
        scopeColumn.addSelectionListener(new SearchSortSelectionAdapter(this, scopeColumn, index));
    }
    
    private void createSearchForm(Composite searchComposite) {
        queryText = new Text(searchComposite, SWT.WRAP);
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        gridData.minimumWidth = 30;
        queryText.setLayoutData(gridData);
        
        searchButton = new Button(searchComposite, SWT.BUTTON1);
        searchButton.setText("Search...");
        searchButton.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                Activator.inheritVeriniceContextState();
                List<VeriniceSearchResultObject> results = ServiceFactory.lookupSearchService().executeSimpleQuery(queryText.getText());
                fillResultByTypeCombo(results);
//                refreshTable(results);
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        
        resultsByTypeCombo = new Combo(searchComposite,  SWT.DROP_DOWN | SWT.READ_ONLY);
        resultsByTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        resultsByTypeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
//                refreshTable();
            }
        });
    }
    
    private Composite createSearchComposite(Composite composite) {
        Composite comboComposite = new Composite(composite, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        comboComposite.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout(6, true);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        comboComposite.setLayout(gridLayout);
        return comboComposite;
    }
    
    private Composite createTableComposite(Composite composite) {
        Composite tableComposite = new Composite(composite, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableComposite.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        tableComposite.setLayout(gridLayout);
        return tableComposite;
    }
    
    private Composite createContainerComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.FILL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        return composite;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.RightsEnabledView#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.SEARCHVIEW;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.RightsEnabledView#getViewId()
     */
    @Override
    public String getViewId() {
        return ID;
    }
    
    private void refreshTable(VeriniceSearchResultObject results){
        if(viewer != null){
            viewer.setInput(results);
            viewer.refresh(true);
        }
    }
    
    private void fillResultByTypeCombo(List<VeriniceSearchResultObject> results){
        List<String> resultList = new ArrayList<String>();
        for(VeriniceSearchResultObject r : results){
            if(r.getHits() > 0){
                resultList.add(r.getEntityTypeId() + "\t(" + String.valueOf(r.getHits()) + ")");
            }
        }
        resultsByTypeCombo.setItems(resultList.toArray(new String[resultList.size()]));
    }

}
