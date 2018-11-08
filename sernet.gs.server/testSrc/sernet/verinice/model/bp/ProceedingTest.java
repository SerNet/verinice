package sernet.verinice.model.bp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ProceedingTest {

    @Test
    public void testRequirements() {
        assertTrue("basic only requires basic",
                Proceeding.BASIC.requires(SecurityLevel.BASIC));
        assertFalse("basic only requires basic",
                Proceeding.BASIC.requires(SecurityLevel.STANDARD));
        assertFalse("basic only requires basic",
                Proceeding.BASIC.requires(SecurityLevel.HIGH));
        assertTrue("every proceeding shall require unedited(null)",
                Proceeding.BASIC.requires(null));

        assertTrue("standard requires everything",
                Proceeding.STANDARD.requires(SecurityLevel.BASIC));
        assertTrue("standard requires everything",
                Proceeding.STANDARD.requires(SecurityLevel.STANDARD));
        assertTrue("standard requires everything",
                Proceeding.STANDARD.requires(SecurityLevel.HIGH));
        assertTrue("every proceeding shall require unedited(null)",
                Proceeding.STANDARD.requires(null));

        assertTrue("core requires everything",
                Proceeding.CORE.requires(SecurityLevel.BASIC));
        assertTrue("core requires everything",
                Proceeding.CORE.requires(SecurityLevel.STANDARD));
        assertTrue("core requires everything",
                Proceeding.CORE.requires(SecurityLevel.HIGH));
        assertTrue("every proceeding shall require unedited(null)", Proceeding.CORE.requires(null));
    }
}
