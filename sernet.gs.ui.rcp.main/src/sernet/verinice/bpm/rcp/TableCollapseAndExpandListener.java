/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bpm.rcp;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * This is a helper class for {@link TaskView}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
final class TableCollapseAndExpandListener implements Listener {
    @Override
    public void handleEvent(Event e) {
        final TreeItem treeItem = (TreeItem) e.item;
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                for (TreeColumn tc : treeItem.getParent().getColumns()) {
                    tc.pack();
                }
            }
        });
    }
}