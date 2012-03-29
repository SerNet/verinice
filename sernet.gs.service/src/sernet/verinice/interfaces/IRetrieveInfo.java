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
	public abstract boolean isProperties();

	public abstract IRetrieveInfo setProperties(boolean properties);

	/**
	 * @return true if links-up are joined and retrieved
	 */
	public abstract boolean isLinksUp();

	public abstract IRetrieveInfo setLinksUp(boolean linksUp);

	/**
	 * @return true if properties of links-up are joined and retrieved
	 */
	public abstract boolean isLinksUpProperties();

	public abstract IRetrieveInfo setLinksUpProperties(boolean linksUpProperties);

	/**
	 * @return true if links-down are joined and retrieved
	 */
	public abstract boolean isLinksDown();

	public abstract IRetrieveInfo setLinksDown(boolean linksDown);

	/**
	 * @return true if properties of links-down are joined and retrieved
	 */
	public abstract boolean isLinksDownProperties();

	public abstract IRetrieveInfo setLinksDownProperties(
			boolean linksDownProperties);

	/**
	 * @return true if children are joined and retrieved
	 */
	public abstract boolean isChildren();

	public abstract IRetrieveInfo setChildren(boolean children);

	/**
	 * @return true if properties of children are joined and retrieved
	 */
	public abstract boolean isChildrenProperties();

	public abstract IRetrieveInfo setChildrenProperties(
			boolean childrenProperties);

	public abstract IRetrieveInfo setGrandchildren(boolean grandchildren);

	public abstract boolean isGrandchildren();

	public abstract boolean isParent();
	
	public abstract boolean isParentPermissions();

	public abstract IRetrieveInfo setParent(boolean parent);

	public abstract boolean isSiblings();

	public abstract IRetrieveInfo setSiblings(boolean siblings);

	public abstract boolean isPermissions();

	public abstract IRetrieveInfo setPermissions(boolean permissions);

	public abstract IRetrieveInfo setChildrenPermissions(
			boolean childrenPermissions);

	public abstract boolean isChildrenPermissions();

	/**
	 * @return true if inner joins are used
	 */
	public abstract boolean isInnerJoin();

	public abstract IRetrieveInfo setInnerJoin(boolean innerJoin);

}