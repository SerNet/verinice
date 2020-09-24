/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
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
 * Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.connect.HitroUtil;
import sernet.snutils.TagHelper;

/**
 * This class provides methods for convenient read-only access to the
 * preferences. The class does not yet provide access to all the preferences. It
 * can and should be extended with missing methods.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public final class Preferences {

    private static final Logger LOG = Logger.getLogger(Preferences.class);

    private Preferences() {
        super();
    }

    public static boolean isStandalone() {
        return PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER
                .equals(getPreferenceStore().getString(PreferenceConstants.OPERATION_MODE));
    }

    public static boolean isServerMode() {
        return PreferenceConstants.OPERATION_MODE_REMOTE_SERVER
                .equals(getPreferenceStore().getString(PreferenceConstants.OPERATION_MODE));
    }

    public static boolean isBpCatalogLoadedFromZipFile() {
        return getPreferenceStore().getString(PreferenceConstants.GSACCESS)
                .equals(PreferenceConstants.GSACCESS_ZIP);
    }

    public static String getBpCatalogFilePath() {
        String bpCatalogFilePath = null;
        if (isBpCatalogLoadedFromZipFile()) {
            bpCatalogFilePath = getPreferenceStore().getString(PreferenceConstants.BSIZIPFILE);
        } else {
            bpCatalogFilePath = getPreferenceStore().getString(PreferenceConstants.BSIDIR);
            try {
                bpCatalogFilePath = (new File(bpCatalogFilePath)).toURI().toURL().toString();
            } catch (MalformedURLException e) {
                LOG.error("Error while getting base protection catalog file path.", e);
            }
        }
        return bpCatalogFilePath;
    }

    public static boolean isModelSafeguardsActive() {
        return getPreferenceStore().getBoolean(PreferenceConstants.BP_MODEL_SAFEGUARDS);
    }

    public static boolean isModelDummySafeguardsActive() {
        return getPreferenceStore().getBoolean(PreferenceConstants.BP_MODEL_DUMMY_SAFEGUARDS);
    }

    public static String getPrivacyCatalogFilePath() {
        return getPreferenceStore().getString(PreferenceConstants.DSZIPFILE);
    }

    public static String getServerUrl() {
        return getPreferenceStore().getString(PreferenceConstants.VNSERVER_URI);
    }

    private static IPreferenceStore getPreferenceStore() {
        return Activator.getDefault().getPreferenceStore();
    }

    public static final String[] getEditorTags() {
        String tagString = getPreferenceStore().getString(PreferenceConstants.HUI_TAGS);
        String[] tags = null;
        if (PreferenceConstants.HUI_TAGS_ALL.equals(tagString)) {
            Set<String> allTagsSet = HitroUtil.getInstance().getTypeFactory().getAllTags();
            Collection<String> tagsExcludedByDefault = TagHelper.getTags(
                    getPreferenceStore().getString(PreferenceConstants.HUI_TAGS_DEFAULT_EXCLUDED));
            tags = allTagsSet.stream().filter(tag -> !tagsExcludedByDefault.contains(tag))
                    .toArray(String[]::new);
        } else {
            tags = TagHelper.getTags(tagString).stream().toArray(String[]::new);
        }
        return tags;
    }

}
