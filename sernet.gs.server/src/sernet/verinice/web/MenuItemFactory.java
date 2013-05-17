/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.MethodExpressionActionListener;

import org.primefaces.component.menuitem.MenuItem;

/**
 * MenuItemFactory contains methods to create Primefaces menut items.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class MenuItemFactory {

    private static FacesContext facesCtx = FacesContext.getCurrentInstance();
    private static ELContext elementCtx = facesCtx.getELContext();
    private static ExpressionFactory factory = facesCtx.getApplication().getExpressionFactory();
    private static MethodExpression methodExpression = factory.createMethodExpression(elementCtx, "#{tree.selectPath}", Void.class,new Class[]{ActionEvent.class});
    private static MethodExpressionActionListener actionListener = new MethodExpressionActionListener(methodExpression);
    
    /**
     * Creates a new item for the breadcrumb menu 
     * 
     * @return A Primefaces menu item
     */
    public static MenuItem createNavigationMenuItem() {
        MenuItem item = new MenuItem();
        item.setStyle("padding: 0;");
        item.setUpdate(":tableForm,:navForm");
        item.addActionListener(actionListener);
        return item;
    }
}
