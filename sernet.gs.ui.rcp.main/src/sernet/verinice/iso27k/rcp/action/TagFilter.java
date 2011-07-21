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
package sernet.verinice.iso27k.rcp.action;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.IISO27Scope;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.IISO27kGroup;

public class TagFilter extends ViewerFilter {

    public static final String NO_TAG = Messages.getString("TagFilter.0"); //$NON-NLS-1$

    String[] pattern;
    private StructuredViewer viewer;
    private boolean filterOrgs;

    public TagFilter(StructuredViewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object o) {
        boolean result = true;
        if (o instanceof IISO27kElement 
            && !(o instanceof Group)
            && (!(o instanceof IISO27Scope) || filterOrgs)
            && pattern!=null) {
            result = false;
            IISO27kElement element = (IISO27kElement) o;
            for (String tag : pattern) {
                if (tag.equals(NO_TAG)) {
                    if (element.getTags().size() < 1) {
                        result = true;
                    }
                }
                for (String zielTag : element.getTags()) {
                    if (zielTag.equals(tag)) {
                        result = true;
                    }
                }
            }
        }
        return result;
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
        } else {
            // deactivate
            pattern = null;
            if (active) {
                viewer.removeFilter(this);
            }
        }

    }

    public boolean isActive() {
        return getPattern() != null && getPattern().length > 0;
    }

	public boolean isFilterOrg() {
		return filterOrgs;
	}

	public void setFilterOrgs(boolean filterOrgs) {
		this.filterOrgs = filterOrgs;
	}

}
