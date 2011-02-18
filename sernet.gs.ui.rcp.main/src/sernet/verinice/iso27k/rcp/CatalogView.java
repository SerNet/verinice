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
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.service.CsvFile;
import sernet.gs.service.VeriniceCharset;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.DeleteNote;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadAttachmentFile;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadAttachments;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveAttachment;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveNote;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.iso27k.IItem;
import sernet.verinice.iso27k.rcp.action.ControlDragListener;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.AttachmentFile;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.rcp.IAttachedToPerspective;
import sernet.verinice.service.commands.LoadBSIModel;
import sernet.verinice.service.iso27k.ImportCatalog;
import sernet.verinice.service.iso27k.Item;
import sernet.verinice.service.iso27k.ItemControlTransformer;

/**
 * @author Daniel <dm[at]sernet[dot]de>
 * 
 */
public class CatalogView extends ViewPart implements IAttachedToPerspective  {

	private static final Logger LOG = Logger.getLogger(CatalogView.class);

	public static final String ID = "sernet.verinice.iso27k.rcp.CatalogView"; //$NON-NLS-1$
	
	public static final DateFormat DATE_TIME_FORMAT_SHORT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

	private Action addCatalogAction;
	
	private Action deleteCatalogAction;
	
	private Action expandAllAction;

	private Action collapseAllAction;
	
	private DragSourceListener dragListener;

	private ICommandService commandService;

	private TreeViewer viewer;
	
	private Label labelCatalog;
	
	private Combo comboCatalog;
	
	private ComboModel<Attachment> comboModel;
	
	private Label labelFilter;
	
	private Text filter;
	
	private BSIModel bsiModel;
	
	private CatalogTextFilter textFilter;
	
