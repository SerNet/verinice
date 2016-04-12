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

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import sernet.verinice.rcp.linktable.composite.combo.VeriniceLinkTableElementComboViewer;
import sernet.verinice.service.linktable.IObjectModelService;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class VeriniceLinkTableColumn {

    private static final Logger LOG = Logger.getLogger(VeriniceLinkTableColumn.class);
    private static final int DEFAULT_GAP = 5;
    private int style;
    private VeriniceLinkTableComposite ltrParent;
    private Label columName;
    private int colNumber;
    private String name;
    private Button deleteButton;
    private Composite column;
    private VeriniceLinkTableElementComboViewer firstCombo;
    private IObjectModelService contentService;
    private Composite dragAndDropArea;
    private Button upButton;
    private Button downButton;

    public VeriniceLinkTableColumn(IObjectModelService contentService, VeriniceLinkTableColumn copy, int number) {
        this.ltrParent = copy.ltrParent;
        this.style = copy.style;
        this.colNumber = number;
        this.contentService = contentService;
        createColumn();
        firstCombo = (VeriniceLinkTableElementComboViewer) copy.getFirstCombo().copy(null, column, columName);
        refresh();
    }

    public VeriniceLinkTableColumn(IObjectModelService contentService, VeriniceLinkTableComposite parent, int style,
            int number) {
        this.ltrParent = parent;
        this.style = style;
        this.colNumber = number;
        this.contentService = contentService;
        createColumn();
        addFirstCombo();
    }

    /**
     * @param path
     * @param contentService2
     * @param ltrComposite
     * @param style2
     * @param i
     */
    public VeriniceLinkTableColumn(List<String> path, IObjectModelService contentService,
            VeriniceLinkTableComposite parent, int style, int number) {
        this.ltrParent = parent;
        this.style = style;
        this.colNumber = number;
        this.contentService = contentService;

        createColumn();

        addFirstCombo();
        firstCombo.setInput(new Object());
        firstCombo.setColumnPath(path);
        refresh();

    }

    private void addFirstCombo() {
        firstCombo = new VeriniceLinkTableElementComboViewer(this, column,
                style | SWT.READ_ONLY);
        FormData comboData = new FormData();
        comboData.left = new FormAttachment(columName, DEFAULT_GAP);
        comboData.top = new FormAttachment(columName, 0, SWT.CENTER);
        firstCombo.getCombo().setLayoutData(comboData);
        column.layout(true);

    }


    private void createColumn() {

        column = new Composite(ltrParent.getColumnsContainer(), style);
        FormLayout layoutColumn = new FormLayout();
        layoutColumn.marginHeight = 0;
        layoutColumn.marginWidth = 0;
        column.setLayout(layoutColumn);

        dragAndDropArea = addDragAndDropButtons();
        FormData dragAndDropAreaData = new FormData();
        dragAndDropAreaData.left = new FormAttachment(0);
        dragAndDropArea.setLayoutData(dragAndDropAreaData);

        deleteButton = new Button(column, style);
        deleteButton.setText("--");
        FormData deleteButtonData = new FormData();
        deleteButtonData.left = new FormAttachment(dragAndDropArea, DEFAULT_GAP);
        deleteButtonData.top = new FormAttachment(dragAndDropArea, 0, SWT.CENTER);
        deleteButton.setLayoutData(deleteButtonData);

        columName = new Label(column, style);
        setColumnName();
        FormData columNameData = new FormData();
        columNameData.left = new FormAttachment(deleteButton, DEFAULT_GAP);
        columNameData.top = new FormAttachment(dragAndDropArea, 0, SWT.CENTER);
        columNameData.width = 80;
        columName.setLayoutData(columNameData);
        
    }

    private Composite addDragAndDropButtons() {

        Composite ddComposite = new Composite(column, style);
        // FormLayout layoutColumn = new FormLayout();
        // layoutColumn.marginHeight = 0;
        // layoutColumn.marginWidth = 0;
        // column.setLayout(layoutColumn);

        upButton = new Button(ddComposite, style);
        upButton.setText("+");
        GridDataFactory.swtDefaults().hint(40, 20).applyTo(upButton);
        upButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (LOG.isDebugEnabled()) {

                    LOG.debug("column " + colNumber + " up");
                }
                moveUp();

            }
        });

        downButton = new Button(ddComposite, style);
        downButton.setText("-");
        GridDataFactory.swtDefaults().hint(40, 20).applyTo(downButton);
        downButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("column " + colNumber + " down");
                }
                moveDown();

            }
        });
        GridLayoutFactory.swtDefaults().generateLayout(ddComposite);
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
        column.pack(true);
        column.layout(true);

    }

    public void setColumnNumber(int num) {
        colNumber = num;
        setColumnName();
    }

    Button getDeleteButton() {
        return deleteButton;
    }

    /*
     * (non-Javadoc)
     * 
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
        result = prime * result + style;
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
        VeriniceLinkTableColumn other = (VeriniceLinkTableColumn) obj;
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
        if (style != other.style)
            return false;
        return true;
    }

    public Composite getColumn() {
        return column;
    }

    public void refresh() {
        column.pack(true);
        column.layout(true);
        ltrParent.refresh(true);
    }

    public VeriniceLinkTableElementComboViewer getFirstCombo() {
        return firstCombo;
    }

    public int getColumnNumber() {
        return colNumber;
    }

    public IObjectModelService getContentService() {
        return contentService;
    }

    public VeriniceLinkTableComposite getLtrParent() {
        return ltrParent;
    }

    public String getColumnPath() {
        return firstCombo.getColumnPath();
    }

}
