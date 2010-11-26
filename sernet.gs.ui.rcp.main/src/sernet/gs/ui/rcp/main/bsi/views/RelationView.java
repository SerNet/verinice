package sernet.gs.ui.rcp.main.bsi.views;


import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.FindRelationsFor;
import sernet.verinice.iso27k.rcp.ISMView;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ISO27KModel;


/**
 * This view displays all relations (links) for a slected element and allows the user to change the link type.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class RelationView extends ViewPart implements IRelationTable {

	private static final Logger LOG = Logger.getLogger(ISMView.class);

	
	public static final String ID = "sernet.gs.ui.rcp.main.bsi.views.RelationView"; //$NON-NLS-1$
	
	private TableViewer viewer;
	private Action jumpToAction;
//	private Action action2;
	private Action doubleClickAction;
	private ISelectionListener selectionListener;
	private CnATreeElement inputElmt;

	private RelationViewContentProvider contentProvider;

	private IModelLoadListener loadListener;

	/**
	 * The constructor.
	 */
	public RelationView() {
	}

	/**
	 * @param elmt
	 */
	public void loadLinks(final CnATreeElement elmt) {

		if (!CnAElementHome.getInstance().isOpen()
		        || inputElmt == null) {
			return;
		}



		WorkspaceJob job = new WorkspaceJob(Messages.RelationView_0) {
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				Activator.inheritVeriniceContextState();

				try {
				
				 Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            viewer.setInput(new PlaceHolder("Lade Relationen..."));
                        }
                    });
                    
					monitor.setTaskName(Messages.RelationView_0);

					FindRelationsFor command = new FindRelationsFor(elmt);
					command = ServiceFactory.lookupCommandService()
							.executeCommand(command);
					final CnATreeElement linkElmt = command.getElmt();

					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							viewer.setInput(linkElmt);
						}
					});
				} catch (Exception e) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							viewer.setInput(new PlaceHolder(Messages.RelationView_3));
						}
					});

					ExceptionUtil.log(e, Messages.RelationView_4);
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(false);
		job.schedule();
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new RelationTableViewer(this, parent, SWT.FULL_SELECTION | SWT.MULTI, false);
		contentProvider = new RelationViewContentProvider(this, viewer);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new RelationViewLabelProvider(this));
		viewer.setSorter(new RelationByNameSorter(this, COLUMN_TITLE, COLUMN_TYPE_IMG));

		// try to add listeners once on startup, and register for model changes:
		addBSIModelListeners();
		addISO27KModelListeners();
        hookModelLoadListener();
		
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		hookPageSelection();
	}
	
	/**
	 * 
	 */
	private void hookModelLoadListener() {
		this.loadListener = new IModelLoadListener() {

			public void closed(BSIModel model) {
				removeModelListeners();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						viewer.setInput(new PlaceHolder("")); //$NON-NLS-1$
					}
				});
			}

			public void loaded(BSIModel model) {
				synchronized (loadListener) {
					addBSIModelListeners();
				}
			}

            @Override
            public void loaded(ISO27KModel model) {
                synchronized (loadListener) {
                    addISO27KModelListeners();   
                }
            }
			
		};
		CnAElementFactory.getInstance().addLoadListener(loadListener);
	}
	
	   /**
     * 
     */
    protected void addBSIModelListeners() {
        WorkspaceJob initDataJob = new WorkspaceJob(sernet.verinice.iso27k.rcp.Messages.ISMView_InitData) {
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask(sernet.verinice.iso27k.rcp.Messages.ISMView_InitData, IProgressMonitor.UNKNOWN);
                    if (CnAElementFactory.isModelLoaded()) {
                        CnAElementFactory.getInstance().getLoadedModel().addBSIModelListener(contentProvider);
                    }
                } catch (Exception e) {
                    LOG.error("Error while loading data.", e); //$NON-NLS-1$
                    status= new Status(Status.ERROR, "sernet.gs.ui.rcp.main", Messages.RelationView_7,e); //$NON-NLS-1$
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        JobScheduler.scheduleInitJob(initDataJob);      
    }

	/**
	 * 
	 */
	protected void addISO27KModelListeners() {
		WorkspaceJob initDataJob = new WorkspaceJob(sernet.verinice.iso27k.rcp.Messages.ISMView_InitData) {
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				IStatus status = Status.OK_STATUS;
				try {
					monitor.beginTask(sernet.verinice.iso27k.rcp.Messages.ISMView_InitData, IProgressMonitor.UNKNOWN);
					if (CnAElementFactory.isIsoModelLoaded()) {
						CnAElementFactory.getInstance().getISO27kModel().addISO27KModelListener(contentProvider);
					}
				} catch (Exception e) {
					LOG.error("Error while loading data.", e); //$NON-NLS-1$
					status= new Status(Status.ERROR, "sernet.gs.ui.rcp.main", Messages.RelationView_7,e); //$NON-NLS-1$
				} finally {
					monitor.done();
				}
				return status;
			}
		};
		JobScheduler.scheduleInitJob(initDataJob);			
	}

	/**
	 * 
	 */
	protected void removeModelListeners() {
		CnAElementFactory.getInstance().getLoadedModel().removeBSIModelListener(contentProvider);
		CnAElementFactory.getInstance().getISO27kModel().removeISO27KModelListener(contentProvider);
	}

	public CnATreeElement getInputElement() {
		return this.inputElmt;
	}
	
	
	

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				RelationView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}
	
	private void hookPageSelection() {
		selectionListener = new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part,
					ISelection selection) {
				pageSelectionChanged(part, selection);
			}
		};
		getSite().getPage().addPostSelectionListener(selectionListener);
		
		/**
		 * Own selection provider returns a CnALin k Object of the selected row.
		 * Uses the viewer for all other methods.
		 */
		getSite().setSelectionProvider(viewer);

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		CnAElementFactory.getInstance().removeLoadListener(loadListener);
		removeModelListeners();
		getSite().getPage().removePostSelectionListener(selectionListener);
		super.dispose();
	}
	
	protected void pageSelectionChanged(IWorkbenchPart part,
			ISelection selection) {
		if (part == this)
			return;

		if (!(selection instanceof IStructuredSelection))
			return;

		if (((IStructuredSelection) selection).size() != 1)
			return;

		Object element = ((IStructuredSelection) selection).getFirstElement();
		if (element instanceof CnATreeElement) {
			setNewInput((CnATreeElement)element);
		}
	}
	
	/**
	 * @param element
	 */
	private void setNewInput(CnATreeElement elmt) {
		this.inputElmt = elmt;
		loadLinks(elmt);
		setViewTitle(Messages.RelationView_9 + elmt.getTitle());
	}

	private void setViewTitle(String title) {
		this.setContentDescription(title);
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(jumpToAction);
		manager.add(new Separator());
//		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(jumpToAction);
//		manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(jumpToAction);
//		manager.add(action2);
	}

	private void makeActions() {
		jumpToAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if (obj == null)
					return;
				
				CnALink link = (CnALink) obj;
				if (CnALink.isDownwardLink(inputElmt, link))
					setNewInput(link.getDependency());
				else
					setNewInput(link.getDependant());
			}
		};
		jumpToAction.setText(Messages.RelationView_10);
		jumpToAction.setToolTipText(Messages.RelationView_11);
		jumpToAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.ARROW_IN));
		
