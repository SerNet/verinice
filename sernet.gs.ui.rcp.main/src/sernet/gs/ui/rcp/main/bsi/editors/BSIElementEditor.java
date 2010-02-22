/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.hibernate.StaleObjectStateException;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HitroUtil;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadElementForEditor;
import sernet.gs.ui.rcp.main.service.crudcommands.RefreshElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.IEntityChangedListener;
import sernet.hui.common.connect.PropertyChangedEvent;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.multiselectionlist.IMLPropertyType;
import sernet.hui.swt.widgets.HitroUIComposite;
import sernet.verinice.iso27k.model.IISO27kElement;

/**
 * Editor for all BSI elements with attached HUI entities.
 * 
 * Uses the HUI framework to edit all properties defined in the entity's xml
 * description (SNCA.xml)
 * 
 * @author koderman@sernet.de
 * 
 */
public class BSIElementEditor extends EditorPart {
	public static final String EDITOR_ID = "sernet.gs.ui.rcp.main.bsi.editors.bsielementeditor";
	private HitroUIComposite huiComposite;
	private boolean isModelModified = false;
	private Boolean isWriteAllowed = null;
	
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

			monitor.beginTask("Speichern", IProgressMonitor.UNKNOWN);
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
					ExceptionUtil.log(e, "Fehler beim Schließen des Editors.");
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
			ExceptionUtil.log(new IllegalStateException(), "Keine Schreibrechte auf dem gegebenen Element.");
			return;
		}
		try {
			// save element, refresh etc:
			BSIElementEditorInput editorinput = (BSIElementEditorInput) getEditorInput();
			CnAElementHome.getInstance().update(cnAElement);
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
			ExceptionUtil.log(se, "Fehler beim Speichern.");
		} catch (Exception e) {
			ExceptionUtil.log(e, "Fehler beim Speichern.");
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
			throw new PartInitException("invalid input");
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
				setPartName(getPartName() + " (SCHREIBGESCHÜTZT)");
			}

			// create view of all properties, read only or read/write:
			huiComposite.createView(entity, getIsWriteAllowed(), true);
			InputHelperFactory.setInputHelpers(entityType, huiComposite);
			huiComposite.resetInitialFocus();
			
			// create in place editor for links to other objects:
			linkMaker.createPartControl(getIsWriteAllowed());
			linkMaker.setInputElmt(cnAElement);
		} catch (Exception e) {
			ExceptionUtil.log(e, "Konnte BSI Element Editor nicht öffnen");
		}

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
		formData.bottom = new FormAttachment(66, -1);
		huiComposite.setLayoutData(formData);
		
		linkMaker = new LinkMaker(parent);
		FormData formData2 = new FormData();
		formData2.top = new FormAttachment(66, 1);
		formData2.left = new FormAttachment(0,1);
		formData2.right = new FormAttachment(100, -1);
		formData2.bottom = new FormAttachment(100, -1);
		linkMaker.setLayoutData(formData2);
		
		initContent();
		// if opened the first time, save initialized entity:
		if (isDirty())
			save(false);

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
		linkMaker.dispose();
		huiComposite.closeView();
		cnAElement.getEntity().removeListener(modelListener);
		EditorRegistry.getInstance().closeEditor(((BSIElementEditorInput) getEditorInput()).getId());
		super.dispose();
	}

}
