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
package sernet.verinice.service;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;

import sernet.gs.server.Activator;
import sernet.gs.service.AbstractReportTemplateService;
import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.model.report.ReportTemplateMetaData;

public class DummyReportDepositService extends AbstractReportTemplateService
        implements IReportDepositService {

    private static final Logger LOG = Logger.getLogger(DummyReportDepositService.class);

    @Override
    public void add(ReportTemplateMetaData metadata, byte[] file, String locale) {
    }

    @Override
    public void remove(ReportTemplateMetaData metadata, String locale) {
    }

    @Override
    public void update(ReportTemplateMetaData metadata, String locale) {
    }

    @Override
    protected boolean isHandeledByReportDeposit() {
        return true;
    }

    @Override
    protected String getTemplateDirectory() {
        try {
            URL url = FileLocator.find(Activator.getDefault().getBundle(),
                    new Path("/WebContent/WEB-INF/reportDeposit/"), null);
            URL fileUrl = FileLocator.toFileURL(url);
            return FileUtils.toFile(fileUrl).getAbsolutePath();
        } catch (IOException ex) {
            LOG.error("jar file with reports not found", ex);
        }

        return null;
    }
}
