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
package sernet.verinice.validation;

import org.eclipse.osgi.util.NLS;

/**
 *
 */
@SuppressWarnings("restriction")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "sernet.verinice.validation.messages"; //$NON-NLS-1$
    
    public static String ValidationView_1;
    public static String ValidationView_2;
    public static String ValidationView_3;
    public static String ValidationView_4;
    public static String ValidationView_5;
    public static String ValidationView_6;
    public static String ValidationView_7;
    public static String ValidationView_8;
    
    public static String CreateValidationJob_1;

    
    
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
