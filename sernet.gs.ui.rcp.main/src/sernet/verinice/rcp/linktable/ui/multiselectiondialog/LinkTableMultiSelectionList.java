/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 *     Ruth Motza <rm[at]sernet[dot]de> - adapion of copied class
 ******************************************************************************/
package sernet.verinice.rcp.linktable.ui.multiselectiondialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import sernet.verinice.rcp.linktable.ui.LinkTableComposite;
import sernet.verinice.rcp.linktable.ui.Messages;
import sernet.verinice.service.model.IObjectModelService;

/**
 * @see LinkTableMultiSelectionControl
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class LinkTableMultiSelectionList {

    private static final Logger LOG = Logger.getLogger(LinkTableMultiSelectionList.class);

    private LinkTableMultiSelectionDialog parent;
    private Composite list;
    private Group group;
    private Map<String, Button> checkboxes = new HashMap<>();
    private Set<String> allUsedRelationIds;
    private Button allRelations;

    private LinkTableMultiSelectionControl vltMultiSelectionControl;

    private LinkTableComposite vltComposite;

    private List<String> selectedRelations;

    public LinkTableMultiSelectionList(LinkTableMultiSelectionDialog parent) {
        this.parent = parent;
        vltMultiSelectionControl = parent.getLTRMultiSelectionControl();
        vltComposite = vltMultiSelectionControl.getVltParent();
        selectedRelations = vltComposite.getVeriniceLinkTable().getRelationIds();
        create();
    }

    public void refresh() {
        list.getParent().dispose();
        list = createScrolledList();
        int height = createButtons();
        GridData gd = (GridData) group.getLayoutData();
        gd.heightHint = height;
        list.pack();
        group.pack();
        group.layout();
    }

    private void create() {

        allUsedRelationIds = new HashSet<>(
                vltComposite.getAllUsedRelationIds());
        createAllRelationsButton();

        if (allUsedRelationIds.isEmpty()) {
            allRelations.setSelection(true);
            allRelations.setEnabled(false);
            new Label(parent.getDialogShell(), SWT.NONE)
                    .setText(Messages.LinkTableMultiSelectionList_0);
        } else {

            group = createGroup();
            list = createScrolledList();
            int height = createButtons();
            GridData gd = (GridData) group.getLayoutData();
            gd.heightHint = height;

            initializeCheckboxes();
            list.pack();
            parent.getParent().pack();
            GridDataFactory.swtDefaults().grab(true, true).align(GridData.FILL, GridData.FILL)
                    .applyTo(group);
            group.layout();
        }
    }


    private void createAllRelationsButton() {

        boolean useAllRelationIds = vltComposite.getVeriniceLinkTable().getRelationIds().isEmpty();
        allRelations = new Button(parent.getDialogShell(), SWT.CHECK | SWT.LEFT);
        allRelations.setText(Messages.MultiSelectionControl_2);
        SelectionListener allRelationIDsListener = new SelectionListener() {

            public void widgetSelected(SelectionEvent event) {

                LOG.debug(allRelations.getText() + " selected: " + allRelations.getSelection());
                vltMultiSelectionControl.setUseAllRelationIds(allRelations.getSelection());
                updateCheckBoxes();

                vltMultiSelectionControl.writeToTextField();

            }

            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }
        };
        allRelations.addSelectionListener(allRelationIDsListener);

        allRelations.setSelection(useAllRelationIds);

    }

    private Group createGroup() {
        Group checkboxGroup = new Group(parent.getDialogShell(), SWT.BORDER);

        GridData groupLData = new GridData();
        groupLData.horizontalIndent = 0;

        GridDataFactory.swtDefaults().align(GridData.FILL, GridData.CENTER).hint(SWT.DEFAULT, 100)
                .span(4, 1).grab(true, false).indent(0, SWT.DEFAULT).applyTo(checkboxGroup);
        checkboxGroup.setLayoutData(groupLData);
        checkboxGroup.setText(Messages.MultiSelectionList_0);
        checkboxGroup.setLayout(new FillLayout(SWT.V_SCROLL));

        return checkboxGroup;
    }

    private int createButtons() {

        IObjectModelService modelService = vltComposite.getContentService();
        int btnsHeight = 0;
        int i = 0;
        for (String id : allUsedRelationIds) {
            ++i;
            Button checkbox = new Button(list, SWT.CHECK | SWT.LEFT);
            checkbox.setText(id + " (" + modelService.getRelationLabel(id) + ")");
            checkbox.setData(id);
            checkbox.pack();

            checkbox.addSelectionListener(new SelectionListener() {

                public void widgetSelected(SelectionEvent event) {
                    Set<String> selectedItems = getSelectedItems();
                    allRelations.setSelection(selectedItems.isEmpty()
                            || selectedItems.size() == checkboxes.size());
                    vltMultiSelectionControl.setUseAllRelationIds(allRelations.getSelection());
                    vltMultiSelectionControl.writeToTextField();

                }

                public void widgetDefaultSelected(SelectionEvent event) {
                    widgetSelected(event);
                }
            });

            checkboxes.put(id, checkbox);
            if (i < 6) {
                btnsHeight = btnsHeight + checkbox.getBounds().height + 5;
            }
        }
        return btnsHeight;
    }

    private Composite createScrolledList() {
        ScrolledComposite scrollPane = new ScrolledComposite(group,
                SWT.V_SCROLL);
        Composite scrolledList = new Composite(scrollPane, SWT.NULL);
        GridLayoutFactory.swtDefaults().numColumns(1).equalWidth(false).generateLayout(scrolledList);
        scrollPane.setContent(scrolledList);
        return scrolledList;
    }

    public Set<String> getSelectedItems(){
        Set<String> set = new HashSet<>();
        for(Entry<String, Button> box : checkboxes.entrySet()){
            if(box.getValue().getSelection()){
                set.add(box.getKey());
            }
        }
        return set;

    }

    public void initializeCheckboxes() {


        if (selectedRelations.isEmpty()) {
            selectedRelations = new ArrayList<>(allUsedRelationIds);
        }
        for (String id : selectedRelations) {

            Button checkbox = (Button) checkboxes.get(id);
            if (checkbox != null) {
                checkbox.setSelection(true);
            } else {
                selectedRelations.remove(id);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(id + " removed from set");
                }
            }
        }
    }

    public void updateCheckBoxes() {

        for (Button checkbox : checkboxes.values()) {
            checkbox.setSelection(allRelations.getSelection());
        }
    }
}
