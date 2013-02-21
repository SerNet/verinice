/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.iso27k.rcp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class OrganizationWidget {

    private List<CnATreeElement> orgsAndITVs;   
    private ITreeSelection selection;
    private CnATreeElement selectedElement;
    private Set<CnATreeElement> selectedElementSet;
    
    private Group group;
    private List<Button> radioOrganizationList;
    
    /**
     * @param parent
     * @param style
     * @throws CommandException 
     */
    public OrganizationWidget(Composite parent) throws CommandException {
        group = new Group(parent, SWT.NONE);
        initData();
        init();
    }
    
    public OrganizationWidget(Composite parent, ITreeSelection selection) throws CommandException {
        group = new Group(parent, SWT.NONE);
        this.selection = selection;
        initData();
        init();
    }
    
    public OrganizationWidget(Composite parent, CnATreeElement selectedElement) throws CommandException {
        group = new Group(parent, SWT.NONE);
        this.selectedElement = selectedElement;
        initData();
        init();
    }
    
    public OrganizationWidget(Composite parent, ITreeSelection selection, CnATreeElement selectedElement) throws CommandException {
        group = new Group(parent, SWT.NONE);
        this.selection = selection;
        this.selectedElement = selectedElement;
        initData();
        init();
    }

    private void init() {
        final int gdMinimumWidth = 550;
        final int gdHeightHint = 200; 
        group.setText(Messages.SamtExportDialog_2);
        GridLayout groupOrganizationLayout = new GridLayout(1, true);
        group.setLayout(groupOrganizationLayout);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true,true);
        gd.minimumWidth = gdMinimumWidth;
        gd.heightHint = gdHeightHint; 
        group.setLayoutData(gd);

        ScrolledComposite scrolledComposite = new ScrolledComposite(group, SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        scrolledComposite.setExpandHorizontal(true);
        
        Composite innerComposite = new Composite (scrolledComposite, SWT.NONE); 
        scrolledComposite.setContent(innerComposite); 
        innerComposite.setLayoutData(new GridData (SWT.FILL, SWT.FILL,true, false)); 
        innerComposite.setLayout(new GridLayout (1, false));
        
        selectedElementSet = new HashSet<CnATreeElement>();      
        
        ArrayList<CnATreeElement> preSelectedElements = new ArrayList<CnATreeElement>(0);
        if(selection != null && !selection.isEmpty()){
            Iterator<CnATreeElement> iter = selection.iterator();
            while(iter.hasNext()){
                preSelectedElements.add(iter.next());
            }
        } else if(selectedElement != null) {
            preSelectedElements.add(selectedElement);
        }
        
        SelectionListener organizationListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button checkbox = (Button) e.getSource();
                selectedElement = (CnATreeElement) checkbox.getData();
                if(checkbox.getSelection()) {
                    selectedElementSet.add(selectedElement);                  
                } else {
                    selectedElementSet.remove(selectedElement);
                }
                super.widgetSelected(e);
            }
        };
               
        radioOrganizationList = new LinkedList<Button>();
        for(CnATreeElement elmt : orgsAndITVs) {
            Button radioOrganization = new Button(innerComposite, SWT.CHECK);
            radioOrganization.setText(elmt.getTitle());
            radioOrganization.setData(elmt);
            radioOrganization.addSelectionListener(organizationListener);
            if (preSelectedElements.contains(elmt)) {
                radioOrganization.setSelection(true);
                selectedElement = elmt; 
                selectedElementSet.add(elmt);             
            }
            if (orgsAndITVs.size() == 1) {
                radioOrganization.setSelection(true);
                selectedElement = elmt;
                selectedElementSet.add(elmt);
            }
            radioOrganizationList.add(radioOrganization);
        }
        
        
        
        scrolledComposite.setVisible(true);
        Point size = innerComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT); 
        size.y += orgsAndITVs.size() * 2;
        innerComposite.setSize(size); 
        group.layout(); 
    }
    
    private void initData() throws CommandException {
        /*
         * Widgets for selection of an IT network or organization:
         */

        LoadCnAElementByType<Organization> cmdLoadOrganization = new LoadCnAElementByType<Organization>(Organization.class);
        cmdLoadOrganization = ServiceFactory.lookupCommandService().executeCommand(cmdLoadOrganization);
        
        
        LoadCnAElementByType<ITVerbund> cmdItVerbund = new LoadCnAElementByType<ITVerbund>(ITVerbund.class);
        cmdItVerbund = ServiceFactory.lookupCommandService().executeCommand(cmdItVerbund);
            
        orgsAndITVs = new ArrayList<CnATreeElement>();
        orgsAndITVs.addAll(cmdLoadOrganization.getElements());
        orgsAndITVs.addAll(cmdItVerbund.getElements());
        orgsAndITVs = sortOrgListByTitle(orgsAndITVs);
    }
    
    public void addSelectionLiustener(SelectionListener listener) {
        if(radioOrganizationList!=null) {
            for (Button radioButton : radioOrganizationList) {
                radioButton.addSelectionListener(listener);
            }
        }
    }
    
    private List<CnATreeElement> sortOrgListByTitle(List<CnATreeElement> list){
        Collections.sort(list, new Comparator<CnATreeElement>() {
            @Override
            public int compare(CnATreeElement o1, CnATreeElement o2) {
                NumericStringComparator comparator = new NumericStringComparator();
                return comparator.compare(o1.getTitle(), o2.getTitle());
            }
        });
        return list;
    }

    public CnATreeElement getSelectedElement() {
        return selectedElement;
    }

    public void setSelectedElement(CnATreeElement selectedElement) {
        this.selectedElement = selectedElement;
    }

    public Set<CnATreeElement> getSelectedElementSet() {
        return selectedElementSet;
    }

}
