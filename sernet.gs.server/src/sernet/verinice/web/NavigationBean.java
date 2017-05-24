/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.web;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "nav")
@SessionScoped
public class NavigationBean {
    
    public static final String VIEW_ID_TASK = "/todo/task.xhtml";
    
    public static final String VIEW_ID_TODO = "/todo/todo.xhtml";
    
    public static final String VIEW_ID_EDIT = "/edit/index.xhtml";

    public static final String CLASS_ACTIVE = "active";
    
    public static final String CLASS_INACTIVE = "inactive";
    
    public static final String CLASS_NAV_ACTIVE = "liNavi active";

    public static final String CLASS_NAV_INACTIVE = "liNavi";

    public String getTaskStyle() {
        return VIEW_ID_TASK.equals(getViewId()) ? CLASS_ACTIVE : CLASS_INACTIVE;
    }
    
    public String getBsiTodoStyle() {
        return VIEW_ID_TODO.equals(getViewId()) ? CLASS_ACTIVE : CLASS_INACTIVE;
    }

    public String getEditStyle() {
        return VIEW_ID_EDIT.equals(getViewId()) ? CLASS_ACTIVE : CLASS_INACTIVE;
    }

    public String getIndexNavStyle() {
        String fullId = "/portal/index.xhtml";
        return fullId.equals(getViewId()) ? CLASS_NAV_ACTIVE : CLASS_NAV_INACTIVE;
    }

    public String getDownloadNavStyle() {
        String fullId = "/portal/download.xhtml";
        return fullId.equals(getViewId()) ? CLASS_NAV_ACTIVE : CLASS_NAV_INACTIVE;
    }

    public String getManualNavStyle() {
        String fullId = "/portal/manual.xhtml";
        return fullId.equals(getViewId()) ? CLASS_NAV_ACTIVE : CLASS_NAV_INACTIVE;
    }

    public String getTodoNavStyle() {
        return VIEW_ID_TODO.equals(getViewId()) || VIEW_ID_EDIT.equals(getViewId()) || VIEW_ID_TASK.equals(getViewId()) ? CLASS_NAV_ACTIVE : CLASS_NAV_INACTIVE;
    }
    
    private String getViewId() {
        return FacesContext.getCurrentInstance().getViewRoot().getViewId();
    }
}
