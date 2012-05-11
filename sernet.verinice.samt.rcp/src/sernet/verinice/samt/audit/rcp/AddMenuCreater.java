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

import java.util.Set;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.iso27k.rcp.action.AddElement;
import sernet.verinice.iso27k.rcp.action.AddGroup;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Creates a pulldown menu in a view toolbar to switch the {@link CnATreeElement}
 * type of the view
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class AddMenuCreater implements IViewActionDelegate, IMenuCreator, ISelectionListener {
    
    private IAction action;
    private Menu menu;
    private boolean enabled = true;
    private GenericElementView groupView;
    private AddAction addElementAction;
    private AddAction addGroupAction;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart view) {
        if(view instanceof ElementView) {
            this.groupView = (GenericElementView) view;
            final String typeId = groupView.getCommandFactory().getElementTypeId();
            final String groupId = groupView.getCommandFactory().getGroupTypeId();
            // this is not a typo: "groupId"
            String title = AddElement.TITLE_FOR_TYPE.get(groupId);
            addElementAction = new AddAction(typeId, title, groupView);
            addElementAction.setImageDescriptor(ImageDescriptor.createFromImage(ImageCache.getInstance().getISO27kTypeImage(typeId)));
            //view.getSite().getPage().addPostSelectionListener(addElementAction);
            title = AddGroup.TITLE_FOR_TYPE.get(groupId);
            addGroupAction = new AddAction(groupId, title, groupView);
            addGroupAction.setImageDescriptor(ImageDescriptor.createFromImage(ImageCache.getInstance().getISO27kTypeImage(groupId))); 
            //view.getSite().getPage().addPostSelectionListener(addGroupAction);              
            view.getSite().getPage().addPostSelectionListener(this);
        }       
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        addElementAction.run();
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
      if (action != this.action)
      {
        action.setMenuCreator(this);
        this.action = action;
      }
    }

    // IMenuCreator methods
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#dispose()
     */
    public void dispose()
    {
      if (menu != null)
      {
        menu.dispose();
      }
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
     */
    public Menu getMenu(Control parent)
    {
      menu = new Menu(parent);
      if(isEnabled()) {
          addActionToMenu(menu, addElementAction);
          addActionToMenu(menu, addGroupAction);
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
    
    /*
     * (non-Javadoc)
     * 
     * @seeorg.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.
     * IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (part instanceof ElementView && part!=groupView) {
            if (selection instanceof IStructuredSelection) {
                Object element = ((IStructuredSelection) selection).getFirstElement();
                if (element instanceof CnATreeElement) {
                    boolean addElementEnabled = false;
                    boolean addGroupEnabled = false;
                    String elementType = groupView.getCommandFactory().getElementTypeId();
                    String groupType = groupView.getCommandFactory().getGroupTypeId();
                    String selectedElementType = ((CnATreeElement) element).getTypeId();
                    EntityType entityType = HitroUtil.getInstance().getTypeFactory().getEntityType(selectedElementType);
                    Set<HuiRelation> relationSet = entityType.getPossibleRelations();
                    for (HuiRelation huiRelation : relationSet) {
                        if (huiRelation.getTo().equals(elementType)) {                          
                            addElementEnabled = true;
                        }
                        if (huiRelation.getTo().equals(groupType)) {                     
                            addGroupEnabled = true;
                        }
                        if(addElementEnabled && addGroupEnabled) {
                            break;
                        }
                    }
                    addElementAction.setEnabled(addElementEnabled);
                    addGroupAction.setEnabled(addGroupEnabled); 
                    setEnabled(addElementEnabled||addGroupEnabled);
                    action.setEnabled(isEnabled());
                }
            }
        }
    }


    private void addActionToMenu(Menu menu, IAction action)
    {
      ActionContributionItem item= new ActionContributionItem(action);
      item.fill(menu, -1);
    }


    protected boolean isEnabled() {
        return enabled;
    }


    protected void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

  }

