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

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import sernet.verinice.model.common.CnATreeElement;

public class Emptychart extends UmsetzungBarChart implements ISelectionChartGenerator{

	@Override
	public JFreeChart createChart() {
		return createBarChart(createEmptyBarDataset());
	}
	
	private Object createEmptyBarDataset() {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		return dataset;
	}

	@Override
	public JFreeChart createChart(CnATreeElement elmt) {
		return createBarChart(createEmptyBarDataset());
	}
	
	
}
