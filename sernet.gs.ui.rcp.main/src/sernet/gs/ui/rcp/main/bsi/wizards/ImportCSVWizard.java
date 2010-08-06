package sernet.gs.ui.rcp.main.bsi.wizards;

import java.io.File;

import javax.xml.bind.JAXB;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.sync.commands.SyncCommand;
import sernet.verinice.interfaces.CommandException;

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

		SyncCommand command = new SyncCommand(entityPage.getSourceId(),
				entityPage.getInsertState(), entityPage.getUpdateState(),
				entityPage.getDeleteState(), sr.getSyncData(), sr
						.getSyncMapping());
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
		// TODO rschuster: Needs to be implemented. 
		throw new UnsupportedOperationException();
		/*
		 * xmlDoc.syncRequest(entityPage.getInsertState(),
		 * entityPage.getUpdateState(), entityPage.getDeleteState(),
		 * entityPage.getSourceId());
		 * xmlDoc.mapping(propertyPage.getEntityName(),
		 * entityPage.getEntityNameId(), propertyPage.getPropertyTable());
		 * xmlDoc.data(propertyPage.getEntityName(),
		 * propertyPage.getInhaltDerDatei(), propertyPage.getPropertyColumns());
		 * this.syncRequestXML = xmlDoc.getSyncRequestXML();
		 */

		// return null;
	}
}
