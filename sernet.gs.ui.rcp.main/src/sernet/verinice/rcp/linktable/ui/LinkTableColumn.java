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

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import sernet.verinice.rcp.linktable.ui.combo.LinkTableComboViewer;
import sernet.verinice.rcp.linktable.ui.combo.LinkTableElementComboViewer;
import sernet.verinice.rcp.linktable.ui.combo.LinkTableOperationType;
import sernet.verinice.service.linktable.ColumnPathParser;
import sernet.verinice.service.model.HUIObjectModelService;
import sernet.verinice.service.model.IObjectModelService;

/**
 * Container for all widgets needed for a report query column.
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
    private String alias;
    private Button deleteButton;
    private Composite columnContainer;

    private Button selectButton;

    private IObjectModelService contentService;
    private LinkTableElementComboViewer firstCombo;

    private static final Pattern COLUMN_NAVIGATOR_TOKEN = Pattern.compile("[.<>/:]");

    public LinkTableColumn(LinkTableColumn copy, int number) {
        this.ltrParent = copy.ltrParent;
        this.columnNumber = number;
        this.contentService = copy.getContentService();
        createColumn();
        firstCombo = (LinkTableElementComboViewer) copy.getFirstCombo().copy(null, columnContainer,
                selectButton, this);
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
        Display.getDefault().asyncExec(() -> {
            createColumn();
            addFirstCombo();
            firstCombo.setInteractive(false);
            firstCombo.setColumnPath(ColumnPathParser.removeAlias(path));
            alias = ColumnPathParser.extractAlias(path);
            firstCombo.setInteractive(true);
        });
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

    public void clearAlias() {
        alias = null;
    }

    public String getColumnPath() {
        String columnPath = firstCombo.getColumnPath();
        if (alias == null) {
            alias = buildAlias(columnPath);
        }
        return columnPath + " AS " + alias;
    }

    private String buildAlias(String columnPath) {
        String[] columnPathElements = COLUMN_NAVIGATOR_TOKEN.split(columnPath);
        String message;

        int numberOfTokens = columnPathElements.length;
        if (numberOfTokens >= 2) {
            String propertyId = columnPathElements[numberOfTokens - 1];
            String element = columnPathElements[numberOfTokens - 2];
            if (logger.isDebugEnabled()) {
                logger.debug(columnPath);
                logger.debug("Element:" + element);
                logger.debug("Property:" + propertyId);
            }
            if (columnPath.contains(LinkTableOperationType.RELATION.getOutput())) {
                message = HUIObjectModelService.getCnaLinkPropertyMessage(propertyId);
            } else {
                message = getContentService().getLabel(propertyId) + " ("
                        + getContentService().getLabel(element) + ")";
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Cannot use String split approach for column path " + columnPath
                        + ", using old way");
            }
            int propertyBeginning = columnPath
                    .lastIndexOf(LinkTableOperationType.PROPERTY.getOutput());
            String propertyId = columnPath.substring(propertyBeginning + 1);
            if (columnPath.contains(":")) {
                message = HUIObjectModelService.getCnaLinkPropertyMessage(propertyId);
            } else {
                message = getContentService().getLabel(propertyId);
            }
        }
        message = StringUtils.replaceEachRepeatedly(message,
                new String[] { "/", ":", ".", "<", ">" }, new String[] { "", "", "", "", "" });
        message = message.replaceAll(" ", "__");
        return message;
    }

    public Button getSelectButton() {
        return selectButton;
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
}
