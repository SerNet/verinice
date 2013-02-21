/*******************************************************************************
 * Copyright (c) 2011 Sebastian Hagedorn <sh@sernet.de>.
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
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views.chart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jfree.data.category.DefaultCategoryDataset;

import sernet.gs.ui.rcp.main.common.model.CSRMassnahmenSummaryHome;
import sernet.verinice.interfaces.CommandException;

/**
 *
 */
public class CSRMaturitySpiderChart extends MaturitySpiderChart {
   
    @Override
    protected Object createSpiderDataset() throws CommandException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        CSRMassnahmenSummaryHome dao = new CSRMassnahmenSummaryHome();

        Map<String, Double> items2 = dao.getControlMaxGroups(getElmt());
        Set<Entry<String, Double>> entrySet2 = items2.entrySet();
        for (Entry<String, Double> entry : sort(entrySet2)) {
            dataset.addValue(entry.getValue(), Messages.MaturitySpiderChart_1, entry.getKey());
        }

        Map<String, Double> items4 = dao.getControlGoal2Groups(getElmt());
        Set<Entry<String, Double>> entrySet4 = items4.entrySet();
        for (Entry<String, Double> entry : sort(entrySet4)) {
            dataset.addValue(entry.getValue(), Messages.MaturitySpiderChart_2, entry.getKey());
        }

        Map<String, Double> items3 = dao.getControlGoal1Groups(getElmt());
        Set<Entry<String, Double>> entrySet3 = items3.entrySet();
        for (Entry<String, Double> entry : sort(entrySet3)) {
            dataset.addValue(entry.getValue(), Messages.MaturitySpiderChart_3, entry.getKey());
        }

        Map<String, Double> items1 = dao.getControlGroups(getElmt());
        Set<Entry<String, Double>> entrySet = items1.entrySet();
       
        for (Entry<String, Double> entry : sort(entrySet)) {
            dataset.addValue(entry.getValue(), Messages.MaturitySpiderChart_4, entry.getKey());
        }
        
      

        return dataset;
    }
    
    /**
     * @param entrySet
     * @return
     */
    private List<Entry<String, Double>> sort(Set<Entry<String, Double>> entrySet) {
        ArrayList<Entry<String, Double>> list = new ArrayList<Entry<String,Double>>();
        list.addAll(entrySet);
        Collections.sort(list, new Comparator<Entry<String, Double>>() {
            public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        return list;
    }
}
