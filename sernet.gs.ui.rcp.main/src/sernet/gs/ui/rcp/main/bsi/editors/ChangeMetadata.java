/*******************************************************************************
 * Copyright (c) 2019 Jochen Kemnade.
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
package sernet.gs.ui.rcp.main.bsi.editors;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import sernet.hui.common.connect.Entity;
import sernet.hui.swt.widgets.Colors;
import sernet.verinice.model.common.CnATreeElement;

public class ChangeMetadata extends Composite {

    private Text createdBy;
    private Text createdAt;
    private Text changedBy;
    private Text changedAt;

    public ChangeMetadata(Composite parent) {
        super(parent, SWT.BORDER);
        GridLayout gridLayout = new GridLayout(2, false);
        setLayout(gridLayout);

        createLabel(Messages.CreatedBy);
        createdBy = createText();

        createLabel(Messages.CreatedAt);
        createdAt = createText();

        createLabel(Messages.ChangedBy);
        changedBy = createText();

        createLabel(Messages.ChangedAt);
        changedAt = createText();

    }

    private Label createLabel(String text) {
        Label label = new Label(this, SWT.NONE);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label.setText(text);
        return label;
    }

    private Text createText() {
        Text text = new Text(this, SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        makeReadOnly(text);
        return text;
    }

    protected static void makeReadOnly(Text field) {
        field.setEditable(false);
        field.setBackground(Colors.GREY);
    }

    public void setElement(CnATreeElement cnAElement) {
        Entity entity = cnAElement.getEntity();
        createdBy.setText(Optional.ofNullable(entity.getCreatedBy()).orElse(StringUtils.EMPTY));
        updateField(createdAt, entity.getCreatedAt());

        changedBy.setText(Optional.ofNullable(entity.getChangedBy()).orElse(StringUtils.EMPTY));
        updateField(changedAt, entity.getChangedAt());

    }

    private void updateField(Text field, Date date) {
        if (date != null) {
            ZonedDateTime dt = date.toInstant().atZone(ZoneId.systemDefault());
            String formattedDate = DateTimeFormatter
                    .ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.MEDIUM).format(dt);
            field.setText(formattedDate);
        } else {
            field.setText(StringUtils.EMPTY);
        }
    }

}
