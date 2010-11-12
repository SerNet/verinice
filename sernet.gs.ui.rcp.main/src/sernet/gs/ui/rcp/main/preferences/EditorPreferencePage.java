package sernet.gs.ui.rcp.main.preferences;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.bsi.dialogs.Messages;
import sernet.hui.common.connect.HitroUtil;

public class EditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage  {

    private CheckboxTableViewer viewer;

    public EditorPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription(sernet.gs.ui.rcp.main.preferences.Messages.getString("EditorPreferencePage.0")); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        BooleanFieldEditor booleanFieldEditor = new BooleanFieldEditor(PreferenceConstants.HUI_TAGS_STRICT, sernet.gs.ui.rcp.main.preferences.Messages.getString("EditorPreferencePage.1"), getFieldEditorParent()); //$NON-NLS-1$
        addField(booleanFieldEditor);
        
        createTagList();
        
    }

    /**
     * 
     */
    private void createTagList() {
        viewer = CheckboxTableViewer.newCheckList((Composite)getControl(), SWT.BORDER|SWT.V_SCROLL);
        Table table = viewer.getTable();
        table.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        table.setHeaderVisible(false);
        table.setLinesVisible(false);
        
        TableColumn checkboxColumn = new TableColumn(table, SWT.LEFT);
        checkboxColumn.setText(""); //$NON-NLS-1$
        checkboxColumn.setWidth(35);

        TableColumn tagColumn = new TableColumn(table, SWT.LEFT);
        tagColumn.setText(sernet.gs.ui.rcp.main.preferences.Messages.getString("EditorPreferencePage.2")); //$NON-NLS-1$
        tagColumn.setWidth(100);
        
        viewer.setContentProvider(new ArrayContentProvider());

        viewer.setLabelProvider(new ITableLabelProvider() {
            @Override
            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }

            @Override
            public String getColumnText(Object element, int columnIndex) {
                if (columnIndex ==1) {
                    return (String) element;
                }
                return null;
            }

            public void addListener(ILabelProviderListener listener) {
            }

            public void dispose() {
            }

            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            public void removeListener(ILabelProviderListener listener) {
            }
        });
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        // get checked objects, cast to string:
        List<Object> tagList = Arrays.asList(viewer.getCheckedElements());
        String tagString = join(tagList);
        Preferences prefs = Activator.getDefault().getPluginPreferences();
        prefs.setValue(PreferenceConstants.HUI_TAGS, tagString);
        return super.performOk();
    }

    private String join(List tags) {
        Iterator iter;
        if (tags == null || (!(iter = tags.iterator()).hasNext()))
            return ""; //$NON-NLS-1$

        StringBuilder oBuilder = new StringBuilder(String.valueOf(iter.next()));
        while (iter.hasNext()) {
            oBuilder.append(",").append(iter.next()); //$NON-NLS-1$
        }
        return oBuilder.toString();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#initialize()
     */
    @Override
    protected void initialize() {
        super.initialize();
        Activator.inheritVeriniceContextState();
        Set<String> allTags = HitroUtil.getInstance().getTypeFactory().getAllTags();
        viewer.setInput(allTags.toArray());
        
        String prefTags = Activator.getDefault().getPluginPreferences().getString(PreferenceConstants.HUI_TAGS);
        String[] prefTagsArr = split(prefTags);
        viewer.setCheckedElements(prefTagsArr);
    }

    /**
     * @param tags
     * @return
     */
    private String[] split(String tags) {
        if (tags == null)
            return new String[] {};
        
        tags.replaceAll("\\s+", ""); //$NON-NLS-1$ //$NON-NLS-2$
        return tags.split(","); //$NON-NLS-1$
    }

  

}


