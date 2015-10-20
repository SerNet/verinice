/**
 *
 */
package sernet.gs.ui.rcp.gsimport;

import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.common.model.PlaceHolder;

/**
 * @author shagedorn
 *
 */
public class GsImportMappingContentProvider implements IStructuredContentProvider {

    GSImportMappingView view;

    TableViewer viewer;

    public GsImportMappingContentProvider(GSImportMappingView view) {
        this.view = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose() {
        // empty
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(Viewer v, Object arg1, Object arg2) {
        this.viewer = (TableViewer)v;
        this.viewer.refresh();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof PlaceHolder) {
            return new Object[] {inputElement};
        }
        if(inputElement instanceof Map) {
            Map<String, String> map = (Map<String, String>)inputElement;
            Object [] content = new Object[map.size()];
            Set<String> keys = map.keySet();
            String[] keyArray = keys.toArray(new String[keys.size()]);
            for(int i = 0; i < keyArray.length; i++) {
                Object[] entry = new Object[2];
                entry[0] = keyArray[i];
                entry[1] = map.get(keyArray[i]);
                content[i] = entry;
            }
            return content;
        }

        return new Object[] {};
    }

}
