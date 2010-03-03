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
package sernet.gs.ui.rcp.main.bsi.views;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.editors.AttachmentEditor;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.model.Attachment;
import sernet.gs.ui.rcp.main.bsi.model.AttachmentFile;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.DeleteNote;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadAttachmentFile;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadAttachments;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.rcp.StatusResult;

/**
 * Lists files {@link Attachment} attached to a CnATreeElement.
 * User can view, save, delete and add files by toolbar buttons.
 * 
 * @see AttachmentEditor - Editor for metadata of files
 * @see LoadAttachments - Command for loading files
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class FileView extends ViewPart {

	private static final Logger LOG = Logger.getLogger(FileView.class);
	
	public static final String ID = "sernet.gs.ui.rcp.main.bsi.views.FileView"; //$NON-NLS-1$
	
	private static Map<String, String> mimeImageMap = new Hashtable<String, String>();
	static {
		for (int i = 0; i < Attachment.ARCHIVE_MIME_TYPES.length; i++) {
			mimeImageMap.put(Attachment.ARCHIVE_MIME_TYPES[i], ImageCache.MIME_ARCHIVE);
		}
		for (int i = 0; i < Attachment.AUDIO_MIME_TYPES.length; i++) {
			mimeImageMap.put(Attachment.AUDIO_MIME_TYPES[i], ImageCache.MIME_AUDIO);
		}
		for (int i = 0; i < Attachment.DOCUMENT_MIME_TYPES.length; i++) {
			mimeImageMap.put(Attachment.DOCUMENT_MIME_TYPES[i], ImageCache.MIME_DOCUMENT);
		}
		for (int i = 0; i < Attachment.HTML_MIME_TYPES.length; i++) {
			mimeImageMap.put(Attachment.HTML_MIME_TYPES[i], ImageCache.MIME_HTML);
		}
		for (int i = 0; i < Attachment.IMAGE_MIME_TYPES.length; i++) {
			mimeImageMap.put(Attachment.IMAGE_MIME_TYPES[i], ImageCache.MIME_IMAGE);
		}
		for (int i = 0; i < Attachment.PDF_MIME_TYPES.length; i++) {
			mimeImageMap.put(Attachment.PDF_MIME_TYPES[i], ImageCache.MIME_PDF);
		}
		for (int i = 0; i < Attachment.PRESENTATION_MIME_TYPES.length; i++) {
			mimeImageMap.put(Attachment.PRESENTATION_MIME_TYPES[i], ImageCache.MIME_PRESENTATION);
		}
		for (int i = 0; i < Attachment.SPREADSHEET_MIME_TYPES.length; i++) {
			mimeImageMap.put(Attachment.SPREADSHEET_MIME_TYPES[i], ImageCache.MIME_SPREADSHEET);
		}
		for (int i = 0; i < Attachment.TEXT_MIME_TYPES.length; i++) {
			mimeImageMap.put(Attachment.TEXT_MIME_TYPES[i], ImageCache.MIME_TEXT);
		}
		for (int i = 0; i < Attachment.VIDEO_MIME_TYPES.length; i++) {
			mimeImageMap.put(Attachment.VIDEO_MIME_TYPES[i], ImageCache.MIME_VIDEO);
		}
		for (int i = 0; i < Attachment.XML_MIME_TYPES.length; i++) {
			mimeImageMap.put(Attachment.XML_MIME_TYPES[i], ImageCache.MIME_XML);
		}
		
	}
	
	private ICommandService	commandService;
	
	private Composite parent;
	
	protected TableViewer viewer;
	protected TableColumn iconColumn;
	protected TableColumn fileNameColumn;
	protected TableColumn mimeTypeColumn;
	protected TableColumn textColumn;
	protected TableColumn dateColumn;
	protected TableColumn versionColumn;
	TableSorter tableSorter = new TableSorter();
	
	private AttachmentContentProvider contentProvider = new AttachmentContentProvider(this);
	
	private ISelectionListener selectionListener;

	private Action addFileAction;

	private Action deleteFileAction;

	private Action doubleClickAction;
	
	private Action saveCopyAction;
	
	private Action openAction;
	
	private Action toggleLinkAction;
	
	private boolean linkToElements = true;
	
	private CnATreeElement currentCnaElement;
	
	private IModelLoadListener modelLoadListener;
	
	public FileView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		initView(parent);
	}

	private void initView(Composite parent) {
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
		hookDND();
		fillLocalToolBar();
	}

	/**
	 * 
	 */
	private void hookDND() {
		new FileDropTarget(this);
	}

	private void createTable(Composite parent) {
		viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new AttachmentLabelProvider());
		Table table = viewer.getTable();
		iconColumn = new TableColumn(table, SWT.LEFT);;
		iconColumn.setWidth(26);
		iconColumn.addSelectionListener(new SortSelectionAdapter(this,iconColumn,0));
		
		fileNameColumn = new TableColumn(table, SWT.LEFT);
		fileNameColumn.setText("Name");
		fileNameColumn.setWidth(120);
		fileNameColumn.addSelectionListener(new SortSelectionAdapter(this,fileNameColumn,1));
		
		mimeTypeColumn = new TableColumn(table, SWT.LEFT);
		mimeTypeColumn.setText("Typ");
		mimeTypeColumn.setWidth(50);
		mimeTypeColumn.addSelectionListener(new SortSelectionAdapter(this,mimeTypeColumn,2));
		
		textColumn = new TableColumn(table, SWT.LEFT);
		textColumn.setText("Beschreibung");
		textColumn.setWidth(350);
		textColumn.addSelectionListener(new SortSelectionAdapter(this,textColumn,3));
		
		dateColumn = new TableColumn(table, SWT.LEFT);
		dateColumn.setText("Datum");
		dateColumn.setWidth(120);
		dateColumn.addSelectionListener(new SortSelectionAdapter(this,dateColumn,4));
		
		versionColumn = new TableColumn(table, SWT.LEFT);
		versionColumn.setText("Version");
		versionColumn.setWidth(60);
		versionColumn.addSelectionListener(new SortSelectionAdapter(this,versionColumn,5));
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		viewer.setSorter(tableSorter);
		
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
	
	protected void startInitDataJob() {
		WorkspaceJob initDataJob = new WorkspaceJob(Messages.ISMView_InitData) {
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				IStatus status = Status.OK_STATUS;
				try {
					monitor.beginTask(Messages.ISMView_InitData, IProgressMonitor.UNKNOWN);
					Activator.inheritVeriniceContextState();
					loadFiles();
				} catch (Exception e) {
					LOG.error("Error while loading data.", e);
					status= new Status(Status.ERROR, "sernet.gs.ui.rcp.main", "Error while loading data.",e); //$NON-NLS-1$
				} finally {
					monitor.done();
				}
				return status;
			}
		};
		JobScheduler.scheduleInitJob(initDataJob);
	}
	
	public void loadFiles() {
		try {
			Integer id = null;
			if(linkToElements && getCurrentCnaElement()!=null) {
				id = getCurrentCnaElement().getDbId();
			}
			LoadAttachments command = new LoadAttachments(id);		
			command = getCommandService().executeCommand(command);		
			final List<Attachment> attachmentList = command.getAttachmentList();
			if(attachmentList!=null) {
				Display.getDefault().syncExec(new Runnable(){
					public void run() {
						viewer.setInput(attachmentList);
					}
				});
				for (final Attachment attachment : attachmentList) {
					// set transient cna-element-titel
					if(getCurrentCnaElement()!=null) {
						attachment.setCnAElementTitel(getCurrentCnaElement().getTitle());
					}
					attachment.addListener(new Attachment.INoteChangedListener() {
						public void noteChanged() {
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
		loadFiles();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	
	
	private void fillLocalToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		manager.add(this.toggleLinkAction);
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
		        fd.setFilterPath(System.getProperty("user.home"));
		        String selected = fd.open();
		        if(selected!=null && selected.length()>0) {
		        	File file = new File(selected);
		    		if (file.isDirectory())
		    			return;
		    		
					Attachment attachment = new Attachment();
					attachment.setCnATreeElementId(getCurrentCnaElement().getDbId());
					attachment.setCnAElementTitel(getCurrentCnaElement().getTitle());
					attachment.setTitel(file.getName());
					attachment.setDate(Calendar.getInstance().getTime());
					attachment.setFilePath(selected);
					attachment.addListener(new Attachment.INoteChangedListener() {
						public void noteChanged() {
							loadFiles();
						}
					});
					EditorFactory.getInstance().openEditor(attachment);	
		        }
			}
		};
		addFileAction.setText("Datei hinzufügen...");
		addFileAction.setToolTipText("Datei hinzufügen (oder einfach Dateien per DragnDrop in dieses Fenster ziehen)");
		addFileAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.NOTE_NEW));
		addFileAction.setEnabled(false);
		
		deleteFileAction = new Action() {
			public void run() {
				int count = ((IStructuredSelection) viewer.getSelection()).size();
				boolean confirm = MessageDialog.openConfirm(getViewer().getControl().getShell(), "Wirklich löschen?", 
						"Wollen Sie die markierten " + count + " Attachments wirklich löschen?");
				if (!confirm)
					return;
				
				Iterator iterator = ((IStructuredSelection) viewer.getSelection()).iterator();
				while (iterator.hasNext()) {
					Attachment sel = (Attachment) iterator.next();
					DeleteNote command = new DeleteNote(sel);		
					try {
						command = getCommandService().executeCommand(command);
					} catch (CommandException e) {
						LOG.error("Error while saving note", e);
						ExceptionUtil.log(e, "Fehler beim Löschen der Notiz.");
					}
				}
				
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
		
		toggleLinkAction = new Action("Link to elements", SWT.TOGGLE) {
			public void run() {
				linkToElements=!linkToElements;
				toggleLinkAction.setChecked(linkToElements);
				if(CnAElementFactory.isModelLoaded()) {
					loadFiles();
				} else if(modelLoadListener==null) {
					// model is not loaded yet: add a listener to load data when it's laoded
					modelLoadListener = new IModelLoadListener() {

						public void closed(BSIModel model) {
							// nothing to do
						}

						public void loaded(BSIModel model) {
							startInitDataJob();
						}
						
					};
					CnAElementFactory.getInstance().addLoadListener(modelLoadListener);
				}
			}
		};
		toggleLinkAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.LINKED));
		toggleLinkAction.setChecked(linkToElements);
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
				ExceptionUtil.log(e, "Error while attaching notes");
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
	
	
	public static String getImageForMimeType(String mimeType) {
		return mimeImageMap.get(mimeType);
	}
	
	private static class AttachmentLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			if (element instanceof PlaceHolder) {
				return null;
			}
			Attachment attachment = (Attachment) element;
			if(columnIndex==0) {
				String mimeType = (attachment.getMimeType()!=null) ? attachment.getMimeType().toLowerCase() : "";			
				return ImageCache.getInstance().getImageDescriptor(mimeImageMap.get(mimeType)).createImage();
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
	
	private static class TableSorter extends ViewerSorter {
		private int propertyIndex;
		private static final int DEFAULT_SORT_COLUMN = 0;
		private static final int DESCENDING = 1;
		private static final int ASCENDING = 0;
		private int direction = ASCENDING;

		public TableSorter() {
			this.propertyIndex = DEFAULT_SORT_COLUMN;
			this.direction = ASCENDING;
		}

		public void setColumn(int column) {
			if (column == this.propertyIndex) {
				// Same column as last sort; toggle the direction
				direction = (direction==ASCENDING) ? DESCENDING : ASCENDING;
			} else {
				// New column; do an ascending sort
				this.propertyIndex = column;
				direction = ASCENDING;
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			Attachment a1 = (Attachment) e1;
			Attachment a2 = (Attachment) e2;
			int rc = 0;
			if(e1==null) {
				if(e2!=null) {
					rc = 1;
				}
			} else if(e2==null) {
				if(e1!=null) {
					rc = -1;
				}
			} else {
				// e1 and e2 != null	
				switch (propertyIndex) {
				case 0:
					String mimeType1 = a1.getMimeType();
					String mimeType2 = a2.getMimeType();
					if (mimeType1 == null || mimeType2 == null)
						return 0;
					String image1 = mimeImageMap.get(mimeType1);
					String image2 = mimeImageMap.get(mimeType2);				
					if(image1!=null && image2!=null) {
						rc = image1.compareTo(image2);
					}
					break;
				case 1:
					rc = a1.getFileName().compareTo(a2.getFileName());
					break;
				case 2:
					rc = a1.getMimeType().compareTo(a2.getMimeType());
					break;
				case 3:
					rc = a1.getText().compareTo(a2.getText());
					break;
				case 4:
					rc = a1.getDate().compareTo(a2.getDate());
					break;
				case 5:
					rc = a1.getVersion().compareTo(a2.getVersion());
					break;
				default:
					rc = 0;
				}
			}
			// If descending order, flip the direction
			if (direction == DESCENDING) {
				rc = -rc;
			}
			return rc;
		}

	}
	
	private static class SortSelectionAdapter extends SelectionAdapter {
		FileView fileView;
		TableColumn column;
		int index;
		
		public SortSelectionAdapter(FileView fileView, TableColumn column, int index) {
			this.fileView = fileView;
			this.column = column;
			this.index = index;
		}
	
		@Override
		public void widgetSelected(SelectionEvent e) {
			fileView.tableSorter.setColumn(index);
			int dir = fileView.viewer.getTable().getSortDirection();
			if (fileView.viewer.getTable().getSortColumn() == column) {
				dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
			} else {

				dir = SWT.DOWN;
			}
			fileView.viewer.getTable().setSortDirection(dir);
			fileView.viewer.getTable().setSortColumn(column);
			fileView.viewer.refresh();
		}

	}

	/**
	 * @return
	 */
	public TableViewer getViewer() {
		return this.viewer;
	}
}
