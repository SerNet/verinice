/*******************************************************************************
 * Copyright (c) 2019 Alexander Koderman
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
 *     Alexander Koderman - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces.oda;

import sernet.hui.common.connect.Entity;

/**
 * A filter that can be used to include or exclude <code>Entity</code> objects
 * based on criteria given at filter creation.
 * 
 * @author akoderman
 *
 */
public interface IChainableFilter {

    /**
     * Test the given entity object against the filter criteria.
     * 
     * @param entity
     * @return <code>true</code> if the filter matches. <code>false</code> if
     *         not.
     */
    public boolean matches(Entity entity);

    /**
     * Return this filter wrapped in a <code>List</code> object. May be used to
     * chain filters together easily.
     * 
     * @return
     */
    public FilterChain asList();
}
