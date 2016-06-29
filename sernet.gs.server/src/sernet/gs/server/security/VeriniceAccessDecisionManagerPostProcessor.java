/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels <bw@sernet.de>.
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
 *     Benjamin Weißenfels <bw@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.server.security;

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.AccessDecisionManager;
import org.springframework.security.vote.AffirmativeBased;
import org.springframework.security.vote.RoleVoter;

/**
 * The default {@link AccessDecisionManager} is always initialized with the
 * {@link RoleVoter}, which does not understand our ACTION_ID model.
 * 
 * <p>
 * This bean must be available before the spring security chain is initialized,
 * otherwise spring throws an exception that beans which are annotated with
 * action ids are unknown roles.
 * </p>
 *
 * @author Benjamin Weißenfels <bw@sernet.de>
 *
 */
public class VeriniceAccessDecisionManagerPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private static final String VERINICE_ACTION_ID_VOTER_BEAN_NAME = "veriniceActionIdVoter";

    private static final String DEFAULT_ACCESS_MANAGER_BEAN_NAME = "_accessManager";

    private ApplicationContext applicationContext;

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.config.BeanPostProcessor#
     * postProcessBeforeInitialization(java.lang.Object, java.lang.String)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        return bean;
    }

    private boolean isDefaultAccessManagerBean(Object bean, String beanName) {
        return (bean instanceof AffirmativeBased) && DEFAULT_ACCESS_MANAGER_BEAN_NAME.equals(beanName);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.config.BeanPostProcessor#
     * postProcessAfterInitialization(java.lang.Object, java.lang.String)
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        if (isDefaultAccessManagerBean(bean, beanName)) {
            AffirmativeBased affirmativeBased = (AffirmativeBased) bean;
            List decisionVoters = affirmativeBased.getDecisionVoters();

            // initialize voter by hand
            VeriniceActionIdVoter veriniceActionIdVoter = (VeriniceActionIdVoter) applicationContext.getBean(VERINICE_ACTION_ID_VOTER_BEAN_NAME);
            decisionVoters.add(veriniceActionIdVoter);
        }

        return bean;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.context.ApplicationContextAware#setApplicationContext
     * (org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

    }
}
