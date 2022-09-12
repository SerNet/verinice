/*******************************************************************************
 * Copyright (c) 2022 Jochen Kemnade.
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
package sernet.gs.ui.rcp.main.bsi.views;

import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import sernet.verinice.model.bp.groups.ImportBpGroup;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.iso27k.ImportIsoGroup;

public class ImportGroupFirstComparator extends ViewerComparator {

    public ImportGroupFirstComparator() {
        this(null);
    }

    public ImportGroupFirstComparator(Comparator<? super String> comparator) {
        super(comparator);
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        if (e1 instanceof ImportBpGroup || e1 instanceof ImportIsoGroup
                || e1 instanceof ImportBsiGroup) {
            return -1;
        } else if (e2 instanceof ImportBpGroup || e2 instanceof ImportIsoGroup
                || e2 instanceof ImportBsiGroup) {
            return 1;
        }
        return super.compare(viewer, e1, e2);
    }
}
