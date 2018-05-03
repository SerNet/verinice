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
 *     Alexander Ben Nasrallah <an@sernet.de>
 ******************************************************************************/
package sernet.verinice.web.poseidon;

import static org.apache.commons.io.FilenameUtils.concat;
import static sernet.gs.web.Util.getMessage;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

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

    private ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
    private File clientDir = new File(externalContext.getRealPath(BASE_CLIENT_DIR));

    /**
     * Checks whether there is at least one client available.
     */
    public boolean clientsAvailable() {
        return clientDir.listFiles().length != 0;
    }

    /**
     * @return A list of files in the client directory sorted by name ascending.
     */
    public File[] getClientFiles() {
        File[] files = clientDir.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        return files;
    }

    public String getClientPath(String clientName) {
        return new File(BASE_CLIENT_DIR, clientName).getPath();
    }

    /**
     * Checks whether there is at least one manual available.
     */
    public boolean manualsAvailable() {
        return existsUserManualPdf() || existsUserManualHtml();
    }

    public boolean existsUserManualPdf() {
        File file = new File(externalContext.getRealPath(concat(MANUAL_DIR, getManualName("manual_pdf_name"))));
        return file.exists();
    }

    public boolean existsUserManualHtml() {
        File file = new File(externalContext.getRealPath(concat(MANUAL_DIR, getManualName("manual_html_name"))));
        return file.exists();
    }

    private String getManualName(String key) {
        return getMessage(MESSAGES, key);
    }
}
