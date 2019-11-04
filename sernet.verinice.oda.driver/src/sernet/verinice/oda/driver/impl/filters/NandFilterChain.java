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
package sernet.verinice.oda.driver.impl.filters;


import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.oda.FilterChain;
import sernet.verinice.interfaces.oda.IChainableFilter;

/**
 * 
 * Returns false if all of the given filters match. Evaluation will
 * short-circuit, so as soon as one filter does not match, further
 * evaluation stops and <code>false</code> is returned.
 * 
 * Use this if you want to exclude entities if they show all of the given
 * properties.
 * 
 * To use this class comfortably, use the static convenience methods provided by the <code>Filters</code> factory class.
 * 
 */
public class NandFilterChain extends FilterChain implements IChainableFilter {

    private static final long serialVersionUID = -9156945143234284673L;

    public NandFilterChain(IChainableFilter[] filters) {
        super(filters);
    }
    
    @Override
    public boolean matches(Entity entity) {
        return ! getFilterList().stream().allMatch(aFilter -> aFilter.matches(entity));
    }
}
