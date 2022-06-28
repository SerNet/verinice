/*******************************************************************************
 * Copyright (c) 2022 Jochen Kemnade <jk{a}sernet{dot}de>.
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
 ******************************************************************************/

package sernet.verinice.rcp.tree;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.ITargetObject;
import sernet.hui.swt.SWTResourceManager;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.bp.CheckHasReferencesCommand;

public class BpTreeLabelProvider extends TreeLabelProvider
        implements IColorProvider, IFontProvider {

    private static final Logger LOG = Logger.getLogger(BpTreeLabelProvider.class);

    private Object currentElement;
    private boolean currentElementHasReferences;
    private Font fontElementWithReferences;
    private Color colorElementWithReferences;

    public BpTreeLabelProvider() {
        Font defaultFont = JFaceResources.getDefaultFont();
        FontData fontData = defaultFont.getFontData()[0];
        fontElementWithReferences = SWTResourceManager.getFont(fontData.getName(),
                fontData.getHeight(), SWT.ITALIC);
        colorElementWithReferences = SWTResourceManager.getColor(80, 95, 121);
    }

    @Override
    public Color getForeground(Object element) {
        updateResultIfOutdated(element);
        if (currentElementHasReferences) {
            return colorElementWithReferences;
        }
        return null;
    }

    @Override
    public Color getBackground(Object element) {
        return null;
    }

    @Override
    public Font getFont(Object element) {
        updateResultIfOutdated(element);
        if (currentElementHasReferences) {
            return fontElementWithReferences;
        }
        return null;
    }

    private void updateResultIfOutdated(Object element) {
        if (element != currentElement) {
            currentElement = element;
            currentElementHasReferences = false;
            if (element instanceof ITargetObject) {
                CnATreeElement targetObject = (CnATreeElement) element;
                CheckHasReferencesCommand command = new CheckHasReferencesCommand(
                        targetObject.getDbId());
                ICommandService commandService = (ICommandService) VeriniceContext
                        .get(VeriniceContext.COMMAND_SERVICE);
                try {
                    command = commandService.executeCommand(command);
                    currentElementHasReferences = command.getResult();
                } catch (CommandException e) {
                    LOG.error("Cannot determine referencing status for " + element, e);
                }
            }
        }
    }
}
