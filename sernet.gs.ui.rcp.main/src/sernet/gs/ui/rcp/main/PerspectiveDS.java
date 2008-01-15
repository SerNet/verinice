package sernet.gs.ui.rcp.main;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import sernet.gs.ui.rcp.main.bsi.views.BSIMassnahmenView;
import sernet.gs.ui.rcp.main.bsi.views.BrowserView;
import sernet.gs.ui.rcp.main.bsi.views.DSModelView;

public class PerspectiveDS implements IPerspectiveFactory {
	public static final String ID = "sernet.gs.ui.rcp.main.dsperspective";
	
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		
		//layout.addStandaloneView(NavigationView.ID,  false, IPageLayout.LEFT, 0.25f, editorArea);
		layout.addView(DSModelView.ID,  IPageLayout.LEFT, 0.25f, editorArea);
		
		layout.getViewLayout(DSModelView.ID).setCloseable(true);
		layout.addPerspectiveShortcut(ID);

//		PlatformUI.getWorkbench().showPerspective(YOUR_PERSPECTIVE_ID, 
//				PlatformUI.getWorkbench().getActiveWorkbenchWindow());


	}
	
	
}
