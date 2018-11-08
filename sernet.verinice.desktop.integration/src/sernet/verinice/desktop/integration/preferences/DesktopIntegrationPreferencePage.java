/*******************************************************************************
 * Copyright (c) 2018 Alexander Ben Nasrallah <an@sernet.de>
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
 *     Alexander Ben Nasrallah
 ******************************************************************************/
package sernet.verinice.desktop.integration.preferences;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sernet.verinice.desktop.integration.Activator;

/**
 * This page bundles preference to integrate verinice. to the users desktop
 * environment.
 *
 * Currently only XDG desktop files are supported, by writing
 * to $HOME/.local/applications/verinice.desktop.
 *
 * In case of multiple installed instances: Last saver wins!
 */
public class DesktopIntegrationPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private final Path desktopFilePath = Paths.get(System.getProperty("user.home"),
            ".local/share/applications/verinice.desktop");
    private final Logger logger = LoggerFactory.getLogger(DesktopIntegrationPreferencePage.class);

    public DesktopIntegrationPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription(Messages.DesktopIntegrationPreferencePage_Description);
    }

    public void createFieldEditors() {
        addField(new BooleanFieldEditor(PreferenceConstants.SHORTCUT_EXISTS,
                Messages.DesktopIntegrationPreferencePage_ShowInMenu,
                BooleanFieldEditor.DEFAULT, getFieldEditorParent()));
    }

    public void init(IWorkbench workbench) {
        getPreferenceStore().setValue(PreferenceConstants.SHORTCUT_EXISTS,
                desktopFilePath.toFile().exists());
    }

    @Override
    public boolean performOk() {
        if (!super.performOk()) {
            return false;
        }
        try {
            if (getPreferenceStore().getBoolean(PreferenceConstants.SHORTCUT_EXISTS)) {
                List<String> lines = getDesktopFileLines();
                Files.write(desktopFilePath, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
            } else {
                Files.delete(desktopFilePath);
            }
        }
        catch (IOException e) {
            logger.error("Error handling desktop file.", e);
        }
        return true;
    }

    private List<String> getDesktopFileLines() {
        File launcher = new File(System.getProperty("eclipse.launcher"));
        List<String> lines = new ArrayList<>();
        lines.add("[Desktop Entry]");
        lines.add("Encoding=UTF-8");
        lines.add("Version=1.0");
        lines.add("Type=Application");
        lines.add("Terminal=false");
        lines.add(String.format("Exec=%s", launcher.getAbsolutePath()));
        lines.add("Name=verinice.");
        // We have to hope that the icon file has not been renamed:
        lines.add(String.format("Icon=%s/icon.xpm", launcher.getParent()));
        lines.add("Categories=Office");
        return lines;
    }
}