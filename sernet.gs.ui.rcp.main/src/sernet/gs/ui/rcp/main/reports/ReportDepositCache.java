/*******************************************************************************
 * Copyright (c) 2014 benjamin.
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
 *     benjamin <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.reports;

import sernet.gs.service.AbstractReportTemplateService;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.rcp.ReportTemplateSync;

/**
 * Caches report templates which are synchronized by the
 * {@link IReportDepositService}.
 * 
 * @see ReportTemplateSync
 * @see LocalReportTemplateService
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class ReportDepositCache extends AbstractReportTemplateService {

    @Override
    public boolean isHandeledByReportDeposit() {
        return true;
    }

    @Override
    public String getTemplateDirectory() {
        return CnAWorkspace.getInstance().getRemoteReportTemplateDir();
    }

}
