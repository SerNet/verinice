/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
package sernet.verinice.graph;

import sernet.verinice.model.common.CnATreeElement;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class Edge {

    public static final String RELATIVES = "relatives";
    
    private CnATreeElement source;
    
    private CnATreeElement target;
    
    private String type;

    public Edge(CnATreeElement parent, CnATreeElement child) {
        this(parent, child, RELATIVES);
    }
    
    public Edge(CnATreeElement source, CnATreeElement target, String type) {
        super();
        this.source = source;
        this.target = target;
        this.type = type;
    }
   
    public CnATreeElement getSource() {
        return source;
    }

    public void setSource(CnATreeElement source) {
        this.source = source;
    }

    public CnATreeElement getTarget() {
        return target;
    }

    public void setTarget(CnATreeElement target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
}
