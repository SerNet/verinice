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
package sernet.verinice.rcp;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

/**
 * Default no-op implementation meant for extension
 */
public class PartListenerAdapter implements IPartListener2 {

    @Override
    public void partActivated(IWorkbenchPartReference partRef) {
        // default implementation does nothing
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
        // default implementation does nothing
    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
        // default implementation does nothing
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partRef) {
        // default implementation does nothing
    }

    @Override
    public void partOpened(IWorkbenchPartReference partRef) {
        // default implementation does nothing
    }

    @Override
    public void partHidden(IWorkbenchPartReference partRef) {
        // default implementation does nothing
    }

    @Override
    public void partVisible(IWorkbenchPartReference partRef) {
        // default implementation does nothing
    }

    @Override
    public void partInputChanged(IWorkbenchPartReference partRef) {
        // default implementation does nothing
    }

}
