/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.linktable.composite;

import java.util.*;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import sernet.verinice.rcp.linktable.VeriniceLinkTableUtil;
import sernet.verinice.rcp.linktable.composite.multiselectiondialog.VeriniceLinkTableMultiSelectionControl;
import sernet.verinice.service.linktable.ColumnPathParser;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTable;
import sernet.verinice.service.model.IObjectModelService;

/**
 * Composite to edit or create ltr-columns.
 * 
 * 
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class VeriniceLinkTableComposite extends Composite {

    private static final Logger LOG = Logger.getLogger(VeriniceLinkTableComposite.class);

    private List<VeriniceLinkTableFieldListener> listeners = new ArrayList<>();
    private static final Point DEFAULT_MARGIN = new Point(5, 5);
    private static final Point DEFAULT_MARGIN_CONTENT = new Point(10, 10);
    private Composite columnsContainer;
    private Composite body;
    private Composite bodyBody;
    private ScrolledComposite c2;
    private Composite buttons;
    private VeriniceLinkTableMultiSelectionControl multiControl;

    private int numCols = 0;
    private ArrayList<VeriniceLinkTableColumn> columns = new ArrayList<>();

    private IObjectModelService contentService;
    private boolean useAllScopes = true;
    private VeriniceLinkTable ltrContent = null;

    public VeriniceLinkTableComposite(VeriniceLinkTable ltrContent,
            IObjectModelService contentService,
            Composite parent, int style) {

        super(parent, style);
        this.contentService = contentService;
        this.ltrContent = ltrContent;
        useAllScopes = ltrContent.useAllScopes();
        createContent();

    }

    private void createContent() {

        FormLayout layout = new FormLayout();
        layout.marginHeight = DEFAULT_MARGIN.y;
        layout.marginWidth = DEFAULT_MARGIN.x;

        Composite rootContainer = new Composite(this, getStyle());

        setHead(rootContainer);

        setBody(rootContainer);

        rootContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayoutFactory.swtDefaults().margins(DEFAULT_MARGIN).generateLayout(rootContainer);

    }

    private void setHead(Composite parent) {

        Composite head = new Composite(parent, getStyle());

        Composite scopeButtons = new Composite(head, getStyle());
        final Button[] scopeRadios = new Button[2];
        scopeRadios[0] = new Button(scopeButtons, SWT.RADIO);
        final Button useAllScopesButton = scopeRadios[0];
        scopeRadios[0].setText(Messages.VeriniceLinkTableComposite_0);

        scopeRadios[1] = new Button(scopeButtons, SWT.RADIO);

        final Button useSelectedScopes = scopeRadios[1];
        useSelectedScopes.setText(Messages.VeriniceLinkTableComposite_1);

        SelectionAdapter listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                Button selected = (Button) event.widget;
                if (LOG.isDebugEnabled()) {
                    LOG.debug(selected.getText() + " is selected");
                }

                useAllScopes = selected == useAllScopesButton;
                updateVeriniceContent();

            }
        };

        useAllScopesButton.addSelectionListener(listener);
        useSelectedScopes.addSelectionListener(listener);
        useAllScopesButton.setSelection(useAllScopes);
        useSelectedScopes.setSelection(!useAllScopes);

        GridLayoutFactory.swtDefaults().margins(DEFAULT_MARGIN).numColumns(1)
                .generateLayout(scopeButtons);
        Composite multiControlContainer = new Composite(head, getStyle());
        multiControl = new VeriniceLinkTableMultiSelectionControl(multiControlContainer,
                this);

        GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(multiControlContainer);
        GridData multiControlContainerData = new GridData(GridData.VERTICAL_ALIGN_CENTER);
        multiControlContainer.setLayoutData(multiControlContainerData);
        GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(head);

    }

    private void setBody(Composite parent) {
        c2 = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

        body = new Composite(c2, getStyle());
        c2.setContent(body);
        c2.setExpandHorizontal(true);
        c2.setExpandVertical(true);
        c2.setLayoutData(new GridData(GridData.FILL_BOTH));

        bodyBody = new Composite(body, getStyle());

        columnsContainer = new Composite(bodyBody, getStyle());
        GridLayoutFactory.swtDefaults().generateLayout(columnsContainer);
        addButtons(bodyBody);
        if (ltrContent.getColumnPaths() != null && !ltrContent.getColumnPaths().isEmpty()) {
            addColumnsWithContent();
        } else {
            addColumn(null);
        }

        GridLayoutFactory.swtDefaults().margins(DEFAULT_MARGIN_CONTENT).generateLayout(bodyBody);
        GridLayoutFactory.swtDefaults().margins(0, 0).generateLayout(body);
        GridLayoutFactory.swtDefaults().margins(0, 0).generateLayout(c2);
    }

    private void addColumnsWithContent() {

        for (String column : ltrContent.getColumnPaths()) {

            List<String> path = ColumnPathParser.getColumnPathAsList(column);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Element " + path);
            }
            addColumn(path);
        }
        refresh(true);

    }

    private void addColumn(List<String> path) {
        VeriniceLinkTableColumn column;
        boolean isNewColumn = true;
        if (path == null) {
            column = new VeriniceLinkTableColumn(this, getStyle(), ++numCols);
        } else {
            if (columns.isEmpty()) {
                column = new VeriniceLinkTableColumn(path, this, ++numCols);
            } else {

                column = new VeriniceLinkTableColumn(columns.get(0).getFirstCombo().getSelection(),
                        path, this, ++numCols);
            }
            isNewColumn = false;

        }
        columns.add(column);
        addDeleteButtonListener(column);
        handleMoreThanOneColumn(isNewColumn);
    }

    private void handleMoreThanOneColumn(boolean isNewColumn) {
        boolean oneClumn = columns.size() <= 1;
        VeriniceLinkTableColumn firstColumn = columns.get(0);
        firstColumn.getDeleteButton().setEnabled(!oneClumn);
        firstColumn.getFirstCombo().getCombo().setEnabled(oneClumn);
        if (!oneClumn) {
            VeriniceLinkTableColumn lastColumn = columns.get(columns.size() - 1);
            lastColumn.getFirstCombo().getCombo().setEnabled(false);
            if (isNewColumn) {
                lastColumn.getFirstCombo().getCombo()
                        .select(firstColumn.getFirstCombo().getCombo().getSelectionIndex());
                lastColumn.getFirstCombo().selectionChanged(null);
            }

        } else {
            if (isNewColumn) {
                firstColumn.getFirstCombo().getCombo().select(0);
                firstColumn.getFirstCombo().selectionChanged(null);
            }
        }

    }

    private void addDeleteButtonListener(final VeriniceLinkTableColumn column) {

        column.getDeleteButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {

                boolean delete = columns.remove(column);
                handleMoreThanOneColumn(false);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("deleted " + delete);
                }
                column.getColumn().dispose();
                numCols = columns.size();
                renameColumns();
                refresh(true);
            }
        });

    }

    protected void renameColumns() {
        int i = 1;
        for (VeriniceLinkTableColumn column : columns) {
            column.setColumnNumber(i++);
        }

    }

    public void refresh(boolean updateVeriniceLinkTable) {
        columnsContainer.pack(true);
        bodyBody.pack(true);
        body.pack(true);
        c2.setMinSize(bodyBody.getClientArea().width, bodyBody.getClientArea().height);
        c2.showControl(buttons);
        body.layout(true);
        bodyBody.layout(true);
        columnsContainer.layout(true);
        c2.layout(true);
        if (updateVeriniceLinkTable) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("update veriniceLinkTable");
            }
            updateVeriniceContent();
        }

    }

    public void updateVeriniceContent() {

        if (ltrContent == null) {
            ltrContent = new VeriniceLinkTable.Builder("new").build();
        }
        ArrayList<String> columnPaths = new ArrayList<>(columns.size());
        String path;
        for (VeriniceLinkTableColumn column : columns) {

            path = column.getColumnPath();
            columnPaths.add(path);
        }
        ltrContent.setAllScopes(useAllScopes);
        if (useAllScopes) {
            ltrContent.getScopeIds().clear();
        }
        ltrContent.setColumnPaths(columnPaths);
        ltrContent.setRelationIds(new ArrayList<>(multiControl.getSelectedRelationIDs()));
        if (LOG.isDebugEnabled()) {
            LOG.debug(columnPaths.size() + " columns");
        }
        fireEvent("updateVeriniceLinkTable", ltrContent, isValidVeriniceLinkTable());

    }

    private boolean isValidVeriniceLinkTable() {
        return VeriniceLinkTableUtil.isValidVeriniceLinkTable(ltrContent);
    }

    private void addButtons(Composite parent) {

        buttons = new Composite(parent, getStyle());
        GridDataFactory.swtDefaults().applyTo(buttons);

        Button addEmptyColumn = new Button(buttons, SWT.PUSH);
        addEmptyColumn.setText(Messages.VeriniceLinkTableComposite_2);

        addEmptyColumn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                addColumn(null);
                refresh(true);

            }

        });

        Button cloneColumn = new Button(buttons, SWT.PUSH);
        cloneColumn.setText(Messages.VeriniceLinkTableComposite_3);

        cloneColumn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                VeriniceLinkTableColumn lastColumn = columns.get(columns.size() - 1);
                VeriniceLinkTableColumn duplicatedColumn = new VeriniceLinkTableColumn(
                        lastColumn, ++numCols);
                columns.add(duplicatedColumn);
                addDeleteButtonListener(duplicatedColumn);
                handleMoreThanOneColumn(false);
                refresh(true);

            }
        });

        GridLayoutFactory.swtDefaults().margins(DEFAULT_MARGIN).numColumns(2)
                .generateLayout(buttons);

    }

    public Composite getColumnsContainer() {
        return columnsContainer;
    }

    public void showComposite(Composite composite) {
        c2.showControl(composite);

    }

    public VeriniceLinkTable getContent() {

        updateVeriniceContent();
        return ltrContent;
    }

    public Set<String> getAllUsedRelationIds() {
        HashSet<String> relationIDs = new HashSet<>();

        for (VeriniceLinkTableColumn column : columns) {
            relationIDs.addAll(column.getFirstCombo().getAllUsedRelationIds());
        }

        return relationIDs;
    }

    public IObjectModelService getContentService() {
        return contentService;
    }

    public void addListener(VeriniceLinkTableFieldListener l) {
        if (l != null)
            listeners.add(l);
    }

    public void fireEvent(String fieldName, Object newValue, boolean isValid) {
        for (VeriniceLinkTableFieldListener l : listeners) {
            l.fieldValueChanged(isValid);
        }
    }

    public void moveColumnUp(VeriniceLinkTableColumn column) {
        int index = columns.indexOf(column);

        if (index < 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("column " + column + "not found");
            }
            return;
        }
        if (index == 0) {

            if (LOG.isDebugEnabled()) {
                LOG.debug("column " + column + " is first column, not possible to move up");
            }
            return;
        }
        VeriniceLinkTableColumn prevElement = columns.get(index - 1);
        columns.set(index - 1, column);
        columns.set(index, prevElement);
        column.getColumn().moveAbove(prevElement.getColumn());

        renameColumns();
        refresh(true);

    }

    public void moveColumnDown(VeriniceLinkTableColumn column) {
        int index = columns.indexOf(column);

        if (index < 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("column " + column + "not found");
            }
            return;
        }
        if (index == columns.size() - 1) {

            if (LOG.isDebugEnabled()) {
                LOG.debug("column " + column + " is last column, not possible to move down");
            }
            return;
        }
        VeriniceLinkTableColumn nextElement = columns.get(index + 1);
        columns.set(index + 1, column);
        columns.set(index, nextElement);
        column.getColumn().moveBelow(nextElement.getColumn());

        renameColumns();
        refresh(true);

    }

}
