/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.linktable;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.rcp.linktable.ui.CsvExportDialog;
import sernet.verinice.rcp.linktable.ui.LinkTableComposite;
import sernet.verinice.rcp.linktable.ui.combo.LinkTableOperationType;
import sernet.verinice.service.csv.ICsvExport;
import sernet.verinice.service.linktable.CnaLinkPropertyConstants;
import sernet.verinice.service.linktable.ColumnPathParseException;
import sernet.verinice.service.linktable.ColumnPathParser;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTable;
import sernet.verinice.service.model.HUIObjectModelLoader;
import sernet.verinice.service.model.IObjectModelService;
import sernet.verinice.service.model.ObjectModelValidationException;

/**
 * Util class for {@link LinkTableComposite}
 * 
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class LinkTableUtil {

    private static final Logger LOG = Logger.getLogger(LinkTableUtil.class);
    private static HashMap<String, String> vltExtensions = null;
    private static HashMap<String, String> csvExtensions = null;
    private static CsvExportDialog csvDialog;
    private static IObjectModelService loader;

    private static final String ALIAS_DELIMITER = " AS ";
    
    static {    
        if (vltExtensions == null) {
            vltExtensions = new HashMap<>();
            vltExtensions.put("*" + VeriniceLinkTable.VLT, "verinice link table (.vlt)"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (csvExtensions == null) {
            csvExtensions = new HashMap<>();
            csvExtensions.put("*" + ICsvExport.CSV_FILE_SUFFIX, "CSV table (.csv)"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private LinkTableUtil() {
        // to prevent instantiation
    }

    /**
     * 
     * @param shell
     *            - the shell for the {@link FileDialog}
     * @param text
     *            - the header for the {@link FileDialog}
     * @param defaultFolderPreference
     *            - the id of the defaultFolderpreferences to be updated for the
     *            default directory
     * @param filterExtensions
     *            - the possible extensions to filter in the {@link FileDialog}
     * @param style
     *            - SWT.OPEN or SWT.SAVE
     * @param defaultFileName
     *            - the default filename to be used, just considered if the
     *            style is SWT.SAVE
     * @return the absolute filepath to the chosen location
     */
    public static String createFilePath(Shell shell, String text, String defaultFolderPreference,
            Map<String, String> filterExtensions, int style, String defaultFileName) {
        FileDialog dialog = new FileDialog(shell, style);

        String extension = filterExtensions.keySet().iterator().next().substring(1);
        dialog.setText(text);
        dialog.setFilterPath(getDirectory(defaultFolderPreference));

        String filename;
        if (defaultFileName != null && style == SWT.SAVE) {
            filename = defaultFileName;
        } else {
            filename = Messages.LinkTableUtil_1 + extension;
        }
        filename = addExtensionIfMissing(extension, filename);
        dialog.setFileName(filename);
        ArrayList<String> extensions = new ArrayList<>(filterExtensions.keySet());
        extensions.add("*.*"); //$NON-NLS-1$
        dialog.setFilterExtensions(extensions.toArray(new String[] {})); // $NON-NLS-1$
        ArrayList<String> extensionNames = new ArrayList<>(filterExtensions.values());
        extensionNames.add(Messages.VeriniceLinkTableUtil_1);
        dialog.setFilterNames(extensionNames.toArray(new String[] {}));
        dialog.setFilterIndex(0);
        dialog.setOverwrite(true);

        String filePath = dialog.open();
        if (filePath != null) {

            File file = new File(filePath);
            String dir = file.getParent();
            Activator.getDefault().getPreferenceStore().setValue(defaultFolderPreference, dir);
        }
        filePath = addExtensionIfMissing(extension, filePath);
        return filePath;
    }

    private static String addExtensionIfMissing(String extension, String filePath) {
        if (filePath != null && !filePath.endsWith(extension)) {
            return filePath+= extension;
        }
        return filePath;
    }

    /**
     * 
     * @param shell
     *            - the shell for the {@link FileDialog}
     * @param text
     *            - the header for the {@link FileDialog}
     * @param defaultFolderPreference
     *            - the id of the defaultFolderpreferences to be updated for the
     *            default directory
     * @param filterExtensions
     *            - the possible extensions to filter in the {@link FileDialog}
     * @param style
     *            - SWT.OPEN or SWT.SAVE
     * @return the absolute filepath to the chosen location
     */
    public static String createFilePath(Shell shell, String text, String defaultFolderPreference,
            Map<String, String> filterExtensions, int style) {
        return createFilePath(shell, text, defaultFolderPreference, filterExtensions, style, null);
    }

    private static String getDirectory(String defaultFolderPreference) {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        String dir = prefs.getString(defaultFolderPreference);
        if (dir == null || dir.isEmpty()) {
            dir = System.getProperty("user.home"); //$NON-NLS-1$
        }
        if (!dir.endsWith(System.getProperty("file.separator"))) { //$NON-NLS-1$
            dir = dir + System.getProperty("file.separator"); //$NON-NLS-1$
        }
        return dir;
    }

    /**
     * creating a filepath for vlt files
     * 
     * @see #createFilePath(Shell, String, String, Map, int)
     */
    public static String createVltFilePath(Shell shell, String text, int style,
            String defaultFileName) {
        return createFilePath(shell, text, PreferenceConstants.DEFAULT_FOLDER_VLT, vltExtensions,
                style, defaultFileName);
    }

    /**
     * creating a filepath for vlt files
     * 
     * @see #createFilePath(Shell, String, String, Map, int)
     */
    public static String createCsvFilePath(Shell shell, String text, String defaultFileName) {
        return createFilePath(shell, text, PreferenceConstants.DEFAULT_FOLDER_CSV_EXPORT,
                csvExtensions, SWT.SAVE, defaultFileName);
    }

    /**
     * Returns a filepath but in addition there are all scopes to be chosen for
     * the CSV-Export.
     * 
     * @see CsvExportDialog
     */
    public static String createCsvFilePathAndHandleScopes(Shell shell, String text,
            VeriniceLinkTable veriniceLinkTable) {

        csvDialog = new CsvExportDialog(Display.getCurrent().getActiveShell(), text,
                veriniceLinkTable);
        if (csvDialog.open() == Dialog.OK) {
            return csvDialog.getFilePath();
        }

        return null;

    }


    public static LinkTableValidationResult isValidVeriniceLinkTable(
            VeriniceLinkTable veriniceLinkTable) {

        LinkTableValidationResult result = new LinkTableValidationResult();
        result.setValid(true);
        try {
            validateColumnPathsElements(veriniceLinkTable.getColumnPaths());
            validateRelationIds(veriniceLinkTable.getRelationIds());
            validateRelations(veriniceLinkTable.getColumnPaths());
        } catch (ObjectModelValidationException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e);
            }
            result.setValid(false);
            result.setMessage(e.getMessage());

        }

        return result;
    }

    private static void validateRelations(List<String> columnPaths) throws ObjectModelValidationException {

        Set<Entry<String, String>> relations = getRelations(columnPaths);
        for (Entry<String, String> relation : relations) {
            Set<String> possibleRelationPartners = getLoader()
                    .getPossibleRelationPartners(relation.getKey());
            if (!possibleRelationPartners.contains(relation.getValue())) {
                throw new ObjectModelValidationException("Relation " + relation.toString() + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

    }

    private static void validateColumnPathsElements(List<String> list) throws ObjectModelValidationException {

        for (String path : list) {
            try {
                validateColumnPath(path);
            } catch (Exception e) {
                throw new ObjectModelValidationException(path + Messages.LinkTableUtil_2, e);
            }
        }

    }

    public static void validateColumnPath(String path) throws ObjectModelValidationException {
        try {
            ColumnPathParser.throwExceptionIfInvalid(path);
        } catch (ColumnPathParseException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e);
            }
            throw new ObjectModelValidationException(path + " is no valid column path", e); //$NON-NLS-1$
        }
        Set<String> objectTypeIds = ColumnPathParser.getObjectTypeIds(path);
        for (String id : objectTypeIds) {

            boolean validTypeId = getLoader().isValidTypeId(id);
            if (!validTypeId) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(id + " is no typeId"); //$NON-NLS-1$
                }
                throw new ObjectModelValidationException(validTypeId + " is no valid type ID"); //$NON-NLS-1$
            }
        }
    }

    /**
     * Returns a set of all relations with key- value pairs, where the key is
     * the first typeID and the value the second.
     * 
     */
    public static Set<Entry<String, String>> getRelations(List<String> columnPathes) {

        Set<Entry<String, String>> relations = new HashSet<>();

        for (String path : columnPathes) {
            List<String> pathElements = ColumnPathParser.getColumnPathAsList(path);
            int index = 0;
            for (String element : pathElements) {
                if (LinkTableOperationType.isRelation(element)) {
                    relations.add(new SimpleEntry<String, String>(pathElements.get(index - 1),
                            pathElements.get(index + 1)));

                }
                index++;
            }

        }

        return relations;
    }

    private static void validateRelationIds(List<String> list) throws ObjectModelValidationException {

        for (String id : list) {
            if (!getLoader().isValidRelationId(id)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(id + " is no RelationID"); //$NON-NLS-1$
                }
                throw new ObjectModelValidationException(id + " is no valid relation id"); //$NON-NLS-1$
            }
        }
    }
    
    public static String getCnaLinkPropertyMessage(String cnaLinkProperty) {
        switch (cnaLinkProperty) {
        case CnaLinkPropertyConstants.TYPE_TITLE:
            return Messages.LinkTableColumn_CnaLink_Property_Title;
        case CnaLinkPropertyConstants.TYPE_DESCRIPTION:
            return Messages.LinkTableColumn_CnaLink_Property_Description;
        case CnaLinkPropertyConstants.TYPE_RISK_VALUE_C:
            return Messages.LinkTableColumn_CnaLink_Property_C;
        case CnaLinkPropertyConstants.TYPE_RISK_VALUE_I:
            return Messages.LinkTableColumn_CnaLink_Property_I;
        case CnaLinkPropertyConstants.TYPE_RISK_VALUE_A:
            return Messages.LinkTableColumn_CnaLink_Property_A;
        case CnaLinkPropertyConstants.TYPE_RISK_VALUE_C_WITH_CONTROLS:
            return Messages.LinkTableColumn_CnaLink_Property_C_With_Controls;
        case CnaLinkPropertyConstants.TYPE_RISK_VALUE_I_WITH_CONTROLS:
            return Messages.LinkTableColumn_CnaLink_Property_I_With_Controls;
        case CnaLinkPropertyConstants.TYPE_RISK_VALUE_A_WITH_CONTROLS:
            return Messages.LinkTableColumn_CnaLink_Property_A_With_Controls;
        case CnaLinkPropertyConstants.TYPE_RISK_TREATMENT:
            return Messages.LinkTableColumn_CnaLink_Property_Risk_Treatment;
        default:
            return Messages.LinkTableColumn_CnaLink_Property_Unknown;
        }
    }

    /**
     * create an alias for a columnPath to improve headers in csv-file.
     */
    public static String createAlias(String columnPath) {
        String[] columnPathElements = columnPath.split("\\.|\\<|\\>|\\/|\\:");
        int lastElement = columnPathElements.length - 1;
        String propertyId;
        String message;
        try {
            propertyId = columnPathElements[lastElement];
            String element = columnPathElements[lastElement - 1];
            LOG.debug(columnPath);
            LOG.debug("Element:" + columnPathElements[lastElement - 1]);
            LOG.debug("Property:" + propertyId);
            if (columnPath.contains(LinkTableOperationType.RELATION.getOutput())) {
                message = LinkTableUtil.getCnaLinkPropertyMessage(propertyId);
            } else {
                message = loader.getLabel(propertyId) + " ("
                        + loader.getLabel(element) + ")";
            }
        } catch (IndexOutOfBoundsException e) {
            LOG.warn("String-split did not work, using old way, column path: " + columnPath);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stackstrace: ", e);
            }
            int propertyBeginning = columnPath
                    .lastIndexOf(LinkTableOperationType.PROPERTY.getOutput());
            propertyId = columnPath.substring(propertyBeginning + 1);
            if (columnPath.contains(":")) {
                message = LinkTableUtil.getCnaLinkPropertyMessage(propertyId);
            } else {
                message = loader.getLabel(propertyId);
            }
        }
        message = StringUtils.replaceEachRepeatedly(message,
                new String[] { "/", ":", ".", "<", ">" }, new String[] { "", "", "", "", "" });
        message = message.replaceAll(" ", "_");

        return columnPath + ALIAS_DELIMITER + message;
    }
    
    public static IObjectModelService getLoader() {
        if (loader == null) {
            loader = createLoader();
        }
        return loader;
    }

    private static IObjectModelService createLoader() {
        return loader = (IObjectModelService) HUIObjectModelLoader.getInstance();
    }
    

}
