/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.service.Retriever;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.common.model.CnATreeElementScopeUtils;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.bp.rcp.filter.ThreatByProceedingFilterUtil;
import sernet.verinice.model.bp.IBpModelListener;
import sernet.verinice.model.bp.ISecurityLevelProvider;
import sernet.verinice.model.bp.Proceeding;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.NullListener;
import sernet.verinice.model.iso27k.IISO27KModelListener;
import sernet.verinice.model.iso27k.ISO27KModel;

/**
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class RelationViewContentProvider extends NullListener implements IStructuredContentProvider,
        IBSIModelListener, IISO27KModelListener, IBpModelListener {

    private IRelationTable view;
    private TableViewer viewer;

    public RelationViewContentProvider(IRelationTable view, TableViewer viewer) {
        this.view = view;
        this.viewer = viewer;
    }

    @Override
    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        if (newInput instanceof PlaceHolder) {
            return;
        }
        CnATreeElement inputElmt = (CnATreeElement) newInput;
        view.setInputElmt(inputElmt);

        viewer.refresh();
    }

    public Object[] getElements(Object obj) {
        if (obj instanceof PlaceHolder) {
            return new Object[] { obj };
        }

        if (view == null || view.getInputElmt() == null) {
            return new Object[] {};
        }
        CnATreeElement inputElement = view.getInputElmt();
        Set<CnALink> linksDown = inputElement.getLinksDown();
        Set<CnALink> linksUp = inputElement.getLinksUp();
        if (linksDown.isEmpty() && linksUp.isEmpty()) {
            return new Object[] {};
        }
        Stream<CnALink> linksDownStream = linksDown.stream();
        Stream<CnALink> linksUpStream = linksUp.stream();
        boolean filterByProceeding = Activator.getDefault().getPreferenceStore()
                .getBoolean(PreferenceConstants.FILTER_INFORMATION_NETWORKS_BY_PROCEEDING);
        if (filterByProceeding) {
            CnATreeElement scope = CnATreeElementScopeUtils.getScope(inputElement);

            if (scope instanceof ItNetwork) {
                ItNetwork itNetwork = (ItNetwork) scope;
                itNetwork = (ItNetwork) Retriever.checkRetrieveElement(itNetwork);
                Proceeding proceeding = itNetwork.getProceeding();
                if (proceeding != null) {
                    linksDownStream = filterLinksByProceeding(linksDownStream,
                            CnALink::getDependency, proceeding);
                    linksUpStream = filterLinksByProceeding(linksUpStream, CnALink::getDependant,
                            proceeding);
                }
            }
        }
        return Stream.concat(linksDownStream, linksUpStream).toArray();
    }

    private static Stream<CnALink> filterLinksByProceeding(Stream<CnALink> links,
            Function<CnALink, CnATreeElement> elementExtractor, Proceeding proceeding) {
        return links.filter(link -> {
            CnATreeElement element = elementExtractor.apply(link);
            if (element instanceof BpThreat) {
                return ThreatByProceedingFilterUtil
                        .showThreatWhenProceedingFilterIsEnabled((BpThreat) element);
            }
            if (element instanceof ISecurityLevelProvider) {
                return proceeding.requires(((ISecurityLevelProvider) element).getSecurityLevel());
            }
            return true;
        });

    }

    /*
     * @see
     * sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#childChanged(sernet.gs.
     * ui.rcp.main.common.model.CnATreeElement,
     * sernet.gs.ui.rcp.main.common.model.CnATreeElement)
     */
    @Override
    public void childChanged(CnATreeElement child) {
        // reload because a title may have changed
        view.reloadAll();
    }

    /*
     * @see
     * sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#databaseChildRemoved(
     * sernet.gs.ui.rcp.main.common.model.CnATreeElement)
     */
    @Override
    public void databaseChildRemoved(CnATreeElement child) {
        view.reloadAll();
    }

    /*
     * @see
     * sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#linkAdded(sernet.gs.ui.
     * rcp.main.common.model.CnALink)
     */
    @Override
    public void linksAdded(Collection<CnALink> links) {
        view.reloadAll();
    }

    /*
     * @see
     * sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#linkChanged(sernet.gs.
     * ui.rcp.main.common.model.CnALink)
     */
    @Override
    public void linkChanged(CnALink old, CnALink link, Object source) {
        if (view.equals(source)) {
            view.reload(old, link);
        } else {
            view.reloadAll();
        }
    }

    /*
     * @see
     * sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#linkRemoved(sernet.gs.
     * ui.rcp.main.common.model.CnALink)
     */
    @Override
    public void linkRemoved(CnALink link) {
        view.reloadAll();
    }

    /*
     * @see
     * sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#modelRefresh(java.lang.
     * Object)
     */
    @Override
    public void modelRefresh(Object source) {
        view.reloadAll();
    }

    /*
     * @see
     * sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#modelReload(sernet.gs.
     * ui.rcp.main.bsi.model.BSIModel)
     */
    @Override
    public void modelReload(BSIModel newModel) {
        view.reloadAll();
    }

    /*
     * @see
     * sernet.verinice.iso27k.model.IISO27KModelListener#modelReload(sernet.
     * verinice.iso27k.model.ISO27KModel)
     */
    @Override
    public void modelReload(ISO27KModel newModel) {
        view.reloadAll();
    }

    /*
     * @see
     * sernet.verinice.model.iso27k.IBpModelListener#modelReload(sernet.verinice
     * .model.bp.elements.BpModel)
     */
    @Override
    public void modelReload(BpModel newModel) {
        view.reloadAll();
    }
}
