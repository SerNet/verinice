/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.hibernate.StaleObjectStateException;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadElementForEditor;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.IEntityChangedListener;
import sernet.hui.common.connect.PropertyChangedEvent;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.multiselectionlist.IMLPropertyType;
import sernet.hui.swt.widgets.HitroUIComposite;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.IBSIStrukturKategorie;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.samt.SamtTopic;

/**
 * Editor for all BSI elements with attached HUI entities.
 * 
 * Uses the HUI framework to edit all properties defined in the entity's xml
 * description (SNCA.xml)
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */
public class BSIElementEditor extends EditorPart {
	public static final String EDITOR_ID = "sernet.gs.ui.rcp.main.bsi.editors.bsielementeditor"; //$NON-NLS-1$
	private HitroUIComposite huiComposite;
	private boolean isModelModified = false;
	private Boolean isWriteAllowed = null;
	
	// TODO the editor needs another way to determine whether or not to show the linkmaker so we can remove this reference to the SAMT bundle:
	public static final String SAMT_PERSPECTIVE_ID = "sernet.verinice.samt.rcp.SamtPerspective";
    // limit display in SAMT perspective to properties tagged as "VDA-ISA" (simplified view):
	private static final String SAMT_PERSPECTIVE_DEFAULT_TAGS = "VDA-ISA";
	
	private IEntityChangedListener modelListener = new IEntityChangedListener() {

		public void dependencyChanged(IMLPropertyType arg0, IMLPropertyOption arg1) {
			// not relevant
		}

		public void selectionChanged(IMLPropertyType arg0, IMLPropertyOption arg1) {
			modelChanged();
		}

		public void propertyChanged(PropertyChangedEvent evt) {
			modelChanged();
		}

	};
	private CnATreeElement cnAElement;
	private LinkMaker linkMaker;

	public void doSave(IProgressMonitor monitor) {
		if (isModelModified) {

			monitor.beginTask(Messages.BSIElementEditor_1, IProgressMonitor.UNKNOWN);
			save(true);
			monitor.done();

			// TODO akoderman we need a way to close (with save dialog) or
			// update editors of objects that have been changed in the database,
			// i.e. by triggers (protection level)
			// // close all other open editors on save (but only the ones
			// without changes):
			IEditorReference[] editorReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
			ArrayList<IEditorReference> closeOthers = new ArrayList<IEditorReference>();
			BSIElementEditorInput myInput = (BSIElementEditorInput) getEditorInput();

			allEditors: for (IEditorReference editorReference : editorReferences) {
				IEditorInput input;
				try {
					if (editorReference.isPinned() || editorReference.isDirty())
						continue allEditors;

					input = editorReference.getEditorInput();
					if (input instanceof BSIElementEditorInput) {
						BSIElementEditorInput bsiInput = (BSIElementEditorInput) input;
						if (!bsiInput.getId().equals(myInput.getId())) {
							closeOthers.add(editorReference);
						}
					}
				} catch (PartInitException e) {
					ExceptionUtil.log(e, Messages.BSIElementEditor_2);
				}
			}

			IEditorReference[] closeArray = (IEditorReference[]) closeOthers.toArray(new IEditorReference[closeOthers.size()]);
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditors(closeArray, true /*
																												 * ask
																												 * save
																												 */);
		}
	}

	private void save(boolean completeRefresh) {
		if (!getIsWriteAllowed()) {
			ExceptionUtil.log(new IllegalStateException(), Messages.BSIElementEditor_3);
			return;
		}
		try {
			// save element, refresh etc:
			BSIElementEditorInput editorinput = (BSIElementEditorInput) getEditorInput();
			CnAElementHome.getInstance().updateEntity(cnAElement);
			isModelModified = false;
			firePropertyChange(IEditorPart.PROP_DIRTY);

			// notify all views of change:
			CnAElementFactory.getModel(cnAElement).childChanged(cnAElement.getParent(), cnAElement);

			// cause complete refresh, necessary for viewers to call getchildren
			// etc.
			if (completeRefresh) {			
				CnAElementFactory.getModel(cnAElement).refreshAllListeners(IBSIModelListener.SOURCE_EDITOR);
			}

		} catch (StaleObjectStateException se) {
			// close editor, loosing changes:
			ExceptionUtil.log(se, Messages.BSIElementEditor_0);
		} catch (Exception e) {
			ExceptionUtil.log(e, Messages.BSIElementEditor_5);
		}
	}

	@Override
	public void doSaveAs() {
		// not supported
	}

	void modelChanged() {
		boolean wasDirty = isDirty();
		isModelModified = true;

		if (!wasDirty)
			firePropertyChange(IEditorPart.PROP_DIRTY);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if (!(input instanceof BSIElementEditorInput))
			throw new PartInitException("invalid input"); //$NON-NLS-1$
		setSite(site);
		setInput(input);
		setPartName(input.getName());
	}

