/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
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
 * Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp;

import org.eclipse.jface.preference.IPreferenceStore;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

/**
 * This class provides methods for convenient read-only access to the
 * preferences. The class does not yet provide access to all the preferences. It
 * can and should be extended with missing methods.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public final class Preferences {

    private Preferences() {
        super();
    }

    public static boolean isStandalone() {
        return PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER
                .equals(getPreferenceStore().getString(PreferenceConstants.OPERATION_MODE));
    }

    public static boolean isServerMode() {
        return PreferenceConstants.OPERATION_MODE_REMOTE_SERVER
                .equals(getPreferenceStore().getString(PreferenceConstants.OPERATION_MODE));
    }

    public static String getServerUrl() {
        return getPreferenceStore().getString(PreferenceConstants.VNSERVER_URI);
    }

    private static IPreferenceStore getPreferenceStore() {
        return Activator.getDefault().getPreferenceStore();
    }

}
