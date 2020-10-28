/*******************************************************************************
 * Copyright (c) 2020 Jonas Jordan
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.junit.Assert;
import org.junit.Test;

import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.groups.BpRequirementGroup;

/**
 * Test the {@Link RecursiveTreeFilter}.
 */
public class RecursiveTreeFilterTest {

    @Test
    public void positiveRoot() {
        ViewerFilter delegateFilterMock = mock(ViewerFilter.class);
        Viewer viewerMock = mock(Viewer.class);

        Object parentMock = mock(Object.class);

        BpRequirementGroup elementMock = mock(BpRequirementGroup.class);
        when(delegateFilterMock.select(viewerMock, parentMock, elementMock)).thenReturn(true);

        RecursiveTreeFilter sut = new RecursiveTreeFilter(delegateFilterMock);

        Assert.assertTrue(sut.select(viewerMock, parentMock, elementMock));
        verify(delegateFilterMock, times(1)).select(viewerMock, parentMock, elementMock);
    }

    @Test
    public void negativeRootNoChildren() {
        ViewerFilter delegateFilterMock = mock(ViewerFilter.class);
        StructuredViewer viewerMock = mock(StructuredViewer.class);
        ITreeContentProvider treeContentProviderMock = mock(ITreeContentProvider.class);
        when(viewerMock.getContentProvider()).thenReturn(treeContentProviderMock);

        Object parentMock = mock(Object.class);

        BpRequirementGroup rootMock = mock(BpRequirementGroup.class);
        when(delegateFilterMock.select(viewerMock, parentMock, rootMock)).thenReturn(false);
        when(treeContentProviderMock.getChildren(rootMock)).thenReturn(new Object[0]);

        RecursiveTreeFilter sut = new RecursiveTreeFilter(delegateFilterMock);

        Assert.assertFalse(sut.select(viewerMock, parentMock, rootMock));
        verify(delegateFilterMock, times(1)).select(viewerMock, parentMock, rootMock);
    }

    @Test
    public void negativeRootPositiveChild() {
        ViewerFilter delegateFilterMock = mock(ViewerFilter.class);
        ITreeContentProvider treeContentProviderMock = mock(ITreeContentProvider.class);

        StructuredViewer viewerMock = mock(StructuredViewer.class);
        when(viewerMock.getContentProvider()).thenReturn(treeContentProviderMock);

        Object parentMock = mock(Object.class);

        BpRequirementGroup rootMock = mock(BpRequirementGroup.class);
        when(delegateFilterMock.select(viewerMock, parentMock, rootMock)).thenReturn(false);

        BpRequirement negativeChild = mock(BpRequirement.class);
        when(delegateFilterMock.select(viewerMock, rootMock, negativeChild)).thenReturn(true);

        BpRequirement positiveChild = mock(BpRequirement.class);
        when(delegateFilterMock.select(viewerMock, rootMock, positiveChild)).thenReturn(true);

        when(treeContentProviderMock.getChildren(rootMock))
                .thenReturn(new Object[] { negativeChild, positiveChild });

        RecursiveTreeFilter sut = new RecursiveTreeFilter(delegateFilterMock);

        Assert.assertTrue(sut.select(viewerMock, parentMock, rootMock));
        verify(delegateFilterMock, times(1)).select(viewerMock, parentMock, rootMock);
        verify(delegateFilterMock, times(1)).select(viewerMock, rootMock, negativeChild);
    }

