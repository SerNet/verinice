package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.hibernate.StaleObjectStateException;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Client;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.Server;
import sernet.gs.ui.rcp.main.bsi.model.SonstIT;
import sernet.gs.ui.rcp.main.bsi.model.TKKategorie;
import sernet.gs.ui.rcp.main.bsi.model.TelefonKomponente;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementById;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadEntityByType;
import sernet.gs.ui.rcp.main.service.crudcommands.RefreshElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.IEntityChangedListener;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyChangedEvent;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.multiselectionlist.IMLPropertyType;
import sernet.hui.swt.widgets.HitroUIComposite;
import sernet.hui.swt.widgets.IInputHelper;
import sernet.snutils.DBException;

/**
 * Editor for all BSI elements with attached HUI entities.
 * 
 * Uses the HUI framework to edit all properties defined
 * in the entity's xml description (SNCA.xml)
 * 
 * @author koderman@sernet.de
 *
 */
public class BSIElementEditor extends EditorPart {
	public static final String EDITOR_ID = "sernet.gs.ui.rcp.main.bsi.editors.bsielementeditor";
	private HitroUIComposite huiComposite;
	private boolean isModelModified = false;
	
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
	
	
	public void doSave(IProgressMonitor monitor) {
		if (isModelModified) {
			monitor.beginTask("Speichern", IProgressMonitor.UNKNOWN);
			save();
			monitor.done();
		}
	}
	
	

	private void save() {
		BSIElementEditorInput editorinput = (BSIElementEditorInput) getEditorInput();
		try {
			CnAElementHome.getInstance().update(cnAElement);
			isModelModified = false;
			firePropertyChange(IEditorPart.PROP_DIRTY);
			
			// notify all views of change:
			CnAElementFactory.getLoadedModel().childChanged(cnAElement.getParent(), cnAElement);
		} catch (StaleObjectStateException se) {
			// close editor, loosing changes:
			ExceptionUtil.log(se, "Fehler beim Speichern.");
		}
		catch (Exception e) {
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
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		if (! (input instanceof BSIElementEditorInput))
			throw new PartInitException("invalid input");
		setSite(site);
		setInput(input);
		setPartName(input.getName());
	}
	
	private void initContent() {
		try {
			cnAElement = ((BSIElementEditorInput)getEditorInput()).getCnAElement();
			RefreshElement<CnATreeElement> command = new RefreshElement<CnATreeElement>(cnAElement);
			command = ServiceFactory.lookupCommandService().executeCommand(
					command);
			cnAElement = command.getElement();
			
			Entity entity = cnAElement.getEntity();
			EntityType entityType = HUITypeFactory.getInstance()
				.getEntityType(entity.getEntityType());
			// add listener to mark editor as dirty on changes:
			entity.addChangeListener(this.modelListener);
			huiComposite.createView(entity, true, true);
			InputHelperFactory.setInputHelpers(entityType, huiComposite);
			huiComposite.resetInitialFocus();
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
	
	@Override
	public void createPartControl(Composite parent) {
		huiComposite = new HitroUIComposite(parent, SWT.NULL, false);
		initContent();
		// if opened the first time, save initialized entity:
		if (isDirty())
			save();
			
	}
	
	public boolean isNotAskAndSave() {
		return true;
	}

	@Override
	public void setFocus() {
		//huiComposite.setFocus();
		huiComposite.resetInitialFocus();
	}
	
	@Override
	public void dispose() {
		CnAElementFactory.getLoadedModel().refreshAllListeners();
		huiComposite.closeView();
		cnAElement.getEntity().removeListener(modelListener);
		EditorRegistry.getInstance().closeEditor(
				( (BSIElementEditorInput)getEditorInput() ).getId()
				);
		super.dispose();
	}

}
