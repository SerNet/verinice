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

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import bsh.Interpreter;
import sernet.verinice.oda.driver.impl.Query;


/**
 * special implementation of {@link ClassLoader} that prevents loading classes out of the sernet-namespace except some special utility classes.
 * This is loader is used by beanshell interpreter to execute/evaluate beanshell-code within BIRT-report-templates.
 * See {@link Query} for implementation of {@link Interpreter}
 * 
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
public class ReportClassLoader extends ClassLoader {
    
    
    private final static Logger LOG = Logger.getLogger(ReportClassLoader.class);
    
    private static final Set<String> PREFIXES = new HashSet<String>();
    
    static{
        PREFIXES.add("sernet.");
        PREFIXES.add("java.util.Arrays");
        PREFIXES.add("java.util.Collections");
        PREFIXES.add("java.util.List");
        PREFIXES.add("java.util.Set");
        PREFIXES.add("java.util.HashSet");
        PREFIXES.add("java.util.ArrayList");
        PREFIXES.add("java.text.DateFormat");
        PREFIXES.add("java.text.SimpleDateFormat");
        PREFIXES.add("java.util.regex");
        PREFIXES.add("java.util.Comparator");
    }
    
    
    @SuppressWarnings("restriction")
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
      if (isTrustedClass(name)){
          return super.loadClass(name);
      } else {
//          LOG.error("Prevent loading of class:\t" + name + " due to security restrictions");
          throw new ClassNotFoundException("Prevent loading of class:\t" + name + " due to security restrictions");
      }
    }

    /** only classes of sernet namespace are allowed to being exexcuted
     * implement a whitelist if more than sernet. namespace is needed here 
     * @param name
     * @return
     */
    private boolean isTrustedClass(String name){
        if(name != null){
            return checkPrefixes(name);
        }
        return false;
    }

    private boolean checkPrefixes(String name) {
        for(String prefix : PREFIXES){
            if(name.startsWith(prefix)){
                return true;
            }
        }
        return false;
    }

}
