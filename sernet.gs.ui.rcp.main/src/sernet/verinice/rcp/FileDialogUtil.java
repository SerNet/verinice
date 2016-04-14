/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

/**
 *
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class FileDialogUtil {
    
    private static final Logger LOG = Logger.getLogger(FileDialogUtil.class);

    private int style;
    private String title;
    private String fileSuffix;
    private String fileTypeLabel;
    private String defaultFolderPreference;
    
    /**
     * @param builder
     */
    private FileDialogUtil(Builder builder) {
        setDefaultFolderPreference(builder.getDefaultFolderPreference());
        setFileSuffix(builder.getFileSuffix());
        setFileTypeLabel(builder.getFileTypeLabel());
        setStyle(builder.getStyle());
        setTitle(builder.getTitle());
    }

    public String open() {
        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), style);
        dialog.setText(title);
        try {
            dialog.setFilterPath(getDirectory());
        } catch (Exception e1) {
            LOG.debug("Error with file path: " + getDirectory(), e1);
            dialog.setFileName(""); //$NON-NLS-1$
        }
        if(fileSuffix!=null) {
            dialog.setFilterExtensions(new String[] {"*" + fileSuffix}); //$NON-NLS-1$
        }
        if(fileTypeLabel!=null) {
            dialog.setFilterNames(new String[] {fileTypeLabel});
            dialog.setFilterIndex(0);
        }
        return dialog.open();
    }

    private String getDirectory() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        String dir = prefs.getString(defaultFolderPreference);
        if(dir==null || dir.isEmpty()) {
            dir = System.getProperty("user.home");
        }
        if (!dir.endsWith(System.getProperty("file.separator"))) {
            dir = dir + System.getProperty("file.separator");
        }
        return dir;
    }
    
    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public String getFileTypeLabel() {
        return fileTypeLabel;
    }

    public void setFileTypeLabel(String fileTypeLabel) {
        this.fileTypeLabel = fileTypeLabel;
    }

    public String getDefaultFolderPreference() {
        return defaultFolderPreference;
    }

    public void setDefaultFolderPreference(String defaultFolderPreference) {
        this.defaultFolderPreference = defaultFolderPreference;
    }

    public static class Builder {
        
        private int style = SWT.OPEN;
        private String title = "Select File";
        private String fileSuffix = ".txt";
        private String fileTypeLabel = "Text file (*.txt)";
        private String defaultFolderPreference = PreferenceConstants.DEFAULT_FOLDER_DIALOG;
       
        public Builder(int style, String title) {
            super();
            this.style = style;
            this.title = title;
        }
        
        public FileDialogUtil build() {
            return new FileDialogUtil(this);
        }
        
        public String open() {
            FileDialogUtil dialogUtil = new FileDialogUtil(this);
            return dialogUtil.open();
        }
        
        public int getStyle() {
            return style;
        }
        public Builder setStyle(int style) {
            this.style = style;
            return this;
        }
        public String getTitle() {
            return title;
        }
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }
        public String getFileSuffix() {
            return fileSuffix;
        }
        public Builder setFileSuffix(String fileSuffix) {
            this.fileSuffix = fileSuffix;
            return this;
        }
        public String getFileTypeLabel() {
            return fileTypeLabel;
        }
        public Builder setFileTypeLabel(String fileTypeLabel) {
            this.fileTypeLabel = fileTypeLabel;
            return this;
        }
        public String getDefaultFolderPreference() {
            return defaultFolderPreference;
        }
        public Builder setDefaultFolderPreference(String defaultFolderPreference) {
            this.defaultFolderPreference = defaultFolderPreference;
            return this;
        }
        
        
    }
}
