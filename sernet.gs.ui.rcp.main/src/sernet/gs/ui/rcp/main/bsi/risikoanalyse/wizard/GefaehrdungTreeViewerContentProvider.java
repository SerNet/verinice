package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;

public class GefaehrdungTreeViewerContentProvider extends ArrayContentProvider
		implements ITreeContentProvider {
	
	private ArrayList<GefaehrdungsUmsetzung> gefaehrdungsUmsetzungen;

	public GefaehrdungTreeViewerContentProvider(ArrayList<GefaehrdungsUmsetzung> list) {
		// super();
		gefaehrdungsUmsetzungen = list;
	}

	public Object[] getChildren(Object parentElement) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasChildren(Object element) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {

		// return super.getElements(inputElement);
		
		if (inputElement instanceof ArrayList) {
			return ((ArrayList<GefaehrdungsUmsetzung>) inputElement).toArray();
		} else {
			return null;
		}
		
		/*
		ArrayList<GefaehrdungsUmsetzung> tmp = (ArrayList<GefaehrdungsUmsetzung>) inputElement;
		
		Object[] objList = new Object[2000];
		int i = 0;
		
		for (GefaehrdungsUmsetzung gefaehrdung : tmp) {
			objList[i] = gefaehrdung;
			i++;
		}
		
		return objList;
		*/
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		super.dispose();
	}
	
	// public void dispose() {
		// TODO Auto-generated method stub
	// }

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
		super.inputChanged(viewer, oldInput, newInput);
	}
	
	// public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
	// }

}
