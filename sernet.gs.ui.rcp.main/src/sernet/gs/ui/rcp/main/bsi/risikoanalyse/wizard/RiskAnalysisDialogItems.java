/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Container for the RiskAnalysisDialog Items to ensure support of generic types
 * in the Dialog
 * 
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class RiskAnalysisDialogItems<T> extends ArrayList<T> {

    private static final long serialVersionUID = -8819960766641466844L;
    private Class<T> genericType;

    public RiskAnalysisDialogItems(Class<T> c) {
        this.genericType = c;
    }

    public RiskAnalysisDialogItems(List<T> items, Class<T> c) {
        super(items);
        this.genericType = c;
    }

    public Class<T> getGenericType() {
        return genericType;
    }
}
