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
package sernet.verinice.samt.audit.rcp;

import java.util.AbstractSequentialList;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Evidence;
import sernet.verinice.model.iso27k.Finding;
import sernet.verinice.model.iso27k.Organization;

/**
 * Creates a pulldown menu in a view toolbar to switch the {@link CnATreeElement}
 * type of the view
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class SwitchElementMenuCreater implements IViewActionDelegate, IMenuCreator {
    
    private IAction action;
    private GenericElementView groupView;
    private AbstractSequentialList<SwitchElementAction> handlerList;
    private Iterator<SwitchElementAction> handlerIterator;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart view) {
        if(view instanceof ElementView) {
            this.groupView = (GenericElementView) view;
            handlerList = new LinkedList<SwitchElementAction>();         
            handlerList.add(new SwitchElementAction(groupView,Asset.TYPE_ID));
            handlerList.add(new SwitchElementAction(groupView,sernet.verinice.model.iso27k.Control.TYPE_ID));           
            handlerList.add(new SwitchElementAction(groupView,Audit.TYPE_ID));           
            handlerList.add(new SwitchElementAction(groupView,Finding.TYPE_ID));       
            handlerList.add(new SwitchElementAction(groupView,Evidence.TYPE_ID));
            handlerList.add(new SwitchElementAction(groupView,Organization.TYPE_ID));        
            handlerIterator = handlerList.iterator();
        }       
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        if(handlerIterator!=null) {
            if(!handlerIterator.hasNext()) {
                handlerIterator = handlerList.iterator();
            }
            handlerIterator.next().run();
        }
    }


    // IMenuCreator methods
    public void selectionChanged(IAction action, ISelection selection)
    {
      if (action != this.action)
      {
        action.setMenuCreator(this);
        this.action = action;
      }
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#dispose()
     */
    public void dispose(){
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
     */
    public Menu getMenu(Control parent)
    {
      Menu menu = new Menu(parent);
      for (Iterator<SwitchElementAction> iterator = handlerList.iterator(); iterator.hasNext();) {
        addActionToMenu(menu, iterator.next());
      }
      return menu;
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
     */
    public Menu getMenu(Menu parent)
    {
      // Not used
      return null;
    }


    private void addActionToMenu(Menu menu, IAction action)
    {
      ActionContributionItem item= new ActionContributionItem(action);
      item.fill(menu, -1);
    }

  }

