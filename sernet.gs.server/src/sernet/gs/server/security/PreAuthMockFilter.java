/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin <dm[at]sernet[dot]de>.
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
package sernet.gs.server.security;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.log4j.Logger;

/**
 * Use this for testing only!
 * 
 * Sevlet filter which adds a HTTP-Header to every request to test
 * HTTP Header pre authentication of verinice.
 * See veriniceserver-security-preauth.xml
 * 
 * You can activate this filter in web.xml by adding a filter definition:
 * <filter>
 *	<filter-name>preauthMockFilter</filter-name>
 *	<filter-class>sernet.gs.server.security.PreAuthMockFilter</filter-class>
 * </filter>
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class PreAuthMockFilter implements Filter {
	
	private static Logger log = Logger.getLogger(PreAuthMockFilter.class);
	
	private FilterConfig filterConfig = null;
	
	/**
	 * Values of HTTP header: user name in a pre auth senario
	 */
	private static final String DEFAULT_USER_NAME = "admin"; 
	
	private static final String USER_NAME_PARAM = "preauthMockUser";
	
	/**
	 * Name of HTTP header
	 */
	private static String httpRequestHeaderName = "iv-user";
	
	
	public void destroy() {
		log.debug("entered MockAuthFilter.destroy() method");
		this.filterConfig = null;
		log.debug("exited MockAuthFilter.destroy() method");
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if(log.isDebugEnabled()) {
			log.debug("entered MockAuthFilter.doFilter() method");
		}
		String userName = DEFAULT_USER_NAME;
		ServletContext context = this.filterConfig.getServletContext();
		if(context==null){
			log.warn("this.filterConfig.getServletContext()== NULL !!");
		} else {
		    if(context.getInitParameter(USER_NAME_PARAM)!=null) {
		        userName = context.getInitParameter(USER_NAME_PARAM);
		    }
		}
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest((HttpServletRequest) request);
		
		if(mockRequest.getHeader(httpRequestHeaderName)==null || !mockRequest.getHeader(httpRequestHeaderName).equalsIgnoreCase(userName)){
			mockRequest.addHeader(httpRequestHeaderName, userName);	
			if(log.isDebugEnabled()) {
				log.debug("HTTP header added, name: " + httpRequestHeaderName + ", value: " + userName);
			}
		}
		chain.doFilter(mockRequest, response);
	}

	public void init(FilterConfig arg0) throws ServletException {
		log.debug("entered MockAuthFilter.init() method");
		this.filterConfig = arg0;
		log.debug("exited MockAuthFilter.init() method");
	}
	
	class MockHttpServletRequest extends HttpServletRequestWrapper {

		private Hashtable<String, String> headerMap;
		
		public MockHttpServletRequest(HttpServletRequest request) {
			super(request);
			headerMap = new Hashtable<String, String>();
		}
		
		@Override
		public String getHeader(String name) {
			String value = headerMap.get(name);
			if(value==null) {
				value = super.getHeader(name);
			}
			return value;
		}
		
		@Override
		public Enumeration<String> getHeaderNames() {
			// TODO: add headerMap keys
			return super.getHeaderNames();
		}

		public void addHeader(String name, String value) {
			headerMap.put(name, value);
		}
		
	}
}
