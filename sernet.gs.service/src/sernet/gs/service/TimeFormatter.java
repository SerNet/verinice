/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.service;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class TimeFormatter {
    
    public static String getHumanRedableTime(long ms) {
        double x = ms / 1000.0;
        long seconds = Math.round(x % 60);
        x /= 60;
        long minutes = Math.round(x % 60);
        x /= 60;
        long hours = Math.round(x % 24);
        x /= 24;
        long days = Math.round(x);
        StringBuilder sb = new StringBuilder();
        if(days>0) {
            sb.append(days).append(" d");
        }
        if(hours>0) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append(hours).append(" h");
        }
        if(minutes>0 && days<1) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append(minutes).append(" m");
        }
        if(seconds>0 && hours<1) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append(seconds).append(" s");
        }        
        return sb.toString();     
    }
    
}
