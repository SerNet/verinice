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
import org.springframework.security.AccessDecisionManager;
import org.springframework.security.vote.AffirmativeBased;
import org.springframework.security.vote.RoleVoter;

/**
 * The default {@link AccessDecisionManager} is always initialized with the
 * {@link RoleVoter}, which does not understand our ACTION_ID modell.
 *
 * @author Benjamin Weißenfels <bw@sernet.de>
 *
 */
public class VeriniceAccessDecisionManagerPostProcessor implements BeanPostProcessor {

    private final String DEFAULT_ACCESS_MANAGER_BEAN_NAME = "_accessManager";

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.config.BeanPostProcessor#
     * postProcessBeforeInitialization(java.lang.Object, java.lang.String)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {

        if (isDefaultAccessManagerBean(bean, beanName)) {
            AffirmativeBased affirmativeBased = (AffirmativeBased) bean;
            List decisionVoters = affirmativeBased.getDecisionVoters();
            decisionVoters.add(new VeriniceAccessDecisionVoter());
        }

        return bean;
    }

    private boolean isDefaultAccessManagerBean(Object bean, String beanName) {
        return (bean instanceof AffirmativeBased)
                && DEFAULT_ACCESS_MANAGER_BEAN_NAME.equals(beanName);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.config.BeanPostProcessor#
     * postProcessAfterInitialization(java.lang.Object, java.lang.String)
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }
}
