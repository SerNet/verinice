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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import sernet.gs.service.NumericStringComparator;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.bp.rcp.BaseProtectionTreeSorter;
import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.bp.ISecurityLevelProvider;
import sernet.verinice.model.bp.SecurityLevel;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 *
 */
public class RelationComparator extends ViewerComparator {

    private Set<String> sorterProperties;
    private NumericStringComparator numComp = new NumericStringComparator();
    private BaseProtectionTreeSorter baseProtectionTreeSorter = new BaseProtectionTreeSorter();
    private static HUITypeFactory huiTypeFactory = HUITypeFactory.getInstance();

    public RelationComparator(String... sorterProperties) {
        this.sorterProperties = new HashSet<>(sorterProperties.length);
        Collections.addAll(this.sorterProperties, sorterProperties);
    }

    @Override
    public boolean isSorterProperty(Object element, String property) {
        return sorterProperties.contains(property);
    }

    @Override
    public int compare(Viewer viewer, Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            return 0;
        }

        CnATreeElement elementInQuestion = (CnATreeElement) viewer.getInput();

        CnALink link1 = (CnALink) o1;
        CnALink link2 = (CnALink) o2;

        CnATreeElement linkedElement1 = CnALink.getRelationObject(elementInQuestion, link1);
        CnATreeElement linkedElement2 = CnALink.getRelationObject(elementInQuestion, link2);

        String otherElementTypeId1 = linkedElement1.getTypeId();
        String otherElementTypeId2 = linkedElement2.getTypeId();

        if (elementInQuestion instanceof IBpElement) {
            // sort links to bp elements according to the order in which they
            // appear in the tree
            int category1 = baseProtectionTreeSorter.category(linkedElement1);
            int category2 = baseProtectionTreeSorter.category(linkedElement2);
            if (category1 != category2) {
                return Integer.compare(category1, category2);
            }
        } else {
            // sort links to non-bp elements by the elements' internationalized
            // element type names
            String otherElementTypeName1 = huiTypeFactory.getEntityType(otherElementTypeId1)
                    .getName();
            String otherElementTypeName2 = huiTypeFactory.getEntityType(otherElementTypeId2)
                    .getName();

            int comparisonByLinkedElementTypeNames = otherElementTypeName1
                    .compareTo(otherElementTypeName2);
            if (comparisonByLinkedElementTypeNames != 0) {
                return comparisonByLinkedElementTypeNames;
            }
        }

        // sort by internationalized relation name
        String relationName1 = huiTypeFactory.getRelation(link1.getRelationId()).getName();
        String relationName2 = huiTypeFactory.getRelation(link2.getRelationId()).getName();
        int comparisonByRelationNames = relationName1.compareTo(relationName2);
        if (comparisonByRelationNames != 0) {
            return comparisonByRelationNames;
        }

        if (linkedElement1 instanceof ISecurityLevelProvider
                && linkedElement2 instanceof ISecurityLevelProvider) {
            // sort by security level
            SecurityLevel securityLevel1 = ((ISecurityLevelProvider) linkedElement1)
                    .getSecurityLevel();
            SecurityLevel securityLevel2 = ((ISecurityLevelProvider) linkedElement2)
                    .getSecurityLevel();
            int comparisonBySecurityLevel = SecurityLevel.compare(securityLevel1, securityLevel2);
            if (comparisonBySecurityLevel != 0) {
                return comparisonBySecurityLevel;
            }
        }

        // as a last resort, sort by title
        String title1 = RelationViewLabelProvider
                .getLinkTargetTitleIncludingPotentialIdentifier(elementInQuestion, link1);
        String title2 = RelationViewLabelProvider
                .getLinkTargetTitleIncludingPotentialIdentifier(elementInQuestion, link2);

        return numComp.compare(title1, title2);
    }
}
