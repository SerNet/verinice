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
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

/**
 * Represents the local report directory, which can be filled by the user and is
 * not synchronized with report deposit.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class LocalReportTemplateService extends AbstractReportTemplateService {

    @Override
    public boolean isHandeledByReportDeposit() {
        return false;
    }

    @Override
    public String getTemplateDirectory() {
        return getLocalReportPath();
    }

    private String getLocalReportPath() {

        String templateDir = Activator.getDefault().getIReportTemplateDirectoryService().getDirectory();

        if (templateDir == null) {
            templateDir = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.REPORT_LOCAL_TEMPLATE_DIRECTORY);
        } else {
            Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.REPORT_LOCAL_TEMPLATE_DIRECTORY, templateDir);
        }
        return templateDir;
    }

}
