/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *      Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces;


public interface IRetrieveInfo {

	/**
	 * @return true if properties are joined and retrieved
	 */
	abstract boolean isProperties();

	abstract IRetrieveInfo setProperties(boolean properties);

	/**
	 * @return true if links-up are joined and retrieved
	 */
	abstract boolean isLinksUp();

	abstract IRetrieveInfo setLinksUp(boolean linksUp);

	/**
	 * @return true if properties of links-up are joined and retrieved
	 */
	abstract boolean isLinksUpProperties();

	abstract IRetrieveInfo setLinksUpProperties(boolean linksUpProperties);

	/**
	 * @return true if links-down are joined and retrieved
	 */
	abstract boolean isLinksDown();

	abstract IRetrieveInfo setLinksDown(boolean linksDown);

	/**
	 * @return true if properties of links-down are joined and retrieved
	 */
	abstract boolean isLinksDownProperties();

	abstract IRetrieveInfo setLinksDownProperties(
			boolean linksDownProperties);

	/**
	 * @return true if children are joined and retrieved
	 */
	abstract boolean isChildren();

	abstract IRetrieveInfo setChildren(boolean children);

	/**
	 * @return true if properties of children are joined and retrieved
	 */
	abstract boolean isChildrenProperties();

	abstract IRetrieveInfo setChildrenProperties(
			boolean childrenProperties);

	abstract IRetrieveInfo setGrandchildren(boolean grandchildren);

	abstract boolean isGrandchildren();

	abstract boolean isParent();
	
	abstract boolean isParentPermissions();

	abstract IRetrieveInfo setParent(boolean parent);

	abstract boolean isSiblings();

	abstract IRetrieveInfo setSiblings(boolean siblings);

	abstract boolean isPermissions();

	abstract IRetrieveInfo setPermissions(boolean permissions);

	abstract IRetrieveInfo setChildrenPermissions(
			boolean childrenPermissions);

	abstract boolean isChildrenPermissions();

	/**
	 * @return true if inner joins are used
	 */
	abstract boolean isInnerJoin();

	abstract IRetrieveInfo setInnerJoin(boolean innerJoin);

}