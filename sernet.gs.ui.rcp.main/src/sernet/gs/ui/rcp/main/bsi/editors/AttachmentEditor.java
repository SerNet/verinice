/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.ArrayList;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.IEntityChangedListener;
import sernet.hui.common.connect.PropertyChangedEvent;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.multiselectionlist.IMLPropertyType;
import sernet.hui.swt.widgets.HitroUIComposite;
import sernet.snutils.DBException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bsi.Addition.INoteChangedListener;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.AttachmentFile;
import sernet.verinice.service.commands.SaveAttachment;
import sernet.verinice.service.commands.SaveNote;

public class AttachmentEditor extends EditorPart {

    private static final Logger LOG = Logger.getLogger(AttachmentEditor.class);

    public static final String EDITOR_ID = "sernet.gs.ui.rcp.main.bsi.editors.attachmenteditor"; //$NON-NLS-1$

    Attachment attachment;

    Composite parent;

    HitroUIComposite huiComposite;

    Composite contentComp;

    FileDialog fd;

    Text fileName;

    Text textNote;

    Label date;

    private ICommandService commandService;

    private boolean isModelModified = false;

    private IEntityChangedListener modelListener = new IEntityChangedListener() {
        public void dependencyChanged(IMLPropertyType arg0, IMLPropertyOption arg1) {
            // not relevant
        }

        public void selectionChanged(IMLPropertyType arg0, IMLPropertyOption arg1) {
            modelChanged();
        }

        public void propertyChanged(PropertyChangedEvent evt) {
            modelChanged();
        }
    };

    void modelChanged() {
        boolean wasDirty = isDirty();
        isModelModified = true;

        if (!wasDirty) {
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    @Override
    public boolean isDirty() {
        return isModelModified;
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        monitor.beginTask(Messages.AttachmentEditor_1, IProgressMonitor.UNKNOWN);
        boolean isNew = attachment.getDbId() == null;
        Set<INoteChangedListener> listener = attachment.getListener();
        SaveNote command = new SaveNote(attachment);
        try {
            command = getCommandService().executeCommand(command);
            attachment = (Attachment) command.getAddition();
            huiComposite.dispose();
            huiComposite = new HitroUIComposite(parent, false);
            huiComposite.createView(attachment.getEntity(), true, true, new String[] {} , false, ServiceFactory.lookupValidationService().getPropertyTypesToValidate(attachment.getEntity(), attachment.getDbId()), Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.USE_VALIDATION_GUI_HINTS));
            parent.layout();
            // file-data is immutable, just save new file-data
            if (isNew) {
                AttachmentFile attachmentFile = new AttachmentFile();
                attachmentFile.readFileData(attachment.getFilePath());
                SaveAttachment saveFileCommand = new SaveAttachment(attachmentFile);
                attachmentFile.setDbId(attachment.getDbId());
                saveFileCommand = getCommandService().executeCommand(saveFileCommand);
                saveFileCommand.clear();
            }
        } catch (Exception e) {
            LOG.error("Error while saving file", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.AttachmentEditor_3);
        }
        monitor.done();
        attachment.getListener().addAll(listener);
        isModelModified = false;
        firePropertyChange(IEditorPart.PROP_DIRTY);
        attachment.getEntity().addChangeListener(this.modelListener);
        setPartName(attachment.getTitel());
        attachment.fireChange();
    }

    @Override
    public void doSaveAs() {
        // TODO Auto-generated method stub

    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        if (!(input instanceof AttachmentEditorInput)) {
            throw new PartInitException(Messages.AttachmentEditor_4);
        }
        AttachmentEditorInput noteEditorInput = (AttachmentEditorInput) input;
        attachment = noteEditorInput.getInput();
        setSite(site);
        setInput(noteEditorInput);
        setPartName(noteEditorInput.getName());
        // add listener to mark editor as dirty on changes:
        noteEditorInput.getInput().getEntity().addChangeListener(this.modelListener);
    }

    @Override
    public boolean isSaveAsAllowed() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void createPartControl(Composite parent) {
        this.parent = parent;
        huiComposite = new HitroUIComposite(parent, false);
        try {
            // no validation here, so empty list passed
            huiComposite.createView(attachment.getEntity(), true, true, new String[] {}, false, new ArrayList<String>(0), Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.USE_VALIDATION_GUI_HINTS));
        } catch (DBException e) {
            LOG.error("Error while creating editor", e); //$NON-NLS-1$
        }
        InputHelperFactory.setInputHelpers(HitroUtil.getInstance().getTypeFactory().getEntityType(attachment.getEntity().getEntityType()), huiComposite);
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void dispose() {
        IEditorInput editorInput = getEditorInput();
        if(editorInput instanceof NoteEditorInput) {
            EditorRegistry.getInstance().closeEditor(String.valueOf(((NoteEditorInput) editorInput).getId()));
        }
        if(editorInput instanceof AttachmentEditorInput) {
            EditorRegistry.getInstance().closeEditor(String.valueOf(((AttachmentEditorInput) editorInput).getId()));
        }
        super.dispose();
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandServive();
        }
        return commandService;
    }

    private ICommandService createCommandServive() {
        return ServiceFactory.lookupCommandService();
    }

}
