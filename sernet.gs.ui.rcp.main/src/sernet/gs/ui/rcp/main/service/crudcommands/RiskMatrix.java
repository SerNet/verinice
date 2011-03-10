/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;

/**
 * Matrix to save how often a particular impact / probabiliuty combination was used during risk analysis.
 * 
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class RiskMatrix implements Serializable {
        Integer[][] map; 
        
        /**
         * 
         */
        public RiskMatrix(int maxProb, int maxImpact) {
            map = new Integer[maxProb+1][maxImpact+1];
            init(map);
        }
        
        /**
         * @param map2
         */
        private void init(Integer[][] map2) {
            for (int i=0; i < map.length; i++) {
                for (int j=0; j < map[i].length; j++ ) {
                    map[i][j] = 0;
                }
            }
        }

        public void increaseCount(Integer probability, Integer impact) {
            if (impact==null || probability == null
                    || probability < 0 || probability > map.length-1
                    || impact < 0 || impact > map[0].length-1) {
                return;
            }
            map[probability][impact]++;
        }
        
        public String[] getColumnTitles() {
            String[] titles = new String[map[0].length];
            for (int i=0; i < map[0].length; i++) {
                titles[i] = "IMPACT_" + i;
            }
            return titles;
        }
        
}


