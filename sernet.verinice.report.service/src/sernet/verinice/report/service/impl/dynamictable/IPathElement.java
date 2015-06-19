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
package sernet.verinice.report.service.impl.dynamictable;

import java.util.Map;

import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IPathElement {

    public static final char DELIMITER_LINK = '/';
    public static final char DELIMITER_CHILD = '>';
    public static final char DELIMITER_PARENT = '<';
    public static final char DELIMITER_PROPERTY = '.';
    

    
    /**
     * @param nextToken
     */
    void setTypeId(String nextToken);
    
    /**
     * @param element 
     * @param graph
     */
    void load(CnATreeElement element, VeriniceGraph graph);
    
    IPathElement getChild();
    void setChild(IPathElement child);

    Map<String, Map<String, Object>> getResult();
    
    Map<String, String> createValueMap(Map<String, String> map, String key);

}
