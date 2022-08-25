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
package sernet.verinice.rcp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.dialogs.Messages;
import sernet.gs.ui.rcp.main.bsi.filter.TextFilter;
import sernet.gs.ui.rcp.main.bsi.views.CnAImageProvider;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.IAbbreviatedElement;
import sernet.hui.common.connect.IIdentifiableElement;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.CnATreeElementLabelGenerator;
import sernet.verinice.service.commands.LoadCnAElementByEntityTypeId;
import sernet.verinice.service.commands.LoadContainingObjects;
import sernet.verinice.service.commands.LoadElementTitles;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ElementSelectionComponent {

    private static final Logger log = Logger.getLogger(ElementSelectionComponent.class);

    private Composite container;

    private TableViewer viewer;
    private Text text;
    private Button checkbox;

    private CnATreeElementTitleFilter filter;

    private List<CnATreeElement> elementList;
    private Integer scopeId;
    private Integer groupId;
    private Set<String> typeIDs;
    private boolean scopeOnly;
    private boolean showScopeCheckbox;
    private static final String COLUMN_IMG = "_img"; //$NON-NLS-1$
    private static final String COLUMN_SCOPE_ID = "_scope_id"; //$NON-NLS-1$
    private static final String COLUMN_LABEL = "_label"; //$NON-NLS-1$
    private static Map<Integer, String> titleMap = new HashMap<>();

    private static final Comparator<CnATreeElement> BY_NAME = (elmt1, elmt2) -> {
        if (titleMap != null) {
            String title1 = titleMap.get(elmt1.getScopeId());
            String title2 = titleMap.get(elmt2.getScopeId());
            if (title1 != null && title2 != null) {
                int allScopeTitles = title1.compareTo(title2);
                if (allScopeTitles == 0) {
                    return makeTitle(elmt1).compareTo(makeTitle(elmt2));
                }
                return title1.compareTo(title2);
            } else {
                if (title1 == null && title2 == null) {
                    return makeTitle(elmt1).compareTo(makeTitle(elmt2));
                }
                if (title1 == null) {
                    return 1;
                }
                // title2 == null
                return -1;
            }
        }
        return makeTitle(elmt1).compareTo(makeTitle(elmt2));
    };

    private static final Comparator<CnATreeElement> BY_SCOPE = (elmt1, elmt2) -> {
        return getScopeTitle(elmt1).compareTo(getScopeTitle(elmt2));
    };

    private Map<Integer, CnATreeElement> containingObjectsByElementId;

    private final Comparator<CnATreeElement> byContainingObject = (elmt1, elmt2) -> {

        String text1 = getContainingObjectTitle(elmt1).orElse(null);
        String text2 = getContainingObjectTitle(elmt2).orElse(null);

        if (text1 != null && text2 != null) {
            return text1.compareTo(text2);
        }
        if (text1 == null) {
            return 1;
        }
        // text2 == null
        return -1;

    };

    private List<CnATreeElement> selectedElements = new ArrayList<>();

    private Integer height;

    private boolean includeCompendiumElements;

    private boolean showContainingObject;

    public ElementSelectionComponent(Composite container, String type, Integer scopeId) {
        this(container, type, scopeId, null);
    }

    public ElementSelectionComponent(Composite container, String type, Integer scopeId,
            Integer groupId) {
        this(container, Collections.singleton(type), scopeId, groupId);
    }

    public ElementSelectionComponent(Composite container, Set<String> typeIDs, Integer scopeId,
            Integer groupId) {
        super();
        this.container = container;
        this.typeIDs = typeIDs;
        this.scopeId = scopeId;
        this.groupId = groupId;
        scopeOnly = true;
        showScopeCheckbox = true;
    }

    @SuppressWarnings("unchecked")
    public void init() {

        final int formAttachmentDefaultOffset = 5;
        final int column1Width = 25;
        final int column2Width = 200;
        final int column3Width = 150;
        final int pathColomnWidth = 250;

        final int formData2Numerator = 100;
        final int formData3Numerator = formData2Numerator;
        container.setLayout(new FormLayout());

        Label label1 = new Label(container, SWT.NULL);
        label1.setText(Messages.CnATreeElementSelectionDialog_3);

        FormData formData = new FormData();
        formData.top = new FormAttachment(0, formAttachmentDefaultOffset);
        formData.left = new FormAttachment(0, formAttachmentDefaultOffset);
        label1.setLayoutData(formData);
        label1.pack();

        text = new Text(container, SWT.BORDER);
        FormData formData2 = new FormData();
        formData2.top = new FormAttachment(0, formAttachmentDefaultOffset);
        formData2.left = new FormAttachment(label1, formAttachmentDefaultOffset);
        formData2.right = new FormAttachment(formData2Numerator,
                (-1) * formAttachmentDefaultOffset);
        text.setLayoutData(formData2);
        text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filter.setPattern(text.getText());
            }
        });

        if (isShowScopeCheckbox()) {
            SelectionListener listener = new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Button button = (e.getSource() instanceof Button) ? (Button) e.getSource()
                            : null;
                    if (button != null) {
                        scopeOnly = button.getSelection();
                        loadElements();
                    }
                }

            };
            checkbox = SWTElementFactory.generateCheckboxButton(container,
                    Messages.CnATreeElementSelectionDialog_4, true, listener);
            FormData checkboxFD = new FormData();
            checkboxFD.top = new FormAttachment(text, formAttachmentDefaultOffset);
            checkboxFD.left = new FormAttachment(0, formAttachmentDefaultOffset);
            checkbox.setLayoutData(checkboxFD);
            checkbox.pack();
        }

        viewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        FormData formData3 = new FormData();
        if (isShowScopeCheckbox()) {
            formData3.top = new FormAttachment(checkbox, formAttachmentDefaultOffset);
        } else {
            formData3.top = new FormAttachment(text, formAttachmentDefaultOffset);
        }
        formData3.left = new FormAttachment(0, formAttachmentDefaultOffset);
        formData3.right = new FormAttachment(formData3Numerator,
                (-1) * formAttachmentDefaultOffset);
        formData3.bottom = new FormAttachment(formData3Numerator,
                (-1) * formAttachmentDefaultOffset);
        if (getHeight() != null) {
            formData3.height = getHeight();
        }
        viewer.getTable().setLayoutData(formData3);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setLinesVisible(true);

        // image column:
        TableViewerColumn column1 = new TableViewerColumn(viewer, SWT.LEFT);
        column1.getColumn().setText(""); //$NON-NLS-1$
        column1.getColumn().setWidth(column1Width);
        column1.getColumn().setResizable(false);
        column1.setLabelProvider(new ImageColumnCellLabelProvider());

        // label column
        TableViewerColumn column2 = new TableViewerColumn(viewer, SWT.LEFT);
        column2.getColumn().setText(Messages.CnATreeElementSelectionDialog_9);
        column2.getColumn().setResizable(true);
        column2.getColumn().setWidth(column2Width);
        column2.getColumn().setData(BY_NAME);
        column2.setLabelProvider(new LabelColumnCellLabelProvider());

        // scope id column:
        TableViewerColumn column3 = new TableViewerColumn(viewer, SWT.LEFT);
        column3.getColumn().setText(Messages.CnATreeElementSelectionDialog_10);
        column3.getColumn().setWidth(column3Width);
        column3.getColumn().setResizable(true);
        column3.getColumn().setData(BY_SCOPE);
        column3.setLabelProvider(new ScopeIdColumnCellLabelProvider());

        // path column:
        if (showContainingObject) {
            TableViewerColumn containingObject = new TableViewerColumn(viewer, SWT.LEFT);
            containingObject.getColumn().setText(Messages.ContainingObject);
            containingObject.getColumn().setWidth(pathColomnWidth);
            containingObject.getColumn().setResizable(true);
            containingObject.getColumn().setData(byContainingObject);
            containingObject.setLabelProvider(new ContainingObjectLabelProvider());

        }
        Table table = viewer.getTable();
        TableColumn[] columns = table.getColumns();

        // index starts at 1, we don't want to sort after the image column
        for (int i = 1; i < columns.length; i++) {
            TableColumn column = columns[i];
            column.addListener(SWT.Selection, e -> {
                final TableColumn sortColumn = table.getSortColumn();
                int direction = table.getSortDirection();

                if (column.equals(sortColumn)) {
                    direction = direction == SWT.UP ? SWT.DOWN : SWT.UP;
                } else {
                    table.setSortColumn(column);
                    direction = SWT.UP;
                }
                table.setSortDirection(direction);
                viewer.refresh();
            });
        }
        table.setSortColumn(columns[1]);
        table.setSortDirection(SWT.DOWN);

        viewer.setColumnProperties(new String[] { COLUMN_IMG, COLUMN_SCOPE_ID, COLUMN_LABEL });
        viewer.setContentProvider(new ArrayContentProvider());
        filter = new CnATreeElementTitleFilter(viewer);

        viewer.setComparator(new ElementTableViewerComparator());

        viewer.addSelectionChangedListener(
                event -> selectedElements = ((IStructuredSelection) viewer.getSelection())
                        .toList());

        viewer.addDoubleClickListener(
                event -> selectedElements = ((IStructuredSelection) viewer.getSelection())
                        .toList());
    }

    public void loadElements() {
        loadElementsAndSelect(null);
    }

    public void loadElementsAndSelect(final CnATreeElement selected) {
        if (typeIDs == null || typeIDs.isEmpty()) {
            return;
        }
        viewer.setInput(Collections
                .singletonList(new PlaceHolder(Messages.CnATreeElementSelectionDialog_6)));

        WorkspaceJob job = new WorkspaceJob(Messages.CnATreeElementSelectionDialog_7) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                Activator.inheritVeriniceContextState();

                try {
                    monitor.setTaskName(Messages.CnATreeElementSelectionDialog_8);
                    loadElementsFromDb();
                    setSelectedElement(selected);
                } catch (Exception e) {
                    ExceptionUtil.log(e, Messages.CnATreeElementSelectionDialog_0);
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.schedule();
    }

    protected void setSelection() {
        if (selectedElements != null && !selectedElements.isEmpty()) {
            getViewer().setSelection(new StructuredSelection(selectedElements));
        }
    }

    private static String makeTitle(CnATreeElement elmt) {
        return CnATreeElementLabelGenerator.getElementTitle(elmt);
    }

    public List<CnATreeElement> getSelectedElements() {
        return selectedElements;
    }

    public Composite getContainer() {
        return container;
    }

    public TableViewer getViewer() {
        return viewer;
    }

    public boolean isScopeOnly() {
        return scopeOnly;
    }

    public void setScopeOnly(boolean scopeOnly) {
        this.scopeOnly = scopeOnly;
        if (this.checkbox != null) {
            this.checkbox.setSelection(scopeOnly);
        }
    }

    public boolean isShowScopeCheckbox() {
        return showScopeCheckbox;
    }

    public void setShowScopeCheckbox(boolean showScopeCheckbox) {
        this.showScopeCheckbox = showScopeCheckbox;
    }

    public void setIncludeCompendiumElements(boolean includeCompendiumElements) {
        this.includeCompendiumElements = includeCompendiumElements;
    }

    public void setShowContainingObject(boolean showContainingObject) {
        this.showContainingObject = showContainingObject;
    }

    private void loadElementsFromDb() {
        List<CnATreeElement> elements = typeIDs.stream().flatMap(typeId -> {
            LoadCnAElementByEntityTypeId command;
            if (scopeOnly) {
                command = new LoadCnAElementByEntityTypeId(typeId, getScopeId(), getGroupId());
            } else {
                command = new LoadCnAElementByEntityTypeId(typeId, includeCompendiumElements);
            }
            try {
                command = ServiceFactory.lookupCommandService().executeCommand(command);
            } catch (CommandException e) {
                throw new RuntimeCommandException(e);
            }
            return command.getElements().stream();
        }).collect(Collectors.toList());
        if (showContainingObject && !elements.isEmpty()) {
            try {
                LoadContainingObjects command = new LoadContainingObjects(elements);
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                containingObjectsByElementId = command.getResult();

            } catch (CommandException e) {
                log.error("Error loading parent target object", e);
            }
        }
        showElementsInTable(elements);
    }

    private void showElementsInTable(final List<CnATreeElement> list) {
        Display.getDefault().asyncExec(() -> {
            if (list != null) {
                viewer.setInput(list);
            } else {
                viewer.setInput(Collections.emptyList());
            }
        });
        elementList = list;
    }

    public void deselectElements() {
        Display.getDefault().syncExec(() -> {
            getViewer().getTable().deselectAll();
            getViewer().setSelection(new StructuredSelection());
            selectedElements.clear();
        });
    }

    @SuppressWarnings("unchecked")
    public void setSelectedElement(final CnATreeElement selectedElement) {
        if (selectedElement != null) {
            int i = elementList.indexOf(selectedElement);
            if (i != -1) {
                Display.getDefault().syncExec(() -> {
                    getViewer().getTable().deselectAll();
                    getViewer().setSelection(new StructuredSelection(selectedElement));
                    selectedElements = ((IStructuredSelection) viewer.getSelection()).toList();
                });
            }
        }
    }

    public Integer getScopeId() {
        return scopeId;
    }

    public void setScopeId(Integer scopeId) {
        this.scopeId = scopeId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public void setTypeId(String typeId) {
        this.typeIDs = Collections.singleton(typeId);
    }

    public void setTypeIds(Set<String> typeIds) {
        this.typeIDs = typeIds;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    private static String getScopeTitle(CnATreeElement elmt) {
        String title = "";

        try {
            if (!titleMap.containsKey(elmt.getScopeId())) {
                title = loadElementsTitles(elmt);
            } else {
                title = titleMap.get(elmt.getScopeId());
            }
        } catch (CommandException e) {
            log.error("Error while getting element", e);
        }
        return title;
    }

    private Optional<String> getContainingObjectTitle(CnATreeElement elmt) {
        CnATreeElement containingObject = containingObjectsByElementId.get(elmt.getDbId());
        return Optional.ofNullable(containingObject)
                .map(CnATreeElementLabelGenerator::getElementTitle);
    }

    private static String loadElementsTitles(CnATreeElement elmt) throws CommandException {
        LoadElementTitles scopeCommand;
        scopeCommand = new LoadElementTitles();
        scopeCommand = ServiceFactory.lookupCommandService().executeCommand(scopeCommand);
        titleMap = scopeCommand.getElements();
        return titleMap.get(elmt.getScopeId());
    }

    private static final class ElementTableViewerComparator extends ViewerComparator {
        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            Table table = ((TableViewer) viewer).getTable();
            TableColumn sortColumn = table.getSortColumn();
            Comparator comparator = sortColumn == null ? null : (Comparator) sortColumn.getData();
            if (comparator != null && table.getSortDirection() == SWT.UP) {
                comparator = comparator.reversed();
            }
            return comparator == null ? 0 : comparator.compare(e1, e2);
        }
    }

    private static final class ScopeIdColumnCellLabelProvider extends CellLabelProvider {
        @Override
        public void update(ViewerCell cell) {
            if (cell.getElement() instanceof PlaceHolder) {
                cell.setText(((PlaceHolder) cell.getElement()).getTitle());
                return;
            }
            CnATreeElement elmt = (CnATreeElement) cell.getElement();

            String title = getScopeTitle(elmt);
            cell.setText(title);
        }

    }

    private static final class LabelColumnCellLabelProvider extends CellLabelProvider {
        @Override
        public void update(ViewerCell cell) {
            if (cell.getElement() instanceof PlaceHolder) {
                cell.setText(((PlaceHolder) cell.getElement()).getTitle());
                return;
            }
            cell.setText(makeTitle((CnATreeElement) cell.getElement()));
        }
    }

    private static final class ImageColumnCellLabelProvider extends CellLabelProvider {
        @Override
        public void update(ViewerCell cell) {
            if (cell.getElement() instanceof PlaceHolder) {
                return;
            }
            CnATreeElement element = (CnATreeElement) cell.getElement();
            Image image = CnAImageProvider.getImage(element);
            cell.setImage(image);
        }
    }

    private final class ContainingObjectLabelProvider extends CellLabelProvider {
        @Override
        public void update(ViewerCell cell) {
            if (cell.getElement() instanceof PlaceHolder) {
                cell.setText(((PlaceHolder) cell.getElement()).getTitle());
                return;
            }

            CnATreeElement element = (CnATreeElement) cell.getElement();
            getContainingObjectTitle(element).ifPresent(cell::setText);
        }
    }

    private class CnATreeElementTitleFilter extends TextFilter {

        public CnATreeElementTitleFilter(TableViewer viewer) {
            super(viewer);
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (!(element instanceof CnATreeElement) || getRegex() == null) {
                return true;
            }
            CnATreeElement elmt = (CnATreeElement) element;

            if (getRegex().matcher(elmt.getTitle()).find()) {
                return true;
            }

            if (elmt instanceof IAbbreviatedElement
                    && getRegex().matcher(((IAbbreviatedElement) elmt).getAbbreviation()).find()) {
                return true;
            }

            if (elmt instanceof IIdentifiableElement
                    && getRegex().matcher(((IIdentifiableElement) elmt).getIdentifier()).find()) {
                return true;
            }

            if (getRegex().matcher(getScopeTitle(elmt)).find()) {
                return true;
            }

            if (showContainingObject) {
                Optional<String> containingObjectTitle = getContainingObjectTitle(elmt);
                if (containingObjectTitle.isPresent()
                        && getRegex().matcher(containingObjectTitle.get()).find()) {
                    return true;
                }
            }
            return false;
        }
    }
}