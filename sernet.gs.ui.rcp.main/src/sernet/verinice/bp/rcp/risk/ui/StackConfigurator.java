/*******************************************************************************
 * Copyright (c) 2018 Alexander Ben Nasrallah.
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
 ******************************************************************************/
package sernet.verinice.bp.rcp.risk.ui;

import java.util.List;
import java.util.Stack;

import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * A Composite displaying a list of editable rows of which only the last can be
 * deleted. New rows can only be appended and the end of the list.
 *
 * Rows which are present on construction are watched for deletion.
 */
public abstract class StackConfigurator<T> extends Composite {
    public static final int ELEMENT_SPACING = 10;
    public static final int ELEMENT_MARGINS = (int) (ELEMENT_SPACING * 2.5);
    public static final int LABLE_SIZE = 180;

    private Composite pane;
    private int maxValues;
    private int numberOfNewElements;
    private Stack<T> deletedRows;
    private List<T> rowData;

    public StackConfigurator(Composite parent, int maxValues) {
        super(parent, SWT.NONE);
        this.maxValues = maxValues;
        reset();
    }

    /**
     * Resets the stack of original entries. Should be used when the current
     * list is persistent, i.e. becomes the new original.
     */
    public void reset() {
        numberOfNewElements = 0;
        deletedRows = new Stack<>();
    }

    public List<T> getDeleted() {
        return deletedRows;
    }

    /**
     * Inherited classes should call this when their data set has changes.
     */
    protected void refresh(List<T> data) {
        rowData = data;
        if (pane != null) {
            pane.dispose();
        }
        draw();
        redraw();
    }

    private void draw() {
        setLayout(new GridLayout(1, true));
        pane = new Composite(this, SWT.NONE);
        pane.setLayout(new RowLayout(SWT.VERTICAL));

        for (int i = 0; i < rowData.size(); i++) {
            Composite rowComposite = new Composite(pane, SWT.NONE);
            rowComposite.setLayout(RowLayoutFactory.createFrom(new RowLayout(SWT.HORIZONTAL))
                    .spacing(ELEMENT_SPACING).create());

            Composite dataComposite = new Composite(rowComposite, SWT.NONE);
            dataComposite.setLayout(new FillLayout());

            Composite buttonComposite = new Composite(rowComposite, SWT.NONE);
            buttonComposite.setLayout(RowLayoutFactory.createFrom(new RowLayout(SWT.VERTICAL))
                    .spacing(ELEMENT_SPACING).create());

            addRow(dataComposite, rowData.get(i));
            if (i == rowData.size() - 1) {
                if (rowData.size() > 1) {
                    addRemoveButton(buttonComposite);
                }
                if (rowData.size() < maxValues) {
                    addAddButton(buttonComposite);
                }
            }
        }
        pane.requestLayout();
    }

    protected abstract void addRow(Composite parent, T rowData);

    protected abstract void onAddClicked();

    protected abstract void onRemoveClicked();

    private void addAddButton(Composite parent) {
        Button add = new Button(parent, SWT.NONE);
        add.setText("+");
        add.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                numberOfNewElements++;
                onAddClicked();
            }
        });
    }

    private void addRemoveButton(Composite parent) {
        Button remove = new Button(parent, SWT.NONE);
        remove.setText("-");
        remove.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (numberOfNewElements == 0) {
                    deletedRows.push(rowData.get(rowData.size() - 1));
                } else {
                    numberOfNewElements--;
                }
                onRemoveClicked();
            }
        });
    }
}
