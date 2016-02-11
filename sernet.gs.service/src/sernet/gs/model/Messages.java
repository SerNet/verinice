/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.model;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public final class Messages extends NLS {
    private static final String BUNDLE_NAME = "sernet.gs.model.messages"; //$NON-NLS-1$
    public static String Gefaehrdung_0;
    public static String Gefaehrdung_1;
    public static String Gefaehrdung_2;
    public static String Gefaehrdung_3;
    public static String Gefaehrdung_4;
    public static String Gefaehrdung_5;
    public static String Gefaehrdung_6;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