    @Test
    public void negativeRootNegativeChildren() {
        ViewerFilter delegateFilterMock = mock(ViewerFilter.class);
        ITreeContentProvider treeContentProviderMock = mock(ITreeContentProvider.class);

        StructuredViewer viewerMock = mock(StructuredViewer.class);
        when(viewerMock.getContentProvider()).thenReturn(treeContentProviderMock);

        Object parentMock = mock(Object.class);

        BpRequirementGroup rootMock = mock(BpRequirementGroup.class);
        when(delegateFilterMock.select(viewerMock, parentMock, rootMock)).thenReturn(false);

        BpRequirement negativeChild1 = mock(BpRequirement.class);
        when(delegateFilterMock.select(viewerMock, rootMock, negativeChild1)).thenReturn(false);

        BpRequirement negativeChild2 = mock(BpRequirement.class);
        when(delegateFilterMock.select(viewerMock, rootMock, negativeChild2)).thenReturn(false);

        when(treeContentProviderMock.getChildren(rootMock))
                .thenReturn(new Object[] { negativeChild1, negativeChild2 });

        RecursiveTreeFilter sut = new RecursiveTreeFilter(delegateFilterMock);

        Assert.assertFalse(sut.select(viewerMock, parentMock, rootMock));
        verify(delegateFilterMock, times(1)).select(viewerMock, parentMock, rootMock);
    }

    @Test
    public void negativeRootPositveGrandChild() {
        ViewerFilter delegateFilterMock = mock(ViewerFilter.class);
        ITreeContentProvider treeContentProviderMock = mock(ITreeContentProvider.class);

        StructuredViewer viewerMock = mock(StructuredViewer.class);
        when(viewerMock.getContentProvider()).thenReturn(treeContentProviderMock);

        Object parentMock = mock(Object.class);

        BpRequirementGroup rootMock = mock(BpRequirementGroup.class);
        when(delegateFilterMock.select(viewerMock, null, rootMock)).thenReturn(false);

        BpRequirementGroup negativeChild = mock(BpRequirementGroup.class);
        when(delegateFilterMock.select(viewerMock, rootMock, negativeChild)).thenReturn(false);

        BpRequirement positiveGrandChild = mock(BpRequirement.class);
        when(delegateFilterMock.select(viewerMock, negativeChild, positiveGrandChild))
                .thenReturn(true);

        when(treeContentProviderMock.getChildren(rootMock))
                .thenReturn(new Object[] { negativeChild });

        when(treeContentProviderMock.getChildren(negativeChild))
                .thenReturn(new Object[] { positiveGrandChild });

        RecursiveTreeFilter sut = new RecursiveTreeFilter(delegateFilterMock);

        Assert.assertTrue(sut.select(viewerMock, parentMock, rootMock));
        verify(delegateFilterMock, times(1)).select(viewerMock, parentMock, rootMock);
        verify(delegateFilterMock, times(1)).select(viewerMock, rootMock, negativeChild);
    }

    @Test
    public void negativeRootNegativeDescendants() {
        ViewerFilter delegateFilterMock = mock(ViewerFilter.class);
        ITreeContentProvider treeContentProviderMock = mock(ITreeContentProvider.class);

        StructuredViewer viewerMock = mock(StructuredViewer.class);
        when(viewerMock.getContentProvider()).thenReturn(treeContentProviderMock);

        Object parentMock = mock(Object.class);

        BpRequirementGroup rootMock = mock(BpRequirementGroup.class);
        when(delegateFilterMock.select(viewerMock, null, rootMock)).thenReturn(false);

        BpRequirementGroup negativeChild = mock(BpRequirementGroup.class);
        when(delegateFilterMock.select(viewerMock, rootMock, negativeChild)).thenReturn(false);

        BpRequirement negativeGrandChild = mock(BpRequirement.class);
        when(delegateFilterMock.select(viewerMock, negativeChild, negativeGrandChild))
                .thenReturn(false);

        when(treeContentProviderMock.getChildren(rootMock))
                .thenReturn(new Object[] { negativeChild });

        when(treeContentProviderMock.getChildren(negativeChild))
                .thenReturn(new Object[] { negativeGrandChild });

        RecursiveTreeFilter sut = new RecursiveTreeFilter(delegateFilterMock);

        Assert.assertFalse(sut.select(viewerMock, parentMock, rootMock));
        verify(delegateFilterMock, times(1)).select(viewerMock, parentMock, rootMock);
        verify(delegateFilterMock, times(1)).select(viewerMock, rootMock, negativeChild);
        verify(delegateFilterMock, times(1)).select(viewerMock, negativeChild, negativeGrandChild);
    }

