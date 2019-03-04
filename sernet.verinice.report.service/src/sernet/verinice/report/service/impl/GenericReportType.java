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
package sernet.verinice.report.service.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;

import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.core.runtime.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.interfaces.IVeriniceConstants;
import sernet.verinice.interfaces.oda.IVeriniceOdaDriver;
import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportOptions;
import sernet.verinice.interfaces.report.IReportType;
import sernet.verinice.interfaces.report.ReportTypeException;
import sernet.verinice.model.report.ReportTemplateMetaData;
import sernet.verinice.report.service.Activator;
import sernet.verinice.report.service.impl.security.ReportSecurityManager;
import sernet.verinice.security.report.ReportSecurityContext;

/**
 *
 */
public class GenericReportType implements IReportType {

    private final static Logger LOG = LoggerFactory.getLogger(GenericReportType.class);
    
    private IReportOptions options;
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#getId()
     */
    @Override
    public String getId() {
        return "";
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#getLabel()
     */
    @Override
    public String getLabel() {
        return "";
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#getDescription()
     */
    @Override
    public String getDescription() {
        return "";
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#getOutputFormats()
     */
    @Override
    public IOutputFormat[] getOutputFormats() {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#createReport(sernet.verinice.interfaces.report.IReportOptions)
     * has nothing to do with creating the report, this is just a setter for the options-attribute
     */
    @Override
    public void createReport(IReportOptions reportOptions) {
        this.options = reportOptions;
    }
    
    

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#getReportFile()
     */
    @Override
    public String getReportFile() {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#setReportFile(java.lang.String)
     */
    @Override
    public void setReportFile(String file) {
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#getUseCaseID()
     */
    @Override
    public String getUseCaseID() {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#createReport(sernet.verinice.model.report.ReportTemplateMetaData)
     */
    @Override
    public void createReport(ReportTemplateMetaData metadata) {
        BIRTReportService brs = new BIRTReportService();
        URL rptURL = null;
        try {
            if(metadata.isServer()){
                rptURL = new URL(getDepositPath(IReportDepositService.REPORT_DEPOSIT_CLIENT_REMOTE) + metadata.getFilename());
            } else {
    		    IVeriniceOdaDriver odaDriver = Activator.getDefault().getOdaDriver();
                URI locationConst = URIUtil.fromString(odaDriver.getLocalReportLocation());

                if(LOG.isDebugEnabled()){
                    LOG.debug("determined value for \"locationConst\":\t" + locationConst);
                    LOG.debug("File to open:\t" + metadata.getFilename());
                }

                URI u = URIUtil.append(locationConst, metadata.getFilename());
                if(LOG.isDebugEnabled()){
                    LOG.debug("URI generated:\t" + u.toString());
                    LOG.debug("Unencoded URI:\t" + URIUtil.toUnencodedString(u));
                    LOG.debug("Default Charset:\t" + Charset.defaultCharset().displayName(Locale.getDefault()));
                    LOG.debug("Default Locale:\t" + Locale.getDefault().getLanguage());
                }

                File f1 = new File(URIUtil.toUnencodedString(u));
                rptURL = f1.toURI().toURL();
            }
        } catch (MalformedURLException e) {
            LOG.error("Error reading" + " rptdesign", e);
            throw new ReportTypeException(e);
        } catch (URISyntaxException e) {
            LOG.error("Unable to parse URL of template location", e);
            throw new ReportTypeException(e);
        }

        if(LOG.isDebugEnabled()){
            LOG.debug("Trying to open report from template at:\t" + rptURL.toString());
        }

        ReportSecurityContext reportSecurityContext = new ReportSecurityContext(options, rptURL, brs.getLogfile(), metadata);
        ReportSecurityManager secureReportExecutionManager = new ReportSecurityManager(reportSecurityContext);
        IRunAndRenderTask task = brs.createTask(reportSecurityContext.getRptDesignUrl());
        task = brs.prepareTaskForRendering(task, options);
        brs.performRenderTask(task, secureReportExecutionManager);
    }
    
    public static String getDepositPath(String locationConstant){
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty(IVeriniceConstants.OSGI_INSTANCE_AREA));
        if(!(sb.toString().endsWith(String.valueOf(File.separatorChar)))){
            sb.append(File.separatorChar);
        }
        sb.append(locationConstant);
        if(!(sb.toString().endsWith(String.valueOf(File.separatorChar)))){
            sb.append(File.separatorChar);
        }        
        return sb.toString();
    }
    

}
