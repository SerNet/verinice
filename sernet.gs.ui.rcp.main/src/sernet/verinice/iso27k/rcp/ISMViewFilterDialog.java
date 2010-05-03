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
 *     Robert Schuster <r.schuster@tarent.de> - rewritten to use set of classes
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.filter.TagFilter;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.verinice.iso27k.rcp.action.ISMViewFilter;

/**
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */
public class ISMViewFilterDialog extends Dialog {

    private static final Logger log = Logger.getLogger(ISMViewFilterDialog.class);

    private Composite container;

    private String[] tagPattern;
    private Group tagGroup;
    private CheckboxTableViewer viewer;
    private String[] checkedElements;

    private Group hideEmptyGroup;
    private Button hideEmptyCheckbox;
    private boolean hideEmpty;

    public ISMViewFilterDialog(Shell parent, ISMViewFilter ismViewFilter) {
        super(parent);
        setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE);
        this.tagPattern = ismViewFilter.getTagFilter().getPattern();
        this.hideEmpty = ismViewFilter.getHideEmptyFilter().isHideEmpty();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        container.setLayout(layout);

        Label intro = new Label(container, SWT.NONE);
        intro.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
        intro.setText(Messages.ISMViewFilterDialog_0);

        tagGroup = createTagfilterGroup(container);
        hideEmptyGroup = createHideEmptyGroup(container);

        initContent();
        container.layout();
        return container;
    }

    private Group createTagfilterGroup(Composite parent) {
        Group groupComposite = new Group(parent, SWT.BORDER);
        groupComposite.setText(Messages.ISMViewFilterDialog_1);
        GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1);
        groupComposite.setLayoutData(gridData);
        groupComposite.setLayout(new GridLayout(1, false));

        ScrolledComposite comp = new ScrolledComposite(groupComposite, SWT.V_SCROLL);
        comp.setLayoutData(new GridData(GridData.FILL_BOTH));
        comp.setExpandHorizontal(true);

        viewer = CheckboxTableViewer.newCheckList(comp, SWT.BORDER);
        Table table = viewer.getTable();
        table.setHeaderVisible(false);
        table.setLinesVisible(false);

        comp.setContent(viewer.getControl());

        // workaround to prevent tableviewer size from exceeding shell size:
        comp.setMinSize(100, 100);

        TableColumn checkboxColumn = new TableColumn(table, SWT.LEFT);
        checkboxColumn.setText(""); //$NON-NLS-1$
        checkboxColumn.setWidth(35);

        TableColumn imageColumn = new TableColumn(table, SWT.LEFT);
        imageColumn.setText(Messages.ISMViewFilterDialog_3);
        imageColumn.setWidth(100);

        viewer.setContentProvider(new ArrayContentProvider());

        viewer.setLabelProvider(new ITableLabelProvider() {

            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }

            public String getColumnText(Object element, int columnIndex) {
                if (columnIndex == 1) {
                    return (String) element;
                }
                return null;
            }

            public void addListener(ILabelProviderListener listener) {
            }

            public void dispose() {
            }

            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            public void removeListener(ILabelProviderListener listener) {
            }
        });

        return groupComposite;
    }

    /**
     * @param container2
     * @return
     */
    private Group createHideEmptyGroup(Composite parent) {
        Group groupComposite = new Group(parent, SWT.BORDER);
        GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1);
        groupComposite.setLayoutData(gridData);
        groupComposite.setLayout(new GridLayout(1, false));
        hideEmptyCheckbox = new Button(groupComposite, SWT.CHECK);
        hideEmptyCheckbox.setText("Hide empty groups");
        return groupComposite;
    }

    public String[] getCheckedElements() {
        return checkedElements;
    }

    public boolean getHideEmpty() {
        return hideEmpty;
    }

    public boolean isHideEmpty() {
        return getHideEmpty();
    }

    protected void initContent() {
        List<String> tags;
        try {
            tags = CnAElementHome.getInstance().getTags();
            tags.add(0, TagFilter.NO_TAG);
            viewer.setInput(tags);
        } catch (CommandException e) {
            ExceptionUtil.log(e, Messages.ISMViewFilterDialog_4);
        }

        // workaround to prevent tableviewer size from exceeding shell size:
        viewer.getTable().setSize(200, 200);

        if (tagPattern != null) {
            viewer.setCheckedElements(tagPattern);
        }
        tagGroup.getParent().layout(true);
        
        hideEmptyCheckbox.setSelection(getHideEmpty());
    }

    @Override
    public boolean close() {
        // get checked objects, cast to string:
        List<Object> tagList = Arrays.asList(viewer.getCheckedElements());
        this.checkedElements = tagList.toArray(new String[tagList.size()]);
        this.hideEmpty = hideEmptyCheckbox.getSelection();
        return super.close();
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.ISMViewFilterDialog_5);

        // workaround to prevent tableviewer size from exceeding shell size:
        newShell.setSize(400, 500);
    }

}
