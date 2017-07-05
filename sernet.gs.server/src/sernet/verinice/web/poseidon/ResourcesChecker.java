/*******************************************************************************
 * Copyright (c) 2017 Benjamin Weißenfels.
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
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.web.poseidon;

import static sernet.gs.web.Util.getMessage;

import java.io.File;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import static org.apache.commons.io.FilenameUtils.concat;

/**
 * Checks if resources are available.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "resourcesChecker")
public class ResourcesChecker {

    private static final String MANUAL_DIR = "manual";
    private static final String BASE_CLIENT_DIR = "clients";
    private static final String MESSAGES = "sernet.verinice.web.WebMessages";


    /**
     * Checks whether there is at least one client available.
     */
    public boolean clientsAvailable() {
        return existsWindowsClient64Bit() || existsWindowsClient32Bit() || existsLinuxClient64Bit() || existsLinuxClient32Bit();
    }

    /**
     * Checks whether there is at leas one manual available.
     */
    public boolean manualsAvailable() {
        return existsUserManualPdf() || existsUserManualHtml();
    }


    public boolean existsWindowsClient64Bit() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String fileName = concat(BASE_CLIENT_DIR, "verinice-windows-x86_64.zip");
        File file = new File(externalContext.getRealPath(fileName));
        return file.exists();
    }

    public boolean existsWindowsClient32Bit() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String fileName = concat(BASE_CLIENT_DIR, "verinice-windows-x86.zip");
        File file = new File(externalContext.getRealPath(fileName));
        return file.exists();
    }

    public boolean existsLinuxClient64Bit() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String fileName = concat(BASE_CLIENT_DIR, "verinice-linux-x86_64.zip");
        File file = new File(externalContext.getRealPath(fileName));
        return file.exists();
    }

    public boolean existsLinuxClient32Bit() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String fileName = concat(BASE_CLIENT_DIR, "verinice-linux-x86.zip");
        File file = new File(externalContext.getRealPath(fileName));
        return file.exists();
    }

    public boolean existsUserManualPdf() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        File file = new File(externalContext.getRealPath(concat(MANUAL_DIR, getManualName("manual_pdf_name"))));
        return file.exists();
    }

    public boolean existsUserManualHtml() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        File file = new File(externalContext.getRealPath(concat(MANUAL_DIR, getManualName("manual_html_name"))));
        return file.exists();
    }

    private String getManualName(String key) {
        return getMessage(MESSAGES, key);
    }
}
