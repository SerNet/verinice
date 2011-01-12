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
package sernet.verinice.report.service.impl;

import org.eclipse.osgi.util.NLS;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "sernet.verinice.report.service.impl.messages"; //$NON-NLS-1$
    public static String AbhaengigkeitenReport_1;
    public static String AbhaengigkeitenReport_2;
    public static String AllItemsReport_0;
    public static String AllItemsReport_2;
    public static String AuditberichtReport_0;
    public static String AuditberichtReport_1;
    public static String BasisSichCheckReport_1;
    public static String BasisSichCheckReport_2;
    public static String ComprehensiveSamtReportType_0;
    public static String ComprehensiveSamtReportType_2;
    public static String ModellierungReport_1;
    public static String ModellierungReport_2;
    public static String RealisierungsplanReport_1;
    public static String RealisierungsplanReport_2;
    public static String RisikoanalyseReport_1;
    public static String RisikoanalyseReport_2;
    public static String RiskByAssetReport_0;
    public static String RiskByAssetReport_2;
    public static String RiskTreatmentReport_0;
    public static String RiskTreatmentReport_1;
    public static String SamtReportType_0;
    public static String SamtReportType_2;
    public static String StrukturanalyseReport_1;
    public static String StrukturanalyseReport_2;
    public static String UserReportType_1;
    public static String UserReportType_3;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
