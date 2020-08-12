/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.IHibernateCommandService;
import java.io.ObjectInputFilter;
import java.io.ObjectInputFilter.Config;

/**
 * Initialize environemnt on Verinice server on startup.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 *
 */
public class ServerInitializer {

    private static final Logger log = Logger.getLogger(ServerInitializer.class);

    private static final AtomicBoolean filterEnabled = new AtomicBoolean();

    private static VeriniceContext.State state;

    private IHibernateCommandService hibernateCommandService;

    /**
     * Initializes the current thread with the VeriniceContext.State of the
     * client application.
     * 
     * <p>
     * Calling this method is needed when the Activator was run on a different
     * thread then the Application class.
     * </p>
     *
     * <p>
     * This method is called by a ServletRequestListener so make sure that you
     * really need to call this method. If you are in something like a JSF bean
     * the call goes always through the servlet stack and the context is already
     * set.
     * </p>
     */
    public static void inheritVeriniceContextState() {
        VeriniceContext.setState(state);
    }

    public void initialize() {
        log.debug("Initializing server context...");
        // After this we can use the getInstance() methods from HitroUtil and
        // GSScraperUtil
        VeriniceContext.setState(state);

        // The work objects in the HibernateCommandService can only be set
        // at this point because otherwise we would have a circular dependency
        // in the Spring configuration (= commandService needs workObjects
        // and vice versa)
        if (hibernateCommandService != null) {
            hibernateCommandService.setWorkObjects(state);
        }

        // Our tests create multiple contexts, each of which initializes a
        // server context. We need to make sure that we enable the filter only
        // once.
        if (filterEnabled.compareAndSet(false, true)) {
            enableSerializationFilter();
        }
    }

    private void enableSerializationFilter() {
        Config.setSerialFilter(new VeriniceSerializationFilter());
    }

    public void setWorkObjects(VeriniceContext.State workObjects) {
        ServerInitializer.state = workObjects;
    }

    public VeriniceContext.State getWorkObjects() {
        return state;
    }

    public void setHibernateCommandService(IHibernateCommandService hibernateCommandService) {
        this.hibernateCommandService = hibernateCommandService;
    }

    public IHibernateCommandService getHibernateCommandService() {
        return hibernateCommandService;
    }

    private static final class VeriniceSerializationFilter implements ObjectInputFilter {

        static {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    VeriniceSerializationFilter.class
                            .getResourceAsStream("/serialization-filter-class-whitelist.txt"),
                    StandardCharsets.UTF_8))) {
                allowedClasses = br.lines().collect(Collectors.toSet());
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize serialization filter");
            }

        }
        private static final Set<String> allowedClasses;
        private final List<String> allowedClassNamePrefixes = Arrays.asList("sernet.",
                "org.jgrapht.util.SupplierUtil$$");

        @Override
        public Status checkInput(FilterInfo arg0) {
            Class<?> serialClass = arg0.serialClass();
            if (log.isInfoEnabled()) {
                log.info("Checking deserialization for class " + serialClass + ", at depth "
                        + arg0.depth());
            }
            if (serialClass == null) {
                log.debug(" -> Allowing reference to existing object");
                return Status.ALLOWED;
            }
            if (serialClass.isPrimitive()) {
                log.debug("-> Allowing deserialization of primitive type");
                return Status.ALLOWED;
            }
            if (serialClass.isArray()) {
                do {
                    serialClass = serialClass.getComponentType();
                } while (serialClass.isArray());
                if (log.isDebugEnabled()) {
                    log.debug("Unwrapped array base type: " + serialClass);
                }
                if (serialClass.isPrimitive()) {
                    log.debug("-> Allowing array of primitive type");
                    return Status.ALLOWED;
                }
            }
            if (allowedClasses.contains(serialClass.getName())) {
                log.debug("-> Allowing deserialization because of class name whitelist");
                return Status.ALLOWED;
            }
            String className = serialClass.getName();
            if (allowedClassNamePrefixes.stream().anyMatch(className::startsWith)) {
                log.debug("-> Allowing deserialization because of package name whitelist");
                return Status.ALLOWED;
            }
            log.error("Rejecting deserialization of data with class " + serialClass);
            return Status.REJECTED;
        }
    }

}
