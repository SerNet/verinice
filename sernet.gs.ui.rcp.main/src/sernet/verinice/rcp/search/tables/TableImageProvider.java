/*******************************************************************************
 * Copyright (c) 2015 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.search.tables;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.IControl;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.model.search.VeriniceSearchResultRow;
import sernet.verinice.rcp.search.column.IconColumn;

/**
 * Provides image path for the {@link SearchResultsTableViewer} icon column.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class TableImageProvider {

    private final static Logger LOG = Logger.getLogger(TableImageProvider.class);

    public static Image getImagePath(VeriniceSearchResultRow row) {

        ImageCache imgCache = ImageCache.getInstance();

        // check if custom image is set
        String imagePath = row.getValueFromResultString(IconColumn.ICON_PROPERTY_NAME);
        if (!imagePath.isEmpty()) {
            return imgCache.getCustomImage(imagePath);
        }

        String typeId = row.getParent().getEntityTypeId();
        LOG.debug("seach image for type id: " + typeId);

        if (SamtTopic.TYPE_ID.equals(typeId)) {
            return imgCache.getControlImplementationImage(getSamtTopiOptionStatus(row.getValueFromResultString(SamtTopic.PROP_MATURITY)));
        }

        if (Control.TYPE_ID.equals(typeId)) {
            return imgCache.getControlImplementationImage(retrieveOptionStatus(row.getValueFromResultString(IControl.PROP_IMPL)));
        }

        // retrieve default images
        if (imgCache.isBSITypeElement(typeId)) {
            return imgCache.getBSITypeImage(typeId);
        }

        if (imgCache.isISO27kTypeElement(typeId)) {
            return imgCache.getISO27kTypeImage(typeId);
        }

        return null;
    }

    /**
     * Detects the status of a {@link SamtTopic}, so a matching status icon can
     * be displayed in the search table.
     *
     * The value checks the human readable and translated message from a
     * message.properties file. The status constants comes from {@link IControl}
     * .
     *
     * Since the index also stores the status message in the language the server
     * is running, this will only work if the client is configured with the same
     * language when the search index was created. Otherwise no status will be
     * found.
     *
     * @param status
     *            is the translated message from a messages.properties file.
     * @return A status from IControl.IMP*
     */
    private static String getSamtTopiOptionStatus(String status) {
        if (getTypeIDTranslation("samt_topic_maturity_null").equals(status)) {
            return IControl.IMPLEMENTED_NOTEDITED;
        }

        return IControl.IMPLEMENTED_YES;
    }

    /**
     * Detects the status of a control, so a matching status icon can be
     * displayed in the search table.
     *
     * The value checks the human readable and translated message from a
     * message.properties file. The status constants comes from {@link IControl}
     * .
     *
     * Since the index also stores the status message in the language the server
     * is running, this will only work if the client is configured with the same
     * language when the search index was created. Otherwise no status will be
     * found.
     *
     * @param status
     *            is the translated message from a messages.properties file.
     * @return A status from IControl.IMP*
     */
    private static String retrieveOptionStatus(String status) {
        if (getTypeIDTranslation(IControl.IMPLEMENTED_NO).equals(status)) {
            return IControl.IMPLEMENTED_NO;
        }

        if (getTypeIDTranslation(IControl.IMPLEMENTED_YES).equals(status)) {
            return IControl.IMPLEMENTED_YES;
        }

        if (getTypeIDTranslation(Control.IMPLEMENTED_NOTEDITED).equals(status)) {
            return IControl.IMPLEMENTED_NOTEDITED;
        }

        if (getTypeIDTranslation(IControl.IMPLEMENTED_NA).equals(status)) {
            return IControl.IMPLEMENTED_NA;
        }

        if (getTypeIDTranslation(Control.IMPLEMENTED_PARTLY).equals(status)) {
            return IControl.IMPLEMENTED_PARTLY;
        }

        return ImageCache.UNKNOWN;
    }

    private static String getTypeIDTranslation(String id) {
        return HUITypeFactory.getInstance().getMessage(id);
    }
}
