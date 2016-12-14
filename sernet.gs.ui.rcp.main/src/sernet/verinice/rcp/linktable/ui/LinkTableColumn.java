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

package sernet.verinice.rcp.linktable.ui;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import sernet.verinice.rcp.linktable.ui.combo.LinkTableComboViewer;
import sernet.verinice.rcp.linktable.ui.combo.LinkTableElementComboViewer;
import sernet.verinice.rcp.linktable.ui.combo.LinkTableOperationType;
import sernet.verinice.service.linktable.ColumnPathParser;
import sernet.verinice.service.model.HUIObjectModelService;
import sernet.verinice.service.model.IObjectModelService;

import java.util.List;

/**
 * Container for all widgets needed for a vlt-table column.
 * 
 * @see LinkTableComboViewer
 * @see LinkTableComposite
 * @author Ruth Motza {@literal <rm[at]sernet[dot]de>}
 */
public class LinkTableColumn {

    private static final Logger logger = Logger.getLogger(LinkTableColumn.class);

    public static final int DEFAULT_GAP = 6;

    private LinkTableComposite ltrParent;
    private int columnNumber;
    private String name;
    private Button deleteButton;
    private Composite columnContainer;

    private Button selectButton;

    private IObjectModelService contentService;
    private LinkTableElementComboViewer firstCombo;

    public LinkTableColumn(LinkTableColumn copy, int number) {

        this.ltrParent = copy.ltrParent;
        this.columnNumber = number;
        this.contentService = copy.getContentService();
        createColumn();
        firstCombo = (LinkTableElementComboViewer) copy.getFirstCombo().copy(null, columnContainer,
                selectButton);
    }

    public LinkTableColumn(LinkTableComposite parent, int style, int number) {

        this.ltrParent = parent;
        this.columnNumber = number;
        this.contentService = parent.getContentService();
        createColumn();
        addFirstCombo();
    }

    public LinkTableColumn(List<String> path, LinkTableComposite parent, int number) {

        this.ltrParent = parent;
        this.columnNumber = number;
        this.contentService = parent.getContentService();

        createColumn();

        addFirstCombo();
        firstCombo.setInput(new Object());
        firstCombo.setColumnPath(ColumnPathParser.removeAlias(path));
    }

    public LinkTableColumn(ISelection selection, List<String> path, LinkTableComposite parent,
            int number) {

        this.ltrParent = parent;
        this.columnNumber = number;
        this.contentService = parent.getContentService();

        createColumn();

        addFirstCombo();
        firstCombo.setInput(new Object());
        StructuredSelection element = (StructuredSelection) selection;
        firstCombo.setColumnPath(element.getFirstElement().toString(),
                ColumnPathParser.removeAlias(path));
    }

    private void addFirstCombo() {
        firstCombo = new LinkTableElementComboViewer(this, columnContainer);
        FormData comboData = new FormData();
        comboData.left = new FormAttachment(selectButton, DEFAULT_GAP);
        comboData.top = new FormAttachment(selectButton, 0, SWT.CENTER);
        firstCombo.getCombo().setLayoutData(comboData);
        columnContainer.layout(true);

    }

    private void createColumn() {

        columnContainer = new Composite(ltrParent.getColumnsContainer(),
                SWT.NONE | SWT.NO_RADIO_GROUP);

        FormLayout layoutColumn = new FormLayout();
        layoutColumn.marginHeight = 5;
        layoutColumn.marginWidth = 0;
        columnContainer.setLayout(layoutColumn);

        Listener radioGroup = new SelectColumnListener(this);

        selectButton = new Button(columnContainer, SWT.RADIO);
        FormData selectButtonFormData = new FormData();
        selectButton.setLayoutData(selectButtonFormData);
        selectButton.addListener(SWT.Selection, radioGroup);
    }

    private final class SelectColumnListener implements Listener {

        private LinkTableColumn selectedColumn;

        public SelectColumnListener(LinkTableColumn selectedColumn) {
            this.selectedColumn = selectedColumn;
        }

        @Override
        public void handleEvent(Event event) {
            for (LinkTableColumn column : ltrParent.getColumns()) {
                column.getSelectButton().setSelection(false);
            }
            Button selectedButton = (Button) event.widget;
            selectedButton.setSelection(true);
            ltrParent.setSelectedColumn(selectedColumn);
        }
    }

    public void setColumnNumber(int num) {
        columnNumber = num;
        columnContainer.pack(true);
        columnContainer.layout(true);
    }

    Button getDeleteButton() {
        return deleteButton;
    }

    /**
     * @see org.eclipse.swt.widgets.Widget#toString()
     */
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + columnNumber;
        result = prime * result + ((selectButton == null) ? 0 : selectButton.hashCode());
        result = prime * result + ((ltrParent == null) ? 0 : ltrParent.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LinkTableColumn other = (LinkTableColumn) obj;
        if (columnNumber != other.columnNumber) {
            return false;
        }
        if (selectButton == null) {
            if (other.selectButton != null) {
                return false;
            }
        } else if (!selectButton.equals(other.selectButton)) {
            return false;
        }
        if (ltrParent == null) {
            if (other.ltrParent != null) {
                return false;
            }
        } else if (!ltrParent.equals(other.ltrParent)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    public Composite getColumnContainer() {
        return columnContainer;
    }

    public void refresh() {
        if (!columnContainer.isDisposed()) {
            columnContainer.pack(true);
            columnContainer.layout(true);
        }
        ltrParent.refresh(UpdateLinkTable.COLUMN_PATHS);
    }

    public LinkTableElementComboViewer getFirstCombo() {
        return firstCombo;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public IObjectModelService getContentService() {
        return contentService;
    }

    public LinkTableComposite getLtrParent() {
        return ltrParent;
    }

    public String getColumnPath() {
        String columnPath = firstCombo.getColumnPath();
        return createAlias(columnPath);
    }
    
    public String createAlias(String columnPath) {
        String[] columnPathElements = columnPath.split("\\.|\\<|\\>|\\/|\\:");
        int lastElement = columnPathElements.length - 1;
        String propertyId;
        String message;
        try {
            propertyId = columnPathElements[lastElement];
            String element = columnPathElements[lastElement - 1];
            logger.debug(columnPath);
            logger.debug("Element:" + element);
            logger.debug("Property:" + propertyId);
            if (columnPath.contains(LinkTableOperationType.RELATION.getOutput())) {
                message = HUIObjectModelService.getCnaLinkPropertyMessage(propertyId);
            } else {
                message = getContentService().getLabel(propertyId) + " ("
                        + getContentService().getLabel(element) + ")";
            }
        } catch (IndexOutOfBoundsException e) {
            logger.warn("String-split did not work, using old way", e);
            int propertyBeginning = columnPath
                    .lastIndexOf(LinkTableOperationType.PROPERTY.getOutput());
            propertyId = columnPath.substring(propertyBeginning + 1);
            if (columnPath.contains(":")) {
                message = HUIObjectModelService.getCnaLinkPropertyMessage(propertyId);
            } else {
                message = getContentService().getLabel(propertyId);
            }
        }
        message = StringUtils.replaceEachRepeatedly(message,
                new String[] { "/", ":", ".", "<", ">" }, new String[] { "", "", "", "", "" });
        message = message.replaceAll(" ", "__");

        return columnPath + " AS " + message;
    }

    public Button getSelectButton() {
        return selectButton;
    }
}
