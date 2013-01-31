/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.server;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.util.StopWatch;

/**
 * Logs the runtime of methods of a ProceedingJoinPoint.
 * Method logRuntime is executes by Spring AOP.
 * 
 * See Spring AOP configuration in veriniceserver-debug.xml.
 * 
 * Activate Logging by setting Log4j-Log-Level of this class and of targetName class
 * to DEBUG.
 * 
 * See: http://murygin.wordpress.com/2007/10/25/logging-mit-spring-aop/
 * for a german documentation.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class RuntimeLogger { 
	private static final Logger LOG = Logger.getLogger(RuntimeLogger.class);

	// this method is the around advice
	public Object logRuntime(final ProceedingJoinPoint call) throws Throwable {
		Object returnValue;
		if (LOG.isDebugEnabled()) {
			final String targetClassName = call.getTarget().getClass().getName();
			final String simpleTargetClassName = call.getTarget().getClass().getSimpleName();
			final String targetMethodName = call.getSignature().getName();
			final Logger targetLog = Logger.getLogger(targetClassName);
			if (targetLog.isDebugEnabled()) {
				final StopWatch clock = new StopWatch(getClass().getName());
				try {
					clock.start(call.toShortString());
					returnValue = call.proceed();
				} finally {
					clock.stop();
					final StringBuffer sb = new StringBuffer("Laufzeit ");
					sb.append(simpleTargetClassName).append(".").append(targetMethodName).append(": ");
					sb.append(clock.getTotalTimeMillis());
					targetLog.debug(sb.toString());
				}
			} else {
				returnValue = call.proceed();
			}
		} else {
			returnValue = call.proceed();
		}
		return returnValue;
	}
}
