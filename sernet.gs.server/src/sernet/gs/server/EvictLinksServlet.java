/*******************************************************************************
 * Copyright (c) 2019 Alexander Ben Nasrallah.
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
 ******************************************************************************/
package sernet.gs.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

import sernet.gs.service.ServerInitializer;
import sernet.hui.common.VeriniceContext;

/**
 * This servlet provides a HTTP endpoint to evict links from 2nd level hibernate
 * cache.
 */
public class EvictLinksServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(EvictLinksServlet.class);
    private final SessionFactory sessionFactory;

    public EvictLinksServlet() {
        ServerInitializer.inheritVeriniceContextState();
        sessionFactory = (SessionFactory) VeriniceContext.get(VeriniceContext.SESSION_FACTORY);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        LOG.info("evicting links from 2nd level cache");
        sessionFactory.evictCollection("sernet.verinice.model.common.CnATreeElement.linksDown");
        sessionFactory.evictCollection("sernet.verinice.model.common.CnATreeElement.linksUp");
    }

}
