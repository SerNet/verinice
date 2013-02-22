/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sernet.gs.service.VeriniceCharset;
import sernet.gs.ui.rcp.main.Activator;

/**
 * Main preference page for CnA Tool Settings.
 * 
 * @author akoderman[at]sernet[dot]de
 * 
 */
public class GeneralSettingsPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {


	

	private static final String[][] ENCODING_COMBO_VALUES = new String[][]{
        new String[]{VeriniceCharset.CHARSET_UTF_8.displayName(),VeriniceCharset.CHARSET_UTF_8.name()},
        new String[]{VeriniceCharset.CHARSET_ISO_8859_15.displayName(),VeriniceCharset.CHARSET_ISO_8859_15.name()},
        new String[]{VeriniceCharset.CHARSET_WINDOWS_1252.displayName(),VeriniceCharset.CHARSET_WINDOWS_1252.name()}       
	};
	
	
    private static final String[][] THUMBNAIL_SIZE_VALUES = new String[][]{
        new String[]{Messages.getString("GeneralSettingsPage.5"),"0"}, //$NON-NLS-1$ //$NON-NLS-2$
        new String[]{Messages.getString("GeneralSettingsPage.8"),"20"}, //$NON-NLS-1$ //$NON-NLS-2$
        new String[]{Messages.getString("GeneralSettingsPage.10"),"50"}, //$NON-NLS-1$ //$NON-NLS-2$
        new String[]{Messages.getString("GeneralSettingsPage.12"),"80"}, //$NON-NLS-1$ //$NON-NLS-2$
        new String[]{Messages.getString("GeneralSettingsPage.14"),"110"}, //$NON-NLS-1$ //$NON-NLS-2$
        new String[]{Messages.getString("GeneralSettingsPage.16"),"150"} //$NON-NLS-1$ //$NON-NLS-2$
    };

	public GeneralSettingsPage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.getString("GeneralSettingsPage.0")); //$NON-NLS-1$
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
	    BooleanFieldEditor derbyWarning = new BooleanFieldEditor(PreferenceConstants.FIRSTSTART, Messages.getString("GeneralSettingsPage.1"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(derbyWarning);
		
		BooleanFieldEditor linkToEditor = new BooleanFieldEditor(PreferenceConstants.LINK_TO_EDITOR, Messages.getString("GeneralSettingsPage.4"),  //$NON-NLS-1$
                getFieldEditorParent());
        addField(linkToEditor);

        BooleanFieldEditor errorPopups = new BooleanFieldEditor(PreferenceConstants.ERRORPOPUPS, Messages.getString("GeneralSettingsPage.2"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(errorPopups);

		BooleanFieldEditor inputHelperHints = new BooleanFieldEditor(PreferenceConstants.INPUTHINTS, Messages.getString("GeneralSettingsPage.3"), getFieldEditorParent()); //$NON-NLS-1$
		addField(inputHelperHints);

		BooleanFieldEditor showAlienDecorator = new BooleanFieldEditor(PreferenceConstants.SHOW_ALIEN_DECORATOR, Messages.getString("GeneralSettingsPage.ShowAlienDecorator"), getFieldEditorParent()); //$NON-NLS-1$
		addField(showAlienDecorator);
		
		BooleanFieldEditor showDBIDDecorator = new BooleanFieldEditor(PreferenceConstants.SHOW_DBID_DECORATOR, Messages.getString("GeneralSettingsPage.ShowDBIDDecorator"), getFieldEditorParent()); //$NON-NLS-1$
		addField(showDBIDDecorator);
		
		BooleanFieldEditor useValidationGuiHints = new BooleanFieldEditor(PreferenceConstants.USE_VALIDATION_GUI_HINTS, Messages.getString("GeneralSettingsPage.UseValidationGuiHints"), getFieldEditorParent()); //$NON-NLS-1$
		addField(useValidationGuiHints);
		
		BooleanFieldEditor useAutomaticValidation = new BooleanFieldEditor(PreferenceConstants.USE_AUTOMATIC_VALIDATION, Messages.getString("GeneralSettingsPage.UseValidationAlways"), getFieldEditorParent());
		addField(useAutomaticValidation);
		
		ComboFieldEditor encodingFieldEditor = new ComboFieldEditor(PreferenceConstants.CHARSET_CATALOG, 
		        Messages.getString("GeneralSettingsPage.6"),  //$NON-NLS-1$
				ENCODING_COMBO_VALUES, 
				getFieldEditorParent());
        addField(encodingFieldEditor);
        
        ComboFieldEditor thumbnailSizeEditor = new ComboFieldEditor(PreferenceConstants.THUMBNAIL_SIZE, 
                Messages.getString("GeneralSettingsPage.7"),  //$NON-NLS-1$
                THUMBNAIL_SIZE_VALUES, 
                getFieldEditorParent());
        addField(thumbnailSizeEditor);
        
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getProperty().equals(FieldEditor.VALUE)) {
			checkState();
		}
	}

	@Override
	protected void checkState() {
		super.checkState();
		if (!isValid()) {
			return;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}
