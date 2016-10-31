/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp;

import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.UIServices;
import org.eclipse.equinox.p2.core.spi.IAgentServiceFactory;

/**
 * Component that provides a factory that can create and initialize
 * {@link UIServices} instances. The service which is created by the factory
 * provides authentication info for a HTTP request. You can set a 
 * custom message for the auth dialog in this service.
 * 
 * This service factory is configured in file OSGI-INF/uiservice.xml
 * and in the section "Service-Component:" in META-INF/MANIFEST.MF. 
 * 
 * See this article about OSGi Declarative Services:
 * http://www.ibm.com/support/knowledgecenter/de/SSEQTP_8.5.5/com.ibm.websphere.wlp.doc/ae/twlp_declare_services_ds.html
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ServiceUIComponent implements IAgentServiceFactory {
    
    public ServiceUIComponent() {
        super();
    }

    @Override
    public Object createService(IProvisioningAgent agent) {
        return new ValidationServiceUI();
    }
    

}
