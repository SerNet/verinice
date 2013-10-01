/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.iso27k;

import org.apache.log4j.Logger;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public final class InheritLogger {
    
    private final String classLog;
    
    private static final Logger INHERIT_LOG = Logger.getLogger("INHERIT");
    
    private InheritLogger(final String className) {
        classLog = new StringBuilder().append(" [").append(className).append("]").toString();
    }
    
    @SuppressWarnings("unchecked")
    public static InheritLogger getLogger(final Class clazz) {
        return new InheritLogger(clazz.getSimpleName());
    }
    
    public void debug(final Object o) {
        INHERIT_LOG.debug(concatLog(o));
    }  
    public void debug(final Object o, final Throwable t) {
        INHERIT_LOG.debug(concatLog(o),t);
    }
    public boolean isDebug() {
        return INHERIT_LOG.isDebugEnabled();
    }
    
    public void info(final Object o) {
        INHERIT_LOG.info(concatLog(o));
    }  
    public void info(final Object o, final Throwable t) {
        INHERIT_LOG.info(concatLog(o),t);
    }
    public boolean isInfo() {
        return INHERIT_LOG.isInfoEnabled();
    }
    
    public void warn(final Object o) {
        INHERIT_LOG.warn(concatLog(o));
    } 
    public void warn(final Object o, final Throwable t) {
        INHERIT_LOG.warn(concatLog(o),t);
    }
    
    public void error(final Object o) {
        INHERIT_LOG.error(concatLog(o));
    }    
    public void error(final Object o, final Throwable t) {
        INHERIT_LOG.error(concatLog(o),t);
    }
    
    private String concatLog(final Object o) {
        return new StringBuilder().append(o).append(classLog).toString();
    }
    
}
