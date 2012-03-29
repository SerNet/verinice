/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.oda.driver.preferences;

import java.io.File;
import java.util.logging.Level;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import sernet.verinice.oda.driver.Activator;


/**
 *  sets defaults for report preferences
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("user.home"));
        sb.append(File.separator);
        sb.append("verinice");
        sb.append(File.separator);
        sb.append("verinice-reports.log");
        store.setDefault(PreferenceConstants.REPORT_LOG_FILE, sb.toString());
        store.setDefault(PreferenceConstants.REPORT_LOGGING_ENABLED, false);
        store.setDefault(PreferenceConstants.REPORT_LOGGING_LVL, Level.SEVERE.toString());
    }

}
