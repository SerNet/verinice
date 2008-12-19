package sernet.gs.ui.rcp.main;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetView;

import sernet.gs.ui.rcp.main.bsi.views.BSIMassnahmenView;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.bsi.views.BrowserView;

public class Perspective implements IPerspectiveFactory {
	public static final String ID = "sernet.gs.ui.rcp.main.perspective";

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		
		//layout.addStandaloneView(NavigationView.ID,  false, IPageLayout.LEFT, 0.25f, editorArea);
		layout.addView(BSIMassnahmenView.ID,  IPageLayout.LEFT, 0.3f, editorArea);
		
		layout.addView(BsiModelView.ID,  IPageLayout.LEFT, 0.4f, editorArea);
		
		IFolderLayout folder = layout.createFolder("messages", 
				IPageLayout.BOTTOM, 0.5f, editorArea);
		folder.addPlaceholder(BrowserView.ID + ":*");
		folder.addView(BrowserView.ID);
		
		layout.getViewLayout(BSIMassnahmenView.ID).setCloseable(true);
		layout.getViewLayout(BrowserView.ID).setCloseable(true);
	}
	
	
}
