/*******************************************************************************
 * Copyright (c) 2020 Finn Westendorf
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
 ******************************************************************************/
package sernet.verinice.bp.rcp.consolidator;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.databinding.viewers.IViewerObservableSet;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.swt.SWTResourceManager;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.service.commands.bp.ConsolidatorCheckPermissionsCommand;
import sernet.verinice.service.commands.bp.ConsolidatorCheckPermissionsCommand.PermissionDeniedReason;

/**
 * This is the page where the user selects the modules for the consolidator.
 */
public class ModuleSelectionPage extends WizardPage {
    private List<ConsolidatorTableContent> allModules;
    private CheckboxTreeViewer treeViewer;
    private ConsolidatorWizard wizard;
    private Map<Integer, PermissionDeniedReason> moduleIDsWithWritePermissionIssues = Collections
            .emptyMap();

    public ModuleSelectionPage(ConsolidatorWizard consolidatorWizard,
            @NonNull List<ConsolidatorTableContent> allModules) {
        super("wizardPage");
        this.wizard = consolidatorWizard;
        setPageComplete(false);
        this.allModules = allModules;
        setTitle(Messages.selectModules);
        setDescription(Messages.selectTheModulesToBeConsolidated);
        WritableSet<ConsolidatorTableContent> selectedModules = wizard.getSelectedModules();
        selectedModules.addChangeListener(event -> {

            setPageComplete(!selectedModules.isEmpty());
            setMessage(null, WARNING);
            if (!moduleIDsWithWritePermissionIssues.isEmpty()) {
                Set<Integer> moduleIDs = new HashSet<>(selectedModules.stream()
                        .map(it -> it.getModule().getDbId()).collect(Collectors.toSet()));
                moduleIDs.retainAll(moduleIDsWithWritePermissionIssues.keySet());
                if (!moduleIDs.isEmpty()) {
                    setMessage(Messages.nonWritableModulesWarning, WARNING);
                }
            }
        });
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);

        setControl(container);
        container.setLayout(new GridLayout(1, false));

        treeViewer = new CheckboxTreeViewer(container, SWT.BORDER);

        Tree tree = treeViewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        TreeViewerColumn tableViewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        TreeColumn tblclmnNewColumn = tableViewerColumn.getColumn();
        tblclmnNewColumn.setWidth(160);
        tblclmnNewColumn.setText(Messages.title);
        tblclmnNewColumn.setData(Comparator.comparing(ConsolidatorTableContent::getTitle));

        TreeViewerColumn tableViewerColumn1 = new TreeViewerColumn(treeViewer, SWT.NONE);
        TreeColumn tblclmnNewColumn1 = tableViewerColumn1.getColumn();
        tblclmnNewColumn1.setWidth(200);
        tblclmnNewColumn1.setText(Messages.scope);
        tblclmnNewColumn1.setData(Comparator.comparing(ConsolidatorTableContent::getScope));

        TreeViewerColumn tableViewerColumn2 = new TreeViewerColumn(treeViewer, SWT.NONE);
        TreeColumn tblclmnNewColumn2 = tableViewerColumn2.getColumn();
        tblclmnNewColumn2.setWidth(100);
        tblclmnNewColumn2.setText(Messages.parent);
        tblclmnNewColumn2.setData(Comparator.comparing(ConsolidatorTableContent::getParent));
        initDataBindings(new DataBindingContext());

        treeViewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                Tree tree = ((CheckboxTreeViewer) viewer).getTree();
                TreeColumn sortColumn = tree.getSortColumn();
                Comparator comparator = sortColumn == null ? null
                        : (Comparator) sortColumn.getData();
                if (comparator != null && tree.getSortDirection() == SWT.UP) {
                    comparator = comparator.reversed();
                }
                return comparator == null ? 0 : comparator.compare(e1, e2);
            }
        });
        TreeColumn[] columns = tree.getColumns();

        for (int i = 0; i < columns.length; i++) {
            TreeColumn column = columns[i];
            column.addListener(SWT.Selection, e -> {
                final TreeColumn sortColumn = tree.getSortColumn();
                int direction = tree.getSortDirection();

                if (column.equals(sortColumn)) {
                    direction = direction == SWT.UP ? SWT.DOWN : SWT.UP;
                } else {
                    tree.setSortColumn(column);
                    direction = SWT.DOWN;
                }
                tree.setSortDirection(direction);
                treeViewer.refresh();
            });
        }
        tree.setSortColumn(columns[0]);
        tree.setSortDirection(SWT.DOWN);

        if (allModules.isEmpty()) {
            setErrorMessage(Messages.noMatchingModules);
        } else {
            if (!Activator.getDefault().isStandalone()) {

                Set<Integer> moduleIDs = allModules.stream().map(it -> it.getModule().getDbId())
                        .collect(Collectors.toSet());

                ConsolidatorCheckPermissionsCommand checkPermissionsCommand = new ConsolidatorCheckPermissionsCommand(
                        moduleIDs, true, true, true, true);
                try {
                    checkPermissionsCommand = ServiceFactory.lookupCommandService()
                            .executeCommand(checkPermissionsCommand);
                    moduleIDsWithWritePermissionIssues = checkPermissionsCommand
                            .getPermissionIssues();
                    treeViewer.refresh(true);
                } catch (CommandException e) {
                    throw new RuntimeCommandException("Error checking write permissions", e);
                }
            }
        }
    }

    // This code is generated, when re-generating, add SuppressWarnings again.
    protected void initDataBindings(DataBindingContext bindingContext) {
        @SuppressWarnings("rawtypes")
        final IObservableFactory factory = target -> {
            if (target instanceof IObservable) {
                return (IObservable) target;
            }
            return null;
        };
        @SuppressWarnings("unchecked")
        final ObservableListTreeContentProvider<ConsolidatorTableContent> contentProvider = new ObservableListTreeContentProvider<>(
                factory, null);
        treeViewer.setContentProvider(contentProvider);

        IObservableMap<ConsolidatorTableContent, Object>[] observeMaps = observeMaps(
                contentProvider.getKnownElements(), ConsolidatorTableContent.class,
                new String[] { "title", "scope", "parent" });
        treeViewer.setLabelProvider(new ModuleSelectionLabelProvider(observeMaps));
        //
        IObservableList<Object> selfList = Properties.selfList(ConsolidatorTableContent.class)
                .observe(allModules.stream().map(x -> (Object) x).collect(Collectors.toList()));
        treeViewer.setInput(selfList);

        IViewerObservableSet<ConsolidatorTableContent> checkboxTreeViewerObserveCheckedElements = ViewerProperties
                .checkedElements(ConsolidatorTableContent.class).observe((Viewer) treeViewer);
        bindingContext.bindSet(checkboxTreeViewerObserveCheckedElements,
                wizard.getSelectedModules(), null, null);
    }

    private static <E> IObservableMap<E, Object>[] observeMaps(IObservableSet<E> domain,
            Class<E> type, String[] propertyNames) {
        @SuppressWarnings("unchecked")
        IObservableMap<E, Object>[] result = new IObservableMap[propertyNames.length];
        for (int i = 0; i < propertyNames.length; i++) {
            result[i] = PojoProperties.value(type, propertyNames[i]).observeDetail(domain);
        }
        return result;
    }

    private class ModuleSelectionLabelProvider extends ObservableMapLabelProvider
            implements IColorProvider, IFontProvider {

        private final Font italicFont;

        public ModuleSelectionLabelProvider(
                IObservableMap<ConsolidatorTableContent, Object>[] observeMaps) {
            super(observeMaps);
            Font defaultFont = JFaceResources.getDefaultFont();
            FontData fontData = defaultFont.getFontData()[0];
            italicFont = SWTResourceManager.getFont(fontData.getName(), fontData.getHeight(),
                    SWT.ITALIC);
        }

        @Override
        public Color getForeground(Object element) {
            ConsolidatorTableContent c = (ConsolidatorTableContent) element;
            if (moduleIDsWithWritePermissionIssues.containsKey(c.getModule().getDbId())) {
                return SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY);
            }
            return null;
        }

        @Override
        public Color getBackground(Object element) {
            return null;
        }

        @Override
        public Font getFont(Object element) {
            ConsolidatorTableContent c = (ConsolidatorTableContent) element;
            if (moduleIDsWithWritePermissionIssues.containsKey(c.getModule().getDbId())) {
                return italicFont;
            }
            return null;
        }

    }
}
