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
package sernet.gs.ui.rcp.main.bsi.views;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.editors.AttachmentEditor;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.model.Attachment;
import sernet.gs.ui.rcp.main.bsi.model.AttachmentFile;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.DeleteNote;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadAttachmentFile;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadAttachments;

/**
 * Lists files {@link Attachment} attached to a CnATreeElement.
 * User can view, save, delete and add files by toolbar buttons.
 * 
 * @see AttachmentEditor - Editor for metadata of files
 * @see LoadAttachments - Command for loading files
 * @author Daniel Murygin <dm@sernet.de>
 */
public class FileView extends ViewPart {

	private static final Logger LOG = Logger.getLogger(FileView.class);
	
	public static final String ID = "sernet.gs.ui.rcp.main.bsi.views.FileView"; //$NON-NLS-1$
	
	private ICommandService	commandService;
	
	private Composite parent;
	
	protected TableViewer viewer;
	protected TableColumn iconColumn;
	protected TableColumn fileNameColumn;
	protected TableColumn mimeTypeColumn;
	protected TableColumn textColumn;
	protected TableColumn dateColumn;
	protected TableColumn versionColumn;
	
	private AttachmentContentProvider contentProvider = new AttachmentContentProvider(this);
	
	private ISelectionListener selectionListener;

	private Action addFileAction;

	private Action deleteFileAction;

	private Action doubleClickAction;
	
	private Action saveCopyAction;
	
	private Action openAction;
	
	private CnATreeElement currentCnaElement;
	
