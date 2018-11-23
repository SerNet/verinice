/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sernet.verinice.model.bp.risk.Frequency;

public final class FrequencyConfigurator extends StackConfigurator<Frequency> {

    private static final int MAX_NUMBER_OF_FREQUENCIES = 10;

    public FrequencyConfigurator(Composite parent, List<Frequency> editorState,
            Runnable fireProperyChange) {
        super(parent, MAX_NUMBER_OF_FREQUENCIES, editorState, fireProperyChange);
    }

    @Override
    protected @NonNull Frequency generateNewData(int index) {
        return new Frequency(Frequency.getPropertyKeyForIndex(index), "", "");
    }

    @Override
    protected void addRow(Composite parent, Frequency frequency) {
        parent.setLayout(
                RowLayoutFactory.createFrom(new RowLayout(SWT.HORIZONTAL)).spacing(10).create());

        Composite currentItemLeft = new Composite(parent, SWT.NONE);

        currentItemLeft.setLayout(
                RowLayoutFactory.createFrom(new RowLayout(SWT.VERTICAL)).spacing(5).create());
        Text labelField = new Text(currentItemLeft, SWT.BORDER);
        labelField.setLayoutData(new RowData(LABEL_WIDTH, SWT.DEFAULT));
        labelField.setText(frequency.getLabel());

        final ControlDecoration txtDecorator = createLabelFieldDecoration(labelField,
                Messages.errorUniqueFrequencyLabels);
        updateDecoratorVisibility(txtDecorator, frequency.getId(), frequency.getLabel());
        labelField.addModifyListener(e -> {
            if (e.getSource() instanceof Text) {
                String newLabel = ((Text) e.getSource()).getText();
                updateDecoratorVisibility(txtDecorator, frequency.getId(), newLabel);
                updateValue(frequency.getId(),
                        valueFromEditorState -> valueFromEditorState.withLabel(newLabel));
            }
        });

        Text descriptionField = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
        descriptionField.setLayoutData(new RowData(450, 80));
        descriptionField.setText(frequency.getDescription());
        descriptionField.addModifyListener(e -> {
            if (e.getSource() instanceof Text) {
                String newDescription = ((Text) e.getSource()).getText();
                updateValue(frequency.getId(), valueFromEditorState -> valueFromEditorState
                        .withDescription(newDescription));
            }
        });
    }

}
