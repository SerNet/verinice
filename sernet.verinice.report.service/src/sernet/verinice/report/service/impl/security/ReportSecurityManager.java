/*******************************************************************************
 * Copyright (c) 2016 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn <sh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.report.service.impl.security;

import java.security.Permission;

import sernet.verinice.interfaces.report.IReportPermissionHandler;
import sernet.verinice.security.report.ReportSecurityContext;
import sernet.verinice.security.report.ReportSecurityException;

/**
 * Manager ensures that no unauthorized code gets called from within a report template. 
 * neither using beanshell or javascript
 * 
 * notice: 
 * - violations caused by misuse of javascript (rhino-engine) will get logged
 *  in the report-logfile (rhino-engine runs within the context of the birt-engine)
 *  
 * - violations nested in beanshell are logged in the client-logile (beanshell-interpreter is running in 
 * sernet.verinice.oda.driver.impl.Query (in the verinice-Client-Context)
 * 
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
public class ReportSecurityManager extends SecurityManager {

    private boolean protectionEnabled = true;

    private static final String VERINICE_RUN_QUERY_METHOD = "org.eclipse.birt.report.engine.api.impl.RunAndRenderTask.run";

    private ReportSecurityContext reportSecurityContext;
    
    ReportSecurityPermissionManager permissionManager; 

    public ReportSecurityManager(ReportSecurityContext reportSecurityContext) { 
        super();
        this.reportSecurityContext = reportSecurityContext;
        this.permissionManager 
            = new ReportSecurityPermissionManager(reportSecurityContext);
    }

    /**
     * 
     * check if permission is allowed beeing executed,
     * return with voiud in case of positive result,
     * otherwise an {@link SecurityException} ( {@link ReportSecurityException}
     * in most cases) gets thrown
     */
    @Override
    public void checkPermission(Permission permission){
        if (isCalledByRunQuery()){
            if (!protectionEnabled){
                return;
            }
            permissionSpecificHandling(permission);
        }
    }


    /**
     * methods is called any time a permission that needs to be handled by
     * the security manager appears in the context of report-generation
     *  
     * Permission gets passed to an  
     * @param permission
     */
    private void permissionSpecificHandling(Permission permission) {

        IReportPermissionHandler handler = 
                permissionManager.getHandlerForPermission(permission);

        handler.handlePermission(permission);
    }


    /**
     * preventes use of Runtime.getRuntime().exec("rm -rf /");
     */
    @Override
    public void checkExec(String command){
        if (!protectionEnabled){
            return;
        }
        if (isCalledByRunQuery()){
            throw new ReportSecurityException(Messages.UNAUTHORIZED_EXECUTION_CALL_DETECTED);
        } 
    }

    /**
     * this disables(!) this manager and behaves like
     * System.setSecurityManager(null)
     * so call with caution!
     * 
     * its needed here because of report security is switchable by user and needs to be turned of again when report creation is finished 
     * @param protectionEnabled
     */
    protected synchronized void setProtectionEnabled(boolean protectionEnabled){
        this.protectionEnabled = protectionEnabled;
    }

    private boolean isCalledByRunQuery(){
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()){
            if (stackTraceElement.toString().startsWith(VERINICE_RUN_QUERY_METHOD)){
                return true;
            }
        }
        return false;
    }

    protected String getReportOutputName(){
        return this.reportSecurityContext.getTemplateMetaData().getOutputname();
    }
}
