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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.RightsEnabledAction;
import sernet.gs.ui.rcp.main.bsi.editors.AttachmentEditor;
import sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditorInput;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.DeleteNote;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.iso27k.rcp.ILinkedWithEditorView;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.rcp.LinkWithEditorPartListener;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.AttachmentFile;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.service.commands.LoadAttachmentFile;
import sernet.verinice.service.commands.LoadAttachments;

/**
 * Lists files {@link Attachment} attached to a CnATreeElement. User can view,
 * save, delete and add files by toolbar buttons.
 * 
 * @see AttachmentEditor - Editor for metadata of files
 * @see LoadAttachments - Command for loading files
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class FileView extends ViewPart implements ILinkedWithEditorView, IPropertyChangeListener {
    
    static final Logger LOG = Logger.getLogger(FileView.class);

    public static final String ID = "sernet.gs.ui.rcp.main.bsi.views.FileView"; //$NON-NLS-1$
    
    private static final int DEFAULT_THUMBNAIL_SIZE = 0;

    private static Map<String, String> mimeImageMap = new Hashtable<String, String>();
    static {
        for (int i = 0; i < Attachment.getArchiveMimeTypes().length; i++) {
            mimeImageMap.put(Attachment.getArchiveMimeTypes()[i], ImageCache.MIME_ARCHIVE);
        }
        for (int i = 0; i < Attachment.getAudioMimeTypes().length; i++) {
            mimeImageMap.put(Attachment.getAudioMimeTypes()[i], ImageCache.MIME_AUDIO);
        }
        for (int i = 0; i < Attachment.getDocumentMimeTypes().length; i++) {
            mimeImageMap.put(Attachment.getDocumentMimeTypes()[i], ImageCache.MIME_DOCUMENT);
        }
        for (int i = 0; i < Attachment.getHtmlMimeTypes().length; i++) {
            mimeImageMap.put(Attachment.getHtmlMimeTypes()[i], ImageCache.MIME_HTML);
        }
        for (int i = 0; i < Attachment.getImageMimeTypes().length; i++) {
            mimeImageMap.put(Attachment.getImageMimeTypes()[i], ImageCache.MIME_IMAGE);
        }
        for (int i = 0; i < Attachment.getPdfMimeTypes().length; i++) {
            mimeImageMap.put(Attachment.getPdfMimeTypes()[i], ImageCache.MIME_PDF);
        }
        for (int i = 0; i < Attachment.getPresentationMimeTypes().length; i++) {
            mimeImageMap.put(Attachment.getPresentationMimeTypes()[i], ImageCache.MIME_PRESENTATION);
        }
        for (int i = 0; i < Attachment.getSpreadsheetMimeTypes().length; i++) {
            mimeImageMap.put(Attachment.getSpreadsheetMimeTypes()[i], ImageCache.MIME_SPREADSHEET);
        }
        for (int i = 0; i < Attachment.getTextMimeTypes().length; i++) {
            mimeImageMap.put(Attachment.getTextMimeTypes()[i], ImageCache.MIME_TEXT);
        }
        for (int i = 0; i < Attachment.getVideoMimeTypes().length; i++) {
            mimeImageMap.put(Attachment.getVideoMimeTypes()[i], ImageCache.MIME_VIDEO);
        }
        for (int i = 0; i < Attachment.getXmlMimeTypes().length; i++) {
            mimeImageMap.put(Attachment.getXmlMimeTypes()[i], ImageCache.MIME_XML);
        }

    }

    private ICommandService commandService;

    private TableViewer viewer;

    private TableViewerColumn imageColumn;

    
    
    
    
    private TableSorter tableSorter = new TableSorter();

    private List<Attachment> attachmentList;

    private AttachmentContentProvider contentProvider = new AttachmentContentProvider(this);

    private ISelectionListener selectionListener;

    private RightsEnabledAction addFileAction;

    private RightsEnabledAction deleteFileAction;

    private Action doubleClickAction;

    private Action saveCopyAction;

    private Action openAction;

    private Action toggleLinkAction;

    private boolean isLinkingActive = true;

    private CnATreeElement currentCnaElement;

    private IModelLoadListener modelLoadListener;

    private IPartListener2 linkWithEditorPartListener = new LinkWithEditorPartListener(this);

    private AttachmentImageCellProvider imageCellProvider = null;
    
    public FileView() {
        super();
        Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
    }

    public String getRightID() {
        return ActionRightIDs.FILES;
    }

    @Override
    public void createPartControl(Composite parent) {
        initView(parent);
    }

    private void initView(Composite parent) {
        parent.setLayout(new FillLayout());
        try {
            createTable(parent);
            getSite().setSelectionProvider(viewer);
            hookPageSelection();
            viewer.setInput(new PlaceHolder(Messages.FileView_0));
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.BrowserView_3);
            LOG.error("Error while creating control", e); //$NON-NLS-1$
        }
        makeActions();
        hookActions();
        hookDND();
        fillLocalToolBar();

        getSite().getPage().addPartListener(linkWithEditorPartListener);
    }

    /**
	 * 
	 */
    private void hookDND() {
        new FileDropTarget(this);
    }

    private void createTable(Composite parent) {
        TableColumn iconColumn;
        TableColumn fileNameColumn;
        TableColumn mimeTypeColumn;
        TableColumn textColumn;
        TableColumn dateColumn;
        TableColumn versionColumn;
        
        final int widthHeightPadding = 4;
        final int itemColumnWidth = 26;
        final int filenameColumnWidth = 152;
        final int mimeTypeColumnWidth = 50;
        final int textColumnWidth = 250;
        final int dateColumnWidth = 120;
        final int versionColumnWidth = 60;
        
        viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(new AttachmentLabelProvider());
        Table table = viewer.getTable();
        
        table.addListener(SWT.MeasureItem, new Listener() {   
            public void handleEvent(Event event) {
               // height cannot be per row so simply set
               event.height = getThumbnailSize() + widthHeightPadding;
            }
         });
           
        imageColumn = new TableViewerColumn(viewer, SWT.LEFT);
        imageColumn.setLabelProvider(getImageCellProvider());
        if(getThumbnailSize()>0) {         
            imageColumn.getColumn().setWidth(getThumbnailSize() + widthHeightPadding);          
        } else {
            // dummy column
            imageColumn.getColumn().setWidth(0);
        }
        
        iconColumn = new TableColumn(table, SWT.LEFT);
        iconColumn.setWidth(itemColumnWidth);
        iconColumn.addSelectionListener(new SortSelectionAdapter(this, iconColumn, 0));

        fileNameColumn = new TableColumn(table, SWT.LEFT);
        fileNameColumn.setText(Messages.FileView_2);
        fileNameColumn.setWidth(filenameColumnWidth);
        fileNameColumn.addSelectionListener(new SortSelectionAdapter(this, fileNameColumn, 1));
        
        mimeTypeColumn = new TableColumn(table, SWT.LEFT);
        mimeTypeColumn.setText(Messages.FileView_3);
        mimeTypeColumn.setWidth(mimeTypeColumnWidth);
        mimeTypeColumn.addSelectionListener(new SortSelectionAdapter(this, mimeTypeColumn, 2));

        textColumn = new TableColumn(table, SWT.LEFT);
        textColumn.setText(Messages.FileView_4);
        textColumn.setWidth(textColumnWidth);
        textColumn.addSelectionListener(new SortSelectionAdapter(this, textColumn, 3));

        dateColumn = new TableColumn(table, SWT.LEFT);
        dateColumn.setText(Messages.FileView_5);
        dateColumn.setWidth(dateColumnWidth);
        dateColumn.addSelectionListener(new SortSelectionAdapter(this, dateColumn, widthHeightPadding));

        versionColumn = new TableColumn(table, SWT.LEFT);
        versionColumn.setText(Messages.FileView_6);
        versionColumn.setWidth(versionColumnWidth);
        versionColumn.addSelectionListener(new SortSelectionAdapter(this, versionColumn, 5));

        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        viewer.setSorter(tableSorter);

    }

    /**
     * @return
     */
    private CellLabelProvider getImageCellProvider() {
        if(imageCellProvider==null) {
            imageCellProvider = new AttachmentImageCellProvider(getThumbnailSize());
        }
        return imageCellProvider;
    }

    /**
     * @return
     */
    private int getThumbnailSize() {
        int size = DEFAULT_THUMBNAIL_SIZE;
        String sizeString = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.THUMBNAIL_SIZE);
        if(sizeString!=null && !sizeString.isEmpty()) {
            size = Integer.parseInt(sizeString);
        }    
        return size;
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
            openAction.setEnabled(element != null);
            saveCopyAction.setEnabled(element != null);
            deleteFileAction.setEnabled(element != null && deleteFileAction.checkRights());
            return;
        }

        if (!(selection instanceof IStructuredSelection)) {
            openAction.setEnabled(false);
            saveCopyAction.setEnabled(false);
            deleteFileAction.setEnabled(false);
            return;
        }
        elementSelected(element);
        if (element instanceof CnATreeElement) {
            setNewInput((CnATreeElement) element);
        }
    }

    protected void elementSelected(Object element) {
        try {
            if (element instanceof CnATreeElement) {
                addFileAction.setEnabled(addFileAction.checkRights());
                setCurrentCnaElement((CnATreeElement) element);
                loadFiles();

            } else {
                addFileAction.setEnabled(false);
            }
            Attachment att = (Attachment) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
            openAction.setEnabled(att != null);
            saveCopyAction.setEnabled(att != null);
            deleteFileAction.setEnabled(att != null && deleteFileAction.checkRights());

        } catch (Exception e) {
            LOG.error("Error while loading notes", e); //$NON-NLS-1$
        }
    }

    protected void startInitDataJob() {
        WorkspaceJob initDataJob = new WorkspaceJob(sernet.verinice.iso27k.rcp.Messages.ISMView_InitData) {
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask(sernet.verinice.iso27k.rcp.Messages.ISMView_InitData, IProgressMonitor.UNKNOWN);
                    Activator.inheritVeriniceContextState();
                    loadFiles();
                } catch (Exception e) {
                    LOG.error("Error while loading data.", e); //$NON-NLS-1$
                    status = new Status(Status.ERROR, "sernet.gs.ui.rcp.main", "Error while loading data.", e); //$NON-NLS-1$ //$NON-NLS-2$
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
            if(isLinkingActive()) {
                if(getCurrentCnaElement() != null) {
                    id = getCurrentCnaElement().getDbId();
                } else {
                    return;
                }
            }
            LoadAttachments command = new LoadAttachments(id);
            command = getCommandService().executeCommand(command);
            attachmentList = command.getAttachmentList();
            if (attachmentList != null) {
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        viewer.setInput(attachmentList);
                    }
                });
                for (final Attachment attachment : attachmentList) {
                    // set transient cna-element-titel
                    if (getCurrentCnaElement() != null) {
                        attachment.setCnAElementTitel(getCurrentCnaElement().getTitle());
                    }
                    attachment.addListener(new Attachment.INoteChangedListener() {
                        public void noteChanged() {
                            loadFiles();
                        }
                    });
                }
            }

        } catch (Exception e) {
            LOG.error("Error while loading attachment", e); //$NON-NLS-1$
            ExceptionUtil.log(e, "Error while attachment notes"); //$NON-NLS-1$
        }
    }

    protected void editFile(Attachment attachment) {
        EditorFactory.getInstance().openEditor(attachment);
    }

    protected void deleteFile(Attachment attachment) {
        DeleteNote command = new DeleteNote(attachment);
        try {
            getCommandService().executeCommand(command);
        } catch (CommandException e) {
            LOG.error("Error while saving attachment", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.FileView_13);
        }
        loadFiles();
    }

    /**
     * Passing the focus request to the viewer's control.
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent changeEvent) {
        final int thumbnailWidthPadding = 4;
        if(changeEvent.getProperty().equals(PreferenceConstants.THUMBNAIL_SIZE) && imageCellProvider!=null
                && !changeEvent.getNewValue().equals(changeEvent.getOldValue())) {
            imageCellProvider.setThumbSize(Integer.valueOf(changeEvent.getNewValue().toString()));
            imageCellProvider.clearCache();
            if(getThumbnailSize()>0) {
                imageColumn.getColumn().setWidth(getThumbnailSize() + thumbnailWidthPadding);
            } else {
                imageColumn.getColumn().setWidth(0);
            }
            loadFiles();
        }
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
        addFileAction = new RightsEnabledAction(ActionRightIDs.ADDFILE) {
            public void run() {
                FileDialog fd = new FileDialog(FileView.this.getSite().getShell());
                fd.setText(Messages.FileView_14);
                fd.setFilterPath(System.getProperty("user.home")); //$NON-NLS-1$
                String selected = fd.open();
                if (selected != null && selected.length() > 0 && !createAndOpenAttachment(selected)){
                    return;
                }
            }
        };
        addFileAction.setText(Messages.FileView_16);
        addFileAction.setToolTipText(Messages.FileView_17);
        addFileAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.NOTE_NEW));
        addFileAction.setEnabled(false);

        deleteFileAction = new RightsEnabledAction(ActionRightIDs.DELETEFILE) {
            public void run() {
                int count = ((IStructuredSelection) viewer.getSelection()).size();
                boolean confirm = MessageDialog.openConfirm(getViewer().getControl().getShell(), Messages.FileView_18, NLS.bind(Messages.FileView_19, count));
                if (!confirm){
                    return;
                }
                deleteAttachments();
                loadFiles();
            }
        };
        deleteFileAction.setText(Messages.FileView_23);
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

        toggleLinkAction = new RightsEnabledAction(ActionRightIDs.SHOWALLFILES, Messages.FileView_24, SWT.TOGGLE){
            public void run() {
                isLinkingActive = !isLinkingActive;
                toggleLinkAction.setChecked(isLinkingActive());
                checkModelAndLoadFiles();
            }
        };
        toggleLinkAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.LINKED));
        toggleLinkAction.setChecked(false);
    }

    private void openFile() {
        Attachment attachment = (Attachment) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
        if (attachment != null) {
            try {
                LoadAttachmentFile command = new LoadAttachmentFile(attachment.getDbId());
                command = getCommandService().executeCommand(command);
                AttachmentFile attachmentFile = command.getAttachmentFile();
                String tempDir = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
                if (attachmentFile != null && tempDir != null) {
                   if (!tempDir.endsWith(String.valueOf(File.separatorChar))) {
                        tempDir = tempDir + File.separatorChar;
                    }
                    String path = tempDir + attachment.getFileName();
                    try {
                        attachmentFile.writeFileData(path);
                        Program.launch(path);
                    } catch (IOException e) {
                        LOG.error("Error while loading attachment", e); //$NON-NLS-1$
                        ExceptionUtil.log(e, Messages.FileView_27);
                    }
                }
            } catch (Exception e) {
                LOG.error("Error while loading attachment", e); //$NON-NLS-1$
                ExceptionUtil.log(e, Messages.FileView_27);
            }
        }
    }

    protected void saveCopy(Attachment attachment) {
        FileDialog fd = new FileDialog(FileView.this.getSite().getShell(), SWT.SAVE);
        fd.setText(Messages.FileView_30);
        fd.setFilterPath("~"); //$NON-NLS-1$
        fd.setFileName(attachment.getFileName());
        String selected = fd.open();
        if (selected != null) {
            try {
                LoadAttachmentFile command = new LoadAttachmentFile(attachment.getDbId());
                command = getCommandService().executeCommand(command);
                AttachmentFile attachmentFile = command.getAttachmentFile();
                attachmentFile.writeFileData(selected);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("File saved: " + selected); //$NON-NLS-1$
                }
            } catch (Exception e) {
                LOG.error("Error while saving file", e); //$NON-NLS-1$
                ExceptionUtil.log(e, Messages.FileView_34);
            }
        }

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
        getSite().getPage().removePartListener(linkWithEditorPartListener);
        Activator.getDefault().getPreferenceStore().removePropertyChangeListener(this);
        if(attachmentList!=null) {
            for (Attachment attachment : attachmentList) {
                attachment.removeAllListener();
            }
        }
        if(imageCellProvider!=null) {
            imageCellProvider.shutdownCache();
        }
    }

    public static String getImageForMimeType(String mimeType) {
        return mimeImageMap.get(mimeType);
    }

    public static Display getDisplay() {
        Display display = Display.getCurrent();
        //may be null if outside the UI thread
        if (display == null){
           display = Display.getDefault();
        }
        return display;       
     }
    
    private static class AttachmentLabelProvider extends LabelProvider implements ITableLabelProvider {      
        
        public Image getColumnImage(Object element, int columnIndex) {
            if (element instanceof PlaceHolder) {
                return null;
            }
            Attachment attachment = (Attachment) element;
            if (columnIndex == 1) {
                String mimeType = (attachment.getMimeType() != null) ? attachment.getMimeType().toLowerCase() : "";
                String imageType = mimeImageMap.get(mimeType);
                if (imageType != null) {
                    return ImageCache.getInstance().getImageDescriptor(mimeImageMap.get(mimeType)).createImage();
                }
                return ImageCache.getInstance().getImage(ImageCache.UNKNOWN_FILE_TYPE);
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
                    return ""; //$NON-NLS-1$
                }
                DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                Attachment attachment = (Attachment) element;
                switch (columnIndex) {
                case 2:
                    return attachment.getTitel(); //$NON-NLS-1$
                case 3:
                    return attachment.getMimeType(); //$NON-NLS-1$
                case 4:
                    return attachment.getText(); //$NON-NLS-1$
                case 5:
                    return (attachment.getDate() != null) ? dateFormat.format(attachment.getDate()) : null; //$NON-NLS-1$
                case 6:
                    return attachment.getVersion(); //$NON-NLS-1$
                default:
                    return null;
                }
            } catch (Exception e) {
                LOG.error("Error while getting column text", e); //$NON-NLS-1$
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
            super();
            this.propertyIndex = DEFAULT_SORT_COLUMN;
            this.direction = ASCENDING;
        }

        public void setColumn(int column) {
            if (column == this.propertyIndex) {
                // Same column as last sort; toggle the direction
                direction = (direction == ASCENDING) ? DESCENDING : ASCENDING;
            } else {
                // New column; do an ascending sort
                this.propertyIndex = column;
                direction = ASCENDING;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface
         * .viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            Attachment a1 = (Attachment) e1;
            Attachment a2 = (Attachment) e2;
            int rc = 0;
            if (e1 == null && e2 != null) {
                rc = 1;
            } else if (e2 == null && e1 != null) {
                rc = -1;
            } else {
                // e1 and e2 != null
                rc = compareNullSafe(a1, a2);
            }
            // If descending order, flip the direction
            if (direction == DESCENDING) {
                rc = -rc;
            }
            return rc;
        }

        private int compareNullSafe(Attachment a1, Attachment a2) {
            int rc = 0;
            switch (propertyIndex) {
            case 0:
                String mimeType1 = a1.getMimeType();
                String mimeType2 = a2.getMimeType();
                if (mimeType1 == null || mimeType2 == null){
                    return 0;
                }
                String image1 = mimeImageMap.get(mimeType1);
                String image2 = mimeImageMap.get(mimeType2);
                if (image1 != null && image2 != null) {
                    rc = image1.compareTo(image2);
                }
                break;
            case 1:
                rc = a1.getFileName().compareTo(a2.getFileName());
                break;
            case 2:
                mimeType1 = a1.getMimeType();
                mimeType2 = a2.getMimeType();
                if (mimeType1 == null || mimeType2 == null){
                    return 0;
                }
                rc = mimeType1.compareTo(mimeType2);
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
            return rc;
        }

    }

    private static class SortSelectionAdapter extends SelectionAdapter {
        private FileView fileView;
        private TableColumn column;
        private int index;

        public SortSelectionAdapter(FileView fileView, TableColumn column, int index) {
            super();
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.rcp.ILinkedWithEditorView#editorActivated(org.
     * eclipse.ui.IEditorPart)
     */
    @Override
    public void editorActivated(IEditorPart editor) {
        if (!isLinkingActive() || !getViewSite().getPage().isPartVisible(this) || editor == null) {
            return;
        }
        CnATreeElement element = BSIElementEditorInput.extractElement(editor);
        if (element == null) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Element in editor :" + element.getUuid());
            LOG.debug("Loading attached files of element now..."); //$NON-NLS-1$
        }
        elementSelected(element);

    }

    /**
     * @return
     */
    private boolean isLinkingActive() {
        return isLinkingActive;
    }

    private void setNewInput(CnATreeElement elmt) {
        setViewTitle(Messages.FileView_7 + " " + elmt.getTitle());
    }

    private void setViewTitle(String title) {
        this.setContentDescription(title);
    }

    private boolean createAndOpenAttachment(String selected) {
        File file = new File(selected);
        if (file.isDirectory()){
            return false;
        }
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
        return true;
    }

    private void deleteAttachments() {
        Iterator iterator = ((IStructuredSelection) viewer.getSelection()).iterator();
        while (iterator.hasNext()) {
            Attachment sel = (Attachment) iterator.next();
            DeleteNote command = new DeleteNote(sel);
            try {
                command = getCommandService().executeCommand(command);
            } catch (CommandException e) {
                LOG.error("Error while saving note", e); //$NON-NLS-1$
                ExceptionUtil.log(e, Messages.FileView_22);
            }
        }
    }

    private void checkModelAndLoadFiles() {
        if (CnAElementFactory.isModelLoaded()) {
            loadFiles();
        } else if (modelLoadListener == null) {
            // model is not loaded yet: add a listener to load data when
            // it's laoded
            modelLoadListener = new IModelLoadListener() {

                public void closed(BSIModel model) {
                    // nothing to do
                }

                public void loaded(BSIModel model) {
                    startInitDataJob();
                }

                @Override
                public void loaded(ISO27KModel model) {
                    // work is done in loaded(BSIModel model)
                }

            };
            CnAElementFactory.getInstance().addLoadListener(modelLoadListener);
        }
    }

}
