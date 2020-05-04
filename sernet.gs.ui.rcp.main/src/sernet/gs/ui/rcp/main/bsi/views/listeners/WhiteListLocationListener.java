/*******************************************************************************
 * Copyright (c) 2019 Finn Westendorf <fw[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 * Finn Westendorf <fw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views.listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;

public final class WhiteListLocationListener implements LocationListener {
    List<String> whiteList;
    public static final WhiteListLocationListener DEFAULT = new WhiteListLocationListener(
            Collections.singletonList("about:blank"));

    public WhiteListLocationListener(Collection<String> whiteList) {
        this.whiteList = new ArrayList<>(whiteList);
    }

    @Override
    public void changing(LocationEvent event) {
        event.doit = whiteList.contains(event.location);
    }

    @Override
    public void changed(LocationEvent event) {
    }
}
