/*******************************************************************************
 * Copyright (c) 2017 Benjamin Weißenfels.
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
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package org.primefaces.poseidon.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.ui.FilterChainOrder;
import org.springframework.security.ui.SpringSecurityFilter;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class HandleXhrTimeoutFilter extends SpringSecurityFilter {

    private String invalidSessionUserName;

    private String getLoginUrl() {
        return "/auth/login.xhtml";
    }

    @Override
    public int getOrder() {
        return FilterChainOrder.LOGOUT_FILTER;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.security.ui.SpringSecurityFilter#doFilterHttp(javax.
     * servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
     * javax.servlet.FilterChain)
     */
    @Override
    protected void doFilterHttp(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (isInValidSession() && isXHR(req)) {
            // Redirecting an ajax request has to be done in the following
            // way:
            // http://javaevangelist.blogspot.dk/2013/01/jsf-2x-tip-of-day-ajax-redirection-from.html
            String redirectURL = res.encodeRedirectURL(req.getContextPath() + getLoginUrl());
            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><partial-response><redirect url=\"").append(redirectURL).append("\"></redirect></partial-response>");
            res.setCharacterEncoding("UTF-8");
            res.setContentType("text/xml");
            PrintWriter pw = res.getWriter();
            pw.println(sb.toString());
            pw.flush();

        } else {
            chain.doFilter(req, res);
        }
    }

    private boolean isXHR(HttpServletRequest req) {
        return "partial/ajax".equals(req.getHeader("Faces-Request"));
    }

    private boolean isInValidSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal().equals(invalidSessionUserName);
    }

    public String getInvalidSessionUserName() {
        return invalidSessionUserName;
    }

    public void setInvalidSessionUserName(String invalidSessionUserName) {
        this.invalidSessionUserName = invalidSessionUserName;
    }

}
