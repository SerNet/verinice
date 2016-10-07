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
package sernet.verinice.service.linktable;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class LinkTableResult {

    private int edgeId;
    private int elementId;
    private Object result;

    public LinkTableResult(int edgeId, int elementId, Object result) {
        super();
        this.edgeId = edgeId;
        this.elementId = elementId;
        this.result = result;
    }

    public int getEdgeId() {
        return edgeId;
    }

    public int getElementId() {
        return elementId;
    }

    public Object getResult() {
        return result;
    }

}
