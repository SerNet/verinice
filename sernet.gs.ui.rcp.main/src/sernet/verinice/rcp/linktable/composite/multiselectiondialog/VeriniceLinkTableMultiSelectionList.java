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
 *     Ruth Motza <rm[at]sernet[dot]de> - adapion of new class
 ******************************************************************************/
package sernet.verinice.rcp.linktable.composite.multiselectiondialog;

import java.util.*;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import sernet.verinice.rcp.linktable.composite.Messages;
import sernet.verinice.rcp.linktable.composite.VeriniceLinkTableComposite;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class VeriniceLinkTableMultiSelectionList {

    private VeriniceLinkTableMultiSelectionDialog parent;

    private Composite list;

    private Group group;

    private Map<String, Button> checkboxes = new HashMap<>();

    private Set<String> options;

    private boolean contextMenuPresent = false;

    private Button allRelations;

    private SelectionListener allRelationIDsListener;

    private static final Logger LOG = Logger.getLogger(VeriniceLinkTableMultiSelectionList.class);

    public VeriniceLinkTableMultiSelectionList(VeriniceLinkTableMultiSelectionDialog parent) {
        this.parent = parent;
        create();
    }

    public boolean isContextMenuPresent() {
        return contextMenuPresent;
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

        createAllRelationsButton();

        VeriniceLinkTableComposite ltrParent = parent.getLTRMultiSelectionControl().getLtrParent();
        options = new HashSet<>(ltrParent.getAllUsedRelationIds());

        group = createGroup();
        list = createScrolledList();
        int height = createButtons();
        GridData gd = (GridData) group.getLayoutData();
        gd.heightHint = height;
        if (parent.getLTRMultiSelectionControl().useAllRelationIds()) {
            updateCheckBoxes();
        } else {
            setSelection();
        }
        list.pack();
        parent.getParent().pack();
    }

    private void createAllRelationsButton() {

        boolean useAllRelationIds = parent.getLTRMultiSelectionControl().useAllRelationIds();
        allRelations = new Button(parent.getDialogShell(), SWT.CHECK | SWT.LEFT);
        allRelations.setText(Messages.MultiSelectionControl_2);
        allRelationIDsListener = new SelectionListener() {

            public void widgetSelected(SelectionEvent event) {
                widgetSelected();
            }

            public void widgetSelected() {

                LOG.debug(allRelations.getText() + " selected: " + allRelations.getSelection());
                parent.getLTRMultiSelectionControl()
                        .setUseAllRelationIds(allRelations.getSelection());
                updateCheckBoxes();
                
                
                parent.getLTRMultiSelectionControl().writeToTextField();

            }

            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected();
            }
        };
        allRelations.addSelectionListener(allRelationIDsListener);

        allRelations.setSelection(useAllRelationIds);

    }

    private Group createGroup() {
        Group checkboxGroup = new Group(parent.getDialogShell(), SWT.BORDER);

        GridData groupLData = new GridData();
        groupLData.verticalAlignment = GridData.CENTER;
        groupLData.horizontalAlignment = GridData.FILL;
        groupLData.widthHint = -1;
        groupLData.heightHint = 100;
        groupLData.horizontalIndent = 0;
        groupLData.horizontalSpan = 4;
        groupLData.verticalSpan = 1;
        groupLData.grabExcessHorizontalSpace = true;
        groupLData.grabExcessVerticalSpace = false;
        checkboxGroup.setLayoutData(groupLData);
        checkboxGroup.setText(Messages.MultiSelectionList_0);
        checkboxGroup.setLayout(new FillLayout(SWT.V_SCROLL));
        return checkboxGroup;
    }

    private int createButtons() {

        VeriniceLinkTableComposite ltrParent = parent.getLTRMultiSelectionControl().getLtrParent();
        int btnsHeight = 0;
        int i = 0;
        for (String id : options) {
            ++i;
            Button checkbox = new Button(list, SWT.CHECK | SWT.LEFT);
            checkbox.setText(ltrParent.getContentService().getRelationLabel(id));
            checkbox.setData(id);
            checkbox.pack();


            checkbox.addSelectionListener(new SelectionListener() {

                public void widgetSelected(SelectionEvent event) {
                    Button box = (Button) event.widget;
                    String id = (String) box.getData();
                    Set<String> selectedRelations = parent.getLTRMultiSelectionControl()
                            .getSelectedRelationIDs();
                    if (box.getSelection()) {
                        LOG.debug("selected: " + id);

                        selectedRelations.add(id);
                    } else {
                        LOG.debug("unselected: " + id);

                        boolean isDeleted = selectedRelations.remove(id);
                        LOG.debug("is deleted: " + isDeleted);
                    }
                    
                    allRelations.setSelection(selectedRelations.size() == checkboxes.size());
                    parent.getLTRMultiSelectionControl()
                            .setUseAllRelationIds(allRelations.getSelection());
                    parent.getLTRMultiSelectionControl().writeToTextField();

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
        GridLayout listLayout = new GridLayout(1, false);
        scrolledList.setLayout(listLayout);
        scrollPane.setContent(scrolledList);
        return scrolledList;
    }


    /**
     * Selects an option.
     * 
     * @param optionId
     *            the ID of the option to select
     */
    public void setSelection() {
        Set<String> selectedRelationIDs = parent.getLTRMultiSelectionControl()
                .getSelectedRelationIDs();
        for (String id : selectedRelationIDs) {

            Button checkbox = (Button) checkboxes.get(id);
            if (checkbox != null) {
                checkbox.setSelection(true);
            } else {
                selectedRelationIDs.remove(id);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(id + " removed from set");
                }
            }
        }
    }


    public void setLayoutData(GridData scrolledComposite1LData) {
        group.setLayoutData(scrolledComposite1LData);
        group.layout();
    }

    public void updateCheckBoxes() {

        boolean useAllRelationIDs = parent.getLTRMultiSelectionControl().useAllRelationIds();
        Set<String> selectedRelationIDs = parent.getLTRMultiSelectionControl().getSelectedItems();

        for (Button checkbox : checkboxes.values()) {
            checkbox.setSelection(useAllRelationIDs);
                
        }

        if (useAllRelationIDs) {
            selectedRelationIDs.addAll(checkboxes.keySet());
        }else{
            selectedRelationIDs.clear();
        }

        allRelations.setSelection(useAllRelationIDs);
    }
}
