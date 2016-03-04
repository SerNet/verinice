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

import org.apache.log4j.Logger;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;

import sernet.verinice.security.report.ReportSecurityContext;

/**
 * this thread passes a prepared {@link IRunAndRenderTask} (representing a verinice-report)
 * to the BIRT-Report-Engine and executes it from within a secured context. That
 * prevents executing unauthorized code via beanshell or javascript (executed via the rhino-engine)
 * code.
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
public class ReportExecutionThread extends Thread {
    
    private static final Logger LOG = Logger.getLogger(ReportExecutionThread.class);
    
    private ReportSecurityManager reportSecurityManager ;
    private ReportSecurityContext reportSecurityContext;
    private IRunAndRenderTask task;
    
    public ReportExecutionThread(IRunAndRenderTask task, ReportSecurityContext reportSecurityContext){
        this.task = task;
        this.reportSecurityContext = reportSecurityContext;
        reportSecurityManager = new ReportSecurityManager(reportSecurityContext);
    }
    
    @Override
    public void run() {
      SecurityManager old = System.getSecurityManager();
      System.setSecurityManager(reportSecurityManager);
      runUntrustedCode();
      // without this, we cannot reset the securityManager, because
      // that needs to be forbidden from within report excecution
      reportSecurityManager.setProtectionEnabled(false);
      System.setSecurityManager(old);
    }
    
    /**
     * note that the so called "untrusted" code is not the line
     * task.run()
     * but the user-generated code, contained in datasets or javascript snippets within the template
     */
    private void runUntrustedCode() {
      try {
          task.run();
      } catch (EngineException t) {
          LOG.error("Something went wrong on executing the report:\t" + reportSecurityContext.getRptDesignUrl(), t);
      }
    }

}
