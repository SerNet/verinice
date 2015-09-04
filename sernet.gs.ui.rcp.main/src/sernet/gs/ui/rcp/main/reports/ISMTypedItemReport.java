/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak[at]sernet[dot]de>.
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
package sernet.gs.ui.rcp.main.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import sernet.gs.ui.rcp.office.IOOTableRow;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.model.iso27k.Organization;

/**
 * Export all items of the given CnaTreeElementType.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class ISMTypedItemReport extends BsiReport
	implements IBSIReport, ISMReport {

	public ISMTypedItemReport(Properties reportProperties) {
		super(reportProperties);
	}

	private List<CnATreeElement> items;
	private Organization organization;
	private String entityTypeId;
	
	public String getEntityTypeId() {
		return entityTypeId;
	}

	public void setEntityTypeId(String entityTypeId) {
		this.entityTypeId = entityTypeId;
	}

	public String getTitle() {
		return "[ISM] Export all items of selected type";
	}
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.reports.BsiReport#isDefaultColumn(java.lang.String)
	 */
	@Override
	public boolean isDefaultColumn(String propertyId) {
	    // include titles and abbreviations as default columns for all elements:
	    if (propertyId.indexOf("_name") >-1 ||
	            propertyId.indexOf("_abbr") >-1 ) {
	        return true;
	    }
	    
	    // include state for controls:
	    if (propertyId.indexOf("_implemented") >-1 
             || propertyId.indexOf(Control.PROP_MATURITY) >-1 
	        || propertyId.indexOf(Control.PROP_WEIGHT1) >-1 
	        || propertyId.indexOf(Control.PROP_WEIGHT2) >-1 
	        || propertyId.indexOf(Control.PROP_THRESHOLD1) >-1 
	         || propertyId.indexOf(Control.PROP_THRESHOLD2) >-1 ) {
            return true;
        }
	    
	    return false;
	}

	public List<CnATreeElement> getItems() {
		if (items != null){
			return items;
		}
		items = new ArrayList<CnATreeElement>();
		
		Organization org = getOrganization();
		addAllItems(org);
		Collections.sort(items, new ISMItemComparator());
		return items;
	}

	
	/**
	 * Recursively add all children.
	 * @param verbund
	 */
	private void addAllItems(CnATreeElement elmt) {
		if (elmt.getEntityType().getId().equals(entityTypeId)) {
			addAllSubitems(elmt);
		} 
		for (CnATreeElement child: elmt.getChildren()) {
			addAllItems(child);
		}
	}

	/**
     * @param elmt
     */
    private void addAllSubitems(CnATreeElement elmt) {
        if (!items.contains(elmt)){
            items.add(elmt);
        }
        for (CnATreeElement child: elmt.getChildren()) {
            addAllSubitems(child);
        }
    }

    public List<IOOTableRow> getReport(PropertySelection shownColumns) {
        final int maxColumnsPadding = 3;
		ArrayList<IOOTableRow> rows = new ArrayList<IOOTableRow>();
		List<String> columns = new ArrayList<String>(0);
		int maxColumns = 0;
		for (CnATreeElement child : items) {
		    if (!(child instanceof IISO27kGroup)) {
                columns = shownColumns.get(child.getEntity().getEntityType());
                if (columns.size()>maxColumns){
                    maxColumns = columns.size();
                }
                rows.add(new PropertyHeadersRow(
                        child,
                        columns,
                        IOOTableRow.ROW_STYLE_SUBHEADER));
                
                // add header for controlgroups:
                String[] cols = new String[maxColumns+maxColumnsPadding];
                for(int i=0; i < maxColumns; ++i) {
                    cols[i] = "";
                }
                cols[maxColumns] = "Sum: Maturity * weight";
                cols[maxColumns+1] = "Sum: Weight";
                cols[maxColumns+2] = "Weighted maturity level";
                rows.add(new SimpleRow(IOOTableRow.ROW_STYLE_SUBHEADER, cols));
                break;
		    }
		}


		for (CnATreeElement child : items) {
		    if (child instanceof Control) {
		        Control control = (Control) child;
		        columns = shownColumns.get(child.getEntity().getEntityType());
		        if (columns.size()>maxColumns){
                    maxColumns = columns.size();
		        }
		        ControlMaturityRow row = new ControlMaturityRow(
		                control, 
		                columns, 
		                IOOTableRow.ROW_STYLE_ELEMENT);
		        rows.add(new PrefilledRow(row));
		        continue;
		    }
		    else if (child instanceof ControlGroup) {
		        ControlGroup control = (ControlGroup) child;
		        columns = shownColumns.get(child.getEntity().getEntityType());
		        // maxColumns is used to pad controlGroups up to control's columns:
		        if (columns.size()>maxColumns){
                    maxColumns = columns.size();
		        }
		        ControlGroupMaturityRow row = new ControlGroupMaturityRow(
		                control, 
		                columns, 
		                IOOTableRow.ROW_STYLE_ELEMENT, 
		                maxColumns);
		        rows.add(new PrefilledRow(row));
		        continue;
            }
		    else {
		        columns = shownColumns.get(child.getEntity().getEntityType());
		        rows.add(new PropertiesRow(
		                child, 
		                columns, 
		                IOOTableRow.ROW_STYLE_ELEMENT));
		        continue;
		    }
		}
		
		return rows;
	}

	public Organization getOrganization() {
		return organization;
	}
	
	public void setOrganization(Organization org) {
		this.organization = org;
	}	
}