	public FileView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		parent.setLayout(new FillLayout());
		try {
		    createTable(parent);		
			getSite().setSelectionProvider(viewer);		
			hookPageSelection();
			viewer.setInput(new PlaceHolder("kein Element ausgewählt"));
		} catch (Exception e) {
			ExceptionUtil.log(e, Messages.BrowserView_3);
			LOG.error("Error while creating control", e);
		}	
		makeActions();
		hookActions();
		fillLocalToolBar();
	}

	private void createTable(Composite parent) {
		viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new AttachmentLabelProvider());
		Table table = viewer.getTable();
		iconColumn = new TableColumn(table, SWT.LEFT);;
		iconColumn.setWidth(26);
		fileNameColumn = new TableColumn(table, SWT.LEFT);
		fileNameColumn.setText("Name");
		fileNameColumn.setWidth(120);
		mimeTypeColumn = new TableColumn(table, SWT.LEFT);
		mimeTypeColumn.setText("Typ");
		mimeTypeColumn.setWidth(50);
		textColumn = new TableColumn(table, SWT.LEFT);
		textColumn.setText("Info");
		textColumn.setWidth(350);
		dateColumn = new TableColumn(table, SWT.LEFT);
		dateColumn.setText("Datum");
		dateColumn.setWidth(120);
		versionColumn = new TableColumn(table, SWT.LEFT);
		versionColumn.setText("Version");
		versionColumn.setWidth(60);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
	}
	
	private void hookActions() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				saveCopyAction.setEnabled(true);
				openAction.setEnabled(true);
			}			
		});
	}
	
	private void hookPageSelection() {
		selectionListener = new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				pageSelectionChanged(part, selection);
			}
		};
		getSite().getPage().addPostSelectionListener(selectionListener);
	}
	
	protected void pageSelectionChanged(IWorkbenchPart part, ISelection selection) {
		Object element = ((IStructuredSelection) selection).getFirstElement();
		if (part == this) {
			openAction.setEnabled(element!=null);
			saveCopyAction.setEnabled(element!=null);
			deleteFileAction.setEnabled(element!=null);
			return;
		}
			
		if (!(selection instanceof IStructuredSelection)) {
			openAction.setEnabled(false);
			saveCopyAction.setEnabled(false);
			deleteFileAction.setEnabled(false);
			return;
		}
		try {
			if(element instanceof CnATreeElement) {
				addFileAction.setEnabled(true);		
				setCurrentCnaElement((CnATreeElement) element);
				clear();
				loadFiles();
				
			} else {
				addFileAction.setEnabled(false);
			}
			Attachment att = (Attachment) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
			openAction.setEnabled(att!=null);
			saveCopyAction.setEnabled(att!=null);
			deleteFileAction.setEnabled(att!=null);
			
		} catch (Exception e) {
			LOG.error("Error while loading notes", e);
		}
	}
	
	public void clear() {
		
	}
	
	public void loadFiles() {
		try {
			LoadAttachments command = new LoadAttachments(getCurrentCnaElement().getDbId());		
			command = getCommandService().executeCommand(command);		
			List<Attachment> attachmentList = command.getAttachmentList();
			if(attachmentList!=null) {
				viewer.setInput(attachmentList);
				for (final Attachment attachment : attachmentList) {
					// set transient cna-element-titel
					attachment.setCnAElementTitel(getCurrentCnaElement().getTitel());
					attachment.addListener(new Attachment.INoteChangedListener() {
						public void noteChanged() {
							clear();
							loadFiles();
						}
					});
				}
			}
		
		} catch(Exception e) {
			LOG.error("Error while loading attachment", e);
			ExceptionUtil.log(e, "Error while attachment notes");
		}
	}
	
	protected void editFile(Attachment attachment) {
		EditorFactory.getInstance().openEditor(attachment);
	}
	
	protected void deleteFile(Attachment attachment) {
		DeleteNote command = new DeleteNote(attachment);		
		try {
			command = getCommandService().executeCommand(command);
		} catch (CommandException e) {
			LOG.error("Error while saving attachment", e);
			ExceptionUtil.log(e, "Fehler beim Speichern der Datei.");
		}
		clear();
		loadFiles();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	
	
	private void fillLocalToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		manager.add(this.addFileAction);
		manager.add(this.deleteFileAction);
		manager.add(this.saveCopyAction);
		manager.add(this.openAction);
	}
	
	private void makeActions() {
		addFileAction = new Action() {
			public void run() {
				FileDialog fd = new FileDialog(FileView.this.getSite().getShell());
		        fd.setText("Anhang auswählen...");
		        fd.setFilterPath("~");
		        String selected = fd.open();
		        if(selected!=null && selected.length()>0) {
					Attachment attachment = new Attachment();
					attachment.setCnATreeElementId(getCurrentCnaElement().getDbId());
					attachment.setCnAElementTitel(getCurrentCnaElement().getTitel());
					attachment.setTitel("neue Datei");
					attachment.setDate(Calendar.getInstance().getTime());
					attachment.setFilePath(selected);
					attachment.addListener(new Attachment.INoteChangedListener() {
						public void noteChanged() {
							clear();
							loadFiles();
						}
					});
					EditorFactory.getInstance().openEditor(attachment);	
		        }
			}
		};
		addFileAction.setText("Datei hinzufügen...");
		addFileAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.NOTE_NEW));
		addFileAction.setEnabled(false);
		
		deleteFileAction = new Action() {
			public void run() {
				Attachment sel = (Attachment) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				DeleteNote command = new DeleteNote(sel);		
				try {
					command = getCommandService().executeCommand(command);
				} catch (CommandException e) {
					LOG.error("Error while saving note", e);
					ExceptionUtil.log(e, "Fehler beim Speichern der Notiz.");
				}
				clear();
				loadFiles();			
			}
		};
		deleteFileAction.setText("Datei löschen...");
		deleteFileAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.DELETE));
		deleteFileAction.setEnabled(false);

		doubleClickAction = new Action() {
			public void run() {
				Object sel = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				EditorFactory.getInstance().openEditor(sel);
			}
		};
		
		saveCopyAction = new Action() {
			public void run() {
				Attachment attachment = (Attachment) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				saveCopy(attachment);
			}
		};
		saveCopyAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.SAVE));
		saveCopyAction.setEnabled(false);
		
		openAction = new Action() {
			public void run() {
				openFile();
			}

			
		};
		openAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.VIEW));
		openAction.setEnabled(false);
	}
	
	private void openFile() {
		Attachment attachment = (Attachment) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		if(attachment!=null) {
			try {
				LoadAttachmentFile command = new LoadAttachmentFile(attachment.getDbId());		
				command = getCommandService().executeCommand(command);		
				AttachmentFile attachmentFile = command.getAttachmentFile();
				String tempDir = System.getProperty("java.io.tmpdir");
				if(attachmentFile!=null && tempDir!=null) {
					if(!tempDir.endsWith(String.valueOf(File.separatorChar))) {
						tempDir = tempDir + File.separatorChar;
					}
					String path = tempDir + attachment.getFileName();
					try {
						attachmentFile.writeFileData(path);
						Program.launch(path);
					} catch (IOException e) {
						LOG.error("Error while saving temp file", e);
						ExceptionUtil.log(e, "Fehler beim Öffnen der Datei.");
					}
				}
			} catch(Exception e) {
				LOG.error("Error while loading attachment", e);
				ExceptionUtil.log(e, "Error while attachment notes");
			}
		}
	}
	
	protected void saveCopy(Attachment attachment) {
		FileDialog fd = new FileDialog(FileView.this.getSite().getShell(),SWT.SAVE);
        fd.setText("Datei speichern...");
        fd.setFilterPath("~");
        fd.setFileName(attachment.getFileName());
        String selected = fd.open();
        if(selected!=null) {
	        try {
	        	LoadAttachmentFile command = new LoadAttachmentFile(attachment.getDbId());		
				command = getCommandService().executeCommand(command);		
				AttachmentFile attachmentFile = command.getAttachmentFile();
				attachmentFile.writeFileData(selected);
				if (LOG.isDebugEnabled()) {
					LOG.debug("File saved: " + selected);
				}
			} catch (Exception e) {
				LOG.error("Error while saving file", e);
				ExceptionUtil.log(e, "Fehler beim Speichern der Datei.");
			}
        }
        
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

	
	public CnATreeElement getCurrentCnaElement() {
		return currentCnaElement;
	}

	public void setCurrentCnaElement(CnATreeElement currentCnaElement) {
		this.currentCnaElement = currentCnaElement;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		getSite().getPage().removePostSelectionListener(selectionListener);
	}
	
	private static class AttachmentLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			if (element instanceof PlaceHolder) {
				return null;
			}
			Attachment attachment = (Attachment) element;
			if(columnIndex==0) {
				String mimeType = (attachment.getMimeType()!=null)?attachment.getMimeType().toLowerCase():null;
				if(Arrays.asList(Attachment.ARCHIVE_MIME_TYPES).contains(mimeType)) {
					return ImageCache.getInstance().getImageDescriptor(ImageCache.MIME_ARCHIVE).createImage();
				} else if(Arrays.asList(Attachment.AUDIO_MIME_TYPES).contains(mimeType)) {
					return ImageCache.getInstance().getImageDescriptor(ImageCache.MIME_AUDIO).createImage();
				} else if(Arrays.asList(Attachment.DOCUMENT_MIME_TYPES).contains(mimeType)) {
					return ImageCache.getInstance().getImageDescriptor(ImageCache.MIME_DOCUMENT).createImage();
				} else if(Arrays.asList(Attachment.HTML_MIME_TYPES).contains(mimeType)) {
					return ImageCache.getInstance().getImageDescriptor(ImageCache.MIME_HTML).createImage();
				} else if(Arrays.asList(Attachment.IMAGE_MIME_TYPES).contains(mimeType)) {
					return ImageCache.getInstance().getImageDescriptor(ImageCache.MIME_IMAGE).createImage();
				} else if(Arrays.asList(Attachment.PDF_MIME_TYPES).contains(mimeType)) {
					return ImageCache.getInstance().getImageDescriptor(ImageCache.MIME_PDF).createImage();
				}else if(Arrays.asList(Attachment.PRESENTATION_MIME_TYPES).contains(mimeType)) {
					return ImageCache.getInstance().getImageDescriptor(ImageCache.MIME_PRESENTATION).createImage();
				}else if(Arrays.asList(Attachment.SPREADSHEET_MIME_TYPES).contains(mimeType)) {
					return ImageCache.getInstance().getImageDescriptor(ImageCache.MIME_SPREADSHEET).createImage();
				}else if(Arrays.asList(Attachment.TEXT_MIME_TYPES).contains(mimeType)) {
					return ImageCache.getInstance().getImageDescriptor(ImageCache.MIME_TEXT).createImage();
				}else if(Arrays.asList(Attachment.VIDEO_MIME_TYPES).contains(mimeType)) {
					return ImageCache.getInstance().getImageDescriptor(ImageCache.MIME_VIDEO).createImage();
				}else if(Arrays.asList(Attachment.XML_MIME_TYPES).contains(mimeType)) {
					return ImageCache.getInstance().getImageDescriptor(ImageCache.MIME_XML).createImage();
				}			
				return ImageCache.getInstance().getImageDescriptor(ImageCache.MIME_UNKNOWN).createImage();
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			try {
				if (element instanceof PlaceHolder) {
					if (columnIndex == 1) {
						PlaceHolder ph = (PlaceHolder) element;
						return ph.getTitle();
					}
					return "";
				}
				DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
				Attachment attachment = (Attachment) element;
				switch(columnIndex) {
				case 1: 
					return attachment.getFileName(); //$NON-NLS-1$
				case 2: 
					return attachment.getMimeType(); //$NON-NLS-1$
				case 3: 
					return attachment.getText(); //$NON-NLS-1$
				case 4: 				 
					return (attachment.getDate()!=null) ? dateFormat.format(attachment.getDate()) : null; //$NON-NLS-1$
				case 5: 
					return attachment.getVersion(); //$NON-NLS-1$
				default: 
					return null;
				}
			} catch (Exception e) {
				LOG.error("Error while getting column text", e);
				throw new RuntimeException(e);
			}
		}
		
	}
}
