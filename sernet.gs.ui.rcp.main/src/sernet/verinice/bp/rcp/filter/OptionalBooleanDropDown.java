/*******************************************************************************
 * Copyright (c) 2020 Jonas Jordan
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
package sernet.verinice.bp.rcp.filter;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import sernet.verinice.rcp.Messages;

/**
 * A dropdown combo UI element with three options: Default (empty), Yes (true) &
 * No (false).
 */
public class OptionalBooleanDropDown {
    private Combo combo;

    public OptionalBooleanDropDown(Composite parent) {
        this.combo = new Combo(parent, SWT.DROP_DOWN);
        this.combo.setItems("-", Messages.OptionalBooleanDropDown_Yes, Messages.OptionalBooleanDropDown_No);
    }

    public Optional<Boolean> getSelection() {
        switch (this.combo.getSelectionIndex()) {
        case 1:
            return Optional.of(true);
        case 2:
            return Optional.of(false);
        default:
            return Optional.empty();
        }
    }

    public void select(@NonNull Optional<Boolean> value) {
        combo.select(value.map(v -> v ? 1 : 2).orElse(0));
    }
}
