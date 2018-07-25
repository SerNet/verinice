/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
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
 ******************************************************************************/
package sernet.verinice.bp.rcp.filter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.SecurityLevel;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BpDocument;
import sernet.verinice.model.bp.elements.BpIncident;
import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bp.elements.BpRecord;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.bp.elements.Device;
import sernet.verinice.model.bp.elements.IcsSystem;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.elements.Network;
import sernet.verinice.model.bp.elements.Room;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.common.ElementFilter;

/**
 * The filter dialog for the base protection view
 */
public class BaseProtectionFilterDialog extends Dialog {

    private static final String[] BASE_PROTECTION_TYPES = new String[] { BusinessProcess.TYPE_ID,
            Application.TYPE_ID, ItSystem.TYPE_ID, IcsSystem.TYPE_ID, Device.TYPE_ID,
            Network.TYPE_ID, Room.TYPE_ID, BpPerson.TYPE_ID, BpRequirement.TYPE_ID,
            BpThreat.TYPE_ID, Safeguard.TYPE_ID, BpDocument.TYPE_ID, BpIncident.TYPE_ID,
            BpRecord.TYPE_ID };

    private static final int VIEWER_TABLE_WIDTH = 470;
    private static final int VIEWER_TABLE_HEIGHT = 135;

    private Set<Button> implementationStatusButtons = new HashSet<>();

    private Set<Button> qualifierButtons = new HashSet<>();

    private CheckboxTableViewer elementTypeSelector;

    private CheckboxTableViewer tagsSelector;

    private Button applyTagFilterToItNetworksCheckbox;
    private Button hideEmptyGroupsCheckbox;
    private Button filterByProceedingCheckbox;

    private @NonNull BaseProtectionFilterParameters filterParameters;
    private final @NonNull BaseProtectionFilterParameters defaultFilterParams;

