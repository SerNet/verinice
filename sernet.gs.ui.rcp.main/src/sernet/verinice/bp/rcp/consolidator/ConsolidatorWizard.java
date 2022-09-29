/*******************************************************************************
 * Copyright (c) 2020 Finn Westendorf
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
package sernet.verinice.bp.rcp.consolidator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import sernet.gs.ui.rcp.main.common.model.CnATreeElementScopeUtils;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.service.commands.bp.ConsoliData;
import sernet.verinice.service.commands.crud.LoadModulesWithParentsAndScope;

/**
 * ConsolidatorWizard is the UI of the consolidator for MoGS.
 * 
 * Right click on a module and click consolidator, to use that module as basis
 * for the consolidator. The consolidator will search for and show other modules
 * with that identifier. After selecting the modules you want to overwrite, you
 * can select which properties of the module and/or attached threats and
 * safeguards you want to consolidate.
 */
public class ConsolidatorWizard extends Wizard {

    private static final Logger logger = Logger.getLogger(ConsolidatorWizard.class);

    @SuppressWarnings("null")
    @NonNull
    Collection<Entry<BpRequirementGroup, ItNetwork>> potentialTargeRequirementGroupsAndScopes = Collections
            .emptySet();
    final @NonNull BpRequirementGroup sourceModule;
    @SuppressWarnings("null")
    private @NonNull WritableSet<@NonNull ConsolidatorTableContent> selectedModules = WritableSet
            .withElementType(ConsolidatorTableContent.class);
    private @NonNull Set<String> selectedPropertyGroups = new HashSet<>();

    public ConsolidatorWizard(@NonNull BpRequirementGroup sourceModule) {
        String title = Messages.consolidator;
        if (!sourceModule.getIdentifier().isEmpty()) {
            title += " [" + sourceModule.getIdentifier() + "]";
        }
        setWindowTitle(title);
        this.sourceModule = sourceModule;
    }

    private @NonNull Collection<Entry<BpRequirementGroup, ItNetwork>> findOtherModules(
            @NonNull BpRequirementGroup module, boolean includeAllScopes) {

        LoadModulesWithParentsAndScope compoundLoader = includeAllScopes
                ? new LoadModulesWithParentsAndScope()
                : new LoadModulesWithParentsAndScope(new Integer[] { module.getScopeId() });
        try {
            compoundLoader = ServiceFactory.lookupCommandService().executeCommand(compoundLoader);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Collections.emptySet();
        }
        VeriniceGraph graph = compoundLoader.getGraph();
        Stream<BpRequirementGroup> s = graph.getElements(BpRequirementGroup.class).stream()
                .filter(x -> x.getIdentifier().equals(module.getIdentifier()))
                .filter(x -> !x.equals(module)).filter(x -> !CatalogModel.TYPE_ID
                        .equals(CnATreeElementScopeUtils.getScope(x).getParent().getTypeId()));
        if (!includeAllScopes) {
            s = s.filter(x -> x.getScopeId().equals(module.getScopeId()));
        }

        @NonNull
        Map<BpRequirementGroup, ItNetwork> requirementGroupsToContainingScopes = new HashMap<>();
        s.forEach(r -> {
            ItNetwork scope = (ItNetwork) graph.getElement(r.getScopeId());
            requirementGroupsToContainingScopes.putIfAbsent(r, scope);
        });

        return requirementGroupsToContainingScopes.entrySet();
    }

    private boolean askIfShouldIncludeAllScopes() {
        MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        messageBox.setText(Messages.allScopesTitle);
        messageBox.setMessage(Messages.allScopes);
        return messageBox.open() == SWT.YES;
    }

    @Override
    public void addPages() {
        potentialTargeRequirementGroupsAndScopes = findOtherModules(sourceModule,
                askIfShouldIncludeAllScopes());

        @NonNull
        List<@NonNull ConsolidatorTableContent> list = ConsolidatorTableContent
                .getContent(potentialTargeRequirementGroupsAndScopes);
        addPage(new ModuleSelectionPage(this, list));
        addPage(new DataSelectionPage(this));
    }

    @Override
    public boolean performFinish() {
        MessageBox warning = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
        warning.setText(Messages.consolidate);
        warning.setMessage(Messages.consolidatorWarning);
        if (warning.open() == SWT.YES) {
            ConsoliData xdata = new ConsoliData(sourceModule, selectedPropertyGroups,
                    selectedModules.stream().map(x -> x.getModule().getUuid())
                            .collect(Collectors.toSet()));
            String consolidatorError = Consolidator.consolidate(xdata);
            if (consolidatorError != null) {
                MessageBox errorMessage = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                errorMessage.setText(Messages.consolidatorFailed);
                errorMessage.setMessage(consolidatorError);
                errorMessage.open();
            } else {
                logger.debug("ConsolidatorWizard was successful.");
            }
        }
        return true;
    }

    @NonNull
    Set<String> getSelectedPropertyGroups() {
        return selectedPropertyGroups;
    }

    WritableSet<ConsolidatorTableContent> getSelectedModules() {
        return selectedModules;
    }
}
