/*******************************************************************************
 * Copyright (c) 2014 Sebastian Hagedorn <sh@sernet.de>.
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
package sernet.verinice.interfaces;

import sernet.verinice.model.report.ReportTemplateMetaData;

/**
 * Provides some methods to upload and editing customer report templates. For
 * retrieving reports look at {@link IReportTemplateService}.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public interface IReportDepositService extends IReportTemplateService {

    void add(ReportTemplateMetaData metadata, byte[] file, String locale) throws ReportDepositException;

    void remove(ReportTemplateMetaData metadata, String locale) throws ReportDepositException;

    void update(ReportTemplateMetaData metadata, String locale) throws ReportDepositException;

    String getDepositLocation() throws ReportDepositException;

}
