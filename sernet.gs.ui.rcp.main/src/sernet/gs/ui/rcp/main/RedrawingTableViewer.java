/*******************************************************************************
 * Copyright (c) 2021 Jochen Kemnade.
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
package sernet.gs.ui.rcp.main;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/**
 * Workaround for table refreshing issues on macOS Big Sur (VN-2880) Redraws the
 * table whenever the input is changed
 */
public class RedrawingTableViewer extends TableViewer {

    private static final boolean RUNNING_ON_MACOS = SWT.getPlatform().equals("cocoa");

    public RedrawingTableViewer(Composite parent) {
        super(parent);
    }

    public RedrawingTableViewer(Composite parent, int style) {
        super(parent, style);
    }

    public RedrawingTableViewer(Table table) {
        super(table);
    }

    @Override
    protected void inputChanged(Object input, Object oldInput) {
        super.inputChanged(input, oldInput);
        if (RUNNING_ON_MACOS) {
            getTable().redraw();
        }
    }

}
