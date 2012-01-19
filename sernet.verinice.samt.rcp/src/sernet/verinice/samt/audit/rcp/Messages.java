/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
package sernet.verinice.samt.audit.rcp;

import org.eclipse.osgi.util.NLS;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "sernet.verinice.samt.audit.rcp.messages"; //$NON-NLS-1$
    public static String AddAction_1;
    public static String ElementView_1;
    public static String ElementView_2;
    public static String ElementView_3;
    public static String ElementView_4;
    public static String ElementView_5;
    public static String ElementView_8;
    public static String ElementViewContentProvider_1;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
