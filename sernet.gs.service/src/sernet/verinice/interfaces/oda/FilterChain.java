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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A series of filters that can be addressed as a single filter operation.
 * 
 * Leaves implementation of the actual match operation to the implementing
 * subclasses.
 * 
 * @author akoderman
 *
 */
public abstract class FilterChain implements IChainableFilter {

    /**
     * 
     */
    private static final long serialVersionUID = 3691821501759654412L;
    
    private List<IChainableFilter> filterList;
    
    public FilterChain(IChainableFilter... filters) {
        filterList = new ArrayList<>();
        filterList.addAll(Arrays.asList(filters));
    }
    
    /**
     * Limited direct access to the list of filters.
     * 
     * @return
     */
    protected List<IChainableFilter> getFilterList() {
        return this.filterList;
    }
    
    @Override
    public FilterChain asList() {
        return this;
    }
    
    /**
     * Add more filters to the series.
     * 
     * @param filterChain
     * @return
     */
    public FilterChain addFilters(FilterChain filterChain) {
        this.filterList.addAll(filterChain.getFilterList());
        return this;
    }
}
