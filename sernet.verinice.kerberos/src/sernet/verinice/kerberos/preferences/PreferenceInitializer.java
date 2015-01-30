/*******************************************************************************
 * Copyright (c) 2015 verinice.
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
 *     verinice <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.kerberos.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import sernet.verinice.kerberos.Activator;

/**
 * @author Benjamin Weiﬂenfels <bw[at]sernet[dot]de>
 *
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    
    private static final String VERINICEPRO_AD_DEFAULT_SERVICE_NAME = "verinicepro";
    
    private static final boolean KERBEROS_DEFAULT_STATUS = false;

    public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.VERINICEPRO_SERVICE_NAME, VERINICEPRO_AD_DEFAULT_SERVICE_NAME);
		store.setDefault(PreferenceConstants.KERBEROS_STATUS, KERBEROS_DEFAULT_STATUS);
	}

}
