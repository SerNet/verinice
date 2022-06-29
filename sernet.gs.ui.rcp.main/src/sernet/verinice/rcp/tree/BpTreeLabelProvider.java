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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.DefaultModelLoadListener;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.ITargetObject;
import sernet.hui.swt.SWTResourceManager;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bp.DefaultBpModelListener;
import sernet.verinice.model.bp.IBpModelListener;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.bp.CheckAllReferencesCommand;
import sernet.verinice.service.commands.bp.CheckHasReferencesCommand;

public class BpTreeLabelProvider extends TreeLabelProvider
        implements IColorProvider, IFontProvider {

    private static final Logger LOG = Logger.getLogger(BpTreeLabelProvider.class);

    private Map<Integer, Boolean> cache = new ConcurrentHashMap<>(10000);

    private Font fontElementWithReferences;
    private Color colorElementWithReferences;

    private final IBpModelListener listener = new DefaultBpModelListener() {

        @Override
        public void linkRemoved(CnALink link) {
            handleLink(link);
        }

        @Override
        public void linkChanged(CnALink old, CnALink link, Object source) {
            handleLink(old);
            handleLink(link);
        }

        @Override
        public void linkAdded(CnALink link) {
            handleLink(link);
        }

        private void handleLink(CnALink link) {
            cache.remove(link.getDependant().getDbId());
            cache.remove(link.getDependency().getDbId());
        }

        @Override
        public void modelReload(BpModel newModel) {
            cache.clear();
            populateCache();
        }

    };

    public BpTreeLabelProvider() {
        super();
        Font defaultFont = JFaceResources.getDefaultFont();
        FontData fontData = defaultFont.getFontData()[0];
        fontElementWithReferences = SWTResourceManager.getFont(fontData.getName(),
                fontData.getHeight(), SWT.ITALIC);
        colorElementWithReferences = SWTResourceManager.getColor(80, 95, 121);
        CnAElementFactory.getInstance().addLoadListener(new DefaultModelLoadListener() {

            @Override
            public void loaded(BpModel model) {
                CnAElementFactory.getInstance().removeLoadListener(this);
                model.addModITBOModelListener(listener);
                populateCache();
            }

        });

    }

    private void populateCache() {
        CheckAllReferencesCommand command = new CheckAllReferencesCommand();
        ICommandService commandService = (ICommandService) VeriniceContext
                .get(VeriniceContext.COMMAND_SERVICE);
        try {
            command = commandService.executeCommand(command);
            cache.putAll(command.getResult());
        } catch (CommandException e) {
            LOG.error("Error populating cache", e);
        }

    }

    @Override
    public void dispose() {
        CnAElementFactory.getInstance().getBpModel().removeBpModelListener(listener);
        super.dispose();
    }

    @Override
    public Color getForeground(Object o) {
        CnATreeElement element = (CnATreeElement) o;
        boolean currentElementHasReferences = element instanceof ITargetObject
                && cache.computeIfAbsent(element.getDbId(), this::checkHasReferences);
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
    public Font getFont(Object o) {
        CnATreeElement element = (CnATreeElement) o;
        boolean currentElementHasReferences = element instanceof ITargetObject
                && cache.computeIfAbsent(element.getDbId(), this::checkHasReferences);
        if (currentElementHasReferences) {
            return fontElementWithReferences;
        }
        return null;
    }

    private boolean checkHasReferences(Integer elementId) {
        CheckHasReferencesCommand command = new CheckHasReferencesCommand(elementId);
        ICommandService commandService = (ICommandService) VeriniceContext
                .get(VeriniceContext.COMMAND_SERVICE);
        try {
            command = commandService.executeCommand(command);
            return command.getResult();
        } catch (CommandException e) {
            LOG.error("Cannot determine referencing status for " + elementId, e);
            return false;
        }
    }
}
