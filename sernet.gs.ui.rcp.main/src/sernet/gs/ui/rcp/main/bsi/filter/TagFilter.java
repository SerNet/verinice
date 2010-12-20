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
package sernet.gs.ui.rcp.main.bsi.filter;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.ITVerbund;

public class TagFilter extends ViewerFilter {

    public static final String NO_TAG = Messages.TagFilter_0;

    String[] pattern;
    private StructuredViewer viewer;
    private boolean filterItVerbund;

    public TagFilter(StructuredViewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (!(element instanceof IBSIStrukturElement) 
        	|| ((element instanceof ITVerbund) && !filterItVerbund)) {
            return true;
        }

        IBSIStrukturElement zielobjekt = (IBSIStrukturElement) element;
        for (String tag : pattern) {
            if (tag.equals(NO_TAG)) {
                if (zielobjekt.getTags().size() < 1) {
                    return true;
                }
            }

            for (String zielTag : zielobjekt.getTags()) {
                if (zielTag.equals(tag)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String[] getPattern() {
        return pattern;
    }

    public void setPattern(String[] newPattern) {
        boolean active = pattern != null;
        if (newPattern != null && newPattern.length > 0) {
            pattern = newPattern;
            if (active) {
                viewer.refresh();
            } else {
                viewer.addFilter(this);
                active = true;
            }
            return;
        }

        // else deactivate:
        pattern = null;
        if (active) {
            viewer.removeFilter(this);
        }

    }
    
    public boolean isFilterItVerbund() {
		return filterItVerbund;
	}

	public void setFilterItVerbund(boolean filterItVerbund) {
		this.filterItVerbund = filterItVerbund;
	}

}