    /**
     * @param hideEmptyGroupsByDefault
     *            this value is used when the filter is cleared (reset button).
     */
    public BaseProtectionFilterDialog(Shell parentShell,
            @NonNull BaseProtectionFilterParameters filterParameters,
            @NonNull BaseProtectionFilterParameters defaultFilterParams) {
        super(parentShell);
        this.filterParameters = filterParameters;
        this.defaultFilterParams = defaultFilterParams;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.BaseProtectionFilterDialog_Title);
    }

    /**
     * Create contents of the dialog.
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);

        Label intro = new Label(container, SWT.NONE);
        intro.setText(Messages.BaseProtectionFilterDialog_IntroText);

        addFilterByProceeding(container);
        addImplementationStatusGroup(container);
        addQualiferGroup(container);
        addElementTypesGroup(container);
        try {
            addTagsGroup(container);
        } catch (CommandException e) {
            throw new RuntimeException("Failed to initialize filter", e);
        }
        addApplyTagFilterToItNetworksGroup(container);
        addHideEmptyGroup(container);

        setValues(filterParameters);
        return container;
    }

    private void addImplementationStatusGroup(Composite parent) {
        Group boxesComposite = new Group(parent, SWT.BORDER);
        boxesComposite.setText(Messages.BaseProtectionFilterDialog_ImplementationState);
        GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1);
        boxesComposite.setLayoutData(gridData);
        GridLayout layout = new GridLayout(ImplementationStatus.values().length, false);
        boxesComposite.setLayout(layout);

        for (final ImplementationStatus status : ImplementationStatus.values()) {
            final Button button = new Button(boxesComposite, SWT.CHECK);
            button.setText(status.getLabel());
            button.setData(status);
            button.setSelection(filterParameters.getImplementationStatuses().contains(status));
            implementationStatusButtons.add(button);
        }
    }

    private void addQualiferGroup(Composite parent) {
        Group boxesComposite = new Group(parent, SWT.BORDER);
        boxesComposite.setText(Messages.BaseProtectionFilterDialog_Qualifier);
        GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1);
        boxesComposite.setLayoutData(gridData);
        GridLayout layout = new GridLayout(SecurityLevel.values().length + 1, false);
        boxesComposite.setLayout(layout);

        for (final SecurityLevel qualifier : SecurityLevel.values()) {
            addButton(boxesComposite, qualifier);
        }
        addButton(boxesComposite, null);
    }

    private void addButton(Group boxesComposite, final SecurityLevel qualifier) {
        final Button button = new Button(boxesComposite, SWT.CHECK);
        button.setText(qualifier == null ? Messages.BaseProtectionFilterDialog_Property_Value_Null
                : qualifier.getLabel());
        button.setData(qualifier);
        qualifierButtons.add(button);
    }

    private void addElementTypesGroup(Composite container) {
        Group elementTypes = new Group(container, SWT.NONE);
        elementTypes.setText(Messages.BaseProtectionFilterDialog_Objects);
        GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1);
        elementTypes.setLayoutData(gridData);
        elementTypes.setLayout(new GridLayout());

        ScrolledComposite scrolledComposite = new ScrolledComposite(elementTypes, SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        scrolledComposite.setExpandHorizontal(true);

        elementTypeSelector = CheckboxTableViewer.newCheckList(scrolledComposite, SWT.BORDER);
        Table table = elementTypeSelector.getTable();
        table.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false));

        elementTypeSelector.setContentProvider(ArrayContentProvider.getInstance());
        elementTypeSelector.setInput(BASE_PROTECTION_TYPES);
        elementTypeSelector.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return getTypeFactory().getMessage((String) element);
            }

            @Override
            public Image getImage(Object element) {
                return ImageCache.getInstance().getImageForTypeId((String) element);
            }
        });

        scrolledComposite.setContent(table);
        table.setSize(VIEWER_TABLE_WIDTH, VIEWER_TABLE_HEIGHT);

    }

    private void addTagsGroup(Composite container) throws CommandException {
        Group tags = new Group(container, SWT.NONE);
        tags.setText(Messages.BaseProtectionFilterDialog_Tags);
        GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1);
        tags.setLayoutData(gridData);
        tags.setLayout(new GridLayout());

        ScrolledComposite scrolledComposite = new ScrolledComposite(tags, SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        scrolledComposite.setExpandHorizontal(true);

        tagsSelector = CheckboxTableViewer.newCheckList(scrolledComposite, SWT.BORDER);
        Table table = tagsSelector.getTable();
        table.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false));

        tagsSelector.setContentProvider(ArrayContentProvider.getInstance());
        List<String> availableTags = CnAElementHome.getInstance().getTags();
        availableTags.add(0, ElementFilter.NO_TAG);
        tagsSelector.setInput(availableTags);
        tagsSelector.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return (String) element;
            }
        });

        scrolledComposite.setContent(table);
        table.setSize(VIEWER_TABLE_WIDTH, VIEWER_TABLE_HEIGHT);

    }

    private void addApplyTagFilterToItNetworksGroup(Composite parent) {
        Group groupComposite = new Group(parent, SWT.BORDER);
        GridData gridData = new GridData(GridData.FILL, GridData.END, true, false);
        groupComposite.setLayoutData(gridData);
        groupComposite.setLayout(new GridLayout(1, false));
        applyTagFilterToItNetworksCheckbox = new Button(groupComposite, SWT.CHECK);
        applyTagFilterToItNetworksCheckbox
                .setText(Messages.BaseProtectionFilterDialog_Apply_Tag_Filter_To_IT_Networks);
    }

    private void addHideEmptyGroup(Composite parent) {
        Group groupComposite = new Group(parent, SWT.BORDER);
        GridData gridData = new GridData(GridData.FILL, GridData.END, true, false);
        groupComposite.setLayoutData(gridData);
        groupComposite.setLayout(new GridLayout(1, false));
        hideEmptyGroupsCheckbox = new Button(groupComposite, SWT.CHECK);
        hideEmptyGroupsCheckbox.setText(Messages.BaseProtectionFilterDialog_Hide_Empty_Groups);
    }

    private void setValues(BaseProtectionFilterParameters params) {
        for (Button button : implementationStatusButtons) {
            boolean isSelected = params.getImplementationStatuses().contains(button.getData());
            button.setSelection(isSelected);
        }
        for (Button button : qualifierButtons) {
            boolean isSelected = params.getSecurityLevels().contains(button.getData());
            button.setSelection(isSelected);
        }
        elementTypeSelector.setCheckedElements(
                params.getElementTypes().toArray(new String[params.getElementTypes().size()]));
        tagsSelector.setCheckedElements(
                params.getTags().toArray(new String[params.getTags().size()]));
        applyTagFilterToItNetworksCheckbox.setSelection(params.isApplyTagFilterToItNetworks());
        hideEmptyGroupsCheckbox.setSelection(params.isHideEmptyGroups());
    }

    private void addFilterByProceeding(Composite parent) {
        Group groupComposite = new Group(parent, SWT.BORDER);
        GridData gridData = new GridData(GridData.FILL, GridData.END, true, false);
        groupComposite.setLayoutData(gridData);
        groupComposite.setLayout(new GridLayout(1, false));
        filterByProceedingCheckbox = new Button(groupComposite, SWT.CHECK);
        filterByProceedingCheckbox
                .setText(Messages.BaseProtectionFilterDialog_Filter_By_Proceeding);
        filterByProceedingCheckbox.setSelection(filterParameters.isFilterByNetworkProceeding());
    }

    /**
     * Create contents of the button bar.
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button clearFilterButton = createButton(parent, IDialogConstants.NO_ID,
                Messages.BaseProtectionFilterDialog_Clear, false);
        clearFilterButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setValues(defaultFilterParams);
            }
        });
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    public boolean close() {
        Set<ImplementationStatus> statuses = new HashSet<>(ImplementationStatus.values().length);
        for (Button button : implementationStatusButtons) {
            if (button.getSelection()) {
                statuses.add((ImplementationStatus) button.getData());
            }
        }

        Set<SecurityLevel> levels = new HashSet<>(ImplementationStatus.values().length);
        for (Button button : qualifierButtons) {
            if (button.getSelection()) {
                levels.add((SecurityLevel) button.getData());
            }
        }

        Object[] checkedElements = elementTypeSelector.getCheckedElements();
        Set<String> types = new HashSet<>(checkedElements.length);
        for (Object checkedElementType : checkedElements) {
            types.add((String) checkedElementType);
        }

        Set<String> tags = new HashSet<>();
        for (Object checkedTag : tagsSelector.getCheckedElements()) {
            tags.add((String) checkedTag);
        }

        filterParameters = BaseProtectionFilterParameters.builder()
                .withImplementationStatuses(statuses).withSecurityLevels(levels)
                .withElementTypes(types).withTags(tags)
                .withApplyTagFilterToItNetworks(applyTagFilterToItNetworksCheckbox.getSelection())
                .withFilterByNetworkProceeding(filterByProceedingCheckbox.getSelection())
                .withHideEmptyGroups(hideEmptyGroupsCheckbox.getSelection()).build();

        return super.close();
    }

    private static HUITypeFactory getTypeFactory() {
        return (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
    }

    public @NonNull BaseProtectionFilterParameters getFilterParameters() {
        return filterParameters;
    }
}
