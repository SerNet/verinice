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
package sernet.gs.ui.rcp.main.bsi.wizards;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.StatusLine;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

/**
 * Wizard page to allow the user to choose the OpenOffice installation that
 * should be used for export, templates etc.
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */
public class ChooseExportMethodPage extends WizardPage {

    private static final Logger LOG = Logger.getLogger(ChooseExportMethodPage.class);

    private static final int MAX_SEARCH = 10000;

    private Text oodirText;

    private String ooDir = null;
    private int dirsSearched = 0;

    private Text calcTemplateText;

    private Text documentTemplateText;

    protected ChooseExportMethodPage() {
        super("chooseExport"); //$NON-NLS-1$
        setTitle(Messages.ChooseExportMethodPage_1);
        setDescription(Messages.ChooseExportMethodPage_2);
    }

    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        container.setLayout(gridLayout);
        setControl(container);

        final Label label2 = new Label(container, SWT.NULL);
        GridData gridData7 = new GridData(GridData.HORIZONTAL_ALIGN_END);
        label2.setLayoutData(gridData7);
        label2.setText(Messages.ChooseExportMethodPage_3);
        
        oodirText = new Text(container, SWT.BORDER);
        GridData gridData2 = new GridData(GridData.FILL_HORIZONTAL);
        oodirText.setLayoutData(gridData2);
        oodirText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0) {
                ((ExportWizard) getWizard()).setOoPath(getDirPath(oodirText));
                updatePageComplete();
            }
        });
        oodirText.setText(findOODir());

        final Button btn3 = new Button(container, SWT.NULL);
        btn3.setText(Messages.ChooseExportMethodPage_4);
        btn3.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                browseForDir(oodirText);
            }
        });

        final Label label3 = new Label(container, SWT.NULL);
        GridData gridData9 = new GridData(GridData.HORIZONTAL_ALIGN_END);
        label3.setLayoutData(gridData9);
        label3.setText(Messages.ChooseExportMethodPage_5);

        calcTemplateText = new Text(container, SWT.BORDER);
        calcTemplateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        calcTemplateText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0) {
                ((ExportWizard) getWizard()).setTemplatePath(getDirPath(calcTemplateText));
                updatePageComplete();
            }
        });
        Preferences prefs = Activator.getDefault().getPluginPreferences();
        calcTemplateText.setText(prefs.getString(PreferenceConstants.OOTEMPLATE));

        final Button btn5 = new Button(container, SWT.NULL);
        btn5.setText(Messages.ChooseExportMethodPage_6);
        btn5.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                browseForFile(calcTemplateText);
            }
        });

        final Label label4 = new Label(container, SWT.NULL);
        GridData gridData10 = new GridData(GridData.HORIZONTAL_ALIGN_END);
        label4.setLayoutData(gridData10);
        label4.setText(Messages.ChooseExportMethodPage_7);

        documentTemplateText = new Text(container, SWT.BORDER);
        documentTemplateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        documentTemplateText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0) {
                ((ExportWizard) getWizard()).setTextTemplatePath(getDirPath(documentTemplateText));
                updatePageComplete();
            }
        });
        documentTemplateText.setText(prefs.getString(PreferenceConstants.OOTEMPLATE_TEXT));

        final Button btn6 = new Button(container, SWT.NULL);
        btn6.setText(Messages.ChooseExportMethodPage_8);
        btn6.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                browseForFile(documentTemplateText);
            }
        });

        updatePageComplete();
    }

    private String findOODir() {
        ooDir = null;
        String textOoDir = Activator.getDefault().getPluginPreferences().getString(PreferenceConstants.OODIR);
        if (isOOPathValid(textOoDir)) {
            return textOoDir;
        }

        LOG.warn("OO Pfad in Preferences stimmt nicht. Suche..."); //$NON-NLS-1$
        StatusLine.setMessage(Messages.ChooseExportMethodPage_15);
        
        String[] dirs = new String[] { textOoDir, "/usr/lib", "/usr/local", "/usr/lib64" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        for (int i = 0; i < dirs.length; i++) {
            dirsSearched = 0;
            if (dirsSearched > MAX_SEARCH) {
                return ""; //$NON-NLS-1$
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Searching OpenOffice in directory: " + dirs[i]); //$NON-NLS-1$
            }
            findOOSubDir(dirs[i]);
            if (ooDir != null) {
                return ooDir;
            }
        }

        return ""; //$NON-NLS-1$

    }

    private void findOOSubDir(String dir) {
        File f = new File(dir);
        if (!f.isDirectory()) {
            return;
        }
        ++dirsSearched;

        File[] files = f.listFiles();
        if (files == null) {
            return;
        }
        Pattern pat = Pattern.compile("(office|ooo)", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
        for (int i = 0; i < files.length; i++) {
            if (ooDir != null || dirsSearched > MAX_SEARCH) {
                return;
            }

            Matcher m = pat.matcher(files[i].getPath());
            String path = files[i].getAbsolutePath();
            if (m.find() && isOOPathValid(files[i].getAbsolutePath())) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Path to OpenOffice found: " + path); //$NON-NLS-1$
                }
                StatusLine.setMessage(NLS.bind(Messages.ChooseExportMethodPage_16,path)); 
                Activator.getDefault().getPluginPreferences().setValue(PreferenceConstants.OODIR,path);
                ooDir = path;
                return;
            } else {
                if (files[i].isDirectory()) {
                    // recurse down:
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Searching OpenOffice in directory: " + path); //$NON-NLS-1$
                    }
                    findOOSubDir(path);
                }
            }
        }

        return;
    }

    protected void browseForDir(Text text) {
        DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
        dialog.setFilterPath(getDirPath(text));
        String path = dialog.open();
        text.setText(path != null ? path : ""); //$NON-NLS-1$
    }

    protected void browseForFile(Text text) {
        FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
        dialog.setFilterExtensions(new String[] { "*.odt", "*.ods", "*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String path = dialog.open();
        text.setText(path != null ? path : ""); //$NON-NLS-1$
    }

    private String getDirPath(Text text) {
        String textString = text != null ? text.getText().trim() : ""; //$NON-NLS-1$
        Path path = new Path(textString);
        return path.toOSString();
    }

    ExportWizard getExportWizard() {
        return ((ExportWizard) getWizard());
    }

    private boolean isOOPathValid(String dir) {
        return new Path(dir + File.separator + "program").toFile().exists(); //$NON-NLS-1$
    }

    private void updatePageComplete() {
        if (!isMasterValid()) {
            setMessage(null);
            setErrorMessage(Messages.ChooseExportMethodPage_9);
            setPageComplete(false);
            return;
        }

        if (!isOOPathValid(getDirPath(oodirText))) {
            setMessage(null);
            setErrorMessage(Messages.ChooseExportMethodPage_10);
            setPageComplete(false);
            return;
        }

        setMessage(Messages.ChooseExportMethodPage_28);
        setErrorMessage(null);
        setPageComplete(true);
    }

    private boolean isMasterValid() {
        File file = new Path(getDirPath(calcTemplateText)).toFile();
        File file2 = new Path(getDirPath(documentTemplateText)).toFile();
        return (file.exists() && file.isFile() && file2.exists() && file2.isFile());
        // return (file.exists() && file.isFile());
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            updatePageComplete();
        }
    }

}
