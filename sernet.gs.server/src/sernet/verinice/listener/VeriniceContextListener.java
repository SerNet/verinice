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
package sernet.verinice.listener;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.apache.log4j.Logger;

import sernet.gs.service.ServerInitializer;
import sernet.hui.common.VeriniceContext;

/**
 * Sets the verinice context state for every http request.
 * 
 * This is useful for frameworks like JSF which does not have access to the IoC
 * of spring. So in case of recycling of tomcat threads the
 * {@link VeriniceContext} is always initialized.
 * 
 * Note: The {@link ThreadLocal} of the {@link VeriniceContext} is not cleared
 * after every request because, some other process in other threads needs still
 * access. This behaviour is unclear and needs more investigation.
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class VeriniceContextListener implements ServletRequestListener {

    private final Logger log = Logger.getLogger(VeriniceContextListener.class);

    @Override
    public void requestDestroyed(ServletRequestEvent arg0) {

    }

    @Override
    public void requestInitialized(ServletRequestEvent servletRequestEvent) {
        if (VeriniceContext.getState().getMap().isEmpty()) {
            log.debug("verinice context map was not initalized.");
            ServerInitializer.inheritVeriniceContextState();
        }
    }
}
