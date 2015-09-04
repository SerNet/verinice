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
package sernet.verinice.model.bsi.risikoanalyse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sernet.gs.model.Gefaehrdung;

/**
 * Helper methods to work with threat lists in situations where equals (UUID)
 * comparison is inappropriate.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public abstract class GefaehrdungsUtil {

    public static List<GefaehrdungsUmsetzung> removeBySameId(List<GefaehrdungsUmsetzung> allGefaehrdungsUmsetzungen, GefaehrdungsUmsetzung gefaehrdung) {

        if (gefaehrdung == null) {
            return null;
        }

        GefaehrdungsUmsetzung gefaehrdungInList = null;
        List<GefaehrdungsUmsetzung> found = new ArrayList<GefaehrdungsUmsetzung>();
        for (Iterator iterator = allGefaehrdungsUmsetzungen.iterator(); iterator.hasNext();) {
            gefaehrdungInList = (GefaehrdungsUmsetzung) iterator.next();
            if (gefaehrdung.getId() == null || gefaehrdungInList.getId() == null) {
                continue;
            }
            if (gefaehrdungInList.getId().equals(gefaehrdung.getId())) {
                found.add(gefaehrdungInList);
            }
        }

        for (GefaehrdungsUmsetzung toDelete : found) {
            allGefaehrdungsUmsetzungen.remove(toDelete);
        }

        return found;
    }

    public static List<GefaehrdungsUmsetzung> removeBySameId(List<GefaehrdungsUmsetzung> allGefaehrdungsUmsetzungen, Gefaehrdung gefaehrdung) {

        if (gefaehrdung == null) {
            return null;
        }

        GefaehrdungsUmsetzung gefaehrdungInList = null;
        List<GefaehrdungsUmsetzung> found = new ArrayList<GefaehrdungsUmsetzung>();
        findGefaehrdung: for (Iterator iterator = allGefaehrdungsUmsetzungen.iterator(); iterator.hasNext();) {
            gefaehrdungInList = (GefaehrdungsUmsetzung) iterator.next();
            if (gefaehrdung.getId() == null || gefaehrdungInList.getId() == null) {
                continue;
            }
            if (gefaehrdungInList.getId().equals(gefaehrdung.getId())) {
                found.add(gefaehrdungInList);
            }
        }

        for (GefaehrdungsUmsetzung toDelete : found) {
            allGefaehrdungsUmsetzungen.remove(toDelete);
        }

        return found;
    }

    @SuppressWarnings("unchecked")
    public static boolean listContainsById(List selectedArrayList, Gefaehrdung currentGefaehrdung) {
        for (Iterator iterator = selectedArrayList.iterator(); iterator.hasNext();) {
            Object object = iterator.next();
            if (object instanceof Gefaehrdung) {
                Gefaehrdung gefaehrdung = (Gefaehrdung) object;
                if (gefaehrdung.getId().equals(currentGefaehrdung.getId())) {
                    return true;
                }
            }
            if (object instanceof GefaehrdungsUmsetzung) {
                GefaehrdungsUmsetzung gefaehrdung = (GefaehrdungsUmsetzung) object;
                if (gefaehrdung.getId().equals(currentGefaehrdung.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean listContainsById(List<GefaehrdungsUmsetzung> selectedArrayList, GefaehrdungsUmsetzung currentGefaehrdung) {
        for (GefaehrdungsUmsetzung gefaehrdung : selectedArrayList) {
            if (gefaehrdung.getId().equals(currentGefaehrdung.getId())) {
                return true;
            }
        }
        return false;
    }

}
