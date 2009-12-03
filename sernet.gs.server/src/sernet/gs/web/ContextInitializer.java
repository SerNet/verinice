/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm@sernet.de>.
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
 *     Daniel <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;

import sernet.gs.server.ServerInitializer;

public class ContextInitializer implements Filter {

	private final Logger log = Logger.getLogger(ContextInitializer.class);

	public void destroy() {
		if (log.isDebugEnabled()) {
			log.debug("destroy called...");
		}

	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (log.isDebugEnabled()) {
			log.debug("doFilter called...");
		}
		ServerInitializer.inheritVeriniceContextState();
		// proceed along the chain
	    chain.doFilter(request, response);
	}

	public void init(FilterConfig arg0) throws ServletException {
		if (log.isDebugEnabled()) {
			log.debug("init called...");
		}

	}

}
