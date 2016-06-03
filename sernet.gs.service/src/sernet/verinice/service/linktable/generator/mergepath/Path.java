/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels.
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
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.linktable.generator.mergepath;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de> 
 *
 */
public class Path {
    
    final private List<PathElement> pathElements;
    
    
    public Path() {
        pathElements = new LinkedList<>();
    }
    
    public void addPathElement(PathElement e){
        pathElements.add(e);
    }
    
    public List<PathElement> getPathElements(){
        return new LinkedList<PathElement>(pathElements);
    }

    @Override
    public String toString() {
        return "Path [pathElements=" + pathElements + "]";
    }
    
    public int getSize(){
        return pathElements.size();
    }
}
