/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.ITypedElement;

@SuppressWarnings("serial")
public class Permission implements Serializable, ITypedElement, Comparable<Permission> {

	private static transient Logger log = Logger.getLogger(Permission.class);
	
    private static Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(Permission.class);
        }
        return log;
    }
	
	private Integer dbId;
	
	private CnATreeElement cnaTreeElement;
	
	private String role;
	
	private boolean readAllowed;
	
	private boolean writeAllowed;

    public static final String TYPE_ID = "permission";
	
	protected Permission() {
		// Constructor for Hibernate - does intentionally nothing.
	}
	
	 /* (non-Javadoc)
     * @see sernet.hui.common.connect.ITypedElement#getTypeId()
     */
    public String getTypeId() {
        return TYPE_ID;
    }
	
	public Integer getDbId() {
		return dbId;
	}

	public void setDbId(Integer dbId) {
		this.dbId = dbId;
	}

	public CnATreeElement getCnaTreeElement() {
		return cnaTreeElement;
	}

	public void setCnaTreeElement(CnATreeElement cnaTreeElement) {
		this.cnaTreeElement = cnaTreeElement;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isReadAllowed() {
		return readAllowed;
	}

	public void setReadAllowed(boolean readAllowed) {
		this.readAllowed = readAllowed;
	}

	public boolean isWriteAllowed() {
		return writeAllowed;
	}

	public void setWriteAllowed(boolean writeAllowed) {
		this.writeAllowed = writeAllowed;
	}
	
	/**
	 * Creates a {@link Permission} instance and automatically ties it to a
	 * {@link CnATreeElement}.
	 * 
	 * @param treeElement
	 * @param role
	 * @param readAllowed
	 * @param writeAllowed
	 * @return
	 */
	public static Permission createPermission( 
			CnATreeElement treeElement, 
			String role, 
			boolean readAllowed, 
			boolean writeAllowed) {
		Permission p = new Permission();
		p.setCnaTreeElement(treeElement);
		p.setRole(role);
		p.setReadAllowed(readAllowed);
		p.setWriteAllowed(writeAllowed);	
		return p;
	}

	/**
	 * Copy a set of permissions to an object.
	 * 
	 * @param cte the target object on which to set the permissions
	 * @param perms the permission currently assigned on the source object
	 * @return newly created set of permissions
	 */
	public static Set<Permission> clonePermissionSet(CnATreeElement cte, Set<Permission> perms) {
		HashSet<Permission> clone = null;
		if(cte==null) {
			 getLog().warn("Element is null");
		} else if(cte.getUuid()==null) {
		     getLog().warn("Element uuid is null");
		}
		if(perms!=null) {
			clone = new HashSet<Permission>(perms.size());	
			for (Permission p : perms) {
				Permission np = clonePermission(cte, p);		
				clone.add(np);
			}	
		}
		return clone;
	}

	public static Permission clonePermission(CnATreeElement cte, Permission p) {
		Permission np = createPermission(
							cte,
							p.getRole(),
							p.isReadAllowed(),
							p.isWriteAllowed());
		return np;
	}

	@Override
	public int hashCode() {
		int result = 1;
		try {
			final int prime = 31;
			
			result = prime * result + ((cnaTreeElement==null) ? 0 : cnaTreeElement.hashCode());
			result = prime * result + ((role == null) ? 0 : role.hashCode());
		} catch(Throwable t ) {
			getLog().error("Error while creating hashcode, element UUID: " + cnaTreeElement.getUuid() + ", role: " + role, t);
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		try {
			if (this == obj){
				return true;
			}
			if (!(obj instanceof Permission)){
				return false;
			}
			Permission other = (Permission) obj;
			if (cnaTreeElement == null && other.cnaTreeElement != null){
			    return false;
			} else if (!cnaTreeElement.equals(other.cnaTreeElement)) {
				return false;
			}
			if (role == null && other.role != null){
			    return false;
			} else if (!role.equals(other.role)){
				return false;
			}
			return true;
		} catch(Throwable t ) {
			getLog().error("Error in equals, element UUID: " + cnaTreeElement.getUuid() + ", role: " + role, t);
			return false;
		}
	}

	@Override
	public int compareTo(Permission o) {
		final int thisIsLess = -1;
		final int equal = 0;
		final int thisIsGreater = 1;
		int result = thisIsLess;
		if(o!=null && o.getRole()!=null) {
			if(this.getRole()!=null) {
				result = this.getRole().compareTo(o.getRole());
			} else {
				result = thisIsGreater;
			}
		} else {
			if(this.getRole()==null) {
				result = equal;
			}
		}
		return result;
	}
}
