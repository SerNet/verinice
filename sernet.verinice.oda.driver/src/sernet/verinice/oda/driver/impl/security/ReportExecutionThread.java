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
package sernet.verinice.oda.driver.impl.security;

/**
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
public class ReportExecutionThread extends Thread {
    
    private Object pass = new Object();
    private ReportClassLoader loader = new ReportClassLoader();
    private ReportSecurityManager sm = new ReportSecurityManager(pass);
    public void run() {
      SecurityManager old = System.getSecurityManager();
      System.setSecurityManager(sm);
      runUntrustedCode();
//      sm.disable(pass);
      System.setSecurityManager(old);
    }
    private void runUntrustedCode() {
      try {
        // run the custom class's main method for example:
//        loader.loadClass("customclassname")
//          .getMethod("main", String[].class)
//          .invoke(null, new Object[]{...});
          this.hashCode();
      } catch (Throwable t) {
          // LOG error
      }
    }

}
