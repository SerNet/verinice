package sernet.gs.ui.rcp.main.bsi.views.chart;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.lucene.document.DateTools;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.DataUtilities;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.views.Messages;
import sernet.gs.ui.rcp.main.bsi.views.actions.TodoViewFilterAction;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.NullModel;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;

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

			public void modelRefresh() {
				drawChart();
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
