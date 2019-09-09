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

import sernet.verinice.interfaces.oda.FilterChain;
import sernet.verinice.interfaces.oda.IChainableFilter;

/**
 * A factory for command filters. A comfortable way to use this class is to
 * statically import its methods:
 * 
 * <blockquote>
 * <pre>
 * import static sernet.verinice.oda.drivers.impl.filters.Filters.*;
 * </pre>
 * </blockquote>
 * 
 * It is then possible to create filters by chaining calls together:
 * 
 * <blockquote>
 * <pre>
 * and( is("prop_id1", "I want this"), is("prop_id2", "...and this") )
 * </pre>
 * </blockquote>
 * 
 * You can combine more filters if you want:
 * 
 * <blockquote>
 * <pre>
 *    or( and (
 *            is("prop_id1", "I want this"), 
 *            is("prop_id2", "...and this") 
 *        ),
 *        is("prop_id1", "Or simply this (forget about the first two)"
 *    )
 * </pre>
 * </blockquote>
 * 
 * To ignore elements when they contain a certain property:
 * <br/>(This will filter out all entities where the first OR last name is "JohnDoe").
 * 
 * <blockquote>
 * <pre>
 *    noneOf( 
 *         is("prop_lastname",  "JohnDoe"), 
 *         is("prop_firstname", "JohnDoe") 
 *    )
 * </pre>
 * </blockquote>
 * 
 * 
 * If you want to ignore only entities where BOTH properties match, use "notAll":
 * 
 * <blockquote>
 * <pre>
 *    notAll( 
 *         is("prop_lastname", "John"), 
 *         is("prop_firstname", "Doe") 
 *    )
 * </pre>
 * </blockquote>
 * 
 * This will filter out entities only if the first name is "John" AND the last name is "Doe".
 */
public final class Filters {

    private Filters() {
    }
    
    /**
     * Return true if the given value is present for the given property.
     * 
     * The value is given as a regular expression and will be matched against
     * the value of the given property-ID.
     * 
     * @param propertyType
     *            The wanted property-type-ID.
     * @param propertyValueRegex
     *            A regular expression to match the value of the given property
     *            against. The filter will run a case-sensitive regular
     *            expression match against the property value. The property
     *            value will be a literal value entered by the user, a date
     *            value in milliseconds, a translated user-facing String for a
     *            property-option or an integer value.
     * @return
     */
    public static IChainableFilter is(String propertyType, String propertyValueRegex) {
        return new SinglePropertyFilter(propertyType, propertyValueRegex);
    }

    /**
     * Returns true if all of the given filters match. Evaluation will
     * short-circuit, so as soon as one filter does not match, further
     * evaluation stops and <code>false</code> is returned.
     * 
     * @param filters
     * @return <code>true</code> if all of the filters match. <code>false</code>
     *         if one or more do not match.
     */
    public static FilterChain and(IChainableFilter... filters) {
        return new AndFilterChain(filters);
    }

    /**
     * Returns true if any of the given filters match. Evaluation will
     * short-circuit, so as soon as one filter matches, further evaluation stops
     * and <code>false</code> is returned.
     * 
     * @param filters
     * @return <code>true</code> if one or more of the filters match.
     *         <code>false</code> if not a single match is found.
     */
    public static FilterChain or(IChainableFilter... filters) {
        return new OrFilterChain(filters);
    }

    /**
     * Returns true if none of the given filters match. Evaluation will
     * short-circuit, so as soon as one filter matches, further evaluation stops
     * and <code>false</code> is returned.
     * 
     * Use this if you want to exclude entities with one of the given
     * properties.
     * 
     * @param filters
     * @return <code>true</code> if none of the filters match.
     *         <code>false</code> if at least one match is found.
     */
    public static FilterChain noneOf(IChainableFilter... filters) {
        return new NorFilterChain(filters);
    }

    /**
     * 
     * Returns false if all of the given filters match. Evaluation will
     * short-circuit, so as soon as one filter does not match, further
     * evaluation stops and <code>true</code> is returned.
     * 
     * Use this if you want to exclude entities if they show all of the given
     * properties.
     * 
     * @param filters
     * @return <code>true</code> if at least one of the filters does not match.
     *         <code>false</code> if
     */
    public static FilterChain notAll(IChainableFilter... filters) {
        return new NandFilterChain(filters);
    }

}
