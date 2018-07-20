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
package sernet.verinice.bp.rcp.filter;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.ImageCache;

/**
 * This action shows the base protection filter dialog
 */
public class BaseProtectionFilterAction extends Action {
    private StructuredViewer viewer;

    private @NonNull BaseProtectionFilterParameters filterParameters;
    private final @NonNull BaseProtectionFilterParameters defaultFilterParams;

    public BaseProtectionFilterAction(StructuredViewer viewer,
            @NonNull BaseProtectionFilterParameters defaultFilterParams) {
        super("Filter..."); // //$NON-NLS-1$
        this.viewer = viewer;
        this.defaultFilterParams = defaultFilterParams;
        this.filterParameters = defaultFilterParams;
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.FILTER));
    }

    @Override
    public void run() {
        BaseProtectionFilterDialog dialog = new BaseProtectionFilterDialog(
                Display.getCurrent().getActiveShell(), filterParameters, defaultFilterParams);
        if (dialog.open() != InputDialog.OK) {
            return;
        }
        filterParameters = dialog.getFilterParameters();

        Collection<ViewerFilter> viewerFilters = BaseProtectionFilterBuilder
                .makeFilters(filterParameters);

        ViewerFilter[] filtersAsArray = viewerFilters
                .toArray(new ViewerFilter[viewerFilters.size()]);
        viewer.setFilters(filtersAsArray);

        if (defaultFilterParams.equals(filterParameters)) {
            setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.FILTER));
        } else {
            setImageDescriptor(
                    ImageCache.getInstance().getImageDescriptor(ImageCache.FILTER_ACTIVE));
        }
    }
}
