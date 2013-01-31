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
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.web;

import java.io.IOException;

import javax.faces.application.ResourceHandler;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import sernet.gs.service.ServerInitializer;
import sernet.hui.common.VeriniceContext;

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
		if(VeriniceContext.getServerUrl()==null) {
		    VeriniceContext.setServerUrl(getServerUrl((HttpServletRequest) request));
		}
		
		ServerInitializer.inheritVeriniceContextState();
		
		disableCaching((HttpServletRequest)request, (HttpServletResponse)response);
		
		// proceed along the chain
	    chain.doFilter(request, response);
	}

    private String getServerUrl(HttpServletRequest request) {
        final int port80 = 80;
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int portNumber = request.getServerPort();
        String contextPath = request.getContextPath();
        StringBuilder sb = new StringBuilder();
        sb.append(scheme).append("://").append(serverName);
        if(portNumber!=port80) {
            sb.append(":").append(portNumber);
        }      
        if(contextPath!=null && !contextPath.isEmpty()) {
            sb.append(contextPath).append("/");
        } else {
            sb.append("/");
        }
        return sb.toString();
    }
    
    private void disableCaching(HttpServletRequest request, HttpServletResponse response)  {
        if (!request.getRequestURI().startsWith(request.getContextPath() + ResourceHandler.RESOURCE_IDENTIFIER)) { // Skip JSF resources (CSS/JS/Images/etc)
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
            response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
            response.setDateHeader("Expires", 0); // Proxies.
        }
    }

	public void init(FilterConfig arg0) throws ServletException {
		if (log.isDebugEnabled()) {
			log.debug("init called...");
		}

	}

}