//		action2 = new Action() {
//			public void run() {
//				showMessage("Action 2 executed");
//			}
//		};
//		action2.setText("Action 2");
//		action2.setToolTipText("Action 2 tooltip");
//		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
//				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				CnALink link = (CnALink) obj;

				// open the object on the other side of the link:
				if (CnALink.isDownwardLink(inputElmt, link))
					EditorFactory.getInstance().updateAndOpenObject(link.getDependency());
				else
					EditorFactory.getInstance().updateAndOpenObject(link.getDependant());
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			Messages.RelationView_12,
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * 
	 */
	public void reload(CnALink oldLink, CnALink newLink) {
		newLink.setDependant(oldLink.getDependant());
		newLink.setDependency(oldLink.getDependency());
		
		boolean removedLinkDown = inputElmt.removeLinkDown(oldLink);
		boolean removedLinkUp = inputElmt.removeLinkUp(oldLink);
		if (removedLinkUp)
			inputElmt.addLinkUp(newLink);
		if (removedLinkDown)
			inputElmt.addLinkDown(newLink);
		viewer.refresh();
		
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.views.IRelationTable#getInputElmt()
	 */
	public CnATreeElement getInputElmt() {
		return this.inputElmt;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.views.IRelationTable#setInputElmt(sernet.gs.ui.rcp.main.common.model.CnATreeElement)
	 */
	public void setInputElmt(CnATreeElement inputElmt) {
		this.inputElmt = inputElmt;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.views.IRelationTable#reloadAll()
	 */
	public void reloadAll() {
		loadLinks(inputElmt);
	}
}