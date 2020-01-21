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
package sernet.verinice.rcp.bp;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.DefaultModelLoadListener;
import sernet.verinice.model.bp.DefaultBpModelListener;
import sernet.verinice.model.bp.IBpModelListener;
import sernet.verinice.model.bp.ISecurityLevelProvider;
import sernet.verinice.model.bp.SecurityLevel;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Decorates {@link ISecurityLevelProvider} elements with an overlay indicating
 * the security level that they implement.
 */
public class SecurityLevelDecorator extends LabelProvider implements ILightweightLabelDecorator {
    /** Decorator ID (must match ID in plugin.xml) */
    private static final String ID = "sernet.verinice.rcp.bp.securityLevelDecorator";
    private static final String IMAGE_BASIC = "overlays/sec_level_b.png";
    private static final String IMAGE_STANDARD = "overlays/sec_level_s.png";
    private static final String IMAGE_HIGH = "overlays/sec_level_e.png";

    public SecurityLevelDecorator() {
        CnAElementFactory.getInstance().addLoadListener(new DefaultModelLoadListener() {
            @Override
            public void loaded(BpModel model) {
                super.loaded(model);
                model.addModITBOModelListener(getModelListener());
            }
        });
    }

    @Override
    public void decorate(Object element, IDecoration decoration) {
        if (element instanceof ISecurityLevelProvider) {
            ISecurityLevelProvider securable = (ISecurityLevelProvider) element;
            SecurityLevel level = securable.getSecurityLevel();
            if (level != null) {
                decoration.addOverlay(
                        ImageCache.getInstance().getImageDescriptor(getImageFilename(level)));
            }
        }
    }

    private String getImageFilename(SecurityLevel level) {
        switch (level) {
        case BASIC:
            return IMAGE_BASIC;
        case STANDARD:
            return IMAGE_STANDARD;
        case HIGH:
            return IMAGE_HIGH;
        default:
            throw new IllegalArgumentException("Security level not supported.");
        }
    }

    private IBpModelListener getModelListener() {
        return new DefaultBpModelListener() {

            @Override
            public void childAdded(CnATreeElement category, CnATreeElement child) {
                updateDecorations();
            }

            @Override
            public void childChanged(CnATreeElement child) {
                updateDecorations();
            }

            @Override
            public void childRemoved(CnATreeElement category, CnATreeElement child) {
                updateDecorations();
            }

            @Override
            public void databaseChildAdded(CnATreeElement child) {
                updateDecorations();
            }

            @Override
            public void databaseChildChanged(CnATreeElement child) {
                updateDecorations();
            }

            @Override
            public void databaseChildRemoved(ChangeLogEntry entry) {
                updateDecorations();
            }

            @Override
            public void databaseChildRemoved(CnATreeElement child) {
                updateDecorations();
            }

            @Override
            public void linkAdded(CnALink link) {
                updateDecorations();
            }

            @Override
            public void linkChanged(CnALink old, CnALink link, Object source) {
                updateDecorations();
            }

            @Override
            public void linkRemoved(CnALink link) {
                updateDecorations();
            }

            @Override
            public void modelRefresh(Object object) {
                updateDecorations();
            }

            @Override
            public void modelReload(BpModel newModel) {
                updateDecorations();
            }
        };
    }

    private void updateDecorations() {
        Display.getDefault()
                .asyncExec(() -> PlatformUI.getWorkbench().getDecoratorManager().update(ID));
    }
}
