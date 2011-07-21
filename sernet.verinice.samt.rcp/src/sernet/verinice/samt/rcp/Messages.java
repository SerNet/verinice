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
package sernet.verinice.samt.rcp;

import org.eclipse.osgi.util.NLS;

/**
 * @author Daniel Murygin <dm@sernet.de>
 *
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "sernet.verinice.samt.rcp.messages"; //$NON-NLS-1$
    public static String AddISAToOrganisation_0;
    public static String AddISAToOrganisation_1;
    public static String AddISAToOrganisation_3;
    public static String AddSelfAssessment_0;
    public static String AddSelfAssessment_1;
    public static String AddSelfAssessment_2;
    public static String AddSelfAssessmentMenuAction_1;
    public static String AddSelfAssessmentMenuAction_2;
    public static String AssignAllIsaTopics_0;
    public static String AssignAllIsaTopics_1;
    public static String AssignAllIsaTopics_2;
    public static String AssignAllIsaTopics_3;
    public static String SamtPreferencePage_0;
	public static String SamtPreferencePage_1;
    public static String SamtPreferencePage_2;
    public static String SamtPreferencePage_3;
    public static String SamtPreferencePage_4;
    public static String SamtPreferencePage_5;
    public static String SamtView_1;
    public static String SamtView_2;
    public static String SamtView_4;
    public static String SamtWorkspace_0;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
