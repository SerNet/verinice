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

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.service.commands.bp.ConsolidatorCheckPermissionsCommand;
import sernet.verinice.service.commands.bp.ConsolidatorCheckPermissionsCommand.PermissionDeniedReason;

/**
 * This is a page of the consolidator to select which data to consolidate.
 * <p>
 * This collects a list of property group ids. To represent the general info
 * that isn't in any group, it uses the pseudo-IDs *_general.
 */
public class DataSelectionPage extends WizardPage {
    private static final String PROP_GRP_BP_REQUIREMENT_GROUP_GENERAL = "bp_requirement_group_general"; //$NON-NLS-1$
    private static final String PROP_GRP_BP_REQUIREMENT_GENERAL = "bp_requirement_general"; //$NON-NLS-1$
    private static final String PROP_GRP_BP_SAFEGUARD_GENERAL = "bp_safeguard_general"; //$NON-NLS-1$
    private static final String PROP_GRP_BP_THREAT_GENERAL = "bp_threat_general"; //$NON-NLS-1$

    ConsolidatorWizard wizard;
    Composite composite;

    OptionListener selectionListener = new OptionListener();

    private final class OptionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            Button b = (Button) e.getSource();
            String option = (String) b.getData();
            if (b.getSelection()) {
                wizard.getSelectedPropertyGroups().add(option);
            } else {
                wizard.getSelectedPropertyGroups().remove(option);
            }