	private IModelLoadListener modelLoadListener;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		try {
			initView(parent);
			startInitDataJob();		
		} catch (Exception e) {
			LOG.error("Error while creating catalog view", e); //$NON-NLS-1$
			ExceptionUtil.log(e, "Error while opening Catalog-View."); //$NON-NLS-1$
		}	
	}



	/**
	 * 
	 */
	protected void startInitDataJob() {
		WorkspaceJob initDataJob = new WorkspaceJob(Messages.ISMView_InitData) {
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				IStatus status = Status.OK_STATUS;
				try {
					monitor.beginTask(Messages.ISMView_InitData, IProgressMonitor.UNKNOWN);
					loadCatalogAttachmets();
				} catch (Exception e) {
					LOG.error("Error while loading data.", e); //$NON-NLS-1$
					status= new Status(Status.ERROR, "sernet.gs.ui.rcp.main", "Error while loading data.",e); //$NON-NLS-1$ //$NON-NLS-2$
				} finally {
					monitor.done();
				}
				return status;
			}
		};
		JobScheduler.scheduleInitJob(initDataJob);
	}



	private void initView(Composite parent) {
		GridLayout gl = new GridLayout(1, true);
		parent.setLayout(gl);
		
		Composite compForm = new Composite(parent,SWT.NONE);
		compForm.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout glForm = new GridLayout(2, false);
		compForm.setLayout(glForm);
		labelCatalog = new Label(compForm,SWT.NONE);
		labelCatalog.setText(Messages.CatalogView_4);
		comboCatalog = new Combo(compForm, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboCatalog.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		comboCatalog.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		    	  comboModel.setSelectedIndex(comboCatalog.getSelectionIndex());
		    	  openCatalog();
		    	  deleteCatalogAction.setEnabled(true);
		      }
		    });
		comboModel = new ComboModel<Attachment>(new ComboModelLabelProvider<Attachment>() {
			@Override
			public String getLabel(Attachment attachment) {
				StringBuilder sb = new StringBuilder();
				sb.append(attachment.getFileName());
				sb.append(" (").append(DATE_TIME_FORMAT_SHORT.format(attachment.getDate())).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
				return sb.toString();
			}		
		});
		
		labelFilter = new Label(compForm,SWT.NONE);
		labelFilter.setText(Messages.CatalogView_7);
		filter = new Text(compForm, SWT.BORDER);
		filter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filter.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {				
			}
			public void keyReleased(KeyEvent e) {
				textFilter.setPattern(filter.getText());
			}		
		});
		
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		//viewer.setSorter(new KapitelSorter());
			
		getSite().setSelectionProvider(viewer);
		
		makeActions();
		hookActions();
		hookDNDListeners();
		fillLocalToolBar();
	}
	


	/**
	 * 
	 */
	private void loadCatalogAttachmets() {
		try {
			Activator.inheritVeriniceContextState();
			if(getBsiModel()!=null) {
				// model is loaded: load data
				LoadAttachments command = new LoadAttachments(getBsiModel().getDbId());		
				command = getCommandService().executeCommand(command);		
				List<Attachment> attachmentList = command.getAttachmentList();
				comboModel.clear();
				for (Attachment attachment : attachmentList) {
					comboModel.add(attachment);
				}
				Display.getDefault().syncExec(new Runnable(){
					public void run() {
						comboCatalog.setItems(comboModel.getLabelArray());
					}
				});
			} else if(modelLoadListener==null) {
				// model is not loaded yet: add a listener to load data when it's laoded
				modelLoadListener = new IModelLoadListener() {

					public void closed(BSIModel model) {
						// nothing to do
					}

					public void loaded(BSIModel model) {
                        // work is done in loaded(ISO27KModel model)
					}

                    @Override
                    public void loaded(ISO27KModel model) {
                        startInitDataJob();
                    }
					
				};
				CnAElementFactory.getInstance().addLoadListener(modelLoadListener);
			}
			
			
		} catch(Exception e) {
			LOG.error("Error while loading catalogs", e); //$NON-NLS-1$
			ExceptionUtil.log(e, Messages.CatalogView_0);
		}
	}
	
	/**
	 * @return 
	 * @throws CommandException 
	 * 
	 */
	private Attachment saveFile(ImportCatalog importCatalog) throws CommandException {
		CsvFile csvFile = importCatalog.getCsvFile();
		Attachment attachment = null;
		if(csvFile!=null) {			
			attachment = new Attachment();
			attachment.setCnATreeElementId(getBsiModel().getDbId());
			attachment.setCnAElementTitel(getBsiModel().getTitle());
			Date now = Calendar.getInstance().getTime();
			attachment.setDate(now);
			attachment.setFilePath(csvFile.getFilePath());
			attachment.setTitel(attachment.getFileName());
			attachment.setText(Messages.CatalogView_10 + DateFormat.getDateTimeInstance().format(now));
			SaveNote command = new SaveNote(attachment);	
			command = getCommandService().executeCommand(command);
			attachment = (Attachment) command.getAddition();
			
			AttachmentFile attachmentFile = new AttachmentFile();
			attachmentFile.setDbId(attachment.getDbId());
			attachmentFile.setFileData(csvFile.getFileContent());
			SaveAttachment saveAttachmentFile = new SaveAttachment(attachmentFile);
			saveAttachmentFile = getCommandService().executeCommand(saveAttachmentFile);
			attachmentFile = saveAttachmentFile.getElement();
			saveAttachmentFile.clear();
		}
		return attachment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private void makeActions() {
		addCatalogAction = new Action() {
			public void run() {
				importCatalog();
			}
		};
		addCatalogAction.setText(Messages.CatalogView_11);
		addCatalogAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.NOTE_NEW));
		addCatalogAction.setEnabled(true);
		
		deleteCatalogAction = new Action() {
			public void run() {
				boolean confirm = MessageDialog.openConfirm(viewer.getControl().getShell(), sernet.verinice.iso27k.rcp.Messages.CatalogView_12, sernet.verinice.iso27k.rcp.Messages.CatalogView_13);
				if (confirm)
					deleteCatalog();
			}
		};
		deleteCatalogAction.setText(Messages.CatalogView_14);
		deleteCatalogAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.DELETE));
		deleteCatalogAction.setEnabled(false);
		
		textFilter = new CatalogTextFilter(viewer);
		
		expandAllAction = new Action() {
			@Override
			public void run() {
				viewer.expandAll();
			}
		};
		expandAllAction.setText(Messages.CatalogView_15);
		expandAllAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.EXPANDALL));

		collapseAllAction = new Action() {
			@Override
			public void run() {
				viewer.collapseAll();
			}
		};
		collapseAllAction.setText(Messages.CatalogView_16);
		collapseAllAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.COLLAPSEALL));

		
		dragListener = new ControlDragListener(viewer);
	}

	/**
	 * 
	 */
	private void hookActions() {
	}
	
	private void hookDNDListeners() {
		Transfer[] types = new Transfer[] { TextTransfer.getInstance(),FileTransfer.getInstance() };
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		viewer.addDragSupport(operations, types, dragListener);
	}
	/**
	 * 
	 */
	private void fillLocalToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		manager.add(this.addCatalogAction);
		manager.add(this.deleteCatalogAction);
		manager.add(this.expandAllAction);
		manager.add(this.collapseAllAction);
	}

	public ICommandService getCommandService() {
		if (commandService == null) {
			commandService = createCommandService();
		}
		return commandService;
	}

	private ICommandService createCommandService() {
		return ServiceFactory.lookupCommandService();
	}

	static class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {

		public void dispose() {
		}

		public Object[] getChildren(Object parent) {
			return ((IItem) parent).getItems().toArray();		
		}

		public Object[] getElements(Object parent) {
			return getChildren(parent);
		}

		public Object getParent(Object child) {
			return null;
		}

		public boolean hasChildren(Object parent) {
			return ((IItem) parent).getItems().size()>0;
		}

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
	}
	
	static class ViewLabelProvider extends LabelProvider {

		public Image getImage(Object obj) {
			IItem item = (IItem) obj;
			String image = ImageCache.UNKNOWN;
			
			if(item.getDescription()!=null && item.getItems().size()>0) {
				image = ImageCache.BAUSTEIN;
			}
			else if(item.getDescription()!=null && item.getTypeId()==IItem.CONTROL) {
				image = ImageCache.STUFE_NONE;
			}
			else if(item.getDescription()!=null && item.getTypeId()==IItem.ISA_TOPIC) {
				image = ImageCache.ISA_TOPIC;
			}
			else if(item.getDescription()!=null && item.getTypeId()==IItem.THREAT) {
				image = ImageCache.ISO27K_THREAT;
			}
			else if(item.getDescription()!=null && item.getTypeId()==IItem.VULNERABILITY) {
			    image = ImageCache.ISO27K_VULNERABILITY;
			}
			return ImageCache.getInstance().getImage(image);	
		}

		public String getText(Object obj) {
			IItem item = ((IItem)obj);
			String label = "";
			if(item!=null) {
			    label = ItemControlTransformer.truncate(item.getName(), 80);
			}
			return label;
		}
	}

	public BSIModel getBsiModel() {
		if(bsiModel==null) {
			try {
				bsiModel = CnAElementFactory.getLoadedModel();
			} catch (Exception e) {
				LOG.error("Error while creating BSI-Model", e); //$NON-NLS-1$
			}
		}
		return bsiModel;
	}

	public void setBsiModel(BSIModel bsiModel) {
		this.bsiModel = bsiModel;
	}
	
	public BSIModel loadBsiModel() {
		LoadBSIModel loadBSIModel = new LoadBSIModel();
		try {
			loadBSIModel = getCommandService().executeCommand(loadBSIModel);
		} catch (CommandException e) {
			LOG.error("Error while loading BSI-Model.", e); //$NON-NLS-1$
		}
		bsiModel = loadBSIModel.getModel();
		return bsiModel;
	}

	private void openCatalog() {
		try {
			Attachment selected = comboModel.getSelectedObject();
			if(selected!=null) {
			    // load attachment/file from database
				LoadAttachmentFile loadAttachmentFile = new LoadAttachmentFile(selected.getDbId());
				loadAttachmentFile = getCommandService().executeCommand(loadAttachmentFile);
				if(loadAttachmentFile!=null && loadAttachmentFile.getAttachmentFile()!=null && loadAttachmentFile.getAttachmentFile().getFileData()!=null) {
					// import the file
					ImportCatalog importCatalog = new ImportCatalog(loadAttachmentFile.getAttachmentFile().getFileData());
					importCatalog = getCommandService().executeCommand(importCatalog);
					if(importCatalog.getCatalog()!=null) {
						viewer.setInput(importCatalog.getCatalog().getRoot());
					}
				}
			}
		} catch(Exception e) {
			LOG.error("Error while loading catalog", e); //$NON-NLS-1$
			ExceptionUtil.log(e, Messages.CatalogView_21);
		}
	}

	private void importCatalog() {
		FileDialog fd = new FileDialog(CatalogView.this.getSite().getShell());
		fd.setText(Messages.CatalogView_22);
		fd.setFilterPath("~"); //$NON-NLS-1$
		fd.setFilterExtensions(new String[] { "*.csv" }); //$NON-NLS-1$
		String selected = fd.open();
		if (selected != null && selected.length() > 0) {
			try {
			    
				ImportCatalog importCatalog = new ImportCatalog(selected,getCharset());
				importCatalog = getCommandService().executeCommand(importCatalog);
				Attachment attachment = saveFile(importCatalog);
				if(importCatalog.getCatalog()!=null) {
					viewer.setInput(importCatalog.getCatalog().getRoot());
				}
				comboModel.add(attachment);
				String[] labelArray = comboModel.getLabelArray();
				comboCatalog.setItems(labelArray);
				deleteCatalogAction.setEnabled(labelArray.length>0);
				selectComboItem(attachment);
			} catch (Exception e) {
				LOG.error("Error while reading file data", e);
				ExceptionUtil.log(e, Messages.CatalogView_26);
			}
		}
	}
	
	private Charset getCharset() {
	    // read the charset from preference store
	    // charset value is set in CharsetHandler
	    String charsetName = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.CHARSET_CATALOG);
	    Charset charset = VeriniceCharset.CHARSET_DEFAULT;
	    if(charsetName!=null && !charsetName.isEmpty()) {
	        charset = Charset.forName(charsetName);
	    }
	    return charset;
	}
	
	/**
	 * @param attachment
	 */
	private void selectComboItem(Attachment attachment) {
		comboModel.setSelectedObject(attachment);
		// indexes that are out of range are ignored in Combo
		comboCatalog.select(comboModel.getSelectedIndex());
	}

	/**
	 * 
	 */
	protected void deleteCatalog() {
		try {
			Attachment selected = comboModel.getSelectedObject();
			DeleteNote command = new DeleteNote(selected);		
			command = getCommandService().executeCommand(command);
			comboModel.removeSelected();
			openCatalog();
			comboCatalog.setItems(comboModel.getLabelArray());
			comboCatalog.select(comboModel.getSelectedIndex());
			if(comboModel.getSelectedIndex()<0) {
				deleteCatalogAction.setEnabled(false);
				viewer.setInput(new Item());
			}
		} catch(Exception e) {
			LOG.error("Error while deleting catalog", e); //$NON-NLS-1$
			ExceptionUtil.log(e, Messages.CatalogView_28);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see sernet.verinice.rcp.IAttachedToPerspective#getPerspectiveId()
	 */
	public String getPerspectiveId() {
		return Iso27kPerspective.ID;
	}
}
