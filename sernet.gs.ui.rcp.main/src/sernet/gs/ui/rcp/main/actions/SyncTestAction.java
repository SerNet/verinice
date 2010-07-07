package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

import de.sernet.sync.sync.ISyncWS;
import de.sernet.sync.sync.SyncRequest;
import de.sernet.sync.sync.SyncWS; //import de.sernet.sync.sync.SyncWSService;

import sernet.gs.ui.rcp.main.Activator;
import sernet.hui.common.VeriniceContext;

//import sernet.verinice.sync.client.Simple;

public class SyncTestAction extends Action {

	private IWorkbenchWindow window;

	public SyncTestAction(IWorkbenchWindow window) {
		this.window = window;
		setText("Synctest");
	}

	@Override
	public void run() {

		Activator.inheritVeriniceContextState();
		ISyncWS syncService = (ISyncWS) VeriniceContext
				.get(VeriniceContext.SYNC_SERVICE);

		SyncRequest syncRequest = new SyncRequest();
		syncRequest.setSyncData(null);
		syncRequest.setSyncMapping(null);

		try {
			syncService.sync(syncRequest);
		} catch (Exception e) {
			System.out.println("#### #### ####");
			e.printStackTrace();
		}
	}

}
