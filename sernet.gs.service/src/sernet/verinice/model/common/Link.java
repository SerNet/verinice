/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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
package sernet.verinice.model.common;

import java.io.Serializable;


/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class Link implements Serializable {

    private CnATreeElement from;
    private CnATreeElement to;
    private String relationId;
    private String comment;
    
    /**
     * @param from The dependant
     * @param to The dependency
     */
    public Link(CnATreeElement from, CnATreeElement to, String relationId, String comment) {
        super();
        this.from = from;
        this.to = to;
        this.relationId = relationId;
        this.comment = comment;
    }
    
    public Link(CnATreeElement from, CnATreeElement to, String relationId) {
        this(from, to, relationId, "");
    }
    
    public Link(CnATreeElement from, CnATreeElement to) {
        this(from, to, "", "");
    }
    
    public CnATreeElement getFrom() {
        return from;
    }
    public void setFrom(CnATreeElement from) {
        this.from = from;
    }
    public CnATreeElement getTo() {
        return to;
    }
    public void setTo(CnATreeElement to) {
        this.to = to;
    }
    public String getRelationId() {
        return relationId;
    }
    public void setRelationId(String relationId) {
        this.relationId = relationId;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    
}
