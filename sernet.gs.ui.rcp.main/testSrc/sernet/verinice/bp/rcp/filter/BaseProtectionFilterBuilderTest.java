/*******************************************************************************
 * Copyright (c) 2020 Jochen Kemnade.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.junit.BeforeClass;
import org.junit.Test;

import sernet.gs.ui.rcp.main.Activator;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.snutils.DBException;
import sernet.verinice.bp.rcp.filter.BaseProtectionFilterParameters.Builder;
import sernet.verinice.model.bp.ImplementationStatus;
import sernet.verinice.model.bp.SecurityLevel;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.common.CnATreeElement;

public class BaseProtectionFilterBuilderTest {

    @BeforeClass
    public static void setupTypeFactory() throws DBException {
        HUITypeFactory huiTypeFactory = HUITypeFactory
                .createInstance(BaseProtectionFilterBuilderTest.class
                        .getResource("/" + HUITypeFactory.HUI_CONFIGURATION_FILE));
        VeriniceContext.put(VeriniceContext.HUI_TYPE_FACTORY, huiTypeFactory);
        new Activator();
    }

    @Test
    public void matchingElementIsShownWhenFilteringByImplementationState() {

        BpRequirementGroup rg = new BpRequirementGroup(null);
        BpRequirement req1 = new BpRequirement(rg);
        req1.setImplementationStatus(ImplementationStatus.YES);
        rg.addChild(req1);
        BpRequirement req2 = new BpRequirement(rg);
        rg.addChild(req2);

        Builder parametersBuilder = BaseProtectionFilterParameters.builder()
                .withImplementationStatuses(
                        EnumSet.of(ImplementationStatus.YES, ImplementationStatus.NOT_APPLICABLE));

        assertTrue(elementIsShown(parametersBuilder.withHideEmptyGroups(false).build(), rg));
        assertTrue(elementIsShown(parametersBuilder.withHideEmptyGroups(true).build(), rg));

    }

    @Test
    public void elementWithoutRequirementIsNotHiddenWhenFilteringBySecurityLevel() {
        BaseProtectionFilterParameters parameters = BaseProtectionFilterParameters.builder()
                .withSecurityLevels(EnumSet.of(SecurityLevel.BASIC)).withHideEmptyGroups(false)
                .build();

        ItSystem isSystem = new ItSystem(null);
        assertTrue(elementIsShown(parameters, isSystem));
    }

    @Test
    public void emptyGroupIsShownWhenFilteringByImplementationState() {
        BaseProtectionFilterParameters parameters = BaseProtectionFilterParameters.builder()
                .withImplementationStatuses(
                        EnumSet.of(ImplementationStatus.YES, ImplementationStatus.NOT_APPLICABLE))
                .withHideEmptyGroups(false).build();

        BpRequirementGroup rg = new BpRequirementGroup(null);
        BpRequirement req1 = new BpRequirement(rg);
        rg.addChild(req1);
        BpRequirement req2 = new BpRequirement(rg);
        rg.addChild(req2);

        assertTrue(elementIsShown(parameters, rg));
    }

    @Test
    public void emptyGroupIsHiddenWhenFilteringByImplementationState() {
        BaseProtectionFilterParameters parameters = BaseProtectionFilterParameters.builder()
                .withImplementationStatuses(
                        EnumSet.of(ImplementationStatus.YES, ImplementationStatus.NOT_APPLICABLE))
                .withHideEmptyGroups(true).build();

        BpRequirementGroup rg = new BpRequirementGroup(null);
        BpRequirement req1 = new BpRequirement(rg);
        rg.addChild(req1);
        BpRequirement req2 = new BpRequirement(rg);
        rg.addChild(req2);

        assertFalse(elementIsShown(parameters, rg));
    }

    @Test
    public void emptyGroupIsShownWhenFilteringBySecurityLevel() {
        BaseProtectionFilterParameters parameters = BaseProtectionFilterParameters.builder()
                .withSecurityLevels(EnumSet.of(SecurityLevel.BASIC)).withHideEmptyGroups(false)
                .build();

        BpRequirementGroup rg = new BpRequirementGroup(null);
        BpRequirement req1 = new BpRequirement(rg);
        rg.addChild(req1);

        assertTrue(elementIsShown(parameters, rg));
    }

    @Test
    public void emptyGroupIsHiddenWhenFilteringBySecurityLevel() {
        BaseProtectionFilterParameters parameters = BaseProtectionFilterParameters.builder()
                .withSecurityLevels(EnumSet.of(SecurityLevel.BASIC)).withHideEmptyGroups(true)
                .build();

        BpRequirementGroup rg = new BpRequirementGroup(null);
        BpRequirement req1 = new BpRequirement(rg);
        rg.addChild(req1);

        assertFalse(elementIsShown(parameters, rg));
    }

    @Test
    public void emptyGroupIsHiddenWhenFilteringByImplementationStatusAndSecurityLevel() {
        BaseProtectionFilterParameters parameters = BaseProtectionFilterParameters.builder()
                .withImplementationStatuses(EnumSet.of(ImplementationStatus.YES))
                .withSecurityLevels(EnumSet.of(SecurityLevel.STANDARD)).withHideEmptyGroups(true)
                .build();

        BpRequirementGroup rg = new BpRequirementGroup(null);
        BpRequirement req1 = new BpRequirement(rg);
        rg.addChild(req1);
        req1.setImplementationStatus(ImplementationStatus.YES);
        BpRequirement req2 = new BpRequirement(rg);
        rg.addChild(req2);
        req2.setSecurityLevel(SecurityLevel.STANDARD);

        assertFalse(elementIsShown(parameters, rg));
    }

    @Test
    public void targetObjectIsHiddenWhenFilteringByTypeAndImplementationState() {
        BaseProtectionFilterParameters parameters = BaseProtectionFilterParameters.builder()
                .withImplementationStatuses(EnumSet.of(ImplementationStatus.YES))
                .withElementTypes(Collections.singleton(BpRequirement.TYPE_ID))
                .withHideEmptyGroups(true).build();

        ItSystem itSystem = new ItSystem(null);
        BpRequirementGroup rg = new BpRequirementGroup(itSystem);
        itSystem.addChild(rg);
        BpRequirement req1 = new BpRequirement(rg);
        rg.addChild(req1);
        req1.setImplementationStatus(ImplementationStatus.NO);
        SafeguardGroup sg = new SafeguardGroup(itSystem);
        itSystem.addChild(sg);
        Safeguard s1 = new Safeguard(sg);
        sg.addChild(s1);
        s1.setImplementationStatus(ImplementationStatus.YES);

        assertFalse(elementIsShown(parameters, itSystem));
    }

    private static boolean elementIsShown(BaseProtectionFilterParameters parameters,
            CnATreeElement element) {
        StructuredViewer viewer = new TestViewer();

        Collection<ViewerFilter> filters = BaseProtectionFilterBuilder.makeFilters(parameters,
                new PreferenceStore());
        return filters.stream().allMatch(filter -> filter.select(viewer, null, element));
    }

    private static final class TestViewer extends StructuredViewer {
        @Override
        protected Widget doFindInputItem(Object element) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected Widget doFindItem(Object element) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected List<?> getSelectionFromWidget() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void internalRefresh(Object element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void reveal(Object element) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void setSelectionToWidget(@SuppressWarnings("rawtypes") List l, boolean reveal) {
            throw new UnsupportedOperationException();

        }

        @Override
        public Control getControl() {
            throw new UnsupportedOperationException();
        }

        @Override
        public IContentProvider getContentProvider() {
            return new ITreeContentProvider() {

                @Override
                public Object[] getElements(Object inputElement) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Object[] getChildren(Object parentElement) {
                    return ((CnATreeElement) parentElement).getChildrenAsArray();
                }

                @Override
                public Object getParent(Object element) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean hasChildren(Object element) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}