            checkPermissions();
        }
    }

    public DataSelectionPage(@NonNull ConsolidatorWizard wizard) {
        super("wizardPage"); //$NON-NLS-1$
        setTitle(Messages.dataSelection);
        setDescription(Messages.selectTheDataToBeConsolidated);
        setPageComplete(false);
        this.wizard = wizard;
        wizard.getSelectedModules().addChangeListener(event -> {
            checkPermissions();
            getContainer().updateButtons();
        });
    }

    private Group createGroup(String title) {
        Group g = new Group(composite, SWT.NONE);
        g.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        g.setText(title);
        g.setLayout(new GridLayout(1, false));
        return g;
    }

    private void createCheckbox(Group group, String title, String propertyId, boolean selected) {
        Button b = new Button(group, SWT.CHECK);
        b.addSelectionListener(selectionListener);
        b.setText(title);
        b.setData(propertyId);
        if (selected) {
            b.setSelection(true);
            wizard.getSelectedPropertyGroups().add(propertyId);
        }
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        setControl(container);
        container.setLayout(new GridLayout(1, false));

        ScrolledComposite scrolledComposite = new ScrolledComposite(container,
                SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        composite = new Composite(scrolledComposite, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        Group moduleGrp = createGroup(Messages.module);
        createCheckbox(moduleGrp, Messages.general, PROP_GRP_BP_REQUIREMENT_GROUP_GENERAL, true);

        Group requirementGrp = createGroup(Messages.requirement);
        createCheckbox(requirementGrp, Messages.general, PROP_GRP_BP_REQUIREMENT_GENERAL, true);
        createCheckbox(requirementGrp, Messages.implementation,
                BpRequirement.PROP_GRP_IMPLEMENTATION, true);
        createCheckbox(requirementGrp, Messages.costs, BpRequirement.PROP_GRP_KOSTEN, false);
        createCheckbox(requirementGrp, Messages.dataProtection,
                BpRequirement.PROP_GRP_DATA_PROTECTION_OBJECTIVES_EUGDPR, false);
        createCheckbox(requirementGrp, Messages.kix, BpRequirement.PROP_GRP_KIX, false);
        createCheckbox(requirementGrp, Messages.audit, BpRequirement.PROP_GRP_AUDIT, false);
        createCheckbox(requirementGrp, Messages.revision, BpRequirement.PROP_GRP_REVISION, false);

        Group threatGrp = createGroup(Messages.threat);
        createCheckbox(threatGrp, Messages.general, PROP_GRP_BP_THREAT_GENERAL, true);
        createCheckbox(threatGrp, Messages.riskWithout, BpThreat.PROP_GRP_RISK_WITHOUT_SAFEGUARDS,
                false);
        createCheckbox(threatGrp, Messages.riskWithoudAdditional,
                BpThreat.PROP_GRP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS, true);
        createCheckbox(threatGrp, Messages.riskTreatment,
                BpThreat.PROP_GRP_RISK_TREATMENT_OPTION_GROUP, true);
        createCheckbox(threatGrp, Messages.riskWithAdditional,
                BpThreat.PROP_GRP_RISK_WITH_ADDITIONAL_SAFEGUARDS, true);

        Group safeguardGrp = createGroup(Messages.safeguard);
        createCheckbox(safeguardGrp, Messages.general, PROP_GRP_BP_SAFEGUARD_GENERAL, true);
        createCheckbox(safeguardGrp, Messages.implementation, Safeguard.PROP_GRP_IMPLEMENTATION,
                true);
        createCheckbox(safeguardGrp, Messages.costs, Safeguard.PROP_GRP_GROUP_KOSTEN, false);
        createCheckbox(safeguardGrp, Messages.dataProtection,
                Safeguard.PROP_GRP_DATA_PROTECTION_OBJECTIVES_EUGDPR, false);
        createCheckbox(safeguardGrp, Messages.kix, Safeguard.PROP_GRP_KIX, false);
        createCheckbox(safeguardGrp, Messages.revision, Safeguard.PROP_GRP_REVISION, false);

        scrolledComposite.setContent(composite);
        scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            checkPermissions();
        }
    }

    private void checkPermissions() {
        if (!Activator.getDefault().isStandalone()) {
            setErrorMessage(null);
            WritableSet<ConsolidatorTableContent> selectedModules = wizard.getSelectedModules();

            Set<Integer> moduleIDs = selectedModules.stream().map(it -> it.getModule().getDbId())
                    .collect(Collectors.toSet());

            boolean needsWritePermissionOnModule = wizard.getSelectedPropertyGroups().stream()
                    .anyMatch(it -> it.startsWith(BpRequirementGroup.TYPE_ID));
            boolean needsWritePermissionOnRequirements = wizard.getSelectedPropertyGroups().stream()
                    .anyMatch(it -> it.startsWith(BpRequirement.TYPE_ID)
                            && !it.startsWith(BpRequirementGroup.TYPE_ID));
            boolean needsWritePermissionOnSafeguards = wizard.getSelectedPropertyGroups().stream()
                    .anyMatch(it -> it.startsWith(Safeguard.TYPE_ID));
            boolean needsWritePermissionOnThreats = wizard.getSelectedPropertyGroups().stream()
                    .anyMatch(it -> it.startsWith(BpThreat.TYPE_ID));

            ConsolidatorCheckPermissionsCommand checkPermissionsCommand = new ConsolidatorCheckPermissionsCommand(
                    moduleIDs, needsWritePermissionOnModule, needsWritePermissionOnRequirements,
                    needsWritePermissionOnSafeguards, needsWritePermissionOnThreats);
            try {
                checkPermissionsCommand = ServiceFactory.lookupCommandService()
                        .executeCommand(checkPermissionsCommand);
                Map<Integer, PermissionDeniedReason> permissionIssues = checkPermissionsCommand
                        .getPermissionIssues();
                boolean issuesFound = !permissionIssues.isEmpty();
                setPageComplete(!issuesFound);

                if (issuesFound) {
                    Set<PermissionDeniedReason> reasons = permissionIssues.values().stream()
                            .collect(Collectors.toSet());
                    if (reasons.contains(PermissionDeniedReason.MODULE)) {
                        setErrorMessage(Messages.DataSelectionPage_PermissionError_Modules);
                    } else if (reasons.contains(PermissionDeniedReason.REQUIREMENTS)) {
                        setErrorMessage(Messages.DataSelectionPage_PermissionError_Requirements);
                    } else if (reasons.contains(PermissionDeniedReason.LINKED_OBJECTS)) {
                        setErrorMessage(Messages.DataSelectionPage_PermissionError_LinkedObjects);
                    } else {
                        throw new IllegalStateException("Unhandled reasons: " + reasons); //$NON-NLS-1$
                    }
                }
            } catch (CommandException ex) {
                throw new RuntimeCommandException("Error checking write permissions", ex); //$NON-NLS-1$
            }
        }
    }
}