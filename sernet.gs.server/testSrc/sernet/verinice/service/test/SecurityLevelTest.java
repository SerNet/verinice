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
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import sernet.verinice.model.bp.IImplementableSecurityLevelProvider;
import sernet.verinice.model.bp.SecurityLevel;
import sernet.verinice.model.bp.SecurityLevelUtil;

/**
 * Test the {@Link SecurityLevelUtil}.
 */
public class SecurityLevelTest {

    /** BASIC level pending-> expect null. */
    @Test
    public void determinesNoLevel() {
        List<IImplementableSecurityLevelProvider> requirements = new ArrayList<>();

        IImplementableSecurityLevelProvider req = mock(IImplementableSecurityLevelProvider.class);
        when(req.getSecurityLevel()).thenReturn(SecurityLevel.BASIC);
        when(req.getImplementationPending()).thenReturn(false);
        requirements.add(req);

        req = mock(IImplementableSecurityLevelProvider.class);
        when(req.getSecurityLevel()).thenReturn(SecurityLevel.BASIC);
        when(req.getImplementationPending()).thenReturn(true);
        requirements.add(req);

        SecurityLevel result = SecurityLevelUtil.getImplementedSecurityLevel(requirements);

        assertNull(result);
    }

    /** BASIC level implemented, STANDARD level pending-> expect BASIC level. */
    @Test
    public void determinesBasicLevel() {
        List<IImplementableSecurityLevelProvider> requirements = new ArrayList<>();

        IImplementableSecurityLevelProvider req = mock(IImplementableSecurityLevelProvider.class);
        when(req.getSecurityLevel()).thenReturn(SecurityLevel.BASIC);
        when(req.getImplementationPending()).thenReturn(false);
        requirements.add(req);

        req = mock(IImplementableSecurityLevelProvider.class);
        when(req.getSecurityLevel()).thenReturn(SecurityLevel.BASIC);
        when(req.getImplementationPending()).thenReturn(false);
        requirements.add(req);

        req = mock(IImplementableSecurityLevelProvider.class);
        when(req.getSecurityLevel()).thenReturn(SecurityLevel.STANDARD);
        when(req.getImplementationPending()).thenReturn(false);
        requirements.add(req);

        req = mock(IImplementableSecurityLevelProvider.class);
        when(req.getSecurityLevel()).thenReturn(SecurityLevel.STANDARD);
        when(req.getImplementationPending()).thenReturn(true);
        requirements.add(req);

        SecurityLevel result = SecurityLevelUtil.getImplementedSecurityLevel(requirements);

        assertEquals(SecurityLevel.BASIC, result);
    }

    /** BASIC and STANDARD level implemented -> expect STANDARD */
    @Test
    public void determinesStandardLevel() {
        List<IImplementableSecurityLevelProvider> requirements = new ArrayList<>();

        IImplementableSecurityLevelProvider req = mock(IImplementableSecurityLevelProvider.class);
        when(req.getSecurityLevel()).thenReturn(SecurityLevel.BASIC);
        when(req.getImplementationPending()).thenReturn(false);
        requirements.add(req);

        req = mock(IImplementableSecurityLevelProvider.class);
        when(req.getSecurityLevel()).thenReturn(SecurityLevel.STANDARD);
        when(req.getImplementationPending()).thenReturn(false);
        requirements.add(req);

        SecurityLevel result = SecurityLevelUtil.getImplementedSecurityLevel(requirements);

        assertEquals(SecurityLevel.STANDARD, result);
    }

    /** All levels implemented -> expect HIGH */
    @Test
    public void determinesHighLevel() {
        List<IImplementableSecurityLevelProvider> requirements = new ArrayList<>();

        IImplementableSecurityLevelProvider req = mock(IImplementableSecurityLevelProvider.class);
        when(req.getSecurityLevel()).thenReturn(SecurityLevel.BASIC);
        when(req.getImplementationPending()).thenReturn(false);
        requirements.add(req);

        req = mock(IImplementableSecurityLevelProvider.class);
        when(req.getSecurityLevel()).thenReturn(SecurityLevel.STANDARD);
        when(req.getImplementationPending()).thenReturn(false);
        requirements.add(req);

        req = mock(IImplementableSecurityLevelProvider.class);
        when(req.getSecurityLevel()).thenReturn(SecurityLevel.HIGH);
        when(req.getImplementationPending()).thenReturn(false);
        requirements.add(req);

        SecurityLevel result = SecurityLevelUtil.getImplementedSecurityLevel(requirements);

        assertEquals(SecurityLevel.HIGH, result);
    }

    /** STANDARD level implemented, but BASIC pending -> expect null */
    @Test
    public void considersPendingLowerLevel() {
        List<IImplementableSecurityLevelProvider> requirements = new ArrayList<>();

        IImplementableSecurityLevelProvider req = mock(IImplementableSecurityLevelProvider.class);
        when(req.getSecurityLevel()).thenReturn(SecurityLevel.BASIC);
        when(req.getImplementationPending()).thenReturn(true);
        requirements.add(req);

        req = mock(IImplementableSecurityLevelProvider.class);
        when(req.getSecurityLevel()).thenReturn(SecurityLevel.STANDARD);
        when(req.getImplementationPending()).thenReturn(false);
        requirements.add(req);

        SecurityLevel result = SecurityLevelUtil.getImplementedSecurityLevel(requirements);

        assertNull(result);
    }

    /** Only HIGH level implemented -> expect HIGH */
    @Test
    public void skipsMissingLevels() {
        List<IImplementableSecurityLevelProvider> requirements = new ArrayList<>();

        IImplementableSecurityLevelProvider req = mock(IImplementableSecurityLevelProvider.class);
        when(req.getSecurityLevel()).thenReturn(SecurityLevel.HIGH);
        when(req.getImplementationPending()).thenReturn(false);
        requirements.add(req);

        SecurityLevel result = SecurityLevelUtil.getImplementedSecurityLevel(requirements);

        assertEquals(SecurityLevel.HIGH, result);
    }
}
