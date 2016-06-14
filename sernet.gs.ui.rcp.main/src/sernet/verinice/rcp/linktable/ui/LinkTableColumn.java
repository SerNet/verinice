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

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.rcp.linktable.ui.combo.LinkTableComboViewer;
import sernet.verinice.rcp.linktable.ui.combo.LinkTableElementComboViewer;
import sernet.verinice.service.model.IObjectModelService;

/**
 * Container for all widgets needed for a vlt-table column
 * 
 * 
 * @see LinkTableComboViewer
 * @see LinkTableComposite
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class LinkTableColumn {

    private static final Logger LOG = Logger.getLogger(LinkTableColumn.class);
    public static final int DEFAULT_GAP = 6;
    private LinkTableComposite ltrParent;
    private Label columName;
    private int colNumber;
    private String name;
    private Button deleteButton;
    private Composite columnContainer;
    
    private Button upButton;
    private Button downButton;

    private IObjectModelService contentService;
    private LinkTableElementComboViewer firstCombo;

    private static final String ALIAS_DELIMITER = " AS ";

    public LinkTableColumn(LinkTableColumn copy, int number) {
        this.ltrParent = copy.ltrParent;
        this.colNumber = number;
        this.contentService = copy.getContentService();
        createColumn();
        firstCombo = (LinkTableElementComboViewer) copy.getFirstCombo().copy(null, columnContainer, columName);
    }

    public LinkTableColumn(LinkTableComposite parent, int style,
            int number) {

        this.ltrParent = parent;
        this.colNumber = number;
        this.contentService = parent.getContentService();
        createColumn();
        addFirstCombo();
    }

    public LinkTableColumn(List<String> path,
            LinkTableComposite parent, int number) {

        this.ltrParent = parent;
        this.colNumber = number;
        this.contentService = parent.getContentService();

        createColumn();

        addFirstCombo();
        firstCombo.setInput(new Object());
        firstCombo.setColumnPath(path);

    }

    public LinkTableColumn(ISelection selection, List<String> path,
            LinkTableComposite parent, int number) {

        this.ltrParent = parent;
        this.colNumber = number;
        this.contentService = parent.getContentService();

        createColumn();

        addFirstCombo();
        firstCombo.setInput(new Object());
        StructuredSelection element = (StructuredSelection) selection;
        firstCombo.setColumnPath(element.getFirstElement().toString(), path);
    }

    private void addFirstCombo() {
        firstCombo = new LinkTableElementComboViewer(this, columnContainer);
        FormData comboData = new FormData();
        comboData.left = new FormAttachment(columName, DEFAULT_GAP);
        comboData.top = new FormAttachment(columName, 0, SWT.CENTER);
        firstCombo.getCombo().setLayoutData(comboData);
        columnContainer.layout(true);

    }


    private void createColumn() {

        columnContainer = new Composite(ltrParent.getColumnsContainer(), SWT.NONE);
        FormLayout layoutColumn = new FormLayout();
        layoutColumn.marginHeight = 0;
        layoutColumn.marginWidth = 0;
        columnContainer.setLayout(layoutColumn);

        Composite dragAndDropArea = addDragAndDropButtons();
        FormData dragAndDropAreaData = new FormData();
        dragAndDropAreaData.left = new FormAttachment(0);
        dragAndDropArea.setLayoutData(dragAndDropAreaData);

        deleteButton = new Button(columnContainer, SWT.NONE);
        deleteButton.setText("-");
        FormData deleteButtonData = new FormData();
        deleteButtonData.left = new FormAttachment(dragAndDropArea, DEFAULT_GAP);
        deleteButtonData.top = new FormAttachment(dragAndDropArea, 0, SWT.CENTER);
        deleteButtonData.height = 45; // height of buttons * 2 + margin
        deleteButton.setLayoutData(deleteButtonData);

        columName = new Label(columnContainer, SWT.NONE);
        setColumnName();
        FormData columNameData = new FormData();
        columNameData.left = new FormAttachment(deleteButton, DEFAULT_GAP);
        columNameData.top = new FormAttachment(dragAndDropArea, 0, SWT.CENTER);
        columNameData.width = 80;
        columName.setLayoutData(columNameData);
        
    }

    private Composite addDragAndDropButtons() {

        Composite ddComposite = new Composite(columnContainer, SWT.NONE);

        upButton = new Button(ddComposite, SWT.ARROW | SWT.UP);
        GridDataFactory.swtDefaults().span(0, 0).hint(30, 20).applyTo(upButton);
        upButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (LOG.isDebugEnabled()) {

                    LOG.debug("columnContainer " + colNumber + " up");
                }
                moveUp();

            }
        });

        downButton = new Button(ddComposite, SWT.ARROW | SWT.DOWN);
        GridDataFactory.swtDefaults().span(0, 0).hint(30, 20).applyTo(downButton);
        downButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("columnContainer " + colNumber + " down");
                }
                moveDown();

            }
        });
        GridLayoutFactory.swtDefaults().margins(0, 0).generateLayout(ddComposite);
        return ddComposite;


    }

    protected void moveUp() {

        ltrParent.moveColumnUp(this);

    }

    protected void moveDown() {

        ltrParent.moveColumnDown(this);

    }

    protected void setColumnName() {
        name = Messages.VeriniceLinkTableColumn_0 + " " + colNumber + ":";
        columName.setText(name);
        columnContainer.pack(true);
        columnContainer.layout(true);

    }

    public void setColumnNumber(int num) {
        colNumber = num;
        setColumnName();
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
        result = prime * result + colNumber;
        result = prime * result + ((columName == null) ? 0 : columName.hashCode());
        result = prime * result + ((deleteButton == null) ? 0 : deleteButton.hashCode());
        result = prime * result + ((ltrParent == null) ? 0 : ltrParent.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LinkTableColumn other = (LinkTableColumn) obj;
        if (colNumber != other.colNumber)
            return false;
        if (columName == null) {
            if (other.columName != null)
                return false;
        } else if (!columName.equals(other.columName))
            return false;
        if (deleteButton == null) {
            if (other.deleteButton != null)
                return false;
        } else if (!deleteButton.equals(other.deleteButton))
            return false;
        if (ltrParent == null) {
            if (other.ltrParent != null)
                return false;
        } else if (!ltrParent.equals(other.ltrParent))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    public Composite getColumn() {
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
        return colNumber;
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

    private String createAlias(String columnPath) {
        int propertyBeginning = columnPath.lastIndexOf(".");
           String propertyId = columnPath.substring(propertyBeginning + 1);
           if (columnPath.contains(":")) {
               columnPath = columnPath + ALIAS_DELIMITER + getCnaLinkPropertyMessage(propertyId);
           } else {
               String message = HUITypeFactory.getInstance().getMessage(propertyId);
               columnPath = columnPath + ALIAS_DELIMITER + message;
           }
        return columnPath;
    }

    public String getCnaLinkPropertyMessage(String cnaLinkProperty) {
        switch (cnaLinkProperty) {
        case "title":
            return Messages.LinkTableColumn_CnaLink_Property_Title;
        case "description":
            return Messages.LinkTableColumn_CnaLink_Property_Description;
        case "risk-value-c":
            return Messages.LinkTableColumn_CnaLink_Property_C;
        case "risk-value-i":
            return Messages.LinkTableColumn_CnaLink_Property_I;
        case "risk-value-a":
            return Messages.LinkTableColumn_CnaLink_Property_A;
        default:
            return Messages.LinkTableColumn_CnaLink_Property_Unknown;
        }
    }

}