    /**
     * All descendants in the hierarchy are negative, but the result should be
     * positive because the lowest level descendants are beyond the maximum
     * depth.
     */
    @Test
    public void negativeRootNegativeDescendantsLimitedDepth() {
        ViewerFilter delegateFilterMock = mock(ViewerFilter.class);
        ITreeContentProvider treeContentProviderMock = mock(ITreeContentProvider.class);

        StructuredViewer viewerMock = mock(StructuredViewer.class);
        when(viewerMock.getContentProvider()).thenReturn(treeContentProviderMock);

        Object parentMock = mock(Object.class);

        BpRequirementGroup rootMock = mock(BpRequirementGroup.class);
        when(delegateFilterMock.select(viewerMock, parentMock, rootMock)).thenReturn(false);

        BpRequirementGroup negativeChild = mock(BpRequirementGroup.class);
        when(delegateFilterMock.select(viewerMock, rootMock, negativeChild)).thenReturn(false);

        BpRequirement negativeGrandChild = mock(BpRequirement.class);
        when(delegateFilterMock.select(viewerMock, negativeChild, negativeGrandChild))
                .thenReturn(false);

        when(treeContentProviderMock.getChildren(rootMock))
                .thenReturn(new Object[] { negativeChild });

        when(treeContentProviderMock.getChildren(negativeChild))
                .thenReturn(new Object[] { negativeGrandChild });

        RecursiveTreeFilter sut = new RecursiveTreeFilter(delegateFilterMock, 1);

        Assert.assertTrue(sut.select(viewerMock, parentMock, rootMock));
        verify(delegateFilterMock, times(1)).select(viewerMock, parentMock, rootMock);
        verify(delegateFilterMock, times(1)).select(viewerMock, rootMock, negativeChild);
        verify(delegateFilterMock, never()).select(viewerMock, negativeChild, negativeGrandChild);
        verify(treeContentProviderMock, never()).getChildren(negativeChild);
    }

    /**
     * All descendants are negative and the result should be negative because
     * the maximum depth is large enough to go through all hierarchy levels.
     */
    @Test
    public void negativeRootNegativeDescendantsSufficientDepth() {
        ViewerFilter delegateFilterMock = mock(ViewerFilter.class);
        ITreeContentProvider treeContentProviderMock = mock(ITreeContentProvider.class);

        StructuredViewer viewerMock = mock(StructuredViewer.class);
        when(viewerMock.getContentProvider()).thenReturn(treeContentProviderMock);

        Object parentMock = mock(Object.class);

        BpRequirementGroup rootMock = mock(BpRequirementGroup.class);
        when(delegateFilterMock.select(viewerMock, parentMock, rootMock)).thenReturn(false);

        BpRequirementGroup negativeChild = mock(BpRequirementGroup.class);
        when(delegateFilterMock.select(viewerMock, rootMock, negativeChild)).thenReturn(false);

        BpRequirement negativeGrandChild = mock(BpRequirement.class);
        when(delegateFilterMock.select(viewerMock, negativeChild, negativeGrandChild))
                .thenReturn(false);

        when(treeContentProviderMock.getChildren(rootMock))
                .thenReturn(new Object[] { negativeChild });

        when(treeContentProviderMock.getChildren(negativeChild))
                .thenReturn(new Object[] { negativeGrandChild });

        RecursiveTreeFilter sut = new RecursiveTreeFilter(delegateFilterMock, 2);

        Assert.assertFalse(sut.select(viewerMock, parentMock, rootMock));
        verify(delegateFilterMock, times(1)).select(viewerMock, parentMock, rootMock);
        verify(delegateFilterMock, times(1)).select(viewerMock, rootMock, negativeChild);
        verify(delegateFilterMock, times(1)).select(viewerMock, negativeChild, negativeGrandChild);
    }
}
