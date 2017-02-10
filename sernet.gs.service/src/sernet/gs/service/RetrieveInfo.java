/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.service;

import java.io.Serializable;

import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IRetrieveInfo;
import sernet.verinice.model.common.CnATreeElement;


/**
 * Parameter of Method retrieve in {@link IBaseDao}.
 * Determined by RetrieveInfo retrieve will join references of {@link CnATreeElement}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class RetrieveInfo implements Serializable, IRetrieveInfo{
	
	private boolean properties = false;
	
	private boolean linksUp = false;
	
	private boolean linksUpProperties = false;
	
	private boolean linksDown = false;
	
	private boolean linksDownProperties = false;
	
	private boolean children = false;
	
	private boolean childrenProperties = false;
	
	private boolean grandchildren = false;
	
	private boolean parent = false;
	
	private boolean parentPermissions = false;
	
    private boolean parentProperties = false;

	private boolean siblings = false;
	
	private boolean permissions = false;
	
	private boolean childrenPermissions = false;
	
	private boolean innerJoin = false;

	public static RetrieveInfo getPropertyInstance() {
	    RetrieveInfo ri = new RetrieveInfo();
	    ri.setProperties(true);
		return ri;
	}
	
	public static RetrieveInfo getChildrenInstance() {
	    RetrieveInfo ri = new RetrieveInfo();
        ri.setChildren(true);
		return ri;
	}
	
	public static RetrieveInfo getPropertyChildrenInstance() {
	    RetrieveInfo ri = new RetrieveInfo();
        ri.setProperties(true);
        ri.setChildren(true);
		return ri;
	}
	
	public RetrieveInfo() {
		super();
	}

	/**
	 * @return true if properties are joined and retrieved
	 */
	@Override
    public boolean isProperties() {
		return properties;
	}

	@Override
    public RetrieveInfo setProperties(boolean properties) {
		this.properties = properties;
		return this;
	}

	/**
	 * @return true if links-up are joined and retrieved
	 */
	@Override
    public boolean isLinksUp() {
		return linksUp;
	}

	@Override
    public RetrieveInfo setLinksUp(boolean linksUp) {
		this.linksUp = linksUp;
		return this;
	}

	/**
	 * @return true if properties of links-up are joined and retrieved
	 */
	@Override
    public boolean isLinksUpProperties() {
		return linksUpProperties;
	}

	@Override
    public RetrieveInfo setLinksUpProperties(boolean linksUpProperties) {
	    if(linksUpProperties) {
	        this.setLinksUp(linksUpProperties);
	    }
		this.linksUpProperties = linksUpProperties;
		return this;
	}

	/**
	 * @return true if links-down are joined and retrieved
	 */
	@Override
    public boolean isLinksDown() {
		return linksDown;
	}

	@Override
    public RetrieveInfo setLinksDown(boolean linksDown) {
		this.linksDown = linksDown;
		return this;
	}

	/**
	 * @return true if properties of links-down are joined and retrieved
	 */
	@Override
    public boolean isLinksDownProperties() {
		return linksDownProperties;
	}

	@Override
    public RetrieveInfo setLinksDownProperties(boolean linksDownProperties) {
	    if(linksDownProperties) {
            this.setLinksDown(linksDownProperties);
        }
		this.linksDownProperties = linksDownProperties;
		return this;
	}
	
	/**
	 * @return true if children are joined and retrieved
	 */
	@Override
    public boolean isChildren() {
		return children;
	}

	@Override
    public RetrieveInfo setChildren(boolean children) {
		this.children = children;
		return this;
	}
	
	/**
	 * @return true if properties of children are joined and retrieved
	 */
	@Override
    public boolean isChildrenProperties() {
		return childrenProperties;
	}

	@Override
    public RetrieveInfo setChildrenProperties(boolean childrenProperties) {
		this.childrenProperties = childrenProperties;
		return this;
	}
	
	@Override
    public RetrieveInfo setGrandchildren(boolean grandchildren) {
		this.grandchildren = grandchildren;
		return this;
	}

	@Override
    public boolean isGrandchildren() {
		return grandchildren;
	}

	@Override
    public boolean isParent() {
		return parent;
	}

	@Override
    public RetrieveInfo setParent(boolean parent) {
		this.parent = parent;
		return this;
	}

    @Override
    public boolean isParentProperties() {
        return parentProperties;
    }

    @Override
    public RetrieveInfo setParentProperties(boolean parentProperties) {
        this.parentProperties = parentProperties;
        return this;
    }

    public RetrieveInfo setParentPermissions(boolean parentPermissions) {
        this.parentPermissions = parentPermissions;
        return this;  
    }

    @Override
    public boolean isParentPermissions() {
        return parentPermissions;
    }

    @Override
    public boolean isSiblings() {
		return siblings;
	}

	@Override
    public RetrieveInfo setSiblings(boolean siblings) {
		this.siblings = siblings;
		return this;
	}

	@Override
    public boolean isPermissions() {
		return permissions;
	}
	
	@Override
    public RetrieveInfo setPermissions(boolean permissions) {
		this.permissions = permissions;
		return this;
	}

	@Override
    public RetrieveInfo setChildrenPermissions(boolean childrenPermissions) {
		this.childrenPermissions = childrenPermissions;
		return this;
	}

	@Override
    public boolean isChildrenPermissions() {
		return childrenPermissions;
	}

	/**
	 * @return true if inner joins are used
	 */
	@Override
    public boolean isInnerJoin() {
		return innerJoin;
	}

	@Override
    public RetrieveInfo setInnerJoin(boolean innerJoin) {
		this.innerJoin = innerJoin;
		return this;
	}

	@Override
    public String toString() {
		StringBuffer sb = new StringBuffer("RetrieveInfo - ");
		if(properties) {
			sb.append( " properties");
		}
		if(linksUp) {
			sb.append(" linksUp");
		}
		if(linksUpProperties) {
			sb.append(" linksUpProperties").append(linksUpProperties);
		}
		if(linksDown) {
			sb.append(" linksDown");
		}
		if(linksDownProperties) {
			sb.append(" linksDownProperties");
		}
		if(children) {
			sb.append(" children");
		}
		if(childrenProperties) {
			sb.append(" childrenProperties");
		}
		if(childrenProperties) {
			sb.append(" innerJoin");
		}
		if(isSiblings()) {
			sb.append(" siblings");
		}
		if(isPermissions()) {
			sb.append(" permissions");
		}
		if(isChildrenPermissions()) {
			sb.append(" childrenPermissions");
		}
        if(isParent()) {
            sb.append(" parent");
        }
        if (parentProperties) {
            sb.append(" parentProperties");
        }
        if (parentProperties) {
            sb.append(" innerJoin");
        }
        if(isParentPermissions()) {
            sb.append(" parentPermissions");
        }
		return sb.toString();
	}

	
}
