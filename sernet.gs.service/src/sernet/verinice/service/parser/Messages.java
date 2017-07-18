/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.parser;

import org.eclipse.osgi.util.NLS;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 *
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "sernet.verinice.service.parser.messages"; //$NON-NLS-1$
    public static String BausteinUmsetzung_0;
    public static String BausteinUmsetzung_1;
    public static String BausteinUmsetzung_2;
    public static String BausteinUmsetzung_3;
    public static String BausteinUmsetzung_4;
    public static String BausteinUmsetzung_5;
    public static String BSIMassnahmenModel_0;
    public static String BSIMassnahmenModel_1;
    public static String BSIMassnahmenModel_10;
    public static String BSIMassnahmenModel_2;
    public static String BSIMassnahmenModel_3;
    public static String BSIMassnahmenModel_4;
    public static String BSIMassnahmenModel_5;
    public static String BSIMassnahmenModel_6;
    public static String BSIMassnahmenModel_7;
    public static String BSIMassnahmenModel_8;
    public static String BSIMassnahmenModel_9;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
