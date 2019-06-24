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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sernet.verinice.model.bp.risk.RiskPropertyValue;

/**
 * A Composite displaying a list of editable rows of which only the last can be
 * deleted. New rows can only be appended and the end of the list.
 *
 * Rows which are present on construction are watched for deletion.
 */
public abstract class StackConfigurator<T extends RiskPropertyValue> extends Composite {
    public static final int ELEMENT_SPACING = 10;
    public static final int ELEMENT_MARGINS = (int) (ELEMENT_SPACING * 2.5);
    public static final int LABEL_WIDTH = 180;

    private Composite pane;
    private int maxValues;
    private int numberOfNewElements;
    private Stack<T> deletedRows;
    protected LinkedHashMap<String, T> editorState;
    private final Runnable fireProperyChange;

    public StackConfigurator(Composite parent, int maxValues, List<T> values,
            Runnable fireProperyChange) {
        super(parent, SWT.NONE);
        this.maxValues = maxValues;
        setEditorState(values);
        this.fireProperyChange = fireProperyChange;
        reset();
        refresh();
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

    public List<T> getEditorState() {
        return new ArrayList<>(editorState.values());
    }

    public void setEditorState(List<T> values) {
        if (this.editorState != null) {
            Set<String> newKeys = values.stream().map(T::getId).collect(Collectors.toSet());

            List<T> cutEntries = editorState.entrySet().stream()
                    .filter(x -> !newKeys.contains(x.getKey())).map(Entry::getValue)
                    .collect(Collectors.toList());
            deletedRows.addAll(cutEntries);
        }
        this.editorState = new LinkedHashMap<>(values.size());
        values.stream().forEach(value -> editorState.put(value.getId(), value));
    }

    public void refresh() {
        if (pane != null) {
            pane.dispose();
        }
        setLayout(new GridLayout(1, true));
        pane = new Composite(this, SWT.NONE);
        pane.setLayout(new RowLayout(SWT.VERTICAL));

        List<T> rowData = getEditorState();
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
            if (i == editorState.size() - 1) {
                if (editorState.size() > 1) {
                    addRemoveButton(buttonComposite);
                }
                if (editorState.size() < maxValues) {
                    addAddButton(buttonComposite);
                }
            }
        }
        Composite extraComposite = new Composite(pane, SWT.NONE);
        extraComposite.setLayout(RowLayoutFactory.createFrom(new RowLayout(SWT.HORIZONTAL))
                .spacing(ELEMENT_SPACING).create());
        getDefault().ifPresent(value -> addRestoreButton(extraComposite, value));

        // The introduction of default data, allows rowData to be empty.
        // In this case a fallback add button is needed.
        if (rowData.isEmpty()) {
            addAddButton(extraComposite);
        }
        pane.requestLayout();
        redraw();
        pack(true);
        if (getParent() instanceof ScrolledComposite) {
            ((ScrolledComposite) getParent()).setMinSize(getClientArea().width,
                    getClientArea().height);
        }
    }

    protected abstract void addRow(Composite parent, T rowData);

    protected abstract @NonNull T generateNewData(int index);

    protected void updateValue(String id, Function<T, T> updater) {
        editorState.computeIfPresent(id, (key, value) -> updater.apply(value));
        fireProperyChange.run();
    }

    private void addAddButton(Composite parent) {
        Button add = new Button(parent, SWT.NONE);
        add.setText("+");
        add.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                numberOfNewElements++;
                T newValue = generateNewData(editorState.size() + 1);
                editorState.put(newValue.getId(), newValue);
                refresh();
                fireProperyChange.run();
            }
        });
    }

    private void addRemoveButton(Composite parent) {
        Button remove = new Button(parent, SWT.NONE);
        remove.setText("-");
        remove.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String lastKey = (String) editorState.keySet().toArray()[editorState.values().size()
                        - 1];
                T lastElement = editorState.remove(lastKey);
                if (numberOfNewElements == 0) {
                    deletedRows.push(lastElement);
                } else {
                    numberOfNewElements--;
                }
                refresh();
                fireProperyChange.run();
            }
        });
    }

    private void addRestoreButton(Composite parent, List<T> defaultValues) {
        Button restore = new Button(parent, SWT.NONE);
        restore.setText(Messages.riskConfigurationReset);
        restore.setToolTipText(Messages.riskConfigurationResetTooltip);
        restore.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setEditorState(defaultValues);
                numberOfNewElements = 0;
                refresh();
                fireProperyChange.run();
            }
        });
    }

    protected static ControlDecoration createLabelFieldDecoration(Text labelField,
            String descriptionText) {
        ControlDecoration txtDecorator = new ControlDecoration(labelField, SWT.TOP | SWT.RIGHT);
        FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
                .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
        Image img = fieldDecoration.getImage();
        txtDecorator.setImage(img);
        txtDecorator.setDescriptionText(descriptionText);
        return txtDecorator;
    }

    protected void updateDecoratorVisibility(ControlDecoration txtDecorator, String id,
            String newLabel) {
        if (!isUniqueAndNonEmpty(id, newLabel, editorState.values())) {
            txtDecorator.show();
        } else {
            txtDecorator.hide();
        }

    }

    /**
     * Can be overwritten to add a reset button, which sets the
     * {@code editorState} to the default value.
     */
    protected Optional<List<T>> getDefault() {
        return Optional.empty();
    }

    private static boolean isUniqueAndNonEmpty(String id, String label,
            Collection<? extends RiskPropertyValue> existingPropertyValues) {
        if (label.isEmpty()) {
            return false;
        }
        for (RiskPropertyValue existingPropertyValue : existingPropertyValues) {
            if (existingPropertyValue.getId().equals(id)) {
                continue;
            }
            String existingLabel = existingPropertyValue.getLabel();
            if (label.equals(existingLabel)) {
                return false;
            }
        }
        return true;
    }

}
