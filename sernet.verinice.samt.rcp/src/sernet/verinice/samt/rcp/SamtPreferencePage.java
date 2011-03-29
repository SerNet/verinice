/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.samt.rcp;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sernet.gs.service.VeriniceCharset;

/**
 * Preference page to edit self-assessment (SAMT) preferences
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
@SuppressWarnings("restriction")
public class SamtPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public static final String CHARSET_SAMT = "sernet.verinice.samt.rcp.charset"; //$NON-NLS-1$
    
    public static final String CATALOG_FILENAME = "sernet.verinice.samt.rcp.catalogFilename"; //$NON-NLS-1$
    
    public static final String EXPAND_ISA = "sernet.verinice.samt.rcp.expandIsa"; //$NON-NLS-1$
    
    public static final String ISA_RESULTS = "sernet.verinice.samt.rcp.isaResult"; //$NON-NLS-1$
    
    public static final String INFO_CONTROLS_LINKED = "info_controls_linked"; //$NON-NLS-1$
    
    
    private ComboFieldEditor encodingFieldEditor;
    private StringFieldEditor catalogFileName;
    private BooleanFieldEditor expandIsa;
	private BooleanFieldEditor showIsaResults;
    private BooleanFieldEditor infoControlsLinked;
    
    private static final String[][] encodingComboValues = new String[][]{
            new String[]{VeriniceCharset.CHARSET_UTF_8.displayName(),VeriniceCharset.CHARSET_UTF_8.name()},
            new String[]{VeriniceCharset.CHARSET_ISO_8859_15.displayName(),VeriniceCharset.CHARSET_ISO_8859_15.name()},
            new String[]{VeriniceCharset.CHARSET_WINDOWS_1252.displayName(),VeriniceCharset.CHARSET_WINDOWS_1252.name()}
            
    };
    
    /**
     * Sets the default preferences values
     * Call this method in BundleActivator.start(..)
     */
    public static void setDefaults() {
        // set default charset for self assessment catalog
        Activator.getDefault().getPreferenceStore().setDefault(CHARSET_SAMT,VeriniceCharset.CHARSET_UTF_8.name());
        Activator.getDefault().getPreferenceStore().setDefault(CATALOG_FILENAME,Messages.SamtWorkspace_0);        
        Activator.getDefault().getPreferenceStore().setDefault(EXPAND_ISA,true);
        Activator.getDefault().getPreferenceStore().setDefault(ISA_RESULTS,false);
        Activator.getDefault().getPreferenceStore().setDefault(INFO_CONTROLS_LINKED,true);
        
    }
    
    
    public SamtPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription(Messages.SamtPreferencePage_1); 
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        encodingFieldEditor = new ComboFieldEditor(CHARSET_SAMT, Messages.SamtPreferencePage_2, encodingComboValues, getFieldEditorParent());
        addField(encodingFieldEditor);
        
        catalogFileName = new StringFieldEditor(CATALOG_FILENAME, Messages.SamtPreferencePage_5, getFieldEditorParent());
        addField(catalogFileName);
        
        expandIsa = new BooleanFieldEditor(EXPAND_ISA, Messages.SamtPreferencePage_3, getFieldEditorParent());
        addField(expandIsa);
        
        showIsaResults = new BooleanFieldEditor(ISA_RESULTS, Messages.SamtPreferencePage_0, getFieldEditorParent());
		addField(showIsaResults);
		
		infoControlsLinked = new BooleanFieldEditor(INFO_CONTROLS_LINKED, Messages.SamtPreferencePage_4, getFieldEditorParent());
        addField(infoControlsLinked);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
    }

}
