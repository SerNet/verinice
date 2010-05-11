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
package sernet.gs.ui.rcp.main.bsi.views.chart;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.JFreeChart;
import org.jfree.experimental.chart.swt.ChartComposite;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.common.model.ChangeLogEntry;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;

/**
 * Displays charts to visualize progress and other data.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class ChartView extends ViewPart {

	public static final String ID = "sernet.gs.ui.rcp.main.chartview"; //$NON-NLS-1$
	private static final Logger LOG = Logger.getLogger(ChartView.class);

	private IModelLoadListener loadListener;

	private IBSIModelListener changeListener;

	private Composite parent;

	private ChartComposite frame;

	private Action chooseBarDiagramAction;

	private Action chooseProgressDiagramAction;

	private IChartGenerator chartType;

	private UmsetzungBarChart barChart;

	private RealisierungLineChart progressChart;

	private Emptychart emptyChart;

	private StufenBarChart stufenChart;

	private Action chooseStufenDiagramAction;

	private LebenszyklusBarChart zyklusChart;

	private Action chooseZyklusDiagramAction;

	private SchichtenBarChart schichtenChart;

	private Action chooseSchichtDiagramAction;

	private MaturitySpiderChart maturitySpiderChart;

	private Action chooseMaturityDiagramAction;

	private ISelectionListener selectionListener;

	private CnATreeElement elmt = null;

	private Action chooseMaturityBarDiagramAction;

	private MaturityBarChart maturityBarChart;

	private CnATreeElement previousElement;

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		frame = new ChartComposite(parent, SWT.NONE, null, true);
		createChartGenerators();
		createSelectionListeners();
		hookSelectionListeners();
		hookPageSelection();
		createMenus();
		this.setContentDescription(Messages.ChartView_0);

		if (CnAElementFactory.getLoadedModel() != null) {
			chartType = barChart;
			drawChart();
		}
	}

	private void createChartGenerators() {
		barChart = new UmsetzungBarChart();
		progressChart = new RealisierungLineChart();
		emptyChart = new Emptychart();
		stufenChart = new StufenBarChart();
		zyklusChart = new LebenszyklusBarChart();
		schichtenChart = new SchichtenBarChart();
		maturitySpiderChart = new MaturitySpiderChart();
		maturityBarChart = new MaturityBarChart();

	}

	private void createMenus() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
		chooseBarDiagramAction = new Action(Messages.ChartView_1, SWT.CHECK) {
			@Override
			public void run() {
				chartType = barChart;
				drawChart();
			}
		};
		chooseBarDiagramAction.setImageDescriptor(ImageCache.getInstance()
				.getImageDescriptor(ImageCache.CHART_BAR));

		chooseProgressDiagramAction = new Action(Messages.ChartView_2,
				SWT.CHECK) {
			@Override
			public void run() {
				chartType = progressChart;
				drawChart();
			}
		};
		chooseProgressDiagramAction.setImageDescriptor(ImageCache.getInstance()
				.getImageDescriptor(ImageCache.CHART_CURVE));

		chooseStufenDiagramAction = new Action(Messages.ChartView_3, SWT.CHECK) {
			@Override
			public void run() {
				chartType = stufenChart;
				drawChart();
			}
		};

		chooseZyklusDiagramAction = new Action(Messages.ChartView_4, SWT.CHECK) {
			@Override
			public void run() {
				chartType = zyklusChart;
				drawChart();
			}
		};

		chooseSchichtDiagramAction = new Action(Messages.ChartView_5, SWT.CHECK) {
			@Override
			public void run() {
				chartType = schichtenChart;
				drawChart();
			}
		};

		chooseMaturityDiagramAction = new Action(Messages.ChartView_7,
				SWT.CHECK) {
			@Override
			public void run() {
				chartType = maturitySpiderChart;
				drawChart();
				setContentDescription(Messages.ChartView_8);
			}
		};
		chooseMaturityDiagramAction.setImageDescriptor(ImageCache.getInstance()
				.getImageDescriptor(ImageCache.CHART_PIE));

		chooseMaturityBarDiagramAction = new Action(Messages.ChartView_9,
				SWT.CHECK) {
			@Override
			public void run() {
				chartType = maturityBarChart;
				drawChart();
				setContentDescription(Messages.ChartView_10);
			}
		};
		chooseMaturityBarDiagramAction.setImageDescriptor(ImageCache
				.getInstance().getImageDescriptor(ImageCache.CHART_BAR));

		menuManager.add(chooseBarDiagramAction);
		menuManager.add(chooseProgressDiagramAction);
		menuManager.add(chooseStufenDiagramAction);
		menuManager.add(chooseZyklusDiagramAction);
		menuManager.add(chooseSchichtDiagramAction);
		menuManager.add(new Separator());
		menuManager.add(chooseMaturityDiagramAction);
		menuManager.add(chooseMaturityBarDiagramAction);

		fillLocalToolBar();
	}

	private void fillLocalToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		manager.add(chooseBarDiagramAction);
		manager.add(chooseProgressDiagramAction);
		manager.add(new Separator());
		manager.add(chooseMaturityDiagramAction);
		manager.add(chooseMaturityBarDiagramAction);
	}

	protected synchronized void drawChart() {
		WorkspaceJob job = new WorkspaceJob(Messages.ChartView_6) {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				Activator.inheritVeriniceContextState();

				if (parent != null && !parent.isDisposed() && frame != null
						&& !frame.isDisposed()) {
					final JFreeChart chart;
					checkModel();
					if (chartType instanceof ISelectionChartGenerator)
						chart = ((ISelectionChartGenerator) chartType)
								.createChart(elmt);
					else
						chart = chartType.createChart();
					if (chart != null) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								try {
									if (frame.isDisposed())
										return;
									frame.setChart(chart);
									frame.forceRedraw();
								} catch (Exception e) {
									// chart disposed:
									LOG.error(e);
								}
							}
						});
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();

	}

	protected void checkModel() {
		if (!CnAElementFactory.isModelLoaded() && !CnAElementFactory.isIsoModelLoaded()) {
			chartType = emptyChart;
		}
	}

	private void createSelectionListeners() {
		loadListener = new IModelLoadListener() {
			public void closed(BSIModel model) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						chartType = emptyChart;
						drawChart();
					}
				});
			}

			public void loaded(final BSIModel model) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						CnAElementFactory.getLoadedModel().addBSIModelListener(
								changeListener);
						chartType = barChart;
						drawChart();
					}
				});
			}
		};

		changeListener = new IBSIModelListener() {

			public void childAdded(CnATreeElement category, CnATreeElement child) {
				// do nothing
			}

			public void childChanged(CnATreeElement category,
					CnATreeElement child) {
				// do nothing
			}

			public void childRemoved(CnATreeElement category,
					CnATreeElement child) {
				// do nothing
			}

			public void linkChanged(CnALink old, CnALink link) {
				// do nothing
			}

			public void linkRemoved(CnALink link) {
				// do nothing

			}

			public void modelRefresh(Object source) {
				drawChart();
			}

			public void linkAdded(CnALink link) {
				// do nothing
			}

			public void databaseChildAdded(CnATreeElement child) {
				// TODO Auto-generated method stub

			}

			public void databaseChildChanged(CnATreeElement child) {
				// TODO Auto-generated method stub

			}

			public void databaseChildRemoved(CnATreeElement child) {
				// TODO Auto-generated method stub

			}

			public void modelRefresh() {
				// TODO Auto-generated method stub

			}

			public void modelReload(BSIModel newModel) {
				// TODO Auto-generated method stub

			}

			public void databaseChildRemoved(ChangeLogEntry entry) {
				// TODO Auto-generated method stub

			}

		};
	}

	private void hookPageSelection() {
		selectionListener = new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part,
					ISelection selection) {
				pageSelectionChanged(part, selection);
			}
		};
		getSite().getPage().addPostSelectionListener(selectionListener);
	}

	/**
	 * @param part
	 * @param selection
	 */
	protected synchronized void pageSelectionChanged(IWorkbenchPart part, ISelection selection) {
        if (!(selection instanceof IStructuredSelection)) 
            return;
        Object element = ((IStructuredSelection) selection).getFirstElement();
        CnATreeElement elmt = (CnATreeElement) element;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Selection changed in chartview."); //$NON-NLS-1$
        }
        
        if (element instanceof CnATreeElement) {
            if (previousElement != null && element == previousElement)
                return;
                
            previousElement = elmt;
         
            this.setContentDescription(NLS.bind(Messages.ChartView_11, new Object[] {elmt.getTitle()}));
            		
            this.elmt = elmt;
            drawChart();
        }
    }

	private void hookSelectionListeners() {
		CnAElementFactory.getInstance().addLoadListener(loadListener);
		if (CnAElementFactory.getLoadedModel() != null)
			CnAElementFactory.getLoadedModel().addBSIModelListener(
					changeListener);
	}

	@Override
	public void dispose() {
		getSite().getPage().removePostSelectionListener(selectionListener);
		CnAElementFactory.getInstance().removeLoadListener(loadListener);
		if (CnAElementFactory.getLoadedModel() != null) {
			CnAElementFactory.getLoadedModel().removeBSIModelListener(
					changeListener);
		}
		if (CnAElementFactory.getLoadedModel() != null)
			CnAElementFactory.getLoadedModel().removeBSIModelListener(
					changeListener);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Disposing chart view " + this); //$NON-NLS-1$
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
