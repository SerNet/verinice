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
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPartReference;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.DefaultModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.DocumentLink;
import sernet.verinice.model.bsi.DocumentReference;
import sernet.verinice.rcp.PartListenerAdapter;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.service.commands.task.FindURLs;

public class DocumentView extends RightsEnabledView {

    public static final String ID = "sernet.gs.ui.rcp.main.documentview"; //$NON-NLS-1$

    private TreeViewer viewer;

    private Action refreshAction;

    private IModelLoadListener loadListener = new DefaultModelLoadListener() {
        @Override
        public void closed(BSIModel model) {
            setInputAsync();
        }

        @Override
        public void loaded(final BSIModel model) {
            setInputAsync();
        }

        private void setInputAsync() {
            Display.getDefault().asyncExec(() -> {
                if (viewer.getContentProvider() != null) {
                    setInput();
                }
            });
        }
    };

    @Override
    public String getRightID() {
        return ActionRightIDs.DOCUMENTVIEW;
    }

    /*
     * @see sernet.verinice.rcp.RightsEnabledView#getViewId()
     */
    @Override
    public String getViewId() {
        return ID;
    }

    protected void setInput() {
        try {
            List<PropertyType> types = HUITypeFactory.getInstance().getURLPropertyTypes();
            Set<String> allIDs = types.stream().map(PropertyType::getId)
                    .collect(Collectors.toSet());
            FindURLs command = new FindURLs(allIDs);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            viewer.setInput(command.getUrls());
        } catch (Exception e) {
            // there's nothing we can do here
        }

    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        final int colum1Width = 200;
        final int colum2Width = 100;
        final int colum3Width = colum2Width;

        viewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.MULTI);
        viewer.getTree().setLinesVisible(true);
        viewer.getTree().setHeaderVisible(true);

        TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof DocumentLink) {
                    return ((DocumentLink) element).getName();
                }
                return ""; //$NON-NLS-1$
            }
        });
        column.getColumn().setText(Messages.DocumentView_2);

        TreeViewerColumn column2 = new TreeViewerColumn(viewer, SWT.NONE);
        column2.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof DocumentLink) {
                    return ((DocumentLink) element).getHref();
                }
                return ""; //$NON-NLS-1$
            }
        });
        column2.getColumn().setText(Messages.DocumentView_4);

        TreeViewerColumn column3 = new TreeViewerColumn(viewer, SWT.NONE);
        column3.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof DocumentReference) {
                    DocumentReference docref = (DocumentReference) element;
                    return docref.getCnaTreeElement().getTitle();
                }
                return ""; //$NON-NLS-1$
            }
        });
        column3.getColumn().setText(Messages.DocumentView_6);

        viewer.setContentProvider(new DocumentContentProvider(viewer));
        viewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                if (e1 instanceof DocumentLink && e2 instanceof DocumentLink) {
                    DocumentLink link1 = (DocumentLink) e1;
                    DocumentLink link2 = (DocumentLink) e2;
                    return link1.getName().compareTo(link2.getName());
                }
                if (e1 instanceof DocumentReference && e2 instanceof DocumentReference) {
                    DocumentReference ref1 = (DocumentReference) e1;
                    DocumentReference ref2 = (DocumentReference) e2;
                    return ref1.getCnaTreeElement().getTitle()
                            .compareTo(ref2.getCnaTreeElement().getTitle());
                }
                return 0;
            }
        });
        if (!Activator.getDefault().isStandalone()
                || Activator.getDefault().getInternalServer().isRunning()) {
            setInput();
        }

        column.getColumn().setWidth(colum1Width);
        column2.getColumn().setWidth(colum2Width);
        column3.getColumn().setWidth(colum3Width);

        makeActions();
        addDoubleClickListener();
        getSite().setSelectionProvider(viewer);
        CnAElementFactory.getInstance().addLoadListener(loadListener);
        fillLocalToolBar();
        addPartLister();
    }

    @Override
    public void setFocus() {
        // we don't handle the focus event
    }

    private void makeActions() {

        refreshAction = new Action(Messages.DocumentView_7, SWT.NONE) {
            @Override
            public void run() {
                setInput();
            }
        };

        refreshAction
                .setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.RELOAD));
    }

    private void addDoubleClickListener() {
        viewer.addDoubleClickListener(event -> {
            Object sel = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
            if (sel instanceof DocumentReference) {
                DocumentReference ref = (DocumentReference) sel;
                EditorFactory.getInstance().openEditor(ref.getCnaTreeElement());
            } else if (sel instanceof DocumentLink) {
                DocumentLink link = (DocumentLink) sel;
                Program.launch(link.getHref());
            }
        });
    }

    private void addPartLister() {
        this.getSite().getWorkbenchWindow().getPartService()
                .addPartListener(new PartListenerAdapter() {
                    /**
                     * this ensures a refresh on reopening the view
                     */
                    @Override
                    public void partActivated(IWorkbenchPartReference reference) {
                        if (!Activator.getDefault().isStandalone()
                                || Activator.getDefault().getInternalServer().isRunning()) {
                            setInput();
                        }
                    }
                });
    }

    private void fillLocalToolBar() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(this.refreshAction);
    }

}
