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

import sernet.verinice.interfaces.ICommand;

/**
 * <p>Commands can implement this interface if they support filtering their
 * delivered results.</p>
 * 
 * <p>The filter criteria must be set before the command is executed. Usually this
 * means that <code>setFilter()</code> has to be called before
 * <code>execute()</code> is called.</p>
 * 
 * <p>If it set after execution, the filter should not be taken into account. The
 * command should not apply the filter for returning results, i.e. the method
 * "getResults()" should not regard the filter.</p>
 * 
 * <p>If you do not actually set a filter on a filterable command, the default
 * implementation should be to return unfiltered results.</p>
 * 
 * @author akoderman
 *
 */
public interface IFilteringCommand extends ICommand {

    /**
     * The filter to be used by the command. This method must be called before
     * the command is executed.
     * 
     * @param filter
     *            The filter to be used. This can be a chained filter with many
     *            individual comparisons. It will be applied to all loaded elements.
     *            
     *            Use <code>null</code> if you want to unset an already existing filter.
     */
    public void setFilterCriteria(IChainableFilter filter);
    
    
    
    /**
     * Returns the state of the filter.
     * 
     * @return <code>true</code> if a filter is set. Returns <code>false</code> if a filter is currently not set on the command.
     */
    public boolean isFilterActive();
}