	private void initContent() {
		try {
			
			cnAElement = ((BSIElementEditorInput) getEditorInput()).getCnAElement();
			
			LoadElementForEditor command = new LoadElementForEditor(cnAElement);
			command = ServiceFactory.lookupCommandService().executeCommand(
					command);
			cnAElement = command.getElement();

			Entity entity = cnAElement.getEntity();
			EntityType entityType = HitroUtil.getInstance().getTypeFactory().getEntityType(entity.getEntityType());

			// Enable dirty listener only for writable objects:
			if (getIsWriteAllowed()) {
				// add listener to mark editor as dirty on changes:
				entity.addChangeListener(this.modelListener);
			} else {
				// do not add listener, user will never be offered to save this editor, modify title to show this:
				setPartName(getPartName() + Messages.BSIElementEditor_7);
			}

			 String tagString = Activator.getDefault().getPluginPreferences().getString(PreferenceConstants.HUI_TAGS);
			 String[] tags = split(tagString);
             boolean strict = Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.HUI_TAGS_STRICT);
            
			// samt perspective offers a simple view, only showing properties tagged with "isa":
			if (isSamtPerspective()) {
                tags = new String[] {SAMT_PERSPECTIVE_DEFAULT_TAGS};
                strict = true;
			}
			
			// create view of all properties, read only or read/write:
			huiComposite.createView(entity, getIsWriteAllowed(), true, tags, strict);
			InputHelperFactory.setInputHelpers(entityType, huiComposite);
			huiComposite.resetInitialFocus();
			
			// create in place editor for links to other objects
			// but not for simplified view:
			if (linkMaker != null) {
			    linkMaker.createPartControl(getIsWriteAllowed());
			    linkMaker.setInputElmt(cnAElement);
			}
		} catch (Exception e) {
			ExceptionUtil.log(e, Messages.BSIElementEditor_8);
		}

	}
	
	 /**
     * @param tags
     * @return
     */
    private String[] split(String tags) {
        if (tags == null)
            return new String[] {};
        
        tags.replaceAll("\\s+", "");
        return tags.split(",");
    }

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void setIcon() {
		Image icon = ImageCache.getInstance().getImage(ImageCache.UNKNOWN);
		if(cnAElement!=null) {
			if(cnAElement instanceof Organization) {
				icon = ImageCache.getInstance().getISO27kTypeImage(Organization.TYPE_ID);
			} else if(cnAElement instanceof Group) {
			    // TODO - getChildTypes()[0] might be a problem for more than one type
				icon = ImageCache.getInstance().getISO27kTypeImage(((Group)cnAElement).getChildTypes()[0]);
			} else if(cnAElement instanceof IISO27kElement) {
				icon = ImageCache.getInstance().getISO27kTypeImage(cnAElement.getTypeId());
			} else if(cnAElement instanceof IBSIStrukturElement || cnAElement instanceof IBSIStrukturKategorie) {
				icon = ImageCache.getInstance().getBSITypeImage(cnAElement.getTypeId());
			}
		}
		setTitleImage(icon);
	}

	@Override
	public boolean isDirty() {
		return isModelModified;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	public void setIsWriteAllowed(Boolean isWriteAllowed) {
		this.isWriteAllowed = isWriteAllowed;
	}

	public Boolean getIsWriteAllowed() {
		if(isWriteAllowed==null) {		
			isWriteAllowed = createIsWriteAllowed();
		}

//		// in samt perspective, edit only samttopics:
//		if (isSamtPerspective()) {
//		    if (!cnAElement.getTypeId().equals(SamtTopic.TYPE_ID)) {
//		        return false;
//		    }
//		}
		return isWriteAllowed;	
	}
	
	public Boolean createIsWriteAllowed() {
		isWriteAllowed = CnAElementHome.getInstance().isWriteAllowed(cnAElement);
		return isWriteAllowed;
	}

	@Override
	public void createPartControl(Composite parent) {
		FormLayout formLayout = new FormLayout();
		parent.setLayout(formLayout);
		
		huiComposite = new HitroUIComposite(parent, SWT.NULL, false);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0, 1);
		formData.left = new FormAttachment(0, 1);
		formData.right = new FormAttachment(100, -1);
		if (isSamtPerspective() ) {
		    formData.bottom = new FormAttachment(100, -1);
		} else {
		    formData.bottom = new FormAttachment(66, -1);
		}
		huiComposite.setLayoutData(formData);
		
		if (!isSamtPerspective()) {
		    linkMaker = new LinkMaker(parent);
		    FormData formData2 = new FormData();
		    formData2.top = new FormAttachment(66, 1);
		    formData2.left = new FormAttachment(0,1);
		    formData2.right = new FormAttachment(100, -1);
		    formData2.bottom = new FormAttachment(100, -1);
		    linkMaker.setLayoutData(formData2);
		}
		
		initContent();
		setIcon();
		
//		// register model change listener:
//		changeListener = new EditorModelChangelistener();
//		  CnAElementFactory.getInstance().getLoadedModel().addBSIModelListener(contentProvider);
//	        CnAElementFactory.getInstance().getISO27kModel().addISO27KModelListener(contentProvider);
		
		// if opened the first time, save initialized entity:
		if (isDirty())
			save(false);
	}

	/**
     * @return
     */
    private boolean isSamtPerspective() {
        IPerspectiveDescriptor perspective = getSite().getWorkbenchWindow().getActivePage().getPerspective();
        // do not show linkmaker in SAMT perspective:
        return perspective.getId().equals(SAMT_PERSPECTIVE_ID);
    }

    public boolean isNotAskAndSave() {
		return true;
	}

	@Override
	public void setFocus() {
		// huiComposite.setFocus();
		huiComposite.resetInitialFocus();
	}

	@Override
	public void dispose() {
	    if (linkMaker != null) {
	        linkMaker.dispose();
	    }
		huiComposite.closeView();
		cnAElement.getEntity().removeListener(modelListener);
		EditorRegistry.getInstance().closeEditor(((BSIElementEditorInput) getEditorInput()).getId());
		
//		CnAElementFactory.getInstance().getLoadedModel().removeBSIModelListener(contentProvider);
//        CnAElementFactory.getInstance().getISO27kModel().removeISO27KModelListener(contentProvider);
        
		super.dispose();
	}

}
