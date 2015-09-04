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

import org.eclipse.osgi.util.NLS;


/**
 * @author Benjamin Weiﬂenfels <bw[at]sernet[dot]de>
 *
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "sernet.verinice.kerberos.preferences.messages";
    public static String KerberosPreferencePage_0;
    public static String KerberosPreferencePage_1;
    public static String KerberosPreferencePage_2;
    public static String KerberosPreferencePage_3;
    public static String KerberosPreferencePage_4;
    public static String KerberosPreferencePage_5;
    public static String KerberosPreferencePage_6;
    public static String KerberosPreferencePage_7;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
