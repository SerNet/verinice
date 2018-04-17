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
package sernet.verinice.rcp.linktable.ui.combo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import sernet.verinice.rcp.linktable.ui.LinkTableColumn;

/**
 * 
 * Base class for the different Combo-tpes of a vlt column.
 * 
 * 
 * @see LinkTableColumn
 * @see LinkTableOperationTypeComboViewer
 * @see LinkTableElementComboViewer
 * @see LinkTablePropertyComboViewer
 * @see LinkTableRelationPropertyComboViewer
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public abstract class LinkTableComboViewer extends ComboViewer
        implements IStructuredContentProvider, ISelectionChangedListener {

    protected LinkTableColumn ltrColumn;
    protected LinkTableComboViewer rightCombo = null;
    protected LinkTableComboViewer leftCombo = null;
    Composite parentComposite = null;

    protected String relatedID;
    protected LinkTableOperationType operationType;

    public LinkTableComboViewer(LinkTableComboViewer leftCombo, String relatedID,
            LinkTableOperationType operationType, LinkTableColumn ltrParent, Composite parent) {

        super(new Combo(parent, SWT.NONE | SWT.READ_ONLY));
        this.parentComposite = parent;
        this.ltrColumn = ltrParent;
        this.setContentProvider(this);
        this.relatedID = relatedID;
        this.operationType = operationType;
        this.leftCombo = leftCombo;

        this.addSelectionChangedListener(this);

        this.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return getLabelText(element).replaceAll("\n", "");
            }

        });

        this.setInput(new Object());
        refreshCombo();

    }

    protected abstract String getLabelText(Object element);

    /*
     * @see
     * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
     * .viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    /*
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose() {
        if (leftCombo != null) {
            parentComposite.dispose();
        }

    }

    /*
     * @see
     * org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.
     * eclipse.jface.viewers.SelectionChangedEvent)
     */
    @Override
    public void selectionChanged(SelectionChangedEvent event) {

        doSelectionChanged();
        if (rightCombo != null) {
            rightCombo.selectionChanged(event);
        } else {
            Control newComposite = createRightCombo();
            refreshViewer();
            ltrColumn.getLtrParent().showComposite(newComposite);
        }
    }

    private Control createRightCombo() {

        Composite newParent = new Composite(parentComposite, SWT.NONE);
        newParent.setLayout(new FormLayout());
        newParent.setLayoutData(getDefaultFormData(getCombo()));
        rightCombo = createChild(newParent);
        newParent.pack(true);
        newParent.layout(true);
        return newParent;
    }

    protected abstract void doSelectionChanged();

    protected abstract LinkTableComboViewer createChild(Composite parent);

    public LinkTableComboViewer copy(LinkTableComboViewer leftCombo, Composite newParent,
            Control formerElement) {

        Composite newParentComposite = new Composite(newParent, SWT.NONE);
        newParentComposite.setLayout(new FormLayout());
        newParentComposite.setLayoutData(getDefaultFormData(formerElement));

        LinkTableComboViewer newViewer = createCopy(leftCombo, ltrColumn, newParentComposite);

        newViewer.getCombo().select(this.getCombo().getSelectionIndex());
        if (rightCombo != null) {
            newViewer.rightCombo = rightCombo.copy(newViewer, newParentComposite,
                    newViewer.getCombo());
            newViewer.rightCombo.leftCombo = newViewer;
        }
        return newViewer;
    }

    protected FormData getDefaultFormData(Control formerElement) {
        FormData comboData = new FormData();
        comboData.left = new FormAttachment(formerElement, LinkTableColumn.DEFAULT_GAP);
        comboData.top = new FormAttachment(formerElement, 0, SWT.CENTER);
        return comboData;
    }

    protected abstract LinkTableComboViewer createCopy(LinkTableComboViewer leftCombo,
            LinkTableColumn ltrParent2, Composite newParent);

    public String getValue() {
        return getCombo().getText();
    }

    public String getCurrentSelection() {
        StructuredSelection selection = (StructuredSelection) this.getSelection();
        return selection != null && selection.getFirstElement() != null
                ? selection.getFirstElement().toString()
                : "";
    }

    public void refreshViewer() {
        super.refresh();
        getCombo().pack(true);
        if (parentComposite != null && !parentComposite.isDisposed()) {
            parentComposite.pack(true);
            parentComposite.layout(true);
        }
        ltrColumn.refresh();
        getCombo().layout(true);
        if (rightCombo != null) {
            rightCombo.refresh();
        }
    }

    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuilder toString = new StringBuilder(
                "Comboviewer " + this.getClass().getSimpleName());
        if (leftCombo != null) {
            toString.append(", leftCombo of type " + leftCombo.getClass().getSimpleName());
            toString.append(", selected: '" + leftCombo.getCurrentSelection());
            toString.append("', disposed: ");
            if (leftCombo.parentComposite != null) {
                toString.append(leftCombo.parentComposite.isDisposed());
            } else {
                toString.append("true");
            }
        } else {
            toString.append(", leftCombo is null!!");
        }

        return toString.toString();
    }

    protected String[] sortElementsByLabel(String[] typeIDs) {
        String [] result = new String[typeIDs.length];
        System.arraycopy(typeIDs, 0, result, 0, typeIDs.length);

        final Map<String, String> translations = new HashMap<>(typeIDs.length);
        for (String typeID : typeIDs) {
            translations.put(typeID, ltrColumn.getContentService().getLabel(typeID));
        }

        Arrays.sort(result, new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                return translations.get(o1).compareTo(translations.get(o2));
            }
        });

        return result;
    }

    protected void selectFirstElement(boolean selectionChangedEvent) {
        this.getCombo().select(0);
        if (selectionChangedEvent) {
            this.selectionChanged(null);
        }
    }

    protected void refreshCombo() {

        this.getCombo().pack(true);
    }

    public String getColumnPath() {

        String message = getCurrentSelection();
        if (rightCombo != null) {
            message += rightCombo.getColumnPath();
        }
        return message;
    }

    public void setColumnPath(List<String> path) {

        if (!path.isEmpty()) {
            select(path.get(0));
            if (path.size() > 1) {
                selectionChanged(null);
                rightCombo.setColumnPath(path.subList(1, path.size()));
            }
        }

    }

    public void setColumnPath(String firstElement, List<String> path) {

        if (!path.isEmpty()) {
            select(firstElement);
            if (path.size() > 1) {
                selectionChanged(null);
                rightCombo.setColumnPath(path.subList(1, path.size()));
            }
        }

    }

    protected abstract void select(String string);

    public Set<String> getAllUsedRelationIds() {

        HashSet<String> set = new HashSet<>();

        set.addAll(doGetAllRelationTypes());

        if (rightCombo != null) {
            set.addAll(rightCombo.getAllUsedRelationIds());
        }
        return set;
    }

    protected abstract Set<String> doGetAllRelationTypes();

}
