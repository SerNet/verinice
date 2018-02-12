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

    public static String Root_Requirement_Group_Name;
    public static String Process_Requirement_Group_Name;
    public static String System_Requirement_Group_Name;
    public static String Root_Threat_Group_Name;
    public static String Elemental_Threat_Group_Name;
    public static String Specific_Threat_Group_Name;
    public static String Specific_Process_Threat_Group_Name;
    public static String Specific_System_Threat_Group_Name;
    public static String IT_Network_Name;
    public static String Qualifier_Standard;
    public static String Qualifier_Basic;
    public static String Qualifier_High;
    public static String Main_Responsible;
    public static String Further_Responsibles;
    public static String Basic_Requirements;
    public static String Basic_Requirements_Intro;
    public static String Standard_Requirements;
    public static String Standard_Requirements_Intro;
    public static String High_Requirements;
    public static String High_Requirements_Intro;
    public static String CIA_Affects;
    public static String CIA_Affects_Yes;
    public static String CIA_Affects_No;
    public static String CIA_Affects_Confidentiality;
    public static String CIA_Affects_Integrity;
    public static String CIA_Affects_Availability;
    public static String Responsibles;
    public static String Requirements;
    public static String Further_Information;
    public static String Literature;
    public static String Introduction;
    public static String Purpose;
    public static String Differentiation;
    public static String Threat_Situation;
    public static String Description;
    public static String Root_Safeguard_Group_Name;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

}
