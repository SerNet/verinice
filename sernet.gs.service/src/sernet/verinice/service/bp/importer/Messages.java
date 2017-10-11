/*******************************************************************************
 * Copyright (c) 2016 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn <sh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.bp.importer;

import org.eclipse.osgi.util.NLS;

/**
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
public class Messages extends NLS {
    
    private static final String BUNDLE_NAME = "sernet.verinice.service.bp.importer.messages"; //$NON-NLS-1$
    
    public static String ROOT_REQUIREMENT_GROUP_NAME;
    public static String PROCESS_REQUIREMENT_GROUP_NAME;
    public static String SYSTEM_REQUIREMENT_GROUP_NAME;
    public static String ROOT_THREAT_GROUP_NAME;
    public static String ELEMENTAL_THREAT_GROUP_NAME;
    public static String SPECIFIC_THREAT_GROUP_NAME;
    public static String SPECIFIC_PROCESS_THREAT_GROUP_NAME;
    public static String SPECIFIC_SYSTEM_THREAT_GROUP_NAME;
    public static String IT_NETWORK_NAME;
    public static String QUALIFIER_STANDARD;
    public static String QUALIFIER_BASIC;
    public static String QUALIFIER_HIGH;
    public static String MAIN_RESPONSIBLE;
    public static String FURTHER_RESPONSIBLES;
    public static String BASIC_REQUIREMENTS;
    public static String STANDARD_REQUIREMENTS;
    public static String HIGH_REQUIREMENTS;
    public static String CIA_AFFECTS;
    public static String CIA_AFFECTS_YES;
    public static String CIA_AFFECTS_NO;
    public static String CIA_AFFECTS_CONFIDENTIALITY;
    public static String CIA_AFFECTS_INTEGRITY;
    public static String CIA_AFFECTS_AVAILABILITY;
    public static String RESPONSIBLES;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


}
