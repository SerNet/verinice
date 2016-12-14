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
package sernet.verinice.oda.driver.designer;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;

import sernet.verinice.service.model.IObjectModelService;


/**
 *
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public final class ServiceFactory {

    // The shared instance.
    private static ServiceFactory instance = new ServiceFactory();
    
    private static BeanFactory beanFactory;
    
    private ServiceFactory() {
        // do not instantiate this class
    }
    
    public static ServiceFactory getInstance() {
        return instance;
    }
    
    public synchronized void openBeanFactory(BundleContext bundleContext) {
        if (beanFactory == null) {    
            Bundle bundle = Platform.getBundle("sernet.gs.server");
            URL url = getClass().getResource("spring-vdesigner.xml");
            OsgiBundleXmlApplicationContext applicationContext = new OsgiBundleXmlApplicationContext(new String[] { url.toString() });
            applicationContext.setBundleContext(bundleContext);
            applicationContext.refresh();
            beanFactory = applicationContext;
        }
    }
    
    public IObjectModelService getObjectModelService() {
        return (IObjectModelService) beanFactory.getBean("objectModelService");
    }
    
    
}
