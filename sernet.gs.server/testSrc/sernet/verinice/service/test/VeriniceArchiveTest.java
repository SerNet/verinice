package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

import junit.framework.Assert;
import sernet.verinice.service.sync.VeriniceArchive;

public class VeriniceArchiveTest {

    @Test
    public void testValidArchive() throws IOException {
        VeriniceArchive archive = null;
        try (InputStream is = VeriniceArchiveTest.class.getResourceAsStream("modplast-1.1.vna")) {
            archive = new VeriniceArchive(is);

            assertEquals("SerNet-DM", archive.getSourceId());
            assertNotNull(archive.getSyncData());
            assertNotNull(archive.getFileData("readme.txt"));

        } finally {
            Optional.ofNullable(archive).ifPresent(VeriniceArchive::clear);
        }
    }

    @Test
    public void queryInvalidFile() throws IOException {
        VeriniceArchive archive = null;
        try (InputStream is = VeriniceArchiveTest.class.getResourceAsStream("modplast-1.1.vna")) {
            archive = new VeriniceArchive(is);
            archive.getFileData("../foo");
            Assert.fail("Access to file outside of the archive should not be allowed");
        }catch(IllegalArgumentException e) {
            assertThat(e.getMessage(), JUnitMatchers.containsString("not contained"));
        } finally {
            Optional.ofNullable(archive).ifPresent(VeriniceArchive::clear);
        }
    }

}
