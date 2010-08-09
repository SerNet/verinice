package sernet.gs.ui.rcp.main.bsi.wizards;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.sync.commands.SyncCommand;
import sernet.verinice.interfaces.CommandException;
import de.sernet.sync.data.SyncData;
import de.sernet.sync.mapping.SyncMapping;
import de.sernet.sync.sync.SyncRequest;

public class ImportCSVWizard extends Wizard {
	private EntitySelectionPage entityPage;
	private PropertiesSelectionPage propertyPage;

	public ImportCSVWizard() {
		super();
		propertyPage = new PropertiesSelectionPage("Select properties");
		entityPage = new EntitySelectionPage("Select entity");
	}

	@Override
	public boolean performFinish() {
		Activator.inheritVeriniceContextState();

		SyncRequest sr = createSyncRequest();

		SyncCommand command = new SyncCommand(sr);
		try {
			command = ServiceFactory.lookupCommandService().executeCommand(
					command);

		} catch (CommandException e) {
			throw new IllegalStateException(e);
		}

		return true;
	}

	public void addPages() {
		addPage(entityPage);
		addPage(propertyPage);
	}

	public IWizardPage getNextPage(IWizardPage page) {
		if (page == entityPage) {
			propertyPage.setEntityName(entityPage.getEntityName());
			propertyPage.setCSVDatei(entityPage.getCSVDatei());
			propertyPage.fillTable();
		}
		return super.getNextPage(page);
	}

	private SyncRequest createSyncRequest() {
		SyncRequest sr = new SyncRequest();
		sr.setInsert(entityPage.getInsertState());
		sr.setUpdate(entityPage.getUpdateState());
		sr.setDelete(entityPage.getDeleteState());
		sr.setSourceId(entityPage.getSourceId());
		
		SyncData sd = new SyncData();
		sr.setSyncData(sd);
		
		SyncMapping sm = new SyncMapping();
		sr.setSyncMapping(sm);
		
		
		
		// TODO: Reimplement the generation of the content
		/*
		 * xmlDoc.mapping(propertyPage.getEntityName(),
		 * entityPage.getEntityNameId(), propertyPage.getPropertyTable());
		 * xmlDoc.data(propertyPage.getEntityName(),
		 * propertyPage.getInhaltDerDatei(), propertyPage.getPropertyColumns());
		 * this.syncRequestXML = xmlDoc.getSyncRequestXML();
		 */

		return sr;
	}
}
