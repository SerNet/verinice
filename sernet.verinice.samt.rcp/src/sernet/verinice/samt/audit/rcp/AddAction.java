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

import java.util.Arrays;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.iso27k.rcp.action.AddGroup;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Set the {@link CnATreeElement} type which is displayed in a
 * {@link GenericElementView}
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class AddAction extends Action implements ISelectionListener {

    private static final Logger LOG = Logger.getLogger(AddAction.class);

    private String objectTypeId;

    private GenericElementView groupView;

    /**
     * Creates an action to set the typeId of an groupView
     * 
     * @param groupView
     *            the view the type is displyed
     * @param typeId
     *            {@link CnATreeElement} type
     * @param title
     * @param groupView
     */
    public AddAction(String typeId, String title, GenericElementView groupView) {
        this.objectTypeId = typeId;
        String title_0 = title;
        if (title_0 == null) {
            title_0 = AddGroup.TITLE_FOR_TYPE.get(typeId);
        }
        setText(title_0);
        setImageDescriptor(ImageDescriptor.createFromImage(ImageCache.getInstance().getISO27kTypeImage(objectTypeId)));
        this.groupView = groupView;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        try {
            CnATreeElement newElement = null;
            CnATreeElement group = this.groupView.getGroupToAdd();
            if (group != null) {
                group = Retriever.retrieveElement(group, new RetrieveInfo().setProperties(true).setChildren(true).setParent(true));
                newElement = CnAElementFactory.getInstance().saveNew(group, this.objectTypeId, null);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("New element - type: " + newElement.getObjectType() + ", title: " + newElement.getTitle() + ", group: " + group.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                // create a link to last selected (foreign) element
                // if no group in this view is selected
                if (groupView.getElementToLink() != null && groupView.getSelectedGroup() == null) {
                    // this method also fires events for added links:
                    CnAElementHome.getInstance().createLinksAccordingToBusinessLogic(newElement, Arrays.asList(groupView.getElementToLink()));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("New element linked - type: " + groupView.getElementToLink().getObjectType() + ", title: " + groupView.getElementToLink().getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    // link is created asynchron
                    // editor is opened in TreeUpdateListener of ElmentView
                    // when linkAdded event is fired
                    groupView.registerforEdit(newElement);
                } else {
                    EditorFactory.getInstance().openEditor(newElement);
                }
            } else {
                LOG.warn("Can't add element. No group found. Type: " + this.objectTypeId); //$NON-NLS-1$
            }
        } catch (Exception e) {
            LOG.error("Error while creating new element.", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.AddAction_1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.
     * IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (part instanceof ElementView && selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof CnATreeElement) {
                boolean addElementEnabled = false;
                String elementType = groupView.getCommandFactory().getElementTypeId();
                String groupType = groupView.getCommandFactory().getGroupTypeId();
                String type = elementType;
                if(objectTypeId.equals(groupType)) {
                    // this is an add group action
                    type = groupType;
                }
                String selectedElementType = ((CnATreeElement) element).getTypeId();
                EntityType entityType = HitroUtil.getInstance().getTypeFactory().getEntityType(selectedElementType);
                Set<HuiRelation> relationSet = entityType.getPossibleRelations();
                for (HuiRelation huiRelation : relationSet) {
                    if (huiRelation.getTo().equals(type)) {
                        addElementEnabled = true;
                        break;
                    }
                }
                this.setEnabled(addElementEnabled);
            }
        }
    }

}
