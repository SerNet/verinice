/*******************************************************************************
 * Copyright (c) 2019 Urs Zeidler.
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
 *     Usr Zeidler - initial API and implementation
 ******************************************************************************/
package sernet.verinice.report.service.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.eclipse.birt.report.model.api.IResourceLocator;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.core.runtime.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.interfaces.oda.IVeriniceOdaDriver;
import sernet.verinice.report.service.Activator;

/**
 * Locates a resource like a property file in the classpath of
 * BIRTReportService.class and the local or the server report deposit(repository).
 */
public final class ReportResourceLocator implements IResourceLocator {
    private static final Logger log = LoggerFactory.getLogger(ReportResourceLocator.class);

    @SuppressWarnings("rawtypes")
    @Override
    public URL findResource(ModuleHandle moduleHandle, String fileName, int type, Map appContext) {
        URL url = findByClassloader(fileName);
        if (url == null) {
            url = findInRepository(fileName);
        }
        if (url == null && log.isWarnEnabled()) {
            log.warn(String.format(
                    "Report resource '%s' could not neither be found through internal resource loader nor through the default one.",
                    fileName));
        }
        return url;
    }

    @Override
    public URL findResource(ModuleHandle moduleHandle, String fileName, int type) {
        URL url = findByClassloader(fileName);
        if (url == null) {
            url = findInRepository(fileName);
        }
        if (url == null && log.isWarnEnabled()) {
            log.warn(String.format(
                    "Report resource '%s' could not neither be found through internal resource loader nor through the default one.",
                    fileName));
        }
        return url;
    }

    /**
     * Searches a file in local report deposit and then in the server report
     * deposit(repository).
     */
    private URL findInRepository(String fileName) {
        try {
            URL url = getUrlFromLocalDirectory(fileName);
            if (url != null) {
                return url;
            }
            return getUrlFromRemoteSyncDirectory(fileName);
        } catch (MalformedURLException | URISyntaxException e) {
            log.error("Error building url for report properties.", e);
        }
        return null;
    }

    /**
     * Returns the url of the file when it exist in in the local report deposit(repository).
     */
    private URL getUrlFromLocalDirectory(String fileName)
            throws URISyntaxException, MalformedURLException {
        IVeriniceOdaDriver odaDriver = Activator.getDefault().getOdaDriver();
        URI locationConst = URIUtil.fromString(odaDriver.getLocalReportLocation());
        File propertyFile = new File(
                URIUtil.toUnencodedString(locationConst) + File.separatorChar + fileName);
        if (propertyFile.exists()) {
            return propertyFile.toURI().toURL();
        }
        return null;
    }

    /**
     * Returns the url of the file when it exist in in the remote report
     * deposit(repository).
     */
    private URL getUrlFromRemoteSyncDirectory(String fileName) throws MalformedURLException {
        String depositPath = GenericReportType
                .getDepositPath(IReportDepositService.REPORT_DEPOSIT_CLIENT_REMOTE);
        String fullFilePath = depositPath + fileName;
        URL url = new URL(fullFilePath);
        String path = url.getPath();
        File file = new File(path);
        if (file.exists()) {
            return url;
        }
        return null;
    }

    /**
     * Finds resources in package of class BIRTReportService.
     * 
     * <p>
     * Important: If report resource are moved into a different package this
     * method *must* be adjusted.
     * </p>
     * 
     * @param resource
     * @return
     */
    private URL findByClassloader(String resource) {
        return BIRTReportService.class.getResource(resource);
    }
}