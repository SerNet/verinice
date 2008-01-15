package sernet.gs.ui.rcp.main.bsi.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.bsi.views.ThreadSafeViewerUpdate;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.IEntityElement;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyType;

public class ChoosePropertiesContentProvider implements ITreeContentProvider {
	
	private CheckboxTreeViewer viewer;

	public ChoosePropertiesContentProvider(CheckboxTreeViewer viewer) {
		this.viewer = viewer;
	}

	public Object[] getChildren(Object element) {
		if (element instanceof EntityType) {
			EntityType type = (EntityType) element;
			ArrayList<IEntityElement> result = new ArrayList<IEntityElement>();
			result.addAll(type.getPropertyTypes());
			result.addAll(type.getPropertyGroups());
			return (IEntityElement[]) result.toArray(new IEntityElement[result.size()]);
		}
		
		if (element instanceof PropertyGroup) {
			PropertyGroup group = (PropertyGroup) element;
			List<PropertyType> types = group.getPropertyTypes();
			return (PropertyType[]) types.toArray(new PropertyType[types.size()]);
		}
		
		return null;
		
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof EntityType ) {
			EntityType type = (EntityType) element;
			return type.getPropertyTypes().size() > 0
			|| type.getPropertyGroups().size() > 0;
		}
		
		if (element instanceof PropertyGroup) {
			PropertyGroup group = (PropertyGroup) element;
			return group.getPropertyTypes().size() > 0;
		}
		
		return false;
	}

	public Object[] getElements(Object inputElement) {
		Collection list = (Collection) inputElement;
		return (EntityType[]) list.toArray(new EntityType[list.size()]);
	}

	public void dispose() {

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (CheckboxTreeViewer) viewer;
		(new ThreadSafeViewerUpdate((TreeViewer) viewer)).refresh();
	}
	
	

}
