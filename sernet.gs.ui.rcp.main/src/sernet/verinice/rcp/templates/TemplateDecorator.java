/******************************************************************************* 
 * Copyright (c) 2016 Viktor Schmidt. 
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
 *     Viktor Schmidt <vschmidt[at]ckc[dot]de> - initial API and implementation 
 ******************************************************************************/ 
package sernet.verinice.rcp.templates;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.model.common.CnATreeElement;

/** 
 * Decorator for a new template module and safeguard
 * 
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de> 
 */ 
public class TemplateDecorator extends LabelProvider implements ILightweightLabelDecorator {

    public static final String IMAGE_PATH = "overlays/sample_decorator.gif";

    public void decorate(Object o, IDecoration decoration) {
        Activator.inheritVeriniceContextState();
        if (o instanceof CnATreeElement) {
            CnATreeElement element = (CnATreeElement) o;
            if (element.isTemplate()) {
                decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(IMAGE_PATH));
            }
        }
    }
}
