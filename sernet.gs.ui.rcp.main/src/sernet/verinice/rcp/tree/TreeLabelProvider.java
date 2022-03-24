/*******************************************************************************
 * Copyright (c) 2009  Daniel Murygin <dm[at]sernet[dot]de>,
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
 *      Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.tree;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;

import sernet.gs.service.StringUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.views.CnAImageProvider;
import sernet.gs.ui.rcp.main.common.model.CnATreeElementLabelGenerator;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.ITargetObject;
import sernet.hui.swt.SWTResourceManager;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.bp.CheckHasReferencesCommand;

/**
 * Label provider for ISO 27000 model elements.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class TreeLabelProvider extends LabelProvider implements IColorProvider, IFontProvider {

    private static final Logger LOG = Logger.getLogger(TreeLabelProvider.class);
    private static final int MAX_TEXT_WIDTH = 80;

    private Object currentElement;
    private boolean currentElementHasReferences;
    private Font fontElementWithReferences;
    private Color colorElementWithReferences;

    public TreeLabelProvider() {
        Font defaultFont = JFaceResources.getDefaultFont();
        FontData fontData = defaultFont.getFontData()[0];
        fontElementWithReferences = SWTResourceManager.getFont(fontData.getName(),
                fontData.getHeight(), SWT.ITALIC);
        colorElementWithReferences = SWTResourceManager.getColor(80, 95, 121);
    }

    @Override
    public Image getImage(Object obj) {
        Image image = ImageCache.getInstance().getImage(ImageCache.UNKNOWN);
        try {
            if (!(obj instanceof CnATreeElement)) {
                return image;
            } else {
                return CnAImageProvider.getImage((CnATreeElement) obj);
            }
        } catch (Exception e) {
            LOG.error("Error while getting image for tree item.", e);
            return image;
        }
    }

    @Override
    public String getText(Object obj) {
        String text = "unknown";
        if (!(obj instanceof CnATreeElement)) {
            return text;
        }
        try {
            CnATreeElement element = (CnATreeElement) obj;
            String title = CnATreeElementLabelGenerator.getElementTitle(element);
            text = StringUtil.truncate(title, MAX_TEXT_WIDTH);
            if (LOG.isDebugEnabled()) {
                text = text + " (db: " + element.getDbId() + ", uu: " + element.getUuid()
                        + ", scope: " + element.getScopeId() + ", ext: " + element.getExtId() + ")";
            }
        } catch (Exception e) {
            LOG.error("Error while getting label for tree item.", e);
        }
        return text;
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
