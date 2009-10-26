/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views.chart;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
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
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class ChartView extends ViewPart {

	public static final String ID = "sernet.gs.ui.rcp.main.chartview";

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

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		frame = new ChartComposite(parent, SWT.NONE, null, true);
		createChartGenerators();
		createSelectionListeners();
		hookSelectionListeners();
		createMenus();

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
	}

	private void createMenus() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
		chooseBarDiagramAction = new Action("Umsetzungsstatus", SWT.CHECK) {
			@Override
			public void run() {
				chartType = barChart;
				drawChart();
			}
		};
		chooseBarDiagramAction.setImageDescriptor(ImageCache.getInstance()
				.getImageDescriptor(ImageCache.CHART_BAR));

		chooseProgressDiagramAction = new Action("Realisierungsplan", SWT.CHECK) {
			@Override
			public void run() {
				chartType = progressChart;
				drawChart();
			}
		};
		chooseProgressDiagramAction.setImageDescriptor(ImageCache.getInstance()
				.getImageDescriptor(ImageCache.CHART_CURVE));

		chooseStufenDiagramAction = new Action("Siegelstufe", SWT.CHECK) {
			@Override
			public void run() {
				chartType = stufenChart;
				drawChart();
			}
		};

		chooseZyklusDiagramAction = new Action("Lebenszyklus", SWT.CHECK) {
			@Override
			public void run() {
				chartType = zyklusChart;
				drawChart();
			}
		};

		chooseSchichtDiagramAction = new Action("Schichten", SWT.CHECK) {
			@Override
			public void run() {
				chartType = schichtenChart;
				drawChart();
			}
		};
		menuManager.add(chooseBarDiagramAction);
		menuManager.add(chooseProgressDiagramAction);
		menuManager.add(chooseStufenDiagramAction);
		menuManager.add(chooseZyklusDiagramAction);
		menuManager.add(chooseSchichtDiagramAction);

		fillLocalToolBar();
	}

	private void fillLocalToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		manager.add(chooseBarDiagramAction);
		manager.add(chooseProgressDiagramAction);
	}

	protected void drawChart() {
		WorkspaceJob job = new WorkspaceJob("Generating chart...") {
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				Activator.inheritVeriniceContextState();
				
				if (parent != null && !parent.isDisposed()) {
					final JFreeChart chart;
					checkModel();
					chart = chartType.createChart();
					if (chart != null) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								frame.setChart(chart);
								frame.forceRedraw();
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
		if (CnAElementFactory.getLoadedModel() == null)
			chartType = emptyChart;
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
						CnAElementFactory.getLoadedModel()
								.addBSIModelListener(changeListener);
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
			
			public void linkChanged(CnALink link) {
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

	private void hookSelectionListeners() {
		CnAElementFactory.getInstance().addLoadListener(loadListener);
		if (CnAElementFactory.getLoadedModel() != null)
			CnAElementFactory.getLoadedModel().addBSIModelListener(
					changeListener);
	}

	@Override
	public void dispose() {
		CnAElementFactory.getInstance().removeLoadListener(loadListener);
		if (CnAElementFactory.getLoadedModel() != null)
			CnAElementFactory.getLoadedModel().removeBSIModelListener(
				changeListener);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
