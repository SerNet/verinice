/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.editors;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.Attachment;
import sernet.gs.ui.rcp.main.bsi.model.AttachmentFile;
import sernet.gs.ui.rcp.main.bsi.model.Addition.INoteChangedListener;
import sernet.gs.ui.rcp.main.common.model.HitroUtil;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveNote;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.IEntityChangedListener;
import sernet.hui.common.connect.PropertyChangedEvent;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.multiselectionlist.IMLPropertyType;
import sernet.hui.swt.widgets.HitroUIComposite;
import sernet.snutils.DBException;

public class AttachmentEditor extends EditorPart {

	private static final Logger LOG = Logger.getLogger(AttachmentEditor.class);
	
	public static final String EDITOR_ID = "sernet.gs.ui.rcp.main.bsi.editors.attachmenteditor";
	
	private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
	
	Attachment attachment;
	
	Composite parent;
	
	HitroUIComposite huiComposite;
	
	Composite contentComp;
	
	FileDialog fd;
	
	Text fileName;
	
	Text textNote;
	
	Label date;
	
	private ICommandService	commandService;

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
		
		if (!wasDirty)
			firePropertyChange(IEditorPart.PROP_DIRTY);
	}
	
	@Override
	public boolean isDirty() {
		return isModelModified;
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		monitor.beginTask("Speichern", IProgressMonitor.UNKNOWN);
		boolean isNew = attachment.getDbId()==null;
		Set<INoteChangedListener> listener = attachment.getListener();
		SaveNote command = new SaveNote(attachment);	
		try {		
			command = getCommandService().executeCommand(command);
			attachment = (Attachment) command.getAddition();
			huiComposite.dispose();
			huiComposite = new HitroUIComposite(parent, SWT.NULL, false);
			huiComposite.createView(attachment.getEntity(), true, true);
			parent.layout();
			// file-data is immutable, just save new file-data
			if(isNew) {
				AttachmentFile attachmentFile = new AttachmentFile();
				attachmentFile.readFileData(attachment.getFilePath());	
				SaveElement<AttachmentFile> saveFileCommand = new SaveElement<AttachmentFile>(attachmentFile);
				attachmentFile.setDbId(attachment.getDbId());
				saveFileCommand = getCommandService().executeCommand(saveFileCommand);
			}
		} catch (Exception e) {
			LOG.error("Error while saving file", e);
			ExceptionUtil.log(e, "Fehler beim Speichern des Attachments.");
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
		if (! (input instanceof AttachmentEditorInput)) {
			throw new PartInitException("invalid input");
		}
		AttachmentEditorInput noteEditorInput = (AttachmentEditorInput) input;
		attachment=noteEditorInput.getInput();
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
		huiComposite = new HitroUIComposite(parent, SWT.NULL, false);
		try {
			huiComposite.createView(attachment.getEntity(), true, true);
		} catch (DBException e) {
			LOG.error("Error while creating editor", e);
		}
		InputHelperFactory.setInputHelpers(HitroUtil.getInstance().getTypeFactory().getEntityType(attachment.getEntity().getEntityType()), huiComposite);
	}

	@Override
	public void setFocus() {
	}
	
	@Override
	public void dispose() {
		EditorRegistry.getInstance().closeEditor(String.valueOf(((NoteEditorInput)getEditorInput()).getId()));
		super.dispose();
	}
	
	public Attachment getAttachment() {
		return attachment;
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

	public ICommandService getCommandService() {
		if(commandService==null) {
			commandService = createCommandServive();
		}
		return commandService;
	}

	private ICommandService createCommandServive() {
		return ServiceFactory.lookupCommandService();
	}

}
