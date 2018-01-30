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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import sernet.gs.service.VeriniceCharset;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.verinice.interfaces.IVeriniceConstants;
import sernet.verinice.interfaces.report.IReportService;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
    
    private static final String STANDALONE_UPDATENEWS_URL_DEFAULT = "https://update.verinice.org/pub/verinice/news.json";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
    public void initializeDefaultPreferences() {
	    final int defaultThumbnailSize = 50;
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		store.setDefault(PreferenceConstants.GSACCESS,
				PreferenceConstants.GSACCESS_ZIP);

		store.setDefault(PreferenceConstants.BSIDIR, CnAWorkspace.getInstance().getWorkdir());

		store.setDefault(PreferenceConstants.ERRORPOPUPS, true);
		store.setDefault(PreferenceConstants.FIRSTSTART, true);
		store.setDefault(PreferenceConstants.INPUTHINTS, true);
		store.setDefault(PreferenceConstants.SHOW_ALIEN_DECORATOR, true);

		store.setDefault(PreferenceConstants.DB_DRIVER,
				PreferenceConstants.DB_DRIVER_DERBY);
		
		store.setDefault(PreferenceConstants.DB_DIALECT,
				PreferenceConstants.DB_DIALECT_DERBY);
		
		final String derbyUrl = PreferenceConstants.DB_URL_DERBY.replace("%s",CnAWorkspace //$NON-NLS-1$
				.getInstance().getWorkdir().replaceAll("\\\\", "/") );
		store.setDefault(PreferenceConstants.DB_URL,
				derbyUrl);
		
		store.setDefault(PreferenceConstants.DB_USER, ""); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.DB_PASS, ""); //$NON-NLS-1$

		store.setDefault(PreferenceConstants.GS_DB_URL, PreferenceConstants.GS_DB_URL_LOCALHOST); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.GS_DB_USER, PreferenceConstants.GS_DB_USER_DEFAULT); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.GS_DB_PASS, ""); //$NON-NLS-1$
		
		store.setDefault(PreferenceConstants.OPERATION_MODE, PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER);
		
		store.setDefault(PreferenceConstants.VNSERVER_URI, PreferenceConstants.VNSERVER_URI_DEFAULT);
		
		store.setDefault(PreferenceConstants.HUI_TAGS, PreferenceConstants.HUI_TAGS_ALL);
		store.setDefault(PreferenceConstants.HUI_TAGS_STRICT, false);
		store.setDefault(PreferenceConstants.SHOW_DBID_DECORATOR, false);
		
		store.setDefault(PreferenceConstants.SHOW_GSMISM_DECORATOR, false);
		store.setDefault(PreferenceConstants.SHOW_RISK_ANALYSIS_DECORATOR, false);
		
		store.setDefault(PreferenceConstants.LINK_TO_EDITOR, true);
		
		store.setDefault(PreferenceConstants.SHOW_LINK_MAKER_IN_EDITOR, true);
		
		store.setDefault(PreferenceConstants.THUMBNAIL_SIZE, defaultThumbnailSize);
		
		store.setDefault(PreferenceConstants.RESTART, false);

		store.setDefault(PreferenceConstants.REPORT_LOCAL_TEMPLATE_DIRECTORY, IReportService.VERINICE_REPORTS_LOCAL);
		
		store.setDefault(PreferenceConstants.INHERIT_SPECIAL_GROUP_ICON, true);
		
		store.setDefault(PreferenceConstants.CUT_INHERIT_PERMISSIONS, false);
		
		store.setDefault(PreferenceConstants.COPY_ATTACHMENTS_WITH_OBJECTS, false);
		
		store.setDefault(PreferenceConstants.ENABLE_RELEASE_PROCESS, false);

		store.setDefault(PreferenceConstants.SEARCH_INDEX_ON_STARTUP, true);
		store.setDefault(PreferenceConstants.SEARCH_DISABLE, false);
		store.setDefault(PreferenceConstants.SEARCH_SORT_COLUMN_BY_SNCA, PreferenceConstants.SEARCH_SORT_COLUMN_BY_SNCA);
        store.setDefault(PreferenceConstants.SEARCH_CSV_EXPORT_SEPERATOR, SearchPreferencePage.SEMICOLON);     
        store.setDefault(PreferenceConstants.SEARCH_CSV_EXPORT_ENCODING, VeriniceCharset.CHARSET_WINDOWS_1250.name());
        
        store.setDefault(PreferenceConstants.DEFAULT_FOLDER_ADDFILE, 
                System.getProperty(IVeriniceConstants.USER_HOME));
        
        store.setDefault(PreferenceConstants.EXPORT_RISK_ANALYSIS, true);
        
        store.setDefault(PreferenceConstants.SHOW_UPDATE_NEWS_DIALOG, false);
        
        store.setDefault(PreferenceConstants.STANDALONE_UPDATENEWS_URL, STANDALONE_UPDATENEWS_URL_DEFAULT);
        store.setDefault(PreferenceConstants.INFO_CONTROLS_TRANSFORMED_TO_MODERNIZED_GS, true);
	}
}
