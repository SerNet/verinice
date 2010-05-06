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
package org.verinice.samt.rcp;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sernet.gs.ui.rcp.main.VeriniceCharset;

/**
 * 
 * @author Daniel Murygin <dm@sernet.de>
 * TODO dm, Externalize Strings
 */
public class SamtPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public static final String CHARSET_SAMT = "org.verinice.samt.rcp.charset";
    
    private ComboFieldEditor encodingFieldEditor;
    
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
        Activator.getDefault().getPreferenceStore().setDefault(CHARSET_SAMT,VeriniceCharset.CHARSET_ISO_8859_15.name());
    }
    
    
    public SamtPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Preferences of self assessments"); 
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        encodingFieldEditor = new ComboFieldEditor(CHARSET_SAMT, "Encoding of Self Assessment CSV File", encodingComboValues, getFieldEditorParent());
        addField(encodingFieldEditor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
    }

}
